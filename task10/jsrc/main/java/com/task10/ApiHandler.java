package com.task10;

import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task10.dto.SignUp;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<Request, APIGatewayV2HTTPResponse> {
ObjectMapper objectMapper = new ObjectMapper();
CognitoService cognitoService = new CognitoService();
	@Override
	public APIGatewayV2HTTPResponse handleRequest(Request input, Context context) {
 	SignUp sign = objectMapper.convertValue(input.getContent(), SignUp.class);
 	SignUp signUp = new SignUp();
	 signUp.setEmail(sign.getEmail());;
	 signUp.setPassword(sign.getPassword());
	return 	APIGatewayV2HTTPResponse.builder()
				.withStatusCode(500)
				.withBody("Successful signup")
				.build();
	}
}
