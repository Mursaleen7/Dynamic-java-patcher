# Dynamic Java Patcher Frontend

A modern React TypeScript frontend for the Dynamic Java Patcher project. This interface allows users to view and interact with runtime patches, monitor performance data, and configure the Java patcher settings.

## Features

- **Patch Management**: View available patches, apply them to your application, and track their status
- **Code Change Visualization**: See before and after code changes for each patch
- **Real-time Notifications**: Get notified when patches are applied or when code changes occur
- **Performance Monitoring**: View performance hotspots identified by the profiler
- **Settings Configuration**: Configure monitored packages and feature settings

## Getting Started

### Prerequisites

- Node.js 16+ and npm/yarn
- Dynamic Java Patcher backend running (see main project README)

### Installation

1. Install dependencies:

```sh
npm install
# or
yarn install
```

2. Create a `.env` file in the frontend directory with the following configuration:

```
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_WS_URL=ws://localhost:8080/ws
```

Adjust the URLs as needed to point to your Java backend server.

### Development

Start the development server:

```sh
npm start
# or
yarn start
```

The application will be available at http://localhost:3000.

### Building for Production

Build the production-ready application:

```sh
npm run build
# or
yarn build
```

The build output will be in the `build` directory, which can be served by any static file server.

## Project Structure

- `src/components/` - React components
- `src/services/` - API and WebSocket services
- `src/types/` - TypeScript type definitions
- `src/App.tsx` - Main application component
- `src/index.tsx` - Application entry point

## Integration with Java Backend

This frontend is designed to work with the Dynamic Java Patcher backend. It communicates with the backend through:

1. **REST API** - For fetching and manipulating patches, settings, and performance data
2. **WebSocket** - For real-time notifications about patch application and code changes

## Contributing

Feel free to submit issues or pull requests to improve the frontend.

## License

See the LICENSE file in the main project repository for details. 