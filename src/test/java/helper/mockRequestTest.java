package helper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import net.datafaker.Faker;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class mockRequestTest {

    Faker faker = new Faker();
    //https://playwright.dev/docs/api/class-route

    Page page;
    Browser browser;
    Playwright playwright;

    String eventTitle = "Event" + Math.random(); // generate random event title to avoid duplicate event title error

    @BeforeMethod
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(false)); // launch chromium (chrome,opera,edge) browser in head mode - run it on the chrome version that is running on my computer
        page = browser.newPage(); // create a new page
        page.navigate("https://eventhub.rahulshettyacademy.com/login");
        PlaywrightAssertions.setDefaultAssertionTimeout(5000); // set default assertion timeout to 2 seconds for all assertions in this test class
    }

    @Test(description = "Mock request")
    public void mockRequest() {
        // the route will load to ui the events from json- in this case 3 events
        page.route("**/api/events**", route -> route.fulfill(
                new Route.FulfillOptions().setPath(Paths.get("src/test/resources/events_4.json"))
        ));
        page.getByLabel("Email").fill("abc@yopmail.com");
        page.getByLabel("Password").fill("Aa123456!");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();
        page.waitForTimeout(5000);

        page.navigate("https://eventhub.rahulshettyacademy.com/events");

        Locator eventCards = page.getByTestId("event-card");
        assertThat(eventCards.first()).isVisible();  //playwrite assert we wait 5 sec (page.waitForTimeout(5000);) for first card to show

        page.getByTestId("nav-bookings").click();
        //wait for bookings to be visible
        Locator myBookingsHeading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("My Bookings"));
        myBookingsHeading.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertThat(myBookingsHeading).isVisible();

        page.route("**api/bookings**", route -> route.resume(
                new Route.ResumeOptions().setUrl("https://eventhub.rahulshettyacademy.com/bookings/120745")));

        page.getByText("View Details").click();


        assertThat(page.getByText("Booking not found").first()).isVisible();


    }
}
