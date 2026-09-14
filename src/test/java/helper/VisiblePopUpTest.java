package helper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class VisiblePopUpTest {

    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;


    @BeforeMethod
    public void setup() {

        // open 2 chrome page/tab to work on parallel
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(false));
        ;
        context = browser.newContext();
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        page = context.newPage();
        page.navigate("https://rahulshettyacademy.com/AutomationPractice/");

    }

    @AfterMethod
    public void teardown() {
        // Stop tracing and export it into a zip archive
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace.zip")));
        browser.close();
        playwright.close();
    }

    @Test(description = "visble elements ")
    public void VisibleElementTest() {
        // check element is hidden
        page.locator("#hide-textbox").click();
        assertThat(page.getByPlaceholder("Hide/Show Example")).isHidden();
    }

    @Test(description = "pop up elements ")
    public void PopUpTest() {
        // check element is hidden
        page.onceDialog(dialog -> dialog.accept());  // place it to listen before the click on alert button
        page.locator("#alertbtn").click();
        page.waitForTimeout(3000);
    }

    @Test(description = "hover and tap")
    public void HoverAndTap(){
        page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Mouse Hover")).hover();
        page.getByRole(AriaRole.LINK,new Page.GetByRoleOptions().setName("Top")).click();
    }

    @Test(description = "frame test")
    public void FrameTest(){
        FrameLocator framePage=page.frameLocator("#courses-iframe");
        framePage.getByRole(AriaRole.LINK,new FrameLocator.GetByRoleOptions().setName("Mentorship")).click();
        assertThat(framePage.locator(".inner-box").first()).isVisible();
        String text=framePage.locator(".content-side>div").first().textContent();
    }

    @Test(description = "screenshot on page/locator level test")
    public void ScreenShotTest(){
        // take page screenshot- in this case the page is what opens: "https://rahulshettyacademy.com/AutomationPractice/
       page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("trace.png")));

       // in locator level
        Locator loc=page.getByPlaceholder("Hide/Show Example");
        loc.screenshot(new Locator.ScreenshotOptions().setPath(Paths.get("newTrace.png")));
    }
}
