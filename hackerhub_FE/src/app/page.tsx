"use client";

import React, { useState, useTransition, useEffect } from 'react';
import { Hero } from '@/components/hero';
import SearchForm from '@/components/search-form';
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
  const [activeTab, setActiveTab] = useState<'all' | 'hackathons' | 'certificates' | 'courses'>('all');

  // ... (useEffect remains same)

  const handleScrape = async (data: SearchFormValues) => {
    // ... (same as before)
    setError(null);
    setEvents([]);
    setHasSearched(true);
    setIsPending(true);

    try {
      await startStreaming(data);
      // Auto-switch tab based on scrape type
      if (data.scrapeType === 'hackathons') setActiveTab('hackathons');
      else if (data.scrapeType === 'certificates') setActiveTab('certificates');
      else if (data.scrapeType === 'courses') setActiveTab('courses');
    } catch (e) {
      setError("Failed to start stream");
      setIsPending(false);
    }
  };

  const startStreaming = (data: SearchFormValues) => {
    return new Promise<void>((resolve) => {
      let activeStreams = 0;
      const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

      const connectStream = (provider: string) => {
        activeStreams++;
        const params = new URLSearchParams({
          domain: data.domain || '',
          location: data.location || '',
          count: String(data.count),
          scrapeType: data.scrapeType || 'hackathons'
        });

        const eventSource = new EventSource(`${API_BASE_URL}/api/scraper/stream/${provider}?${params.toString()}`);

        eventSource.onmessage = (e) => {
          try {
            const newEvent: Event = JSON.parse(e.data);
            console.log('[Stream] Received event:', newEvent.title, 'Type:', newEvent.type);

            // Filter events based on scrapeType - case insensitive and handle null
            const eventType = (newEvent.type || '').toUpperCase();
            if (data.scrapeType === 'hackathons') {
              if (eventType !== 'HACKATHON' && eventType !== '') {
                console.log('[Stream] Filtered out (not hackathon):', newEvent.title, eventType);
                return;
              }
            } else if (data.scrapeType === 'certificates') {
              if (eventType !== 'CERTIFICATION' && eventType !== '') return;
            } else if (data.scrapeType === 'courses') {
              if (eventType !== 'COURSE' && eventType !== '') return;
            }

            setEvents(prev => {
              // Avoid duplicates
              // If ID is present, check by ID. If ID is null (streamed events), check by Title + Provider
              const isDuplicate = prev.some(ev => {
                if (newEvent.id && ev.id === newEvent.id) return true;
                if (!newEvent.id && ev.title === newEvent.title && ev.provider === newEvent.provider) return true;
                return false;
              });

              if (isDuplicate) return prev;

              // Client-side filtering for domain/location
              if (data.domain && !newEvent.title.toLowerCase().includes(data.domain.toLowerCase()) &&
                !newEvent.description?.toLowerCase().includes(data.domain.toLowerCase())) {
                return prev;
              }

              if (data.location) {
                const filterLoc = data.location.toLowerCase();
                const eventLoc = (newEvent.location || '').toLowerCase();
                const isRemote = eventLoc.includes('online') || eventLoc.includes('remote') || eventLoc.includes('worldwide');
                const filterRemote = filterLoc.includes('online') || filterLoc.includes('remote');

                if (filterRemote) {
                  if (!isRemote) return prev;
                } else {
                  if (!eventLoc.includes(filterLoc) && !isRemote) return prev;
                }
              }

              const updated = [...prev, newEvent].sort((a, b) => {
                const dateA = a.date ? new Date(a.date).getTime() : 0;
                const dateB = b.date ? new Date(b.date).getTime() : 0;
                if (dateA !== dateB) return dateB - dateA;
                return new Date(b.scrappedAt).getTime() - new Date(a.scrappedAt).getTime();
              });
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
            setEvents(prev => {
              toast({
                title: "Scraping Completed",
                description: `Successfully found ${prev.length} items.`,
                variant: "default",
              });
              return prev;
            });
            resolve();
          }
        };
      };

      const providers = [];
      const selectedProvider = data.provider || 'all';

      if (data.scrapeType === 'hackathons') {
        if (selectedProvider === 'all') {
          providers.push('mlh', 'devpost', 'oracle', 'ibm', 'microsoft');
        } else {
          providers.push(selectedProvider);
        }
      } else if (data.scrapeType === 'certificates') {
        if (selectedProvider === 'all') {
          providers.push('oracle', 'ibm', 'microsoft');
        } else {
          providers.push(selectedProvider);
        }
      } else {
        // Courses - IBM supports explicit courses, others might be mixed or not supported yet
        if (selectedProvider === 'all') {
          providers.push('ibm', 'oracle', 'microsoft');
        } else {
          providers.push(selectedProvider);
        }
      }

      providers.forEach(p => connectStream(p));
    });
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <Hero />

      <section id="explore" className="py-16 md:py-24">
        <div className="max-w-3xl mx-auto text-center mb-12">
          <h2 className="font-headline text-4xl md:text-5xl font-bold tracking-tight">Find Your Next Challenge</h2>
          <p className="mt-4 text-lg text-muted-foreground">
            Explore hackathons, certifications, and courses from <span className="font-semibold text-primary">MLH, DevPost, Oracle, IBM, and Microsoft</span>.
          </p>
        </div>
        <SearchForm onSubmit={handleScrape} isLoading={isPending} />

        {isPending && (
          <div className="mt-4 text-center animate-pulse">
            <p className="text-primary font-medium">
              Searching... {events.length} items scraped so far
            </p>
          </div>
        )}
      </section>

      {/* Filter Tabs */}
      {events.length > 0 && (
        <div className="flex justify-center flex-wrap gap-2 mb-8">
          <button
            onClick={() => setActiveTab('all')}
            className={`px-6 py-2 rounded-full font-medium transition-all ${activeTab === 'all'
              ? 'bg-primary text-primary-foreground shadow-lg'
              : 'bg-card hover:bg-card/80 text-muted-foreground hover:text-foreground'
              }`}
          >
            All ({events.length})
          </button>
          <button
            onClick={() => setActiveTab('hackathons')}
            className={`px-6 py-2 rounded-full font-medium transition-all ${activeTab === 'hackathons'
              ? 'bg-primary text-primary-foreground shadow-lg'
              : 'bg-card hover:bg-card/80 text-muted-foreground hover:text-foreground'
              }`}
          >
            Hackathons ({events.filter(e => e.type === 'HACKATHON').length})
          </button>
          <button
            onClick={() => setActiveTab('certificates')}
            className={`px-6 py-2 rounded-full font-medium transition-all ${activeTab === 'certificates'
              ? 'bg-primary text-primary-foreground shadow-lg'
              : 'bg-card hover:bg-card/80 text-muted-foreground hover:text-foreground'
              }`}
          >
            Certificates ({events.filter(e => e.type === 'CERTIFICATION').length})
          </button>
          <button
            onClick={() => setActiveTab('courses')}
            className={`px-6 py-2 rounded-full font-medium transition-all ${activeTab === 'courses'
              ? 'bg-primary text-primary-foreground shadow-lg'
              : 'bg-card hover:bg-card/80 text-muted-foreground hover:text-foreground'
              }`}
          >
            Courses ({events.filter(e => e.type === 'COURSE').length})
          </button>
        </div>
      )}

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
          <EventGrid events={
            activeTab === 'all'
              ? events
              : activeTab === 'hackathons'
                ? events.filter(e => e.type === 'HACKATHON')
                : activeTab === 'certificates'
                  ? events.filter(e => e.type === 'CERTIFICATION')
                  : events.filter(e => e.type === 'COURSE')
          } />
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
