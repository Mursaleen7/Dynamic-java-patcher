const express = require('express');
const cors = require('cors');
const http = require('http');
const socketIo = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: "http://localhost:3000",
    methods: ["GET", "POST"]
  }
});

// Middleware
app.use(cors());
app.use(express.json());

// Sample data
const patches = [
  {
    id: "patch-001",
    name: "Security Patch: SQL Injection Protection",
    version: "1.0.1",
    description: "Adds runtime protection against SQL injection vulnerabilities in database queries",
    createdAt: "2025-05-15T10:00:00.000Z",
    status: "available",
    type: "security",
    codeChanges: [
      {
        filePath: "com/example/dao/UserDAO.java",
        className: "com.example.dao.UserDAO",
        methodName: "findByUsername",
        lineStart: 42,
        lineEnd: 48,
        beforeCode: `public User findByUsername(String username) {
    String query = "SELECT * FROM users WHERE username = '" + username + "'";
    PreparedStatement stmt = connection.prepareStatement(query);
    ResultSet rs = stmt.executeQuery();
    // Process results
    return mapResultSetToUser(rs);
}`,
        afterCode: `public User findByUsername(String username) {
    String query = "SELECT * FROM users WHERE username = ?";
    PreparedStatement stmt = connection.prepareStatement(query);
    stmt.setString(1, username);
    ResultSet rs = stmt.executeQuery();
    // Process results
    return mapResultSetToUser(rs);
}`
      }
    ]
  },
  {
    id: "patch-002",
    name: "Performance Hotspot: String Concatenation",
    version: "1.0.0",
    description: "Optimizes a performance hotspot by replacing string concatenation with StringBuilder",
    createdAt: "2025-05-14T15:30:00.000Z",
    status: "applied",
    appliedAt: "2025-05-14T16:45:00.000Z",
    type: "hotspot",
    codeChanges: [
      {
        filePath: "com/example/utils/ReportGenerator.java",
        className: "com.example.utils.ReportGenerator",
        methodName: "generateReport",
        lineStart: 103,
        lineEnd: 114,
        beforeCode: `private String generateReport(List<ReportItem> items) {
    String report = "";
    for (ReportItem item : items) {
        report += "Item: " + item.getName() + "\\n";
        report += "Value: " + item.getValue() + "\\n";
        report += "Category: " + item.getCategory() + "\\n";
        report += "\\n";
    }
    return report;
}`,
        afterCode: `private String generateReport(List<ReportItem> items) {
    StringBuilder report = new StringBuilder();
    for (ReportItem item : items) {
        report.append("Item: ").append(item.getName()).append("\\n");
        report.append("Value: ").append(item.getValue()).append("\\n");
        report.append("Category: ").append(item.getCategory()).append("\\n");
        report.append("\\n");
    }
    return report.toString();
}`
      }
    ]
  },
  {
    id: "patch-003",
    name: "API Deprecation: Legacy Date API",
    version: "1.0.2",
    description: "Replaces usage of deprecated Date APIs with modern java.time equivalents",
    createdAt: "2025-05-13T09:15:00.000Z",
    status: "failed",
    type: "deprecation",
    codeChanges: [
      {
        filePath: "com/example/service/DateService.java",
        className: "com.example.service.DateService",
        methodName: "parseDateString",
        lineStart: 76,
        lineEnd: 81,
        beforeCode: `public Date parseDateString(String dateStr) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    return sdf.parse(dateStr);
}`,
        afterCode: `public LocalDate parseDateString(String dateStr) {
    return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
}`
      }
    ]
  }
];

const performanceData = [
  {
    className: "com.example.service.UserService",
    methodName: "authenticateUser",
    avgExecutionTimeMs: 152.3,
    callCount: 5423,
    slowestMs: 892.1,
    fastestMs: 45.7
  },
  {
    className: "com.example.dao.ProductDAO",
    methodName: "findByCategoryWithFilters",
    avgExecutionTimeMs: 324.8,
    callCount: 1254,
    slowestMs: 1452.3,
    fastestMs: 87.5
  },
  {
    className: "com.example.utils.ReportGenerator",
    methodName: "generateReport",
    avgExecutionTimeMs: 587.2,
    callCount: 324,
    slowestMs: 2345.8,
    fastestMs: 102.3
  },
  {
    className: "com.example.controller.OrderController",
    methodName: "processNewOrder",
    avgExecutionTimeMs: 456.1,
    callCount: 845,
    slowestMs: 1845.2,
    fastestMs: 95.8
  }
];

const settings = {
  profilerEnabled: true,
  deprecationRescueEnabled: true,
  securityPatchesEnabled: true,
  monitoredPackages: [
    "com.example.service",
    "com.example.dao",
    "com.example.controller",
    "com.example.utils"
  ],
  refreshIntervalSeconds: 60
};

