package tests;

import api.EventApiService;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;
import utils.TestDataBuilder;
import utils.TestDataUtils;

import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class UpcomingEventsTest extends BaseTest {

    @Test(description = "Change filters and see that clear filters button is visible")
    public void clearFilterTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        UpcomingEventsPage upcomingEventsPage = new UpcomingEventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
        assertThat(getPage().getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Browse Events →"))).isVisible();
        getPage().getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Browse Events →")).click();
        upcomingEventsPage.selectCity("Bangalore");
        upcomingEventsPage.selectCategory("Concert");
        Assert.assertTrue(upcomingEventsPage.isClearFilterDisplayed());
    }

    @Test(description = "Clear filters button and see that button is not visible")
    public void clickOnClearFilterTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        UpcomingEventsPage upcomingEventsPage = new UpcomingEventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp("abc@yopmail.com", "Aa123456!");
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
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
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();

        UpcomingEventsPage upcomingEventsPage = dashboardPage.clickOnEventsTopLink();
        upcomingEventsPage.fillEventsSearchField("No results");
        assertThat(getPage().getByText("No events found")).isVisible();
        Assert.assertEquals(upcomingEventsPage.countEvents(), 0);
    }

    @Test(description = "filter cards by category")
    public void filterByCategory() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();

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
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();

        UpcomingEventsPage upcomingEventsPage = dashboardPage.clickOnEventsTopLink();
        int cardsBeforeFilter = eventsPage.countCardsThatContainsText("Dilli Diwali Mela").count();
        upcomingEventsPage.fillEventsSearchField("Dilli Diwali Mela");

        assertThat(upcomingEventsPage.getEventCardsLocator()).hasCount(cardsBeforeFilter);
    }

    @Test(description = "Filter events by city, add event,filter again and see events inc")
    public void filterByCity() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        String eventName = TestDataUtils.getRandomEventTitle();
        String city = "Delhi";

        LoginPage loginPage = new LoginPage(getPage());
        EventsPage eventsPage = new EventsPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();

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
