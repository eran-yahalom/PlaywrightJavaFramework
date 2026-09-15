package tests;

import api.EventApiService;
import com.jayway.jsonpath.JsonPath;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;
import utils.MockConstants;
import utils.TestDataBuilder;
import utils.TestDataUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.awaitility.Awaitility.await;

public class BookingTest extends BaseTest {


    @Test(description = "Create booking from API and validate UI booking details")
    public void getBookingDetails() {
        LoginPage loginPage = new LoginPage(getPage());
        HeaderComponent headerComponent = new HeaderComponent(getPage());
        DashboardPage dashboardPage = new DashboardPage(getPage());

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);
        String token = driverDetails.get("bearerToken").toString();

        Map<String, Object> eventPayload = TestDataBuilder.getCreateEventPayload(
                eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getVenue(),
                TestDataUtils.getCity(),
                TestDataUtils.getFutureIsoDate(),
                TestDataUtils.getPrice(),
                TestDataUtils.getSeats()
        );

        Map<String, Object> event = EventApiService.createEventFromAPI(token, eventPayload);

        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() ->
                        Assert.assertEquals(String.valueOf(event.get("status")), "201")
                );
        String createdEventID = String.valueOf(event.get("eventID"));

        Map<String, Object> bookingPayload = TestDataBuilder.getCreateBookingPayload(
                TestDataUtils.getFullName(),
                TestDataUtils.getEmail(),
                TestDataUtils.getPhoneNumber(),
                String.valueOf(TestDataUtils.getQty()),
                createdEventID
        );

        Map<String, Object> booking = EventApiService.bookEventFromAPI(token, bookingPayload);

        Assert.assertTrue(loginPage.loginToApplication(email, password), "Login failed");
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();

        MyBookingsPage myBookingsPage = headerComponent.goToMyBookings();
        List<Map<String, String>> bookingData = myBookingsPage.getAllBookingCardDetails(eventName);

        Assert.assertEquals(booking.get("bookingREF"), bookingData.getFirst().get("bookingRef"));
        Assert.assertEquals(booking.get("bookingQTY"), bookingData.getFirst().get("ticketsNumber").split(" ")[1]);
        Assert.assertEquals(booking.get("bookingID"), bookingData.getFirst().get("testID").replace("#", ""));
        Assert.assertEquals(booking.get("totalPrice"), bookingData.getFirst().get("totalPrice").replace(",", "").trim());
    }

    @Test(description = "Book event from UI")
    public void bookEventFromUI() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        String userName = TestDataUtils.getFullName();
        String phone = TestDataUtils.getPhoneNumber();

        LoginPage loginPage = new LoginPage(getPage());

        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        adminEventPage.goTo();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));
        assertThat(getPage().getByText("Event created!")).isVisible();
        eventsPage.goToEventsPage();

        BookingFormPage bookingFormPage = eventsPage.proceedToBookingEvent(eventName);
        bookingFormPage.bookAnEvent(userName, email, phone);
        assertThat(getPage().getByText("Booking Confirmed!")).isVisible();
        Map<String, Object> createBookingPageData = bookingFormPage.getBookingConfirmedData();
        MyBookingsPage myBookingsPage = bookingFormPage.clickOnViewMyBookingButton();

        List<Map<String, String>> bookingData = myBookingsPage.getAllBookingCardDetails(eventName);

        Assert.assertEquals(createBookingPageData.get("BookingRef"), bookingData.getFirst().get("bookingRef"));
        Assert.assertEquals(createBookingPageData.get("Tickets"), bookingData.getFirst().get("ticketsNumber").split(" ")[1]);
        Assert.assertEquals(createBookingPageData.get("TotalPrice"), bookingData.getFirst().get("totalPrice").replace(",", "").trim());
    }


    @Test(description = "Delete booking from my booking page")
    public void deleteBookingFromMyBookingTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        String userName = TestDataUtils.getFullName();
        String phone = TestDataUtils.getPhoneNumber();

        LoginPage loginPage = new LoginPage(getPage());

        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        adminEventPage.goTo();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));
        assertThat(getPage().getByText("Event created!")).isVisible();
        eventsPage.goToEventsPage();

        BookingFormPage bookingFormPage = eventsPage.proceedToBookingEvent(eventName);
        bookingFormPage.bookAnEvent(userName, email, phone);
        assertThat(getPage().getByText("Booking Confirmed!")).isVisible();
        MyBookingsPage myBookingsPage = bookingFormPage.clickOnViewMyBookingButton();
        int bookingCountBefore = myBookingsPage.getNumberBookingOfCards();
        ViewBookingDetailsPage viewBookingDetailsPage = myBookingsPage.clickOnViewDetailsButton();
        myBookingsPage = viewBookingDetailsPage.clickOnCancelButton();

        assertThat(getPage().getByText("Booking cancelled successfully")).isVisible();
        assertThat(getPage().getByText("No bookings yet")).isVisible();

        int bookingCountAfterDelete = myBookingsPage.getNumberBookingOfCards();
        Assert.assertEquals(bookingCountBefore, bookingCountAfterDelete + 1);
    }

    @Test(description = "Delete booking from view booking details page")
    public void deleteBookingFromViewBookingDetailsTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        String userName = TestDataUtils.getFullName();
        String phone = TestDataUtils.getPhoneNumber();

        LoginPage loginPage = new LoginPage(getPage());

        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        adminEventPage.goTo();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));
        assertThat(getPage().getByText("Event created!")).isVisible();
        eventsPage.goToEventsPage();

        BookingFormPage bookingFormPage = eventsPage.proceedToBookingEvent(eventName);
        bookingFormPage.bookAnEvent(userName, email, phone);

        assertThat(getPage().getByText("Booking Confirmed!")).isVisible();
        MyBookingsPage myBookingsPage = bookingFormPage.clickOnViewMyBookingButton();
        int bookingCountBefore = myBookingsPage.getNumberBookingOfCards();

        Assert.assertTrue(myBookingsPage.clickOnCancelBookingButton());
        assertThat(getPage().getByText("Booking cancelled successfully")).isVisible();
        assertThat(getPage().getByText("No bookings yet")).isVisible();

        int bookingCountAfterDelete = myBookingsPage.getNumberBookingOfCards();
        Assert.assertEquals(bookingCountBefore, bookingCountAfterDelete + 1);
    }

    @Test(description = "Go back to bookings page from view booking details page")
    public void goBackToBookingDetailsPageTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        String userName = TestDataUtils.getFullName();
        String phone = TestDataUtils.getPhoneNumber();

        LoginPage loginPage = new LoginPage(getPage());

        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        adminEventPage.goTo();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));
        assertThat(getPage().getByText("Event created!")).isVisible();
        eventsPage.goToEventsPage();

        BookingFormPage bookingFormPage = eventsPage.proceedToBookingEvent(eventName);
        bookingFormPage.bookAnEvent(userName, email, phone);
        assertThat(getPage().getByText("Booking Confirmed!")).isVisible();
        MyBookingsPage myBookingsPage = bookingFormPage.clickOnViewMyBookingButton();
        int bookingCountBefore = myBookingsPage.getNumberBookingOfCards();
        ViewBookingDetailsPage viewBookingDetailsPage = myBookingsPage.clickOnViewDetailsButton();
        myBookingsPage = viewBookingDetailsPage.clickOnBackToMyBookingButton();

        assertThat(getPage().getByText("Clear all bookings")).isVisible();
        int bookingCountAfter = myBookingsPage.getNumberBookingOfCards();

        Assert.assertEquals(bookingCountBefore, bookingCountAfter);
    }

    @Test(description = "Check eligibility for refound test")
    public void eligibilityForRefoundTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        String userName = TestDataUtils.getFullName();
        String phone = TestDataUtils.getPhoneNumber();

        LoginPage loginPage = new LoginPage(getPage());

        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        adminEventPage.goTo();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));
        assertThat(getPage().getByText("Event created!")).isVisible();
        eventsPage.goToEventsPage();

        BookingFormPage bookingFormPage = eventsPage.proceedToBookingEvent(eventName);
        bookingFormPage.bookAnEvent(userName, email, phone);
        assertThat(getPage().getByText("Booking Confirmed!")).isVisible();
        MyBookingsPage myBookingsPage = bookingFormPage.clickOnViewMyBookingButton();
        ViewBookingDetailsPage viewBookingDetailsPage = myBookingsPage.clickOnViewDetailsButton();
        viewBookingDetailsPage.clickOnRefoundLink();

        getPage().getByTestId("refund-spinner").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN));

        assertThat(getPage().getByText("Eligible for refund. Single-ticket bookings qualify for a full refund.")).isVisible();
        assertThat(getPage().getByText("Event Details")).isVisible();
    }

    @Test(description = "Check My booking page event data matches the booking card")
    public void myBookingEventDataTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        String userName = TestDataUtils.getFullName();
        String phone = TestDataUtils.getPhoneNumber();
        String category = TestDataUtils.getCategory();

        LoginPage loginPage = new LoginPage(getPage());

        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        adminEventPage.goTo();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                category,
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));
        assertThat(getPage().getByText("Event created!")).isVisible();

        eventsPage.goToEventsPage();

        BookingFormPage bookingFormPage = eventsPage.proceedToBookingEvent(eventName);
        bookingFormPage.bookAnEvent(userName, email, phone);
        assertThat(getPage().getByText("Booking Confirmed!")).isVisible();

        MyBookingsPage myBookingsPage = bookingFormPage.clickOnViewMyBookingButton();

        List<Map<String, String>> bookingData = myBookingsPage.getAllBookingCardDetails(eventName);
        ViewBookingDetailsPage viewBookingDetailsPage = myBookingsPage.clickOnViewDetailsButton();

        Map<String, Object> eventDetails = viewBookingDetailsPage.getMyBookingEventDetails();
        Map<String, Object> eventHederValues = viewBookingDetailsPage.getMyBookingHeaderValues();


        Assert.assertEquals(eventHederValues.get("bookingRef"), bookingData.getFirst().get("bookingRef"));
        Assert.assertEquals(eventDetails.get("Category"), category);
        Assert.assertEquals(eventDetails.get("Event"), eventName);
        Assert.assertEquals(eventDetails.get("City"), bookingData.getFirst().get("city"));

        String rawDate1 = eventDetails.get("Date").toString().split(", ")[1].trim(); // "11 July 2027"
        String rawDate2 = bookingData.getFirst().get("date").replaceAll("^[^a-zA-Z0-9]+", "").trim(); // "11 Jul 2027"

        String formattedDate1 = LocalDate.parse(rawDate1, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
                .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));

        Assert.assertEquals(formattedDate1, rawDate2);
    }

    @Test(description = "Remove all booking")
    public void removeAllBooking() {
        LoginPage loginPage = new LoginPage(getPage());
        HeaderComponent headerComponent = new HeaderComponent(getPage());
        DashboardPage dashboardPage = new DashboardPage(getPage());

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        String userName = TestDataUtils.getFullName();
        String phone = TestDataUtils.getPhoneNumber();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);
        String token = driverDetails.get("bearerToken").toString();

        Map<String, Object> eventPayload = TestDataBuilder.getCreateEventPayload(
                eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getVenue(),
                TestDataUtils.getCity(),
                TestDataUtils.getFutureIsoDate(),
                TestDataUtils.getPrice(),
                TestDataUtils.getSeats()
        );

        Map<String, Object> event = EventApiService.createEventFromAPI(token, eventPayload);

        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() ->
                        Assert.assertEquals(String.valueOf(event.get("status")), "201")
                );
        String createdEventID = String.valueOf(event.get("eventID"));

        Map<String, Object> bookingPayload = TestDataBuilder.getCreateBookingPayload(
                userName,
                email,
                phone,
                String.valueOf(TestDataUtils.getQty()),
                createdEventID
        );

        Map<String, Object> booking = EventApiService.bookEventFromAPI(token, bookingPayload);
        Assert.assertEquals(booking.get("bookingStatus"), "confirmed");

        Assert.assertTrue(loginPage.loginToApplication(email, password), "Login failed");
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();

        MyBookingsPage myBookingsPage = headerComponent.goToMyBookings();
        int numberOfBookingCardsBefore = myBookingsPage.getNumberBookingOfCards();
        myBookingsPage.clearAllBooking();

        assertThat(getPage().getByText("Booking cancelled successfully")).isVisible();
        assertThat(getPage().getByText("No bookings yet")).isVisible();

        int numberOfBookingCardsAfter = myBookingsPage.getNumberBookingOfCards();
        Assert.assertEquals(numberOfBookingCardsBefore, numberOfBookingCardsAfter + 1);
    }

    @Test(description = "Book all seats")
    public void bookAlSeats() {
        LoginPage loginPage = new LoginPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());
        DashboardPage dashboardPage = new DashboardPage(getPage());

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        String userName = TestDataUtils.getFullName();
        String phone = TestDataUtils.getPhoneNumber();
        int numOfSeats = 11;

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);
        String token = driverDetails.get("bearerToken").toString();

        Map<String, Object> eventPayload = TestDataBuilder.getCreateEventPayload(
                eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getVenue(),
                TestDataUtils.getCity(),
                TestDataUtils.getFutureIsoDate(),
                TestDataUtils.getPrice(),
                numOfSeats
        );

        Map<String, Object> event = EventApiService.createEventFromAPI(token, eventPayload);

        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() ->
                        Assert.assertEquals(String.valueOf(event.get("status")), "201")
                );
        String createdEventID = String.valueOf(event.get("eventID"));

        Map<String, Object> firstBookingPayload = TestDataBuilder.getCreateBookingPayload(
                userName,
                email,
                phone,
                String.valueOf(numOfSeats - 1),
                createdEventID
        );

        Map<String, Object> booking = EventApiService.bookEventFromAPI(token, firstBookingPayload);
        Assert.assertEquals(booking.get("bookingStatus"), "confirmed");

        Map<String, Object> secondBookingPayload = TestDataBuilder.getCreateBookingPayload(
                userName,
                email,
                phone,
                String.valueOf(1),
                createdEventID
        );

        Map<String, Object> bookingSecond = EventApiService.bookEventFromAPI(token, secondBookingPayload);
        Assert.assertEquals(bookingSecond.get("bookingStatus"), "confirmed");

        Assert.assertTrue(loginPage.loginToApplication(email, password), "Login failed");
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();

        Locator card = eventsPage.getEventCard(eventName);
        Assert.assertTrue(card.getByTestId("book-now-btn").isDisabled());
        assertThat(card.locator("[class*='items-center'] [class$='rounded-full']")).isVisible();
    }

    @Test(description = "Perform booking without details")
    public void bookWithoutDetails() {
        LoginPage loginPage = new LoginPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());
        DashboardPage dashboardPage = new DashboardPage(getPage());

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);
        String token = driverDetails.get("bearerToken").toString();

        Map<String, Object> eventPayload = TestDataBuilder.getCreateEventPayload(
                eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getVenue(),
                TestDataUtils.getCity(),
                TestDataUtils.getFutureIsoDate(),
                TestDataUtils.getPrice(),
                TestDataUtils.getSeats()
        );

        Map<String, Object> eventResponseData = EventApiService.createEventFromAPI(token, eventPayload);

        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() ->
                        Assert.assertEquals(String.valueOf(eventResponseData.get("status")), "201")
                );

        Assert.assertTrue(loginPage.loginToApplication(email, password), "Login failed");
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();

        eventsPage.clickOnEventByTitle(eventName);
        getPage().locator("#confirm-booking").click();
        assertThat(getPage().getByText("Name must be at least 2 chars")).isVisible();
        assertThat(getPage().getByText("Enter a valid email")).isVisible();
        assertThat(getPage().getByText("Enter a valid 10-digit phone")).isVisible();
    }

    @Test(description = "Mock creating bookings with Next.js interception support")
    public void mockCreateBooking() throws IOException {
        LoginPage loginPage = new LoginPage(getPage());
        HeaderComponent headerComponent = new HeaderComponent(getPage());

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        DashboardPage dashboardPage = new DashboardPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);

        Assert.assertEquals(driverDetails.get("status"), "201");

        String token = driverDetails.get("bearerToken").toString();
        int userId = (int) driverDetails.get("userId");

        String eventTitle = "New postman event-1";
        Map<String, Object> eventPayload = TestDataBuilder.getCreateEventPayload(
                eventTitle,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getVenue(),
                TestDataUtils.getCity(),
                TestDataUtils.getFutureIsoDate(),
                TestDataUtils.getPrice(),
                TestDataUtils.getSeats()
        );

        Map<String, Object> event = EventApiService.createEventFromAPI(token, eventPayload);

        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() ->
                        Assert.assertEquals(String.valueOf(event.get("status")), "201")
                );

        int eventId = (int) event.get("eventID");

        String jsonContent = Files.readString(Paths.get("src/test/resources/booking_2.json"));
        String updatedJson = jsonContent
                .replace("{{EVENT_ID}}", String.valueOf(eventId))
                .replace("{{USER_ID}}", String.valueOf(userId));

        getPage().route("**/*bookings*", route -> {
            String resourceType = route.request().resourceType();
            String url = route.request().url();

            if ("fetch".equals(resourceType) || "xhr".equals(resourceType) || url.contains("/api/")) {

                if ("OPTIONS".equalsIgnoreCase(route.request().method())) {
                    route.fulfill(new Route.FulfillOptions()
                            .setStatus(200)
                            .setHeaders(MockConstants.CORS_HEADERS));
                    return;
                }

                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setHeaders(MockConstants.CORS_HEADERS)
                        .setBody(updatedJson));
            } else {
                route.fallback();
            }
        });

        Assert.assertTrue(loginPage.loginToApplication(email, password), "Login failed");
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();

        MyBookingsPage myBookingsPage = headerComponent.goToMyBookings();

        List<Map<String, String>> bookingData = myBookingsPage.getAllBookingCardDetails(eventTitle);

        Assert.assertEquals(bookingData.getFirst().get("bookingRef"), JsonPath.read(jsonContent, "$.data[0].bookingRef"));
        Assert.assertEquals(bookingData.getLast().get("bookingRef"), JsonPath.read(jsonContent, "$.data[1].bookingRef"));
    }
}