package com.hackhub.service.scraper;

import com.hackhub.model.Event;
import com.hackhub.model.EventType;
import com.hackhub.service.EventService;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class IBMScraperService implements ProviderScraperService {

    private static final Logger log = LoggerFactory.getLogger(IBMScraperService.class);
    private final EventService eventService;

    public IBMScraperService(EventService eventService) {
        this.eventService = eventService;
    }

    private WebDriver createDriver() {
        org.openqa.selenium.chrome.ChromeOptions options = new org.openqa.selenium.chrome.ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-extensions");
        return new org.openqa.selenium.chrome.ChromeDriver(options);
    }

    @Override
    public List<Event> scrape() {
        List<Event> events = new ArrayList<>();
        com.hackhub.model.dto.ScrapeRequest req = new com.hackhub.model.dto.ScrapeRequest();
        req.setCount(100);
        req.setScrapeType("ALL");
        scrapeStream(req, events::add);
        return events;
    }

    @Override
    public void scrapeStream(com.hackhub.model.dto.ScrapeRequest request, java.util.function.Consumer<Event> onEvent) {
        WebDriver driver = null;
        try {
            driver = createDriver();
            java.util.concurrent.atomic.AtomicInteger currentCount = new java.util.concurrent.atomic.AtomicInteger(0);
            int limit = request.getCount() > 0 ? request.getCount() : 100;
            String type = request.getScrapeType();
            String keyword = request.getDomain(); // Use domain as search keyword

            // Only scrape the requested type - no fallback to ALL when type is specified
            boolean scrapeCerts = "CERTIFICATES".equalsIgnoreCase(type) || "ALL".equalsIgnoreCase(type);
            boolean scrapeHacks = "HACKATHONS".equalsIgnoreCase(type) || "ALL".equalsIgnoreCase(type);
            boolean scrapeCourses = "COURSES".equalsIgnoreCase(type) || "ALL".equalsIgnoreCase(type);

            // If type is null/empty, default to ALL
            if (type == null || type.isEmpty()) {
                scrapeCerts = true;
                scrapeHacks = true;
                scrapeCourses = true;
            }

            if (scrapeHacks && currentCount.get() < limit) {
                scrapeHackathons(driver, onEvent, limit, currentCount, keyword);
            }

            if (scrapeCerts && currentCount.get() < limit) {
                scrapeCertifications(driver, onEvent, limit, currentCount, keyword);
            }

            if (scrapeCourses && currentCount.get() < limit) {
                scrapeCourses(driver, onEvent, limit, currentCount, keyword);
            }

        } catch (Exception e) {
            log.error("Fatal error during IBM scraping", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void scrapeCertifications(WebDriver driver, java.util.function.Consumer<Event> onEvent, int limit,
            java.util.concurrent.atomic.AtomicInteger currentCount, String keyword) {
        if (currentCount.get() >= limit)
            return;
        Set<String> processedUrls = new HashSet<>();

        log.info("Starting IBM certification scraping...");

        try {
            driver.get("https://www.ibm.com/training/search");
            log.info("Navigated to IBM Training search page.");

            // Wait for page to load
            Thread.sleep(3000);

            // Handle cookie popup if present
            handleCookiePopup(driver);

            // Apply keyword search if provided
            if (keyword != null && !keyword.isEmpty()) {
                applyKeywordSearch(driver, keyword);
            }

            // Apply Certification filter
            log.info("Applying Certification filter...");
            applyLearningTypeFilter(driver, "Certification");
            Thread.sleep(2000); // Wait for results to load

            // Extract certification cards
            List<WebElement> cards = findResultCards(driver);
            log.info("Found {} certification cards", cards.size());

            for (WebElement card : cards) {
                if (currentCount.get() >= limit)
                    break;
                try {
                    Event event = extractCardData(card, EventType.CERTIFICATION);
                    if (event != null && !processedUrls.contains(event.getSourceUrl())) {

                        // Extra check for keyword in title if search wasn't perfect
                        if (keyword != null && !keyword.isEmpty()) {
                            if (!event.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                                continue;
                            }
                        }

                        processedUrls.add(event.getSourceUrl());

                        try {
                            Event savedEvent = eventService.saveEvent(event);
                            onEvent.accept(savedEvent); // Emit event with ID
                            currentCount.incrementAndGet();
                            log.info("Saved IBM certification: {} (ID: {})", event.getTitle(), savedEvent.getId());
                        } catch (Exception e) {
                            log.warn("Failed to save certification '{}': {}", event.getTitle(), e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing certification card", e);
                }
            }

            log.info("Certification scraping completed.");

        } catch (Exception e) {
            log.error("Fatal error in scrapeCertifications", e);
        }
    }

    private void scrapeCourses(WebDriver driver, java.util.function.Consumer<Event> onEvent, int limit,
            java.util.concurrent.atomic.AtomicInteger currentCount, String keyword) {
        if (currentCount.get() >= limit)
            return;
        Set<String> processedUrls = new HashSet<>();

        log.info("Starting IBM course scraping...");

        try {
            // Refresh current page or navigate to search page
            try {
                // Navigate to search page
                driver.navigate().to("https://www.ibm.com/training/search");
                log.info("Navigated to IBM Training search page.");
            } catch (Exception navEx) {
                log.warn("Navigation issue, retrying: {}", navEx.getMessage());
                driver.get("https://www.ibm.com/training/search");
            }

            // Wait for page to load
            Thread.sleep(5000);

            // Handle cookie popup if present
            handleCookiePopup(driver);

            // Apply keyword search if provided
            if (keyword != null && !keyword.isEmpty()) {
                applyKeywordSearch(driver, keyword);
            }

            // Apply Course filter
            log.info("Applying Course filter...");
            applyLearningTypeFilter(driver, "Course");
            Thread.sleep(2000); // Wait for results to load

            // Extract course cards
            List<WebElement> cards = findResultCards(driver);
            log.info("Found {} course cards", cards.size());

            for (WebElement card : cards) {
                if (currentCount.get() >= limit)
                    break;
                try {
                    Event event = extractCardData(card, EventType.COURSE);
                    if (event != null && !processedUrls.contains(event.getSourceUrl())) {

                        // Extra check for keyword
                        if (keyword != null && !keyword.isEmpty()) {
                            if (!event.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                                continue;
                            }
                        }

                        processedUrls.add(event.getSourceUrl());

                        try {
                            eventService.saveEvent(event);
                            onEvent.accept(event); // Emit event
                            currentCount.incrementAndGet();
                            log.info("Saved IBM course: {}", event.getTitle());
                        } catch (Exception e) {
                            log.warn("Failed to save course '{}': {}", event.getTitle(), e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing course card", e);
                }
            }

            log.info("Course scraping completed.");

        } catch (Exception e) {
            log.error("Fatal error in scrapeCourses", e);
        }
    }

    private void applyKeywordSearch(WebDriver driver, String keyword) {
        try {
            WebElement searchInput = driver
                    .findElement(By.cssSelector("input[type='search'], input[placeholder*='Search']"));
            searchInput.clear();
            searchInput.sendKeys(keyword);
            searchInput.sendKeys(org.openqa.selenium.Keys.ENTER);
            Thread.sleep(2000);
            log.info("Applied keyword search: {}", keyword);
        } catch (Exception e) {
            log.warn("Could not apply keyword search: {}", e.getMessage());
        }
    }

    private void handleCookiePopup(WebDriver driver) {
        try {
            // Look for "Accept all" button
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            for (WebElement button : buttons) {
                String buttonText = button.getText().toLowerCase();
                if (buttonText.contains("accept all") || buttonText.contains("accept")) {
                    button.click();
                    log.info("Accepted cookie popup");
                    Thread.sleep(1000);
                    break;
                }
            }
        } catch (Exception e) {
            log.debug("No cookie popup found or already dismissed");
        }
    }

    private void applyLearningTypeFilter(WebDriver driver, String filterType) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // First, try to find and expand the Learning type filter section
            List<WebElement> buttons = driver.findElements(By.tagName("button"));

            // Define variations to look for (e.g. "Course", "Courses")
            List<String> variations = new ArrayList<>();
            variations.add(filterType);
            if (!filterType.endsWith("s")) {
                variations.add(filterType + "s");
            }

            // Look for the filter button with the specified text
            for (WebElement button : buttons) {
                String buttonText = button.getText().trim();
                for (String variation : variations) {
                    if (buttonText.equalsIgnoreCase(variation)) {
                        // Scroll to button
                        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", button);
                        Thread.sleep(500);

                        // Click the filter
                        js.executeScript("arguments[0].click();", button);
                        log.info("Applied {} filter (found as '{}')", filterType, buttonText);
                        return;
                    }
                }
            }

            // If not found directly, try to find checkboxes or radio buttons
            List<WebElement> inputs = driver
                    .findElements(By.cssSelector("input[type='checkbox'], input[type='radio']"));
            for (WebElement input : inputs) {
                WebElement parent = input.findElement(By.xpath("./.."));
                String parentText = parent.getText().trim();
                for (String variation : variations) {
                    if (parentText.equalsIgnoreCase(variation)) {
                        js.executeScript("arguments[0].click();", input);
                        log.info("Applied {} filter via checkbox (found as '{}')", filterType, parentText);
                        return;
                    }
                }
            }

            log.warn("Could not find {} filter button", filterType);

        } catch (Exception e) {
            log.error("Error applying {} filter", filterType, e);
        }
    }

    private List<WebElement> findResultCards(WebDriver driver) {
        List<WebElement> cards = new ArrayList<>();

        try {
            // Wait a bit for cards to render
            Thread.sleep(2000);

            // Find certification or course cards using the correct selectors
            cards = driver.findElements(By.cssSelector(".certification.card, .course.card"));
            log.info("Found {} cards using .card selector", cards.size());

        } catch (Exception e) {
            log.error("Error finding result cards", e);
        }

        return cards;
    }

    private Event extractCardData(WebElement card, EventType eventType) {
        try {
            // Extract title from .title element
            String title = "";
            try {
                WebElement titleElement = card.findElement(By.cssSelector(".title"));
                title = titleElement.getText().trim();
            } catch (Exception e) {
                log.debug("Could not find title element");
                return null;
            }

            if (title.isEmpty()) {
                log.debug("Skipping card with empty title");
                return null;
            }

            // Extract URL from .button-container a
            String url = "";
            try {
                WebElement exploreLink = card.findElement(By.cssSelector(".button-container a"));
                url = exploreLink.getAttribute("href");

                // Make URL absolute if it's relative
                if (url != null && url.startsWith("/")) {
                    url = "https://www.ibm.com" + url;
                }
            } catch (Exception e) {
                log.debug("Could not find explore link");
                return null;
            }

            if (url == null || url.isEmpty()) {
                log.debug("Skipping card with empty URL");
                return null;
            }

            // Extract code from .code strong
            String code = "Unknown";
            try {
                WebElement codeElement = card.findElement(By.cssSelector(".code strong"));
                code = codeElement.getText().trim();
            } catch (Exception e) {
                // Try without strong tag
                try {
                    WebElement codeElement = card.findElement(By.cssSelector(".code"));
                    String codeText = codeElement.getText().trim();
                    // Remove "Code: " prefix if present
                    code = codeText.replace("Code:", "").trim();
                } catch (Exception ex) {
                    log.debug("Could not extract code");
                }
            }

            // Extract level from .level
            String level = "Not specified";
            try {
                WebElement levelElement = card.findElement(By.cssSelector(".level"));
                level = levelElement.getText().trim();
                // Remove any icon text that might be included
                level = level.replaceAll("^[^A-Za-z]+", "").trim();
            } catch (Exception e) {
                log.debug("Could not extract level");
            }

            // Create Event object
            Event event = new Event();
            event.setTitle(title);
            event.setDescription("IBM " + eventType.name().toLowerCase() + ": " + title);
            event.setSourceUrl(url);
            event.setType(eventType);
            event.setProvider("IBM");
            event.setExamCode(code);
            event.setLevel(level);
            event.setLocation("Online");
            event.setPrice(0.0);
            event.setEventDate((java.time.LocalDate) null);

            return event;

        } catch (Exception e) {
            return null;
        }
    }

    private void scrapeHackathons(WebDriver driver, java.util.function.Consumer<Event> onEvent, int limit,
            java.util.concurrent.atomic.AtomicInteger currentCount, String keyword) {
        if (currentCount.get() >= limit)
            return;

        try {
            log.info("Starting IBM hackathon scraping...");

            // Navigate to IBM Developer Events page
            // driver.navigate().refresh(); // Removed to avoid timeouts
            // Thread.sleep(3000);
            driver.navigate().to("https://developer.ibm.com/events/");
            Thread.sleep(5000); // Wait for page load

            log.info("Navigated to IBM Developer Events page");

            // Handle cookie popup if present
            handleCookiePopup(driver);

            // Apply keyword filter (IBM Developer events page has a filter input)
            if (keyword != null && !keyword.isEmpty()) {
                try {
                    WebElement searchInput = driver.findElement(By.cssSelector("input[placeholder*='Search']"));
                    searchInput.clear();
                    searchInput.sendKeys(keyword);
                    searchInput.sendKeys(org.openqa.selenium.Keys.ENTER);
                    Thread.sleep(3000);
                } catch (Exception e) {
                    log.warn("Could not search for keyword on hackathons page");
                }
            }

            // Apply Hackathon filter
            applyHackathonFilter(driver);

            // Wait for results to load
            Thread.sleep(4000);

            // Find all hackathon cards
            List<WebElement> cards = driver.findElements(By.cssSelector(".developer--card"));
            log.info("Found {} hackathon cards", cards.size());

            for (WebElement card : cards) {
                if (currentCount.get() >= limit)
                    break;
                try {
                    // Extract title
                    String title = "";
                    try {
                        WebElement titleElement = card.findElement(By.cssSelector(".developer--card__title, h3"));
                        title = titleElement.getText().trim();
                    } catch (Exception e) {
                        log.debug("Could not extract title");
                        continue;
                    }

                    if (title.isEmpty()) {
                        continue;
                    }

                    // Extract URL
                    String url = "";
                    try {
                        WebElement linkElement = card.findElement(By.cssSelector(".developer--card__block_link, a"));
                        url = linkElement.getAttribute("href");
                    } catch (Exception e) {
                        log.debug("Could not extract URL");
                    }

                    // Extract description
                    String description = "";
                    try {
                        WebElement descElement = card.findElement(By.cssSelector(".developer--card__excerpt"));
                        description = descElement.getText().trim();
                    } catch (Exception e) {
                        description = "IBM Hackathon: " + title;
                    }

                    if (description.isEmpty()) {
                        description = "IBM Hackathon: " + title;
                    }

                    // Extra check for keyword
                    if (keyword != null && !keyword.isEmpty()) {
                        if (!title.toLowerCase().contains(keyword.toLowerCase()) &&
                                !description.toLowerCase().contains(keyword.toLowerCase())) {
                            continue;
                        }
                    }

                    // Create Event object
                    Event event = new Event();
                    event.setTitle(title);
                    event.setDescription(description);
                    event.setSourceUrl(url);
                    event.setType(EventType.HACKATHON);
                    event.setProvider("IBM");
                    event.setLocation("Online"); // Most IBM hackathons are online
                    event.setPrice(0.0); // IBM hackathons are typically free
                    event.setEventDate((java.time.LocalDate) null); // Date parsing can be added later if needed

                    // Save event
                    try {
                        eventService.saveEvent(event);
                        onEvent.accept(event); // Emit event
                        currentCount.incrementAndGet();
                        log.info("Saved IBM hackathon: {}", title);
                    } catch (Exception e) {
                        log.warn("Failed to save hackathon '{}': {}", title, e.getMessage());
                    }

                } catch (Exception e) {
                    log.warn("Failed to extract hackathon data: {}", e.getMessage());
                }
            }

            log.info("Hackathon scraping completed.");

        } catch (Exception e) {
            log.error("Error during hackathon scraping", e);
        }
    }

    private void applyHackathonFilter(WebDriver driver) {
        try {
            log.info("Applying Hackathon filter...");

            // Wait a bit for the page to fully load
            Thread.sleep(2000);

            // Try to find and click the hackathon checkbox
            try {
                WebElement hackathonCheckbox = driver.findElement(By.id("upcoming_hackathon"));

                if (!hackathonCheckbox.isSelected()) {
                    // Scroll to the checkbox
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
                            hackathonCheckbox);
                    Thread.sleep(500);

                    // Click the checkbox using JS to avoid interception
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", hackathonCheckbox);
                    Thread.sleep(1000);
                    log.info("Applied Hackathon filter via checkbox (JS)");
                } else {
                    log.info("Hackathon filter already selected");
                }
            } catch (Exception e) {
                log.warn("Could not find hackathon checkbox with ID 'upcoming_hackathon', trying alternative approach");

                // Alternative: try to find by label text
                List<WebElement> labels = driver.findElements(By.cssSelector("label"));
                for (WebElement label : labels) {
                    if (label.getText().toLowerCase().contains("hackathon")) {
                        // Click using JS
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", label);
                        Thread.sleep(1000);
                        log.info("Applied Hackathon filter via label click (JS)");
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error applying Hackathon filter", e);
        }
    }
}
