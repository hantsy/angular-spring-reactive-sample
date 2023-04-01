package com.example.demo.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostCommand(@NotBlank String title, @NotBlank String content) {
}
