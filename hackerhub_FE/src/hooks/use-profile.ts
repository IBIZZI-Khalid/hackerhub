import { useState, useEffect } from 'react';
import { UserProfile } from '@/lib/types';
import { useAuth } from '@/contexts/AuthContext';

export function useProfile() {
    const { user, token } = useAuth();
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

    useEffect(() => {
        if (!user || !token) {
            setIsLoading(false);
            return;
        }

        const fetchProfile = async () => {
            try {
                const res = await fetch(`${API_URL}/api/profile`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (res.ok) {
                    const data = await res.json();
                    setProfile(data);
                } else if (res.status === 404) {
                    setProfile(null);
                } else {
                    setError('Failed to fetch profile');
                }
            } catch (err) {
                setError('Error connecting to server');
            } finally {
                setIsLoading(false);
            }
        };

        fetchProfile();
    }, [user, token, API_URL]);

    const updatePreferences = async (interests: string[], eventTypes: string[], providers: string[]) => {
        if (!token) return false;

        try {
            const res = await fetch(`${API_URL}/api/profile/preferences`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    interests,
                    eventTypes,
                    providers
                })
            });

            if (res.ok) {
                // Refresh profile
                const newProfile = { ...profile, interests, preferredEventTypes: eventTypes, preferredProviders: providers } as UserProfile;
                setProfile(newProfile);
                return true;
            }
            return false;
        } catch (err) {
            console.error(err);
            return false;
        }
    };

    return { profile, isLoading, error, updatePreferences };
}
