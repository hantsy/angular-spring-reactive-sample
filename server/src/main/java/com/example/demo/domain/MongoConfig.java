package com.example.demo.domain;

import com.example.demo.domain.model.PersistentEntityCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MongoConfig {

    @Bean
    public PersistentEntityCallback persistentEntityCallback() {
        return new PersistentEntityCallback();
    }
}
