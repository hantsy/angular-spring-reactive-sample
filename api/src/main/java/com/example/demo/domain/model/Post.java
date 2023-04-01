package com.example.demo.domain.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.example.demo.domain.model.Status.DRAFT;

@Document
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post implements Serializable {

    @Id
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @Builder.Default
    private Status status = DRAFT;

    @DocumentReference
    List<Comment> comments = Collections.emptyList();

    @CreatedDate
    private LocalDateTime createdDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @LastModifiedBy
    private String lastModifiedBy;

}
