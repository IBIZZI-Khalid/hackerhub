export interface ScrapeRequest {
  domain?: string;
  prize?: string;
  location?: string;
  count: number;
}

export interface SearchFormValues extends ScrapeRequest {
  scrapeType: 'hackathons' | 'certificates' | 'courses';
  provider?: string;
}

export interface UserProfile {
  id: number;
  interests: string[];
  preferredEventTypes: string[];
  preferredProviders: string[];
}

export interface Event {
  id: number;
  title: string;
  description: string;
  blurb: string;
  url: string;
  location: string;
  date: string; // Legacy property, kept for backward compatibility
  eventDate?: string; // Backend sends this (LocalDate from Java)
  imageUrl: string;
  provider: string; // "MLH", "DEVPOST", "Oracle", "IBM", "Microsoft", etc.
  requirements: string | null;
  judges: string | null;
  judgingCriteria: string | null;
  type: "HACKATHON" | "CERTIFICATION" | "COURSE";
  scrappedAt: string;
  category?: string;
  examCode?: string;
  level?: string;
  price?: number;
}
