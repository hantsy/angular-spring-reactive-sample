package com.example.demo.infrastructure.persistence;

import com.example.demo.domain.model.User;
import com.example.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MongoUserRepository  implements UserRepository {
    private final ReactiveMongoTemplate mongoTemplate;
    @Override
    public Mono<User> findByUsername(String username) {
        return this.mongoTemplate.findOne(query(where("username").is(username)), User.class);
    }
}
