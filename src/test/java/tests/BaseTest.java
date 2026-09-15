package tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Allure;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaseTest {

    // 1. Playwright & Browser נשמרים ברמת ה-Thread לשימוש חוזר לאורך כל המחלקה
    private static final ThreadLocal<Playwright> playwrightTL = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserTL = new ThreadLocal<>();

    // 2. Context & Page מוקמים מחדש לכל טסט בנפרד (מבטיח בידוד מוחלט וריצה מהירה)
    private static final ThreadLocal<BrowserContext> contextTL = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageTL = new ThreadLocal<>();

    protected String base_url;
    protected String env;
    protected JsonNode envData;
    protected static Properties prop = new Properties();

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

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() throws IOException {
        // טעינת config.properties פעם אחת בלבד לפני תחילת הריצה
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                prop.load(input);
            } else {
                try (FileInputStream fis = new FileInputStream("src/test/resources/config.properties")) {
                    prop.load(fis);
                }
            }
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws IOException {
        // 1. Resolve environment & config
        String envFromCli = System.getProperty("env");
        env = (envFromCli != null && !envFromCli.trim().isEmpty())
                ? envFromCli.trim()
                : prop.getProperty("env", "qa1").trim();

        loadEnvironmentConfig(env);

        if (envData != null && envData.has("url")) {
            base_url = envData.get("url").asText();
        } else {
            base_url = prop.getProperty("qa.base.url");
        }

        // 2. Lazy Initialization: פתיחת Browser & Playwright פעם אחת בלבד ל-Thread
        if (playwrightTL.get() == null) {
            Playwright playwright = Playwright.create();
            playwrightTL.set(playwright);

            String browserFromCli = System.getProperty("browser");
            String browserName = (browserFromCli != null && !browserFromCli.trim().isEmpty())
                    ? browserFromCli.trim()
                    : prop.getProperty("browser", "chrome").trim();

            String headlessFromCli = System.getProperty("headless");
            boolean isHeadless = (headlessFromCli != null && !headlessFromCli.trim().isEmpty())
                    ? Boolean.parseBoolean(headlessFromCli.trim())
                    : Boolean.parseBoolean(prop.getProperty("headless", "true").trim());

            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(isHeadless);

            Browser browser;
            if ("firefox".equalsIgnoreCase(browserName)) {
                browser = playwright.firefox().launch(options);
            } else if ("safari".equalsIgnoreCase(browserName) || "webkit".equalsIgnoreCase(browserName)) {
                browser = playwright.webkit().launch(options);
            } else {
                browser = playwright.chromium().launch(options);
            }
            browserTL.set(browser);
        }

        // 3. יצירת Context ו-Page חדשים ונקיים לכל טסט (~50ms בלבד)
        BrowserContext context = browserTL.get().newContext();
        Page page = context.newPage();

        contextTL.set(context);
        pageTL.set(page);

        // הגדלת דיפולט ה-Timeout מ-2000ms ל-5000ms למניעת Flaky Tests ב-CI
        PlaywrightAssertions.setDefaultAssertionTimeout(5000);

        if (base_url != null && !base_url.isEmpty()) {
            getPage().navigate(base_url);
        } else {
            throw new IllegalStateException("base_url is null or empty. Check environments.json or config.properties.");
        }
    }

    private void loadEnvironmentConfig(String targetEnv) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream jsonStream = getClass().getClassLoader().getResourceAsStream("environments.json")) {
            InputStream is = (jsonStream != null)
                    ? jsonStream
                    : new FileInputStream("src/test/resources/environments.json");

            JsonNode rootNode = mapper.readTree(is);
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
            if (result.getStatus() == ITestResult.FAILURE && pageTL.get() != null) {
                try {
                    byte[] screenshot = pageTL.get().screenshot(new Page.ScreenshotOptions().setFullPage(true));
                    Allure.addAttachment("Failure Screenshot", "image/png", new ByteArrayInputStream(screenshot), "png");
                } catch (Exception e) {
                    System.err.println("Failed to capture screenshot for Allure: " + e.getMessage());
                }
            }

            if (pageTL.get() != null) pageTL.get().close();
            if (contextTL.get() != null) contextTL.get().close();
        } finally {
            pageTL.remove();
            contextTL.remove();
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (browserTL.get() != null) {
            browserTL.get().close();
            browserTL.remove();
        }
        if (playwrightTL.get() != null) {
            playwrightTL.get().close();
            playwrightTL.remove();
        }
    }
}