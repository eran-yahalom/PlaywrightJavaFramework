package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ViewBookingDetailsPage {

    Page page;
    private static final String CANCEL_BOOKING_BUTTON = "Cancel Booking";
    private static final String BACK_TO_MY_BOOKING_BUTTON = "← Back to My Bookings";
    private static final String CANCEL_BOOKING_BPOPUP_TEXT = "Cancel this booking?";


    public ViewBookingDetailsPage(Page page) {
        this.page = page;
    }

    public MyBookingsPage clickOnCancelButton() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(CANCEL_BOOKING_BUTTON)).click();
        assertThat(page.getByText(CANCEL_BOOKING_BPOPUP_TEXT)).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(CANCEL_BOOKING_BUTTON)).click();
        page.getByTestId("confirm-dialog-yes").click();
        assertThat(page.getByText("Booking cancelled successfully")).isVisible();

        return new MyBookingsPage(page);
    }

    public MyBookingsPage clickOnBackToMyBookingButton() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(BACK_TO_MY_BOOKING_BUTTON)).click();

        return new MyBookingsPage(page);
    }

    public Map<String, Object> getMyBookingEventDetails() {
        Map<String, Object> myBookingData = new HashMap<>();
        myBookingData.put("Event", page.locator("//span[text()='Event']/following-sibling::span").innerText());
        myBookingData.put("Category", page.locator("//span[text()='Category']/following-sibling::span").innerText());
        myBookingData.put("Date", page.locator("//span[text()='Date']/following-sibling::span").innerText());
        myBookingData.put("Venue", page.locator("//span[text()='Venue']/following-sibling::span").innerText());
        myBookingData.put("City", page.locator("//span[text()='City']/following-sibling::span").innerText());

        return myBookingData;
    }

    public Map<String, Object> getMyBookingHeaderValues() {
        Map<String, Object> myBookingHeaderValues = new HashMap<>();
        myBookingHeaderValues.put("bookingRef", page.locator("span.font-mono.text-indigo-600").innerText());
        myBookingHeaderValues.put("eventStatus", page.locator("span.inline-flex.text-emerald-700").innerText());

        return myBookingHeaderValues;
    }

    public Map<String, Object> getCustomerEventDetails() {
        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("Name", page.locator("//span[text()='Name']/following-sibling::span").innerText());
        customerDetails.put("Email", page.locator("//span[text()='Email']/following-sibling::span").innerText());
        customerDetails.put("Phone", page.locator("//span[text()='Phone']/following-sibling::span").innerText());

        return customerDetails;
    }

    public Map<String, Object> paymentSummeryEventDetails() {
        Map<String, Object> paymentSummeryDetails = new HashMap<>();
        paymentSummeryDetails.put("Tickets", page.locator("//span[text()='Tickets']/following-sibling::span").innerText());
        paymentSummeryDetails.put("Price per ticket", page.locator("//span[text()='Price per ticket']/following-sibling::span").innerText());
        paymentSummeryDetails.put("Total Paid", page.locator("//span[text()='Total Paid']/following-sibling::span").innerText());

        return paymentSummeryDetails;
    }

    public void clickOnRefoundLink() {
        page.getByTestId("check-refund-btn").click();
    }
}
