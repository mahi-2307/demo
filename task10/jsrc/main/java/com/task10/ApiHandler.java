package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.environment.ValueTransformer;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL,name = "${booking_userpool}")
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "REGION", value = "${region}"),
		@EnvironmentVariable(key = "COGNITO_ID", value = "${booking_userpool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "CLIENT_ID", value = "${booking_userpool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID)
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().build();
	private final UserService userService = new UserService(cognitoClient);
	private final TableService tableService = new TableService();
	private final ReservationService reservationService = new ReservationService();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		String path = request.getPath();
		String method = request.getHttpMethod();

		switch (path) {
			case "/signup":
				if ("POST".equals(method)) {
					return userService.handleSignup(request);
				}
				break;
			case "/signin":
				if ("POST".equals(method)) {
					return null;
				}
				break;
			case "/tables":
				if ("GET".equals(method)) {
					return tableService.handleGetTables(request);
				} else if ("POST".equals(method)) {
					return tableService.handleCreateTable(request);
				}
				break;
			case "/tables/{tableId}":
				if ("GET".equals(method)) {
					return tableService.handleGetTableById(request);
				}
				break;
			case "/reservations":
				if ("POST".equals(method)) {
					return reservationService.handleCreateReservation(request);
				} else if ("GET".equals(method)) {
					return reservationService.handleGetReservations(request);
				}
				break;
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Invalid request");
	}
}
