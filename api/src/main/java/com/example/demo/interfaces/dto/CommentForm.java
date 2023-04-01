package com.example.demo.interfaces.dto;

import javax.validation.constraints.NotBlank;

public record CommentForm(@NotBlank String content) {
}
