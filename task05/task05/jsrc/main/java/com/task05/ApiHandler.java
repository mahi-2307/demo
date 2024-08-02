package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
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
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent,APIGatewayProxyResponseEvent> {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
			.withRegion("eu-central-1")
			.build();
	private final DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
	private final String DYNAMODB_TABLE_NAME = "cmtr-7767740d-Events";
	@SneakyThrows
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
		JsonParser jsonParser = objectMapper.getFactory().createParser(apiGatewayProxyRequestEvent.getBody());
		JsonNode rootNode = objectMapper.readTree(jsonParser);

		int principalId = rootNode.path("principalId").asInt()	;
		String id = UUID.randomUUID().toString();
		// Get current time in ISO 8601 format
		String createdAt =Instant.now().toString();
		JsonNode bodyNode = rootNode.get("body");
		//EmployeeBody employeeBody = objectMapper.treeToValue(bodyNode, EmployeeBody.class);
		String body = rootNode.path("body").asText();
		Item item = new Item()
				.withPrimaryKey("id", id)
				.withNumber("principalId", principalId)
				.withString("createdAt", createdAt)
				.withString("body", body);

		dynamoDB.getTable(DYNAMODB_TABLE_NAME).putItem(new PutItemSpec().withItem(item));
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

		response.setStatusCode(201);
		response.setBody("event");
return response;
	}
}
