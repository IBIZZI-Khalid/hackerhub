'use client';

import { useRef } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useProfile } from '@/hooks/use-profile';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { User, Mail, LogOut, Star, Calendar, Sparkles } from 'lucide-react';
import { OnboardingModal } from '@/components/onboarding-modal';
import { EventGrid } from '@/components/event-grid';
import { Event } from '@/lib/types';

export default function DashboardPage() {
    const { user, logout, isAuthenticated, isLoading, token } = useAuth();
    const { profile, updatePreferences } = useProfile();
    const router = useRouter();

    const [savedEventsCount, setSavedEventsCount] = useState<number>(0);
    const [favoritesCount, setFavoritesCount] = useState<number>(0);
    const [recommendationsCount, setRecommendationsCount] = useState<number>(0);
    const [loadingData, setLoadingData] = useState(true);

    const [showOnboarding, setShowOnboarding] = useState(false);
    const [recommendations, setRecommendations] = useState<Event[]>([]);

    // Track if we checked for onboarding so we don't flash it
    const hasCheckedOnboarding = useRef(false);

    useEffect(() => {
        if (!isLoading && !isAuthenticated) {
            router.push('/login');
        }
    }, [isAuthenticated, isLoading, router]);

    // Check for onboarding
    // Check for onboarding
    useEffect(() => {
        if (!isLoading && !hasCheckedOnboarding.current) {
            hasCheckedOnboarding.current = true;
            const hasInterests = profile && ((profile.interests && profile.interests.length > 0) ||
                (profile.preferredEventTypes && profile.preferredEventTypes.length > 0));

            if (!hasInterests) {
                setShowOnboarding(true);
            }
        }
    }, [isLoading, profile]);

    useEffect(() => {
        const fetchUserData = async () => {
            if (!user || !token) return;

            try {
                // Fetch saved events
                const interactionsRes = await fetch(`http://localhost:8080/api/interactions/bookmarks`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });

                if (interactionsRes.ok) {
                    const savedEvents = await interactionsRes.json();
                    setSavedEventsCount(savedEvents.length);
                    setFavoritesCount(savedEvents.length);
                }

                // Fetch recommendations
                const recsRes = await fetch('http://localhost:8080/api/recommendations?limit=6', {
                    headers: { 'Authorization': `Bearer ${token}` },
                });

                if (recsRes.ok) {
                    const recsData = await recsRes.json();
                    const recItems: any[] = recsData.recommendations || [];
                    // Map RecommendedItem to Event
                    const events = recItems.map(item => item.event);
                    setRecommendations(events);
                    setRecommendationsCount(events.length);
                }
            } catch (error) {
                console.error('Failed to fetch dashboard data:', error);
            } finally {
                setLoadingData(false);
            }
        };

        if (user && token) {
            fetchUserData();
        }
    }, [user, token, showOnboarding]); // Refresh if onboarding changes preferences

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-white text-xl">Loading...</div>
            </div>
        );
    }

    if (!user) return null;

    return (
        <div className="container mx-auto px-4 py-12">
            <div className="max-w-6xl mx-auto">
                <OnboardingModal
                    isOpen={showOnboarding}
                    onOpenChange={setShowOnboarding}
                    onSave={updatePreferences}
                />

                {/* Header */}
                <div className="mb-12">
                    <h1 className="text-4xl font-bold text-white mb-4">Welcome back, {user.username}! 👋</h1>
                    <p className="text-slate-400 text-lg">Manage your hackathon journey</p>
                </div>

                {/* User Info Card */}
                <Card className="bg-gradient-to-r from-purple-900/50 to-blue-900/50 border-purple-500/30 p-8 mb-8">
                    <div className="flex items-center justify-between flex-wrap gap-4">
                        <div className="space-y-3">
                            <div className="flex items-center gap-3 text-white">
                                <User className="h-5 w-5 text-purple-400" />
                                <span className="font-medium">Username:</span>
                                <span className="text-purple-300">{user.username}</span>
                            </div>
                            <div className="flex items-center gap-3 text-white">
                                <Mail className="h-5 w-5 text-blue-400" />
                                <span className="font-medium">Email:</span>
                                <span className="text-blue-300">{user.email}</span>
                            </div>
                            <div className="flex items-center gap-3 text-white">
                                <Star className="h-5 w-5 text-yellow-400" />
                                <span className="font-medium">Role:</span>
                                <span className="text-yellow-300">{user.role}</span>
                            </div>
                        </div>
                        <div className="flex gap-3">
                            <Button
                                onClick={() => setShowOnboarding(true)}
                                variant="secondary"
                                className="bg-purple-500/20 text-purple-300 hover:bg-purple-500/30"
                            >
                                <Sparkles className="h-4 w-4 mr-2" />
                                Personalize
                            </Button>
                            <Button
                                onClick={logout}
                                variant="outline"
                                className="border-red-400/50 text-red-400 hover:bg-red-500/10"
                            >
                                <LogOut className="h-4 w-4 mr-2" />
                                Logout
                            </Button>
                        </div>
                    </div>
                </Card>

                {/* Stats */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <Card className="bg-slate-800/50 border-slate-700 p-6">
                        <div className="flex items-center gap-4">
                            <div className="bg-blue-500/20 p-3 rounded-lg">
                                <Calendar className="h-6 w-6 text-blue-400" />
                            </div>
                            <div>
                                <p className="text-slate-400 text-sm">Saved Events</p>
                                <p className="text-2xl font-bold text-white">
                                    {loadingData ? '...' : savedEventsCount}
                                </p>
                            </div>
                        </div>
                    </Card>

                    <Card className="bg-slate-800/50 border-slate-700 p-6">
                        <div className="flex items-center gap-4">
                            <div className="bg-purple-500/20 p-3 rounded-lg">
                                <Star className="h-6 w-6 text-purple-400" />
                            </div>
                            <div>
                                <p className="text-slate-400 text-sm">Favorites</p>
                                <p className="text-2xl font-bold text-white">
                                    {loadingData ? '...' : favoritesCount}
                                </p>
                            </div>
                        </div>
                    </Card>

                    <Card className="bg-slate-800/50 border-slate-700 p-6">
                        <div className="flex items-center gap-4">
                            <div className="bg-green-500/20 p-3 rounded-lg">
                                <Sparkles className="h-6 w-6 text-green-400" />
                            </div>
                            <div>
                                <p className="text-slate-400 text-sm">Recommendations</p>
                                <p className="text-2xl font-bold text-white">
                                    {loadingData ? '...' : recommendationsCount > 0 ? recommendationsCount : 'N/A'}
                                </p>
                            </div>
                        </div>
                    </Card>
                </div>

                {/* Recommendations Section */}
                {recommendations.length > 0 && (
                    <div className="mb-12">
                        <h2 className="text-2xl font-bold text-white mb-6 flex items-center gap-2">
                            <Sparkles className="h-6 w-6 text-yellow-500" />
                            Recommended For You
                        </h2>
                        <EventGrid events={recommendations} />
                    </div>
                )}

                {/* Saved Events Section */}
                <Card className="bg-slate-800/50 border-slate-700 p-8">
                    <h2 className="text-2xl font-bold text-white mb-6">Your Saved Events</h2>
                    <div className="text-center py-12">
                        <p className="text-slate-400 text-lg mb-4">No saved events yet</p>
                        <p className="text-slate-500 mb-6">Start exploring and save your favorite hackathons!</p>
                        <Button
                            onClick={() => router.push('/')}
                            className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700"
                        >
                            Browse Events
                        </Button>
                    </div>
                </Card>
            </div>
        </div>
    );
}
