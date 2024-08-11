package com.task10.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/*@author: DEVPROBLEMS(A SARANG KUMAR TAK)*/

@Setter
@Getter
public class Response {

    private int statusCode;
    private String body;
    private Map<String, String> headers;

    public Response(){}

}