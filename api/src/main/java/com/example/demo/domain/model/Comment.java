package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable {

    @Id
    private String id;

    @NotBlank
    private String content;

    @CreatedDate
    private LocalDateTime createdDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @LastModifiedBy
    private String lastModifiedBy;

}
