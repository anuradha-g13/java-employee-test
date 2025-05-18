package com.reliaquest.api.controller;


import com.reliaquest.api.dto.AddEmployeeRequest;
import com.reliaquest.api.helpers.TestDataProvider;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @Test
    void testGetAllEmployees_withEmployees() {
        List<Employee> employees = TestDataProvider.getMockEmployees();
        when(employeeService.getAllEmployees()).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void testGetAllEmployees_emptyList() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void testGetAllEmployees_serviceThrowsException() {
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Service error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeController.getAllEmployees();
        });

        assertEquals("Service error", exception.getMessage());
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void testGetEmployeesByNameSearch_returnsEmployees() {
        String searchString = "John";
        List<Employee> employees = TestDataProvider.getMockEmployees();
        when(employeeService.getEmployeeByName(searchString)).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch(searchString);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getName());
        assertEquals("John Ji", response.getBody().get(1).getName());
        verify(employeeService, times(1)).getEmployeeByName(searchString);
    }

    @Test
    void testGetEmployeesByNameSearch_returnsNotFoundForEmptyList() {
        String searchString = "Unknown";
        when(employeeService.getEmployeeByName(searchString)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch(searchString);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(employeeService, times(1)).getEmployeeByName(searchString);
    }

    @Test
    void testGetEmployeesByNameSearch_returnsNotFoundForNullResponse() {
        String searchString = "Ghost";
        when(employeeService.getEmployeeByName(searchString)).thenReturn(null);

        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch(searchString);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(employeeService, times(1)).getEmployeeByName(searchString);
    }

    @Test
    void testGetEmployeeById_returnsEmployee() {
        Employee employee = TestDataProvider.getMockEmployees().get(0);
        when(employeeService.getEmployeeById(employee.getId().toString())).thenReturn(employee);

        ResponseEntity<Employee> response = employeeController.getEmployeeById(employee.getId().toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getName());
        verify(employeeService, times(1)).getEmployeeById(employee.getId().toString());
    }

    @Test
    void testGetEmployeeById_notFound() {
        String id = "999";
        when(employeeService.getEmployeeById(id)).thenReturn(null);

        ResponseEntity<Employee> response = employeeController.getEmployeeById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(employeeService, times(1)).getEmployeeById(id);
    }

    @Test
    void testGetHighestSalaryOfEmployees_success() {
        when(employeeService.getTopSalary()).thenReturn(100000);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(100000, response.getBody());
    }

    @Test
    void testGetHighestSalaryOfEmployees_nullResponse() {
        when(employeeService.getTopSalary()).thenReturn(null);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(0, response.getBody());
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames_success() {
        List<String> names = Arrays.asList("John", "Alice", "Bob");
        when(employeeService.getTopTenEarnerEmployee()).thenReturn(names);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames_emptyList() {
        when(employeeService.getTopTenEarnerEmployee()).thenReturn(Collections.emptyList());

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).isEmpty());
    }

    @Test
    void testCreateEmployee_success() {
        AddEmployeeRequest request = new AddEmployeeRequest("Alice",40000, 30, "Engineer");
        UUID empId = UUID.randomUUID();

        when(employeeService.addEmployee(request)).thenReturn(empId);

        ResponseEntity<?> response = employeeController.createEmployee(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains(empId.toString()));
    }

    @Test
    void testCreateEmployee_failure() {
        AddEmployeeRequest request = new AddEmployeeRequest("Alice",0, 30, "Engineer");

        when(employeeService.addEmployee(request)).thenReturn(null);

        ResponseEntity<?> response = employeeController.createEmployee(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unable to add Employee", response.getBody());
    }

    @Test
    void testDeleteEmployeeById_success() {
        String empId = "123";
        when(employeeService.removeEmployee(empId)).thenReturn("Alice");

        ResponseEntity<String> response = employeeController.deleteEmployeeById(empId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("All Employee deleted with : name Alice", response.getBody());
    }

    @Test
    void testDeleteEmployeeById_failure() {
        String empId = "999";
        when(employeeService.removeEmployee(empId)).thenReturn(null);

        ResponseEntity<String> response = employeeController.deleteEmployeeById(empId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unable to delete Employee with ID: 999", response.getBody());
    }

}
