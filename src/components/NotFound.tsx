import React from 'react';
import { Box, Button, Container, Paper, Typography } from '@mui/material';
import { Link } from 'react-router-dom';

const NotFound: React.FC = () => {
  return (
    <Container maxWidth="md">
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '70vh',
        }}
      >
        <Paper
          elevation={3}
          sx={{
            p: 4,
            textAlign: 'center',
          }}
        >
          <Typography variant="h1" component="h1" color="primary" gutterBottom>
            404
          </Typography>
          <Typography variant="h4" component="h2" gutterBottom>
            Page Not Found
          </Typography>
          <Typography variant="body1" color="textSecondary" paragraph>
            The page you are looking for doesn't exist or has been moved.
          </Typography>
          <Button
            component={Link}
            to="/"
            variant="contained"
            color="primary"
            size="large"
            sx={{ mt: 2 }}
          >
            Go to Dashboard
          </Button>
        </Paper>
      </Box>
    </Container>
  );
};

export default NotFound; 