package tests;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AdminEventPage;
import pages.HeaderComponent;
import utils.TestDataUtils;

import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AllEventsTest extends BaseTest {

    private AdminEventPage adminEventPage;

    @BeforeMethod
    public void setupNewDriverAndFastLogin() {
        // 1. קריאה למתודה המשותפת שמבצעת API Register, מחלצת Token ומזריקה לדפדפן
        performFastLogin();

        // 2. לחיצה על Manage Events ואתחול Page Objects
        HeaderComponent headerComponent = new HeaderComponent(getPage());
        adminEventPage = headerComponent.clickManageEvents();
    }

    @Test(description = "Count all starter events rows")
    public void countStaterEventRows1() {
        int eventRows = adminEventPage.countEventRows();
        Assert.assertEquals(eventRows, 3, "event rows don't match");
    }

    @Test(description = "Count all starter events rows")
    public void countStaterEventRows() {
        int eventRows = adminEventPage.countEventRows();
        Assert.assertEquals(eventRows, 3, "event rows don't match");
    }

    @Test(description = "Cant delete starter events rows")
    public void cantDeleteStaterEventRows() {
        int eventRowsDeleteButtonCount = adminEventPage.countEventsDeleteButtons();
        Assert.assertEquals(eventRowsDeleteButtonCount, 0, "count after delete is not correct");
    }

    @Test(description = "Add new event and see it in all events")
    public void seeNewEventInAllEventsRows() {
        String eventName = TestDataUtils.getRandomEventTitle();
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
        String eventName = TestDataUtils.getRandomEventTitle();
        int eventRowsBeforeAdding = adminEventPage.countEventRows();
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
        String eventName = TestDataUtils.getRandomEventTitle();
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

        assertThat(getPage().getByText(Pattern.compile("All Events", Pattern.CASE_INSENSITIVE)))
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(10000));

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
        String eventName = TestDataUtils.getRandomEventTitle();
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

        assertThat(card.locator("td>span").first()).hasText(eventName);
        assertThat(card.locator("td>span").nth(1)).hasText((String) uneditedData.get("category"));
        assertThat(card.locator("td:nth-child(3)").first()).hasText((String) uneditedData.get("city"));
    }
}