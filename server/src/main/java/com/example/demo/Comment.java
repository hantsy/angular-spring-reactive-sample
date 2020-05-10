package com.example.demo;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements PersistentEntity, Serializable {

    @Id
    private String id;

    @NotBlank
    private String content;

    private PostId post;

    private LocalDateTime createdDate;

    private Username createdBy;

    private LocalDateTime lastModifiedDate;

    private Username lastModifiedBy;

}
