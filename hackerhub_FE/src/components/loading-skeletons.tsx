"use client";

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Loader2 } from 'lucide-react';

const LOADING_PHRASES = [
  "Searching the web for amazing hackathons... 🔍",
  "Give it a minute, we're finding the best opportunities... ⏱️",
  "Scanning through thousands of events... 🌐",
  "Almost there, hang tight... 🚀",
  "Discovering incredible coding challenges... 💻",
  "Fetching the latest competitions... 🏆",
  "This might take a moment, but it'll be worth it... ⭐",
];

function SkeletonCard() {
  return (
    <Card className="overflow-hidden">
      <Skeleton className="h-48 w-full" />
      <CardHeader>
        <Skeleton className="h-6 w-3/4" />
        <Skeleton className="h-4 w-1/2 mt-2" />
      </CardHeader>
      <CardContent className="space-y-3">
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-2/3" />
      </CardContent>
    </Card>
  );
}

export function LoadingSkeletons({ count = 6 }: { count?: number }) {
  const [phraseIndex, setPhraseIndex] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setPhraseIndex((prev) => (prev + 1) % LOADING_PHRASES.length);
    }, 3000); // Change phrase every 3 seconds

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="space-y-8">
      {/* Loading message with animated icon */}
      <div className="flex flex-col items-center justify-center py-8 space-y-4">
        <Loader2 className="w-12 h-12 text-primary animate-spin" />
        <p className="text-lg font-medium text-center animate-pulse">
          {LOADING_PHRASES[phraseIndex]}
        </p>
        <p className="text-sm text-muted-foreground">
          Please don't close this page...
        </p>
      </div>

      {/* Skeleton cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8">
        {Array.from({ length: count }).map((_, i) => (
          <SkeletonCard key={i} />
        ))}
      </div>
    </div>
  );
}
