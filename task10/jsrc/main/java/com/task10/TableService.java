package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class TableService {

    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
    private final String tablesTableName = "Tables";

    public APIGatewayV2HTTPResponse handleGetTables(APIGatewayV2HTTPEvent request) {
        try {
            ScanRequest scanRequest = new ScanRequest().withTableName(tablesTableName);
            ScanResult result = dynamoDBClient.scan(scanRequest);

            // Convert the ScanResult to a JSON string for the response body
            JSONObject responseBody = new JSONObject();
            responseBody.put("items", result.getItems());

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(responseBody.toString())
                    .build();

        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error fetching tables: " + e.getMessage())
                    .build();
        }
    }

    public APIGatewayV2HTTPResponse handleCreateTable(APIGatewayV2HTTPEvent request) {
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

            JSONObject responseBody = new JSONObject();
            responseBody.put("id", json.getString("id"));

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(responseBody.toString())
                    .build();

        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error creating table: " + e.getMessage())
                    .build();
        }
    }

    public APIGatewayV2HTTPResponse handleGetTableById(APIGatewayV2HTTPEvent request) {
        try {
            String tableId = request.getPathParameters().get("tableId");

            GetItemRequest getItemRequest = new GetItemRequest()
                    .withTableName(tablesTableName)
                    .withKey(Map.of("id", new AttributeValue().withN(tableId)));

            GetItemResult result = dynamoDBClient.getItem(getItemRequest);

            // Convert the GetItemResult to a JSON string for the response body
            JSONObject responseBody = new JSONObject(result.getItem());

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(responseBody.toString())
                    .build();

        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error fetching table: " + e.getMessage())
                    .build();
        }
    }
}
