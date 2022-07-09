package com.example.demo.application;

import com.example.demo.domain.repository.UserRepository;
import com.example.demo.interfaces.dto.LoginRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.logout.LogoutWebFilter;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@Configuration
@Slf4j
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http,
                                                ServerSecurityContextRepository serverSecurityContextRepository,
                                                AuthenticationWebFilter loginFilter,
                                                LogoutWebFilter logoutFilter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(serverSecurityContextRepository)
                .authorizeExchange(it ->
                        it.pathMatchers("/", "/login", "/logout").permitAll()
                                .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                                .pathMatchers("/posts/**").authenticated()
                                .pathMatchers("/auth/**").authenticated()
                                .pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                                .anyExchange().permitAll()
                )
                .addFilterAt(loginFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(logoutFilter, SecurityWebFiltersOrder.LOGOUT)
                .build();

    }

    private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication
                .map(a -> context.getVariables().get("user").equals(a.getName()))
                .map(AuthorizationDecision::new);
    }

    @Bean
    public ServerSecurityContextRepository serverSecurityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public AuthenticationWebFilter loginFilter(ReactiveAuthenticationManager authenticationManager,
                                               ServerSecurityContextRepository serverSecurityContextRepository,
                                               ObjectMapper objectMapper) {
        var filter = new AuthenticationWebFilter(authenticationManager);
        filter.setSecurityContextRepository(serverSecurityContextRepository);
        filter.setServerAuthenticationConverter(exchange ->
                exchange.getRequest().getBody()
                        .cache()
                        .next()
                        .flatMap(buffer -> {
                            try {
                                LoginRequest request = objectMapper.readValue(buffer.asInputStream(), LoginRequest.class);
                                return Mono.just(request);
                            } catch (IOException e) {
                                log.debug("Can't read login request from JSON");
                                return Mono.error(e);
                            }
                        })
                        .map(request -> new UsernamePasswordAuthenticationToken(request.username(), request.password()))
        );
        filter.setRequiresAuthenticationMatcher(pathMatchers(HttpMethod.POST, "/login"));
        filter.setAuthenticationSuccessHandler((webFilterExchange, authentication) -> {
            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.OK);
            var user = (UserDetails) authentication.getPrincipal();
            var data = Map.of("name", user.getUsername(),
                    "roles", AuthorityUtils.authorityListToSet(user.getAuthorities()));
            try {
                var db = new DefaultDataBufferFactory().wrap(objectMapper.writeValueAsBytes(data));
                return webFilterExchange.getExchange().getResponse().writeWith(Mono.just(db));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Mono.empty();
            }
        });
        filter.setAuthenticationFailureHandler((webFilterExchange, e) -> {
            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return webFilterExchange.getExchange().getResponse().setComplete();
        });

        return filter;
    }

    @Bean
    public LogoutWebFilter logoutFilter() {
        var filter = new LogoutWebFilter();
        filter.setLogoutHandler((webFilterExchange, authentication) ->
                webFilterExchange.getExchange().getSession()
                        .flatMap(WebSession::invalidate).then()
        );
        filter.setLogoutSuccessHandler((webFilterExchange, authentication) -> {
            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.OK);
            return webFilterExchange.getExchange().getResponse().setComplete();
        });
        filter.setRequiresLogoutMatcher(pathMatchers(HttpMethod.POST, "/logout"));
        return filter;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        var authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
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
                )
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(username)));
    }
}
