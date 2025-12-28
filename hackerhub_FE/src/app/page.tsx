"use client";

import React, { useState, useTransition, useEffect } from 'react';
import { Hero } from '@/components/hero';
import { SearchForm } from '@/components/search-form';
import { scrapeHackathons } from '@/app/actions';
import { EventGrid } from '@/components/event-grid';
import { LoadingSkeletons } from '@/components/loading-skeletons';
import { type Event, type SearchFormValues } from '@/lib/types';
import { useToast } from '@/hooks/use-toast';
import { Code, ServerCrash } from 'lucide-react';
import { DUMMY_HACKATHONS } from '@/lib/dummy-data';

export default function Home() {
  const { toast } = useToast();
  const [isPending, setIsPending] = useState(false);
  const [events, setEvents] = useState<Event[]>(DUMMY_HACKATHONS);
  const [error, setError] = useState<string | null>(null);
  const [hasSearched, setHasSearched] = useState(false);

  // Load events from localStorage on mount
  useEffect(() => {
    const saved = localStorage.getItem('hackhub-events');
    if (saved) {
      try {
        setEvents(JSON.parse(saved));
      } catch (e) {
        // ignore
      }
    }
  }, []);

  const handleScrape = async (data: SearchFormValues) => {
    setError(null);
    setEvents([]);
    setHasSearched(true);
    setIsPending(true);

    if (data.scrapeType === 'certificates') {
      // ... existing certificates logic or dummy
      setIsPending(false);
      return;
    }

    try {
      await startStreaming(data);
    } catch (e) {
      setError("Failed to start stream");
      setIsPending(false);
    }
  };

  const startStreaming = (data: SearchFormValues) => {
    return new Promise<void>((resolve) => {
      let activeStreams = 0;
      const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

      const connectStream = (provider: 'mlh' | 'devpost') => {
        activeStreams++;
        const params = new URLSearchParams({
          domain: data.domain || '',
          location: data.location || '',
          count: String(data.count)
        });

        const eventSource = new EventSource(`${API_BASE_URL}/api/scraper/stream/${provider}?${params.toString()}`);

        eventSource.onmessage = (e) => {
          try {
            const newEvent = JSON.parse(e.data);
            setEvents(prev => {
              const updated = [...prev, newEvent].sort((a, b) =>
                new Date(b.scrappedAt).getTime() - new Date(a.scrappedAt).getTime()
              );
              localStorage.setItem('hackhub-events', JSON.stringify(updated));
              return updated;
            });
          } catch (err) {
            console.error("Error parsing event", err);
          }
        };

        eventSource.onerror = (e) => {
          console.log(`Stream ${provider} closed or error`);
          eventSource.close();
          activeStreams--;
          if (activeStreams === 0) {
            setIsPending(false);
            resolve();
          }
        };
      };

      connectStream('mlh');
      connectStream('devpost');
    });
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <Hero />

      <section id="explore" className="py-16 md:py-24">
        <div className="max-w-3xl mx-auto text-center mb-12">
          <h2 className="font-headline text-4xl md:text-5xl font-bold tracking-tight">Find Your Next Challenge</h2>
          <p className="mt-4 text-lg text-muted-foreground">
            Filter by platform, technology, and location to discover your perfect hackathon or certificate.
          </p>
        </div>
        <SearchForm onScrape={handleScrape} isLoading={isPending} />
      </section>

      <section className="min-h-[500px]">
        {/* Show skeletons only if we have NO events yet and are pending */}
        {isPending && events.length === 0 && <LoadingSkeletons />}

        {!isPending && error && (
          <div className="flex flex-col items-center justify-center text-center py-20 bg-card rounded-lg">
            <ServerCrash className="w-16 h-16 text-destructive mb-4" />
            <h3 className="font-headline text-2xl font-bold">An Error Occurred</h3>
            <p className="text-muted-foreground mt-2 max-w-md">{error}</p>
          </div>
        )}

        {/* Show grid if we have ANY events (even if still pending/streaming) */}
        {events.length > 0 && (
          <EventGrid events={events} />
        )}

        {!isPending && !error && events.length === 0 && hasSearched && (
          <div className="flex flex-col items-center justify-center text-center py-20 bg-card/50 rounded-lg border-2 border-dashed">
            <Code className="w-16 h-16 text-primary mb-4" />
            <h3 className="font-headline text-2xl font-bold">No Results Found</h3>
            <p className="text-muted-foreground mt-2 max-w-md">Try adjusting your search filters.</p>
          </div>
        )}
      </section>
    </div>
  );
}
