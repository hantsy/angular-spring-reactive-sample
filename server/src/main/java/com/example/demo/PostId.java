package com.example.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class PostId implements Serializable {
    private String id;
}
