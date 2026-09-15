package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class HeaderComponent {
    Page page;

    public HeaderComponent(Page page) {
        this.page = page;
    }

    public void clickAdminMenu() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Admin")).click();
    }

    public AdminEventPage clickManageEvents() {// פותח את התפריט במידה ואינו פתוח
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Events")).click();
        return new AdminEventPage(page);
    }

    public void clickManageBookings() {
        clickAdminMenu();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Manage Bookings")).click();
    }

    public void goToHome() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Home")).click();
    }

    public void goToEvents() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Events")).click();
    }

    public MyBookingsPage goToMyBookings() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("My Bookings")).click();
        return new MyBookingsPage(page);
    }
}