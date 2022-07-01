package com.example.demo.interfaces.dto

import javax.validation.constraints.NotBlank

data class CreateCommentCommand(
    @NotBlank val content: String
)
