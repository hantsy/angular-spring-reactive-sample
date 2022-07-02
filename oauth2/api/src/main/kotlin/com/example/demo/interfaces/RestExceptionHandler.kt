package com.example.demo.interfaces

import com.example.demo.domain.exception.CommentNotFoundException
import com.example.demo.domain.exception.PostNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(
        value = [
            PostNotFoundException::class,
            CommentNotFoundException::class
        ]
    )
    fun handleNotFoundException(ex: Exception): ResponseEntity<Any> {
        return ResponseEntity.notFound().build()
    }
}