package tests;

import api.EventApiService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.AdminEventPage;
import pages.DashboardPage;
import pages.HeaderComponent;
import pages.LoginPage;
import utils.TestDataBuilder;
import utils.TestDataUtils;

import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AllEventsTest extends BaseTest {

    @Test(description = "Count all starter events rows")
    public void countStaterEventRows() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        HeaderComponent headerComponent = new HeaderComponent(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);

        Assert.assertNotNull(driverDetails, "driverDetails is null");
        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        AdminEventPage adminEventPage = headerComponent.clickManageEvents();
        int eventRows = adminEventPage.countEventRows();
        Assert.assertEquals(eventRows, 3, "event rows don't match");
    }

    @Test(description = "Cant delete starter events rows")
    public void cantDeleteStaterEventRows() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        HeaderComponent headerComponent = new HeaderComponent(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);

        Assert.assertNotNull(driverDetails, "driverDetails is null");

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        AdminEventPage adminEventPage = headerComponent.clickManageEvents();
        int eventRowsDeleteButtonCount = adminEventPage.countEventsDeleteButtons();
        Assert.assertEquals(eventRowsDeleteButtonCount, 0, "count after delete is not correct");
    }

    @Test(description = "Add new event and see it in all events")
    public void seeNewEventInAllEventsRows() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        LoginPage loginPage = new LoginPage(getPage());
        HeaderComponent headerComponent = new HeaderComponent(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);
        Assert.assertNotNull(driverDetails, "driverDetails is null");

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);

        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        AdminEventPage adminEventPage = headerComponent.clickManageEvents();
        int eventRowsBeforeAdding = adminEventPage.countEventRows();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));

        assertThat(getPage().getByText("Event created!")).isVisible();
        assertThat(getPage().getByText("All Events")).isVisible();

        adminEventPage.waitForEventToAppear(eventName);
        int eventRowsAfterAdding = adminEventPage.countEventRows();
        Assert.assertEquals(eventRowsBeforeAdding + 1, eventRowsAfterAdding, "event rows are not equal");
        String rawText = getPage()
                .getByText(Pattern.compile("total", Pattern.CASE_INSENSITIVE))
                .last()
                .innerText();

        String numbersOnly = rawText.replaceAll("\\D+", "");

        int totalEvents = numbersOnly.isEmpty() ? 0 : Integer.parseInt(numbersOnly);
        Assert.assertEquals(totalEvents, eventRowsAfterAdding, "total events is not equl");
    }

    @Test(description = "Delete new new event and see it removed all events")
    public void deleteNewEventFromEventsRow() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        LoginPage loginPage = new LoginPage(getPage());
        HeaderComponent headerComponent = new HeaderComponent(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);
        Assert.assertNotNull(driverDetails, "driverDetails is null");

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        AdminEventPage adminEventPage = headerComponent.clickManageEvents();
        int eventRowsBeforeAdding = adminEventPage.countEventRows();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));

        assertThat(getPage().getByText("Event created!")).isVisible();
        assertThat(getPage().getByText("All Events")).isVisible();

        adminEventPage.waitForEventToAppear(eventName);
        int eventRowsAfterAdding = adminEventPage.countEventRows();
        adminEventPage.clickOnEventRowDeleteButton(eventName);
        getPage().getByTestId("confirm-dialog-yes").click();
        adminEventPage.waitForEventToDisappear(eventName);

        assertThat(getPage().getByText("Event deleted")).isVisible();
        Assert.assertEquals(eventRowsBeforeAdding + 1, eventRowsAfterAdding, "events count is not the same");
    }

    @Test(description = "edit new new event successfully")
    public void editNewEvent() {

        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        LoginPage loginPage = new LoginPage(getPage());
        HeaderComponent headerComponent = new HeaderComponent(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);
        Assert.assertNotNull(driverDetails, "driverDetails is null");

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        AdminEventPage adminEventPage = headerComponent.clickManageEvents();

        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));

        assertThat(getPage().getByText(Pattern.compile("Event created", Pattern.CASE_INSENSITIVE)))
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(10000));

        assertThat(getPage().getByText("All Events")).isVisible();

        Locator event = adminEventPage.getEventRow(eventName);
        event.getByText("edit").click();
        adminEventPage.waitForEventToAppear(eventName);
        adminEventPage.editEvent(eventName,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(getPage().getByText("Event updated!")).isVisible();
        adminEventPage.waitForEventToAppear(eventName);
        Locator card = adminEventPage.getEventRow(eventName);

        Assert.assertEquals(card.locator("td>span").first().innerText(), eventName, "event name is not the same");
    }

    @Test(description = "Event data remains the same after close edit without editing")
    public void closeWithoutEditingTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();

        LoginPage loginPage = new LoginPage(getPage());
        HeaderComponent headerComponent = new HeaderComponent(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);
        Assert.assertNotNull(driverDetails);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        AdminEventPage adminEventPage = headerComponent.clickManageEvents();

        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                TestDataUtils.getCity(),
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));

        assertThat(getPage().getByText(Pattern.compile("Event created", Pattern.CASE_INSENSITIVE)))
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(10000));
        assertThat(getPage().getByText("All Events")).isVisible();

        Locator event = adminEventPage.getEventRow(eventName);
        event.getByText("edit").click();

        assertThat(getPage().locator("td:nth-child(3)").first()).not().hasText("");

        Map<String, Object> uneditedData = adminEventPage.getEventData(eventName);
        getPage().getByText("Cancel edit").click();

        Locator card = adminEventPage.getEventRow(eventName);

        Assert.assertEquals(card.locator("td>span").first().innerText(), eventName, "Event names don't match");
        Assert.assertEquals(card.locator("td>span").nth(1).innerText(), uneditedData.get("category"), "Category doesn't match");
        Assert.assertEquals(card.locator("td:nth-child(3)").first().innerText(), uneditedData.get("city"), "City doesn't match");
    }
}
