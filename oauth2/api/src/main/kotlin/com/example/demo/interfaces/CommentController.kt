package com.example.demo.interfaces

import com.example.demo.domain.exception.CommentNotFoundException
import com.example.demo.domain.model.Comment
import com.example.demo.domain.repository.CommentRepository
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/comments")
class CommentController(
    private val comments: CommentRepository
) {

    @GetMapping("/{id}")
    suspend fun getComment(@PathVariable id: UUID): ResponseEntity<Comment> {
        val comment = comments.findById(id) ?: throw CommentNotFoundException(id)
        return ok(comment)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteComment(@PathVariable id: UUID): ResponseEntity<Any> {
        if (!comments.existsById(id)) throw CommentNotFoundException(id)
        comments.deleteById(id)
        return noContent().build()
    }
}