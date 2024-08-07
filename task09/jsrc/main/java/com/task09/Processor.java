package com.task09;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.syndicate.deployment.model.TracingMode;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "processor",
		roleName = "processor-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
		tracingMode = TracingMode.Active
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@EnvironmentVariables(
		@EnvironmentVariable(key = "target_table", value = "${target_table}")
)
public class Processor implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayV2HTTPResponse> {

	private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
			.region(Region.EU_CENTRAL_1)
			.overrideConfiguration(ClientOverrideConfiguration.builder()
					.addExecutionInterceptor(new TracingInterceptor()) // Add X-Ray tracing interceptor
					.build())
			.build();
	private final String tableName = System.getenv("target_table");

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayProxyRequestEvent request, Context context) {

		APIGatewayV2HTTPResponse response;
		try {
			// Fetch weather data
			String weatherData = fetchWeatherData();

			// Generate a unique ID for the DynamoDB item
			String id = UUID.randomUUID().toString();

			// Create a new item to put in the DynamoDB table
			Map<String, AttributeValue> item = new HashMap<>();
			item.put("id", AttributeValue.builder().s(id).build());
			item.put("forecast", AttributeValue.builder().s(weatherData).build());

			// Put the item in the DynamoDB table
			PutItemRequest putItemRequest = PutItemRequest.builder()
					.tableName(tableName)
					.item(item)
					.build();
			PutItemResponse putItemResponse = dynamoDbClient.putItem(putItemRequest);

			// Build the successful response
			response = APIGatewayV2HTTPResponse.builder()
					.withStatusCode(200)
					.withBody(weatherData)
					.build();

		} catch (Exception ex) {
			context.getLogger().log("Error: " + ex.getMessage());
			response = APIGatewayV2HTTPResponse.builder()
					.withStatusCode(500)
					.withBody("{\"statusCode\": 500, \"message\": \"Internal Server Error\"} " + ex.getMessage())
					.build();
		}

		return response;
	}

	private String fetchWeatherData() throws Exception {
		// Create a URL for the Open-Meteo API
		URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=50.4375&longitude=30.5&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m");

		// Open a connection to the URL
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		// Set the request method to GET
		conn.setRequestMethod("GET");

		// Read the response using a Scanner
		Scanner scanner = new Scanner(new InputStreamReader(conn.getInputStream()));
		StringBuilder response = new StringBuilder();

		while (scanner.hasNext()) {
			response.append(scanner.nextLine());
		}

		scanner.close();
		return response.toString();
	}
}
