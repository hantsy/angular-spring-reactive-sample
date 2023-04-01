package com.example.demo.domain.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String id) {
        super("Post #" + id + " was not found.");
    }
}
