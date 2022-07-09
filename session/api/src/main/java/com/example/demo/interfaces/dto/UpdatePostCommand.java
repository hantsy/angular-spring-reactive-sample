package com.example.demo.interfaces.dto;

import javax.validation.constraints.NotBlank;

public record UpdatePostCommand(@NotBlank String title, @NotBlank String content) {
}
