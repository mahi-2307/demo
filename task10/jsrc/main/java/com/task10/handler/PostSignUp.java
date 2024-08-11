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
public class PostSignUp extends CognitoSupport implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    public PostSignUp(CognitoIdentityProviderClient cognitoClient) {
        super(cognitoClient);
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        try {
            SignUp signUp = SignUp.fromJson(requestEvent.getBody());

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

}