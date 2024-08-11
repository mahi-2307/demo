package com.task10.handler;/*
 * Copyright 2024 EPAM Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.task10.dto.SignIn;
import com.task10.CognitoSupport;
import org.json.JSONObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

/**
 * Created by Roman Ivanov on 7/20/2024.
 */
public class PostSignIn extends CognitoSupport implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    public PostSignIn(CognitoIdentityProviderClient cognitoClient) {
        super(cognitoClient);
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        try {
            SignIn signIn = SignIn.fromJson(requestEvent.getBody());

            String accessToken = cognitoSignIn(signIn.getNickName(), signIn.getPassword())
                    .authenticationResult()
                    .idToken();

//            return new APIGatewayProxyResponseEvent()
//                    .withStatusCode(200)
//                    .withBody(new JSONObject().put("accessToken", accessToken).toString());
            return APIGatewayV2HTTPResponse.builder().withStatusCode(200)
                    .withBody(new JSONObject().put("accessToken", accessToken).toString())
                    .build();
        } catch (Exception e) {
//            return new APIGatewayProxyResponseEvent()
//                    .withStatusCode(400)
//                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
            return APIGatewayV2HTTPResponse.builder().withBody(new JSONObject().put("error", e.getMessage()).toString())
                    .withStatusCode(400)
                    .build();
        }
    }

}