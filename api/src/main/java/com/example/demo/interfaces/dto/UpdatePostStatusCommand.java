package com.example.demo.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostStatusCommand(@NotBlank String status) {
}
