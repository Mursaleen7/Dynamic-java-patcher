import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  Divider,
  FormControl,
  FormControlLabel,
  FormGroup,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  SelectChangeEvent,
  Switch,
  TextField,
  Tooltip,
  Typography
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Save as SaveIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import { settingsApi } from '../services/api';
import { AppSettings } from '../types';

const Settings: React.FC = () => {
  const [settings, setSettings] = useState<AppSettings | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [newPackage, setNewPackage] = useState<string>('');

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await settingsApi.getSettings();
      if (response.success && response.data) {
        setSettings(response.data);
      } else {
        throw new Error(response.error || 'Failed to fetch settings');
      }
    } catch (err: any) {
      setError(err.message || 'Something went wrong');
      console.error('Error fetching settings:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveSettings = async () => {
    if (!settings) return;
    
    setSaving(true);
    setError(null);
    setSuccess(false);
    
    try {
      const response = await settingsApi.updateSettings(settings);
      if (response.success && response.data) {
        setSettings(response.data);
        setSuccess(true);
        // Clear success message after 3 seconds
        setTimeout(() => setSuccess(false), 3000);
      } else {
        throw new Error(response.error || 'Failed to update settings');
      }
    } catch (err: any) {
      setError(err.message || 'Something went wrong');
      console.error('Error updating settings:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleSwitchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (!settings) return;
    
    const { name, checked } = event.target;
    setSettings(prev => {
      if (!prev) return prev;
      return { ...prev, [name]: checked };
    });
  };

  const handleRefreshIntervalChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (!settings) return;
    
    const value = parseInt(event.target.value, 10);
    if (isNaN(value) || value < 1) return;
    
    setSettings(prev => {
      if (!prev) return prev;
      return { ...prev, refreshIntervalSeconds: value };
    });
  };

  const handleAddPackage = () => {
    if (!settings || !newPackage.trim()) return;
    
    // Don't add if already exists
    if (settings.monitoredPackages.includes(newPackage.trim())) {
      return;
    }
    
    setSettings(prev => {
      if (!prev) return prev;
      return {
        ...prev,
        monitoredPackages: [...prev.monitoredPackages, newPackage.trim()]
      };
    });
    
    setNewPackage('');
  };

  const handleRemovePackage = (pkg: string) => {
    if (!settings) return;
    
    setSettings(prev => {
      if (!prev) return prev;
      return {
        ...prev,
        monitoredPackages: prev.monitoredPackages.filter(p => p !== pkg)
      };
    });
  };

  const handleNewPackageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setNewPackage(event.target.value);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!settings) {
    return (
      <Box sx={{ p: 3 }}>
        <Paper sx={{ p: 3, bgcolor: '#fff9f9', border: '1px solid #ffdddd' }}>
          <Typography color="error" variant="h6">Error Loading Settings</Typography>
          <Typography color="error">{error || 'Failed to load settings'}</Typography>
          <Button 
            variant="contained" 
            color="primary" 
            sx={{ mt: 2 }}
            onClick={fetchSettings}
          >
            Retry
          </Button>
        </Paper>
      </Box>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Settings
        </Typography>
        <Typography variant="subtitle1" color="textSecondary">
          Configure your Dynamic Java Patcher
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          Settings saved successfully
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Feature Settings
              </Typography>
              <FormGroup>
                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.profilerEnabled}
                      onChange={handleSwitchChange}
                      name="profilerEnabled"
                      color="primary"
                    />
                  }
                  label="Performance Profiler"
                />
                <Typography variant="caption" color="textSecondary" sx={{ ml: 4, mt: -1, mb: 1 }}>
                  Monitors method execution time to identify performance hotspots
                </Typography>

                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.deprecationRescueEnabled}
                      onChange={handleSwitchChange}
                      name="deprecationRescueEnabled"
                      color="primary"
                    />
                  }
                  label="Deprecation Rescue"
                />
                <Typography variant="caption" color="textSecondary" sx={{ ml: 4, mt: -1, mb: 1 }}>
                  Provides runtime fallbacks for deprecated APIs
                </Typography>

                <FormControlLabel
                  control={
                    <Switch
                      checked={settings.securityPatchesEnabled}
                      onChange={handleSwitchChange}
                      name="securityPatchesEnabled"
                      color="primary"
                    />
                  }
                  label="Security Patches"
                />
                <Typography variant="caption" color="textSecondary" sx={{ ml: 4, mt: -1, mb: 1 }}>
                  Applies runtime security patches for vulnerable methods
                </Typography>
              </FormGroup>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Refresh Settings
              </Typography>
              <FormControl fullWidth sx={{ mb: 3 }}>
                <TextField
                  label="Refresh Interval (seconds)"
                  type="number"
                  value={settings.refreshIntervalSeconds}
                  onChange={handleRefreshIntervalChange}
                  InputProps={{ inputProps: { min: 1, max: 3600 } }}
                  helperText="How often to check for new patches (1-3600 seconds)"
                />
              </FormControl>

              <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                <Tooltip title="Reset to defaults">
                  <Button 
                    startIcon={<RefreshIcon />}
                    variant="outlined" 
                    color="secondary"
                    sx={{ mr: 1 }}
                    onClick={() => {
                      setSettings(prev => {
                        if (!prev) return prev;
                        return {
                          ...prev,
                          refreshIntervalSeconds: 60 // Default value
                        };
                      });
                    }}
                  >
                    Reset
                  </Button>
                </Tooltip>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Monitored Packages
              </Typography>
              <Typography variant="body2" paragraph>
                Specify the Java packages that should be monitored for profiling and patching.
              </Typography>

              <Box sx={{ mb: 3 }}>
                <Grid container spacing={2}>
                  <Grid item xs={10}>
                    <TextField
                      fullWidth
                      label="Package Name"
                      placeholder="e.g., com.example.myapp"
                      value={newPackage}
                      onChange={handleNewPackageChange}
                      onKeyPress={(e) => {
                        if (e.key === 'Enter') {
                          handleAddPackage();
                        }
                      }}
                    />
                  </Grid>
                  <Grid item xs={2}>
                    <Button
                      fullWidth
                      variant="contained"
                      color="primary"
                      onClick={handleAddPackage}
                      disabled={!newPackage.trim()}
                      sx={{ height: '56px' }}
                    >
                      <AddIcon />
                    </Button>
                  </Grid>
                </Grid>
              </Box>

              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {settings.monitoredPackages.map((pkg, index) => (
                  <Chip
                    key={index}
                    label={pkg}
                    onDelete={() => handleRemovePackage(pkg)}
                    deleteIcon={<DeleteIcon />}
                    sx={{ mb: 1 }}
                  />
                ))}
                {settings.monitoredPackages.length === 0 && (
                  <Typography color="textSecondary">
                    No packages configured. Add some packages to monitor.
                  </Typography>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Box sx={{ mt: 4, display: 'flex', justifyContent: 'flex-end' }}>
        <Button
          variant="contained"
          color="primary"
          size="large"
          startIcon={saving ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
          onClick={handleSaveSettings}
          disabled={saving}
        >
          {saving ? 'Saving...' : 'Save Settings'}
        </Button>
      </Box>
    </Container>
  );
};

export default Settings; 