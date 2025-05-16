import axios from 'axios';
import { Patch, Notification, PerformanceData, AppSettings, ApiResponse } from '../types';

// Set the base URL for the API
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Patches API
export const patchesApi = {
  // Get all available patches
  getPatches: async (): Promise<ApiResponse<Patch[]>> => {
    try {
      const response = await api.get<ApiResponse<Patch[]>>('/patches');
      return response.data;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  },

  // Get patch details
  getPatch: async (id: string): Promise<ApiResponse<Patch>> => {
    try {
      const response = await api.get<ApiResponse<Patch>>(`/patches/${id}`);
      return response.data;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  },

  // Apply a patch
  applyPatch: async (id: string): Promise<ApiResponse<boolean>> => {
    try {
      const response = await api.post<ApiResponse<boolean>>(`/patches/${id}/apply`);
      return response.data;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  },
};

// Notifications API
export const notificationsApi = {
  // Get all notifications
  getNotifications: async (): Promise<ApiResponse<Notification[]>> => {
    try {
      const response = await api.get<ApiResponse<Notification[]>>('/notifications');
      return response.data;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  },

  // Mark notification as read
  markAsRead: async (id: string): Promise<ApiResponse<boolean>> => {
    try {
      const response = await api.post<ApiResponse<boolean>>(`/notifications/${id}/read`);
      return response.data;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  },
};

// Performance API
export const performanceApi = {
  // Get performance data
  getPerformanceData: async (): Promise<ApiResponse<PerformanceData[]>> => {
    try {
      const response = await api.get<ApiResponse<PerformanceData[]>>('/performance');
      return response.data;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  },
};

// Settings API
export const settingsApi = {
  // Get application settings
  getSettings: async (): Promise<ApiResponse<AppSettings>> => {
    try {
      const response = await api.get<ApiResponse<AppSettings>>('/settings');
      return response.data;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  },

  // Update application settings
  updateSettings: async (settings: Partial<AppSettings>): Promise<ApiResponse<AppSettings>> => {
    try {
      const response = await api.post<ApiResponse<AppSettings>>('/settings', settings);
      return response.data;
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  },
}; 