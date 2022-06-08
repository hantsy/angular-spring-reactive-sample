package com.example.demo.domain;

import com.example.demo.domain.model.Username;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMongoAuditing
class MongoConfig {

    @Bean
    public ReactiveAuditorAware<Username> reactiveAuditorAware() {
        return () -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .map(userDetails -> new Username(userDetails.getUsername()))
                .switchIfEmpty(Mono.empty());
    }
}
