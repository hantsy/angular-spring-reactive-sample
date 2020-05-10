package com.example.demo;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

import static com.example.demo.Post.Status.DRAFT;

@Document
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post implements PersistentEntity, Serializable {

    @Id
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @Builder.Default
    private Status status = DRAFT;

    //@CreatedDate
    //@Builder.Default
    private LocalDateTime createdDate;

    //@CreatedBy
    private Username createdBy;

    //@LastModifiedDate
    private LocalDateTime lastModifiedDate;

    //@LastModifiedBy
    private Username lastModifiedBy;

    enum Status {
        DRAFT,
        PUBLISHED
    }
}
