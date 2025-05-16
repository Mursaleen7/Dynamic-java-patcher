import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Box, 
  Button, 
  Card, 
  CardContent, 
  Chip, 
  CircularProgress, 
  Container, 
  Divider, 
  Grid, 
  Paper, 
  Tooltip, 
  Typography,
  Alert,
  IconButton,
  Tab,
  Tabs
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Check as CheckIcon,
  ErrorOutline as ErrorIcon,
  FileDownload as FileDownloadIcon,
  Code as CodeIcon,
  CompareArrows as CompareArrowsIcon
} from '@mui/icons-material';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { materialLight as lightTheme, materialDark as darkTheme } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { patchesApi } from '../services/api';
import { CodeChange, Patch } from '../types';

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
      id={`change-tabpanel-${index}`}
      aria-labelledby={`change-tab-${index}`}
      {...other}
    >
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

const PatchDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [patch, setPatch] = useState<Patch | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [applying, setApplying] = useState<boolean>(false);
  const [tabValue, setTabValue] = useState<number>(0);
  const [selectedChange, setSelectedChange] = useState<number>(0);
  const [viewMode, setViewMode] = useState<'split' | 'before' | 'after'>('split');

  useEffect(() => {
    const fetchPatchDetails = async () => {
      if (!id) return;
      
      setLoading(true);
      try {
        const response = await patchesApi.getPatch(id);
        if (response.success && response.data) {
          setPatch(response.data);
        } else {
          throw new Error(response.error || 'Failed to fetch patch details');
        }
      } catch (err: any) {
        setError(err.message || 'Something went wrong');
        console.error('Error fetching patch details:', err);
      } finally {
        setLoading(false);
      }
    };
    
    fetchPatchDetails();
  }, [id]);

  const handleApplyPatch = async () => {
    if (!patch) return;
    
    setApplying(true);
    try {
      const response = await patchesApi.applyPatch(patch.id);
      if (response.success) {
        // Refetch patch details to get updated status
        const updatedResponse = await patchesApi.getPatch(patch.id);
        if (updatedResponse.success && updatedResponse.data) {
          setPatch(updatedResponse.data);
        }
      } else {
        throw new Error(response.error || 'Failed to apply patch');
      }
    } catch (err: any) {
      setError(err.message || 'Something went wrong');
      console.error('Error applying patch:', err);
    } finally {
      setApplying(false);
    }
  };

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleChangeSelection = (index: number) => {
    setSelectedChange(index);
  };

  const handleViewModeChange = (mode: 'split' | 'before' | 'after') => {
    setViewMode(mode);
  };

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

  const detectLanguage = (filePath: string): string => {
    const extension = filePath.split('.').pop()?.toLowerCase() || '';
    
    switch (extension) {
      case 'java':
        return 'java';
      case 'js':
        return 'javascript';
      case 'ts':
        return 'typescript';
      case 'jsx':
        return 'jsx';
      case 'tsx':
        return 'tsx';
      case 'html':
        return 'html';
      case 'css':
        return 'css';
      case 'json':
        return 'json';
      case 'xml':
        return 'xml';
      case 'yml':
      case 'yaml':
        return 'yaml';
      case 'md':
        return 'markdown';
      case 'sql':
        return 'sql';
      default:
        return 'java';
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
          <Typography color="error" variant="h6">Error Loading Patch Details</Typography>
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

  if (!patch) {
    return (
      <Box sx={{ p: 3 }}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6">Patch Not Found</Typography>
          <Button 
            variant="contained" 
            color="primary" 
            startIcon={<ArrowBackIcon />}
            sx={{ mt: 2 }}
            onClick={() => navigate('/patches')}
          >
            Back to Patches
          </Button>
        </Paper>
      </Box>
    );
  }

  const selectedCodeChange: CodeChange | undefined = 
    patch.codeChanges.length > 0 ? patch.codeChanges[selectedChange] : undefined;

  return (
    <Container maxWidth="xl">
      <Box sx={{ mb: 2 }}>
        <Button 
          startIcon={<ArrowBackIcon />} 
          onClick={() => navigate('/patches')}
          sx={{ mb: 2 }}
        >
          Back to Patches
        </Button>
        
        <Grid container spacing={2}>
          <Grid item xs={12} md={8}>
            <Typography variant="h4" component="h1">
              {patch.name}
            </Typography>
            <Box sx={{ display: 'flex', mt: 1 }}>
              <Chip 
                label={patch.status} 
                size="small" 
                color={getStatusColor(patch.status) as any} 
                icon={patch.status === 'applied' ? <CheckIcon /> : patch.status === 'failed' ? <ErrorIcon /> : undefined}
                sx={{ mr: 1 }}
              />
              <Chip 
                label={patch.type} 
                size="small" 
                color={getTypeColor(patch.type) as any}
                sx={{ mr: 1 }}
              />
              <Chip 
                label={`v${patch.version}`} 
                size="small" 
                variant="outlined"
              />
            </Box>
          </Grid>
          
          <Grid item xs={12} md={4} sx={{ display: 'flex', justifyContent: { xs: 'flex-start', md: 'flex-end' }, alignItems: 'center' }}>
            {patch.status === 'available' && (
              <Button
                variant="contained"
                color="primary"
                onClick={handleApplyPatch}
                disabled={applying}
                startIcon={applying ? <CircularProgress size={20} /> : <CheckIcon />}
              >
                {applying ? 'Applying...' : 'Apply Patch'}
              </Button>
            )}
          </Grid>
        </Grid>
      </Box>
      
      <Divider sx={{ mb: 3 }} />
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Patch Details
              </Typography>
              <Typography variant="body1" paragraph>
                {patch.description}
              </Typography>
              
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Created
                </Typography>
                <Typography variant="body2">
                  {new Date(patch.createdAt).toLocaleString()}
                </Typography>
              </Box>
              
              {patch.appliedAt && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Applied
                  </Typography>
                  <Typography variant="body2">
                    {new Date(patch.appliedAt).toLocaleString()}
                  </Typography>
                </Box>
              )}
              
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Changes
                </Typography>
                <Typography variant="body2">
                  {patch.codeChanges.length} file{patch.codeChanges.length !== 1 ? 's' : ''} modified
                </Typography>
              </Box>
            </CardContent>
          </Card>
          
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Code Changes
              </Typography>
              
              {patch.codeChanges.length === 0 ? (
                <Alert severity="info">No code changes in this patch</Alert>
              ) : (
                <Box component="nav" aria-label="code changes">
                  {patch.codeChanges.map((change, index) => (
                    <Box 
                      key={index} 
                      sx={{ 
                        p: 1.5, 
                        mb: 1, 
                        borderRadius: 1, 
                        bgcolor: selectedChange === index ? 'action.selected' : 'background.paper',
                        border: '1px solid',
                        borderColor: selectedChange === index ? 'primary.main' : 'divider',
                        cursor: 'pointer',
                        '&:hover': {
                          bgcolor: selectedChange === index ? 'action.selected' : 'action.hover',
                        },
                      }}
                      onClick={() => handleChangeSelection(index)}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <CodeIcon sx={{ mr: 1, fontSize: 16 }} />
                        <Typography variant="subtitle2" noWrap sx={{ flexGrow: 1 }}>
                          {change.filePath.split('/').pop()}
                        </Typography>
                      </Box>
                      <Typography variant="caption" noWrap>
                        {change.className}{change.methodName ? `.${change.methodName}` : ''}
                      </Typography>
                      <Typography variant="caption" color="textSecondary" display="block">
                        Lines {change.lineStart} - {change.lineEnd}
                      </Typography>
                    </Box>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={8}>
          {selectedCodeChange ? (
            <Card>
              <Box sx={{ borderBottom: 1, borderColor: 'divider', p: 1, display: 'flex', justifyContent: 'space-between' }}>
                <Tabs value={tabValue} onChange={handleTabChange} aria-label="code view tabs">
                  <Tab label="Code Changes" />
                  <Tab label="File Info" />
                </Tabs>
                
                <Box>
                  <Tooltip title="View side by side">
                    <IconButton 
                      size="small" 
                      color={viewMode === 'split' ? 'primary' : 'default'}
                      onClick={() => handleViewModeChange('split')}
                    >
                      <CompareArrowsIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="View before only">
                    <IconButton 
                      size="small" 
                      color={viewMode === 'before' ? 'primary' : 'default'}
                      onClick={() => handleViewModeChange('before')}
                    >
                      <FileDownloadIcon sx={{ transform: 'rotate(180deg)' }} />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="View after only">
                    <IconButton 
                      size="small" 
                      color={viewMode === 'after' ? 'primary' : 'default'}
                      onClick={() => handleViewModeChange('after')}
                    >
                      <FileDownloadIcon />
                    </IconButton>
                  </Tooltip>
                </Box>
              </Box>
              
              <CardContent>
                <TabPanel value={tabValue} index={0}>
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="subtitle1">
                      {selectedCodeChange.filePath}
                    </Typography>
                    <Typography variant="caption" color="textSecondary">
                      {selectedCodeChange.className}{selectedCodeChange.methodName ? `.${selectedCodeChange.methodName}` : ''} (Lines {selectedCodeChange.lineStart}-{selectedCodeChange.lineEnd})
                    </Typography>
                  </Box>
                  
                  {viewMode === 'split' ? (
                    <Grid container spacing={2}>
                      <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" gutterBottom>
                          Before
                        </Typography>
                        <Paper variant="outlined" sx={{ overflow: 'auto' }}>
                          <SyntaxHighlighter
                            language={detectLanguage(selectedCodeChange.filePath)}
                            style={lightTheme}
                            showLineNumbers
                            startingLineNumber={selectedCodeChange.lineStart}
                          >
                            {selectedCodeChange.beforeCode}
                          </SyntaxHighlighter>
                        </Paper>
                      </Grid>
                      <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" gutterBottom>
                          After
                        </Typography>
                        <Paper variant="outlined" sx={{ overflow: 'auto' }}>
                          <SyntaxHighlighter
                            language={detectLanguage(selectedCodeChange.filePath)}
                            style={lightTheme}
                            showLineNumbers
                            startingLineNumber={selectedCodeChange.lineStart}
                          >
                            {selectedCodeChange.afterCode}
                          </SyntaxHighlighter>
                        </Paper>
                      </Grid>
                    </Grid>
                  ) : viewMode === 'before' ? (
                    <Box>
                      <Typography variant="subtitle2" gutterBottom>
                        Before
                      </Typography>
                      <Paper variant="outlined" sx={{ overflow: 'auto' }}>
                        <SyntaxHighlighter
                          language={detectLanguage(selectedCodeChange.filePath)}
                          style={lightTheme}
                          showLineNumbers
                          startingLineNumber={selectedCodeChange.lineStart}
                        >
                          {selectedCodeChange.beforeCode}
                        </SyntaxHighlighter>
                      </Paper>
                    </Box>
                  ) : (
                    <Box>
                      <Typography variant="subtitle2" gutterBottom>
                        After
                      </Typography>
                      <Paper variant="outlined" sx={{ overflow: 'auto' }}>
                        <SyntaxHighlighter
                          language={detectLanguage(selectedCodeChange.filePath)}
                          style={lightTheme}
                          showLineNumbers
                          startingLineNumber={selectedCodeChange.lineStart}
                        >
                          {selectedCodeChange.afterCode}
                        </SyntaxHighlighter>
                      </Paper>
                    </Box>
                  )}
                </TabPanel>
                
                <TabPanel value={tabValue} index={1}>
                  <Box>
                    <Typography variant="h6" gutterBottom>
                      File Information
                    </Typography>
                    <Grid container spacing={2}>
                      <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2">
                          File Path
                        </Typography>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                          {selectedCodeChange.filePath}
                        </Typography>
                        
                        <Typography variant="subtitle2">
                          Class Name
                        </Typography>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                          {selectedCodeChange.className}
                        </Typography>
                        
                        {selectedCodeChange.methodName && (
                          <>
                            <Typography variant="subtitle2">
                              Method Name
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 2 }}>
                              {selectedCodeChange.methodName}
                            </Typography>
                          </>
                        )}
                      </Grid>
                      
                      <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2">
                          Line Range
                        </Typography>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                          {selectedCodeChange.lineStart} - {selectedCodeChange.lineEnd}
                        </Typography>
                        
                        <Typography variant="subtitle2">
                          Lines Changed
                        </Typography>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                          {selectedCodeChange.lineEnd - selectedCodeChange.lineStart + 1}
                        </Typography>
                        
                        <Typography variant="subtitle2">
                          Language
                        </Typography>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                          {detectLanguage(selectedCodeChange.filePath).toUpperCase()}
                        </Typography>
                      </Grid>
                    </Grid>
                  </Box>
                </TabPanel>
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardContent>
                <Alert severity="info">
                  {patch.codeChanges.length === 0 
                    ? "This patch doesn't contain any code changes." 
                    : "Select a code change from the list to view details."}
                </Alert>
              </CardContent>
            </Card>
          )}
        </Grid>
      </Grid>
    </Container>
  );
};

export default PatchDetail; 