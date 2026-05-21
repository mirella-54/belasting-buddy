// Service URLs via Vite proxy (dev only)
const USER_MGMT_URL = "/um";
const DOC_PROCESSING_URL = "/dp";
const HOUR_TRACKING_URL = "/ht";
const DOC_STORAGE_URL = "/ds";

// Token management
let accessToken: string | null = null;

const getAuthHeader = (): Record<string, string> =>
  accessToken ? { Authorization: `Bearer ${accessToken}` } : {};

const handleResponse = async (res: Response) => {
  if (res.status === 401) {
    accessToken = null;
    localStorage.removeItem("accessToken");
    localStorage.removeItem("user");
    window.location.href = "/login";
  }
  if (!res.ok) {
    const error = await res.text();
    throw new Error(error || `HTTP ${res.status}`);
  }
  return res.json();
};

type JwtPayload = { sub?: string; preferred_username?: string; [key: string]: unknown };

function decodeJwt(token: string): JwtPayload | null {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export const api = {
  // ============ AUTH ============
  login: async (username: string, password: string) => {
    const res = await fetch(`${USER_MGMT_URL}/api/public/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    if (!res.ok) throw new Error("Login failed");
    const data = await res.json();
    const token = data.access_token as string;
    if (!token) throw new Error("No access token returned");
    accessToken = token;
    localStorage.setItem("accessToken", token);
    const payload = decodeJwt(token);
    const user = {
      id: payload?.sub ?? "",
      username: payload?.preferred_username ?? username,
    };
    localStorage.setItem("user", JSON.stringify(user));
    return data;
  },

  register: async (
    username: string,
    email: string,
    password: string,
    firstName: string,
    lastName: string
  ) => {
    const res = await fetch(`${USER_MGMT_URL}/api/public/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, email, password, firstName, lastName }),
    });
    if (!res.ok) throw new Error("Registration failed");
    return res.json();
  },

  logout: () => {
    accessToken = null;
    localStorage.removeItem("accessToken");
    localStorage.removeItem("user");
  },

  // ============ USER MANAGEMENT ============
  getUser: async (userId: string) => {
    const res = await fetch(`${USER_MGMT_URL}/api/user/${userId}`, {
      headers: { "Content-Type": "application/json", ...getAuthHeader() },
    });
    return handleResponse(res);
  },

  // ============ HOUR TRACKING ============
  getProjects: async (userId: string) => {
    const res = await fetch(`${HOUR_TRACKING_URL}/projects?userId=${userId}`, {
      headers: { ...getAuthHeader() },
    });
    return handleResponse(res);
  },

  createProject: async (userId: string, name: string, description: string) => {
    const res = await fetch(`${HOUR_TRACKING_URL}/projects`, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...getAuthHeader() },
      body: JSON.stringify({ userId, name, description }),
    });
    return handleResponse(res);
  },

  getHours: async (projectId: string) => {
    const res = await fetch(`${HOUR_TRACKING_URL}/hours/${projectId}`, {
      headers: { ...getAuthHeader() },
    });
    return handleResponse(res);
  },

  logHours: async (
    projectId: string,
    day: string,
    hours: number,
    description: string
  ) => {
    const res = await fetch(`${HOUR_TRACKING_URL}/hours/${projectId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json", ...getAuthHeader() },
      body: JSON.stringify({ day, hours, description }),
    });
    return handleResponse(res);
  },

  deleteProject: async (projectId: string) => {
    const res = await fetch(`${HOUR_TRACKING_URL}/projects/${projectId}`, {
      method: "DELETE",
      headers: { ...getAuthHeader() },
    });
    if (!res.ok) throw new Error("Failed to delete project");
    return;
  },

  exportHours: async (projectId: string, format: "csv" | "json") => {
    const res = await fetch(
      `${HOUR_TRACKING_URL}/hours/${projectId}/export?format=${format}`,
      {
        headers: { ...getAuthHeader() },
      }
    );
    if (!res.ok) throw new Error("Export failed");
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `hours_export.${format}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },

  exportAllHours: async (userId: string, format: "csv" | "json") => {
    const res = await fetch(
      `${HOUR_TRACKING_URL}/hours/export-all?userId=${userId}&format=${format}`,
      {
        headers: { ...getAuthHeader() },
      }
    );
    if (!res.ok) throw new Error("Export failed");
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `hours_export_all.${format}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },

  // ============ DOCUMENT PROCESSING ============
  uploadDocument: async (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    const res = await fetch(`${DOC_PROCESSING_URL}/documents/upload`, {
      method: "POST",
      headers: { ...getAuthHeader() },
      body: formData,
    });
    if (res.status === 401) {
      accessToken = null;
      localStorage.removeItem("accessToken");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    if (!res.ok) {
      const error = await res.text();
      throw new Error(error || `HTTP ${res.status}`);
    }
    // Return the text response, not JSON
    return res.text();
  },

  getDocuments: async (userId: string) => {
    const res = await fetch(`${DOC_STORAGE_URL}/api/files-archive/list?userID=${userId}`, {
      headers: { ...getAuthHeader() },
    });
    return handleResponse(res);
  },

  deleteAllDocuments: async (userId: string) => {
    const res = await fetch(`${DOC_STORAGE_URL}/api/files-archive/delete-all?userID=${userId}`, {
      method: "POST",
      headers: { ...getAuthHeader() },
    });
    if (res.status === 401) {
      accessToken = null;
      localStorage.removeItem("accessToken");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    if (!res.ok) {
      const error = await res.text();
      throw new Error(error || `HTTP ${res.status}`);
    }
    return res.text();
  },

  // ============ THIRD-PARTY ACCOUNTS ============
  getThirdPartyAccounts: async (userId: string) => {
    const res = await fetch(`${HOUR_TRACKING_URL}/third-party-accounts?userId=${userId}`, {
      headers: { ...getAuthHeader() },
    });
    return handleResponse(res);
  },

  addThirdPartyAccount: async (userId: string, provider: string, authToken: string) => {
    const res = await fetch(`${HOUR_TRACKING_URL}/third-party-accounts`, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...getAuthHeader() },
      body: JSON.stringify({ userId, provider, authToken, active: true }),
    });
    return handleResponse(res);
  },

  deleteThirdPartyAccount: async (accountId: string) => {
    const res = await fetch(`${HOUR_TRACKING_URL}/third-party-accounts/${accountId}`, {
      method: "DELETE",
      headers: { ...getAuthHeader() },
    });
    if (!res.ok) throw new Error("Failed to delete account");
    return;
  },

  syncHours: async (provider: string, userId: string, startDate?: string, endDate?: string) => {
    let url = `${HOUR_TRACKING_URL}/hours/sync/${provider}?userId=${userId}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    
    const res = await fetch(url, {
      method: "POST",
      headers: { ...getAuthHeader() },
    });
    return handleResponse(res);
  },

  // ============ DOCUMENT STORAGE ============
  archiveDocument: async (fileId: string) => {
    const res = await fetch(`${DOC_STORAGE_URL}/api/files-archive`, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...getAuthHeader() },
      body: JSON.stringify({ fileId }),
    });
    return handleResponse(res);
  },

  getArchivedDocuments: async () => {
    const res = await fetch(`${DOC_STORAGE_URL}/api/files-archive`, {
      headers: { ...getAuthHeader() },
    });
    return handleResponse(res);
  },

  // ============ UTILITIES ============
  isAuthenticated: () => !!accessToken || !!localStorage.getItem("accessToken"),

  restoreToken: () => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      accessToken = token;
    }
  },
};
