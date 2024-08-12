package com.task10.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.task10.dto.NumberWrapper;
import com.task10.dto.Tables;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostTables{

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    private final DynamoDB dynamoDb = new DynamoDB(client);
    private final String DYNAMODB_TABLE_NAME = System.getenv("tables_table");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public APIGatewayV2HTTPResponse handleRequestPost(APIGatewayV2HTTPEvent event) {
        try {
            Tables tables = objectMapper.readValue(event.getBody(), Tables.class);

            Item item = new Item()
                    .withPrimaryKey("id", tables.getId())
                    .withNumber("number", tables.getNumber().getValue())
                    .withNumber("places", tables.getPlaces())
                    .withBoolean("isVip", tables.isVip());



            Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
            table.putItem(item);

            JSONObject responseBody = new JSONObject();
            responseBody.put("id", tables.getId());

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(responseBody.toString())
                    .build();

        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }

    public APIGatewayV2HTTPResponse handleRequestGet() {
        try {
            Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
            ScanRequest scanRequest = new ScanRequest().withTableName(DYNAMODB_TABLE_NAME);
            ScanResult result = client.scan(scanRequest);

            // Convert DynamoDB items to Tables objects
            List<Tables> tablesList = new ArrayList<>();
            for (Map<String, AttributeValue> item : result.getItems()) {
                Tables tableItem = new Tables();
                tableItem.setId(Integer.parseInt(item.get("id").getN()));
                tableItem.setNumber(new NumberWrapper(Integer.parseInt(item.get("number").getM().get("value").getN())));
                tableItem.setPlaces(Integer.parseInt(item.get("places").getN()));
                tableItem.setVip(Boolean.parseBoolean(item.get("isVip").getBOOL().toString()));
                tablesList.add(tableItem);
            }

            JSONObject responseBody = new JSONObject();
            responseBody.put("tables", tablesList);

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(responseBody.toString())
                    .build();

        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }


    public APIGatewayV2HTTPResponse handleRequestGetById(int tableId) {
        try {
            Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
            Item item = table.getItem("id", tableId);

            if (item == null) {
                return createErrorResponse("Table not found.");
            }

            Tables tableData = objectMapper.convertValue(item.asMap(), Tables.class);

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(tableData))
                    .build();

        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }

    private APIGatewayV2HTTPResponse createErrorResponse(String errorMessage) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", errorMessage);

        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(400)
                .withBody(errorResponse.toString())
                .build();
    }
}
