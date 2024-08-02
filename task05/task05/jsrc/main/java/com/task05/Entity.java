package com.task05;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter

@AllArgsConstructor
public class Entity {
    int principalId;
    Map<String, String> body;
    public Entity(String json){
        Gson gson= new Gson();
        Entity entity = gson.fromJson(json,Entity.class);
        this.principalId = entity.principalId;
        this.body = entity.body;
    }

    @Override
    public String toString() {
        return new Gson().toString();
    }
}
