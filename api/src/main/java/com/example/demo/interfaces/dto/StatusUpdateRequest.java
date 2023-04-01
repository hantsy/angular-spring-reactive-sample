package com.example.demo.interfaces.dto;

import javax.validation.constraints.NotBlank;

public record StatusUpdateRequest(@NotBlank String status) {
}
