package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class MyBookingsPage {
    Page page;
    String URL;
    private static final String CLEAR_ALL_BOoKING_TEXT = "Clear all bookings";
    private static final String BOOKING_REF = ".booking-ref";
    private static final String CONFIRMED = "confirmed";
    private static final String VIEW_DETAILS_BUTTON = "View Details";
    private static final String CANCEL_BOOKING_BUTTON = "Cancel Booking";
    private static final String BOOKING_ID_TEST_ID = "booking-id";
    private static final String HYDROBAND = "#booking-card > div > div > div:nth-of-type(2)";
    private final static String TOUR_DATE = "#booking-card > div > div > div:nth-of-type(2) span:nth-child(1)";


    public MyBookingsPage(Page page) {
        this.page = page;
//        this.URL = base_url;
    }

    public int getNumberBookingOfCards() {
        try {
            assertThat(page.getByText("View and manage all your ticket bookings")).isVisible();
            Locator bookingCards = page.getByTestId("booking-card");

            bookingCards.first().waitFor();

            return bookingCards.count();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean clearAllBooking() {
        try {
            page.getByText(CANCEL_BOOKING_BUTTON).click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes, cancel it")).click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean clickOnCancelBookingButton() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(CANCEL_BOOKING_BUTTON)).click();
            page.getByTestId("confirm-dialog-yes").click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

//    public Map<String, String> getAllBookingCardDetails(String bookingCardTitle) {
//        Map<String, String> bookingMap = new HashMap<>();
//        Locator bookingCards = page.getByTestId("booking-card");
//        Locator loc = bookingCards.filter(new Locator.FilterOptions().setHasText(bookingCardTitle)).first();
//
//        bookingMap.put("testID", loc.getByTestId(BOOKING_ID_TEST_ID).innerText());
//        bookingMap.put("bookingRef", loc.locator(BOOKING_REF).innerText());
//        bookingMap.put("confirmedTag", loc.getByText(CONFIRMED).innerText());
//        bookingMap.put("ticketsNumber", loc.getByText(Pattern.compile("ticket", Pattern.CASE_INSENSITIVE)).first().innerText());
//        bookingMap.put("totalPrice", loc.getByText("$").textContent().replace("$", "").trim());
//        bookingMap.put("bookedDate", loc.getByText(Pattern.compile("Booked", Pattern.CASE_INSENSITIVE)).first().innerText());
//
//        return bookingMap;
//    }

    public List<Map<String, String>> getAllBookingCardDetails(String bookingCardTitle) {
        List<Map<String, String>> allBookingsList = new ArrayList<>();

        // סינון כל הכרטיסים שמתאימים לשם האירוע
        Locator matchingCards = page.getByTestId("booking-card")
                .filter(new Locator.FilterOptions().setHasText(bookingCardTitle));

        // ההמתנה המפורשת לכרטיס הראשון מבטיחה שה-DOM נטען לפני שליפת הרשימה
        matchingCards.first().waitFor();

        // מעבר בלולאה על כל האלמנטים הנמצאים
        for (Locator loc : matchingCards.all()) {
            Map<String, String> bookingMap = new HashMap<>();

            bookingMap.put("testID", loc.getByTestId(BOOKING_ID_TEST_ID).innerText());
            bookingMap.put("bookingRef", loc.locator(BOOKING_REF).innerText());
            bookingMap.put("confirmedTag", loc.getByText(CONFIRMED).innerText());
            bookingMap.put("date", loc.locator("div.gap-x-4 > span").first().innerText());
            bookingMap.put("city", loc.locator("div.gap-x-4 > span:nth-child(3)")
                    .first()
                    .innerText()
                    .replaceFirst("^\\S+\\s*", ""));
            bookingMap.put("ticketsNumber", loc.getByText(Pattern.compile("ticket", Pattern.CASE_INSENSITIVE)).first().innerText());
            bookingMap.put("totalPrice", loc.getByText("$").textContent().replace("$", "").trim());
            bookingMap.put("bookedDate", loc.getByText(Pattern.compile("Booked", Pattern.CASE_INSENSITIVE)).first().innerText());

            allBookingsList.add(bookingMap);
        }

        return allBookingsList;
    }

    public ViewBookingDetailsPage clickOnViewDetailsButton() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(VIEW_DETAILS_BUTTON)).click();
        return new ViewBookingDetailsPage(page);
    }
}
