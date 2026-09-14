package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

    public static String formatDate(String rawDate) {
        // 1. Extract only the date part if a timestamp with 'T' is provided
        String dateOnly = rawDate.contains("T") ? rawDate.split("T")[0] : rawDate;

        // 2. Parse into LocalDate (works for "2026-03-11" and "2027-09-19")
        LocalDate date = LocalDate.parse(dateOnly);

        // 3. Format to "EEE, d MMM" (e.g., "Wed, 11 Mar", "Sun, 19 Sep")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH);
        String formatted = date.format(formatter);

        // 4. Ensure September formats as "Sept" to match UI requirements
        return formatted.replace("Sep", "Sept");
    }

    public static void main(String[] args) {
        System.out.println(formatDate("2026-03-11"));       // Output: Wed, 11 Mar
        System.out.println(formatDate("2027-09-19T12:41")); // Output: Sun, 19 Sept
        System.out.println(formatDate("2026-01-05"));       // Output: Mon, 5 Jan
    }
}