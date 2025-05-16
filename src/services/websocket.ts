import { io, Socket } from 'socket.io-client';
import { Notification, Patch } from '../types';

// Event types
type EventCallback<T> = (data: T) => void;
type ErrorCallback = (error: string) => void;

// Event names
export enum WebSocketEvent {
  PATCH_APPLIED = 'patch_applied',
  PATCH_FAILED = 'patch_failed',
  NEW_PATCH_AVAILABLE = 'new_patch_available',
  CODE_CHANGED = 'code_changed',
  PERFORMANCE_ALERT = 'performance_alert',
  SECURITY_ALERT = 'security_alert',
  DEPRECATION_ALERT = 'deprecation_alert',
}

class WebSocketService {
  private socket: Socket | null = null;
  private connected: boolean = false;
  private url: string = '';
  private callbacks: Map<string, EventCallback<any>[]> = new Map();
  private errorCallbacks: ErrorCallback[] = [];

  // Initialize the WebSocket connection
  public init(url: string = process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws'): void {
    if (this.socket) {
      this.socket.disconnect();
    }

    this.url = url;
    this.socket = io(url, {
      reconnection: true,
      reconnectionAttempts: 5,
      reconnectionDelay: 1000,
    });

    this.socket.on('connect', () => {
      this.connected = true;
      console.log('WebSocket connected');
    });

    this.socket.on('disconnect', () => {
      this.connected = false;
      console.log('WebSocket disconnected');
    });

    this.socket.on('error', (error: string) => {
      console.error('WebSocket error:', error);
      this.errorCallbacks.forEach(callback => callback(error));
    });

    // Register event handlers
    Object.values(WebSocketEvent).forEach(event => {
      this.socket?.on(event, (data: any) => {
        const callbacks = this.callbacks.get(event) || [];
        callbacks.forEach(callback => callback(data));
      });
    });
  }

  // Register event listeners
  public on<T>(event: WebSocketEvent, callback: EventCallback<T>): void {
    const callbacks = this.callbacks.get(event) || [];
    callbacks.push(callback);
    this.callbacks.set(event, callbacks);
  }

  // Register error listener
  public onError(callback: ErrorCallback): void {
    this.errorCallbacks.push(callback);
  }

  // Remove event listeners
  public off<T>(event: WebSocketEvent, callback: EventCallback<T>): void {
    const callbacks = this.callbacks.get(event) || [];
    const index = callbacks.indexOf(callback);
    if (index !== -1) {
      callbacks.splice(index, 1);
      this.callbacks.set(event, callbacks);
    }
  }

  // Disconnect WebSocket
  public disconnect(): void {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
      this.connected = false;
    }
  }

  // Check if connected
  public isConnected(): boolean {
    return this.connected;
  }
}

// Create a singleton instance
export const webSocketService = new WebSocketService(); 