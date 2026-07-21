import React, { useEffect, useState, useRef } from 'react';
import { Bell, CheckCircle2, XCircle, CreditCard, Calendar, Award, Check } from 'lucide-react';
import { useNotificationStore } from '../../store/useNotificationStore';
import { Link } from 'react-router-dom';

export const NotificationDropdown: React.FC = () => {
  const { notifications, unreadCount, fetchNotifications, markAllAsRead } = useNotificationStore();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(() => {
      fetchNotifications();
    }, 15000); // Polling suave cada 15 segundos

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'PAYMENT_APPROVED':
        return <CheckCircle2 size={18} style={{ color: 'var(--success)' }} />;
      case 'PAYMENT_REJECTED':
        return <XCircle size={18} style={{ color: 'var(--danger)' }} />;
      case 'VOUCHER_SUBMITTED':
        return <CreditCard size={18} style={{ color: 'var(--warning)' }} />;
      case 'EVENT_PUBLISHED':
        return <Calendar size={18} style={{ color: 'var(--primary)' }} />;
      case 'CREDENTIAL_ISSUED':
        return <Award size={18} style={{ color: 'var(--success)' }} />;
      default:
        return <Bell size={18} style={{ color: 'var(--primary)' }} />;
    }
  };

  return (
    <div ref={dropdownRef} style={{ position: 'relative' }}>
      {/* Botón Campana con Badge */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        style={{
          position: 'relative',
          padding: '0.5rem',
          borderRadius: 'var(--radius-md)',
          backgroundColor: isOpen ? 'var(--primary-light)' : 'transparent',
          color: isOpen ? 'var(--primary)' : 'var(--text-main)',
          border: 'none',
          cursor: 'pointer',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          transition: 'all var(--transition-fast)',
        }}
        title="Centro de Notificaciones"
      >
        <Bell size={20} />
        {unreadCount > 0 && (
          <span
            style={{
              position: 'absolute',
              top: '2px',
              right: '2px',
              backgroundColor: 'var(--danger)',
              color: '#fff',
              borderRadius: '9999px',
              width: '18px',
              height: '18px',
              fontSize: '0.688rem',
              fontWeight: 800,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 0 0 2px var(--bg-surface)',
            }}
          >
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {/* Menú Desplegable Flotante */}
      {isOpen && (
        <div
          className="glass-card"
          style={{
            position: 'absolute',
            right: 0,
            top: 'calc(100% + 0.5rem)',
            width: '360px',
            backgroundColor: 'var(--bg-surface)',
            boxShadow: 'var(--shadow-xl)',
            borderRadius: 'var(--radius-lg)',
            zIndex: 1100,
            overflow: 'hidden',
          }}
        >
          <div
            style={{
              padding: '0.875rem 1.25rem',
              borderBottom: '1px solid var(--border-color)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <div style={{ fontWeight: 800, fontSize: '0.938rem' }}>Notificaciones</div>
            {unreadCount > 0 && (
              <button
                onClick={markAllAsRead}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--primary)',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.25rem',
                }}
              >
                <Check size={14} /> Marcar leídas
              </button>
            )}
          </div>

          <div style={{ maxHeight: '360px', overflowY: 'auto' }}>
            {notifications.length === 0 ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                Sin notificaciones por el momento
              </div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  style={{
                    padding: '0.875rem 1.25rem',
                    borderBottom: '1px solid var(--border-color)',
                    backgroundColor: n.isRead ? 'transparent' : 'var(--primary-light)',
                    display: 'flex',
                    gap: '0.75rem',
                    transition: 'background-color var(--transition-fast)',
                  }}
                >
                  <div style={{ marginTop: '0.125rem', flexShrink: 0 }}>{getNotificationIcon(n.type)}</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 700, fontSize: '0.813rem', marginBottom: '0.125rem' }}>{n.title}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.375rem', lineHeight: 1.35 }}>
                      {n.message}
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <span style={{ fontSize: '0.688rem', color: 'var(--text-muted)' }}>
                        {new Date(n.createdAt).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}
                      </span>
                      {n.link && (
                        <Link
                          to={n.link}
                          onClick={() => setIsOpen(false)}
                          style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--primary)' }}
                        >
                          Ver detalle →
                        </Link>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
