package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import jdk.jfr.Event;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, Response> {

	AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
	private final DynamoDB dynamoDb = new DynamoDB(client);
	private final String DYNAMODB_TABLE_NAME = System.getenv("target_table");
ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public Response handleRequest(APIGatewayProxyRequestEvent event1, Context context) {
		String s = event1.getBody();
		Request request = objectMapper.convertValue(s, Request.class);

		int principalId = request.getPrincipalId();
		Map<String, String> content = request.getContent();

		String newId = UUID.randomUUID().toString();
		String currentTime = DateTimeFormatter.ISO_INSTANT
				.format(Instant.now().atOffset(ZoneOffset.UTC));

		Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);

		Item item = new Item()
				.withPrimaryKey("id", newId)
				.withInt("principalId", principalId)
				.withString("createdAt", currentTime)
				.withMap("body", content);

		table.putItem(item);

		Entity event = Entity.builder()
				.id(newId)
				.principalId(principalId)
				.createdAt(currentTime)
				.body(content)
				.build();

        return Response.builder()
				.statusCode(201)
				.event(event)
				.build();

	}
}