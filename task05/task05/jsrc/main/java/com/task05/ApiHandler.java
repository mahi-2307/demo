package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestStreamHandler {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
			.withRegion("eu-central-1")
			.build();
	private final DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
	private final String DYNAMODB_TABLE_NAME = "cmtr-7767740d-Events";

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		JSONParser jsonParser = new JSONParser();
		JSONObject responseObject = new JSONObject();
		JSONObject responseBody = new JSONObject();

		try {
			// Parse the incoming request to get principalId and content
			JSONObject reqObject = (JSONObject) jsonParser.parse(bufferedReader);
			int principalId = Integer.parseInt(reqObject.get("principalId").toString());
			Map<String, String> content = (Map<String, String>) reqObject.get("content");

			// Generate UUID v4 for the event ID
			String id = UUID.randomUUID().toString();
			// Get current time in ISO 8601 format
			String createdAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

			// Store the item in DynamoDB
			Item item = new Item()
					.withPrimaryKey("id", id)
					.withNumber("principalId", principalId)
					.withString("createdAt", createdAt)
					.withMap("body", content);

			dynamoDB.getTable(DYNAMODB_TABLE_NAME).putItem(new PutItemSpec().withItem(item));

			// Prepare the response
			responseBody.put("id", id);
			responseBody.put("principalId", principalId);
			responseBody.put("createdAt", createdAt);
			responseBody.put("body", content);

			responseObject.put("statusCode", 201);
			responseObject.put("event", responseBody);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		outputStreamWriter.write(responseObject.toString());
		bufferedReader.close();
		outputStreamWriter.close();
	}
}
