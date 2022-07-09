package com.example.demo.domain.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String id) {
        super("Comment #" + id + " was not found.");
    }
}
