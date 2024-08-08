package com.task10;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;
import java.util.Map;

public class UserService {

    private final AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.defaultClient();


    public APIGatewayProxyResponseEvent handleSignup(APIGatewayProxyRequestEvent request) {
        try {
            JSONObject json = new JSONObject(request.getBody());
            String firstName = json.getString("firstName");
            String lastName = json.getString("lastName");
            String email = json.getString("email");
            String password = json.getString("password");

            AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest()
                    .withUserPoolId(System.getenv("COGNITO_ID"))
                    .withUsername(email)
                    .withUserAttributes(
                            new AttributeType().withName("given_name").withValue(firstName),
                            new AttributeType().withName("family_name").withValue(lastName),
                            new AttributeType().withName("email").withValue(email)
                    )
                    .withTemporaryPassword(password);

            cognitoClient.adminCreateUser(createUserRequest);

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("Sign-up successful");

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error during sign-up: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent handleSignin(APIGatewayProxyRequestEvent request) {
        try {
            JSONObject json = new JSONObject(request.getBody());
            String email = json.getString("email");
            String password = json.getString("password");

            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                    .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .withUserPoolId(System.getenv("COGNITO_ID"))
                    .withClientId(System.getenv("CLIENT_ID"))
                    .withAuthParameters(Map.of(
                            "USERNAME", email,
                            "PASSWORD", password
                    ));

            AdminInitiateAuthResult authResult = cognitoClient.adminInitiateAuth(authRequest);
            String accessToken = authResult.getAuthenticationResult().getIdToken();

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(new JSONObject().put("accessToken", accessToken).toString());

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error during sign-in: " + e.getMessage());
        }
    }
}
