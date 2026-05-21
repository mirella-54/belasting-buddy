import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { FileDown, Calculator } from "lucide-react";
import { api } from "@/api";
import { toast } from "sonner";

export function ExportSection() {
  const handleExportVAT = () => {
    toast.info("VAT export coming soon");
  };

  const handleExportIncomeTax = () => {
    toast.info("Income tax export coming soon");
  };

  const handleExportHours = () => {
    toast.info("Hours certificate coming soon");
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <FileDown className="size-5" />
          <CardTitle>Tax Report Export</CardTitle>
        </div>
        <CardDescription>
          Combine hours and documents for tax submission
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <Calculator className="size-4 text-muted-foreground" />
            <h4 className="text-sm font-medium">Tax Summary</h4>
          </div>

          <div className="p-4 border rounded-md bg-muted/20 space-y-3">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Total Hours (2025)</span>
              <span className="font-semibold">Calculating...</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Total Invoices</span>
              <span className="font-semibold">Calculating...</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Total Expenses</span>
              <span className="font-semibold">Calculating...</span>
            </div>
            <Separator />
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">VAT Collected</span>
              <span className="font-semibold">Calculating...</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">VAT Paid</span>
              <span className="font-semibold">Calculating...</span>
            </div>
            <Separator />
            <div className="flex justify-between">
              <span className="font-medium">Net Income</span>
              <span className="font-bold text-lg">Calculating...</span>
            </div>
          </div>
        </div>

        <div className="space-y-2">
          <h4 className="text-sm font-medium">Export Period</h4>
          <div className="grid grid-cols-2 gap-2">
            <Input type="date" defaultValue="2025-01-01" />
            <Input type="date" defaultValue="2025-12-31" />
          </div>
        </div>

        <div className="space-y-2">
          <Button
            variant="outline"
            className="w-full"
            onClick={handleExportVAT}
          >
            Export VAT Report (Quarterly)
          </Button>
          <Button
            variant="outline"
            className="w-full"
            onClick={handleExportIncomeTax}
          >
            Export Income Tax Report (Annual)
          </Button>
          <Button
            variant="outline"
            className="w-full"
            onClick={handleExportHours}
          >
            Export Hours Certificate
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
