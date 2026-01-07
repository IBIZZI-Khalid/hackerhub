"use client";

import Image from 'next/image';
import Link from 'next/link';
import { type Event } from '@/lib/types';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { Calendar, ExternalLink, Heart, MapPin, Award } from 'lucide-react';
import { useFavorites } from '@/hooks/use-favorites';

interface EventCardProps {
  event: Event;
}

const providerStyles: { [key: string]: string } = {
  'MLH': 'bg-mlh text-white border-mlh',
  'DEVPOST': 'bg-devpost text-white border-devpost',
  'Coursera': 'bg-blue-600 text-white border-blue-600',
  'Udemy': 'bg-purple-600 text-white border-purple-600',
  'Oracle': 'bg-red-600 text-white border-red-600',
  'IBM': 'bg-blue-600 text-white border-blue-600',
  'Microsoft': 'bg-sky-500 text-white border-sky-500'
}

export function EventCard({ event }: EventCardProps) {
  const { isFavorite, toggleFavorite } = useFavorites();
  const favorite = isFavorite(event.id);
  const imageUrl = event.imageUrl || `https://picsum.photos/seed/${event.id}/600/400`;
  const providerClass = providerStyles[event.provider] || 'bg-gray-500 text-white border-gray-500';

  return (
    <Card className="h-full flex flex-col group overflow-hidden bg-card/80 backdrop-blur-sm border-white/10 transition-all duration-300 hover:border-primary/50 hover:shadow-2xl hover:shadow-primary/10 hover:-translate-y-1">
      <div className="relative overflow-hidden">
        <div className="absolute top-4 left-4 z-10">
          <Badge className="bg-gradient-to-r from-purple-600 to-blue-600 text-white font-bold">
            {event.type === 'HACKATHON' ? '🏆 Hackathon' : event.type === 'COURSE' ? '📚 Course' : '🎓 Certificate'}
          </Badge>
        </div>
        <Badge
          className={cn('absolute top-4 right-4 z-10 font-bold border-2', providerClass)}
        >
          {event.provider}
        </Badge>
        <Image
          src={imageUrl}
          alt={event.title}
          width={600}
          height={400}
          className="w-full h-48 object-cover transition-transform duration-300 group-hover:scale-105"
          data-ai-hint="hackathon event"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/20 to-transparent transition-opacity duration-300"></div>
        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-all duration-300"></div>
      </div>
      <CardHeader>
        <Link href={`/events/${event.id}`}>
          <CardTitle className="font-headline text-xl leading-tight line-clamp-2 hover:underline">{event.title}</CardTitle>
        </Link>
      </CardHeader>
      <CardContent className="flex-1 space-y-3 text-sm text-muted-foreground">
        {event.type === 'HACKATHON' ? (
          <>
            <div className="flex items-center gap-2">
              <Calendar className="w-4 h-4" />
              <span>{event.eventDate ? new Date(event.eventDate).toLocaleDateString() : 'Date TBD'}</span>
            </div>
            <div className="flex items-center gap-2">
              <MapPin className="w-4 h-4" />
              <span>{event.location || 'Location TBD'}</span>
            </div>
          </>
        ) : (
          <>
            <div className="flex items-center gap-2">
              <Award className="w-4 h-4" />
              <span>{event.type === 'COURSE' ? 'Course' : 'Certification'}</span>
            </div>
            {event.level && (
              <div className="flex items-center gap-2">
                {/* Reusing MapPin or similar if BarChart not imported, but let's just use text for now or generic icon if unsure */}
                <Award className="w-4 h-4" />
                <span>Level: {event.level}</span>
              </div>
            )}
          </>
        )}
        <p className="line-clamp-3">{event.blurb}</p>
      </CardContent>
      <CardFooter className="gap-2">
        <Button asChild variant="outline" className="flex-1">
          <Link href={`/events/${event.id}`}>View Details</Link>
        </Button>
        <Button variant="ghost" size="icon" onClick={() => toggleFavorite(event)}>
          <Heart className={cn("w-5 h-5", favorite ? 'fill-red-500 text-red-500' : 'text-muted-foreground')} />
          <span className="sr-only">Favorite</span>
        </Button>
        <Button asChild variant="ghost" size="icon">
          <a href={event.url} target="_blank" rel="noopener noreferrer">
            <ExternalLink className="w-5 h-5 text-muted-foreground" />
            <span className="sr-only">Open original page</span>
          </a>
        </Button>
      </CardFooter>
    </Card>
  );
}
