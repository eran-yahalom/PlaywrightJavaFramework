package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class EventsPage {
    Page page;
    private static final String EVENT_CARDS_LIST = "event-card";
    private static final String VENUE = ".absolute.top-3.left-3 span";
    private static final String VENUE_DATE = "div[class^='space-y']>div:nth-child(1) span";
    private static final String VENUE_ADDRESS = "div[class^='space-y']>div:nth-child(2) span";
    private static final String CARD_NAME = ".p-4.flex.flex-col.flex-1 a h3";


    public EventsPage(Page page) {
        this.page = page;
    }

    public void goToEventsPage() {
        page.navigate("https://eventhub.rahulshettyacademy.com/events");
    }

    public Locator waitForEventsToLoad() {
        Locator eventCards = page.getByTestId(EVENT_CARDS_LIST);  // list of event cards
        assertThat(eventCards.first()).isVisible();
        return eventCards;
    }

    public Locator getEventCard(String eventTitle) {
        Locator eventCards = waitForEventsToLoad();
        Locator targetCard = eventCards.filter(new Locator.FilterOptions().setHasText(eventTitle)).first();
        assertThat(targetCard).isVisible();
        return targetCard;
    }

    public Locator countCardsThatContainsText(String eventTitle) {
        Locator eventCards = waitForEventsToLoad();
        Locator targetCard = eventCards.filter(new Locator.FilterOptions().setHasText(eventTitle));
        assertThat(targetCard.first()).isVisible();
        return targetCard;
    }

    public Boolean isNewCreatedEventVisible(String eventTitle) {
        try {
            Locator eventCards = page.getByTestId(EVENT_CARDS_LIST);  // list of event cards
            eventCards.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            Locator loc = eventCards.filter(new Locator.FilterOptions().setHasText(eventTitle));
            return loc.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickOnEventByTitle(String eventTitle) {
        try {
            Locator eventCards = waitForEventsToLoad();
            Locator loc = eventCards.filter(new Locator.FilterOptions().setHasText(eventTitle));
            loc.getByText("Book Now").click();
        } catch (Exception e) {
            System.out.println("Error in clickOnEventByTitle: " + e.getMessage());
        }
    }

    public BookingFormPage proceedToBookingEvent(Locator card) {
        try {
            card.getByTestId("book-now-btn").click();
            return new BookingFormPage(page);
        } catch (Exception e) {
            System.out.println("Error in clickOnEventByTitle: " + e.getMessage());
        }
        return null;
    }

    public BookingFormPage proceedToBookingEvent(String eventTitle) {
        try {
            Locator eventCards = waitForEventsToLoad();
            Locator loc = eventCards.filter(new Locator.FilterOptions().setHasText(eventTitle));
            loc.getByTestId("book-now-btn").click();
            return new BookingFormPage(page);
        } catch (Exception e) {
            System.out.println("Error in clickOnEventByTitle: " + e.getMessage());
        }
        return null;
    }

    public int countEventSeats(Locator card) {
        String seatsText = card.getByText("seats").textContent().split(" ")[0];
        return Integer.parseInt(seatsText);
    }

    public int getEventPrice(Locator card) {
        String priceText = card.getByText("$").textContent().replace("$", "").trim();
        return Integer.parseInt(priceText);
    }

    public Map<String, String> getCardDetails(String cardTitle) {
        Map<String, String> cardDetails = new HashMap<>();
        Locator card = getEventCard(cardTitle);
        cardDetails.put("title", card.locator(CARD_NAME).textContent().trim());
        cardDetails.put("price", card.getByText("$").textContent().replace("$", "").trim());
        cardDetails.put("seats", card.getByText("seats").textContent().split(" ")[0]);
        cardDetails.put("category", card.locator(VENUE).textContent().trim());
        cardDetails.put("date", card.locator(VENUE_DATE).textContent().trim());
        cardDetails.put("venue", card.locator(VENUE_ADDRESS).textContent().trim());
        return cardDetails;
    }

    public int countEvents() {
        try {
            Locator eventCards = waitForEventsToLoad();
            return eventCards.count();
        } catch (Exception e) {
            return 0;
        }
    }

    public AdminEventPage clickOnCreateNewEventButton() {
        page.getByText("Add New Event").click();
        return new AdminEventPage(page);
    }
}