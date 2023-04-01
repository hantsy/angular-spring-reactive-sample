package com.example.demo.domain.repository;

import com.example.demo.domain.model.Comment;
import reactor.core.publisher.Mono;

public interface CommentRepository {

    Mono<Comment> findById(String id);

    Mono<Boolean> update(String id, String content);

}
