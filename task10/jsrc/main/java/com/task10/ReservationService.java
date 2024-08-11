package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ReservationService {

    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
    private final String reservationsTableName = "Reservations";

    public APIGatewayV2HTTPResponse handleCreateReservation(APIGatewayV2HTTPEvent request) {
        try {
            JSONObject json = new JSONObject(request.getBody());
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("tableNumber", new AttributeValue().withN(json.getString("tableNumber")));
            item.put("clientName", new AttributeValue().withS(json.getString("clientName")));
            item.put("phoneNumber", new AttributeValue().withS(json.getString("phoneNumber")));
            item.put("date", new AttributeValue().withS(json.getString("date")));
            item.put("slotTimeStart", new AttributeValue().withS(json.getString("slotTimeStart")));
            item.put("slotTimeEnd", new AttributeValue().withS(json.getString("slotTimeEnd")));

            PutItemRequest putItemRequest = new PutItemRequest().withTableName(reservationsTableName).withItem(item);
            dynamoDBClient.putItem(putItemRequest);

            JSONObject responseBody = new JSONObject();
            responseBody.put("reservationId", item.get("tableNumber").getN());

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(responseBody.toString())
                    .build();

        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error creating reservation: " + e.getMessage())
                    .build();
        }
    }

    public APIGatewayV2HTTPResponse handleGetReservations(APIGatewayV2HTTPEvent request) {
        try {
            ScanRequest scanRequest = new ScanRequest().withTableName(reservationsTableName);
            ScanResult result = dynamoDBClient.scan(scanRequest);
            JSONArray reservations = new JSONArray();
            List<Map<String, AttributeValue>> items = result.getItems();
            for (Map<String, AttributeValue> item : items) {
                JSONObject reservation = new JSONObject();
                reservation.put("tableNumber", item.get("tableNumber").getN());
                reservation.put("clientName", item.get("clientName").getS());
                reservation.put("phoneNumber", item.get("phoneNumber").getS());
                reservation.put("date", item.get("date").getS());
                reservation.put("slotTimeStart", item.get("slotTimeStart").getS());
                reservation.put("slotTimeEnd", item.get("slotTimeEnd").getS());
                reservations.put(reservation);
            }

            JSONObject responseBody = new JSONObject();
            responseBody.put("reservations", reservations);

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withBody(responseBody.toString())
                    .build();

        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Error fetching reservations: " + e.getMessage())
                    .build();
        }
    }
}
