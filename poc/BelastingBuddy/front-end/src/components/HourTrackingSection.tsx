import { useState, useEffect, useCallback } from "react";
import { useAuth } from "@/contexts/AuthContext";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Clock, Download, RefreshCw, Link2, Trash2 } from "lucide-react";
import { api } from "@/api";
import { toast } from "sonner";

interface Project {
  id: string;
  name: string;
  totalHours?: number;
}

interface TimeEntry {
  id?: string;
  date: string;
  minutes: number;
  description: string;
  source?: string;
}

interface HoursSummary {
  entries: TimeEntry[];
  totalMinutes: number;
  totalHours?: number;
}

interface ThirdPartyAccount {
  id: string;
  provider: string;
  authToken: string;
  active: boolean;
  connectedDate?: string;
  lastSyncDate?: string;
}

export function HourTrackingSection() {
  const { user } = useAuth();
  const [selectedProject, setSelectedProject] = useState<string | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [hours, setHours] = useState<HoursSummary | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [newProjectName, setNewProjectName] = useState("");
  const [newProjectDesc, setNewProjectDesc] = useState("");
  const [newHours, setNewHours] = useState("");
  const [date, setDate] = useState(new Date().toISOString().split("T")[0]);
  const [description, setDescription] = useState("");
  
  // Third-party integration state
  const [thirdPartyAccounts, setThirdPartyAccounts] = useState<ThirdPartyAccount[]>([]);
  const [showAddAccount, setShowAddAccount] = useState(false);
  const [newAccountProvider, setNewAccountProvider] = useState<"clockify" | "clickup">("clockify");
  const [newAccountToken, setNewAccountToken] = useState("");
  const [isSyncing, setIsSyncing] = useState(false);

  const fetchProjects = useCallback(async () => {
    if (!user?.id) return;
    try {
      setIsLoading(true);
      const data = await api.getProjects(user.id);
      setProjects(data);
    } catch (error) {
      toast.error("Failed to fetch projects");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  }, [user?.id]);

  const fetchHours = useCallback(async () => {
    if (!selectedProject) return;
    try {
      const data = await api.getHours(selectedProject);
      setHours(data);
    } catch (error) {
      console.error(error);
      setHours(null);
      toast.error("Failed to fetch hours");
    }
  }, [selectedProject]);

  const fetchThirdPartyAccounts = useCallback(async () => {
    if (!user?.id) return;
    try {
      const accounts = await api.getThirdPartyAccounts(user.id);
      setThirdPartyAccounts(accounts);
    } catch (error) {
      console.error("Failed to fetch third-party accounts:", error);
    }
  }, [user?.id]);

  // Fetch projects and accounts on mount
  useEffect(() => {
    fetchProjects();
    fetchThirdPartyAccounts();
  }, [fetchProjects, fetchThirdPartyAccounts]);

  // Fetch hours when project is selected
  useEffect(() => {
    fetchHours();
  }, [fetchHours]);

  const handleCreateProject = async () => {
    if (!newProjectName.trim()) {
      toast.error("Project name is required");
      return;
    }

    try {
      setIsLoading(true);
      const newProject = await api.createProject(
        user!.id,
        newProjectName,
        newProjectDesc
      );
      setProjects([...projects, newProject]);
      setNewProjectName("");
      setNewProjectDesc("");
      toast.success("Project created!");
    } catch (error) {
      toast.error("Failed to create project");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogHours = async () => {
    if (!selectedProject || !newHours || !date) {
      toast.error("Please fill in all fields");
      return;
    }

    try {
      setIsLoading(true);
      await api.logHours(selectedProject, date, parseFloat(newHours), description);
      setNewHours("");
      setDescription("");
      setDate(new Date().toISOString().split("T")[0]);
      toast.success("Hours logged!");
      // Refresh hours display
      await fetchHours();
    } catch (error) {
      toast.error("Failed to log hours");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleExport = async () => {
    if (!selectedProject) {
      toast.error("Please select a project");
      return;
    }
    try {
      setIsLoading(true);
      await api.exportHours(selectedProject, "csv");
      toast.success("Hours exported successfully!");
    } catch (error) {
      toast.error("Failed to export hours");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleExportAll = async () => {
    if (!user?.id) {
      toast.error("Not authenticated");
      return;
    }
    try {
      setIsLoading(true);
      await api.exportAllHours(user.id, "csv");
      toast.success("All hours exported successfully!");
    } catch (error) {
      toast.error("Failed to export all hours");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddAccount = async () => {
    if (!user?.id || !newAccountToken.trim()) {
      toast.error("Please provide an API token");
      return;
    }
    try {
      setIsLoading(true);
      await api.addThirdPartyAccount(user.id, newAccountProvider, newAccountToken);
      toast.success(`${newAccountProvider} account connected!`);
      setNewAccountToken("");
      setShowAddAccount(false);
      await fetchThirdPartyAccounts();
    } catch (error) {
      toast.error("Failed to connect account");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteAccount = async (accountId: string) => {
    try {
      setIsLoading(true);
      await api.deleteThirdPartyAccount(accountId);
      toast.success("Account disconnected");
      await fetchThirdPartyAccounts();
    } catch (error) {
      toast.error("Failed to disconnect account");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSync = async (provider: string) => {
    if (!user?.id) return;
    try {
      setIsSyncing(true);
      const result = await api.syncHours(provider, user.id);
      if (result.status === "SUCCESS") {
        toast.success(`Synced entries from ${provider}`);
        await fetchProjects();
        if (selectedProject) await fetchHours();
      } else {
        toast.error(result.message || "Sync failed");
      }
    } catch (error) {
      toast.error(`Failed to sync from ${provider}`);
      console.error(error);
    } finally {
      setIsSyncing(false);
    }
  };

  const handleDeleteProject = async (projectId: string) => {
    if (!confirm("Are you sure you want to delete this project and all its hours?")) {
      return;
    }
    try {
      setIsLoading(true);
      await api.deleteProject(projectId);
      toast.success("Project deleted");
      if (selectedProject === projectId) {
        setSelectedProject(null);
        setHours(null);
      }
      await fetchProjects();
    } catch (error) {
      toast.error("Failed to delete project");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const totalHours = hours?.totalMinutes ? (hours.totalMinutes / 60).toFixed(1) : "0";

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <Clock className="size-5" />
          <CardTitle>Hour Tracking</CardTitle>
        </div>
        <CardDescription>Track hours for freelance projects</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Third-Party Integrations Section */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <h4 className="text-sm font-medium">Third-Party Integrations</h4>
            <Button 
              size="sm" 
              variant="outline" 
              onClick={() => setShowAddAccount(!showAddAccount)}
              disabled={isLoading}
            >
              <Link2 className="size-4 mr-1" />
              Connect Account
            </Button>
          </div>

          {showAddAccount && (
            <div className="p-4 border rounded-md bg-muted/20 space-y-3">
              <div className="space-y-2">
                <label className="text-sm font-medium">Provider</label>
                <select
                  className="w-full p-2 border rounded-md"
                  value={newAccountProvider}
                  onChange={(e) => setNewAccountProvider(e.target.value as "clockify" | "clickup")}
                  disabled={isLoading}
                >
                  <option value="clockify">Clockify</option>
                  <option value="clickup">ClickUp</option>
                </select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">
                  {newAccountProvider === "clockify" ? "API Key" : "API Token:Team ID (format: token:teamId)"}
                </label>
                <Input
                  type="password"
                  placeholder={newAccountProvider === "clockify" ? "Your Clockify API key" : "pk_xxx:123456"}
                  value={newAccountToken}
                  onChange={(e) => setNewAccountToken(e.target.value)}
                  disabled={isLoading}
                />
              </div>
              <div className="flex gap-2">
                <Button
                  size="sm"
                  onClick={handleAddAccount}
                  disabled={isLoading || !newAccountToken.trim()}
                >
                  Connect
                </Button>
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => {
                    setShowAddAccount(false);
                    setNewAccountToken("");
                  }}
                  disabled={isLoading}
                >
                  Cancel
                </Button>
              </div>
            </div>
          )}

          <div className="space-y-2"></div>

          <div className="grid gap-2">
            {thirdPartyAccounts.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-2">
                No accounts connected
              </p>
            ) : (
              thirdPartyAccounts.map((account) => (
                <div
                  key={account.id}
                  className="flex items-center justify-between p-3 border rounded-md"
                >
                  <div className="flex items-center gap-3">
                    <div className="size-8 rounded-full bg-primary/10 flex items-center justify-center">
                      <Link2 className="size-4" />
                    </div>
                    <div>
                      <div className="font-medium capitalize">{account.provider}</div>
                      {account.lastSyncDate && (
                        <div className="text-xs text-muted-foreground">
                          Last sync: {new Date(account.lastSyncDate).toLocaleString()}
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => handleSync(account.provider)}
                      disabled={isSyncing || isLoading}
                    >
                      <RefreshCw className={`size-4 ${isSyncing ? "animate-spin" : ""}`} />
                      Sync
                    </Button>
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => handleDeleteAccount(account.id)}
                      disabled={isLoading}
                    >
                      <Trash2 className="size-4" />
                    </Button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="grid md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h4 className="text-sm font-medium">Projects</h4>
              <div className="flex items-center gap-2">
                <Button size="sm" variant="ghost" onClick={handleExportAll} disabled={isLoading}>
                  <Download className="size-4" />
                  Export All
                </Button>
              </div>
            </div>

            <div className="space-y-2 mb-4">
              <Input
                placeholder="New project name"
                value={newProjectName}
                onChange={(e) => setNewProjectName(e.target.value)}
                disabled={isLoading}
              />
              <Input
                placeholder="Description (optional)"
                value={newProjectDesc}
                onChange={(e) => setNewProjectDesc(e.target.value)}
                disabled={isLoading}
              />
              <Button
                size="sm"
                className="w-full"
                onClick={handleCreateProject}
                disabled={isLoading}
              >
                Create Project
              </Button>
            </div>

            <div className="space-y-2">
              {projects.map((project) => (
                <div
                  key={project.id}
                  className={`flex items-center gap-2 p-3 rounded-md border transition-colors ${
                    selectedProject === project.id
                      ? "border-primary bg-primary/10"
                      : "hover:bg-muted/50"
                  }`}
                >
                  <button
                    onClick={() => setSelectedProject(project.id)}
                    className="flex-1 text-left"
                  >
                    <div className="font-medium">{project.name}</div>
                    {selectedProject === project.id && hours && (
                      <div className="text-sm text-muted-foreground">
                        {totalHours}h logged
                      </div>
                    )}
                  </button>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => handleDeleteProject(project.id)}
                    disabled={isLoading}
                    className="shrink-0"
                  >
                    <Trash2 className="size-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </div>
          </div>

          <div className="space-y-3">
            <h4 className="text-sm font-medium">Log Hours</h4>
            <div className="space-y-2 p-4 border rounded-md bg-muted/20">
              <div className="grid grid-cols-2 gap-2">
                <Input
                  type="date"
                  value={date}
                  onChange={(e) => setDate(e.target.value)}
                  disabled={isLoading}
                />
                <Input
                  type="number"
                  step="0.5"
                  placeholder="Hours"
                  value={newHours}
                  onChange={(e) => setNewHours(e.target.value)}
                  disabled={isLoading}
                />
              </div>
              <Input
                placeholder="Description (optional)"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                disabled={isLoading}
              />
              <Button
                size="sm"
                className="w-full"
                onClick={handleLogHours}
                disabled={!selectedProject || isLoading}
              >
                {isLoading ? "Logging..." : "Log Hours"}
              </Button>
            </div>

            {selectedProject && hours && hours.entries.length > 0 && (
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <h4 className="text-sm font-medium">Recent Entries</h4>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={handleExport}
                    disabled={isLoading}
                  >
                    <Download className="size-4" />
                    Export
                  </Button>
                </div>
                <div className="space-y-2 max-h-48 overflow-y-auto">
                  {hours.entries.map((entry, i) => (
                    <div
                      key={i}
                      className="p-3 border rounded-md flex justify-between text-sm"
                    >
                      <div>
                        <div className="font-medium">{entry.date}</div>
                        {entry.description && (
                          <div className="text-xs text-muted-foreground">
                            {entry.description}
                          </div>
                        )}
                      </div>
                      <div className="font-semibold">
                        {(entry.minutes / 60).toFixed(1)}h
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {selectedProject && (!hours || hours.entries.length === 0) && (
              <div className="p-4 border rounded-md text-center text-sm text-muted-foreground">
                No hours logged yet
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
