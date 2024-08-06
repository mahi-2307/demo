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
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;

import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.amazonaws.xray.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler", roleName = "api_handler-role", isPublishVersion = false)
public class Processor implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final DynamoDB dynamoDb = new DynamoDB(AmazonDynamoDBClientBuilder.standard()
			.withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
			.build());
	private final Table weatherTable = dynamoDb.getTable("Weather");

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		AWSXRay.beginSegment("WeatherLambdaSegment");

		try {
			// Fetch weather data from Open-Meteo API
			String weatherData = fetchWeatherData();

			// Parse the JSON response
			ObjectMapper mapper = new ObjectMapper();
			JsonNode weatherJson = mapper.readTree(weatherData);

			// Store the weather data in DynamoDB
			String id = UUID.randomUUID().toString();
			Item item = new Item()
					.withPrimaryKey("id", id)
					.withJSON("forecast", weatherJson.toString());
			PutItemOutcome outcome = weatherTable.putItem(item);

			// Prepare the response
			APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
			response.setStatusCode(200);
			response.setBody("Weather data saved with ID: " + id);

			return response;

		} catch (Exception e) {
			AWSXRay.getCurrentSegment().addException(e);
			throw new RuntimeException(e);
		} finally {
			AWSXRay.endSegment();
		}
	}

	private String fetchWeatherData() throws Exception {
		URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=50.4375&longitude=30.5&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		Scanner scanner = new Scanner(conn.getInputStream());
		StringBuilder response = new StringBuilder();

		while (scanner.hasNext()) {
			response.append(scanner.nextLine());
		}

		scanner.close();
		return response.toString();
	}
}