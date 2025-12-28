"use client";

import React, { createContext, useContext, useState, ReactNode } from 'react';
import { type Event } from '@/lib/types';
import { DUMMY_HACKATHONS } from '@/lib/dummy-data';

interface EventsContextType {
    events: Event[];
    setEvents: (events: Event[]) => void;
    getEvent: (id: string) => Event | undefined;
}

const EventsContext = createContext<EventsContextType | undefined>(undefined);

export function EventsProvider({ children }: { children: ReactNode }) {
    const [events, setEvents] = useState<Event[]>(DUMMY_HACKATHONS);

    const getEvent = (id: string) => {
        return events.find(event => event.id === id);
    };

    return (
        <EventsContext.Provider value={{ events, setEvents, getEvent }}>
            {children}
        </EventsContext.Provider>
    );
}

export function useEvents() {
    const context = useContext(EventsContext);
    if (context === undefined) {
        throw new Error('useEvents must be used within an EventsProvider');
    }
    return context;
}
