import React, { useState, ReactNode } from 'react';
import { 
  AppBar, Box, Drawer, IconButton, Toolbar, Typography, 
  List, ListItem, ListItemIcon, ListItemText, Badge, 
  Divider, Menu, MenuItem, Avatar 
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  Build as BuildIcon,
  Settings as SettingsIcon,
  Notifications as NotificationsIcon,
  Close as CloseIcon,
} from '@mui/icons-material';
import { Link, useLocation } from 'react-router-dom';
import { Notification } from '../types';
import NotificationPanel from './NotificationPanel';

const drawerWidth = 240;

interface LayoutProps {
  children: ReactNode;
  notifications: Notification[];
}

const Layout: React.FC<LayoutProps> = ({ children, notifications }) => {
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [notificationOpen, setNotificationOpen] = useState(false);
  const [profileMenuAnchor, setProfileMenuAnchor] = useState<null | HTMLElement>(null);
  
  const unreadCount = notifications.filter(n => !n.read).length;

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleNotificationToggle = () => {
    setNotificationOpen(!notificationOpen);
  };

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setProfileMenuAnchor(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setProfileMenuAnchor(null);
  };

  const drawer = (
    <div>
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          Java Patcher
        </Typography>
        <IconButton
          color="inherit"
          aria-label="close drawer"
          edge="end"
          onClick={handleDrawerToggle}
          sx={{ display: { sm: 'none' } }}
        >
          <CloseIcon />
        </IconButton>
      </Toolbar>
      <Divider />
      <List>
        <ListItem 
          button 
          component={Link} 
          to="/" 
          selected={location.pathname === '/'}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon>
            <DashboardIcon />
          </ListItemIcon>
          <ListItemText primary="Dashboard" />
        </ListItem>
        <ListItem 
          button 
          component={Link} 
          to="/patches" 
          selected={location.pathname.startsWith('/patches')}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon>
            <BuildIcon />
          </ListItemIcon>
          <ListItemText primary="Patches" />
        </ListItem>
        <ListItem 
          button 
          component={Link} 
          to="/settings" 
          selected={location.pathname === '/settings'}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon>
            <SettingsIcon />
          </ListItemIcon>
          <ListItemText primary="Settings" />
        </ListItem>
      </List>
    </div>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Dynamic Java Patcher
          </Typography>
          <IconButton color="inherit" onClick={handleNotificationToggle}>
            <Badge badgeContent={unreadCount} color="error">
              <NotificationsIcon />
            </Badge>
          </IconButton>
          <IconButton
            onClick={handleProfileMenuOpen}
            color="inherit"
          >
            <Avatar sx={{ width: 32, height: 32 }}>A</Avatar>
          </IconButton>
          <Menu
            anchorEl={profileMenuAnchor}
            open={Boolean(profileMenuAnchor)}
            onClose={handleProfileMenuClose}
          >
            <MenuItem onClick={handleProfileMenuClose}>Profile</MenuItem>
            <MenuItem onClick={handleProfileMenuClose}>Logout</MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box
        component="main"
        sx={{ 
          flexGrow: 1, 
          p: 3, 
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          mt: '64px' // AppBar height
        }}
      >
        {children}
      </Box>
      
      <NotificationPanel 
        open={notificationOpen} 
        onClose={handleNotificationToggle} 
        notifications={notifications} 
      />
    </Box>
  );
};

export default Layout; 