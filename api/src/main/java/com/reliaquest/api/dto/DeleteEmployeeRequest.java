package com.reliaquest.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public class DeleteEmployeeRequest {

    @NotBlank
    private String name;
}
