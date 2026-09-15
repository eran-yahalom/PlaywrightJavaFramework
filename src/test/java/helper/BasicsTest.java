package helper;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class BasicsTest extends BaseTest {
    //HeadMode - see brawser in test
    //HeadlessMode - run test in background - cant see brawser (bteeter fast, run for CI/CD
    Page page = getContext().newPage();
    String eventTitle = "";

    @Test(description = "registration test")
    public void registerNewUserTest() {
        getPage().navigate(base_url);
        //click on register link
        getPage().getByText("Register").click();

        assertThat(getPage().getByText("Create your account")).isVisible();

//enter new user details
        getPage().getByPlaceholder("you@email.com").fill("rani@yopmail.com");
        getPage().getByTestId("register-password").fill("Aa123456!");
        getPage().getByPlaceholder("Repeat your password").fill("Aa123456!");
        getPage().getByTestId("register-btn").click();

        assertThat(getPage().getByTestId("user-email-display")).isVisible(); // assert that the logged in user email is visible
    }

    @Test(description = "fail registration with existing email test")
    public void failRegistrationTest() {
        // register with existing email -Email already registered
        getPage().navigate(base_url);
        getPage().getByText("Register").click();
        assertThat(getPage().getByText("Create your account")).isVisible();

        getPage().getByPlaceholder("you@email.com").fill("rani@yopmail.com");
        getPage().getByTestId("register-password").fill("Aa123456!");
        getPage().getByPlaceholder("Repeat your password").fill("Aa123456!");
        getPage().getByTestId("register-btn").click();

        assertThat(getPage().getByText("Email already registered")).isVisible();
    }


    @Test(description = "add event test")
    public void addEventTest() {
        // login to the application
        getPage().getByLabel("Email").fill("abc@yopmail.com");
        getPage().getByLabel("Password").fill("Aa123456!");
        getPage().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();

        assertThat(getPage().getByTestId("user-email-display")).isVisible();

        //select Admin >manage events dropdown
        page.locator("a[href='/admin/events']").first().click();

        page.locator("#event-title-input").fill(eventTitle);

        //fill description input field
        page.getByPlaceholder("Describe the event…").fill("Test Event" + eventTitle);

        // get category dropdown and select category
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Category")).selectOption("Concert");

        //fill city input field
        page.getByLabel("city").fill("New York"); // fill city input field with a timeout of 1 second

        //fill venue input field
        page.getByLabel("venue").fill("Madison Square Garden", new Locator.FillOptions().setTimeout(1000)); // fill venue input field with a timeout of 1 second

        // get date input field and fill in date
        page.getByLabel("Event Date & Time").fill("2026-08-13T15:28", new Locator.FillOptions().setTimeout(1000)); // fill date input field with a timeout of 1 second

        //fill price
        page.getByLabel("Price ($)").fill("100");

        //fill seats
        page.getByPlaceholder("e.g. 500").fill("100");
        page.locator("#add-event-btn").click();
        assertThat(page.getByText("Event created!")).isVisible(); // assert that the event is created

        page.locator("a[href='/events']").first().click(); // click on events top menu button

        //
        Locator eventCards = page.locator("#event-card");
        Locator event = eventCards.filter(new Locator.FilterOptions().setHasText(eventTitle)); // click on the event card with the title "Test Event Description2"
        String eventSeats = event.getByText(Pattern.compile("seats", Pattern.CASE_INSENSITIVE)).first().innerText(); // get the number of seats from the event card:Test Event Description3
        int numOfSeatsBeforeBooking = Integer.parseInt(eventSeats.split(" ")[0]); // get the number of seats from the string "100 seats available" - get first string in the array of 3

        Assert.assertEquals(eventSeats, "100 seats available"); // assert that the number of seats is 100

        event.getByText("Book Now").click(); // click on the book now button
        //get available seats
        //Event-page
        String availableSeats = page.getByText(Pattern.compile("seats", Pattern.CASE_INSENSITIVE)).first().innerText();
        String numOfEventPageTotalSeats = availableSeats.split("")[0];
        String numOfEventPageRemainingAvailableSeats = availableSeats.split(" ")[1];

        //Event-book tickets page
        page.getByLabel("Full Name").fill("John Doe");
        page.getByLabel("Email").fill("was@yopmail.com");
        page.getByLabel("Phone Number").fill("1234567890");
        page.getByText("+").click();
        page.getByText("Confirm Booking").click();

        // get booking id and number of tickets
        String bookingRef = page.locator(".booking-ref").innerText();
        String numberOfBookedTickets = page.locator("div.flex:has-text('Tickets')")
                .locator("span.font-medium")
                .innerText();

        String ticketsTotalPrice = page.locator("div.flex:has-text('Total')")
                .locator("span.font-medium")
                .innerText().replace("$", ""); // remove the $ sign from the total price

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("View My Bookings")).click(); // click on my bookings link

        Locator bookingCards = page.locator("#booking-card");
        Locator bookingCard = bookingCards.filter(new Locator.FilterOptions().setHasText(eventTitle)); // get the booking card with the booking id
        String cardRef = bookingCard.locator(".booking-ref").innerText(); // get the booking id from the booking card
        String cardTicketsNumber = bookingCard.getByText(Pattern.compile("tickets", Pattern.CASE_INSENSITIVE)).first().innerText().replaceAll("[^0-9]", "");
        String bookingCardTotal = bookingCard.getByText(Pattern.compile("\\$", Pattern.CASE_INSENSITIVE)).first().innerText().replaceAll("[^0-9]", "");

        Assert.assertEquals(cardRef, bookingRef); // assert that the booking id is the same as the one in the booking card
        Assert.assertEquals(cardTicketsNumber, numberOfBookedTickets); // assert that the number of tickets is the same as the one in the booking card
        Assert.assertEquals(bookingCardTotal, ticketsTotalPrice); // assert that the total price is the same as the one in the booking card

        page.locator("a[href='/events']").first().click(); // click on events top menu button

        Locator eventCards1 = page.locator("#event-card");
        Locator event1 = eventCards1.filter(new Locator.FilterOptions().setHasText(eventTitle)); // click on the event card with the title "Test Event Description2"
        String eventSeats1 = event1.getByText(Pattern.compile("seats", Pattern.CASE_INSENSITIVE)).first().innerText();
        int numOfSeatsBeforeBooking1 = Integer.parseInt(eventSeats1.split(" ")[0]);
        Assert.assertEquals(numOfSeatsBeforeBooking1, numOfSeatsBeforeBooking - Integer.parseInt(numberOfBookedTickets)); // assert that the number of seats is reduced by the number of booked tickets
    }


    @Test(description = "Book and event and verify its booked")
    public void newTest() {

        String pageTitle = page.title();
        System.out.println("page title:" + pageTitle);
        assertThat(page).hasTitle("EventHub — Discover & Book Events"); // assert the page title using playwright assertion
        page.getByLabel("Email").fill("abc@yopmail.com");
        page.getByLabel("Password").fill("Aa123456!");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();

        assertThat(page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Browse Events →"))).isVisible(); // assert the page title using playwright assertion
        // navigate to new event page bu URL
        page.navigate("https://eventhub.rahulshettyacademy.com/admin/events");
        // fill in event title input field
        page.locator("#event-title-input").fill("Test Event");
        page.getByLabel("Title").fill("Test Event1");
        page.getByPlaceholder("Event title").fill("Test Event Description2");
        page.getByTestId("event-title-input").fill("Test Event Description3");

        //fill description input field
        page.getByPlaceholder("Describe the event…").fill("Test Event Description2");
        page.locator("[placeholder='Describe the event…']").fill("Test Event Description2");

        // get category dropdown and select category
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Category")).selectOption("Concert");
        page.locator("#category").selectOption("Festival");
        page.getByLabel("category").selectOption("Workshop");

        //fill city input field
        page.getByLabel("city").fill("New York"); // fill city input field with a timeout of 1 second

        //fill venue input field
        page.getByLabel("venue").fill("Madison Square Garden", new Locator.FillOptions().setTimeout(1000)); // fill venue input field with a timeout of 1 second

        // get date input field and fill in date
        page.getByLabel("Event Date & Time").fill("2026-08-13T15:28", new Locator.FillOptions().setTimeout(1000)); // fill date input field with a timeout of 1 second

        //fill price
        page.getByLabel("Price ($)").fill("100");

        //fill seats
        page.getByPlaceholder("e.g. 500").fill("100");

        // click on the create event button
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("+ Add Event")).click(new Locator.ClickOptions().setTimeout(1000));  // force click on the button even if it is not visible
        // page.locator("[type='submit']").click();

        // get toast message and assert that event is created
        assertThat(page.getByText("Event created!")).isVisible();

// click on events top menu button
        page.getByTestId("nav-events").click();

        // filrter events by title
        Locator eventCards = page.getByTestId("event-card");  // list of event cards
        eventCards.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        //iterate over the event cards and click on the one with the title "Test Event Description3"
        Locator loc = eventCards.filter(new Locator.FilterOptions().setHasText("Test Event Description3"));
        String numOfSeats = loc.getByText("seats").innerText(); // get the number of seats from the event card:Test Event Description3
        assertThat(loc.getByText("seats")).hasText("100 seats available"); // assert that the number of seats is 100

        int nunOfInt = Integer.parseInt(numOfSeats.split(" ")[0]); // get the number of seats from the string "100 seats available" - get first string in the array of 3
        Assert.assertTrue(nunOfInt == nunOfInt, "Number of seats is not 100"); // assert that the number of seats is 100


        assertThat(loc).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(5000)); // only in this assertion wait for 5 seconds for the event card to be visible
        loc.getByRole(AriaRole.LINK).first().click();

        ///
        Locator bookTicketsFormTotal = page.locator("[class^='bg-indigo-50'] div:nth-child(2)");
        String total = page.locator("[class^='bg-indigo-50'] div:nth-child(2) span[class='text-indigo-700']").innerText(); // get the total amount from the book tickets form
        total = total.trim().replace("$", ""); // remove the $ sign from the total amount


