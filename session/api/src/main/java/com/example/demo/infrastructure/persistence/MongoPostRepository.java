package com.example.demo.infrastructure.persistence;

import com.example.demo.domain.model.Comment;
import com.example.demo.domain.model.Post;
import com.example.demo.domain.model.Status;
import com.example.demo.domain.repository.PostRepository;
import com.example.demo.interfaces.dto.PostSummary;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MongoPostRepository implements PostRepository {
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Flux<PostSummary> findByKeyword(String keyword, int offset, int limit) {
        return mongoTemplate
                .find(
                        query(where("title").regex(keyword, "i"))
                                .skip(offset)
                                .limit(limit),
                        Post.class
                )
                .map(it -> new PostSummary(it.getId(), it.getTitle(), it.getCreatedDate()));
    }

    @Override
    public Mono<Long> countByKeyword(String keyword) {
        return mongoTemplate.count(query(where("title").regex(keyword, "i")), Post.class);
    }

    @Override
    public Mono<Post> findById(String id) {
        return mongoTemplate.findById(id, Post.class);
    }

    @Override
    public Mono<Post> create(String title, String content) {
        return mongoTemplate.insert(Post.builder().title(title).content(content).build());
    }

    @Override
    public Mono<Long> update(String id, String title, String content) {
        return mongoTemplate.update(Post.class)
                .matching(where("id").is(id))
                .apply(Update.update("title", title).set("content", content))
                .first()
                .map(UpdateResult::getModifiedCount);
    }

    @Override
    public Mono<Long> updateStatus(String id, Status status) {
        return mongoTemplate.update(Post.class)
                .matching(where("id").is(id))
                .apply(Update.update("status", status))
                .first()
                .map(UpdateResult::getModifiedCount);
    }

    @Override
    public Mono<Long> deleteById(String id) {
        return mongoTemplate.remove(Post.class)
                .matching(where("id").is(id))
                .all()
                .map(DeleteResult::getDeletedCount);
    }

    @Override
    public Mono<Long> addComment(String id, String content) {
        var comment = mongoTemplate.insert(Comment.builder().content(content).build());
        return comment.flatMap(c -> mongoTemplate.update(Post.class)
                .matching(where("id").is(id))
                .apply(new Update().push("comments", c))
                .first()
                .map(UpdateResult::getModifiedCount)
        );
    }

    @Override
    public Mono<Long> removeComment(String id, String commentId) {
        var comment = mongoTemplate.findById(commentId, Comment.class);
        return comment.flatMap(c -> mongoTemplate.update(Post.class)
                .matching(where("id").is(id))
                .apply(new Update().pull("comments", c))
                .first()
                .map(UpdateResult::getModifiedCount)
        );
    }
}
