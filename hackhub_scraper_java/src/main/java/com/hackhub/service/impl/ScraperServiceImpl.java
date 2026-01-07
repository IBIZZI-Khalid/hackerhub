package com.hackhub.service.impl;

import com.hackhub.model.Event;
import com.hackhub.model.EventType;
import com.hackhub.service.ScraperService;

import org.springframework.stereotype.Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperServiceImpl implements ScraperService {

    private static final String DEVPOST_URL = "https://devpost.com/hackathons";
    private static final String MLH_URL = "https://mlh.io/seasons/2026/events";

    private WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-extensions");
        options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36");

        // Use system Chrome/Chromium if available (for Docker), otherwise use
        // WebDriverManager
        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isEmpty()) {
            System.out.println("Using system Chrome/Chromium at: " + chromeBin);
            options.setBinary(chromeBin);
            System.setProperty("webdriver.chrome.driver", System.getenv("CHROMEDRIVER_BIN"));
        } else {
            System.out.println("Using WebDriverManager to setup ChromeDriver");
            WebDriverManager.chromedriver().setup();
        }

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        return driver;
    }

    @Override
    public List<Event> scrapeDevpost(String domain, String location, int count) {
        List<Event> events = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            System.out.println("\n========================================");
            System.out.println("🔍 [DEVPOST SCRAPER] Starting scrape request");
            System.out.println("   Domain: " + (domain == null || domain.isBlank() ? "ALL" : domain));
            System.out.println("   Location: " + (location == null || location.isBlank() ? "ALL" : location));
            System.out.println("   Count: " + count);
            System.out.println("========================================\n");

            int page = 1;
            while (events.size() < count) {
                String searchUrl = DEVPOST_URL + "?search=" + (domain != null ? domain : "") + "&page=" + page;
                System.out.println("🌐 Navigating to page " + page + ": " + searchUrl);
                driver.get(searchUrl);
                Thread.sleep(3000);
                System.out.println("✅ Page " + page + " loaded");

                List<WebElement> tiles = driver.findElements(By.cssSelector(".challenge-listing, .hackathon-tile"));
                System.out.println("📋 Found " + tiles.size() + " hackathon tiles on page " + page);

                if (tiles.isEmpty()) {
                    System.out.println("⚠️  No more tiles found, stopping pagination");
                    break;
                }

                // 1. First Pass: Collect basic info from all tiles on the page
                // We do NOT deep scrape here to avoid StaleElementReferenceExceptions
                List<Event> pageEvents = new ArrayList<>();
                for (WebElement tile : tiles) {
                    Event event = parseDevpostEvent(tile, driver);
                    if (event != null) {
                        // Apply location filter early if possible
                        if (location != null && !location.isBlank()) {
                            if (event.getLocation() == null
                                    || !event.getLocation().toLowerCase().contains(location.toLowerCase())) {
                                continue;
                            }
                        }
                        pageEvents.add(event);
                    }
                }

                System.out.println("   [First Pass] Extracted " + pageEvents.size() + " events from page " + page);

                // 2. Second Pass: Deep Scrape details (now safe to navigate)
                for (Event event : pageEvents) {
                    if (events.size() >= count)
                        break;

                    fetchDevpostDetails(event, driver); // Visits URL, fills description, prizes, etc.
                    events.add(event);
                    System.out.println("   [Pagination] Collected " + events.size() + "/" + count + " events");
                }

                page++;
            }

            System.out.println("\n========================================");
            System.out.println("✅ [DEVPOST SCRAPER] Completed successfully!");
            System.out.println("   Total events returned: " + events.size());
            System.out.println("========================================\n");
        } catch (Exception e) {
            System.err.println("\n❌ [DEVPOST SCRAPER] Error occurred:");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return events;
    }

    @Override
    public List<Event> scrapeMlh(String domain, String location, int count) {
        List<Event> events = new ArrayList<>();
        WebDriver driver = createDriver();

        try {
            System.out.println("\n========================================");
            System.out.println("🔍 [MLH SCRAPER] Starting scrape request");
            System.out.println("   Domain: " + (domain == null || domain.isBlank() ? "ALL" : domain));
            System.out.println("   Location: " + (location == null || location.isBlank() ? "ALL" : location));
            System.out.println("   Count: " + count);
            System.out.println("========================================\n");

            System.out.println("🌐 Navigating to: " + MLH_URL);
            try {
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30)); // Increase timeout
                driver.get(MLH_URL);
            } catch (TimeoutException e) {
                System.out.println("⚠️ Navigation timed out but page might be loaded. Continuing...");
            }
            Thread.sleep(3000);
            System.out.println("✅ Page loaded sequence finished");

            // 1. Get initial list of cards and extract ALL data immediately
            List<WebElement> cards = driver.findElements(By.cssSelector(".event-wrapper"));
            System.out.println("📋 Found " + cards.size() + " total event cards on page");

            // 2. Extract basic info from all cards IMMEDIATELY (before elements go stale)
            List<Event> basicEvents = new ArrayList<>();

            for (int i = 0; i < cards.size() && basicEvents.size() < count; i++) {
                try {
                    WebElement card = cards.get(i);

                    // Extract ALL data from this element NOW before it goes stale
                    String title = "";
                    String url = "";
                    String eventLoc = "";
                    String eventDate = "";
                    String imageUrl = "";

                    try {
                        title = card.findElement(By.cssSelector(".event-name")).getText().trim();
                    } catch (Exception e) {
                        System.err.println("[MLH] Could not get event name: " + e.getMessage());
                        continue; // Skip this card if we can't get the title
                    }

                    try {
                        url = card.findElement(By.cssSelector("a.event-link")).getAttribute("href");
                    } catch (Exception e) {
                        System.err.println("[MLH] Could not get URL for: " + title);
                    }

                    try {
                        eventLoc = card.findElement(By.cssSelector(".event-location")).getText().trim();
                    } catch (Exception ignored) {
                        // Location is optional
                    }

                    try {
                        eventDate = card.findElement(By.cssSelector(".event-date")).getText().trim();
                    } catch (Exception ignored) {
                        // Date is optional
                    }

                    try {
                        imageUrl = card.findElement(By.cssSelector(".image-wrap img")).getAttribute("src");
                    } catch (Exception ignored) {
                        // Image is optional
                    }

                    // Apply filters AFTER extracting data
                    if (domain != null && !domain.isBlank() && !title.toLowerCase().contains(domain.toLowerCase())) {
                        continue;
                    }

                    // Smart location filtering - treat Remote, Online, Worldwide, Everywhere as
                    // equivalent
                    if (location != null && !location.isBlank()) {
                        String filterLoc = location.toLowerCase();
                        String eventLocation = eventLoc.toLowerCase();

                        // Check if filter is for remote events
                        boolean filterIsRemote = filterLoc.contains("remote") || filterLoc.contains("online") ||
                                filterLoc.contains("worldwide") || filterLoc.contains("everywhere");

                        // Check if event is remote
                        boolean eventIsRemote = eventLocation.contains("online") || eventLocation.contains("worldwide")
                                ||
                                eventLocation.contains("everywhere") || eventLocation.contains("remote");

                        // If filtering for remote and event is remote, include it
                        // Otherwise check if location matches normally
                        if (!filterIsRemote || !eventIsRemote) {
                            if (!eventLocation.contains(filterLoc)) {
                                continue;
                            }
                        }
                    }

                    // Create event object with extracted data
                    Event event = new Event();
                    // Generate unique ID from title and URL hash
                    // event.setId() removed - JPA auto-generates ID
                    event.setTitle(title);
                    event.setSourceUrl(url);
                    event.setLocation(eventLoc);
                    // event.setParsedDate(eventDate); // TODO: parse string to LocalDate
                    event.setImageUrl(imageUrl);
                    event.setProvider("MLH");
                    event.setType(EventType.HACKATHON);

                    basicEvents.add(event);
                    System.out.println("[MLH] Extracted: " + title);

                } catch (Exception e) {
                    System.err.println("[MLH] Error parsing card " + i + ": " + e.getMessage());
                }
            }

            System.out.println("[MLH] Successfully extracted " + basicEvents.size() + " events");

            // 3. Deep Scraping (Visit each URL) - optional, skip for now to speed up
            for (Event event : basicEvents) {
                if (events.size() >= count)
                    break;

                // Skip deep scraping for now to avoid timeouts
                // System.out.println("Deep scraping: " + event.getSourceUrl());
                // fetchExternalDetails(event, driver);
                events.add(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return events;
    }

    /**
     * Tiered Extraction Strategy for external sites.
     */

    private void fetchDevpostDetails(Event event, WebDriver driver) {
        if (event.getSourceUrl() == null)
            return;

        try {
            System.out.println("   [Devpost Deep Scrape] Visiting: " + event.getSourceUrl());
            driver.get(event.getSourceUrl());
            Thread.sleep(2000);

            try {
                // Description
                try {
                    WebElement descEl = driver.findElement(By.cssSelector(
                            "#challenge-description, .challenge-description, #challenge-overview, .content-section"));
                    event.setDescription(descEl.getAttribute("innerHTML"));
                } catch (Exception ignored) {
                }

                // Prizes
                try {
                    WebElement prizesEl = driver.findElement(By.cssSelector("#prizes, .prizes"));
                    event.setRequirements(prizesEl.getAttribute("innerHTML")); // Storing prizes in requirements
                } catch (Exception ignored) {
                }

                // Judging Criteria
                try {
                    WebElement criteriaEl = driver.findElement(By.cssSelector("#judging-criteria, .judging-criteria"));
                    event.setJudgingCriteria(criteriaEl.getAttribute("innerHTML"));
                } catch (Exception ignored) {
                }

                System.out.println("   [Devpost Deep Scrape] Data extracted for " + event.getTitle());

            } catch (Exception e) {
                System.out.println("   [Devpost Deep Scrape] Failed to extract details: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("   [Devpost Deep Scrape] Navigation failed: " + e.getMessage());
        }
    }

    private Event parseDevpostEvent(WebElement tile, WebDriver driver) {
        try {
            Event event = new Event();

            // Extract title with retry for StaleElement
            int retries = 0;
            while (retries < 3) {
                try {
                    event.setTitle(tile.findElement(By.cssSelector(".title, h3, .challenge-name")).getText().trim());
                    break;
                } catch (Exception e) {
                    if (e instanceof StaleElementReferenceException || e.getMessage().contains("stale element")) {
                        retries++;
                        if (retries == 3) {
                            System.err.println("⚠️  [DEVPOST] Stale element for title after 3 retries");
                            event.setTitle("Unknown");
                        }
                    } else {
                        System.err.println("⚠️  [DEVPOST] Could not extract title: " + e.getMessage());
                        event.setTitle("Unknown");
                        break;
                    }
                }
            }

            // Extract URL with retry
            retries = 0;
            while (retries < 3) {
                try {
                    event.setSourceUrl(tile.findElement(By.cssSelector("a")).getAttribute("href"));
                    break;
                } catch (Exception e) {
                    if (e instanceof StaleElementReferenceException || e.getMessage().contains("stale element")) {
                        retries++;
                    } else {
                        System.err.println("⚠️  [DEVPOST] Could not extract URL");
                        break;
                    }
                }
            }

            // Extract location
            try {
                event.setLocation(
                        tile.findElement(By.cssSelector(".location, .info, .challenge-location")).getText().trim());
            } catch (Exception e) {
                // Location is optional
            }

            // Extract date
            try {
                // event.setParsedDate() removed - expects LocalDate not String
                // event.setParsedDate(tile.findElement(By.cssSelector(".date, .challenge-date,
                // time")).getText().trim());
            } catch (Exception e) {
                // Date is optional
            }

            // Extract image
            try {
                WebElement img = tile.findElement(By.cssSelector("img"));
                String imgSrc = img.getAttribute("src");
                if (imgSrc != null && !imgSrc.isEmpty()) {
                    event.setImageUrl(imgSrc);
                }
            } catch (Exception e) {
                System.err.println("⚠️  [DEVPOST] Could not extract image for: " + event.getTitle());
            }

            // Deep scrape logic moved to fetchDevpostDetails to avoid
            // StaleElementReferenceException
            // if (event.getSourceUrl() != null) { ... }

            // Generate unique ID from title and URL hash
            // event.setId() removed - JPA auto-generates ID
            event.setProvider("DEVPOST");
            event.setType(EventType.HACKATHON);
            return event;
        } catch (Exception e) {
            System.err.println("❌ [DEVPOST] Failed to parse event tile: " + e.getMessage());
            return null;
        }

    }

    @Override
    public void streamDevpost(String domain, String location, int count, java.util.function.Consumer<Event> onEvent,
            Runnable onComplete) {
        WebDriver driver = createDriver();

        try {
            System.out.println("🔍 [DEVPOST SCRAPER STREAM] Starting");
            int page = 1;
            int totalEmitted = 0;

            while (totalEmitted < count) {
                String searchUrl = DEVPOST_URL + "?search=" + (domain != null ? domain : "") + "&page=" + page;
                driver.get(searchUrl);
                Thread.sleep(3000);

                List<WebElement> tiles = driver.findElements(By.cssSelector(".challenge-listing, .hackathon-tile"));
                if (tiles.isEmpty())
                    break;

                // 1. First Pass: Collect basic info
                List<Event> pageEvents = new ArrayList<>();
                for (WebElement tile : tiles) {
                    Event event = parseDevpostEvent(tile, driver);
                    if (event != null) {
                        if (location != null && !location.isBlank()) {
                            if (event.getLocation() == null
                                    || !event.getLocation().toLowerCase().contains(location.toLowerCase())) {
                                continue;
                            }
                        }
                        pageEvents.add(event);
                    }
                }

                // 2. Second Pass: Deep Scrape & Emit
                for (Event event : pageEvents) {
                    if (totalEmitted >= count)
                        break;
                    fetchDevpostDetails(event, driver);
                    onEvent.accept(event); // Emit immediately
                    totalEmitted++;
                }

                page++;
            }
        } catch (Exception e) {
            System.err.println("Error streaming Devpost: " + e.getMessage());
        } finally {
            driver.quit();
            onComplete.run();
        }
    }

    @Override
    public void streamMlh(String domain, String location, int count, java.util.function.Consumer<Event> onEvent,
            Runnable onComplete) {
        WebDriver driver = createDriver();

        try {
            System.out.println("🔍 [MLH SCRAPER STREAM] Starting");

            // Increase timeout and add retry logic
            try {
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));
                driver.get(MLH_URL);
                System.out.println("✅ [MLH] Page loaded successfully");
            } catch (TimeoutException e) {
                System.out.println("⚠️ [MLH] Page load timed out, retrying...");
                driver.quit();
                driver = createDriver();
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));
                driver.get(MLH_URL);
                System.out.println("✅ [MLH] Page loaded on retry");
            }

            Thread.sleep(3000);

            List<WebElement> cards = driver.findElements(By.cssSelector(".event-wrapper"));
            int emitted = 0;

            for (WebElement card : cards) {
                if (emitted >= count)
                    break;

                try {
                    // Extract data (Simplified version of scrapeMlh internal logic)
                    String title = card.findElement(By.cssSelector(".event-name")).getText().trim();

                    // Filter Domain
                    if (domain != null && !domain.isBlank() && !title.toLowerCase().contains(domain.toLowerCase()))
                        continue;

                    // Extract other fields
                    Event event = new Event();
                    event.setTitle(title);
                    event.setSourceUrl(card.findElement(By.cssSelector("a.event-link")).getAttribute("href"));
                    try {
                        event.setLocation(card.findElement(By.cssSelector(".event-location")).getText().trim());
                    } catch (Exception e) {
                    }
                    try {
                        // event.setParsedDate() removed - expects LocalDate not String
                        // event.setParsedDate(card.findElement(By.cssSelector(".event-date")).getText().trim());
                    } catch (Exception e) {
                    }
                    try {
                        event.setImageUrl(card.findElement(By.cssSelector(".image-wrap img")).getAttribute("src"));
                    } catch (Exception e) {
                    }

                    // event.setId() removed - JPA auto-generates ID
                    event.setProvider("MLH");
                    event.setType(EventType.HACKATHON);

                    // Filter Location
                    if (location != null && !location.isBlank()) {
                        String lowerLoc = event.getLocation().toLowerCase();
                        if (!lowerLoc.contains(location.toLowerCase()) && !lowerLoc.contains("online")
                                && !lowerLoc.contains("remote")) {
                            continue;
                        }
                    }

                    // Optional: Deep scrape here if needed (fetchExternalDetails)
                    // fetchExternalDetails(event, driver);

                    onEvent.accept(event);
                    emitted++;

                } catch (Exception e) {
                    System.err.println("Error parsing MLH card: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error streaming MLH: " + e.getMessage());
        } finally {
            driver.quit();
            onComplete.run();
        }
    }
}