// fill book tickets form and click on the book button
        page.getByLabel("Full Name").fill("John Doe");
        page.getByLabel("Email").fill("was@yopmail.com");
        page.getByLabel("Phone Number").fill("1234567890");
        page.getByText("+").click();
        page.getByText("Confirm Booking").click();


        for (Locator eventCard : eventCards.all()) {
            String eventTitle = eventCard.locator("[class$='leading-snug']").textContent(); // get the event title from the card
            if (eventTitle.equals("Test Event Description3")) {
                eventCard.getByRole(AriaRole.LINK).first().click(); // get first link in card and click on it
                break;
            }
        }

        //My booking page get booking id
        Locator bookingCards = page.locator("#booking-card");
        Locator bookingText = bookingCards.filter(new Locator.FilterOptions().setHasText("T-M4ZF7E")); // get the booking id from the booking card

        assertThat(bookingText).isVisible(); // assert that the booking id is visible

        /// ///////////////


        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirm Booking"))).isVisible(); // assert that the add event button is visible
        // 1. לחיצה על כפתור ה-Admin לפתיחת התפריט
        // page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Admin")).click();

// 2. לחיצה על הקישור Manage Events שנפתח בתפריט
//        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Events")).click();
//        page.locator("div>a[href='/admin/events']").click();
        // or go to first item in dropdown:
        //page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Events")).first().click();


        // clickon Events top menu button
        // page.locator("#nav-events").click();
//        page.getByTestId("nav-events").click();


        //wait to see all the changes
        page.waitForTimeout(5000);


    }


    @Test
    public void DemoTest() {


//          Browser fireFoxBrowser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(false)); // launch firefox browser in head mode; // launch chromium browser headless mode
//           Browser safariBrowser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false)); // launch safari browser in head mode; // launch chromium browser headless mode


//        String pageTitle = page.title(); // get the page title
//        System.out.println("page title:" + pageTitle); // print the page title
//        //   Assert.assertEquals(pageTitle, "EventHub — Discover & Book Events", "Page title does not match"); // assert the page title
//        assertThat(page).hasTitle("EventHub — Discover & Book Events"); // assert the page title using playwright assertion
//        page.getByLabel("Email").fill("abc@yopmail.com");
//        page.getByLabel("Password").fill("123456");

        // page.getByPlaceholder("you@email.com").fill("abc@yopmail.com");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In"))
                .click();


//        browser.close(); // close the browser
//        playwright.close(); // close playwright instance
    }

    @Test
    public void DemoTest2() {

    }

    @AfterMethod
    public void TearDown() {
        // Cleanup code if needed
    }
}
