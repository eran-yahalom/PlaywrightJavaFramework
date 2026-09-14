package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class RegistrationPage {
    Page page;
    private static final String DISCOVER_AND_BOOK_TEXT = "Discover & Book";
    private static final String EMAIL_LABEL = "Email";
    private static final String EMAIL_PLACEHOLDER = "you@email.com";
    private static final String PASSWORD_LABEL = "Password";
    private static final String CONFIRM_PASSWORD_PLACEHOLDER = "Repeat your password";
    private static final String PASSWORD_PLACEHOLDER = "Min 8 chars, uppercase, number & symbol";
    private static final String REGISTRATION_SUCCESS_MESSAGE = "Registration successful";
    private static final String EMAIL_ALREADY_EXIST = "Email already registered";
    private static final String EMPTY_PASSWORD = "Password does not meet the requirements below";
    private static final String EMAIL_NOT_VALID = "Enter a valid email";
    private static final String PASSWORD_NOT_MATCH = "Passwords do not match";
    private static final String CREATE_ACCOUNT_BUTTON = "Create Account";
    private static final String SIGN_UP_LINK = "Create Account";

    public RegistrationPage(Page page) {
        this.page = page;
    }

    public DashboardPage enterRegistrationInfo(String email, String password, String confirmPassword) {
        page.getByPlaceholder(EMAIL_PLACEHOLDER).fill(email);
        page.getByPlaceholder(PASSWORD_PLACEHOLDER).fill(password);
        page.getByPlaceholder(CONFIRM_PASSWORD_PLACEHOLDER).fill(confirmPassword);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(CREATE_ACCOUNT_BUTTON)).click();
        return new DashboardPage(page);
    }
}
