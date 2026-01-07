"use client";

import { useMemo, useEffect, useState } from 'react';
import Image from 'next/image';
import DOMPurify from 'dompurify';
import { notFound } from 'next/navigation';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Calendar, ExternalLink, GanttChart, MapPin, Scale, Trophy, Award } from 'lucide-react';
import { type Event } from '@/lib/types';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';


function DetailSection({ icon, title, content }: { icon: React.ReactNode, title: string, content: string | null }) {
  if (!content) return null;

  // Sanitize content on the client-side
  const cleanContent = typeof window !== 'undefined' ? DOMPurify.sanitize(content) : content;

  return (
    <div className="mt-6">
      <h3 className="font-headline text-lg font-semibold flex items-center gap-2 mb-2">
        {icon}
        {title}
      </h3>
      <div
        className="prose prose-sm prose-invert max-w-none text-muted-foreground"
        dangerouslySetInnerHTML={{ __html: cleanContent }}
      />
    </div>
  )
}

const providerStyles: { [key: string]: string } = {
  'MLH': 'bg-mlh text-white border-mlh',
  'DEVPOST': 'bg-devpost text-white border-devpost',
  'Coursera': 'bg-blue-600 text-white border-blue-600',
  'Udemy': 'bg-purple-600 text-white border-purple-600'
}

export default function EventDetailPage({ params }: { params: { id: string } }) {
  const [event, setEvent] = useState<Event | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Load event from API first, fallback to localStorage
  useEffect(() => {
    const fetchEvent = async () => {
      const eventId = parseInt(params.id, 10);

      // Try to fetch from API first
      try {
        const response = await fetch(`http://localhost:8080/api/events/${eventId}`);
        if (response.ok) {
          const data = await response.json();
          setEvent(data);
          setIsLoading(false);
          return;
        }
      } catch (error) {
        console.log('API fetch failed, trying localStorage:', error);
      }

      // Fallback to localStorage
      const saved = localStorage.getItem('hackhub-events');
      if (saved) {
        try {
          const events: Event[] = JSON.parse(saved);
          const found = events.find(e => e.id === eventId);
          setEvent(found || null);
        } catch (e) {
          console.error('Failed to load event from localStorage:', e);
        }
      }
      setIsLoading(false);
    };

    fetchEvent();
  }, [params.id]);

  const cleanDescription = useMemo(() => {
    if (typeof window !== 'undefined' && event?.description) {
      return DOMPurify.sanitize(event.description);
    }
    return '';
  }, [event?.description]);

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <div className="h-64 md:h-80 w-full bg-muted animate-pulse rounded-lg mb-8" />
        <div className="h-12 w-3/4 bg-muted animate-pulse rounded mb-4" />
        <div className="h-6 w-1/2 bg-muted animate-pulse rounded mb-8" />
        <div className="space-y-4">
          <div className="h-4 bg-muted animate-pulse rounded" />
          <div className="h-4 bg-muted animate-pulse rounded" />
          <div className="h-4 bg-muted animate-pulse rounded w-2/3" />
        </div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="container mx-auto px-4 py-20 text-center">
        <h1 className="text-2xl font-bold mb-4">Event Not Found</h1>
        <p className="text-muted-foreground mb-8">We couldn't find the details for this event. It might have expired or wasn't saved correctly.</p>
        <Button asChild>
          <Link href="/">Return Home</Link>
        </Button>
      </div>
    );
  }

  const imageUrl = event.imageUrl || `https://picsum.photos/seed/${event.id}/1200/400`;
  const providerClass = providerStyles[event.provider] || 'bg-gray-500 text-white border-gray-500';

  return (
    <div className="bg-background text-foreground">
      <div className="relative h-64 md:h-80 w-full">
        <Image
          src={imageUrl}
          alt={event.title}
          fill
          className="object-cover"
          data-ai-hint="hackathon banner"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/70 to-transparent" />
        <Badge
          className={cn('absolute top-4 right-4 z-10 font-bold border-2', providerClass)}
        >
          {event.provider}
        </Badge>
        <div className="absolute top-4 left-4 z-10">
          <Button asChild variant="outline" size="icon">
            <Link href="/">
              <ArrowLeft />
            </Link>
          </Button>
        </div>
      </div>
      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <header className='text-left mb-8'>
          <h1 className="font-headline text-3xl md:text-5xl font-bold">{event.title}</h1>
          <div className="text-lg pt-4 flex flex-col sm:flex-row sm:items-center gap-x-6 gap-y-2 text-muted-foreground">
            {event.type === "HACKATHON" ? (
              <>
                <span className='flex items-center gap-2'><Calendar className='w-5 h-5' /> {event.date}</span>
                <span className='flex items-center gap-2'><MapPin className='w-5 h-5' /> {event.location}</span>
              </>
            ) : (
              <span className='flex items-center gap-2'><Award className='w-5 h-5' /> Certificate Program</span>
            )}
          </div>
        </header>

        <Button asChild className="my-6 w-full sm:w-auto">
          <a href={event.url} target="_blank" rel="noopener noreferrer">
            Visit Official Page <ExternalLink className="ml-2 w-4 h-4" />
          </a>
        </Button>

        <div
          className="prose prose-invert max-w-none text-muted-foreground"
          dangerouslySetInnerHTML={{ __html: cleanDescription }}
        />

        <DetailSection icon={<GanttChart />} title="Requirements" content={event.requirements} />
        <DetailSection icon={<Scale />} title="Judges" content={event.judges} />
        <DetailSection icon={<Trophy />} title="Judging Criteria" content={event.judgingCriteria} />

      </div>
    </div>
  );
}
