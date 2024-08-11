package com.task10.dto;

import org.json.JSONObject;

public class SignIn {
    private final String nickName;
    private final String password;

    public SignIn(String nickName, String password) {
        if (nickName == null || password == null) {
            throw new IllegalArgumentException("Missing or incomplete data.");
        }
        this.nickName = nickName;
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public String getPassword() {
        return password;
    }

    public static SignIn fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String nickName = json.optString("nickName", null);
        String password = json.optString("password", null);

        return new SignIn(nickName, password);
    }
}
