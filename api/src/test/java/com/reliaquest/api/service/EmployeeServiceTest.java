package com.reliaquest.api.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.reliaquest.api.dto.AddEmployeeRequest;
import com.reliaquest.api.dto.Response;
import com.reliaquest.api.exception.EmployeeNotFound;
import com.reliaquest.api.exception.FailureException;
import com.reliaquest.api.exception.InvalidDataException;
import com.reliaquest.api.exception.RemoteAccessException;
import com.reliaquest.api.helpers.TestDataProvider;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.remote.RemoteClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {


    private RemoteClient remoteClient;
    private Cache<String, Employee> mockCache;

    private EmployeeService employeeService;

    @BeforeEach
    void setup() {
        remoteClient = mock(RemoteClient.class);
        mockCache = CacheBuilder.newBuilder().build();

        employeeService = new EmployeeService(remoteClient, mockCache);
    }
    @Test
    void getAllEmployees_success() {
        List<Employee> mockList = TestDataProvider.getMockEmployees();
        Response<List<Employee>> response =new Response<>(mockList);

        when(remoteClient.getList(any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        assertEquals(mockList.get(0).getName(), result.get(0).getName());
    }

    @Test
    void getAllEmployees_fallbackToCache() {
        List<Employee> employees = TestDataProvider.getMockEmployees();

        mockCache.put(employees.get(0).getId().toString(), employees.get(0));
        when(remoteClient.getList(any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Error"));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void getEmployeeByName_success() {
        List<Employee> employees = TestDataProvider.getMockEmployees();
        Employee emp = employees.get(0);
        Response<List<Employee>> response =new Response<>(List.of(emp));

        when(remoteClient.getList(any(), any(), any())).thenReturn(ResponseEntity.ok(response));

        List<Employee> result = employeeService.getEmployeeByName("John");

        assertEquals(1, result.size());
        assertEquals(employees.get(0).getName(), result.get(0).getName());
    }

    @Test
    void getEmployeeByName_invalidInput() {
        assertThrows(InvalidDataException.class, () -> employeeService.getEmployeeByName("123@"));
    }

    @Test
    void getEmployeeByName_notFound() {
        Response<List<Employee>> response =new Response<>(Collections.emptyList());
        when(remoteClient.getList(any(), any(), any())).thenReturn(ResponseEntity.ok(response));
        assertThrows(EmployeeNotFound.class, () -> employeeService.getEmployeeByName("Ghost"));
    }

    @Test
    void getEmployeeById_success() {
        List<Employee> employees = TestDataProvider.getMockEmployees();
        Employee emp = employees.get(0);
        Response<Employee> response =new Response<>(emp);

        when(remoteClient.get(eq(emp.getId().toString()), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        Employee result = employeeService.getEmployeeById(emp.getId().toString());

        assertEquals("John Doe", result.getName());
    }

    @Test
    void getEmployeeById_invalidUUID() {
        assertThrows(InvalidDataException.class, () -> employeeService.getEmployeeById("not-a-uuid"));
    }

    @Test
    void getEmployeeById_fallbackToCache() {
        List<Employee> employees = TestDataProvider.getMockEmployees();

        mockCache.put(employees.get(0).getId().toString(), employees.get(0));

        when(remoteClient.get(any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("error"));

        Employee result = employeeService.getEmployeeById(employees.get(0).getId().toString());

        assertEquals("John Doe", result.getName());
    }

    @Test
    void getEmployeeById_notFound() {
        UUID id = UUID.randomUUID();
        when(remoteClient.get(eq(id.toString()), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThrows(EmployeeNotFound.class, () -> employeeService.getEmployeeById(id.toString()));
    }

    @Test
    void getTopSalary_success() {
        List<Employee> employees = TestDataProvider.getMockEmployees();
        Response<List<Employee>> response =new Response<>(List.of(employees.get(0), employees.get(1)));

        when(remoteClient.getList(any(), any(), any())).thenReturn(ResponseEntity.ok(response));

        Integer topSalary = employeeService.getTopSalary();
        assertEquals(400000, topSalary);
    }

    @Test
    void getTopSalary_emptyList() {
        Response<List<Employee>> response =new Response<>(Collections.emptyList());
        when(remoteClient.getList(any(), any(), any())).thenReturn(ResponseEntity.ok(response));
        assertThrows(EmployeeNotFound.class, () -> employeeService.getTopSalary());
    }

    @Test
    void getTopTenEarnerEmployee_success() {
        List<Employee> employees = TestDataProvider.getAllMockEmployees();
        Response<List<Employee>> response =new Response<>(employees);
        when(remoteClient.getList(any(), any(), any())).thenReturn(ResponseEntity.ok(response));

        List<String> topTen = employeeService.getTopTenEarnerEmployee();
        assertEquals(10, topTen.size());
    }

    @Test
    void addEmployee_success() {

        AddEmployeeRequest request = new AddEmployeeRequest("Tom", 40000,30, "Dev");
        UUID newId = UUID.randomUUID();
        Employee emp = new Employee(newId, "Rob",20000, 30, "HR", "hr@gmail.com");

        Response<Employee> response =new Response<>(emp);

        when(remoteClient.post(any(), eq(request), any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(response));

        UUID result = employeeService.addEmployee(request);

        assertEquals(newId, result);
    }

    @Test
    void addEmployee_failure_nullResponse() {
        AddEmployeeRequest request = new AddEmployeeRequest("Tom", 40000,30, "Dev");
        when(remoteClient.post(any(), eq(request), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThrows(FailureException.class, () -> employeeService.addEmployee(request));
    }

    @Test
    void addEmployee_remoteException() {
        AddEmployeeRequest request = new AddEmployeeRequest("Tom", 40000,30, "Dev");
        when(remoteClient.post(any(), eq(request), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("error"));

        assertThrows(RemoteAccessException.class, () -> employeeService.addEmployee(request));
    }

    @Test
    void removeEmployee_success() {
        UUID id = UUID.randomUUID();
        Employee emp = new Employee(id, "Rob",20000, 30, "HR", "hr@gmail.com");
        mockCache.put(id.toString(), emp);
        Response<Employee> response =new Response<>(emp);

        when(remoteClient.get(eq(id.toString()), any(), any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(response));
        when(remoteClient.delete(any(), any(), any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(new Response<>(true)));

        String name = employeeService.removeEmployee(id.toString());

        assertEquals("Rob", name);
        assertNull(mockCache.getIfPresent(id.toString()));
    }

    @Test
    void removeEmployee_failure() {
        UUID id = UUID.randomUUID();
        Employee emp = new Employee(id, "Rob",20000, 30, "HR", "hr@gmail.com");
        mockCache.put(id.toString(), emp);
        Response<Employee> response = new Response<>(emp);
        when(remoteClient.get(eq(id.toString()), any(), any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(response));
        when(remoteClient.delete(any(), any(),any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(new Response<>(false)));

        String name = employeeService.removeEmployee(id.toString());

        assertNull(name);
        assertNotNull(mockCache.getIfPresent(id.toString())); // still there
    }
}
