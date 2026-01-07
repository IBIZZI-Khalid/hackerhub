"use client";

import { useState, useEffect, useCallback } from 'react';
import { type Event } from '@/lib/types';
import { useToast } from './use-toast';

const FAVORITES_KEY = 'hackhub-favorites';

// Helper to get auth token
const getAuthToken = () => {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('authToken');
  }
  return null;
};

// Helper to get user ID
const getUserId = () => {
  if (typeof window !== 'undefined') {
    const userStr = localStorage.getItem('authUser');
    if (userStr) {
      const user = JSON.parse(userStr);
      return user.id;
    }
  }
  return null;
};

export const useFavorites = () => {
  const [favorites, setFavorites] = useState<Event[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { toast } = useToast();

  useEffect(() => {
    const loadFavorites = async () => {
      const token = getAuthToken();
      const userId = getUserId();

      // If user is authenticated, fetch from backend
      if (token && userId) {
        try {
          // Use the correct bookmarks endpoint which returns List<Event>
          const response = await fetch(`http://localhost:8080/api/interactions/bookmarks`, {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          });

          if (response.ok) {
            const savedEvents = await response.json();
            setFavorites(savedEvents);
            // Also update localStorage as cache
            localStorage.setItem(FAVORITES_KEY, JSON.stringify(savedEvents));
          } else {
            // Backend failed, fallback to localStorage
            const stored = localStorage.getItem(FAVORITES_KEY);
            if (stored) setFavorites(JSON.parse(stored));
          }
        } catch (error) {
          console.error('Failed to load favorites from backend, using localStorage:', error);
          // Fallback to localStorage
          const stored = localStorage.getItem(FAVORITES_KEY);
          if (stored) setFavorites(JSON.parse(stored));
        }
      } else {
        // Not authenticated, use localStorage only
        try {
          const storedFavorites = localStorage.getItem(FAVORITES_KEY);
          if (storedFavorites) {
            setFavorites(JSON.parse(storedFavorites));
          }
        } catch (error) {
          console.error('Failed to load favorites from localStorage', error);
        }
      }
      setIsLoading(false);
    };

    loadFavorites();
  }, []); // Empty dependency array - load once on mount

  const saveFavorites = (newFavorites: Event[]) => {
    try {
      setFavorites(newFavorites);
      localStorage.setItem(FAVORITES_KEY, JSON.stringify(newFavorites));
    } catch (error) {
      console.error('Failed to save favorites to localStorage', error);
    }
  };

  const saveToBackend = async (eventId: number, action: 'SAVE' | 'UNSAVE') => {
    const token = getAuthToken();
    const userId = getUserId();

    if (!token || !userId) {
      return; // User not logged in, only use localStorage
    }

    try {
      if (action === 'SAVE') {
        // Save interaction to backend - mapped to BOOKMARK
        await fetch(`http://localhost:8080/api/interactions/track/${eventId}`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            eventId: eventId,
            type: 'BOOKMARK', // Use valid InteractionType
            source: 'web_client'
          }),
        });
      } else {
        // For unsave, use the DELETE endpoint
        await fetch(`http://localhost:8080/api/interactions/bookmarks/${eventId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
          }
        });
      }
    } catch (error) {
      console.error('Failed to sync favorite with backend:', error);
    }
  };

  const addFavorite = useCallback((event: Event) => {
    const newFavorites = [...favorites, event];
    saveFavorites(newFavorites);
    saveToBackend(event.id, 'SAVE'); // Sync with backend
    toast({
      title: "Added to Favorites",
      description: `"${event.title}" has been saved to your account.`,
    });
  }, [favorites, toast]);

  const removeFavorite = useCallback((eventId: number) => {
    const eventToRemove = favorites.find(fav => fav.id === eventId);
    const newFavorites = favorites.filter((fav) => fav.id !== eventId);
    saveFavorites(newFavorites);
    saveToBackend(eventId, 'UNSAVE'); // Sync with backend
    if (eventToRemove) {
      toast({
        title: "Removed from Favorites",
        description: `"${eventToRemove.title}" has been removed.`,
      });
    }
  }, [favorites, toast]);

  const isFavorite = useCallback((eventId: number) => {
    return favorites.some((fav) => fav.id === eventId);
  }, [favorites]);

  const toggleFavorite = useCallback((event: Event) => {
    if (isFavorite(event.id)) {
      removeFavorite(event.id);
    } else {
      addFavorite(event);
    }
  }, [isFavorite, addFavorite, removeFavorite]);

  return { favorites, addFavorite, removeFavorite, isFavorite, toggleFavorite, isLoading };
};

