package com.example.demo.web.dto;

import javax.validation.constraints.NotBlank;

public record CreatPostCommand(@NotBlank String title, @NotBlank String content) {
}
