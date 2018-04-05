package com.example.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class Count implements Serializable {

    private long count;
}
