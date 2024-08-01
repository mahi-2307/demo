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
    String id;
    int principalId;
    String currentTime;
    Map<String, String> body;
    public Entity(String json){
        Gson gson= new Gson();
        Entity entity = gson.fromJson(json,Entity.class);
        this.id = entity.id;
        this.principalId = entity.principalId;
        this.currentTime = entity.currentTime;
        this.body = entity.body;
    }

    @Override
    public String toString() {
        return new Gson().toString();
    }
}
