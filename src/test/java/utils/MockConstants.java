package utils;

import java.util.Map;

public class MockConstants {

    public static final Map<String, String> CORS_HEADERS = Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS",
            "Access-Control-Allow-Headers", "*"
    );
}