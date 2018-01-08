package com.example.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class PostId implements Serializable {
    private String id;
}
