package com.hackhub.controller;

import com.hackhub.model.Event;
import com.hackhub.model.dto.ScrapeRequest;
import com.hackhub.service.ScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scraper")
public class ScraperController {

    private final ScraperService scraperService;
    private final com.hackhub.service.scraper.OracleScraperService oracleScraperService;
    private final com.hackhub.service.scraper.IBMScraperService ibmScraperService;
    private final com.hackhub.service.scraper.MicrosoftScraperService microsoftScraperService;
    private final com.hackhub.service.EventService eventService;

    @Autowired
    public ScraperController(ScraperService scraperService,
            com.hackhub.service.scraper.OracleScraperService oracleScraperService,
            com.hackhub.service.scraper.IBMScraperService ibmScraperService,
            com.hackhub.service.scraper.MicrosoftScraperService microsoftScraperService,
            com.hackhub.service.EventService eventService) {
        this.scraperService = scraperService;
        this.oracleScraperService = oracleScraperService;
        this.ibmScraperService = ibmScraperService;
        this.microsoftScraperService = microsoftScraperService;
        this.eventService = eventService;
    }

    @PostMapping("/devpost")
    public ResponseEntity<List<Event>> scrapeDevpost(@RequestBody ScrapeRequest request) {
        // Apply defaults for null/empty parameters (and use local vars for logging)
        String effectiveDomain = (request.getDomain() == null) ? "" : request.getDomain();
        String effectiveLocation = (request.getLocation() == null) ? "" : request.getLocation();
        int effectiveCount = (request.getCount() <= 0) ? 10 : request.getCount();
        if (effectiveCount > 50)
            effectiveCount = 50;

        // Update request object for consistency
        request.setDomain(effectiveDomain);
        request.setLocation(effectiveLocation);
        request.setCount(effectiveCount);

        System.out.println("📥 [DEVPOST] Received scrape request: Domain='" + effectiveDomain
                + "', Location='" + effectiveLocation + "', Count=" + effectiveCount);

        List<Event> events = scraperService.scrapeDevpost(
                effectiveDomain,
                effectiveLocation,
                effectiveCount);

        return ResponseEntity.ok(events);
    }

    @PostMapping("/mlh")
    public ResponseEntity<List<Event>> scrapeMlh(@RequestBody ScrapeRequest request) {
        // Apply defaults for null/empty parameters (and use local vars for logging)
        String effectiveDomain = (request.getDomain() == null) ? "" : request.getDomain();
        String effectiveLocation = (request.getLocation() == null) ? "" : request.getLocation();
        int effectiveCount = (request.getCount() <= 0) ? 10 : request.getCount();
        if (effectiveCount > 50)
            effectiveCount = 50;

        // Update request object for consistency
        request.setDomain(effectiveDomain);
        request.setLocation(effectiveLocation);
        request.setCount(effectiveCount);

        System.out.println("📥 [MLH] Received scrape request: Domain='" + effectiveDomain
                + "', Location='" + effectiveLocation + "', Count=" + effectiveCount);

        List<Event> events = scraperService.scrapeMlh(effectiveDomain, effectiveLocation, effectiveCount);

        return ResponseEntity.ok(events);
    }

