package com.example.demo.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatPostCommand(@NotBlank String title, @NotBlank String content) {
}
