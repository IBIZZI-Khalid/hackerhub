import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ArrowDown, Bot, Calendar, Heart } from 'lucide-react';
import Link from 'next/link';

export function Hero() {
  const stats = [
    {
      icon: <Bot className="h-6 w-6 text-accent" />,
      title: '5 Platforms',
      description: 'MLH & Devpost & more ...',
    },
    {
      icon: <Calendar className="h-6 w-6 text-accent" />,
      title: 'Real-time Data',
      description: 'Freshly Scraped Events',
    },
    {
      icon: <Heart className="h-6 w-6 text-accent" />,
      title: 'Save Favorites',
      description: 'Track Your Interests',
    },
  ];

  return (
    <section className="relative overflow-hidden pt-20 pb-28 md:pt-28 md:pb-36 text-center">
      <div className="absolute inset-0 -z-10 bg-[radial-gradient(40%_50%_at_50%_30%,#9D32D340_0%,#262329_100%)]"></div>
      <div className="container">
        <h1 className="font-headline text-5xl md:text-7xl font-extrabold tracking-tighter bg-gradient-to-br from-white to-gray-400 gradient-text">
          Discover Global Hackathons
        </h1>
        <p className="mt-6 max-w-2xl mx-auto text-lg md:text-xl text-muted-foreground">
          Your central hub for exploring the latest hackathons from Major League Hacking and Devpost. Find, filter, and favorite events to fuel your next big project.
        </p>
        <div className="mt-8 flex justify-center gap-4">
          <Button asChild size="lg" className="bg-primary hover:bg-primary/90 text-primary-foreground font-semibold">
            <Link href="#explore">
                Explore Events
                <ArrowDown className="ml-2 h-5 w-5" />
            </Link>
          </Button>
        </div>

        <div className="mt-20 grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto">
            {stats.map((stat, index) => (
                <Card key={index} className="bg-white/5 border-white/10 backdrop-blur-sm">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-muted-foreground">{stat.title}</CardTitle>
                        {stat.icon}
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{stat.description}</div>
                    </CardContent>
                </Card>
            ))}
        </div>
      </div>
    </section>
  );
}
