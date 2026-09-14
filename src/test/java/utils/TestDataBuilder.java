package utils;

import net.datafaker.Faker;

import java.util.HashMap;
import java.util.Map;

public class TestDataBuilder {

    static Faker faker = new Faker();

    public static Map<String, Object> getLoginPayload(String email,
                                                      String password) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        return payload;
    }

    public static Map<String, Object> getCreateEventPayload(String eventName,
                                                            String description,
                                                            String category,
                                                            String venue,
                                                            String city,
                                                            String eventDate,
                                                            int price,
                                                            int totalSeats) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", eventName);
        payload.put("description", description);
        payload.put("category", category);
        payload.put("venue", venue);
        payload.put("city", city);
        payload.put("eventDate", eventDate);
        payload.put("price", price);
        payload.put("totalSeats", totalSeats);

        return payload;
    }

    public static Map<String, Object> getCreateBookingPayload(String name,
                                                              String email,
                                                              String phone,
                                                              String quantity,
                                                              String eventId) {
        Map<String, Object> bookingData = new HashMap<>();

        bookingData.put("customerName", name);
        bookingData.put("customerEmail", email);
        bookingData.put("customerPhone", phone);
        bookingData.put("quantity", quantity);
        bookingData.put("eventId", eventId);

        return bookingData;

    }
}
