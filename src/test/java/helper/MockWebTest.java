package helper;

import com.jayway.jsonpath.JsonPath;
import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;
import net.datafaker.Faker;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class MockWebTest {
    // mock class can help you simulate
    // example: a UI toast that appears when we see 9 events in page
    // instead of adding 9 events we mock the json reponse so it will think
    // that 9 events are visible- sp ill see the toast in UI
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
        PlaywrightAssertions.setDefaultAssertionTimeout(2000); // set default assertion timeout to 2 seconds for all assertions in this test class
    }

    @Test(description = "Mock 9 events so we can see the 9 event sandbox limit text")
    public void mock() {
        // the route will load to ui the events from json- in this case 3 events
        page.route("**/api/events**", route -> route.fulfill(
                new Route.FulfillOptions().setPath(Paths.get("src/test/resources/events_9.json"))
        ));
        page.getByLabel("Email").fill("abc@yopmail.com");
        page.getByLabel("Password").fill("Aa123456!");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();
        page.waitForTimeout(5000);

        page.navigate("https://eventhub.rahulshettyacademy.com/events");

        assertThat(page.getByText(Pattern.compile("sandbox", Pattern.CASE_INSENSITIVE)).first())
                .containsText("Your sandbox holds up to");
        page.waitForTimeout(5000);
    }

    @Test(description = "Mock 4 events so we can see the 4 event in UI")
    public void countMock() {
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
        Assert.assertEquals(eventCards.count(), 3);
        assertThat(page.locator("mx-1")).isHidden(); // 9 elements banner is not visible

    }

    @Test(description = "Number of mocked elements in UI equal to number in JSON file")
    public void countNumberOfMockElements() {
        page.navigate("https://eventhub.rahulshettyacademy.com/login");
        page.getByLabel("Email").fill("abc@yopmail.com");
        page.getByLabel("Password").fill("Aa123456!");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();
        page.waitForTimeout(5000);
        // Navigate to My Bookings
        page.navigate("https://eventhub.rahulshettyacademy.com/bookings"); // navigate will check that page loaded not if page elements are loaded
        page.waitForTimeout(10000);
        // 5. Assertion (Playwright automatically retries until timeout if loading takes time)
        assertThat(page.getByText("New postman event-1").first()).isVisible();
        Locator eventCards = page.getByTestId("event-card");
        Assert.assertEquals(eventCards.count(), 6);
        page.waitForTimeout(5000);
    }

    @Test(description = "Mock creating bookings with Next.js interception support")
    public void mockCreateBooking() throws IOException {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();

        // 1. Context Setup & API Registration
        BrowserContext context = page.context();
        APIRequestContext apiContext = context.request();

        // Register User
        Map<String, Object> registerPayload = Map.of(
                "email", email,
                "password", password
        );
        APIResponse registerResponse = apiContext.post(
                "https://api.eventhub.rahulshettyacademy.com/api/auth/register",
                RequestOptions.create().setData(registerPayload)
        );
        Assert.assertTrue(registerResponse.ok(), "Registration API failed");

        String bearerToken = JsonPath.read(registerResponse.text(), "$.token");
        int userId = JsonPath.read(registerResponse.text(), "$.user.id");

        // Create Event via API
        Map<String, Object> createEventPayload = Map.of(
                "title", "New postman event-1",
                "description", "postman API event",
                "category", "Sports",
                "venue", "23223",
                "city", "ssdsd",
                "eventDate", "2029-08-21T13:13:00.000Z",
                "price", 100,
                "totalSeats", 300
        );

        APIResponse createEventResponse = apiContext.post(
                "https://api.eventhub.rahulshettyacademy.com/api/events",
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + bearerToken)
                        .setData(createEventPayload)
        );
        Assert.assertTrue(createEventResponse.ok(), "Create Event API failed");
        int eventId = JsonPath.read(createEventResponse.text(), "$.data.id");

        // 2. Prepare Dynamic Mock Body
        String jsonContent = Files.readString(Paths.get("src/test/resources/booking_2.json"));
        String updatedJson = jsonContent
                .replace("{{EVENT_ID}}", String.valueOf(eventId))
                .replace("{{USER_ID}}", String.valueOf(userId));

        System.out.println("Updated JSON sent to Mock: " + updatedJson);

        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS",
                "Access-Control-Allow-Headers", "*"
        );

        // 3. Set Route Interception (Filter Documents vs Fetch/XHR)
        context.route("**/*bookings*", route -> {
            String resourceType = route.request().resourceType();
            String url = route.request().url();

            // Check if it's an API call or Next.js Fetch request
            if ("fetch".equals(resourceType) || "xhr".equals(resourceType) || url.contains("/api/")) {

                // Handle CORS Preflight
                if ("OPTIONS".equalsIgnoreCase(route.request().method())) {
                    route.fulfill(new Route.FulfillOptions()
                            .setStatus(200)
                            .setHeaders(headers));
                    return;
                }

                // Return Mock Response
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setHeaders(headers)
                        .setBody(updatedJson));
            } else {
                // Allow HTML Document Navigation
                route.fallback();
            }
        });
        page.waitForTimeout(5000);
        // 4. UI Login & Navigation
    }
}
// 1. הגדר את ה-Mock ראשון!

