import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;

public class AdminPageTest {
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
        context = browser.newContext(new Browser.NewContextOptions()
            .setViewportSize(1280, 720));
        page = context.newPage();
        page.navigate("http://localhost:4200/admin");
        page.waitForSelector("article.admin");
    }

    @Test
    void verifyAdminPageStructure() {
        // 1. Verify header section
        assertAll(
            () -> assertEquals("Admin Page", 
                page.locator("h1.admin-heading").textContent()),
            () -> assertTrue(
                page.locator("p.admin-description:has-text('only accessible to administrators')")
                .isVisible())
        );

        // 2. Verify statistics
        Locator stats = page.locator("section.admin-data p");
        assertAll(
            () -> assertEquals(3, stats.count()),
            () -> assertTrue(stats.nth(0).textContent().contains("searches:")),
            () -> assertTrue(stats.nth(1).textContent().contains("detail views:")),
            () -> assertTrue(stats.nth(2).textContent().contains("applications:"))
        );

        // 3. Verify applications table
        Locator table = page.locator("app-applications table");
        assertAll(
            () -> assertEquals(4, table.locator("th").count()),
            () -> assertTrue(table.locator("th:has-text('Location ID')").isVisible()),
            () -> assertTrue(table.locator("th:has-text('Actions')").isVisible())
        );

        // 4. Verify table rows
        Locator rows = table.locator("tbody tr");
        assertTrue(rows.count() >= 1, "At least one application should exist");

        Locator lastRowActions = rows.last().locator("button");
        assertAll(
            () -> assertEquals(2, lastRowActions.count()),
            () -> assertEquals("confirm", lastRowActions.first().textContent()),
            () -> assertEquals("cancel", lastRowActions.last().textContent())
        );
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        if (testInfo.getTags().contains("failed")) {
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/" + testInfo.getDisplayName() + ".png")));
        }
        context.close();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }
}