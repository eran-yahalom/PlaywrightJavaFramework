package tests;

import api.EventApiService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Route;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.*;
import utils.DataProviderUtils;
import utils.DateUtils;
import utils.TestDataBuilder;
import utils.TestDataUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.awaitility.Awaitility.await;

public class EventTest extends BaseTest {

    @DataProvider(name = "eventData")
    public Object[][] eventData() throws IOException {
        return DataProviderUtils.getJsonDataToMap("/src/test/resources/eventBookingData.json");
    }

    @Test(dataProvider = "eventData", description = "Add event test", priority = 1)
    public void addNewEvent(HashMap<String, String> eventData) {
        LoginPage loginPage = new LoginPage(getPage());
        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(getPage().request(), payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        adminEventPage.goTo();
        adminEventPage.createNewEvent(eventName,
                eventData.get("description"),
                eventData.get("category"),
                eventData.get("city"),
                eventData.get("venue"),
                eventData.get("eventDate"),
                eventData.get("price"),
                eventData.get("availableSeats"));
        assertThat(getPage().getByText("Event created!")).isVisible();
        eventsPage.goToEventsPage();
        Map<String, String> eventDetails = eventsPage.getCardDetails(eventName);
        Assert.assertEquals(eventDetails.get("title"), eventName, "Event title does not match");
        Assert.assertEquals(eventDetails.get("venue"), eventData.get("venue") + ", " + eventData.get("city"), "Event venue does not match");
        Assert.assertEquals(eventDetails.get("date"), DateUtils.formatDate(eventData.get("eventDate")), "Event date does not match");
        Assert.assertEquals(eventDetails.get("category"), eventData.get("category"), "Event address does not match");
    }

    @Test(description = "Check that newly created event is visible in events page", priority = 1)
    public void checkEventIsInEventsPageTest() {
        LoginPage loginPage = new LoginPage(getPage());
        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(getPage().request(), payload);

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
        Assert.assertTrue(eventsPage.isNewCreatedEventVisible(eventName),
                "Newly created event is not visible in events page");
    }

    @Test(description = "Number of seats in new event is correct", priority = 1)
    public void numOfSeatsInNewEventIsCorrect() {

        LoginPage loginPage = new LoginPage(getPage());
        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        int numberOfSetSeats = TestDataUtils.getSeats();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(getPage().request(), payload);

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
                String.valueOf(numberOfSetSeats));

        assertThat(getPage().getByText("Event created!")).isVisible();
        eventsPage.goToEventsPage();
        Assert.assertTrue(eventsPage.isNewCreatedEventVisible(eventName),
                "Newly created event is not visible in events page");

        Locator card = eventsPage.getEventCard(eventName);
        int numberOfEventSeats = eventsPage.countEventSeats(card);
        BookingFormPage bookingFormPage = eventsPage.proceedToBookingEvent(card);
        int numberOfSeatsInBookingForm = bookingFormPage.getAvailableSeats();

        Assert.assertEquals(numberOfEventSeats, numberOfSetSeats, "Number of seats in new event is not correct");
        Assert.assertEquals(numberOfSeatsInBookingForm, numberOfSetSeats, "Number of seats in booking event is not correct");
    }

    @Test(description = "Event price is the same in app pages", priority = 1)
    public void eventPriceIsSameInTest() {
        LoginPage loginPage = new LoginPage(getPage());
        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        int eventSetPrice = TestDataUtils.getPrice();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(getPage().request(), payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        adminEventPage.goTo();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(eventSetPrice),
                String.valueOf(TestDataUtils.getSeats()));
        assertThat(getPage().getByText("Event created!")).isVisible();
        eventsPage.goToEventsPage();
        Assert.assertTrue(eventsPage.isNewCreatedEventVisible(eventName),
                "Newly created event is not visible in events page");

        Locator card = eventsPage.getEventCard(eventName);
        int eventPrice = eventsPage.getEventPrice(card);
        Assert.assertEquals(eventPrice, eventSetPrice, "Event price is not correct in events page");
        BookingFormPage bookingFormPage = eventsPage.proceedToBookingEvent(card);

        int pricePerTicket = bookingFormPage.getPricePerTicket();
        int bookTicketPrice = bookingFormPage.getPBookTicketsPrice();
        int totalPrice = bookingFormPage.getTotalPrice();

        Assert.assertEquals(pricePerTicket, eventSetPrice, "Price per ticket is not correct");
        Assert.assertEquals(bookTicketPrice, eventSetPrice, "Book ticket price is not correct");
        Assert.assertEquals(totalPrice, eventSetPrice, "Total price is not correct");
    }

