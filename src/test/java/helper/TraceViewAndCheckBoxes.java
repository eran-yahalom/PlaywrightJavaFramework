package helper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

public class TraceViewAndCheckBoxes {
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
        page.navigate("https://rahulshettyacademy.com/loginpagePractise/");

    }

    @AfterMethod
    public void teardown() {
        // Stop tracing and export it into a zip archive
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace.zip")));
        browser.close();
        playwright.close();
    }

    @Test
    public void TraceAndCheckBoxesTest() {
        // handle the page in the new tab that opened after clicking the link
        // the new page will be saved inside newPage variable
        // and we can use it to perform actions on the new page

        Page newPage = context.waitForPage(() -> {
            page.locator("a[href='https://rahulshettyacademy.com/documents-request']").click();
        });
        newPage.waitForLoadState();
        System.out.println(newPage.title());
        assertTrue(newPage.getByText("Documents request").isVisible());

        // get all the text in page that is written in red
        String allRedTextWithTheEmail = newPage.locator(".red").textContent();

        String email = newPage.getByText(Pattern.compile("\\@", Pattern.CASE_INSENSITIVE)).first().innerText().trim();

        // switch back to the first page-
        // no need to switch back to the first page because we are already
        // on the first page, but we can bring it to front
        page.bringToFront();
        //go back to the first page and fill the email in the username field
        page.getByLabel("Username").fill(email);
        // get value from username input field
        page.getByLabel("Username").inputValue();

        // in order to see the trace, we need to check the checkboxes for screenshots,
        // snapshots and sources in the trace.zip file
        // run: mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="show-trace https://example.com/trace.zip"
        //and place there the zip file from yout project folder
    }

    @Test(description = "Use radio button,checkBox and dropdown")
    public void UIControlsTest() {
        page.waitForLoadState();
        // click on radio button User
        Locator userRadio = page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("User"));
        userRadio.check();

// handle pop up
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Okay")).click();
        Assert.assertTrue(userRadio.isChecked());

        //click on i agree checkbox
        Locator terms = page.getByRole(AriaRole.CHECKBOX);
        terms.check();
        Assert.assertTrue(terms.isChecked());

        //dropdown with no label  so we cant use:  page.getByLabel("#category").selectOption("Festival");
        // page.locator("select.form-control").selectOption("Teacher");
        page.getByRole(AriaRole.COMBOBOX).selectOption("Teacher");

    }
}