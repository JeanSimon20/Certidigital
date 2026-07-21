import { create } from 'zustand';
import { notificationService, NotificationItem } from '../features/notifications/services/notification.service';

interface NotificationState {
  notifications: NotificationItem[];
  unreadCount: number;
  isLoading: boolean;
  fetchNotifications: () => Promise<void>;
  markAllAsRead: () => Promise<void>;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],
  unreadCount: 0,
  isLoading: false,

  fetchNotifications: async () => {
    try {
      set({ isLoading: true });
      const data = await notificationService.getUserNotifications();
      const unread = data.filter((n) => !n.isRead).length;
      set({ notifications: data, unreadCount: unread });
    } catch (err) {
      console.error('Error al obtener notificaciones:', err);
    } finally {
      set({ isLoading: false });
    }
  },

  markAllAsRead: async () => {
    try {
      await notificationService.markAllAsRead();
      const updated = get().notifications.map((n) => ({ ...n, isRead: true }));
      set({ notifications: updated, unreadCount: 0 });
    } catch (err) {
      console.error('Error al marcar notificaciones como leídas:', err);
    }
  },
}));
