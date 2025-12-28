import { Rocket } from 'lucide-react';

export function Footer() {
  return (
    <footer className="border-t py-6 md:py-8">
      <div className="container flex items-center justify-center gap-2">
        <Rocket className="h-5 w-5 text-primary" />
        <p className="text-sm text-muted-foreground">
          &copy; {new Date().getFullYear()} HackHub Explorer
        </p>
      </div>
    </footer>
  );
}
