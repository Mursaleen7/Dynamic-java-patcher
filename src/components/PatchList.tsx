import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  Divider,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  SelectChangeEvent,
  Tab,
  Tabs,
  Typography,
} from '@mui/material';
import { Link } from 'react-router-dom';
import { patchesApi } from '../services/api';
import { Patch } from '../types';

// Tab Panel interface and component
interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`patch-tabpanel-${index}`}
      aria-labelledby={`patch-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

function a11yProps(index: number) {
  return {
    id: `patch-tab-${index}`,
    'aria-controls': `patch-tabpanel-${index}`,
  };
}

// Main Component
const PatchList: React.FC = () => {
  const [patches, setPatches] = useState<Patch[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState<number>(0);
  const [typeFilter, setTypeFilter] = useState<string>('all');
  
  useEffect(() => {
    const fetchPatches = async () => {
      setLoading(true);
      try {
        const response = await patchesApi.getPatches();
        if (response.success && response.data) {
          setPatches(response.data);
        } else {
          throw new Error(response.error || 'Failed to fetch patches');
        }
      } catch (err: any) {
        setError(err.message || 'Something went wrong');
        console.error('Error fetching patches:', err);
      } finally {
        setLoading(false);
      }
    };
    
    fetchPatches();
  }, []);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleTypeFilterChange = (event: SelectChangeEvent) => {
    setTypeFilter(event.target.value);
  };

  // Filter patches based on tab and type filter
  const getFilteredPatches = () => {
    let filtered = [...patches];
    
    // Filter by tab (status)
    if (tabValue === 0) {
      filtered = filtered.filter(patch => patch.status === 'available');
    } else if (tabValue === 1) {
      filtered = filtered.filter(patch => patch.status === 'applied');
    } else if (tabValue === 2) {
      filtered = filtered.filter(patch => patch.status === 'failed');
    }
    
    // Filter by type
    if (typeFilter !== 'all') {
      filtered = filtered.filter(patch => patch.type === typeFilter);
    }
    
    return filtered;
  };

  const filteredPatches = getFilteredPatches();

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'available':
        return 'primary';
      case 'applied':
        return 'success';
      case 'failed':
        return 'error';
      default:
        return 'default';
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'hotspot':
        return 'primary';
      case 'deprecation':
        return 'secondary';
      case 'security':
        return 'error';
      case 'general':
        return 'warning';
      default:
        return 'default';
    }
  };

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
          <Typography color="error" variant="h6">Error Loading Patches</Typography>
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
          Patches
        </Typography>
        <Typography variant="subtitle1" color="textSecondary">
          Manage and apply patches to your Java application
        </Typography>
      </Box>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={8}>
            <Tabs value={tabValue} onChange={handleTabChange} aria-label="patch tabs">
              <Tab 
                label={
                  <>
                    Available
                    <Chip 
                      label={patches.filter(p => p.status === 'available').length} 
                      size="small" 
                      color="primary"
                      sx={{ ml: 1 }}
                    />
                  </>
                } 
                {...a11yProps(0)} 
              />
              <Tab 
                label={
                  <>
                    Applied
                    <Chip 
                      label={patches.filter(p => p.status === 'applied').length} 
                      size="small" 
                      color="success"
                      sx={{ ml: 1 }}
                    />
                  </>
                } 
                {...a11yProps(1)} 
              />
              <Tab 
                label={
                  <>
                    Failed
                    <Chip 
                      label={patches.filter(p => p.status === 'failed').length} 
                      size="small" 
                      color="error"
                      sx={{ ml: 1 }}
                    />
                  </>
                } 
                {...a11yProps(2)} 
              />
            </Tabs>
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth size="small">
              <InputLabel id="type-filter-label">Filter by Type</InputLabel>
              <Select
                labelId="type-filter-label"
                id="type-filter"
                value={typeFilter}
                label="Filter by Type"
                onChange={handleTypeFilterChange}
              >
                <MenuItem value="all">All Types</MenuItem>
                <MenuItem value="hotspot">Performance Hotspot</MenuItem>
                <MenuItem value="deprecation">API Deprecation</MenuItem>
                <MenuItem value="security">Security Patch</MenuItem>
                <MenuItem value="general">General Fix</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Box>

      {filteredPatches.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="textSecondary" variant="h6">
            No patches found matching your criteria
          </Typography>
          <Typography color="textSecondary" sx={{ mt: 1 }}>
            {typeFilter !== 'all' ? 'Try changing your type filter or' : ''} check another tab
          </Typography>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {filteredPatches.map((patch) => (
            <Grid item xs={12} md={6} key={patch.id}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography variant="h6">{patch.name}</Typography>
                    <Box>
                      <Chip 
                        label={patch.status} 
                        size="small" 
                        color={getStatusColor(patch.status) as any} 
                        sx={{ mr: 1 }}
                      />
                      <Chip 
                        label={patch.type} 
                        size="small" 
                        color={getTypeColor(patch.type) as any}
                      />
                    </Box>
                  </Box>
                  
                  <Typography color="textSecondary" sx={{ mb: 2 }}>
                    {patch.description}
                  </Typography>
                  
                  <Divider sx={{ my: 2 }} />
                  
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Box>
                      <Typography variant="caption" display="block">
                        Created: {new Date(patch.createdAt).toLocaleString()}
                      </Typography>
                      {patch.appliedAt && (
                        <Typography variant="caption" display="block">
                          Applied: {new Date(patch.appliedAt).toLocaleString()}
                        </Typography>
                      )}
                    </Box>
                    
                    <Box>
                      <Button
                        component={Link}
                        to={`/patches/${patch.id}`}
                        variant="outlined"
                        color="primary"
                        size="small"
                        sx={{ mr: 1 }}
                      >
                        Details
                      </Button>
                      
                      {patch.status === 'available' && (
                        <Button
                          variant="contained"
                          color="primary"
                          size="small"
                          onClick={() => patchesApi.applyPatch(patch.id)}
                        >
                          Apply
                        </Button>
                      )}
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Container>
  );
};

export default PatchList; 