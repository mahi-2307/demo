package com.task10.dto;

import lombok.Getter;
import org.json.JSONObject;

@Getter
public class SignIn {
    private final String email;
    private final String password;

    public SignIn(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Missing or incomplete data.");
        }
        this.email = email;
        this.password = password;
    }

    public static SignIn fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String nickName = json.optString("email", null);
        String password = json.optString("password", null);

        return new SignIn(nickName, password);
    }
}
