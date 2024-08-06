package com.task09;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "processor",
		roleName = "processor-role",
		isPublishVersion = false
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@EnvironmentVariables(
		@EnvironmentVariable(key = "target_bucket",
				value = "${target_bucket}")
)
public class Processor implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayV2HTTPResponse> {

	private final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
			.withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())) // Attach X-Ray tracing handler
			.build();
	private final DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
	private final String DYNAMODB_TABLE_NAME = System.getenv("target_bucket");
	private final Table weatherTable = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		// Start X-Ray segment
		AWSXRay.beginSegment("ProcessorLambdaSegment");

		APIGatewayV2HTTPResponse response;
		try {
			// Fetch weather data
			String weatherData = fetchWeatherData();

			// Generate a unique ID for the DynamoDB item
			String id = UUID.randomUUID().toString();

			// Create a new item to put in the DynamoDB table
			Item item = new Item()
					.withPrimaryKey("id", id)
					.withString("forecast", weatherData);

			// Put the item in the table
			weatherTable.putItem(item);

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
		} finally {
			// End the X-Ray segment
			AWSXRay.endSegment();
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
