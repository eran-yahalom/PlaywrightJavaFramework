package api;

import com.jayway.jsonpath.JsonPath;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import org.testng.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventApiService {

    private static final String BASE_URL = "https://api.eventhub.rahulshettyacademy.com";
    private static final String BOOKING_URL = "/api/bookings";
    private static final String EVENTS_URL = "/api/events";
    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";

    public static Map<String, Object> registerNewDriverAPI(APIRequestContext requestContext, Map<String, Object> payload) {
        Map<String, Object> registerDetails = new HashMap<>();

        APIResponse registerAPIResponse = requestContext.post(BASE_URL + REGISTER_URL,
                RequestOptions.create()
                        .setData(payload)
                        .setHeader("Content-Type", "application/json"));

        Assert.assertTrue(registerAPIResponse.ok(), "Registration API failed: " + registerAPIResponse.text());

        registerDetails.put("bearerToken", JsonPath.read(registerAPIResponse.text(), "$.token"));
        registerDetails.put("userId", JsonPath.read(registerAPIResponse.text(), "$.user.id"));
        registerDetails.put("status", String.valueOf(registerAPIResponse.status()));

        return registerDetails;
    }

    public static Map<String, Object> registerInvalidDriverAPI(APIRequestContext requestContext, Map<String, Object> payload) {
        Map<String, Object> registerDetails = new HashMap<>();

        APIResponse registerAPIResponse = requestContext.post(BASE_URL + REGISTER_URL,
                RequestOptions.create()
                        .setData(payload)
                        .setHeader("Content-Type", "application/json"));

        Assert.assertFalse(registerAPIResponse.ok());

        registerDetails.put("success", JsonPath.read(registerAPIResponse.text(), "$.success"));
        registerDetails.put("error", JsonPath.read(registerAPIResponse.text(), "$.error"));
        registerDetails.put("status", registerAPIResponse.status());

        return registerDetails;
    }

    public static Map<String, Object> invalidLoginFromAPI(APIRequestContext requestContext, Map<String, Object> payload) {
        Map<String, Object> loginDetails = new HashMap<>();

        APIResponse response = requestContext.post(BASE_URL + LOGIN_URL,
                RequestOptions.create()
                        .setData(payload)
                        .setHeader("Content-Type", "application/json"));

        loginDetails.put("status", response.status());
        loginDetails.put("error", JsonPath.read(response.text(), "$.error"));

        return loginDetails;
    }

    public static Map<String, Object> validLoginFromAPI(APIRequestContext requestContext, Map<String, Object> payload) {
        Map<String, Object> loginDetails = new HashMap<>();

        APIResponse response = requestContext.post(BASE_URL + LOGIN_URL,
                RequestOptions.create()
                        .setData(payload)
                        .setHeader("Content-Type", "application/json"));

        Assert.assertTrue(response.ok(), "Login API failed: " + response.text());

        loginDetails.put("status", response.status());
        loginDetails.put("success", JsonPath.read(response.text(), "$.success"));
        loginDetails.put("token", JsonPath.read(response.text(), "$.token"));

        return loginDetails;
    }

    public static Map<String, Object> createEventFromAPI(APIRequestContext requestContext, String token, Map<String, Object> payload) {
        Map<String, Object> eventDetails = new HashMap<>();

        APIResponse response = requestContext.post(BASE_URL + EVENTS_URL,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token)
                        .setHeader("Content-Type", "application/json")
                        .setData(payload));

        Assert.assertTrue(response.ok(), "Create event API failed: " + response.text());

        eventDetails.put("eventID", JsonPath.read(response.text(), "$.data.id"));
        eventDetails.put("status", String.valueOf(response.status()));

        return eventDetails;
    }

    public static Map<String, Object> bookEventFromAPI(APIRequestContext requestContext, String token, Map<String, Object> payload) {
        Map<String, Object> bookingDetails = new HashMap<>();

        APIResponse response = requestContext.post(BASE_URL + BOOKING_URL,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token)
                        .setHeader("Content-Type", "application/json")
                        .setData(payload));

        Assert.assertTrue(response.ok(), "Book event API failed: " + response.text());

        bookingDetails.put("bookingREF", JsonPath.read(response.text(), "$.data.bookingRef"));
        bookingDetails.put("bookingQTY", String.valueOf(JsonPath.read(response.text(), "$.data.numberOfTickets")));
        bookingDetails.put("bookingID", String.valueOf(JsonPath.read(response.text(), "$.data.id")));
        bookingDetails.put("totalPrice", String.valueOf(JsonPath.read(response.text(), "$.data.totalPrice")));
        bookingDetails.put("bookingStatus", JsonPath.read(response.text(), "$.data.status"));

        return bookingDetails;
    }

    public static Map<String, Object> getAllEvents(APIRequestContext requestContext, String token) {
        Map<String, Object> allEventsDetails = new HashMap<>();

        APIResponse response = requestContext.get(BASE_URL + EVENTS_URL,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token)
                        .setHeader("Content-Type", "application/json"));

        Assert.assertTrue(response.ok(), "Get all events API failed: " + response.text());

        List<Object> eventsList = JsonPath.read(response.text(), "$.data");
        List<Integer> idList = JsonPath.read(response.text(), "$.data[*].id");

        allEventsDetails.put("status", response.status());
        allEventsDetails.put("totalEvents", eventsList.size());
        allEventsDetails.put("idList", idList);

        return allEventsDetails;
    }

    public static Map<String, Object> deleteEvent(APIRequestContext requestContext, String token, int eventId) {
        Map<String, Object> deleteDetails = new HashMap<>();

        APIResponse response = requestContext.delete(BASE_URL + EVENTS_URL + "/" + eventId,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token)
                        .setHeader("Content-Type", "application/json"));

        Assert.assertTrue(response.ok(), "Delete event API failed: " + response.text());

        deleteDetails.put("status", response.status());
        deleteDetails.put("success", JsonPath.read(response.text(), "$.success"));

        return deleteDetails;
    }
}