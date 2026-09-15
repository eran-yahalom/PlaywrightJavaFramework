package api;

import com.jayway.jsonpath.JsonPath;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.testng.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventApiService {

    private final APIRequestContext requestContext;
    private static final String BASE_URL = "https://api.eventhub.rahulshettyacademy.com";
    private static final String BOOKING_URL = "/api/bookings";
    private static final String EVENTS_URL = "/api/events";
    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";
    private static final String DELETE_URL = "/api/events/";

    public EventApiService(APIRequestContext requestContext) {
        this.requestContext = requestContext;
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

    public static Map<String, Object> invalidLoginFromAPI(Map<String, Object> payload) {
        Map<String, Object> loginDetails = new HashMap<>();
        Playwright playwright = Playwright.create();
        APIRequestContext requestContext = playwright.request().newContext();
        APIResponse response = requestContext.post(BASE_URL + LOGIN_URL,
                RequestOptions.create().setData(payload));

        Assert.assertNotNull(response);
        loginDetails.put("success", JsonPath.read(response.text(), "$.success"));
        loginDetails.put("error", JsonPath.read(response.text(), "$.error"));
        loginDetails.put("status", response.status());

        return loginDetails;
    }

    public static Map<String, Object> validLoginFromAPI(Map<String, Object> payload) {
        Map<String, Object> loginDetails = new HashMap<>();
        Playwright playwright = Playwright.create();
        APIRequestContext requestContext = playwright.request().newContext();
        APIResponse response = requestContext.post(BASE_URL + LOGIN_URL,
                RequestOptions.create().setData(payload));

        Assert.assertNotNull(response);
        loginDetails.put("success", JsonPath.read(response.text(), "$.success"));
        loginDetails.put("bearerToken", JsonPath.read(response.text(), "$.token"));
        loginDetails.put("userID", JsonPath.read(response.text(), "$.user.id"));
        loginDetails.put("status", response.status());

        return loginDetails;
    }

    public static Map<String, Object> createEventFromAPI(String token, Map<String, Object> eventData) {
        Map<String, Object> eventDetails = new HashMap<>();
        Playwright playwright = Playwright.create();
        APIRequestContext requestContext = playwright.request().newContext();
        APIResponse response = requestContext.post(BASE_URL + EVENTS_URL,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token)
                        .setData(eventData));

        Assert.assertNotNull(response);

        eventDetails.put("status", response.status());
        eventDetails.put("eventID", JsonPath.read(response.text(), "$.data.id"));
        eventDetails.put("price", JsonPath.read(response.text(), "$.data.price"));
        eventDetails.put("totalSeats", JsonPath.read(response.text(), "$.data.totalSeats"));
        eventDetails.put("availableSeats", JsonPath.read(response.text(), "$.data.availableSeats"));
        eventDetails.put("eventDate", JsonPath.read(response.text(), "$.data.eventDate"));
        eventDetails.put("title", JsonPath.read(response.text(), "$.data.title"));
        eventDetails.put("category", JsonPath.read(response.text(), "$.data.category"));
        eventDetails.put("venue", JsonPath.read(response.text(), "$.data.venue"));

        return eventDetails;
    }

    public static Map<String, Object> bookEventFromAPI(String token, Map<String, Object> bookingData) {
        Map<String, Object> bookingEventData = new HashMap<>();
        Playwright playwright = Playwright.create();
        APIRequestContext requestContext = playwright.request().newContext();
        APIResponse response = requestContext.post(BASE_URL + BOOKING_URL,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token)
                        .setData(bookingData));

        bookingEventData.put("bookingID", JsonPath.read(response.text(), "$.data.id").toString());
        bookingEventData.put("eventID", JsonPath.read(response.text(), "$.data.event.id").toString());
        bookingEventData.put("bookingQTY", JsonPath.read(response.text(), "$.data.quantity").toString());
        bookingEventData.put("bookingStatus", JsonPath.read(response.text(), "$.data.status"));
        bookingEventData.put("bookingREF", JsonPath.read(response.text(), "$.data.bookingRef"));
        bookingEventData.put("bookingRCategory", JsonPath.read(response.text(), "$.data.event.category"));
        bookingEventData.put("bookingVenue", JsonPath.read(response.text(), "$.data.event.venue"));
        bookingEventData.put("totalPrice", JsonPath.read(response.text(), "$.data.totalPrice"));

        return bookingEventData;
    }

    public static Map<String, Object> getAllEvents(String token) {
        Map<String, Object> allEventsData = new HashMap<>();
        Playwright playwright = Playwright.create();
        APIRequestContext requestContext = playwright.request().newContext();
        APIResponse response = requestContext.get(BASE_URL + EVENTS_URL,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token));

        List<Integer> eventIds = JsonPath.read(response.text(), "$.data[*].id");
        allEventsData.put("status", response.status());
        allEventsData.put("totalEvents", JsonPath.read(response.text(), "$.pagination.total"));
        allEventsData.put("idList", eventIds);

        return allEventsData;
    }

    public static Map<String, Object> deleteEvent(String token, Object eventId) {
        Map<String, Object> allEventsData = new HashMap<>();
        Playwright playwright = Playwright.create();
        APIRequestContext requestContext = playwright.request().newContext();
        APIResponse response = requestContext.delete(BASE_URL + DELETE_URL + eventId,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token));

        allEventsData.put("success", JsonPath.read(response.text(), "$.success"));
        allEventsData.put("message", JsonPath.read(response.text(), "$.message"));

        return allEventsData;
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

    public APIResponse updateEvent(String token, int eventId, Map<String, Object> updateData) {
        return requestContext.put(BASE_URL + "/api/events/" + eventId,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token)
                        .setData(updateData));
    }

    public APIResponse getEventById(String token, int eventId) {
        return requestContext.get(BASE_URL + "/api/events/" + eventId,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token));
    }

    public APIResponse getAllEvents(String token, int page, int limit) {
        return requestContext.get(BASE_URL + "/api/events",
                RequestOptions.create()
                        .setQueryParam("page", String.valueOf(page))
                        .setQueryParam("limit", String.valueOf(limit))
                        .setHeader("Authorization", "Bearer " + token));
    }

    public APIResponse createBooking(String token, String name, String email, String phone, int quantity, int eventId) {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("customerName", name);
        bookingData.put("customerEmail", email);
        bookingData.put("customerPhone", phone);
        bookingData.put("quantity", quantity);
        bookingData.put("eventId", eventId);

        return requestContext.post(BASE_URL + "/api/bookings",
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token)
                        .setData(bookingData));
    }

    public APIResponse getBookingByRef(String token, String bookingRef) {
        return requestContext.get(BASE_URL + "/api/bookings/ref/" + bookingRef,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + token));
    }
}