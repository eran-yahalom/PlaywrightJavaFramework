package tests;

import api.EventApiService;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.DashboardPage;
import pages.LoginPage;
import utils.TestDataBuilder;
import utils.TestDataUtils;

import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginTest extends BaseTest {

    @Test(description = "Successful login test", priority = 1, groups = {"smoke"})
    public void successfulLoginTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        DashboardPage dashboardPage = new DashboardPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        Assert.assertTrue(loginPage.loginToApplication(email, password), "Login failed");
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
    }

    @Test(description = "Unsuccessful login test", priority = 2)
    public void unsuccessfullyLoginTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());

        loginPage.loginToApplication(email, password);
        assertThat(getPage().getByText("Invalid email or password")).isVisible();
    }

    @Test(description = "Login test without password", priority = 3)
    public void loginWithoutPasswordTest() {
        String email = TestDataUtils.getEmail();

        LoginPage loginPage = new LoginPage(getPage());

        loginPage.loginToApplication(email, "");
        assertThat(getPage().getByText("Password must be at least 6 characters")).isVisible();
    }

    @Test(description = "Login test without email", priority = 4)
    public void loginWithoutEmailTest() {
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());

        loginPage.loginToApplication("", password);
        assertThat(getPage().getByText("Enter a valid email")).isVisible();
    }

    @Test(description = "Login test without email or password", priority = 5)
    public void loginWithoutEmailOrPasswordTest() {
        LoginPage loginPage = new LoginPage(getPage());

        loginPage.loginToApplication("", "");
        assertThat(getPage().getByText("Enter a valid email")).isVisible();
        assertThat(getPage().getByText("Password must be at least 6 characters")).isVisible();
    }

    @Test(description = "Successful login to app test", priority = 0)
    public void successfulLoginToAppTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);

        DashboardPage dashboardPage = loginPage.loginToApp(email, password);
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
    }

    @Test(description = "Login from API with not valid driver", priority = 0, groups = {"smoke"})
    public void loginFromAPIInvalidDriverTest() {
        String password = TestDataUtils.getPassword();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload("abc@yopmail.com", password);
        Map<String, Object> driverDetails = EventApiService.invalidLoginFromAPI(payload);
        Assert.assertEquals(driverDetails.get("status"), 400);
        Assert.assertEquals(driverDetails.get("error"), "Invalid email or password");
    }

    @Test(description = "Login from API with valid driver", priority = 0)
    public void loginFromAPIValidDriverTest() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        EventApiService.registerNewDriverAPI(payload);
        Map<String, Object> driverDetails = EventApiService.validLoginFromAPI(payload);
        Assert.assertEquals(driverDetails.get("status"), 200);
        Assert.assertTrue((Boolean) driverDetails.get("success"));
    }
}
