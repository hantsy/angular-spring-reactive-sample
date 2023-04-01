package com.example.demo.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentForm(@NotBlank String content) {
}
