/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;

/**
 * @author hantsy
 */
@Document(collection = "users")
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;
    private String username;

    @JsonIgnore
    private String password;

    @Email
    private String email;

    @Builder.Default()
    private boolean active = true;

    @Builder.Default()
    private List<String> roles = Collections.emptyList();

}
