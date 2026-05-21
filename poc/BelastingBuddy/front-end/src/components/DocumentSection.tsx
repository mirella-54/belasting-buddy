import { useState, useEffect, useRef } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { FileText, Upload, Check, X, Trash2 } from "lucide-react";
import { api } from "@/api";
import { toast } from "sonner";

interface Document {
  id: string;
  name: string;
  type: string;
  date: string;
  amount: string;
  vat: string;
  status: "Processed" | "Error" | "Processing";
}

interface FileMetadata {
  fileName: string;
  contentType: string;
  storageUrl: string;
  uploadTimestamp: number;
  content: string;
  userID: string;
  amount?: string;
  vat?: string;
}

interface UploadingFile {
  file: File;
  status: "pending" | "uploading" | "success" | "error";
  error?: string;
}

export function DocumentSection() {
  const [selectedFile, setSelectedFile] = useState<string | null>(null);
  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);
  const [documents, setDocuments] = useState<Document[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isFetching, setIsFetching] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  // Ref to prevent setting state after unmount
  const isMounted = useRef(true);

  // Fetch documents every second
  useEffect(() => {
    isMounted.current = true;

    const fetchDocuments = async () => {
      try {
        setIsFetching(true);
        const user = localStorage.getItem("user");
        if (!user) return;

        const userId = JSON.parse(user).id;
        const data = await api.getDocuments(userId);

        // Transform backend data to frontend format
        const transformedDocs: Document[] = (data as FileMetadata[]).map((doc, index) => {
          const date = new Date(doc.uploadTimestamp);

          // Use plaintext amount and VAT fields from metadata
          const amount = doc.amount || "N/A";
          
          // Calculate VAT from total if not provided (21% VAT included in total)
          let vat = doc.vat;
          if (!vat && doc.amount) {
            try {
              // Remove currency symbols and parse
              const cleanAmount = doc.amount.replace(/[^0-9.]/g, '');
              const totalAmount = parseFloat(cleanAmount);
              
              if (!isNaN(totalAmount)) {
                // VAT = Total × (21 / 121)
                const vatAmount = totalAmount * (21.0 / 121.0);
                vat = `$${vatAmount.toFixed(2)}`;
              } else {
                vat = "21%";
              }
            } catch {
              vat = "21%";
            }
          } else if (!vat) {
            vat = "21%";
          }
          
          // Determine status based on whether data has been extracted
          const status: "Processed" | "Error" | "Processing" = 
            doc.amount ? "Processed" : "Processing";

          // Create a unique ID using fileName and timestamp
          const uniqueId = `${doc.fileName}-${doc.uploadTimestamp}-${index}`;

          return {
            id: uniqueId,
            name: doc.fileName,
            type: doc.contentType.split("/")[1]?.toUpperCase() || "UNKNOWN",
            date: date.toLocaleDateString(),
            amount,
            vat,
            status,
          };
        });

        if (isMounted.current) setDocuments(transformedDocs);
      } catch (error) {
        if (isMounted.current) {
          console.error("Failed to fetch documents:", error);
          toast.error("Failed to load documents");
        }
      } finally {
        if (isMounted.current) setIsFetching(false);
      }
    };

    fetchDocuments();
    const interval = setInterval(fetchDocuments, 1000);

    return () => {
      isMounted.current = false;
      clearInterval(interval);
    };
  }, []);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    const validFiles: UploadingFile[] = [];
    const errors: string[] = [];

    Array.from(files).forEach((file) => {
      // Validate file type
      if (!file.type.startsWith("image/") && file.type !== "application/pdf") {
        errors.push(`${file.name}: Only PDF and image files are allowed`);
        return;
      }

      // Validate file size (10MB)
      if (file.size > 10 * 1024 * 1024) {
        errors.push(`${file.name}: File size must be less than 10MB`);
        return;
      }

      validFiles.push({ file, status: "pending" });
    });

    if (errors.length > 0) {
      errors.forEach((error) => toast.error(error));
    }

    if (validFiles.length > 0) {
      setUploadingFiles((prev) => [...prev, ...validFiles]);
    }
  };

  const handleUpload = async () => {
    if (uploadingFiles.length === 0) {
      toast.error("Please select files");
      return;
    }

    setIsLoading(true);

    // Upload each file separately
    for (let i = 0; i < uploadingFiles.length; i++) {
      const uploadingFile = uploadingFiles[i];
      
      if (uploadingFile.status !== "pending") continue;

      // Update status to uploading
      setUploadingFiles((prev) =>
        prev.map((f, idx) =>
          idx === i ? { ...f, status: "uploading" } : f
        )
      );

      try {
        await api.uploadDocument(uploadingFile.file);
        
        // Update status to success
        setUploadingFiles((prev) =>
          prev.map((f, idx) =>
            idx === i ? { ...f, status: "success" } : f
          )
        );
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Upload failed";
        
        // Update status to error
        setUploadingFiles((prev) =>
          prev.map((f, idx) =>
            idx === i ? { ...f, status: "error", error: errorMessage } : f
          )
        );
      }
    }

    const successCount = uploadingFiles.filter(f => f.status === "success").length;
    const errorCount = uploadingFiles.filter(f => f.status === "error").length;
    
    if (successCount > 0) {
      toast.success(`${successCount} document(s) uploaded for processing!`);
    }
    if (errorCount > 0) {
      toast.error(`${errorCount} document(s) failed to upload`);
    }

    // Clear successfully uploaded files after a delay
    setTimeout(() => {
      setUploadingFiles((prev) => prev.filter((f) => f.status === "error"));
    }, 2000);

    // Reset file input
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (input) input.value = "";

    setIsLoading(false);
  };

  const handleClearAll = async () => {
    console.log("Clear All clicked, documents count:", documents.length);
    if (documents.length === 0) {
      toast.error("No documents to delete");
      return;
    }

    // Confirm before deleting
    if (!window.confirm(`Are you sure you want to delete all ${documents.length} document(s)? This action cannot be undone.`)) {
      console.log("User cancelled deletion");
      return;
    }

    try {
      setIsDeleting(true);
      const user = localStorage.getItem("user");
      if (!user) {
        toast.error("User not found");
        return;
      }

      const userId = JSON.parse(user).id;
      console.log("Calling deleteAllDocuments for user:", userId);
      const response = await api.deleteAllDocuments(userId);
      console.log("Delete response:", response);
      toast.success(response);
      
      // Clear local state immediately
      setDocuments([]);
      setSelectedFile(null);
    } catch (error) {
      console.error("Delete error:", error);
      toast.error(error instanceof Error ? error.message : "Failed to delete documents");
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <FileText className="size-5" />
          <CardTitle>Document Processing</CardTitle>
        </div>
        <CardDescription>
          Upload invoices and receipts for automatic data extraction
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="border-2 border-dashed rounded-lg p-8 text-center hover:bg-muted/50 transition-colors cursor-pointer">
          <label className="cursor-pointer">
            <Upload className="size-8 mx-auto mb-2 text-muted-foreground" />
            <p className="text-sm font-medium">Upload documents</p>
            <p className="text-xs text-muted-foreground">
              PDF, PNG, JPG up to 10MB
            </p>
            <input
              type="file"
              className="hidden"
              accept=".pdf,.png,.jpg,.jpeg"
              onChange={handleFileSelect}
              disabled={isLoading}
              multiple
            />
          </label>
        </div>

        {uploadingFiles.length > 0 && (
          <div className="p-4 border rounded-md bg-muted/20 space-y-3">
            <div className="space-y-2">
              {uploadingFiles.map((uploadingFile, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between text-sm"
                >
                  <div className="flex items-center gap-2 flex-1 min-w-0">
                    <FileText className="size-4 flex-shrink-0" />
                    <span className="truncate">{uploadingFile.file.name}</span>
                  </div>
                  <div className="flex-shrink-0 ml-2">
                    {uploadingFile.status === "pending" && (
                      <span className="text-xs text-muted-foreground">Ready</span>
                    )}
                    {uploadingFile.status === "uploading" && (
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-primary" />
                    )}
                    {uploadingFile.status === "success" && (
                      <Check className="size-4 text-green-600" />
                    )}
                    {uploadingFile.status === "error" && (
                      <X className="size-4 text-red-500" />
                    )}
                  </div>
                </div>
              ))}
            </div>
            <div className="flex gap-2">
              <Button
                size="sm"
                className="flex-1"
                onClick={handleUpload}
                disabled={isLoading || uploadingFiles.every(f => f.status !== "pending")}
              >
                {isLoading ? "Uploading..." : `Upload ${uploadingFiles.filter(f => f.status === "pending").length} File(s)`}
              </Button>
              {!isLoading && (
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setUploadingFiles([])}
                >
                  Clear
                </Button>
              )}
            </div>
          </div>
        )}

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <h4 className="text-sm font-medium">Recent Documents ({documents.length})</h4>
            {documents.length > 0 && (
              <Button
                size="sm"
                variant="destructive"
                onClick={handleClearAll}
                disabled={isDeleting}
                className="h-7 text-xs"
              >
                <Trash2 className="size-3 mr-1" />
                {isDeleting ? "Deleting..." : "Clear All"}
              </Button>
            )}
          </div>
          <div className="space-y-2 max-h-[300px] overflow-y-auto">
            {isFetching && !documents ? (
              <p className="text-xs text-muted-foreground text-center py-4">
                Loading documents...
              </p>
            ) : documents.length === 0 ? (
              <p className="text-xs text-muted-foreground text-center py-4">
                No documents uploaded yet
              </p>
            ) : (
              documents.map((doc) => (
                <button
                  key={doc.id}
                  onClick={() => setSelectedFile(doc.id)}
                  className={`w-full text-left p-4 rounded-md border transition-colors ${
                    selectedFile === doc.id
                      ? "border-primary bg-primary/10"
                      : "hover:bg-muted/50"
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <FileText className="size-4" />
                        <span className="font-medium text-sm">{doc.name}</span>
                      </div>
                      <div className="text-xs text-muted-foreground mt-1">
                        {doc.type} - {doc.date}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="font-semibold">{doc.amount}</div>
                      <div className="text-xs text-muted-foreground">
                        VAT: {doc.vat}
                      </div>
                    </div>
                  </div>
                  {doc.status === "Processed" && (
                    <div className="flex items-center gap-1 mt-2 text-xs text-green-600">
                      <Check className="size-3" />
                      Data extracted
                    </div>
                  )}
                  {doc.status === "Error" && (
                    <div className="flex items-center gap-1 mt-2 text-xs text-red-500/80">
                      <X className="size-3" />
                      Extraction failed
                    </div>
                  )}
                  {doc.status === "Processing" && (
                    <div className="flex items-center gap-1 mt-2 text-xs text-blue-500">
                      <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-blue-500" />
                      Processing...
                    </div>
                  )}
                </button>
              ))
            )}
          </div>
        </div>

        {selectedFile && (() => {
          const doc = documents.find((d) => d.id === selectedFile);
          return doc ? (
            <div className="p-4 border rounded-md bg-muted/20 space-y-2">
              <h4 className="text-sm font-medium">Extracted Data</h4>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div>
                  <span className="text-muted-foreground">Document:</span>
                  <p className="font-medium mt-1">{doc.name}</p>
                </div>
                <div>
                  <span className="text-muted-foreground">Type:</span>
                  <p className="font-medium mt-1">{doc.type}</p>
                </div>
                <div>
                  <span className="text-muted-foreground">Amount:</span>
                  <Input className="mt-1" defaultValue={doc.amount} />
                </div>
                <div>
                  <span className="text-muted-foreground">VAT:</span>
                  <Input className="mt-1" defaultValue={doc.vat} />
                </div>
              </div>
              <Button size="sm" variant="outline" className="w-full">
                Update Data
              </Button>
            </div>
          ) : null;
        })()}
      </CardContent>
    </Card>
  );
}
