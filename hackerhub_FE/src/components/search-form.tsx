"use client";

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Slider } from '@/components/ui/slider';
import { Card, CardContent } from '@/components/ui/card';
import { Loader2, Search } from 'lucide-react';
import { useState } from 'react';
import { type SearchFormValues } from '@/lib/types';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

const formSchema = z.object({
  domain: z.string().optional(),
  prize: z.string().optional(),
  location: z.string().optional(),
  count: z.number().min(5).max(50),
  scrapeType: z.enum(['hackathons', 'certificates', 'courses']),
  provider: z.string().optional().default('all'),
});

export default function SearchForm({ onSubmit, isLoading }: { onSubmit: (values: SearchFormValues) => void; isLoading: boolean }) {
  const [sliderValue, setSliderValue] = useState(10);
  const [maxPrize, setMaxPrize] = useState(5000);

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      domain: '',
      prize: '5000',
      location: '',
      count: 10,
      scrapeType: 'hackathons',
      provider: 'all',
    },
  });

  const scrapeType = form.watch('scrapeType');

  return (
    <Card className="w-full max-w-4xl mx-auto shadow-2xl">
      <CardContent className="p-8">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
            <FormField
              control={form.control}
              name="scrapeType"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-lg font-semibold">What are you looking for?</FormLabel>
                  <FormControl>
                    <RadioGroup
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                      className="flex flex-col space-y-3"
                    >
                      <FormItem className="flex items-center space-x-3 space-y-0">
                        <FormControl>
                          <RadioGroupItem value="hackathons" />
                        </FormControl>
                        <FormLabel className="font-normal cursor-pointer">
                          Hackathons
                        </FormLabel>
                      </FormItem>
                      <FormItem className="flex items-center space-x-3 space-y-0">
                        <FormControl>
                          <RadioGroupItem value="certificates" />
                        </FormControl>
                        <FormLabel className="font-normal cursor-pointer">
                          Certificates
                        </FormLabel>
                      </FormItem>
                      <FormItem className="flex items-center space-x-3 space-y-0">
                        <FormControl>
                          <RadioGroupItem value="courses" />
                        </FormControl>
                        <FormLabel className="font-normal cursor-pointer">
                          Courses
                        </FormLabel>
                      </FormItem>
                    </RadioGroup>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-x-6 gap-y-8">
              <FormField
                control={form.control}
                name="provider"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Platform / Provider</FormLabel>
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select a provider" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="all">All Providers</SelectItem>
                        {scrapeType === 'hackathons' ? (
                          <>
                            <SelectItem value="mlh">MLH</SelectItem>
                            <SelectItem value="devpost">Devpost</SelectItem>
                            <SelectItem value="oracle">Oracle</SelectItem>
                            <SelectItem value="ibm">IBM</SelectItem>
                            <SelectItem value="microsoft">Microsoft</SelectItem>
                          </>
                        ) : (
                          <>
                            <SelectItem value="oracle">Oracle</SelectItem>
                            <SelectItem value="ibm">IBM</SelectItem>
                            <SelectItem value="microsoft">Microsoft</SelectItem>
                          </>
                        )}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="domain"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Domain / Technology</FormLabel>
                    <FormControl>
                      <Input placeholder={scrapeType === 'hackathons' ? "e.g., AI, Web3, Python" : "e.g., Cloud, Data Science"} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="location"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Location</FormLabel>
                    <FormControl>
                      <Input placeholder="e.g., Remote, NYC" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {scrapeType === 'hackathons' && (
                <FormField
                  control={form.control}
                  name="prize"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>
                        Maximum Prize: <span className='text-primary font-bold'>${maxPrize}</span>
                      </FormLabel>
                      <FormControl>
                        <Slider
                          min={0}
                          max={50000}
                          step={500}
                          value={[maxPrize]}
                          onValueChange={(value) => {
                            const newValue = value[0];
                            field.onChange(`${newValue}`);
                            setMaxPrize(newValue);
                          }}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              )}

              <FormField
                control={form.control}
                name="count"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Number of Results: <span className='text-primary font-bold'>{sliderValue}</span>
                    </FormLabel>
                    <FormControl>
                      <Slider
                        min={5}
                        max={50}
                        step={1}
                        value={[sliderValue]}
                        onValueChange={(value) => {
                          field.onChange(value[0]);
                          setSliderValue(value[0]);
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <Button type="submit" className="w-full text-lg" size="lg" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                  Scraping... this may take a moment
                </>
              ) : (
                <>
                  <Search className="mr-2 h-5 w-5" />
                  Scrape Now
                </>
              )}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}
