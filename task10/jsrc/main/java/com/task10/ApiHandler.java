package com.task10;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
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
import com.task10.handler.PostSignIn;
import com.task10.handler.PostSignUp;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${booking_userpool}")
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "REGION", value = "${region}"),
		@EnvironmentVariable(key = "COGNITO_ID", value = "${booking_userpool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "CLIENT_ID", value = "${booking_userpool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID)
})
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final CognitoIdentityProviderClient cognitoClient;
	private final Map<String, String> headersForCORS;
	private final PostSignUp postSignUp;
	private final PostSignIn postSignIn;
	private final TableService tableService;
	private final ReservationService reservationService;

	public ApiHandler() {
		this.cognitoClient = initCognitoClient();
		this.headersForCORS = initHeadersForCORS();
		this.postSignUp = new PostSignUp(cognitoClient);
		this.postSignIn = new PostSignIn(cognitoClient);
		this.tableService = new TableService();
		this.reservationService = new ReservationService();
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
		String requestPath = event.getRawPath();
		String httpMethod = (event.getRequestContext() != null && event.getRequestContext().getHttp() != null)
				? event.getRequestContext().getHttp().getMethod()
				: null;

		if (requestPath == null || httpMethod == null) {
			return buildResponse(404, "Resource not found....");
		}

		APIGatewayV2HTTPResponse response;
		switch (requestPath) {
			case "/signup":
				if ("POST".equalsIgnoreCase(httpMethod)) {
					response = postSignUp.handleRequest(event, context);
				} else {
					response = buildResponse(400, "Unsupported HTTP method for /signup");
				}
				break;
			case "/signin":
				if ("POST".equalsIgnoreCase(httpMethod)) {
					response = postSignIn.handleRequest(event, context);
				} else {
					response = buildResponse(400, "Unsupported HTTP method for /signin");
				}
				break;
			case "/tables":
				if ("GET".equalsIgnoreCase(httpMethod)) {
					response = tableService.handleGetTables(event);
				} else if ("POST".equalsIgnoreCase(httpMethod)) {
					response = tableService.handleCreateTable(event);
				} else {
					response = buildResponse(400, "Unsupported HTTP method for /tables");
				}
				break;
			case "/tables/{tableId}":
				if ("GET".equalsIgnoreCase(httpMethod)) {
					response = tableService.handleGetTableById(event);
				} else {
					response = buildResponse(400, "Unsupported HTTP method for /tables/{tableId}");
				}
				break;
			case "/reservations":
				if ("POST".equalsIgnoreCase(httpMethod)) {
					response = reservationService.handleCreateReservation(event);
				} else if ("GET".equalsIgnoreCase(httpMethod)) {
					response = reservationService.handleGetReservations(event);
				} else {
					response = buildResponse(400, "Unsupported HTTP method for /reservations");
				}
				break;
			default:
				response = buildResponse(404, "Resource not found");
				break;
		}

		return response;
	}

	private CognitoIdentityProviderClient initCognitoClient() {
		return CognitoIdentityProviderClient.builder()
				.region(Region.of(System.getenv("REGION")))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}

	private Map<String, String> initHeadersForCORS() {
		return Map.of(
				"Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
				"Access-Control-Allow-Origin", "*",
				"Access-Control-Allow-Methods", "*",
				"Accept-Version", "*"
		);
	}

	private APIGatewayV2HTTPResponse buildResponse(int statusCode, String message) {
		return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(statusCode)
				.withBody("{\"statusCode\": " + statusCode + ", \"message\": \"" + message + "\"}")
				.withHeaders(headersForCORS)
				.build();
	}
}
