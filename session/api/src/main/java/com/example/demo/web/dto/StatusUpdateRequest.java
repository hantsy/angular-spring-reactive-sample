package com.example.demo.web.dto;

import javax.validation.constraints.NotBlank;

public record StatusUpdateRequest(@NotBlank String status) {
}
