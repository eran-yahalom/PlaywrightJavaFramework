package helper;

import com.microsoft.playwright.*;
import org.testng.annotations.BeforeMethod;

public class OpenTwoChromeTabsParallel {
    Playwright playwright;
    Browser browser;
    BrowserContext contextA;
    BrowserContext contextB;
    Page pageA;
    Page pageB;

    @BeforeMethod
    public void setup() {

        // open 2 chrome page/tab to work on parallel
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(false));
        contextA=browser.newContext();
        contextB=browser.newContext();
        pageA=contextA.newPage();
        pageB=contextB.newPage();
    }

}
