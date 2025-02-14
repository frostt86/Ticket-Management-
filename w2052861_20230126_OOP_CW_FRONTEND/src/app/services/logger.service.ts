import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, throwError, tap } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root', // Makes this service available throughout the application
})
export class LoggerService {
  private stompClient!: Client; // STOMP client for managing WebSocket connection
  private logSubject = new BehaviorSubject<string[]>([]); // BehaviorSubject to store and stream real-time logs
  private backendUrl = 'http://localhost:8080/api/ticket-pool'; // Base URL for backend API endpoints

  constructor(private http: HttpClient) {}

  /**
   * Establishes a WebSocket connection to the backend.
   * Subscribes to the `/topic/logs` topic to receive real-time log updates.
   */
  connect(): void {
    const socket = new SockJS('http://localhost:8080/ws-logs'); // Create SockJS connection
    this.stompClient = new Client({
      webSocketFactory: () => socket, // Factory method for creating the WebSocket
      debug: (str) => console.log('WebSocket Debug:', str), // Debug logs for WebSocket events
      reconnectDelay: 5000, // Reconnect automatically every 5 seconds on disconnect
    });

    // On successful connection, subscribe to the log topic
    this.stompClient.onConnect = () => {
      console.log('WebSocket connected'); // Log successful connection
      this.stompClient.subscribe('/topic/logs', (message) => {
        const logs = this.logSubject.getValue(); // Get the current list of logs
        logs.push(message.body); // Add the new log message to the list
        this.logSubject.next(logs); // Update the BehaviorSubject with the new log list
        console.log('Log received:', message.body); // Log the received message
      });
    };

    // Handle STOMP protocol errors
    this.stompClient.onStompError = (frame) => {
      console.error('WebSocket STOMP error:', frame.headers['message']); // Log STOMP errors
    };

    this.stompClient.activate(); // Activate the WebSocket connection
  }

  /**
   * Disconnects the WebSocket connection if active.
   */
  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate(); // Deactivate the WebSocket connection
      console.log('Disconnected from WebSocket'); // Log successful disconnection
    }
  }

  /**
   * Provides an observable stream of logs stored in the `logSubject`.
   * Allows real-time log updates to be consumed by subscribers.
   */
  getLogStream(): Observable<string[]> {
    return this.logSubject.asObservable(); // Return the BehaviorSubject as an observable
  }

  /**
   * Sends a request to initialize the ticket pool with the given configuration.
   *
   * @param config - Configuration object containing pool parameters
   * @returns Observable of the backend's response
   */
  initializePool(config: any): Observable<string> {
    const params = new HttpParams()
      .set('maxTicketCapacity', config.maxCapacity) // Maximum ticket capacity
      .set('totalTickets', config.totalTickets) // Total tickets in the pool
      .set('ticketReleaseRate', config.releaseRate) // Ticket release rate
      .set('customerTicketRetrievalRate', config.retrievalRate); // Ticket retrieval rate

    // Send POST request to the backend to initialize the pool
    return this.http.post(`${this.backendUrl}/initialize`, null, {
      params,
      responseType: 'text', // Expect text response from the backend
    }).pipe(
      tap((response) => console.log('Initialization successful:', response)), // Log success response
      catchError((error) => {
        console.error('Error initializing pool:', error); // Log error
        return throwError(() => new Error('Initialization failed.')); // Rethrow error
      })
    );
  }

  /**
   * Sends a request to the backend to clear all logs.
   *
   * @returns Observable of the backend's response
   */
  clearLogs(): Observable<string> {
    // Send POST request to clear logs
    return this.http.post(`${this.backendUrl}/clear-logs`, null, { responseType: 'text' }).pipe(
      tap(() => console.log('Logs cleared successfully.')), // Log success message
      catchError((error) => {
        console.error('Error clearing logs:', error); // Log error
        return throwError(() => new Error('Clearing logs failed.')); // Rethrow error
      })
    );
  }
}
