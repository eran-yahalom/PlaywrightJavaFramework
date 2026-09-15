package utils;

import net.datafaker.Faker;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class TestDataUtils {

    private static final ThreadLocal<Faker> faker = ThreadLocal.withInitial(Faker::new);

    public static Faker getFaker() {
        return faker.get();
    }

    public static String getEmail() {
        return getFaker().internet().emailAddress();
    }

    public static String getPassword() {
        return getFaker().internet().password(8, 16, true, true);
    }

    public static String getFirstName() {
        return getFaker().name().firstName();
    }

    public static String getLastName() {
        return getFaker().name().lastName();
    }

    public static String getFullName() {
        return getFaker().name().fullName();
    }

    public static String getPhoneNumber() {
        return getFaker().phoneNumber().cellPhone();
    }

    public static String getRandomEventTitle() {
        return getFaker().company().catchPhrase() + " " + getFaker().number().numberBetween(100, 999);
    }

    public static String getFutureIsoDatePayLoad() {
        Instant futureInstant = getFaker().timeAndDate().future(365, TimeUnit.DAYS);
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneOffset.UTC);

        return formatter.format(futureInstant);
    }

    public static String getFutureIsoDate() {
        Instant futureInstant = getFaker().timeAndDate().future(365, TimeUnit.DAYS);
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm")
                .withZone(ZoneOffset.UTC);

        return formatter.format(futureInstant);
    }

    public static int getPrice() {
        return getFaker().number().numberBetween(50, 500);
    }

    public static int getSeats() {
        return getFaker().number().numberBetween(1, 100);
    }

    public static String getCategory() {
        return getFaker().options().option("Sports", "Concert", "Workshop");
    }

    public static String getVenue() {
        return getFaker().address().cityName() + " Arena";
    }

    public static String getCity() {
        return getFaker().address().city();
    }

    public static int getQty() {
        return getFaker().number().numberBetween(1, 10);
    }
}