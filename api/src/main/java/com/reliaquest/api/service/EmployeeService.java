package com.reliaquest.api.service;

import com.google.common.cache.Cache;
import com.reliaquest.api.config.CacheConfig;
import com.reliaquest.api.dto.AddEmployeeRequest;
import com.reliaquest.api.dto.DeleteEmployeeRequest;
import com.reliaquest.api.dto.DeleteMockEmployeeInput;
import com.reliaquest.api.dto.Response;
import com.reliaquest.api.exception.EmployeeNotFound;
import com.reliaquest.api.exception.FailureException;
import com.reliaquest.api.exception.InvalidDataException;
import com.reliaquest.api.exception.RemoteAccessException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.remote.RemoteClient;
import com.reliaquest.api.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmployeeService {

    private final RemoteClient remoteClient;
    private final Cache<String, Employee> employeeCache;

    @Autowired
    public EmployeeService(RemoteClient remoteClient,
                           Cache<String, Employee> employeeCache) {
        this.remoteClient = remoteClient;
        this.employeeCache = employeeCache;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        try {
            ResponseEntity<Response<List<Employee>>> response = remoteClient.getList("", null,
                    new ParameterizedTypeReference<Response<List<Employee>>>() {
                    });
            if(response.getBody() == null){
                return Collections.emptyList();
            }
            employees = Objects.requireNonNull(response.getBody()).getData();
            log.debug("Fetched {} employees from external service", employees != null ? employees.size() : 0);
            return employees != null ? employees : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch employees from external service {}, returning from cache", e.getMessage());
            employees.addAll(employeeCache.asMap().values());
            return employees;
        }
    }

    public List<Employee> getEmployeeByName(String searchString) {
        if (!ValidationUtil.validateString(searchString)) {
            throw new InvalidDataException("Invalid search name provided");
        }
        List<Employee> allEmployees = getAllEmployees();
        List<Employee> employees = allEmployees.stream().filter(employee ->
                employee.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
        if (employees.isEmpty()) {
            throw new EmployeeNotFound("No employee found with name " + searchString);
        }
        return employees;
    }

    public Employee getEmployeeById(String id) {
        if (!ValidationUtil.isValidUUID(id)) {
            throw new InvalidDataException("Invalid Emp id provided");
        }
        Employee employee = null;
        try {
            ResponseEntity<Response<Employee>> response = remoteClient.get(id, null,
                    new ParameterizedTypeReference<Response<Employee>>() {
                    });
            if(response.getBody()!=null){
                employee = response.getBody().getData();
                log.debug("Fetched {} employee from external service", employee);
            }
        } catch (RestClientException e) {
            log.error("Failed to fetch employees from external service {}, returning from cache", e.getMessage());
            employee = employeeCache.getIfPresent(id);
        }
        if (employee == null) {
            throw new EmployeeNotFound("No employee found with id " + id);
        }
        return employee;
    }

    public Integer getTopSalary() {
        List<Employee> allEmployees = getAllEmployees();
        Optional<Employee> topEarner = allEmployees.stream()
                .max(Comparator.comparingDouble(Employee::getSalary));
        if (topEarner.isPresent()) {
            return topEarner.get().getSalary();
        }
        throw new EmployeeNotFound("NO employee present");
    }

    public List<String> getTopTenEarnerEmployee() {
        List<Employee> allEmployees = getAllEmployees();
        return allEmployees.stream()
                .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());
    }

    public UUID addEmployee(AddEmployeeRequest employeeInput) {
        if (!validateEmployeeInformation(employeeInput)) {
            throw new InvalidDataException("Invalid Field");
        }
        try {
            Response<Employee> employee = remoteClient.post("", employeeInput, new ParameterizedTypeReference<Response<Employee>>() {
            }).getBody();
            if(employee == null){
                log.info("error adding employee");
                throw new FailureException("failed to added employee");
            }
            log.info("Employee added successfully: {}", employee);
            employeeCache.put(employee.getData().getId().toString(), employee.getData());
            return employee.getData().getId();
        } catch (RestClientException e) {
            log.error("Exception occurred while calling remote client,{}", e.getMessage());
            throw new RemoteAccessException("Exception occurred while calling remote service , " + e.getMessage());
        }
    }

    public String removeEmployee(String id) {
        String employeeName = getEmployeeById(id).getName();
        DeleteMockEmployeeInput deleteRequest = new DeleteMockEmployeeInput(employeeName);
        Response<Boolean> success = remoteClient.delete("", deleteRequest, new ParameterizedTypeReference<Response<Boolean>>() {
        }).getBody();
        if(success !=null && Boolean.TRUE.equals(success.getData())){
            employeeCache.invalidate(id);
            log.info("Employee with ID {} , and name {} deleted successfully",id, employeeName);
            return employeeName;
        }else {
            log.info("failed to delete employee with ID {}",id);
            return null;
        }
    }

    @Scheduled(fixedRateString = "${employee.cache.refresh-rate-ms:300000}")
    public void refreshCache() {
        try {
            List<Employee> employees = getAllEmployees();
            if (employees != null) {
                employees.forEach(emp -> employeeCache.put(emp.getId().toString(), emp));
                log.info("Cache refreshed with {} employees", employees.size());
            } else {
                log.warn("Failed to refresh employee cache, No employees fetched");
            }
        } catch (Exception e) {
            log.warn("Failed to refresh employee cache", e);
        }
    }

    private boolean validateEmployeeInformation(AddEmployeeRequest employeeInput) {
        return ValidationUtil.validateString(employeeInput.getName());
    }
}
