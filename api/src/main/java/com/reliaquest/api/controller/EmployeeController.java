package com.reliaquest.api.controller;

import com.reliaquest.api.dto.AddEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/client/employees")
public class EmployeeController implements IEmployeeController<Employee, AddEmployeeRequest>{

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Fetching All Employees");
        List<Employee> allEmp = employeeService.getAllEmployees();
        return ResponseEntity.ok(allEmp);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch( String searchString) {
        log.info("Searching employees by name: {}", searchString);
        List<Employee> employees = employeeService.getEmployeeByName(searchString);
        if(employees == null || employees.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(employees);
        }
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("Getting employee by id: {}", id);
        Employee employee = employeeService.getEmployeeById(id);
        if(employee == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Getting highest salary of employees");
        Integer topSalary = employeeService.getTopSalary();
        if(topSalary == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
        return ResponseEntity.ok(topSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Getting top 10 highest earning employee names");
        List<String> topEarners = employeeService.getTopTenEarnerEmployee();
        if(topEarners == null || topEarners.isEmpty()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(topEarners);
        }
        return ResponseEntity.ok(topEarners);
    }

    @Override
    public ResponseEntity createEmployee(@RequestBody AddEmployeeRequest employeeInput) {
        log.info("Adding employee with data: {}", employeeInput);
        UUID empId = employeeService.addEmployee(employeeInput);
        if(empId == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to add Employee");
        }
        log.info("Employee added successfully, ID {} ",empId);
        return ResponseEntity.ok("Employee added with ID "+empId);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        log.info("Deleting employee with id: {}", id);
        String empName = employeeService.removeEmployee(id);

        if(empName == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete Employee with ID: "+id);
        }
        log.info("All Employee deleted with Name {} ",empName);
        return ResponseEntity.ok("All Employee deleted with : name "+empName);
    }
}
