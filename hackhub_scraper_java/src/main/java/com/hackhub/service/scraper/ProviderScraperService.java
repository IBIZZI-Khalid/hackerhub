package com.hackhub.service.scraper;

import com.hackhub.model.Event;
import java.util.List;

import com.hackhub.model.dto.ScrapeRequest;

public interface ProviderScraperService {
    List<Event> scrape();

    void scrapeStream(ScrapeRequest request, java.util.function.Consumer<Event> onEvent);
}




