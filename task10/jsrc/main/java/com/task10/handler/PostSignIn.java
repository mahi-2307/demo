package com.task10.handler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.task10.dto.SignIn;
import com.task10.CognitoSupport;
import org.json.JSONObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.regex.Pattern;

public class PostSignIn extends CognitoSupport implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    public PostSignIn(CognitoIdentityProviderClient cognitoClient) {
        super(cognitoClient);
    }
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        try {

            SignIn signIn = SignIn.fromJson(requestEvent.getBody());
            if (!isValidEmail(signIn.getEmail())) {
                return APIGatewayV2HTTPResponse.builder()
                        .withBody(new JSONObject().put("error", "Invalid email format.").toString())
                        .withStatusCode(400)
                        .build();
            }
            if (signIn.getPassword() == null || signIn.getPassword().isEmpty()) {
                return APIGatewayV2HTTPResponse.builder()
                        .withBody(new JSONObject().put("error", "Password cannot be empty.").toString())
                        .withStatusCode(400)
                        .build();
            }
            String accessToken = cognitoSignIn(signIn.getEmail(), signIn.getPassword())
                    .authenticationResult()
                    .idToken();
            return APIGatewayV2HTTPResponse.builder().withStatusCode(200)
                    .withBody(new JSONObject().put("accessToken", accessToken).toString())
                    .build();
        } catch (Exception e) {
         return APIGatewayV2HTTPResponse.builder().withBody(new JSONObject().put("error", e.getMessage()).toString())
                    .withStatusCode(400)
                    .build();
        }
    }
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}