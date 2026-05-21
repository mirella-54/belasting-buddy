export interface User {
  id: string;
  username: string;
  email: string;
}

export interface Project {
  id: string;
  userId: string;
  name: string;
  description: string;
  createdDate: string;
  archived: boolean;
}

export interface TimeEntry {
  id: string;
  projectId: string;
  date: string;
  minutes: number;
  description: string;
  source: string;
}

export interface HoursSummary {
  entries: TimeEntry[];
  totalMinutes: number;
  totalHours: number;
}
