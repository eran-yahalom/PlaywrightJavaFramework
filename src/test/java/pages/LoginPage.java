package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage {

    Page page;
    private static final String REGISTER_LINK = "Register";

    public LoginPage(Page page) {
        this.page = page;
    }

    public boolean loginToApplication(String username, String password) {
        try {
            String pageTitle = page.title();
            System.out.println("page title:" + pageTitle);
            assertThat(page).hasTitle("EventHub — Discover & Book Events"); // assert the page title using playwright assertion
            page.getByLabel("Email").fill(username);
            page.getByLabel("Password").fill(password);

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();
            return true;
        } catch (Exception e) {
            System.out.println("Error in loginToApplication: " + e.getMessage());
            return false;
        }
    }

    public DashboardPage loginToApp(String username, String password) {
        String pageTitle = page.title();
        System.out.println("page title:" + pageTitle);
        assertThat(page).hasTitle("EventHub — Discover & Book Events"); // assert the page title using playwright assertion
        page.getByLabel("Email").fill(username);
        page.getByLabel("Password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();

        return new DashboardPage(page);
    }

    public RegistrationPage goToRegistrationPage() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(REGISTER_LINK)).click();
        return new RegistrationPage(page);
    }
}
