package com.example.demo.domain.repository;

import com.example.demo.domain.model.Post;
import com.example.demo.domain.model.Status;
import com.example.demo.interfaces.dto.PostSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository {
    Flux<PostSummary> findByKeyword(String keyword, int offset, int limit);

    Mono<Long> countByKeyword(String keyword);

    Mono<Post> findById(String id);

    Mono<Post> create(String title, String content);

    Mono<Long> update(String id, String title, String content);

    Mono<Long> updateStatus(String id, Status status);

    Mono<Long> deleteById(String id);

    Mono<Long> addComment(String id, String content);

    Mono<Long> removeComment(String id, String commentId);
}
