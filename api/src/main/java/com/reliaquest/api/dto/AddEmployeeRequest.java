package com.reliaquest.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class AddEmployeeRequest {
    @NonNull
    @NotBlank(message = "Name must not be blank")
    @Size(max = 80, message = "Name must not exceed 80 characters")
    private String name;

    @Positive
    @NotBlank(message = "Salary must not be blank")
    private Integer salary;

    @Min(16)
    @Max(75)
    @NotBlank
    private Integer age;

    @NonNull
    @NotBlank(message = "title must not be blank")
    private String title;

}
