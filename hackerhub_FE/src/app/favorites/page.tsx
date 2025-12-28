"use client";

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useFavorites } from '@/hooks/use-favorites';
import { EventGrid } from '@/components/event-grid';
import { type Event } from '@/lib/types';
import { Button } from '@/components/ui/button';
import { HeartCrack } from 'lucide-react';

export default function FavoritesPage() {
  const { favorites } = useFavorites();
  const [clientFavorites, setClientFavorites] = useState<Event[]>([]);

  useEffect(() => {
    // Ensure favorites are loaded on the client-side to avoid hydration mismatch
    setClientFavorites(favorites);
  }, [favorites]);
  
  return (
    <div className="container mx-auto px-4 py-8">
      <div className="text-center py-12">
        <h1 className="font-headline text-4xl md:text-5xl font-bold tracking-tight bg-gradient-to-br from-white to-gray-400 gradient-text">
          Your Favorite Items
        </h1>
        <p className="mt-4 max-w-2xl mx-auto text-lg text-muted-foreground">
          Here are the hackathons and certificates you've saved.
        </p>
      </div>

      {clientFavorites.length > 0 ? (
        <EventGrid events={clientFavorites} />
      ) : (
        <div className="flex flex-col items-center justify-center text-center py-20 bg-card/50 rounded-lg border-2 border-dashed">
            <HeartCrack className="w-16 h-16 text-primary mb-4" />
            <h3 className="font-headline text-2xl font-bold">No Favorites Yet</h3>
            <p className="text-muted-foreground mt-2 max-w-md">You haven't saved any items. Start exploring to find your next challenge or course!</p>
            <Button asChild className="mt-6">
                <Link href="/#explore">
                    Explore Now
                </Link>
            </Button>
        </div>
      )}
    </div>
  );
}
