package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class TableService {

    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
    private final String tablesTableName = "Tables";

    public APIGatewayProxyResponseEvent handleGetTables(APIGatewayProxyRequestEvent request) {
        try {
            ScanRequest scanRequest = new ScanRequest().withTableName(tablesTableName);
            ScanResult result = dynamoDBClient.scan(scanRequest);

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(result.toString());

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error fetching tables: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent handleCreateTable(APIGatewayProxyRequestEvent request) {
        try {
            JSONObject json = new JSONObject(request.getBody());
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", new AttributeValue().withN(json.getString("id")));
            item.put("number", new AttributeValue().withN(json.getString("number")));
            item.put("places", new AttributeValue().withN(json.getString("places")));
            item.put("isVip", new AttributeValue().withBOOL(json.getBoolean("isVip")));
            if (json.has("minOrder")) {
                item.put("minOrder", new AttributeValue().withN(json.getString("minOrder")));
            }

            PutItemRequest putItemRequest = new PutItemRequest().withTableName(tablesTableName).withItem(item);
            dynamoDBClient.putItem(putItemRequest);

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(new JSONObject().put("id", json.getString("id")).toString());

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error creating table: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent handleGetTableById(APIGatewayProxyRequestEvent request) {
        try {
            String tableId = request.getPathParameters().get("tableId");

            GetItemRequest getItemRequest = new GetItemRequest()
                    .withTableName(tablesTableName)
                    .withKey(Map.of("id", new AttributeValue().withN(tableId)));

            GetItemResult result = dynamoDBClient.getItem(getItemRequest);

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(result.toString());

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error fetching table: " + e.getMessage());
        }
    }
}

