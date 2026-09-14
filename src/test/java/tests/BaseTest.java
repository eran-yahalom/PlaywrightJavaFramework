//package tests;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.microsoft.playwright.*;
//import com.microsoft.playwright.assertions.PlaywrightAssertions;
//import io.qameta.allure.Allure;
//import org.testng.ITestResult;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeMethod;
//
//import java.io.ByteArrayInputStream;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Properties;
//
//public class BaseTest {
//
//    // 1. הגדרת ThreadLocal עבור רכיבי Playwright להרצה מקבילית בטוחה
//    private static final ThreadLocal<Playwright> playwrightTL = new ThreadLocal<>();
//    private static final ThreadLocal<Browser> browserTL = new ThreadLocal<>();
//    private static final ThreadLocal<BrowserContext> contextTL = new ThreadLocal<>();
//    private static final ThreadLocal<Page> pageTL = new ThreadLocal<>();
//
//    protected String base_url;
//    protected String env;
//    protected JsonNode envData;
//
//    // 2. מתודת Getter לגישה ל-Page מתוך מחלקות הטסטים
//    public Page getPage() {
//        return pageTL.get();
//    }
//
//    public BrowserContext getContext() {
//        return contextTL.get();
//    }
//
//    @BeforeMethod(alwaysRun = true)
//    public void setUp() throws IOException {
//        // 1. Load config.properties safely
//        Properties prop = new Properties();
//        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
//            if (input != null) {
//                prop.load(input);
//            } else {
//                try (FileInputStream fis = new FileInputStream("src/test/resources/config.properties")) {
//                    prop.load(fis);
//                }
//            }
//        }
//
//        // 2. Resolve target environment (-Denv CLI parameter > config.properties > default "qa1")
//        String envFromCli = System.getProperty("env");
//        env = (envFromCli != null && !envFromCli.trim().isEmpty())
//                ? envFromCli.trim()
//                : prop.getProperty("env", "qa1").trim();
//
//        // 3. Load dynamic properties from environments.json for the active env
//        loadEnvironmentConfig(env);
//
//        // 4. Assign base_url from environments.json (with fallback to config.properties)
//        if (envData != null && envData.has("url")) {
//            base_url = envData.get("url").asText();
//        } else {
//            base_url = prop.getProperty("qa.base.url");
//        }
//
//        // 5. Resolve browser selection (-Dbrowser CLI > config.properties > default "chrome")
//        String browserFromCli = System.getProperty("browser");
//        String browserName = (browserFromCli != null && !browserFromCli.trim().isEmpty())
//                ? browserFromCli.trim()
//                : prop.getProperty("browser", "chrome").trim();
//
//        // 6. Initialize Playwright & Browser (מופע ייחודי לכל Thread)
//        Playwright playwright = Playwright.create();
//        Browser browser;
//
//        if ("firefox".equalsIgnoreCase(browserName)) {
//            browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(false));
//        } else if ("safari".equalsIgnoreCase(browserName) || "webkit".equalsIgnoreCase(browserName)) {
//            browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false));
//        } else {
//            browser = playwright.chromium().launch(
//                    new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(false)
//            );
//        }
//
//        BrowserContext context = browser.newContext();
//        Page page = context.newPage();
//
//        // שמירת המופעים ב-ThreadLocal
//        playwrightTL.set(playwright);
//        browserTL.set(browser);
//        contextTL.set(context);
//        pageTL.set(page);
//
//        PlaywrightAssertions.setDefaultAssertionTimeout(2000);
//
//        if (base_url != null && !base_url.isEmpty()) {
//            getPage().navigate(base_url);
//        } else {
//            throw new IllegalStateException("base_url is null or empty. Check environments.json or config.properties.");
//        }
//    }
//
//    /**
//     * Reads src/test/resources/environments.json and extracts the node for the target environment.
//     */
//    private void loadEnvironmentConfig(String targetEnv) {
//        ObjectMapper mapper = new ObjectMapper();
//        try (InputStream jsonStream = getClass().getClassLoader().getResourceAsStream("environments.json")) {
//            InputStream is = (jsonStream != null)
//                    ? jsonStream
//                    : new FileInputStream("src/test/resources/environments.json");
//
//            JsonNode rootNode = mapper.readTree(is);
//
//            // Access the nested "environments" object if present, otherwise fall back to rootNode
//            JsonNode environmentsNode = rootNode.has("environments")
//                    ? rootNode.get("environments")
//                    : rootNode;
//
//            if (environmentsNode.has(targetEnv)) {
//                envData = environmentsNode.get(targetEnv);
//            } else {
//                throw new IllegalArgumentException("Environment key '" + targetEnv + "' not found in environments.json!");
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to read environments.json configuration", e);
//        }
//    }
//
//    @AfterMethod(alwaysRun = true)
//    public void tearDown(ITestResult result) {
//        try {
//            // 1. צילום מסך ל-Allure במידה והטסט נכשל (מתבצע לפני סגירת ה-Page)
//            if (result.getStatus() == ITestResult.FAILURE && pageTL.get() != null) {
//                try {
//                    byte[] screenshot = pageTL.get().screenshot(new Page.ScreenshotOptions().setFullPage(true));
//                    Allure.addAttachment("Failure Screenshot", "image/png", new ByteArrayInputStream(screenshot), "png");
//                } catch (Exception e) {
//                    System.err.println("Failed to capture screenshot for Allure: " + e.getMessage());
//                }
//            }
//
//            // 2. סגירת משאבי Playwright
//            if (pageTL.get() != null) pageTL.get().close();
//            if (contextTL.get() != null) contextTL.get().close();
//            if (browserTL.get() != null) browserTL.get().close();
//            if (playwrightTL.get() != null) playwrightTL.get().close();
//        } finally {
//            // ניקוי זיכרון חובה של ה-ThreadLocal כדי למנוע Memory Leaks
//            pageTL.remove();
//            contextTL.remove();
//            browserTL.remove();
//            playwrightTL.remove();
//        }
//    }
//}

package tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Allure;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaseTest {

    // 1. הגדרת ThreadLocal עבור רכיבי Playwright להרצה מקבילית בטוחה
    private static final ThreadLocal<Playwright> playwrightTL = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserTL = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextTL = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageTL = new ThreadLocal<>();

    protected String base_url;
    protected String env;
    protected JsonNode envData;

    // 2. מתודת Getter לגישה ל-Page מתוך מחלקות הטסטים
    public Page getPage() {
        return pageTL.get();
    }

    public BrowserContext getContext() {
        return contextTL.get();
    }

    public Browser getBrowser() {
        return browserTL.get();
    }

    public Playwright getPlaywright() {
        return playwrightTL.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws IOException {
        // 1. Load config.properties safely
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                prop.load(input);
            } else {
                try (FileInputStream fis = new FileInputStream("src/test/resources/config.properties")) {
                    prop.load(fis);
                }
            }
        }

        // 2. Resolve target environment (-Denv CLI parameter > config.properties > default "qa1")
        String envFromCli = System.getProperty("env");
        env = (envFromCli != null && !envFromCli.trim().isEmpty())
                ? envFromCli.trim()
                : prop.getProperty("env", "qa1").trim();

        // 3. Load dynamic properties from environments.json for the active env
        loadEnvironmentConfig(env);

        // 4. Assign base_url from environments.json (with fallback to config.properties)
        if (envData != null && envData.has("url")) {
            base_url = envData.get("url").asText();
        } else {
            base_url = prop.getProperty("qa.base.url");
        }

        // 5. Resolve browser selection (-Dbrowser CLI > config.properties > default "chrome")
        String browserFromCli = System.getProperty("browser");
        String browserName = (browserFromCli != null && !browserFromCli.trim().isEmpty())
                ? browserFromCli.trim()
                : prop.getProperty("browser", "chrome").trim();

        // 6. Resolve headless mode (-Dheadless CLI > config.properties > default "true" for CI)
        String headlessFromCli = System.getProperty("headless");
        boolean isHeadless = (headlessFromCli != null && !headlessFromCli.trim().isEmpty())
                ? Boolean.parseBoolean(headlessFromCli.trim())
                : Boolean.parseBoolean(prop.getProperty("headless", "true").trim());

        // 7. Initialize Playwright & Browser (מופע ייחודי לכל Thread)
        Playwright playwright = Playwright.create();
        Browser browser;

        if ("firefox".equalsIgnoreCase(browserName)) {
            browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(isHeadless));
        } else if ("safari".equalsIgnoreCase(browserName) || "webkit".equalsIgnoreCase(browserName)) {
            browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(isHeadless));
        } else {
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(isHeadless)
            );
        }

        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        // שמירת המופעים ב-ThreadLocal
        playwrightTL.set(playwright);
        browserTL.set(browser);
        contextTL.set(context);
        pageTL.set(page);

        PlaywrightAssertions.setDefaultAssertionTimeout(2000);

        if (base_url != null && !base_url.isEmpty()) {
            getPage().navigate(base_url);
        } else {
            throw new IllegalStateException("base_url is null or empty. Check environments.json or config.properties.");
        }
    }

    /**
     * Reads src/test/resources/environments.json and extracts the node for the target environment.
     */
    private void loadEnvironmentConfig(String targetEnv) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream jsonStream = getClass().getClassLoader().getResourceAsStream("environments.json")) {
            InputStream is = (jsonStream != null)
                    ? jsonStream
                    : new FileInputStream("src/test/resources/environments.json");

            JsonNode rootNode = mapper.readTree(is);

            // Access the nested "environments" object if present, otherwise fall back to rootNode
            JsonNode environmentsNode = rootNode.has("environments")
                    ? rootNode.get("environments")
                    : rootNode;

            if (environmentsNode.has(targetEnv)) {
                envData = environmentsNode.get(targetEnv);
            } else {
                throw new IllegalArgumentException("Environment key '" + targetEnv + "' not found in environments.json!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read environments.json configuration", e);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            // 1. צילום מסך ל-Allure במידה והטסט נכשל (מתבצע לפני סגירת ה-Page)
            if (result.getStatus() == ITestResult.FAILURE && pageTL.get() != null) {
                try {
                    byte[] screenshot = pageTL.get().screenshot(new Page.ScreenshotOptions().setFullPage(true));
                    Allure.addAttachment("Failure Screenshot", "image/png", new ByteArrayInputStream(screenshot), "png");
                } catch (Exception e) {
                    System.err.println("Failed to capture screenshot for Allure: " + e.getMessage());
                }
            }

            // 2. סגירת משאבי Playwright
            if (pageTL.get() != null) pageTL.get().close();
            if (contextTL.get() != null) contextTL.get().close();
            if (browserTL.get() != null) browserTL.get().close();
            if (playwrightTL.get() != null) playwrightTL.get().close();
        } finally {
            // ניקוי זיכרון חובה של ה-ThreadLocal כדי למנוע Memory Leaks
            pageTL.remove();
            contextTL.remove();
            browserTL.remove();
            playwrightTL.remove();
        }
    }
}