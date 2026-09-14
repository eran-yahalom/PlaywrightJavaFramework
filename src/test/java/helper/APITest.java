package helper;

import com.jayway.jsonpath.JsonPath;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.datafaker.Faker;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class APITest {
    Faker faker = new Faker();
    String email = faker.internet().emailAddress();
    String password = faker.internet().password();

    @Test
    public void APITest() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();

        //register new driver

        HashMap<Object, Object> registerPayOut = new HashMap<>();
        registerPayOut.put("email", email);
        registerPayOut.put("password", password);
        Playwright playwright = Playwright.create();
        APIRequestContext apiRequestContext = playwright.request().newContext();

        APIResponse registerAPIResponse = apiRequestContext.post("https://api.eventhub.rahulshettyacademy.com/api/auth/register\n",
                RequestOptions.create().setData(registerPayOut));

        Assert.assertTrue(registerAPIResponse.ok());

        String bearerToken = JsonPath.read(registerAPIResponse.text(), "$.token");
        int userId = JsonPath.read(registerAPIResponse.text(), "$.user.id");

        Assert.assertEquals(registerAPIResponse.status(), 201);


        //login
        HashMap<Object, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", email);
        loginPayload.put("password", password);
//        Playwright playwright = Playwright.create();
//        APIRequestContext apiRequestContext = playwright.request().newContext();
        APIResponse apiResponse = apiRequestContext.post("https://api.eventhub.rahulshettyacademy.com/api/auth/login",
                RequestOptions.create().setData(loginPayload));

        String bearerTokenUser = JsonPath.read(apiResponse.text(), "$.token");
        Assert.assertTrue(apiResponse.ok());
        Assert.assertEquals(apiResponse.status(), 200);

        //create event
        HashMap<Object, Object> createEventDataPayLoad = new HashMap<>();
        createEventDataPayLoad.put("title", "New postman event-1");
        createEventDataPayLoad.put("description", "postman API event");
        createEventDataPayLoad.put("category", "Sports");
        createEventDataPayLoad.put("venue", "23223");
        createEventDataPayLoad.put("city", "ssdsd");
        createEventDataPayLoad.put("eventDate", "2026-08-21T13:13:00.000Z");
        createEventDataPayLoad.put("price", 100);
        createEventDataPayLoad.put("totalSeats", 300);


        APIResponse createEventResponse = apiRequestContext.post("https://api.eventhub.rahulshettyacademy.com/api/events",
                RequestOptions.create().setHeader("Authorization", "Bearer " + bearerToken)
                        .setData(createEventDataPayLoad));

        Assert.assertTrue(createEventResponse.ok());
        String id = JsonPath.read(createEventResponse.text(), "$.data.id").toString();
        Map<String, Object> createEventData = JsonPath.read(createEventResponse.text(), "$.data");
        Object seats = createEventData.get("totalSeats");
        int idNumber = (int) createEventData.get("id");

        // update event - use : createEventDataPayLoad existing eveny layout
        //if we use a new map then we need to add all fields even if we dont want to update them
        HashMap<Object, Object> updateEventData = new HashMap<>(createEventDataPayLoad);

// Modify only the fields you want to update
        updateEventData.put("title", "New postman event-2");
        updateEventData.put("description", "postman API event2");

        APIResponse updateEventResponse = apiRequestContext.put("https://api.eventhub.rahulshettyacademy.com/api/events/" + idNumber,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + bearerToken)
                        .setData(updateEventData)
        );

        Assert.assertTrue(updateEventResponse.ok());
        Assert.assertEquals(updateEventResponse.status(), 200);
        Map<String, Object> UpdateEventData = JsonPath.read(updateEventResponse.text(), "$.data");
        Assert.assertEquals(UpdateEventData.get("title"), "New postman event-2");
        Assert.assertEquals(UpdateEventData.get("description"), "postman API event2");

        // get event
        APIResponse getEventResponse = apiRequestContext.get("https://api.eventhub.rahulshettyacademy.com/api/events/" + idNumber,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + bearerToken)

        );
        Assert.assertTrue(getEventResponse.ok());
        Map<String, Object> eventData = JsonPath.read(createEventResponse.text(), "$.data");
        Assert.assertEquals(eventData.get("id"), idNumber);
        // get events

        APIResponse getEventsResponse = apiRequestContext.get("https://api.eventhub.rahulshettyacademy.com/api/events",
                RequestOptions.create()
                        .setQueryParam("page", "1")
                        .setQueryParam("limit", "11")
                        .setHeader("Authorization", "Bearer " + bearerToken)

        );
        Assert.assertTrue(getEventsResponse.ok());
        List<String> allEventsIds = JsonPath.read(getEventsResponse.text(), "$.data[*].id");
        //Assert.assertTrue(allEventsIds.contains("11"));


        List<Map<String, Object>> getEventsData = JsonPath.read(getEventsResponse.text(), "$.data");
        String eventId = getEventsData.getLast().get("id").toString();

        //create booking of event
        HashMap<Object, Object> createNewBookingData = new HashMap<>();
        createNewBookingData.put("customerName", "eran c");
        createNewBookingData.put("customerEmail", "yahalomern@gmail.com");
        createNewBookingData.put("customerPhone", "+972506534632");
        createNewBookingData.put("quantity", 2);
        createNewBookingData.put("eventId", Integer.parseInt(eventId));

        APIResponse createNewBooking = apiRequestContext.post("https://api.eventhub.rahulshettyacademy.com/api/bookings",
                RequestOptions.create().setHeader("Authorization", "Bearer " + bearerToken)
                        .setData(createNewBookingData));

        Assert.assertTrue(createNewBooking.ok());
        Map<Object, Object> createBooking = JsonPath.read(createNewBooking.text(), "$.data");

        String bookingRef = createBooking.get("bookingRef").toString();
        Assert.assertEquals(createBooking.get("customerName"), "eran c");

        // get booking by ref number
        APIResponse getBookingDetailsByRef = apiRequestContext.get("https://api.eventhub.rahulshettyacademy.com/api/bookings/ref/" + bookingRef,

                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + bearerToken)
        );
        Assert.assertTrue(getBookingDetailsByRef.ok());
        Map<Object, Object> bookingByRefData = JsonPath.read(getBookingDetailsByRef.text(), "$.data");

        Assert.assertEquals(bookingByRefData.get("bookingRef"), bookingRef);
        // delete event
        APIResponse deleteResponse = apiRequestContext.delete("https://api.eventhub.rahulshettyacademy.com/api/events/" + eventId,
                RequestOptions.create().setHeader("Authorization", "Bearer " + bearerToken)
        );

        Assert.assertEquals(deleteResponse.status(), 200);
        Assert.assertFalse(allEventsIds.contains(eventId));

    }
}
