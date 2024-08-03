package com.task05;


import lombok.*;

import java.util.Map;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Entity {
    private String id;
    private int principalId;
    private String createdAt;
    private Map<String,String> body;

}
