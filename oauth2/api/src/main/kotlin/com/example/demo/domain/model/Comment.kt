package com.example.demo.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table(value = "comments")
data class Comment(
    @Id
    @Column("id")
    val id: UUID? = null,

    @Column("content")
    var content: String,

    @Column("post_id")
    val postId: UUID,

    @Column("created_at")
    @CreatedDate
    val createdAt: LocalDateTime? = null,

    @Column("updated_at")
    @LastModifiedDate
    val updatedAt: LocalDateTime? = null,

    @Column("created_by")
    @CreatedBy
    val createdBy: String? = null,

    @Column("version")
    @Version
    @JsonIgnore
    val version: Long? = null,
)