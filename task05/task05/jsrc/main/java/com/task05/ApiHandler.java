package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jdi.request.EventRequest;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import lombok.SneakyThrows;
import org.joda.time.Instant;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<Map<String,Object> ,APIGatewayProxyResponseEvent> {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
			.withRegion("eu-central-1")
			.build();
	private final DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
	private final String DYNAMODB_TABLE_NAME = "cmtr-7767740d-Events";

	@SneakyThrows
	public APIGatewayProxyResponseEvent handleRequest(Map<String,Object> request, Context context) {
//		// Check if request body is null
//		String requestBody = apiGatewayProxyRequestEvent.getBody();
//		if (requestBody == null) {
//			throw new IllegalArgumentException("Request body is null");
//		}
//
//		// Parse the JSON body
//		JsonNode rootNode = objectMapper.readTree(requestBody);
//
//		// Extract principalId
//		JsonNode principalIdNode = rootNode.path("principalId");
//		if (principalIdNode.isMissingNode()) {
//			throw new IllegalArgumentException("Field 'principalId' is missing");
//		}
//		int principalId = principalIdNode.asInt();

		// Generate a unique ID and get the current timestamp


		Map<String, AttributeValue> itemValues = new HashMap<>();

		Random random = new Random();
		int numericId = random.nextInt(Integer.MAX_VALUE);
		itemValues.put("id", new AttributeValue().withS(Integer.toString(numericId)));

		String principalId = String.valueOf(request.getOrDefault("principalId", "defaultPrincipalId"));
		String content = String.valueOf(request.getOrDefault("content", "defaultContent"));
		itemValues.put("principalId", new AttributeValue().withS(principalId));
		itemValues.put("content", new AttributeValue().withS(content));

		amazonDynamoDB.putItem("cmtr-7767740d-Events", itemValues);

//		Map<String, Object> response = new HashMap<String, Object>();
//		response.put("statusCode", 201);
//		response.put("body", itemValues);
		APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
		return apiGatewayProxyResponseEvent.withBody("This is working");


	}
}