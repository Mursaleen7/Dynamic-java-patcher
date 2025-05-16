import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import Dashboard from './components/Dashboard';
import PatchList from './components/PatchList';
import PatchDetail from './components/PatchDetail';
import Settings from './components/Settings';
import NotFound from './components/NotFound';
import Layout from './components/Layout';
import { webSocketService, WebSocketEvent } from './services/websocket';
import { Notification } from './types';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    fontSize: 14,
    fontWeightLight: 300,
    fontWeightRegular: 400,
    fontWeightMedium: 500,
    fontWeightBold: 700,
  },
});

function App() {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    // Initialize WebSocket
    webSocketService.init();

    // Listen for all relevant events
    const handleNotification = (data: Notification) => {
      setNotifications(prev => [data, ...prev]);
    };

    webSocketService.on(WebSocketEvent.PATCH_APPLIED, handleNotification);
    webSocketService.on(WebSocketEvent.PATCH_FAILED, handleNotification);
    webSocketService.on(WebSocketEvent.NEW_PATCH_AVAILABLE, handleNotification);
    webSocketService.on(WebSocketEvent.CODE_CHANGED, handleNotification);
    webSocketService.on(WebSocketEvent.PERFORMANCE_ALERT, handleNotification);
    webSocketService.on(WebSocketEvent.SECURITY_ALERT, handleNotification);
    webSocketService.on(WebSocketEvent.DEPRECATION_ALERT, handleNotification);

    return () => {
      // Cleanup listeners on unmount
      webSocketService.off(WebSocketEvent.PATCH_APPLIED, handleNotification);
      webSocketService.off(WebSocketEvent.PATCH_FAILED, handleNotification);
      webSocketService.off(WebSocketEvent.NEW_PATCH_AVAILABLE, handleNotification);
      webSocketService.off(WebSocketEvent.CODE_CHANGED, handleNotification);
      webSocketService.off(WebSocketEvent.PERFORMANCE_ALERT, handleNotification);
      webSocketService.off(WebSocketEvent.SECURITY_ALERT, handleNotification);
      webSocketService.off(WebSocketEvent.DEPRECATION_ALERT, handleNotification);
      webSocketService.disconnect();
    };
  }, []);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <Layout notifications={notifications}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/patches" element={<PatchList />} />
            <Route path="/patches/:id" element={<PatchDetail />} />
            <Route path="/settings" element={<Settings />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Layout>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App; 