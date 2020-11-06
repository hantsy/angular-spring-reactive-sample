package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

@Configuration
@Profile("cors")
@Slf4j
class WebConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        log.info("configured cors: {}", source);
        return source;
    }
}

@Configuration
class SessionConfig {

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        HeaderWebSessionIdResolver resolver = new HeaderWebSessionIdResolver();
        resolver.setHeaderName("X-AUTH-TOKEN");
        return resolver;
    }
}

@Configuration
class MongoConfig {

    @Bean
    public PersistentEntityCallback persistentEntityCallback() {
        return new PersistentEntityCallback();
    }
}

@Configuration
class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {

        return http
                .csrf(it ->
                        it.disable()
                )
                .httpBasic(it ->
                        it.securityContextRepository(new WebSessionServerSecurityContextRepository())
                )
                .authorizeExchange(it ->
                        it.pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                                .pathMatchers("/posts/**").authenticated()
                                .pathMatchers("/auth/**").authenticated()
                                .pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                                .anyExchange().permitAll()
                )
                .build();

    }

    private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication
                .map(a -> context.getVariables().get("user").equals(a.getName()))
                .map(AuthorizationDecision::new);
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository users) {
        return (username) -> users.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .authorities(u.getRoles().toArray(new String[0]))
                        .accountExpired(!u.isActive())
                        .credentialsExpired(!u.isActive())
                        .disabled(!u.isActive())
                        .accountLocked(!u.isActive())
                        .build()
                );
    }
}
