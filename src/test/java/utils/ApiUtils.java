package utils;

import com.jayway.jsonpath.JsonPath;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.testng.Assert;
import tests.BaseTest;

import java.util.HashMap;
import java.util.Map;

public class ApiUtils {

    private final APIRequestContext requestContext;
    private static final String BASE_URL = "https://api.eventhub.rahulshettyacademy.com";
    private static final String BOOKING_URL = "/api/bookings/";
    private static final String EVENTS_URL = "/api/events/";
    private static final String REGISTER_URL = "/api/auth/register";

    public ApiUtils(APIRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public static APIResponse getBookingByRef(APIRequestContext requestContext, String bookingRef, String token) {
        return requestContext.get(BASE_URL + BOOKING_URL + bookingRef,
                RequestOptions.create().setHeader("Authorization", "Bearer " + token));
    }

    // מתודת עזר למחיקת אירוע
    public static APIResponse deleteEvent(APIRequestContext requestContext, String eventId, String token) {
        return requestContext.delete(BASE_URL + EVENTS_URL + eventId,
                RequestOptions.create().setHeader("Authorization", "Bearer " + token));
    }

    public static Map<String, Object> registerNewDriverAPI(Map<String, Object> payload) {

        Playwright playwright = Playwright.create();
        APIRequestContext apiRequestContext = playwright.request().newContext();
        Map<String, Object> registerDetails = new HashMap<>();
        APIResponse registerAPIResponse = apiRequestContext.post(BASE_URL + REGISTER_URL,
                RequestOptions.create().setData(payload));

        Assert.assertTrue(registerAPIResponse.ok());

        registerDetails.put("bearerToken", JsonPath.read(registerAPIResponse.text(), "$.token"));
        registerDetails.put("userId", JsonPath.read(registerAPIResponse.text(), "$.user.id"));
        registerDetails.put("status", String.valueOf(registerAPIResponse.status()));

        return registerDetails;
    }

    public static Map<String, Object> registerInvalidDriverAPI(Map<String, Object> payload) {

        Playwright playwright = Playwright.create();
        APIRequestContext apiRequestContext = playwright.request().newContext();
        Map<String, Object> registerDetails = new HashMap<>();
        APIResponse registerAPIResponse = apiRequestContext.post(BASE_URL + REGISTER_URL,
                RequestOptions.create().setData(payload));

        Assert.assertFalse(registerAPIResponse.ok());

        registerDetails.put("success", JsonPath.read(registerAPIResponse.text(), "$.success"));
        registerDetails.put("error", JsonPath.read(registerAPIResponse.text(), "$.error"));
        registerDetails.put("status", String.valueOf(registerAPIResponse.status()));

        return registerDetails;
    }

    public Map<String, String> createNewEventAPI(String token, Map<String, Object> eventData) {
        Map<String, String> createEventData = new HashMap<>();
        APIResponse response = requestContext.post(BASE_URL + "/api/events",
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token)
                        .setData(eventData));

        Assert.assertNotNull(response);
        createEventData.put("eventID", JsonPath.read(response.text(), "$.data.id").toString());
        createEventData.put("eventHeader", JsonPath.read(response.text(), "$.data.title"));
        createEventData.put("seats", JsonPath.read(response.text(), "$.data.availableSeats").toString());
        createEventData.put("price", JsonPath.read(response.text(), "$.data.price"));
        createEventData.put("status", String.valueOf(response.status()));
        createEventData.put("statusText", response.statusText());

        return createEventData;
    }
}
