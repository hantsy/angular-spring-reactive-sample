package com.example.demo.domain.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.example.demo.domain.model.Post.Status.DRAFT;

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
    private Username createdBy;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @LastModifiedBy
    private Username lastModifiedBy;

    public Post addComment(Comment c) {
        this.comments.add(c);
        return this;
    }

    public Post removeComment(Comment c) {
        this.comments.removeIf(comment -> comment.getId().equals(c.getId()));
        return this;
    }

    public enum Status {
        DRAFT,
        PENDING_MODERATED,
        PUBLISHED
    }
}
