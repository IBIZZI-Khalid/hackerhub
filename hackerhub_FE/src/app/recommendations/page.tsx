'use client';

import { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Star, Calendar, MapPin, ExternalLink, Sparkles } from 'lucide-react';

interface RecommendedEvent {
    id: number;
    title: string;
    description: string;
    type: string;
    eventDate: string;
    location: string;
    sourceUrl: string;
    provider: string;
    score?: number;
}

export default function RecommendationsPage() {
    const { user, token, isAuthenticated } = useAuth();
    const [recommendations, setRecommendations] = useState<RecommendedEvent[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (isAuthenticated && user && token) {
            fetchRecommendations();
        }
    }, [isAuthenticated, user, token]);

    const fetchRecommendations = async () => {
        try {
            const response = await fetch(`http://localhost:8080/api/recommendations?limit=20`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (response.ok) {
                const data = await response.json();
                console.log('Recommendation Response:', data); // Log to verify strategy
                const items = data.recommendations || [];
                const mappedEvents = items.map((item: any) => ({
                    ...item.event,
                    score: item.score
                }));
                setRecommendations(mappedEvents);
            }
        } catch (error) {
            console.error('Failed to fetch recommendations:', error);
        } finally {
            setLoading(false);
        }
    };

    if (!isAuthenticated) {
        return (
            <div className="container mx-auto px-4 py-12">
                <div className="max-w-md mx-auto text-center">
                    <Sparkles className="h-16 w-16 mx-auto mb-4 text-purple-400" />
                    <h1 className="text-2xl font-bold text-white mb-4">Login Required</h1>
                    <p className="text-slate-400">Please login to see personalized recommendations</p>
                </div>
            </div>
        );
    }

    if (loading) {
        return (
            <div className="container mx-auto px-4 py-12">
                <div className="text-center text-white">Loading recommendations...</div>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-12">
            <div className="mb-8">
                <h1 className="text-4xl font-bold text-white mb-4 flex items-center gap-3">
                    <Sparkles className="h-10 w-10 text-purple-400" />
                    Recommended For You
                </h1>
                <p className="text-slate-400 text-lg">
                    Personalized hackathon recommendations based on your interests and activity
                </p>
            </div>

            {recommendations.length === 0 ? (
                <Card className="bg-slate-800/50 border-slate-700 p-12 text-center">
                    <Sparkles className="h-16 w-16 mx-auto mb-4 text-slate-600" />
                    <h2 className="text-2xl font-bold text-white mb-4">No Recommendations Yet</h2>
                    <p className="text-slate-400 mb-6">
                        Start exploring and interacting with events to get personalized recommendations!
                    </p>
                    <Button
                        onClick={() => window.location.href = '/'}
                        className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700"
                    >
                        Explore Events
                    </Button>
                </Card>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {recommendations.map((event) => (
                        <Card key={event.id} className="bg-slate-800/50 border-slate-700 p-6 hover:border-purple-500/50 transition-all">
                            <div className="flex items-start justify-between mb-4">
                                <div className="flex gap-2">
                                    <span className="text-xs px-3 py-1 rounded-full bg-purple-500/20 text-purple-300">
                                        {event.provider}
                                    </span>
                                    <span className="text-xs px-3 py-1 rounded-full bg-gradient-to-r from-purple-600 to-blue-600 text-white font-bold">
                                        {event.type === 'HACKATHON' ? '🏆 Hackathon' : event.type === 'COURSE' ? '📚 Course' : '🎓 Certificate'}
                                    </span>
                                </div>
                                {event.score && (
                                    <div className="flex items-center gap-1">
                                        <Star className="h-4 w-4 text-yellow-400 fill-yellow-400" />
                                        <span className="text-sm text-slate-400">{event.score.toFixed(1)}</span>
                                    </div>
                                )}
                            </div>

                            <h3 className="text-xl font-bold text-white mb-3 line-clamp-2">
                                {event.title}
                            </h3>

                            <p className="text-slate-400 text-sm mb-4 line-clamp-3">
                                {event.description}
                            </p>

                            <div className="space-y-2 mb-4">
                                {event.eventDate && (
                                    <div className="flex items-center gap-2 text-sm text-slate-400">
                                        <Calendar className="h-4 w-4" />
                                        {new Date(event.eventDate).toLocaleDateString()}
                                    </div>
                                )}
                                {event.location && (
                                    <div className="flex items-center gap-2 text-sm text-slate-400">
                                        <MapPin className="h-4 w-4" />
                                        {event.location}
                                    </div>
                                )}
                            </div>

                            <Button
                                onClick={() => window.open(event.sourceUrl, '_blank')}
                                className="w-full bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700"
                            >
                                <ExternalLink className="h-4 w-4 mr-2" />
                                View Details
                            </Button>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}
