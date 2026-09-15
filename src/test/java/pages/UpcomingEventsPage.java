package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class UpcomingEventsPage {

    Page page;
    private static final String EVENT_SEARCH_PLACEHOLDER = "Search events, venues…";
    private static final String ALL_CATEGORIES = "All Categories";
    private static final String ALL_CITIES = "All Cities";
    private static final String CLEAR_FILTER = "Clear filters";
    private static final String EVENT_CARDS_LIST = "event-card";

    public UpcomingEventsPage(Page page) {
        this.page = page;
    }

    public Locator waitForEventsToLoad() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return page.getByTestId(EVENT_CARDS_LIST);
    }

    public int countEvents() {
        Locator events = page.getByTestId(EVENT_CARDS_LIST);
        try {
            events.first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(5000));
        } catch (TimeoutError e) {
            return 0;
        }
        return events.count();
    }

    public Locator getEventCardsLocator() {
        return page.getByTestId(EVENT_CARDS_LIST);
    }

    public void fillEventsSearchField(String text) {
        page.getByPlaceholder(EVENT_SEARCH_PLACEHOLDER).fill(text);
    }

    public void selectCity(String text) {
        page.locator("select").nth(1).selectOption(text);
    }

    public void selectCategory(String text) {
        page.locator("select").nth(0).selectOption(text);
    }

    public boolean isClearFilterDisplayed() {
        try {
            return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(CLEAR_FILTER)).isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean clickOnClearFilterButton() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(CLEAR_FILTER)).click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCitySelectorEntryCorrect(String cityName) {
        try {
            String city = page.locator("select").nth(1).locator("option:checked").innerText().trim();
            return city.equalsIgnoreCase(cityName);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCategorySelectorEntryCorrect(String categoryName) {
        try {
            String category = page.locator("select").nth(0).locator("option:checked").innerText().trim();
            return category.equalsIgnoreCase(categoryName);
        } catch (Exception e) {
            return false;
        }
    }
}