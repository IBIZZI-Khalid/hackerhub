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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MicrosoftScraperService implements ProviderScraperService {

    private static final Logger log = LoggerFactory.getLogger(MicrosoftScraperService.class);
    private final EventService eventService;

    public MicrosoftScraperService(EventService eventService) {
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
        List<Event> allEvents = new ArrayList<>();
        com.hackhub.model.dto.ScrapeRequest req = new com.hackhub.model.dto.ScrapeRequest();
        req.setCount(100);
        req.setScrapeType("ALL");
        scrapeStream(req, allEvents::add);
        return allEvents;
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

            // If type is null/empty, default to ALL
            if (type == null || type.isEmpty()) {
                scrapeCerts = true;
                scrapeHacks = true;
            }

            // Scrape hackathons
            if (scrapeHacks && currentCount.get() < limit) {
                scrapeHackathons(driver, onEvent, limit, currentCount, keyword);
            }

            // Scrape certifications and courses
            if (scrapeCerts && currentCount.get() < limit) {
                scrapeCertifications(driver, onEvent, limit, currentCount, keyword);

                // Scrape courses
                if (currentCount.get() < limit) {
                    scrapeCourses(driver, onEvent, limit, currentCount, keyword);
                }
            }

        } catch (Exception e) {
            log.error("Error during Microsoft scraping", e);
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

        try {
            log.info("Starting Microsoft certification scraping...");

            // Navigate to credentials page
            if (keyword != null && !keyword.isEmpty()) {
                // Use search endpoint if keyword provided
                driver.get("https://learn.microsoft.com/en-us/credentials/browse/?terms="
                        + java.net.URLEncoder.encode(keyword, "UTF-8"));
                log.info("Navigated to Microsoft Credentials search page with keyword: {}", keyword);
            } else {
                driver.get("https://learn.microsoft.com/en-us/credentials/browse/");
                log.info("Navigated to Microsoft Credentials page");
            }

            Thread.sleep(5000);

            // Handle cookie popup if present
            handleCookiePopup(driver);

            // Scroll to load more content
            scrollToLoadContent(driver, 3);

            // Find all certification cards
            List<WebElement> cards = driver.findElements(By.cssSelector(".card"));
            log.info("Found {} certification cards", cards.size());

            for (WebElement card : cards) {
                if (currentCount.get() >= limit)
                    break;
                try {
                    // Extract title and URL (.card-title is itself an <a> tag)
                    WebElement titleElement = card.findElement(By.cssSelector(".card-title"));
                    String title = titleElement.getText().trim();
                    String url = titleElement.getAttribute("href");

                    if (title.isEmpty()) {
                        continue;
                    }

                    // Extra check for keyword match (sometimes search is broad)
                    if (keyword != null && !keyword.isEmpty()) {
                        if (!title.toLowerCase().contains(keyword.toLowerCase())) {
                            continue;
                        }
                    }

                    // Extract level from metadata (last li element is usually the level)
                    String level = "";
                    try {
                        List<WebElement> metadata = card
                                .findElements(By.cssSelector(".card-template-detail .metadata li"));
                        if (!metadata.isEmpty()) {
                            // Level is typically the last item in the metadata list
                            level = metadata.get(metadata.size() - 1).getText().trim();
                        }
                    } catch (Exception e) {
                        log.debug("Could not extract level");
                    }

                    // Create Event
                    Event event = new Event();
                    event.setTitle(title);
                    event.setDescription("Microsoft certification: " + title);
                    event.setSourceUrl(url);
                    event.setType(EventType.CERTIFICATION);
                    event.setProvider("Microsoft");
                    event.setLevel(level);
                    event.setLocation("Online");
                    event.setPrice(0.0);

                    // Save and get event with ID
                    Event savedEvent = eventService.saveEvent(event);
                    onEvent.accept(savedEvent); // Emit event with ID
                    currentCount.incrementAndGet();
                    log.info("Saved Microsoft certification: {} (ID: {})", title, savedEvent.getId());

                } catch (Exception e) {
                    log.warn("Failed to extract certification: {}", e.getMessage());
                }
            }

            log.info("Certification scraping completed.");

        } catch (Exception e) {
            log.error("Error during certification scraping", e);
        }
    }

    private void scrapeCourses(WebDriver driver, java.util.function.Consumer<Event> onEvent, int limit,
            java.util.concurrent.atomic.AtomicInteger currentCount, String keyword) {
        if (currentCount.get() >= limit)
            return;

        try {
            log.info("Starting Microsoft course scraping...");

            // Navigate to training page
            driver.navigate().refresh();
            Thread.sleep(3000);

            if (keyword != null && !keyword.isEmpty()) {
                driver.get("https://learn.microsoft.com/en-us/training/browse/?terms="
                        + java.net.URLEncoder.encode(keyword, "UTF-8"));
                log.info("Navigated to Microsoft Training search page with keyword: {}", keyword);
            } else {
                driver.get("https://learn.microsoft.com/en-us/training/browse/");
                log.info("Navigated to Microsoft Training page");
            }

            Thread.sleep(5000);

            // Scroll to load content
            scrollToLoadContent(driver, 2);

            // Find all course cards
            List<WebElement> cards = driver.findElements(By.cssSelector(".card"));
            log.info("Found {} course cards", cards.size());

            // Limit to reasonable number (first 50)
            int maxCourses = Math.min(cards.size(), 50);

            for (int i = 0; i < maxCourses; i++) {
                if (currentCount.get() >= limit)
                    break;
                WebElement card = cards.get(i);
                try {
                    // Extract title and URL (.card-title is itself an <a> tag)
                    WebElement titleElement = card.findElement(By.cssSelector(".card-title"));
                    String title = titleElement.getText().trim();
                    String url = titleElement.getAttribute("href");

                    if (title.isEmpty()) {
                        continue;
                    }

                    // Extra keyword check
                    if (keyword != null && !keyword.isEmpty()) {
                        if (!title.toLowerCase().contains(keyword.toLowerCase())) {
                            continue;
                        }
                    }

                    // Extract level from metadata
                    String level = "";
                    try {
                        List<WebElement> metadata = card
                                .findElements(By.cssSelector(".card-template-detail .metadata li"));
                        for (WebElement meta : metadata) {
                            String text = meta.getText().trim();
                            if (text.matches("Beginner|Intermediate|Advanced")) {
                                level = text;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Could not extract level");
                    }

                    // Create Event
                    Event event = new Event();
                    event.setTitle(title);
                    event.setDescription("Microsoft course: " + title);
                    event.setSourceUrl(url);
                    event.setType(EventType.COURSE);
                    event.setProvider("Microsoft");
                    event.setLevel(level);
                    event.setLocation("Online");
                    event.setPrice(0.0);

                    // Save and get event with ID
                    Event savedEvent = eventService.saveEvent(event);
                    onEvent.accept(savedEvent); // Emit event with ID
                    currentCount.incrementAndGet();
                    log.info("Saved Microsoft course: {} (ID: {})", title, savedEvent.getId());

                } catch (Exception e) {
                    log.warn("Failed to extract course: {}", e.getMessage());
                }
            }

            log.info("Course scraping completed.");

        } catch (Exception e) {
            log.error("Error during course scraping", e);
        }
    }

    private void scrapeHackathons(WebDriver driver, java.util.function.Consumer<Event> onEvent, int limit,
            java.util.concurrent.atomic.AtomicInteger currentCount, String keyword) {
        if (currentCount.get() >= limit)
            return;

        try {
            log.info("Starting Microsoft hackathon/event scraping...");

            // Navigate to events page
            // driver.navigate().refresh();
            // Thread.sleep(3000);

            // Note: Microsoft Events doesn't have a simple URL query param for search on
            // this page easily accessible
            // So we will filter results after fetching
            driver.get("https://developer.microsoft.com/en-us/events/");
            Thread.sleep(5000);

            log.info("Navigated to Microsoft Developer Events page");

            // Scroll to load events
            scrollToLoadContent(driver, 2);

            // Find all event cards
            List<WebElement> cards = driver.findElements(By.cssSelector(".card.h-100"));
            log.info("Found {} event cards", cards.size());

            for (WebElement card : cards) {
                if (currentCount.get() >= limit)
                    break;
                try {
                    // Extract title
                    String title = card.findElement(By.cssSelector(".card-title")).getText().trim();

                    // Only process if it's a hackathon/challenge
                    if (!title.toLowerCase().contains("hackathon") &&
                            !title.toLowerCase().contains("challenge")) {
                        continue;
                    }

                    // Extract description early for keyword filtering
                    String description = "";
                    try {
                        description = card.findElement(By.cssSelector(".card-text")).getText().trim();
                    } catch (Exception e) {
                        // Description is optional
                    }

                    // Keyword filter - check title OR description
                    if (keyword != null && !keyword.isEmpty()) {
                        boolean matchesTitle = title.toLowerCase().contains(keyword.toLowerCase());
                        boolean matchesDescription = description.toLowerCase().contains(keyword.toLowerCase());

                        if (!matchesTitle && !matchesDescription) {
                            continue; // Skip if keyword not in title or description
                        }
                    }

                    // Extract URL
                    String url = "";
                    try {
                        WebElement link = card.findElement(By.cssSelector("a"));
                        url = link.getAttribute("href");
                        if (url == null || url.isEmpty()) {
                            log.warn("URL is empty for event: {}", title);
                        }
                    } catch (Exception e) {
                        log.warn("Could not extract URL for event: {}", title);
                    }

                    // Extract date and format
                    String dateText = "";
                    String format = "";
                    LocalDate eventDate = null;
                    try {
                        List<WebElement> paragraphs = card.findElements(By.cssSelector(".card-body p"));
                        for (WebElement p : paragraphs) {
                            String text = p.getText().trim();
                            // Look for "Date/time (PT):" paragraph
                            if (text.contains("Date/time (PT):")) {
                                // Extract date part after "Date/time (PT):"
                                dateText = text.replace("Date/time (PT):", "").trim();
                                eventDate = parseEventDate(dateText);
                                log.debug("Extracted date: {}", dateText);
                            } else if (text.contains("Format:")) {
                                format = text.replace("Format:", "").trim();
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Could not extract date/format: {}", e.getMessage());
                    }

                    // Use the description we already extracted (or fallback to title)
                    if (description == null || description.isEmpty()) {
                        description = "Microsoft Event: " + title;
                    }

                    // Create Event
                    Event event = new Event();
                    event.setTitle(title);
                    event.setDescription(description); // Use actual description
                    event.setSourceUrl(url);
                    event.setType(EventType.HACKATHON);
                    event.setProvider("Microsoft");
                    event.setLocation(
                            format.contains("Online") || format.contains("Livestream") ? "Online" : "In Person");
                    event.setPrice(0.0);
                    event.setEventDate(eventDate);

                    // Save and get event with ID
                    Event savedEvent = eventService.saveEvent(event);
                    onEvent.accept(savedEvent); // Emit event with ID
                    currentCount.incrementAndGet();
                    log.info("Saved Microsoft hackathon: {} (URL: {}, ID: {})", title, url, savedEvent.getId());

                } catch (Exception e) {
                    log.warn("Failed to extract event: {}", e.getMessage());
                }
            }

            log.info("Hackathon scraping completed.");

        } catch (Exception e) {
            log.error("Error during hackathon scraping", e);
        }
    }

    private void handleCookiePopup(WebDriver driver) {
        try {
            Thread.sleep(2000);
            List<WebElement> acceptButtons = driver
                    .findElements(By.cssSelector("button[id*='accept'], button[class*='accept']"));
            if (!acceptButtons.isEmpty()) {
                acceptButtons.get(0).click();
                Thread.sleep(1000);
                log.info("Accepted cookie popup");
            }
        } catch (Exception e) {
            log.debug("No cookie popup found or already accepted");
        }
    }

    private void scrollToLoadContent(WebDriver driver, int times) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (int i = 0; i < times; i++) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private LocalDate parseEventDate(String dateText) {
        if (dateText == null || dateText.isEmpty()) {
            return null;
        }

        try {
            // Remove time part if present (e.g., "January 13, 2026 08:00 am" -> "January
            // 13, 2026")
            String datePart = dateText.split(" \\d{1,2}:\\d{2}")[0].trim();

            // Parse date in format "January 13, 2026" or "Month Day, Year"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
            return LocalDate.parse(datePart, formatter);
        } catch (DateTimeParseException e) {
            log.warn("Could not parse date: '{}'", dateText);
            return null;
        }
    }
}




