package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
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

    public APIGatewayProxyResponseEvent handleCreateReservation(APIGatewayProxyRequestEvent request) {
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

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(new JSONObject().put("reservationId", item.get("tableNumber").getN()).toString());

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error creating reservation: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent handleGetReservations(APIGatewayProxyRequestEvent request) {
        try {
            // Scan the Reservations table
            ScanRequest scanRequest = new ScanRequest().withTableName(reservationsTableName);
            ScanResult result = dynamoDBClient.scan(scanRequest);

            // Create a JSON array to hold the reservations
            JSONArray reservations = new JSONArray();
            List<Map<String, AttributeValue>> items = result.getItems();

            // Iterate through the result and add each item to the JSON array
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

            // Return the list of reservations in the response
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(new JSONObject().put("reservations", reservations).toString());

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error fetching reservations: " + e.getMessage());
        }
    }
}

