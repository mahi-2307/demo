package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import org.joda.time.Instant;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "audit_producer",
	roleName = "audit_producer-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(
		targetTable = "Configuration",
		batchSize = 1
)
@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE)
@EnvironmentVariables(
		@EnvironmentVariable(
				key = "target_table", value = "${target_table}"
		)
)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {
private final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
		.standard()
		.withRegion("eu-central-1")
		.build();
	private final DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
	private final Table auditTable = dynamoDB.getTable("Audit");
	public Void handleRequest(DynamodbEvent  dynamodbEvent, Context context) {
		for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
			if ("INSERT".equals(record.getEventName())) {
				handleInsert(record);
			} else if ("MODIFY".equals(record.getEventName())) {
				handleModify(record);
			}
		}
		return null;
	}

	private void handleInsert(DynamodbEvent.DynamodbStreamRecord record) {
		Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> newImage = record.getDynamodb().getNewImage();

		Map<String, Object> newValue = new HashMap<>();
		newValue.put("key", newImage.get("key").getS());
		newValue.put("value", newImage.get("value").getN());
		Item item = new Item().withPrimaryKey("id", UUID.randomUUID().toString())
				.withString("itemKey", newImage.get("key").getS())
				.withString("modificationTime", Instant.now().toString())
				.withMap("newValue", newValue);

		auditTable.putItem(item);


	}

	private void handleModify(DynamodbEvent.DynamodbStreamRecord record) {
		Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();
		Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> newImage = record.getDynamodb().getNewImage();

		String updatedAttribute = "value";
		String oldValue = oldImage.get("value").getN();
		String newValue = newImage.get("value").getN();

		if (!oldValue.equals(newValue)) {
			Item item = new Item()
					.withPrimaryKey("id", UUID.randomUUID().toString())
					.withString("itemKey", newImage.get("key").getS())
					.withString("modificationTime", Instant.now().toString())
					.withString("updatedAttribute",updatedAttribute)
					.withString("oldValue", oldValue)
					.withString("newValue", newValue);

			auditTable.putItem(item);
		}
	}
}
