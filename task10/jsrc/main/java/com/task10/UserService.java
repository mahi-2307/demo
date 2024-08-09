package com.task10;



import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.dto.SignUp;
import org.json.JSONObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import java.util.Map;
import java.util.regex.Pattern;

public class UserService {

    private final CognitoIdentityProviderClient cognitoClient;

    public UserService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }


    public APIGatewayProxyResponseEvent handleSignup(APIGatewayProxyRequestEvent request) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
//            SignUp signUp = objectMapper.convertValue(request.getBody(),SignUp.class);
//            String firstName = signUp.getFirstName();
//            String lastName = signUp.getLastName();
//            String email = signUp.getEmail();
//            String password =signUp.getPassword();
            JSONObject json = new JSONObject(request.getBody());
            String firstName = json.getString("firstName");
            String lastName = json.getString("lastName");
            String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            String email = json.getString("email");
            Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
            if (email == null || EMAIL_PATTERN.matcher(email).matches()) {
                throw new Exception("Email format is invalid");
            }
            String password = json.getString("password");
//            AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest()
//                    .withUserPoolId(System.getenv("COGNITO_ID"))
//                    .withUsername(email)
//                    .withUserAttributes(
//                            new AttributeType().withName("given_name").withValue(firstName),
//                            new AttributeType().withName("family_name").withValue(lastName),
//                            new AttributeType().withName("email").withValue(email),
//                            new AttributeType().withName("email_verified").withValue("true")
//                    )
//                    .withTemporaryPassword(password)
//                    .withMessageAction("SUPPRESS");
//            cognitoClient.adminCreateUser(createUserRequest);
//            AdminSetUserPasswordRequest setPasswordRequest = new AdminSetUserPasswordRequest()
//                    .withUserPoolId(System.getenv("COGNITO_ID"))
//                    .withUsername(email)
//                    .withPassword(password)
//                    .withPermanent(true);
//
//            cognitoClient.adminSetUserPassword(setPasswordRequest);
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(System.getenv("COGNITO_ID"))
                    .username(email)
                    .password(password)
                    .userAttributes(AttributeType.builder().name("firstName").value(firstName).build(),
                            AttributeType.builder().name("lastName").value(lastName).build()
                    ).build();
            SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequest);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("Sign-up successful" + signUpResponse);

        } catch (AWSCognitoIdentityProviderException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Cognito error: " + e.getErrorMessage());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal server error: " + e.getMessage());
        }
    }


//    public APIGatewayProxyResponseEvent handleSignin(APIGatewayProxyRequestEvent request) {
//        try {
//            JSONObject json = new JSONObject(request.getBody());
//            String email = json.getString("email");
//            String password = json.getString("password");
//
////            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
////                    .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
////                    .withUserPoolId(System.getenv("COGNITO_ID"))
////                    .withClientId(System.getenv("CLIENT_ID"))
////                    .withAuthParameters(Map.of(
////                            "USERNAME", email,
////                            "PASSWORD", password
////                    ));
//
//            // AdminInitiateAuthResult authResult = cognitoClient.adminInitiateAuth(authRequest);
//            //     String accessToken = authResult.getAuthenticationResult().getIdToken();
//
//            //     return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(new JSONObject().put("accessToken", accessToken).toString());
//
////        } catch (NotAuthorizedException e) {
////            return new APIGatewayProxyResponseEvent().withStatusCode(401).withBody("Unauthorized: " + e.getErrorMessage());
//        } catch (AWSCognitoIdentityProviderException e) {
//            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Cognito error: " + e.getErrorMessage());
//        } catch (Exception e) {
//            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal server error: " + e.getMessage());
//        }
//    }
    }
