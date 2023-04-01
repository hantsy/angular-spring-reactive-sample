package com.example.demo.interfaces.dto;

import java.util.List;

public record PaginatedResult<T>(List<T> data, Long count) {
}
