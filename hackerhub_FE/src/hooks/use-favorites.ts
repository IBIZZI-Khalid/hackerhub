"use client";

import { useState, useEffect, useCallback } from 'react';
import { type Event } from '@/lib/types';
import { useToast } from './use-toast';

const FAVORITES_KEY = 'hackhub-favorites';

export const useFavorites = () => {
  const [favorites, setFavorites] = useState<Event[]>([]);
  const { toast } = useToast();

  useEffect(() => {
    try {
      const storedFavorites = localStorage.getItem(FAVORITES_KEY);
      if (storedFavorites) {
        setFavorites(JSON.parse(storedFavorites));
      }
    } catch (error) {
      console.error('Failed to load favorites from localStorage', error);
    }
  }, []);

  const saveFavorites = (newFavorites: Event[]) => {
    try {
      setFavorites(newFavorites);
      localStorage.setItem(FAVORITES_KEY, JSON.stringify(newFavorites));
    } catch (error) {
      console.error('Failed to save favorites to localStorage', error);
    }
  };

  const addFavorite = useCallback((event: Event) => {
    const newFavorites = [...favorites, event];
    saveFavorites(newFavorites);
    toast({
        title: "Added to Favorites",
        description: `"${event.title}" has been saved.`,
    });
  }, [favorites, toast]);

  const removeFavorite = useCallback((eventId: number) => {
    const eventToRemove = favorites.find(fav => fav.id === eventId);
    const newFavorites = favorites.filter((fav) => fav.id !== eventId);
    saveFavorites(newFavorites);
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

  return { favorites, addFavorite, removeFavorite, isFavorite, toggleFavorite };
};
