"use server";

import { z } from 'zod';
import { type Event, type ScrapeRequest } from '@/lib/types';
import { DUMMY_CERTIFICATES } from '@/lib/dummy-data';


const formSchema = z.object({
  domain: z.string().optional(),
  prize: z.string().optional(),
  location: z.string().optional(),
  count: z.number().min(5).max(50),
  scrapeType: z.enum(['hackathons', 'certificates']),
});

type SearchFormValues = z.infer<typeof formSchema>;

async function scrapeProvider(provider: 'mlh' | 'devpost', request: ScrapeRequest): Promise<Event[]> {
  // Use environment variable or fallback to localhost:8080
  const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
  const BASE_URL = `${API_BASE_URL}/api/scraper`;

  try {
    console.log(`[${provider.toUpperCase()}] Calling API: ${BASE_URL}/${provider}`);
    const response = await fetch(`${BASE_URL}/${provider}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
      cache: 'no-store',
    });

    if (!response.ok) {
      const errorBody = await response.text();
      console.error(`Scraping ${provider.toUpperCase()} failed with status ${response.status}: ${errorBody}`);
      throw new Error(`Oops! Something went wrong on our end. Please try again in a moment. 🔧`);
    }

    return response.json();
  } catch (error) {
    if (error instanceof Error && error.message.includes('fetch failed')) {
      throw new Error(`Server is taking a coffee break... We'll be back soon! ☕ Please check if the backend is running on ${API_BASE_URL}`);
    }
    throw error;
  }
}

export async function scrapeHackathons(values: SearchFormValues): Promise<{ data: Event[] | null; error: string | null }> {
  const validation = formSchema.safeParse(values);

  if (!validation.success) {
    return { data: null, error: "Invalid form data provided." };
  }

  const { scrapeType, ...scrapeParams } = validation.data;

  if (scrapeType === 'certificates') {
    // Return dummy certificates for now
    return { data: DUMMY_CERTIFICATES, error: null };
  }

  try {
    const [mlhResults, devpostResults] = await Promise.all([
      scrapeProvider('mlh', scrapeParams),
      scrapeProvider('devpost', scrapeParams),
    ]);
    const combined = [...mlhResults, ...devpostResults].sort((a, b) => new Date(b.scrappedAt).getTime() - new Date(a.scrappedAt).getTime());
    return { data: combined, error: null };
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : "An unknown error occurred during scraping.";
    console.error("Scraping action failed:", errorMessage);
    return { data: null, error: errorMessage };
  }
}
