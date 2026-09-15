package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class BookingFormPage {
    Page page;

    private static final String BOOKING_TICKET_ADD = "+";
    private static final String BOOKING_TICKET_DEC = "-";
    private static final String BOOKING_FULL_NAME = "Full Name";
    private static final String BOOKING_EMAIL = "Email";
    private static final String BOOKING_PHONE_PLACE_HOLDER = "+91 98765 43210";
    private static final String BOOKING_CONFIRM_BUTTON = "Confirm Booking";
    private static final String BOOKING_TOTAL = "[class^='bg-indigo-50'] div:nth-child(2) span[class='text-indigo-700']";

    private static final String AVAILABLE_SEATS_LOCARTOR = "Available";
    public static final String PRICE_PER_TICKET_LABEL = "Price per ticket";
    public static final String BOOK_TICKETS = "Book Tickets";
    public static final String BOOKING_REF = ".booking-ref";
    public static final String VIEW_MY_BOOKING_BUTTON = "View My Bookings";
    public static final String BROWSE_MORE_EVENTS_BUTTON = " Browse More Events";


    public BookingFormPage(Page page) {
        this.page = page;
    }

    public String getValueByLabelName(String locatorText) {
        return page.locator("div")
                .filter(new Locator.FilterOptions().setHasText(locatorText))
                .last()
                .locator("span")
                .first()
                .innerText();
    }

    private String getValueByLabel(String labelText) {
        return page.locator("div")
                .filter(new Locator.FilterOptions().setHasText(labelText))
                .last()
                .locator("p")
                .nth(1)
                .innerText();
    }

    public String getPricePerTicketTotal() {
        return page.getByLabel(PRICE_PER_TICKET_LABEL).textContent().replace("[^0-9.]", "");
    }

    public int getAvailableSeats() {
        assertThat(page.getByText("Book Tickets")).isVisible();
        return Integer.parseInt(getValueByLabelName(AVAILABLE_SEATS_LOCARTOR).split("/")[0].trim());
    }

    public int getPricePerTicket() {
        assertThat(page.getByText("Book Tickets")).isVisible();
        return Integer.parseInt(getValueByLabel(PRICE_PER_TICKET_LABEL).replace("$", "").trim());
    }

    public int getPBookTicketsPrice() {
        assertThat(page.getByText("Book Tickets")).isVisible();
        return Integer.parseInt(getValueByLabelName(BOOK_TICKETS).replace("$", "").trim());
    }

    public int getTotalPrice() {
        assertThat(page.getByText("Book Tickets")).isVisible();
        return Integer.parseInt(page.locator(BOOKING_TOTAL).innerText().replace("$", "").trim());
    }

    public void bookAnEvent(String email, String password, String mobileNumber) {
        page.getByLabel(BOOKING_FULL_NAME).fill(email);
        page.getByLabel(BOOKING_EMAIL).fill(password);
        page.getByPlaceholder(BOOKING_PHONE_PLACE_HOLDER).fill(mobileNumber);
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BOOKING_CONFIRM_BUTTON)).click();
    }

    public Map<String, Object> getBookingConfirmedData() {
        Map<String, Object> bookingConfirmedData = new HashMap<>();
        bookingConfirmedData.put("BookingRef", page.locator(".booking-ref").innerText());
        bookingConfirmedData.put("Customer", page.locator("//span[text()='Customer']/following-sibling::span").innerText());
        bookingConfirmedData.put("Tickets", page.locator("//span[text()='Tickets']/following-sibling::span").innerText());
        bookingConfirmedData.put("TotalPrice", page.locator("//span[text()='Total']/following-sibling::span").innerText().replace("$", ""));

        return bookingConfirmedData;
    }

    public MyBookingsPage clickOnViewMyBookingButton() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName(VIEW_MY_BOOKING_BUTTON)).click();
        return new MyBookingsPage(page);
    }

    public EventsPage clickOnBrowseMoreEventsButton() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BROWSE_MORE_EVENTS_BUTTON)).click();
        return new EventsPage(page);
    }
}
