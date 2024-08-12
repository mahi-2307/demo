package com.task10.handler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.task10.dto.SignUp;
import com.task10.CognitoSupport;
import org.json.JSONObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

import java.util.regex.Pattern;

public class PostSignUp extends CognitoSupport implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    public PostSignUp(CognitoIdentityProviderClient cognitoClient) {
        super(cognitoClient);
    }
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\\$\\%\\^\\*])[A-Za-z0-9\\$\\%\\^\\*]{12,}$");


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        try {
            SignUp signUp = SignUp.fromJson(requestEvent.getBody());
            if (!isValidEmail(signUp.getEmail())) {
                return APIGatewayV2HTTPResponse.builder()
                        .withBody(new JSONObject().put("message", "Invalid email format.").toString())
                        .withStatusCode(400)
                        .build();
            }
            if (!isValidPassword(signUp.getPassword())) {
                return APIGatewayV2HTTPResponse.builder()
                        .withBody(new JSONObject().put("message", "Invalid password. Password must be alphanumeric, include any of the symbols $%^*, and be at least 12 characters long.").toString())
                        .withStatusCode(400)
                        .build();
            }
            // sign up
            String userId = cognitoSignUp(signUp)
                    .user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .map(AttributeType::value)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Sub not found."));
            // confirm sign up
            String idToken = confirmSignUp(signUp)
                    .authenticationResult()
                    .idToken();
            return APIGatewayV2HTTPResponse.builder()
        .withBody(new JSONObject()
                .put("message", "User has been successfully signed up.")
                .put("userId", userId)
                .put("accessToken", idToken)
                .toString()).withStatusCode(200).build();
        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withBody(e.getMessage()).withStatusCode(500).build();
        }
    }
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    private boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}