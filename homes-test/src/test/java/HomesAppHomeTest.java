import com.microsoft.playwright.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;

public class HomesAppHomeTest {
    private final String HOMES_URL = "http://localhost:4200";
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
        page.navigate("http://localhost:4200");
        page.waitForSelector("section.results", 
            new Page.WaitForSelectorOptions().setTimeout(30000));
    }

    @Test
    void shouldDisplayHomesList() {
        Locator results = page.locator("section.results");
        results.waitFor();

        Locator homes = page.locator("app-housing-location");
        assertTrue(homes.count() > 0);

        Locator firstHome = homes.first();
        assertAll(
            () -> assertTrue(firstHome.locator(".listing-photo").isVisible()),
            () -> assertFalse(firstHome.locator(".listing-heading").textContent().isEmpty())
        );
    }

    @Test
    void shouldNavigateToDetailsPage() {
        page.locator("app-housing-location").first().waitFor();
        String expectedTitle = page.locator(".listing-heading").first().textContent();
        
        page.locator("app-housing-location a").first().click();
        page.waitForURL(url -> url.contains("/details/"), 
            new Page.WaitForURLOptions().setTimeout(10000));
        
        assertEquals(expectedTitle, 
            page.locator("h2").first().textContent());
    }
}