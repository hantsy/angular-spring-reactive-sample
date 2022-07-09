package com.example.demo.interfaces.dto;

import javax.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username,
                           @NotBlank String password) {
}
