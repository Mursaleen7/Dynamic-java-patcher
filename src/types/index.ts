// Patch Types
export interface Patch {
  id: string;
  name: string;
  version: string;
  description: string;
  createdAt: string;
  appliedAt?: string;
  status: 'available' | 'applied' | 'failed';
  type: 'hotspot' | 'deprecation' | 'security' | 'general';
  codeChanges: CodeChange[];
}

export interface CodeChange {
  filePath: string;
  className: string;
  methodName?: string;
  lineStart: number;
  lineEnd: number;
  beforeCode: string;
  afterCode: string;
}

// Notification Types
export interface Notification {
  id: string;
  timestamp: string;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  patchId?: string;
  read: boolean;
}

// Performance Types
export interface PerformanceData {
  className: string;
  methodName: string;
  avgExecutionTimeMs: number;
  callCount: number;
  slowestMs: number;
  fastestMs: number;
}

// Application Types
export interface AppSettings {
  profilerEnabled: boolean;
  deprecationRescueEnabled: boolean;
  securityPatchesEnabled: boolean;
  monitoredPackages: string[];
  refreshIntervalSeconds: number;
}

// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
} 