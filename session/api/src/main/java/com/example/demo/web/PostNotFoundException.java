package com.example.demo.web;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String id) {
        super("Post:" + id +" is not found.");
    }
}
