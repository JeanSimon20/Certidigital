import { apiClient } from '../../../services/api/axios.client';
import { ApiResponse } from '../../../types/api.types';

export interface NotificationItem {
  id: string;
  type: string;
  title: string;
  message: string;
  link?: string;
  isRead: boolean;
  createdAt: string;
}

export const notificationService = {
  getUserNotifications: async (): Promise<NotificationItem[]> => {
    const response = await apiClient.get<ApiResponse<NotificationItem[]>>('/api/notifications');
    return response.data.data;
  },

  markAllAsRead: async (): Promise<void> => {
    await apiClient.post<ApiResponse<void>>('/api/notifications/read-all');
  },
};
