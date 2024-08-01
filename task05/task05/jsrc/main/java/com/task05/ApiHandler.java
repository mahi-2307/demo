package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import org.joda.time.Instant;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<Object, Map<String, Object>> {

	public Map<String, Object> handleRequest(Object request, Context context) {
		AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withRegion("eu-central-1")
				.build();
		DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
		Table table = dynamoDB.getTable("cmtr-7767740d-Events");
		try {
			final Map<String, String> detailsMap = new HashMap<>();
			detailsMap.put("name","test");
			detailsMap.put("surname", "item");
//			PutItemOutcome outcome = table.putItem(new Item()
//					.withPrimaryKey("id",1)
//					.with("principalId",1)
//					.with("createdAt", Instant.now())
//					.withMap("content",detailsMap));
//			System.out.println(outcome.toString());
			Map<String, AttributeValue> attributesMap = new HashMap<>();
			attributesMap.put("id",new AttributeValue(String.valueOf(1)));
			attributesMap.put("content", new AttributeValue(String.valueOf(detailsMap)));
			amazonDynamoDB.putItem(
					"cmtr-7767740d-Events",attributesMap);


		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
		System.out.println("Hello from lambda");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 200);
		resultMap.put("body", "Hello from Lambda");
		return resultMap;
	}
}
