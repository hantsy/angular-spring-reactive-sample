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

    Mono<Boolean> update(String id, String title, String content);

    Mono<Boolean> updateStatus(String id, Status status);

    Mono<Boolean> deleteById(String id);

    Mono<Long> deleteAll();

    Mono<Boolean> addComment(String id, String content);

    Mono<Boolean> removeComment(String id, String commentId);
}
