"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Check, BookOpen, Award, Code, GraduationCap } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

interface OnboardingModalProps {
    isOpen: boolean;
    onOpenChange: (open: boolean) => void;
    onSave: (interests: string[], eventTypes: string[], providers: string[]) => Promise<boolean>;
}

export function OnboardingModal({ isOpen, onOpenChange, onSave }: OnboardingModalProps) {
    const { toast } = useToast();
    const [selectedTypes, setSelectedTypes] = useState<string[]>([]);
    const [keywords, setKeywords] = useState("");
    const [isSaving, setIsSaving] = useState(false);

    const toggleType = (type: string) => {
        setSelectedTypes(prev =>
            prev.includes(type) ? prev.filter(t => t !== type) : [...prev, type]
        );
    };

    const handleSave = async () => {
        if (selectedTypes.length === 0) {
            toast({
                title: "Selection Required",
                description: "Please select at least one type of content you are interested in.",
                variant: "destructive"
            });
            return;
        }

        setIsSaving(true);
        // Parse keywords
        const interestList = keywords.split(",").map(k => k.trim()).filter(k => k.length > 0);

        // Default provider set to all for now as user didn't explicitly pick one here
        const providers = ["oracle", "ibm", "microsoft", "mlh", "devpost"];

        const success = await onSave(interestList, selectedTypes, providers);

        setIsSaving(false);
        if (success) {
            toast({
                title: "Preferences Saved",
                description: "Your recommendations will be personalized based on your choices.",
            });
            onOpenChange(false);
        } else {
            toast({
                title: "Error",
                description: "Failed to save preferences. Please try again.",
                variant: "destructive"
            });
        }
    };

    return (
        <Dialog open={isOpen} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[600px]">
                <DialogHeader>
                    <DialogTitle className="text-2xl font-bold text-center">Welcome to HackerHub!</DialogTitle>
                    <DialogDescription className="text-center text-lg">
                        Let's personalize your experience. What are you looking for?
                    </DialogDescription>
                </DialogHeader>

                <div className="grid gap-6 py-4">
                    <div className="space-y-4">
                        <Label className="text-base font-semibold">I'm interested in...</Label>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <Card
                                className={`p-4 cursor-pointer transition-all hover:border-primary flex flex-col items-center gap-3 text-center ${selectedTypes.includes('HACKATHON') ? 'border-primary ring-2 ring-primary/20 bg-primary/5' : ''}`}
                                onClick={() => toggleType('HACKATHON')}
                            >
                                <Code className="w-8 h-8 text-primary" />
                                <span className="font-medium">Hackathons</span>
                                {selectedTypes.includes('HACKATHON') && <Check className="w-4 h-4 absolute top-2 right-2 text-primary" />}
                            </Card>

                            <Card
                                className={`p-4 cursor-pointer transition-all hover:border-primary flex flex-col items-center gap-3 text-center ${selectedTypes.includes('CERTIFICATION') ? 'border-primary ring-2 ring-primary/20 bg-primary/5' : ''}`}
                                onClick={() => toggleType('CERTIFICATION')}
                            >
                                <Award className="w-8 h-8 text-primary" />
                                <span className="font-medium">Certifications</span>
                                {selectedTypes.includes('CERTIFICATION') && <Check className="w-4 h-4 absolute top-2 right-2 text-primary" />}
                            </Card>

                            <Card
                                className={`p-4 cursor-pointer transition-all hover:border-primary flex flex-col items-center gap-3 text-center ${selectedTypes.includes('COURSE') ? 'border-primary ring-2 ring-primary/20 bg-primary/5' : ''}`}
                                onClick={() => toggleType('COURSE')}
                            >
                                <GraduationCap className="w-8 h-8 text-primary" />
                                <span className="font-medium">Courses</span>
                                {selectedTypes.includes('COURSE') && <Check className="w-4 h-4 absolute top-2 right-2 text-primary" />}
                            </Card>
                        </div>
                    </div>

                    <div className="space-y-4">
                        <Label htmlFor="keywords" className="text-base font-semibold">Specific Topics (optional)</Label>
                        <Input
                            id="keywords"
                            placeholder="e.g. Java, Python, Artificial Intelligence, Web3"
                            value={keywords}
                            onChange={(e) => setKeywords(e.target.value)}
                            className="text-lg py-6"
                        />
                        <p className="text-sm text-muted-foreground">Separate topics with commas.</p>
                    </div>
                </div>

                <DialogFooter className="sm:justify-center">
                    <Button size="lg" className="w-full sm:w-auto min-w-[200px]" onClick={handleSave} disabled={isSaving}>
                        {isSaving ? "Saving..." : "Start Exploring"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
