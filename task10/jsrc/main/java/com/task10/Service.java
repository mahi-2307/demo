package com.task10;

import com.amazonaws.services.cognitoidp.model.ConfirmSignUpResult;
import com.amazonaws.services.cognitoidp.model.SignUpResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.dto.Request;
import com.task10.dto.Response;

import java.util.Map;

public class Service {

    // Initialize UserService client here
    private final UserService client;

    // Constructor to initialize UserService
    public Service() {
        this.client = new UserService();
    }

    public Response authentication(APIGatewayV2HTTPEvent request) {
        Response res = new Response();
        String resource = request.getRawPath();
        switch (resource) {
            case "/signup" :
                res = signUp(request.getBody());
                break;
            case "/confirmsignup" :
                res = confirmSignUp(request.getBody());
                break;
            case "/signin" :
                res = login(request.getBody());
                break;
            default:
                res.setBody("Wrong resource name");
                res.setStatusCode(500);
                break;
        }
        return res;
    }

    public Response signUp(String body) {
        Response res = new Response();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> parameters = mapper.readValue(body, new TypeReference<Map<String, String>>(){});
            String name = parameters.get("fullname");
            String email = parameters.get("email");
            String password = parameters.get("password");
            SignUpResult result = client.signUp(name, email, password);
            result.setUserConfirmed(true);
            res.setStatusCode(200);
            res.setBody("Confirmation Code is sent to registered email address" + result.toString());
        } catch (Exception e) {
            res.setStatusCode(500);
            res.setBody(e.getMessage());
        }
        return res;
    }

    public Response confirmSignUp(String body) {
        Response res = new Response();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> parameters = mapper.readValue(body, new TypeReference<Map<String, String>>() {});
            String email = parameters.get("email");
            String confirmationCode = parameters.get("confirmationcode");
            ConfirmSignUpResult result = client.confirmSignUp(email, confirmationCode);
            if (result != null) {
                res.setStatusCode(200);
                res.setBody("Confirm Signup is successful");
            } else {
                res.setStatusCode(400);
                res.setBody("Please try again after sometime");
            }
        } catch (Exception e) {
            res.setStatusCode(500);
            res.setBody(e.getMessage());
        }
        return res;
    }

    public Response login(String body) {
        Response res = new Response();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> parameters = mapper.readValue(body, new TypeReference<Map<String, String>>() {});
            String email = parameters.get("email");
            String password = parameters.get("password");
            Map<String, String> tokens = client.login(email, password);

            if (tokens != null) {
                res.setStatusCode(200);
                res.setBody(tokens.toString());
            } else {
                res.setStatusCode(400);
                res.setBody("Please try again after sometime");
            }
        } catch (Exception e) {
            res.setStatusCode(500);
            res.setBody(e.getMessage());
        }
        return res;
    }
}
