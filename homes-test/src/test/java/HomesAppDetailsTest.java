import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;


public class HomesAppDetailsTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeEach
    void setup() {
        context = browser.newContext();
        page = context.newPage();
        page.navigate("http://localhost:4200/details/0");
        page.waitForSelector("app-details");
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @Test
    void shouldDisplayCorrectHousingDetails() {
        // Main photo
        Locator photo = page.locator(".listing-photo");
        assertAll(
                () -> assertTrue(photo.isVisible()),
                () -> assertEquals("Exterior photo of Acme Fresh Start Housing",
                        photo.getAttribute("alt")));

        // Title and location
        assertAll(
                () -> assertEquals("Acme Fresh Start Housing",
                        page.locator(".listing-heading").textContent()),
                () -> assertEquals("Chicago, IL",
                        page.locator(".listing-location").textContent()));

        // Amenities list
        Locator features = page.locator(".listing-features li");
        assertEquals(3, features.count());
        assertAll(
                () -> assertTrue(features.nth(0).textContent().contains("Units available: 4")),
                () -> assertTrue(features.nth(1).textContent().contains("wifi: true")),
                () -> assertTrue(features.nth(2).textContent().contains("laundry: true")));
    }

    @Test
    void shouldDisplayApplicationForm() {
        // Locate the form section
        Locator formSection = page.locator(".listing-apply");
        assertTrue(formSection.isVisible(), "Apply section should be visible");

        // Verify form structure
        Locator form = formSection.locator("form");
        assertAll(
                () -> assertTrue(form.isVisible(), "Form should be present"),
                () -> assertEquals("Apply now to live here",
                        formSection.locator(".section-heading").textContent(),
                        "Form heading should match"));

        // Check all form fields
        String[] fields = { "First Name", "Last Name", "Email" };
        for (String field : fields) {
            assertTrue(
                    form.locator("label:has-text('" + field + "')").isVisible(),
                    "Field label '" + field + "' should exist");
        }

        // Verify submit button
        Locator submitButton = form.locator("button.primary");
        assertAll(
                () -> assertTrue(submitButton.isVisible(),
                        "Submit button should be visible"),
                () -> assertEquals("Apply", submitButton.textContent(),
                        "Button text should match"));
    }

    @Test
    void shouldShowValidationErrors() {
        page.locator("button:has-text('Apply')").click();
        
        assertTrue(page.locator("text='Some required fields are missing'").isVisible());
    }
}