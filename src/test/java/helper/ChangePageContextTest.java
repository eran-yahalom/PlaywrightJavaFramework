package helper;

import com.microsoft.playwright.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

public class ChangePageContextTest {
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
        page = context.newPage();
        page.navigate("https://rahulshettyacademy.com/loginpagePractise/");

    }

    @Test
    public void childWindowHandeling() {
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
    }
}
