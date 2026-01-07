package com.hackhub.service.scraper;

import com.hackhub.model.Event;
import com.hackhub.model.EventType;
import com.hackhub.service.EventService;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class OracleScraperService implements ProviderScraperService {

    private static final Logger log = LoggerFactory.getLogger(OracleScraperService.class);
    private final EventService eventService;

    @lombok.RequiredArgsConstructor
    private static class DriverCloser implements AutoCloseable {
        private final WebDriver driver;

        @Override
        public void close() {
            if (driver != null)
                driver.quit();
        }
    }

    public OracleScraperService(EventService eventService) {
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
    public void scrapeStream(com.hackhub.model.dto.ScrapeRequest request, Consumer<Event> onEvent) {
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

            if (scrapeHacks && currentCount.get() < limit) {
                scrapeHackathons(driver, onEvent, limit, currentCount, keyword);
            }

            if (scrapeCerts) {
                scrapeCertifications(driver, onEvent, limit, currentCount, keyword);
            }

        } catch (Exception e) {
            log.error("Fatal error during Oracle scraping", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void scrapeCertifications(WebDriver webDriver, Consumer<Event> onEvent, int limit,
            java.util.concurrent.atomic.AtomicInteger currentCount, String keyword) {
        Set<String> processedUrls = new HashSet<>();

        log.info("Starting Oracle certification scraping...");

        try {
            webDriver.get("https://www.oracle.com/education/certification/");
            log.info("Navigated to Oracle Certification page.");

            // Wait for page to load
            Thread.sleep(3000);

            // Dismiss country selector popup if present
            try {
                WebElement popupButton = webDriver.findElement(
                        By.xpath("//button[contains(text(), 'No thanks')]"));
                popupButton.click();
                log.info("Dismissed country selector popup.");
                Thread.sleep(1000);
            } catch (Exception e) {
                log.info("No country selector popup found or already dismissed.");
            }

            // Scroll to certifications section
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            WebElement certSection = new WebDriverWait(webDriver, Duration.ofSeconds(20))
                    .until(driver -> driver.findElement(
                            By.xpath("//h2[contains(text(), 'Browse Oracle Certifications')]")));
            js.executeScript("arguments[0].scrollIntoView(true);", certSection);
            Thread.sleep(1000);
            log.info("Scrolled to 'Browse Oracle Certifications' section.");

            // Find all accordion sections
            List<WebElement> accordionSections = webDriver.findElements(
                    By.cssSelector("ul.rc33w1 > li.rc33w2"));
            log.info("Found {} accordion sections to process.", accordionSections.size());

            // Process each accordion section
            for (int i = 0; i < accordionSections.size(); i++) {
                if (currentCount.get() >= limit)
                    break;
                try {
                    // Re-query the accordion sections to avoid stale element references
                    List<WebElement> sections = webDriver.findElements(
                            By.cssSelector("ul.rc33w1 > li.rc33w2"));
                    WebElement section = sections.get(i);

                    // Get the category name from the header
                    WebElement headerButton = section.findElement(By.cssSelector("a[role='button']"));
                    String category = headerButton.findElement(By.tagName("h6")).getText();
                    log.info("Processing category {}/{}: {}", i + 1, sections.size(), category);

                    // Scroll to section and click to expand
                    js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                            headerButton);
                    Thread.sleep(500);

                    // Use JavaScript click for more reliability
                    js.executeScript("arguments[0].click();", headerButton);
                    Thread.sleep(1500); // Wait for accordion animation and content to appear

                    // Find the visible active container that appears after clicking
                    // The visible content is in div.rc33active > div.rc33w3.open
                    List<WebElement> certificateLinks = webDriver.findElements(
                            By.cssSelector("div.rc33active div.rc33w3.open a.cta-lnk"));

                    // If not found, try alternative selector for visible content
                    if (certificateLinks.isEmpty()) {
                        certificateLinks = webDriver.findElements(
                                By.cssSelector("div.rc33w3[aria-hidden='false'] a.cta-lnk"));
                    }

                    log.info("Found {} certificates in category '{}'", certificateLinks.size(), category);

                    // Extract certificate data
                    for (WebElement link : certificateLinks) {
                        if (currentCount.get() >= limit)
                            break;
                        try {
                            // Get title - try multiple methods to ensure we get the text
                            String title = link.getText().trim();

                            // If getText() returns empty, try getting the text content via JavaScript
                            if (title.isEmpty()) {
                                title = (String) js.executeScript("return arguments[0].textContent;", link);
                                title = title != null ? title.trim() : "";
                                log.debug("Used JavaScript to extract title: {}", title);
                            }

                            String url = link.getAttribute("href");
                            String description = "Oracle Certification: " + title;

                            // Apply keyword filter
                            if (keyword != null && !keyword.isEmpty()) {
                                if (!title.toLowerCase().contains(keyword.toLowerCase()) &&
                                        !category.toLowerCase().contains(keyword.toLowerCase())) {
                                    continue;
                                }
                            }

                            // Skip if already processed
                            if (processedUrls.contains(url)) {
                                log.debug("Skipping duplicate: {}", title);
                                continue;
                            }
                            processedUrls.add(url);

                            // Extract exam code from URL (e.g., "1z0-1085-25")
                            String examCode = extractExamCode(url);

                            // Determine certification level from title
                            String level = extractLevel(title);

                            // Create Event object
                            Event event = new Event();
                            event.setTitle(title);
                            event.setDescription(description);
                            event.setSourceUrl(url);
                            event.setType(EventType.CERTIFICATION);
                            event.setProvider("Oracle");
                            event.setCategory(category);
                            event.setExamCode(examCode);
                            event.setLevel(level);
                            event.setLocation("Online");
                            event.setPrice(0.0); // Price info not available on this page
                            event.setEventDate((java.time.LocalDate) null); // Explicit cast to avoid ambiguity

                            // Save to database
                            try {
                                Event savedEvent = eventService.saveEvent(event);
                                onEvent.accept(savedEvent); // Emit event with ID
                                currentCount.incrementAndGet();
                                log.info("Saved certificate: {} ({}) (ID: {})", title, examCode, savedEvent.getId());
                            } catch (Exception e) {
                                log.warn("Failed to save certificate '{}': {}", title, e.getMessage());
                            }

                        } catch (Exception e) {
                            log.error("Error processing certificate link", e);
                        }
                    }

                    // Collapse the section (optional, but keeps page clean)
                    js.executeScript("arguments[0].click();", headerButton);
                    Thread.sleep(500);

                } catch (Exception e) {
                    log.error("Error processing accordion section {}", i, e);
                }
            }

            log.info("Certification scraping completed.");

        } catch (Exception e) {
            log.error("Fatal error in scrapeCertifications", e);
        }
    }

    /**
     * Extract exam code from Oracle URL
     * Example: "oracle-cloud-infrastructure-2025-foundations-associate-1z0-1085-25"
     * -> "1Z0-1085-25"
     */
    private String extractExamCode(String url) {
        try {
            // Look for pattern like "1z0-XXX-YY" in the URL
            String[] parts = url.split("/");
            for (String part : parts) {
                if (part.matches(".*1z0-\\d+-\\d+.*")) {
                    // Extract just the exam code part
                    String[] subParts = part.split("-");
                    for (int i = 0; i < subParts.length - 2; i++) {
                        if (subParts[i].equalsIgnoreCase("1z0")) {
                            return (subParts[i] + "-" + subParts[i + 1] + "-" + subParts[i + 2]).toUpperCase();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract exam code from URL: {}", url);
        }
        return "Unknown";
    }

    /**
     * Extract certification level from title
     * Looks for keywords: Associate, Professional, Foundations, Essentials
     */
    private String extractLevel(String title) {
        String lowerTitle = title.toLowerCase();

        if (lowerTitle.contains("professional")) {
            return "Professional";
        } else if (lowerTitle.contains("associate")) {
            return "Associate";
        } else if (lowerTitle.contains("foundations")) {
            return "Foundations";
        } else if (lowerTitle.contains("essentials")) {
            return "Essentials";
        } else if (lowerTitle.contains("specialist") || lowerTitle.contains("specialty")) {
            return "Specialist";
        }

        return "Other";
    }

    private void scrapeHackathons(WebDriver webDriver, Consumer<Event> onEvent, int limit,
            java.util.concurrent.atomic.AtomicInteger currentCount, String keyword) {
        if (currentCount.get() >= limit)
            return;
        try {
            webDriver.get("https://www.oracle.com/database/hackathons/");
            new WebDriverWait(webDriver, Duration.ofSeconds(10))
                    .until(driver -> driver.findElement(By.tagName("h2")));

            WebElement titleElement = webDriver.findElement(By.tagName("h2"));
            WebElement descriptionElement = titleElement.findElement(By.xpath("./following-sibling::p"));
            String title = titleElement.getText();
            String description = descriptionElement.getText();

            // Apply keyword filter
            if (keyword != null && !keyword.isEmpty()) {
                if (!title.toLowerCase().contains(keyword.toLowerCase()) &&
                        !description.toLowerCase().contains(keyword.toLowerCase())) {
                    return;
                }
            }

            Event event = new Event();
            event.setTitle(title);
            event.setDescription(description);
            event.setSourceUrl("https://www.oracle.com/database/hackathons/");
            event.setEventDate((java.time.LocalDate) null);
            event.setLocation("Online");
            event.setType(EventType.HACKATHON);
            event.setProvider("Oracle");
            event.setPrice(0.0);
            eventService.saveEvent(event);
            onEvent.accept(event); // Emit event
            currentCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error scraping Oracle hackathons", e);
        }
    }
}




