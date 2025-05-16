import React, { useEffect, useState } from 'react';
import { 
  Box, 
  Card, 
  CardContent, 
  CardHeader, 
  Container, 
  Grid, 
  Paper, 
  Typography,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Button,
  CircularProgress,
  Chip
} from '@mui/material';
import {
  Speed as SpeedIcon,
  Build as BuildIcon,
  Security as SecurityIcon,
  Warning as WarningIcon
} from '@mui/icons-material';
import { Link } from 'react-router-dom';
import { patchesApi, performanceApi, settingsApi } from '../services/api';
import { Patch, PerformanceData, AppSettings } from '../types';

const Dashboard: React.FC = () => {
  const [patches, setPatches] = useState<Patch[]>([]);
  const [performanceData, setPerformanceData] = useState<PerformanceData[]>([]);
  const [settings, setSettings] = useState<AppSettings | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      setLoading(true);
      setError(null);
      
      try {
        // Fetch all data in parallel
        const [patchesResponse, performanceResponse, settingsResponse] = await Promise.all([
          patchesApi.getPatches(),
          performanceApi.getPerformanceData(),
          settingsApi.getSettings()
        ]);
        
        if (patchesResponse.success && patchesResponse.data) {
          setPatches(patchesResponse.data);
        } else {
          throw new Error(patchesResponse.error || 'Failed to fetch patches');
        }
        
        if (performanceResponse.success && performanceResponse.data) {
          setPerformanceData(performanceResponse.data);
        } else {
          throw new Error(performanceResponse.error || 'Failed to fetch performance data');
        }
        
        if (settingsResponse.success && settingsResponse.data) {
          setSettings(settingsResponse.data);
        } else {
          throw new Error(settingsResponse.error || 'Failed to fetch settings');
        }
      } catch (err: any) {
        setError(err.message || 'Something went wrong');
        console.error('Error fetching dashboard data:', err);
      } finally {
        setLoading(false);
      }
    };
    
    fetchDashboardData();
  }, []);

  const getTypeCounts = () => {
    const counts = {
      hotspot: 0,
      deprecation: 0,
      security: 0,
      general: 0
    };
    
    patches.forEach(patch => {
      counts[patch.type]++;
    });
    
    return counts;
  };
  
  const getStatusCounts = () => {
    const counts = {
      available: 0,
      applied: 0,
      failed: 0
    };
    
    patches.forEach(patch => {
      counts[patch.status]++;
    });
    
    return counts;
  };
  
  const typeCounts = getTypeCounts();
  const statusCounts = getStatusCounts();
  
  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress />
      </Box>
    );
  }
  
  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Paper sx={{ p: 3, bgcolor: '#fff9f9', border: '1px solid #ffdddd' }}>
          <Typography color="error" variant="h6">Error Loading Dashboard</Typography>
          <Typography color="error">{error}</Typography>
          <Button 
            variant="contained" 
            color="primary" 
            sx={{ mt: 2 }}
            onClick={() => window.location.reload()}
          >
            Retry
          </Button>
        </Paper>
      </Box>
    );
  }

  return (
    <Container maxWidth="xl">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Dashboard
        </Typography>
        <Typography variant="subtitle1" color="textSecondary">
          Overview of your Dynamic Java Patcher status
        </Typography>
      </Box>
      
      <Grid container spacing={3}>
        {/* Feature Status */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader title="Feature Status" />
            <Divider />
            <CardContent>
              <List sx={{ pt: 0 }}>
                <ListItem>
                  <ListItemIcon>
                    <SpeedIcon color={settings?.profilerEnabled ? "primary" : "disabled"} />
                  </ListItemIcon>
                  <ListItemText 
                    primary="Performance Profiler" 
                    secondary={settings?.profilerEnabled ? "Enabled" : "Disabled"} 
                  />
                  <Chip 
                    label={settings?.profilerEnabled ? "Enabled" : "Disabled"} 
                    color={settings?.profilerEnabled ? "success" : "default"} 
                  />
                </ListItem>
                <ListItem>
                  <ListItemIcon>
                    <BuildIcon color={settings?.deprecationRescueEnabled ? "primary" : "disabled"} />
                  </ListItemIcon>
                  <ListItemText 
                    primary="Deprecation Rescue" 
                    secondary={settings?.deprecationRescueEnabled ? "Enabled" : "Disabled"} 
                  />
                  <Chip 
                    label={settings?.deprecationRescueEnabled ? "Enabled" : "Disabled"} 
                    color={settings?.deprecationRescueEnabled ? "success" : "default"} 
                  />
                </ListItem>
                <ListItem>
                  <ListItemIcon>
                    <SecurityIcon color={settings?.securityPatchesEnabled ? "primary" : "disabled"} />
                  </ListItemIcon>
                  <ListItemText 
                    primary="Security Patches" 
                    secondary={settings?.securityPatchesEnabled ? "Enabled" : "Disabled"} 
                  />
                  <Chip 
                    label={settings?.securityPatchesEnabled ? "Enabled" : "Disabled"} 
                    color={settings?.securityPatchesEnabled ? "success" : "default"} 
                  />
                </ListItem>
              </List>
              
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Monitored Packages
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {settings?.monitoredPackages.map((pkg, index) => (
                    <Chip key={index} label={pkg} size="small" />
                  ))}
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Patch Summary */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader title="Patch Summary" />
            <Divider />
            <CardContent>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" gutterBottom>
                    By Type
                  </Typography>
                  <List dense sx={{ bgcolor: '#f5f5f5', borderRadius: 1 }}>
                    <ListItem>
                      <ListItemIcon>
                        <SpeedIcon color="primary" />
                      </ListItemIcon>
                      <ListItemText primary="Performance Hotspots" />
                      <Chip label={typeCounts.hotspot} size="small" />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon>
                        <BuildIcon color="secondary" />
                      </ListItemIcon>
                      <ListItemText primary="API Deprecations" />
                      <Chip label={typeCounts.deprecation} size="small" />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon>
                        <SecurityIcon color="error" />
                      </ListItemIcon>
                      <ListItemText primary="Security Patches" />
                      <Chip label={typeCounts.security} size="small" />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon>
                        <WarningIcon color="warning" />
                      </ListItemIcon>
                      <ListItemText primary="General Fixes" />
                      <Chip label={typeCounts.general} size="small" />
                    </ListItem>
                  </List>
                </Grid>
                
                <Grid item xs={6}>
                  <Typography variant="subtitle2" gutterBottom>
                    By Status
                  </Typography>
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2">
                      Available Patches
                    </Typography>
                    <Typography variant="h4" color="primary">
                      {statusCounts.available}
                    </Typography>
                  </Box>
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2">
                      Applied Patches
                    </Typography>
                    <Typography variant="h4" color="success.main">
                      {statusCounts.applied}
                    </Typography>
                  </Box>
                  <Box>
                    <Typography variant="body2">
                      Failed Patches
                    </Typography>
                    <Typography variant="h4" color="error">
                      {statusCounts.failed}
                    </Typography>
                  </Box>
                </Grid>
              </Grid>
              
              <Box sx={{ mt: 2, textAlign: 'right' }}>
                <Button
                  component={Link}
                  to="/patches"
                  variant="contained"
                  color="primary"
                >
                  View All Patches
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Performance Hotspots */}
        <Grid item xs={12}>
          <Card>
            <CardHeader 
              title="Top Performance Hotspots" 
              action={
                <Button 
                  size="small" 
                  color="primary"
                  component={Link}
                  to="/performance"
                >
                  View All
                </Button>
              }
            />
            <Divider />
            <CardContent>
              {performanceData.length === 0 ? (
                <Typography color="textSecondary" align="center">
                  No performance data available
                </Typography>
              ) : (
                <Grid container spacing={2}>
                  {performanceData
                    .sort((a, b) => b.avgExecutionTimeMs - a.avgExecutionTimeMs)
                    .slice(0, 5)
                    .map((item, index) => (
                      <Grid item xs={12} key={index}>
                        <Paper sx={{ p: 2 }}>
                          <Typography variant="subtitle2">
                            {item.className}.{item.methodName}
                          </Typography>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1 }}>
                            <Typography variant="body2">
                              Avg: {item.avgExecutionTimeMs.toFixed(2)} ms
                            </Typography>
                            <Typography variant="body2">
                              Calls: {item.callCount}
                            </Typography>
                            <Typography variant="body2">
                              Slowest: {item.slowestMs.toFixed(2)} ms
                            </Typography>
                          </Box>
                        </Paper>
                      </Grid>
                    ))}
                </Grid>
              )}
            </CardContent>
          </Card>
        </Grid>
        
        {/* Recent Patches */}
        <Grid item xs={12}>
          <Card>
            <CardHeader 
              title="Recently Applied Patches" 
              action={
                <Button 
                  size="small" 
                  color="primary"
                  component={Link}
                  to="/patches"
                >
                  View All
                </Button>
              }
            />
            <Divider />
            <CardContent>
              {patches.filter(p => p.status === 'applied').length === 0 ? (
                <Typography color="textSecondary" align="center">
                  No patches have been applied yet
                </Typography>
              ) : (
                <List>
                  {patches
                    .filter(p => p.status === 'applied')
                    .sort((a, b) => new Date(b.appliedAt || '').getTime() - new Date(a.appliedAt || '').getTime())
                    .slice(0, 5)
                    .map(patch => (
                      <ListItem 
                        key={patch.id}
                        button
                        component={Link}
                        to={`/patches/${patch.id}`}
                        divider
                      >
                        <ListItemText
                          primary={patch.name}
                          secondary={
                            <>
                              <Typography component="span" variant="body2" color="textPrimary">
                                {patch.description}
                              </Typography>
                              <Typography variant="caption" display="block" color="textSecondary">
                                Applied: {new Date(patch.appliedAt || '').toLocaleString()}
                              </Typography>
                            </>
                          }
                        />
                        <Chip 
                          label={patch.type} 
                          size="small"
                          color={
                            patch.type === 'security' 
                              ? 'error' 
                              : patch.type === 'hotspot' 
                                ? 'primary' 
                                : patch.type === 'deprecation' 
                                  ? 'secondary' 
                                  : 'default'
                          }
                          sx={{ ml: 1 }}
                        />
                      </ListItem>
                    ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard; 