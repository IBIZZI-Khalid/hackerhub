"use client";

import { type Event } from '@/lib/types';
import { EventCard } from './event-card';

interface EventGridProps {
  events: Event[];
}

export function EventGrid({ events }: EventGridProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8">
      {events.map((event, index) => (
        <div
          key={`${event.provider}-${event.id}`}
          className="animate-in fade-in-0 slide-in-from-bottom-4 duration-500"
          style={{ animationDelay: `${index * 50}ms`, animationFillMode: 'backwards' }}
        >
          <EventCard event={event} />
        </div>
      ))}
    </div>
  );
}
