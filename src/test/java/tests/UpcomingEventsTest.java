package tests;

import api.EventApiService;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AdminEventPage;
import pages.DashboardPage;
import pages.EventsPage;
import pages.UpcomingEventsPage;
import utils.TestDataBuilder;
import utils.TestDataUtils;

import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class UpcomingEventsTest extends BaseTest {

    private DashboardPage dashboardPage;
    private UpcomingEventsPage upcomingEventsPage;

    @BeforeMethod
    public void setupNewDriverAndFastLogin() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        // 1. הרשמת משתמש/דרייבר חדש ב-API
        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);
        Assert.assertNotNull(driverDetails, "driverDetails is null");

        String token = (String) driverDetails.get("bearerToken");

        if (token != null) {
            // 2. הזרקת ה-Token ישירות כמחרוזת Java לפני טעינת הדף
            getPage().context().addInitScript("window.localStorage.setItem('eventhub_token', '" + token + "');");

            // 3. ניווט ל-URL – הדף נטען כשה-Token כבר קיים ב-localStorage
            getPage().navigate("https://eventhub.rahulshettyacademy.com/");
        }

        // 4. לחיצה על Manage Events
        dashboardPage = new DashboardPage(getPage());
        upcomingEventsPage = new UpcomingEventsPage(getPage());

        // 5. אימות שהדף נטען
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
    }


    @Test(description = "Change filters and see that clear filters button is visible")
    public void clearFilterTest() {
        assertThat(getPage().getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Browse Events →"))).isVisible();
        getPage().getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Browse Events →")).click();
        upcomingEventsPage.selectCity("Bangalore");
        upcomingEventsPage.selectCategory("Concert");
        Assert.assertTrue(upcomingEventsPage.isClearFilterDisplayed());
    }

    @Test(description = "Clear filters button and see that button is not visible")
    public void clickOnClearFilterTest() {
        assertThat(getPage().getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Browse Events →"))).isVisible();
        getPage().getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Browse Events →")).click();
        upcomingEventsPage.selectCity("Bangalore");
        upcomingEventsPage.selectCategory("Concert");

        Assert.assertTrue(upcomingEventsPage.clickOnClearFilterButton());
        Assert.assertFalse(upcomingEventsPage.isClearFilterDisplayed());
        Assert.assertTrue(upcomingEventsPage.isCitySelectorEntryCorrect("All Cities"));
        Assert.assertTrue(upcomingEventsPage.isCategorySelectorEntryCorrect("All Categories"));
    }

    @Test(description = "Check that invalid search well show no results")
    public void noResultsFilter() {
        UpcomingEventsPage upcomingEventsPage = dashboardPage.clickOnEventsTopLink();
        upcomingEventsPage.fillEventsSearchField("No results");

        assertThat(getPage().getByText("No events found")).isVisible();
        Assert.assertEquals(upcomingEventsPage.countEvents(), 0);
    }

    @Test(description = "filter cards by category")
    public void filterByCategory() {
        EventsPage eventsPage = new EventsPage(getPage());
        UpcomingEventsPage upcomingEventsPage = dashboardPage.clickOnEventsTopLink();
        int countCardsContainingSearchTextBeforeSearch = eventsPage.countCardsThatContainsText("Concert").count();
        upcomingEventsPage.selectCategory("Concert");
        int countCardsContainingSearchTextAfterSearch = eventsPage.countCardsThatContainsText("Concert").count();

        Assert.assertEquals(countCardsContainingSearchTextBeforeSearch, countCardsContainingSearchTextAfterSearch);
        assertThat(upcomingEventsPage.getEventCardsLocator()).hasCount(countCardsContainingSearchTextAfterSearch);
        //get cards name texts: cards.nth(0).locator(".p-4>a").innerText()
    }

    @Test(description = "filter events by event name")
    public void filterByEventName() {
        EventsPage eventsPage = new EventsPage(getPage());
        UpcomingEventsPage upcomingEventsPage = dashboardPage.clickOnEventsTopLink();
        int cardsBeforeFilter = eventsPage.countCardsThatContainsText("Dilli Diwali Mela").count();
        upcomingEventsPage.fillEventsSearchField("Dilli Diwali Mela");

        assertThat(upcomingEventsPage.getEventCardsLocator()).hasCount(cardsBeforeFilter);
    }

    @Test(description = "Filter events by city, add event,filter again and see events inc")
    public void filterByCity() {
        String eventName = TestDataUtils.getRandomEventTitle();
        String city = "Delhi";
        EventsPage eventsPage = new EventsPage(getPage());
        UpcomingEventsPage upcomingEventsPage = dashboardPage.clickOnEventsTopLink();
        upcomingEventsPage.selectCity("Delhi");
        int cardsAfterFirstFilter = eventsPage.countCardsThatContainsText("Delhi").count();
        AdminEventPage adminEventPage = eventsPage.clickOnCreateNewEventButton();
        adminEventPage.createNewEvent(eventName,
                TestDataUtils.getRandomEventTitle(),
                TestDataUtils.getCategory(),
                city,
                TestDataUtils.getVenue(),
                TestDataUtils.getFutureIsoDate(),
                String.valueOf(TestDataUtils.getPrice()),
                String.valueOf(TestDataUtils.getSeats()));

        assertThat(getPage().getByText("Event created!")).isVisible();
        upcomingEventsPage = dashboardPage.clickOnEventsTopLink();
        upcomingEventsPage.selectCity("Delhi");

        assertThat(upcomingEventsPage.getEventCardsLocator()).hasCount(cardsAfterFirstFilter + 1);
    }
}
