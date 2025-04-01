import com.microsoft.playwright.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.*;

public class FirstTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(false) // Opens browser window
        );
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void testGoogleSearch() {
        page.navigate("https://google.com");
        System.out.println("Page title: " + page.title());
        Assertions.assertEquals("Google", page.title());
    }

    @Test
    void testWithVirtualThreads() throws InterruptedException, ExecutionException {
        try (var scope = Executors.newVirtualThreadPerTaskExecutor()) {
            scope.submit(() -> {
                page.navigate("https://google.com");
                Assertions.assertEquals("Google", page.title());
            }).get(); // Wait for completion
        }
}
}