const notifications = [
  {
    id: "notif-001",
    timestamp: "2025-05-15T10:05:32.000Z",
    title: "New Security Patch Available",
    message: "A new security patch for SQL Injection protection is available for application",
    type: "info",
    patchId: "patch-001",
    read: false
  },
  {
    id: "notif-002",
    timestamp: "2025-05-14T16:45:10.000Z",
    title: "Performance Patch Applied",
    message: "Performance patch for string concatenation was successfully applied",
    type: "success",
    patchId: "patch-002",
    read: true
  },
  {
    id: "notif-003",
    timestamp: "2025-05-13T09:30:22.000Z",
    title: "API Deprecation Patch Failed",
    message: "Failed to apply deprecation patch for legacy Date API. Check logs for details.",
    type: "error",
    patchId: "patch-003",
    read: false
  }
];

// API Routes
app.get('/api/patches', (req, res) => {
  res.json({ success: true, data: patches });
});

app.get('/api/patches/:id', (req, res) => {
  const patch = patches.find(p => p.id === req.params.id);
  if (patch) {
    res.json({ success: true, data: patch });
  } else {
    res.status(404).json({ success: false, error: 'Patch not found' });
  }
});

app.post('/api/patches/:id/apply', (req, res) => {
  const patch = patches.find(p => p.id === req.params.id);
  if (patch) {
    if (patch.status === 'available') {
      patch.status = 'applied';
      patch.appliedAt = new Date().toISOString();
      
      // Send a notification via WebSocket
      const notification = {
        id: `notif-${Date.now()}`,
        timestamp: new Date().toISOString(),
        title: `Patch ${patch.name} Applied`,
        message: `Successfully applied patch ${patch.name}`,
        type: 'success',
        patchId: patch.id,
        read: false
      };
      
      notifications.push(notification);
      io.emit('patch_applied', notification);
      
      res.json({ success: true, data: true });
    } else {
      res.status(400).json({ success: false, error: 'Patch is not in available state' });
    }
  } else {
    res.status(404).json({ success: false, error: 'Patch not found' });
  }
});

app.get('/api/performance', (req, res) => {
  res.json({ success: true, data: performanceData });
});

app.get('/api/settings', (req, res) => {
  res.json({ success: true, data: settings });
});

app.post('/api/settings', (req, res) => {
  const updatedSettings = req.body;
  
  // Update settings with new values
  Object.keys(updatedSettings).forEach(key => {
    if (settings.hasOwnProperty(key)) {
      settings[key] = updatedSettings[key];
    }
  });
  
  res.json({ success: true, data: settings });
});

app.get('/api/notifications', (req, res) => {
  res.json({ success: true, data: notifications });
});

app.post('/api/notifications/:id/read', (req, res) => {
  const notification = notifications.find(n => n.id === req.params.id);
  if (notification) {
    notification.read = true;
    res.json({ success: true, data: true });
  } else {
    res.status(404).json({ success: false, error: 'Notification not found' });
  }
});

// WebSocket events
io.on('connection', (socket) => {
  console.log('New client connected');
  
  socket.on('disconnect', () => {
    console.log('Client disconnected');
  });
});

// Simulate real-time events
function simulateEvents() {
  // Randomly generate an event every 15-30 seconds
  const timeout = Math.floor(Math.random() * 15000) + 15000;
  
  setTimeout(() => {
    const eventTypes = [
      'code_changed',
      'new_patch_available',
      'performance_alert',
      'security_alert',
      'deprecation_alert'
    ];
    
    const eventType = eventTypes[Math.floor(Math.random() * eventTypes.length)];
    const notification = {
      id: `notif-${Date.now()}`,
      timestamp: new Date().toISOString(),
      title: getNotificationTitle(eventType),
      message: getNotificationMessage(eventType),
      type: getNotificationType(eventType),
      read: false
    };
    
    notifications.push(notification);
    io.emit(eventType, notification);
    
    console.log(`Emitted ${eventType} event`);
    simulateEvents();
  }, timeout);
}

function getNotificationTitle(eventType) {
  switch (eventType) {
    case 'code_changed':
      return 'Code Change Detected';
    case 'new_patch_available':
      return 'New Patch Available';
    case 'performance_alert':
      return 'Performance Hotspot Detected';
    case 'security_alert':
      return 'Security Vulnerability Detected';
    case 'deprecation_alert':
      return 'Deprecated API Usage Detected';
    default:
      return 'System Notification';
  }
}

function getNotificationMessage(eventType) {
  switch (eventType) {
    case 'code_changed':
      return 'Runtime changes have been applied to your application code.';
    case 'new_patch_available':
      return 'A new patch is available for your application.';
    case 'performance_alert':
      return `Method ${performanceData[Math.floor(Math.random() * performanceData.length)].methodName} is showing performance issues.`;
    case 'security_alert':
      return 'Potential SQL injection vulnerability detected and automatically mitigated.';
    case 'deprecation_alert':
      return 'Usage of deprecated API detected and automatically redirected.';
    default:
      return 'System notification from Dynamic Java Patcher.';
  }
}

function getNotificationType(eventType) {
  switch (eventType) {
    case 'code_changed':
      return 'info';
    case 'new_patch_available':
      return 'info';
    case 'performance_alert':
      return 'warning';
    case 'security_alert':
      return 'error';
    case 'deprecation_alert':
      return 'warning';
    default:
      return 'info';
  }
}

// Start the server
const PORT = process.env.PORT || 8080;
server.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  simulateEvents();
}); 