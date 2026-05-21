import { Toaster } from "@/components/ui/sonner";
import { useAuth } from "@/contexts/AuthContext";
import { AuthPage } from "@/pages/AuthPage";
import { HourTrackingSection } from "@/components/HourTrackingSection";
import { DocumentSection } from "@/components/DocumentSection";
import { ArchiveSection } from "@/components/ArchiveSection";
import { ExportSection } from "@/components/ExportSection";
import { Button } from "@/components/ui/button";
import { LogOut } from "lucide-react";

function AppContent() {
  const { user, logout, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return <AuthPage />;
  }

  return (
    <div className="min-h-screen bg-background w-full">
      <Toaster />

      <header className="border-b sticky top-0 bg-background/95 backdrop-blur z-10">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold">BelastingBuddy</h1>
              <p className="text-sm text-muted-foreground">
                Tax management for Dutch freelancers
              </p>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-right">
                <p className="text-sm font-medium">{user.username}</p>
                <p className="text-xs text-muted-foreground">Welcome back!</p>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={logout}
                className="gap-2"
              >
                <LogOut className="size-4" />
                Logout
              </Button>
            </div>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <div className="grid lg:grid-cols-2 gap-6">
          <div className="space-y-6">
            <HourTrackingSection />
            <ArchiveSection />
          </div>

          <div className="space-y-6">
            <DocumentSection />
            <ExportSection />
          </div>
        </div>
      </main>
    </div>
  );
}

export default AppContent;
