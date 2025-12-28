export interface ScrapeRequest {
  domain?: string;
  prize?: string;
  location?: string;
  count: number;
}

export interface SearchFormValues extends ScrapeRequest {
  scrapeType: 'hackathons' | 'certificates';
}

export interface Event {
  id: number;
  title: string;
  description: string;
  blurb: string;
  url: string;
  location: string;
  date: string;
  imageUrl: string;
  provider: "MLH" | "DEVPOST" | "Coursera" | "Udemy";
  requirements: string | null;
  judges: string | null;
  judgingCriteria: string | null;
  type: "HACKATHON" | "CERTIFICATE";
  scrappedAt: string;
}