    @PostMapping("/oracle")
    public ResponseEntity<List<Event>> scrapeOracle() {
        try {
            List<Event> events = oracleScraperService.scrape();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/ibm")
    public ResponseEntity<List<Event>> scrapeIBM() {
        try {
            List<Event> events = ibmScraperService.scrape();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/microsoft")
    public ResponseEntity<List<Event>> scrapeMicrosoft() {
        try {
            List<Event> events = microsoftScraperService.scrape();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @PostMapping("/all")
    public ResponseEntity<List<Event>> scrapeAll() {
        java.util.List<Event> allEvents = new java.util.ArrayList<>();
        try {
            allEvents.addAll(oracleScraperService.scrape());
            allEvents.addAll(ibmScraperService.scrape());
            allEvents.addAll(microsoftScraperService.scrape());
            return ResponseEntity.ok(allEvents);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(allEvents); // Return partial results
        }
    }

    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();
    // Limit concurrent Chrome sessions to 3 to prevent resource exhaustion
    private static final java.util.concurrent.Semaphore chromeSemaphore = new java.util.concurrent.Semaphore(3);

    @GetMapping("/stream/devpost")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamDevpost(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "10") int count) {

        System.out.println("\n🔍 [DEVPOST STREAM] Client connected - domain: " + domain + ", location: " + location
                + ", count: " + count);

        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                300_000L); // 5 min timeout
        final java.util.concurrent.atomic.AtomicBoolean streamEnded = new java.util.concurrent.atomic.AtomicBoolean(
                false);
        final java.util.concurrent.atomic.AtomicInteger eventCount = new java.util.concurrent.atomic.AtomicInteger(0);

        executor.execute(() -> {
            try {
                scraperService.streamDevpost(domain, location, count, event -> {
                    if (!streamEnded.get()) {
                        try {
                            emitter.send(event);
                            int sent = eventCount.incrementAndGet();
                            System.out.println("✅ [DEVPOST STREAM] Event #" + sent + " sent: " + event.getTitle());
                        } catch (Exception e) {
                            System.err.println("❌ [DEVPOST STREAM] Send failed: " + e.getMessage());
                            streamEnded.set(true);
                            try {
                                emitter.completeWithError(e);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }, () -> {
                    if (!streamEnded.get()) {
                        streamEnded.set(true);
                        System.out.println("✅ [DEVPOST STREAM] Completed - Total events sent: " + eventCount.get());
                        emitter.complete();
                    }
                });
            } catch (Exception e) {
                System.err.println("❌ [DEVPOST STREAM] Error: " + e.getMessage());
                if (!streamEnded.get()) {
                    streamEnded.set(true);
                    try {
                        emitter.completeWithError(e);
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        return emitter;
    }

    @GetMapping("/stream/mlh")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamMlh(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "10") int count) {

        System.out.println("\n🔍 [MLH STREAM] Client connected - domain: " + domain + ", location: " + location
                + ", count: " + count);

        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                300_000L); // 5 min timeout
        final java.util.concurrent.atomic.AtomicBoolean streamEnded = new java.util.concurrent.atomic.AtomicBoolean(
                false);
        final java.util.concurrent.atomic.AtomicInteger eventCount = new java.util.concurrent.atomic.AtomicInteger(0);

        executor.execute(() -> {
            try {
                scraperService.streamMlh(domain, location, count, event -> {
                    if (!streamEnded.get()) {
                        try {
                            emitter.send(event);
                            int sent = eventCount.incrementAndGet();
                            System.out.println("✅ [MLH STREAM] Event #" + sent + " sent: " + event.getTitle());
                        } catch (Exception e) {
                            System.err.println("❌ [MLH STREAM] Send failed: " + e.getMessage());
                            streamEnded.set(true);
                            try {
                                emitter.completeWithError(e);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }, () -> {
                    if (!streamEnded.get()) {
                        streamEnded.set(true);
                        System.out.println("✅ [MLH STREAM] Completed - Total events sent: " + eventCount.get());
                        emitter.complete();
                    }
                });
            } catch (Exception e) {
                System.err.println("❌ [MLH STREAM] Error: " + e.getMessage());
                if (!streamEnded.get()) {
                    streamEnded.set(true);
                    try {
                        emitter.completeWithError(e);
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        return emitter;
    }

    @GetMapping("/stream/oracle")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamOracle(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) String scrapeType,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String location) {

        System.out.println("\n🔍 [ORACLE STREAM] Client connected - Count: " + count + ", Type: " + scrapeType
                + ", Domain: " + domain + ", Location: " + location);

        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                1800000L);

        // Normalize scrapeType
        String normalizedType = (scrapeType != null && !scrapeType.isEmpty())
                ? scrapeType.toUpperCase()
                : null;

        System.out.println("   [ORACLE] Normalized scrapeType: " + normalizedType);

        executor.execute(() -> {
            try {
                ScrapeRequest req = new ScrapeRequest();
                req.setDomain(domain);
                req.setLocation(location);
                req.setCount(count);
                req.setScrapeType(normalizedType);

                oracleScraperService.scrapeStream(req, event -> {
                    try {
                        emitter.send(event);
                        System.out.println("✅ [ORACLE STREAM] Sent: " + event.getTitle());
                    } catch (Exception e) {
                        System.err.println("❌ [ORACLE STREAM] Send failed: " + e.getMessage());
                    }
                });

                emitter.complete();
                System.out.println("✅ [ORACLE STREAM] Completed");
            } catch (Exception e) {
                System.err.println("❌ [ORACLE STREAM] Error: " + e.getMessage());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/stream/ibm")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamIbm(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) String scrapeType,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String location) {
        return createProviderStream(ibmScraperService, "IBM", count, scrapeType, domain, location);
    }

    @GetMapping("/stream/microsoft")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamMicrosoft(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) String scrapeType,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String location) {
        return createProviderStream(microsoftScraperService, "MICROSOFT", count, scrapeType, domain, location);
    }

    private org.springframework.web.servlet.mvc.method.annotation.SseEmitter createProviderStream(
            com.hackhub.service.scraper.ProviderScraperService service, String providerName, int count,
            String scrapeType, String domain, String location) {

        System.out.println(
                "\n🔍 [" + providerName + " STREAM] Client connected - Count: " + count + ", Type: " + scrapeType
                        + ", Domain: " + domain + ", Location: " + location);

        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                1800000L); // 30 min timeout

        executor.execute(() -> {
            boolean acquired = false;
            try {
                // Normalize scrapeType to uppercase for consistent comparison
                String normalizedType = (scrapeType != null && !scrapeType.isEmpty())
                        ? scrapeType.toUpperCase()
                        : null;

                System.out.println("   [" + providerName + "] Normalized scrapeType: " + normalizedType);

                ScrapeRequest req = new ScrapeRequest();
                req.setCount(count);
                req.setScrapeType(normalizedType);
                req.setDomain(domain);
                req.setLocation(location);

                // Acquire Chrome session permit (max 3 concurrent)
                System.out.println("   [" + providerName + "] Waiting for Chrome session...");
                chromeSemaphore.acquire();
                acquired = true;
                System.out.println("   [" + providerName + "] Chrome session acquired!");

                service.scrapeStream(req, event -> {
                    try {
                        emitter.send(event);
                        System.out.println("✅ [" + providerName + " STREAM] Sent: " + event.getTitle());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
                System.out.println("✅ [" + providerName + " STREAM] Completed");
            } catch (Exception e) {
                System.err.println("❌ [" + providerName + " STREAM] Error: " + e.getMessage());
                emitter.completeWithError(e);
            } finally {
                if (acquired) {
                    chromeSemaphore.release();
                    System.out.println("   [" + providerName + "] Chrome session released");
                }
            }
        });
        return emitter;
    }
}




