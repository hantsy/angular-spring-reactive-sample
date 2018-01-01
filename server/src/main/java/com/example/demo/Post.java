package com.example.demo;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

    @Id
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @CreatedDate
    private LocalDateTime createdDate;

    @CreatedBy
    private Username author;
}
