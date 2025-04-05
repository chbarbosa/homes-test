import com.microsoft.playwright.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.junit.jupiter.api.*;

public class HomesAppHomeTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;
    private Locator filterInput;
    private Locator searchButton;
    private Locator cleanButton;

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
    void teardown(TestInfo testInfo) {
        if (testInfo.getTags().contains("failed")) {
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/" + testInfo.getDisplayName() + ".png")));
        }
        context.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
        page.navigate("http://localhost:4200");
        page.waitForSelector("section.results", 
            new Page.WaitForSelectorOptions().setTimeout(30000));
        this.filterInput = page.locator("input[name='filterText']");
        this.searchButton = page.locator("button:has-text('Search')");
        this.cleanButton = page.locator("button:has-text('Clean')");
    }
    
    @Test
    public void shouldHaveFilterComponentsVisible() {
        assertAll(
            () -> assertTrue(this.filterInput.isVisible(), 
                "Filter input should be visible"),
            () -> assertTrue(this.searchButton.isVisible(), 
                "Search button should be visible"),
            () -> assertTrue(this.cleanButton.isVisible(), 
                "Clean button should be visible")
        );
    }
    
    @Test
    void shouldFilterByLocation() {
        // Type into search field
        this.filterInput.fill("Chicago");
        this.searchButton.click();
        
        // Wait for filtering
        page.waitForTimeout(1000); // Replace with proper wait in real app
        
        // Verify only Chicago listings are visible
        int visibleHomes = page.locator("app-housing-location:visible").count();
        int chicagoHomes = page.locator("text='Chicago, IL'").count();
        
        assertEquals(visibleHomes, chicagoHomes, 
            "Should only show Chicago homes when filtered");
    }

    @Test
    void shouldDisplayHomesList() {
        Locator results = page.locator("section.results");
        results.waitFor();

        Locator homes = page.locator("app-housing-location");
        assertTrue(homes.count() == 10);

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