/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hantsy
 */
@RestController
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/user")
    public Mono<Map> current(@AuthenticationPrincipal Mono<Principal> principal) {
        return principal
                .map( user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", user.getName());
                    map.put("roles", AuthorityUtils.authorityListToSet(((Authentication) user)
                            .getAuthorities()));
                    return map;
                });
    }

    @GetMapping("/users/{username}")
    public Mono<User> get(@PathVariable() String username) {
        return this.users.findByUsername(username);
    }
}