    @Test(description = "Add event from API and check that it is visible in events page", priority = 1)
    public void addEventFromAPIAndCheckInEventsPage() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(getPage().request(), payload);
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

        Map<String, Object> event = EventApiService.createEventFromAPI(getPage().request(), token, eventPayload);

        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() ->
                        Assert.assertEquals(String.valueOf(event.get("status")), "201")
                );
    }

    @Test(description = "Get all events by API", priority = 1)
    public void getAllEventsByAPI() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());
        DashboardPage dashboardPage = new DashboardPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(getPage().request(), payload);
        String token = driverDetails.get("bearerToken").toString();

        Map<String, Object> allEvents = EventApiService.getAllEvents(getPage().request(), token);
        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() ->
                        Assert.assertEquals(allEvents.get("status"), 200)
                );
        int allEventsFromAPICount = (int) allEvents.get("totalEvents");
        Assert.assertTrue(loginPage.loginToApplication(email, password), "Login failed");
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        int eventsNumberFromUI = eventsPage.countEvents();

        Assert.assertEquals(allEventsFromAPICount, eventsNumberFromUI);
    }

    @Test(description = "Delete event by API", priority = 1)
    public void deleteEventsByAPI() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        LoginPage loginPage = new LoginPage(getPage());
        AdminEventPage adminEventPage = new AdminEventPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(getPage().request(), payload);
        String token = driverDetails.get("bearerToken").toString();

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

        Map<String, Object> allEvents = EventApiService.getAllEvents(getPage().request(), token);
        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() ->
                        Assert.assertEquals(allEvents.get("status"), 200)
                );

        int allEventsFromAPICount = (int) allEvents.get("totalEvents");
        int eventsNumberFromUI = eventsPage.countEvents();
        List<Integer> ids = (List<Integer>) allEvents.get("idList");

        Map<String, Object> deleteEvent = EventApiService.deleteEvent(getPage().request(), token, ids.getLast());
        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() ->
                        Assert.assertEquals(deleteEvent.get("success"), true)
                );
        Map<String, Object> allEventsAfterDelete = EventApiService.getAllEvents(getPage().request(), token);
        int afterDeleteAPI = (int) allEventsAfterDelete.get("totalEvents");
        getPage().reload();
        int eventsAfterDeleteUI = eventsPage.countEvents();

        Assert.assertEquals(allEventsFromAPICount, afterDeleteAPI + 1);
        Assert.assertEquals(eventsNumberFromUI, eventsAfterDeleteUI + 1);
    }

    @Test(description = "Mock 9 events so we can see the 9 event sandbox limit text")
    public void mockEventsToSeeSandboxMessage() {

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());

        getPage().route("**/api/events**", route -> route.fulfill(
                new Route.FulfillOptions().setPath(Paths.get("src/test/resources/events_9.json"))
        ));
        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(getPage().request(), payload);

        Assert.assertEquals(driverDetails.get("status"), "201");
        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        dashboardPage.clickOnEventsTopLink();

        assertThat(getPage().getByText(Pattern.compile("sandbox", Pattern.CASE_INSENSITIVE)).first())
                .containsText("Your sandbox holds up to");
    }

    @Test(description = "Mock 4 events so we can see the 4 event in UI")
    public void countMockEvents() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());

        getPage().route("**/api/events**", route -> route.fulfill(
                new Route.FulfillOptions().setPath(Paths.get("src/test/resources/events_4.json"))
        ));
        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(getPage().request(), payload);

        Assert.assertEquals(driverDetails.get("status"), "201");
        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        dashboardPage.clickOnEventsTopLink();
        Locator eventCards = getPage().getByTestId("event-card");

        assertThat(eventCards.first()).isVisible();
        Assert.assertEquals(eventCards.count(), 4);
        assertThat(getPage().locator("mx-1")).isHidden();
    }
}