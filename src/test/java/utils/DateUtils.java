package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

    public static String formatDate(String rawDate) {
        String dateOnly = rawDate.contains("T") ? rawDate.split("T")[0] : rawDate;
        LocalDate date = LocalDate.parse(dateOnly);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH);
        String formatted = date.format(formatter);

        return formatted.replace("Sep", "Sept");
    }

    public static void main(String[] args) {
        System.out.println(formatDate("2026-03-11"));       // Output: Wed, 11 Mar
        System.out.println(formatDate("2027-09-19T12:41")); // Output: Sun, 19 Sept
        System.out.println(formatDate("2026-01-05"));       // Output: Mon, 5 Jan
    }
}