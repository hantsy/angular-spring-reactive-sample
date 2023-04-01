package com.example.demo.interfaces.dto;

import java.time.LocalDateTime;

public record PostSummary(String id, String title, LocalDateTime createdAt) {
}
