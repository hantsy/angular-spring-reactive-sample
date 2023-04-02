package com.example.demo;

import com.example.demo.domain.model.Post;
import com.example.demo.domain.repository.PostRepository;
import com.example.demo.infrastructure.persistence.MongoPostRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Slf4j
@Testcontainers
public class PostRepositoryTest {

    @TestConfiguration
    @Import({MongoPostRepository.class})
    static class TestConfig {
    }

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4");

    @DynamicPropertySource
    static void registerMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl());
    }

    @Autowired
    PostRepository postRepository;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    ReactiveFluentMongoOperations fluentMongoOperations;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var latch = new CountDownLatch(1);
        this.reactiveMongoTemplate.remove(Post.class).all()
            .doOnTerminate(latch::countDown)
            .subscribe(
                r -> log.debug("delete all posts: " + r),
                e -> log.debug("error: " + e),
                () -> log.debug("done")
            );
        latch.await(5000, TimeUnit.MILLISECONDS);
    }


    @Test
    public void testSavePost() {
        var content = "my test content";
        var title = "my test title";
        var saved = this.postRepository.create(title, content);

        StepVerifier.create(saved)
            .consumeNextWith(p -> {
                log.debug("consuming post:: {}", p);
                assertThat(p.getTitle()).isEqualTo(title);
            })
            .expectComplete()
            .verify();

        var id = saved.block().getId();

        this.postRepository.addComment(id, "comment1")
            .then(this.postRepository.findById(id))
            .as(StepVerifier::create)
            .consumeNextWith(p -> {
                log.debug("after add comments: {}", p);
                assertThat(p.getComments()).isNotNull();
            })
            .expectComplete()
            .verify();
    }

    @Test
    public void testFluentOperations() {
        this.fluentMongoOperations.insert(Post.class)
            .all(List.of(
                    Post.builder().title("my test title").content("my test content").build(),
                    Post.builder().title("my second title").content("my second content").build()
                )
            )
            .as(StepVerifier::create)
            .expectNextCount(2)
            .verifyComplete();

//        this.fluentMongoOperations.update(Post.class)
//            .matching(Query.query(Criteria.where("id").is("")))
//            .apply(new Update().pull())


//        this.fluentMongoOperations.remove(Post.class)
//            .matching(Query.query(Criteria.where("id").is("")))
//            .all()

    }


}
