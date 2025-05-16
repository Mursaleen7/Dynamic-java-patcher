import React from 'react';
import {
  Drawer,
  Box,
  Typography,
  IconButton,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  Badge,
  Chip,
} from '@mui/material';
import {
  Close as CloseIcon,
  Info as InfoIcon,
  CheckCircle as SuccessIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';
import { Notification } from '../types';
import { formatDistanceToNow } from 'date-fns';
import { notificationsApi } from '../services/api';

interface NotificationPanelProps {
  open: boolean;
  onClose: () => void;
  notifications: Notification[];
}

const NotificationPanel: React.FC<NotificationPanelProps> = ({
  open,
  onClose,
  notifications,
}) => {
  const handleMarkAsRead = async (id: string) => {
    await notificationsApi.markAsRead(id);
  };

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'info':
        return <InfoIcon color="info" />;
      case 'success':
        return <SuccessIcon color="success" />;
      case 'warning':
        return <WarningIcon color="warning" />;
      case 'error':
        return <ErrorIcon color="error" />;
      default:
        return <InfoIcon color="info" />;
    }
  };

  const getNotificationTypeLabel = (type: string) => {
    switch (type) {
      case 'info':
        return 'Information';
      case 'success':
        return 'Success';
      case 'warning':
        return 'Warning';
      case 'error':
        return 'Error';
      default:
        return 'Information';
    }
  };

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      sx={{ '& .MuiDrawer-paper': { width: { xs: '100%', sm: 400 } } }}
    >
      <Box sx={{ display: 'flex', justifyContent: 'space-between', p: 2 }}>
        <Typography variant="h6">
          Notifications
          <Badge
            badgeContent={notifications.filter((n) => !n.read).length}
            color="error"
            sx={{ ml: 1 }}
          />
        </Typography>
        <IconButton onClick={onClose}>
          <CloseIcon />
        </IconButton>
      </Box>
      <Divider />

      {notifications.length === 0 ? (
        <Box sx={{ p: 3, textAlign: 'center' }}>
          <Typography color="textSecondary">No notifications yet</Typography>
        </Box>
      ) : (
        <List sx={{ p: 0 }}>
          {notifications.map((notification) => (
            <React.Fragment key={notification.id}>
              <ListItem
                button
                onClick={() => handleMarkAsRead(notification.id)}
                alignItems="flex-start"
                sx={{
                  bgcolor: notification.read ? 'inherit' : 'action.hover',
                  p: 2,
                }}
              >
                <ListItemIcon sx={{ mt: 0 }}>
                  {getNotificationIcon(notification.type)}
                </ListItemIcon>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="subtitle1">{notification.title}</Typography>
                      <Chip
                        label={getNotificationTypeLabel(notification.type)}
                        size="small"
                        color={
                          notification.type === 'error'
                            ? 'error'
                            : notification.type === 'warning'
                            ? 'warning'
                            : notification.type === 'success'
                            ? 'success'
                            : 'info'
                        }
                        sx={{ ml: 1 }}
                      />
                    </Box>
                  }
                  secondary={
                    <>
                      <Typography
                        component="span"
                        variant="body2"
                        color="textPrimary"
                        sx={{ display: 'block' }}
                      >
                        {notification.message}
                      </Typography>
                      <Typography
                        component="span"
                        variant="caption"
                        color="textSecondary"
                      >
                        {formatDistanceToNow(new Date(notification.timestamp), {
                          addSuffix: true,
                        })}
                      </Typography>
                    </>
                  }
                />
              </ListItem>
              <Divider />
            </React.Fragment>
          ))}
        </List>
      )}
    </Drawer>
  );
};

export default NotificationPanel; 