package com.reliaquest.api.helpers;

import com.reliaquest.api.model.Employee;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestDataProvider {

    public static List<Employee> getMockEmployees(){
        return Arrays.asList(
                new Employee(UUID.randomUUID(), "John Doe",400000, 30, "Engineer","djhon@gmail.com" ),
                new Employee(UUID.randomUUID(), "John Ji",400000, 30, "Engineer","djhonji@gmail.com" )
        );
    }
    public static List<Employee> getAllMockEmployees(){
        return List.of(
                new Employee(UUID.randomUUID(), "Roxana Fritsch", 270738, 29, "Dynamic Administration Agent", "fixsan@company.com"),
                new Employee(UUID.randomUUID(), "Darryl Beier", 294248, 68, "Mining Executive", "tempsoft@company.com"),
                new Employee(UUID.randomUUID(), "Floyd Wiegand", 310861, 16, "Global Advertising Facilitator", "sonsing@company.com"),
                new Employee(UUID.randomUUID(), "Renaldo Hyatt", 58745, 45, "Consulting Designer", "hatity@company.com"),
                new Employee(UUID.randomUUID(), "Byron Hoeger", 409797, 60, "Retail Consultant", "pannier@company.com"),
                new Employee(UUID.randomUUID(), "Bessie Wintheiser IV", 52009, 66, "Administration Strategist", "alphazap@company.com"),
                new Employee(UUID.randomUUID(), "Phyliss Ruecker", 426464, 21, "Customer Marketing Strategist", "cosmococo@company.com"),
                new Employee(UUID.randomUUID(), "Rebecca Ziemann IV", 34772, 34, "Internal Advertising Strategist", "magik_mike@company.com"),
                new Employee(UUID.randomUUID(), "Leo Funk", 311999, 41, "Consulting Facilitator", "keepitdope_joe@company.com"),
                new Employee(UUID.randomUUID(), "Dr. Agueda Lowe", 449504, 65, "Accounting Designer", "ventosanzap@company.com"),
                new Employee(UUID.randomUUID(), "Harold Zemlak", 79016, 29, "Future Healthcare Agent", "holdlamis@company.com")
        );
    }

}
