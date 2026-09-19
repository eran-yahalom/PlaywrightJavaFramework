package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AdminEventPage {
    Page page;
    private static final String EVENT_TITLE_INPUT_SELECTOR = "#event-title-input";
    private static final String DESCRIPTION = "Describe the event…";
    private final static String CATEGORY = "Category";
    private final static String CITY = "city";
    private final static String VENUE = "venue";
    private static final String EVENT_DATE_AND_TIME = "Event Date & Time";
    private static final String PRICE = "Price ($)";
    private static final String SEATS = "e.g. 500";

    private static final String ALL_EVENTS_ROWS_TEST_ID = "event-table-row";
    private static final String ALL_EVENTS_DELETE_BUTTON_TEST_ID = "delete-event-btn";

    public AdminEventPage(Page page) {
        this.page = page;
    }

    public void goTo() {
        page.navigate("https://eventhub.rahulshettyacademy.com/admin/events");
    }

    public void createNewEvent(String eventTitle, String description,
                               String categoryItem, String city,
                               String venue, String eventDateAndTime,
                               String price, String seats) {

        page.locator(EVENT_TITLE_INPUT_SELECTOR).fill(getOrDefault(eventTitle, "vvvv"));
        page.getByPlaceholder(DESCRIPTION).fill(getOrDefault(description, "vvvv"));
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName(CATEGORY))
                .selectOption(getOrDefault(categoryItem, "Concert"));
        page.getByLabel(CITY).fill(getOrDefault(city, "vvvv"));
        page.getByLabel(VENUE).fill(getOrDefault(venue, "vvvv"));
        page.getByLabel(EVENT_DATE_AND_TIME).fill(getOrDefault(eventDateAndTime, "2027-09-19T12:41"));
        page.getByLabel(PRICE).fill(getOrDefault(price, "10.00"));
        page.getByPlaceholder(SEATS).fill(getOrDefault(seats, "50"));

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("+ Add Event")).click();
    }

    private String getOrDefault(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    public Locator waitForAllEventRowsToLoad() {
        Locator rows = page.getByTestId(ALL_EVENTS_ROWS_TEST_ID);
        rows.first().waitFor(); // ממתין שהשורה הראשונה תופיע ב-DOM ותהיה גלויה
        return rows;
    }

    public void editEvent(String newTitle, String newDescription,
                          String newCategory, String newCity, String newVenue,
                          String newDate, String newPrice, String newSeats) {


        if (newTitle != null) page.locator(EVENT_TITLE_INPUT_SELECTOR).fill(newTitle);
        if (newDescription != null) page.getByPlaceholder(DESCRIPTION).fill(newDescription);
        if (newCategory != null) {
            page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName(CATEGORY))
                    .selectOption(newCategory);
        }
        if (newCity != null) page.getByLabel(CITY).fill(newCity);
        if (newVenue != null) page.getByLabel(VENUE).fill(newVenue);
        if (newDate != null) page.getByLabel(EVENT_DATE_AND_TIME).fill(newDate);
        if (newPrice != null) page.getByLabel(PRICE).fill(newPrice);
        if (newSeats != null) page.getByPlaceholder(SEATS).fill(newSeats);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update Event")).click();
    }

    public Map<String, Object> getEventData(String eventTitle) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", page.locator(EVENT_TITLE_INPUT_SELECTOR).inputValue());
        eventData.put("description", page.getByPlaceholder(DESCRIPTION).inputValue());
        eventData.put("city", page.getByLabel(CITY).inputValue());
        eventData.put("venue", page.getByLabel(VENUE).inputValue());
        eventData.put("time", page.getByLabel(EVENT_DATE_AND_TIME).inputValue());
        eventData.put("price", page.getByLabel(PRICE).inputValue());
        eventData.put("seats", page.getByPlaceholder(SEATS).inputValue());
        eventData.put("category", page.getByRole(AriaRole.COMBOBOX).inputValue());

        return eventData;
    }

    public void waitForEventToAppear(String eventTitle) {
        page.getByTestId(ALL_EVENTS_ROWS_TEST_ID)
                .filter(new Locator.FilterOptions().setHasText(eventTitle))
                .first()
                .waitFor();
    }

    public void waitForEventToDisappear(String eventTitle) {
        assertThat(page.getByTestId(ALL_EVENTS_ROWS_TEST_ID)
                .filter(new Locator.FilterOptions().setHasText(eventTitle)))
                .isHidden();
    }

    public int countEventRows() {
        return waitForAllEventRowsToLoad().count();
    }

    public int countEventsDeleteButtons() {
        waitForAllEventRowsToLoad();
        return page.getByTestId(ALL_EVENTS_DELETE_BUTTON_TEST_ID).count();
    }

    public Locator getEventRow(String eventTitle) {
        Locator eventRows = waitForAllEventRowsToLoad();
        Locator eventRow = eventRows.filter(new Locator.FilterOptions().setHasText(eventTitle)).first();
        assertThat(eventRow).isVisible();
        return eventRow;
    }

    public Locator countEventRowsThatContainsText(String eventTitle) {
        Locator eventRows = waitForAllEventRowsToLoad();
        Locator targetRow = eventRows.filter(new Locator.FilterOptions().setHasText(eventTitle));
        assertThat(targetRow.first()).isVisible();
        return targetRow;
    }


    public void clickOnEventRowDeleteButton(String eventTitle) {
        Locator eventRows = waitForAllEventRowsToLoad();
        Locator targetRow = eventRows.filter(new Locator.FilterOptions().setHasText(eventTitle));
        assertThat(targetRow.first()).isVisible();
        targetRow.getByTestId(ALL_EVENTS_DELETE_BUTTON_TEST_ID).click();
    }
}
