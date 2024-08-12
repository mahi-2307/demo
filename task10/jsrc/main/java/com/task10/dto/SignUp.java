package com.task10.dto;

import lombok.Getter;
import org.json.JSONObject;
@Getter
public class SignUp {
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;

    public SignUp(String email, String password, String firstName, String lastName) {
        if (email == null || password == null || firstName == null || lastName == null) {
            throw new IllegalArgumentException("Missing or incomplete data.");
        }
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public static SignUp fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String email = json.optString("email", null);
        String password = json.optString("password", null);
        String firstName = json.optString("firstName", null);
        String lastName = json.optString("lastName", null);


        return new SignUp(email, password, firstName, lastName);
    }

    @Override
    public String toString() {
        return "SignUp{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
