package com.example.demo.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
class SessionConfig {

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        HeaderWebSessionIdResolver resolver = new HeaderWebSessionIdResolver();
        resolver.setHeaderName("X-AUTH-TOKEN");
        return resolver;
    }
}
