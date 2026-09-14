package tests;

import api.EventApiService;
import com.microsoft.playwright.APIResponse;
import net.datafaker.Faker;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.DashboardPage;
import pages.LoginPage;
import pages.RegistrationPage;
import utils.ApiUtils;
import utils.TestDataBuilder;
import utils.TestDataUtils;

import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


public class RegistrationTest extends BaseTest {

    @Test(description = "Register via API and login with the same credentials", priority = 1)
    public void registerViaAPI() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        DashboardPage dashboardPage=new DashboardPage(getPage());
        Map<String, Object> payload = TestDataBuilder.getLoginPayload(email, password);
        Map<String, Object> driverDetails = EventApiService.registerNewDriverAPI(payload);

        Assert.assertEquals(driverDetails.get("status"), "201");
        Assert.assertTrue(loginPage.loginToApplication(email, password), "Login failed");
//        assertThat(getPage().getByText("Discover & Book")).isVisible();
        assertThat(dashboardPage.getDiscoverTextLocator()).isVisible();
    }

    @Test(description = "Register via API with failed user", priority = 1)
    public void failRegisterViaAPI() {
        String password = TestDataUtils.getPassword();
        Map<String, Object> payload = TestDataBuilder.getLoginPayload("abc@yopmail.com", password);
        Map<String, Object> driverDetails = EventApiService.registerInvalidDriverAPI(payload);
        Assert.assertEquals(driverDetails.get("status"), "400");
    }

    @Test(description = "Register with incorrect mail")
    public void registerWithIncorrectMail() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        RegistrationPage registrationPage = new RegistrationPage(getPage());

        LoginPage loginPage = new LoginPage(getPage());
        loginPage.goToRegistrationPage();
        assertThat(getPage().getByText("Create your account")).isVisible();
        registrationPage.enterRegistrationInfo("accyopmail.com", "Aa1234546", "Aa123456!");
        assertThat((getPage().getByText("Enter a valid email"))).isVisible();
    }

    @Test(description = "Register with existing user")
    public void registerWithExistingUser() {
        String password = TestDataUtils.getPassword();

        RegistrationPage registrationPage = new RegistrationPage(getPage());
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.goToRegistrationPage();
        assertThat(getPage().getByText("Create your account")).isVisible();
        registrationPage.enterRegistrationInfo("abc@yopmail.com", password, password);
        assertThat((getPage().getByText("Email already registered"))).isVisible();
    }

    @Test(description = "Register with invalid password")
    public void registerWithInvalidPassword() {
        String password = "aaa";
        String email=TestDataUtils.getEmail();

        LoginPage loginPage = new LoginPage(getPage());
        RegistrationPage registrationPage = new RegistrationPage(getPage());
        loginPage.goToRegistrationPage();
        assertThat(getPage().getByText("Create your account")).isVisible();
        registrationPage.enterRegistrationInfo(email, password, password);
        assertThat((getPage().getByText("Password does not meet the requirements below"))).isVisible();
    }

    @Test(description = "Register with no password")
    public void registerWithNoPassword() {
        String password = TestDataUtils.getPassword();

        LoginPage loginPage = new LoginPage(getPage());
        RegistrationPage registrationPage = new RegistrationPage(getPage());

        loginPage.goToRegistrationPage();
        assertThat(getPage().getByText("Create your account")).isVisible();
        registrationPage.enterRegistrationInfo("abc@yopmail.com", "", "");
        assertThat((getPage().getByText("Password does not meet the requirements below"))).isVisible();
    }

    @Test(description = "Register with not matching password")
    public void registerWithNotMatchingPassword() {
        String email = TestDataUtils.getEmail();
        String password = TestDataUtils.getPassword();
        LoginPage loginPage = new LoginPage(getPage());
        RegistrationPage registrationPage = new RegistrationPage(getPage());

        loginPage.goToRegistrationPage();
        assertThat(getPage().getByText("Create your account")).isVisible();
        registrationPage.enterRegistrationInfo("abc@yopmail.com", password, password + "1");
        assertThat((getPage().getByText("Passwords do not match"))).isVisible();
    }

    @Test(description = "successfully UI registration")
    public void successfulRegistrationTest() {
        String email = TestDataUtils.getEmail();

        LoginPage loginPage = new LoginPage(getPage());
        RegistrationPage registrationPage = new RegistrationPage(getPage());

        loginPage.goToRegistrationPage();
        assertThat(getPage().getByText("Create your account")).isVisible();
        DashboardPage dashboardPage = registrationPage.enterRegistrationInfo(email, "Aa123456!", "Aa123456!");
        assertThat(getPage().getByText("Logout")).isVisible();
        assertThat(getPage().getByText(email)).isVisible();
    }
}


