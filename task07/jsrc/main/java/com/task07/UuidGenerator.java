package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@LambdaHandler(lambdaName = "uuid_generator",
		roleName = "uuid_generator-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(
		targetRule = "uuid_trigger"
)
@DependsOn(
		name = "uuid_trigger",
		resourceType = ResourceType.CLOUDWATCH_RULE
)
public class UuidGenerator implements RequestHandler<ScheduledEvent, Map<String, Object>> {

	private static final String BUCKET_NAME = "uuid-storage";

	@Override
	public Map<String, Object> handleRequest(ScheduledEvent event, Context context) {
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
		String timestamp = Instant.now().toString();
		String fileName = timestamp + ".json";

		List<String> uuids = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			uuids.add(UUID.randomUUID().toString());
		}

		String content = "{\n  \"ids\": [\n    \"" + String.join("\",\n    \"", uuids) + "\"\n  ]\n}";

		s3Client.putObject(BUCKET_NAME, fileName, content);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("statusCode", 200);
		resultMap.put("body", "UUIDs generated and stored in S3 bucket");

		return resultMap;
	}
}
