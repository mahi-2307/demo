package com.task10.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Request {

    private String body;
    private String resource;

    public Request(){}

}