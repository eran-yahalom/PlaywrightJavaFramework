package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Pattern;

public class DashboardPage {
    Page page;

    public DashboardPage(Page page) {
        this.page = page;
    }

    public Locator getDiscoverTextLocator() {
        return page.getByText(Pattern.compile("Discover", Pattern.CASE_INSENSITIVE));
    }

    public boolean isUserLoggedIn() {
        try {
            Locator userElement = page.getByText("Discover & Book");
            // הוספת המתנה מפורשת של עד 5 שניות להופעת האלמנט
            userElement.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            return userElement.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public UpcomingEventsPage clickOnEventsTopLink() {
        try {
            page.locator("a[href='/events']").first().click();
            return new UpcomingEventsPage(page);
        } catch (Exception e) {
            throw new RuntimeException("Failed to click on Events top link: " + e.getMessage(), e);
        }
    }
}
