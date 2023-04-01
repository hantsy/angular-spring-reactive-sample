package com.example.demo.infrastructure.persistence;

import com.example.demo.domain.model.Comment;
import com.example.demo.domain.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MongoCommentRepository implements CommentRepository {
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Comment> findById(String id) {
        return mongoTemplate.findById(id, Comment.class);
    }

    @Override
    public Mono<Boolean> update(String id, String content) {
        return mongoTemplate.update(Comment.class)
                .matching(query(where("id").is(id)))
                .apply(Update.update("content", content))
                .first()
                .map(it -> it.getModifiedCount() > 0);
    }
}
