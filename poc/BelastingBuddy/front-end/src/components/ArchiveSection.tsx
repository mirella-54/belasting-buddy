import { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Archive, Download, Trash2 } from "lucide-react";
import { api } from "@/api";
import { toast } from "sonner";

interface ArchivedDoc {
  id: string;
  name: string;
  uploadDate: string;
  size: string;
  year: string;
}

export function ArchiveSection() {
  const [archived, setArchived] = useState<ArchivedDoc[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedYear, setSelectedYear] = useState<string>("all");

  useEffect(() => {
    fetchArchived();
  }, []);

  const fetchArchived = async () => {
    try {
      setIsLoading(true);
      const data = await api.getArchivedDocuments();
      setArchived(data || []);
    } catch (error) {
      console.error(error);
      // Fail silently for archive as it may not be fully implemented
      setArchived([]);
    } finally {
      setIsLoading(false);
    }
  };

  const filteredArchived =
    selectedYear === "all"
      ? archived
      : archived.filter((doc) => doc.year === selectedYear);

  const years = Array.from(new Set(archived.map((d) => d.year))).sort().reverse();

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Archive className="size-5" />
            <CardTitle>Document Archive</CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">Loading...</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <Archive className="size-5" />
          <CardTitle>Document Archive</CardTitle>
        </div>
        <CardDescription>
          Long-term storage for tax audit compliance (7 years)
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex gap-2 flex-wrap">
          <Button
            size="sm"
            variant={selectedYear === "all" ? "default" : "outline"}
            onClick={() => setSelectedYear("all")}
          >
            All Years
          </Button>
          {years.map((year) => (
            <Button
              key={year}
              size="sm"
              variant={selectedYear === year ? "default" : "outline"}
              onClick={() => setSelectedYear(year)}
            >
              {year}
            </Button>
          ))}
        </div>

        <div className="space-y-2">
          {filteredArchived.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-4">
              No archived documents
            </p>
          ) : (
            filteredArchived.map((doc) => (
              <div
                key={doc.id}
                className="p-3 border rounded-md hover:bg-muted/50 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <div className="font-medium text-sm">{doc.name}</div>
                    <div className="text-xs text-muted-foreground">
                      {doc.uploadDate} - {doc.size}
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button size="icon-sm" variant="ghost">
                      <Download className="size-4" />
                    </Button>
                    <Button size="icon-sm" variant="ghost">
                      <Trash2 className="size-4" />
                    </Button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}
