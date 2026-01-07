'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function EventsPage() {
    const router = useRouter();

    useEffect(() => {
        router.push('/#explore');
    }, [router]);

    return (
        <div className="container mx-auto px-4 py-8 text-center text-white">
            <p>Redirecting to events...</p>
        </div>
    );
}
