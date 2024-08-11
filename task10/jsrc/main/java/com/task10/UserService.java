package com.task10;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserService {

    private final AWSCognitoIdentityProvider client ;

    public UserService() {
        client = createCognitoClient();
    }

    private AWSCognitoIdentityProvider createCognitoClient() {

        return AWSCognitoIdentityProviderClientBuilder.standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
    }

    public SignUpResult signUp(String name, String email, String password) {
        SignUpRequest request = new SignUpRequest().withClientId(System.getenv("CLIENT_ID") ).withUsername(email).withPassword(password);
        return client.signUp(request);
    }

    public ConfirmSignUpResult confirmSignUp(String email, String confirmationCode) {
        ConfirmSignUpRequest confirmSignUpRequest = new ConfirmSignUpRequest().withClientId(System.getenv("CLIENT_ID")).withUsername(email).withConfirmationCode(confirmationCode);
        return client.confirmSignUp(confirmSignUpRequest);
    }

    public Map<String, String> login(String email, String password) {
        Map<String, String> authParams = new LinkedHashMap<String, String>() {{
            put("USERNAME", email);
            put("PASSWORD", password);
        }};

        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withUserPoolId(System.getenv("COGNITO_ID"))
                .withClientId(System.getenv("CLIENT_ID"))
                .withAuthParameters(authParams);
        AdminInitiateAuthResult authResult = client.adminInitiateAuth(authRequest);
        AuthenticationResultType resultType = authResult.getAuthenticationResult();
        return new LinkedHashMap<String, String>() {{
            put("idToken", resultType.getIdToken());
            put("accessToken", resultType.getAccessToken());
            put("refreshToken", resultType.getRefreshToken());
            put("message", "Successfully login");
        }};

    }
}