import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, Observable, of, tap, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root', // Makes the service available application-wide
})
export class TicketPoolService {
  private apiUrl = 'http://localhost:8080/api/ticket-pool'; // Base URL of the backend

  constructor(private http: HttpClient) {}

  /**
   * Starts or resumes the ticket generation processes.
   *
   * @param vendorCount - Number of vendor threads to start
   * @param consumerCount - Number of consumer threads to start
   * @returns Observable with the backend's response as a string
   */
  startProcesses(vendorCount: number, consumerCount: number): Observable<string> {
    const params = new HttpParams()
      .set('vendorCount', vendorCount.toString()) // Set vendor count parameter
      .set('consumerCount', consumerCount.toString()); // Set consumer count parameter

    return this.http.post(`${this.apiUrl}/start`, null, {
      params, // Attach parameters to the request
      responseType: 'text', // Expect text response
    }).pipe(
      tap((response) => console.log('Start or resume processes successful:', response)), // Log success
      catchError((error) => {
        console.error('Error starting or resuming processes:', error); // Log error
        return throwError(() => new Error('Start or resume processes failed.')); // Throw custom error
      })
    );
  }

  /**
   * Stops all active processes.
   *
   * @returns Observable with the backend's response as a string
   */
  stopProcesses(): Observable<string> {
    return this.http.post(`${this.apiUrl}/stop`, null, {
      responseType: 'text', // Expect text response
    }).pipe(
      tap((response) => console.log('Processes stopped successfully:', response)), // Log success
      catchError((error) => {
        console.error('Error stopping processes:', error); // Log error
        return throwError(() => new Error('Stopping processes failed.')); // Throw custom error
      })
    );
  }

  /**
   * Resets the ticket pool.
   *
   * @returns Observable with the backend's response as a string
   */
  reset(): Observable<string> {
    return this.http.post(`${this.apiUrl}/reset`, null, { responseType: 'text' }).pipe(
      tap((response) => console.log('Pool reset successfully:', response)), // Log success
      catchError((error) => {
        console.error('Error resetting pool:', error); // Log error
        return throwError(() => new Error('Resetting pool failed.')); // Throw custom error
      })
    );
  }

  /**
   * Saves the current ticket pool configuration.
   *
   * @param config - Configuration object with pool parameters
   * @returns Observable with the backend's response as a string
   */
  saveConfiguration(config: any): Observable<string> {
    const params = new HttpParams()
      .set('maxTicketCapacity', config.maxCapacity)
      .set('totalTickets', config.totalTickets)
      .set('ticketReleaseRate', config.releaseRate)
      .set('customerTicketRetrievalRate', config.retrievalRate);

    return this.http.post(`${this.apiUrl}/save`, null, { params, responseType: 'text' }).pipe(
      tap((response) => console.log('Configuration saved successfully:', response)),
      catchError((error) => {
        console.error('Error details:', {
          status: error.status,
          message: error.message,
          error: error.error,
          url: error.url,
        });
        return throwError(() => new Error('Saving configuration failed.'));
      })
    );
  }


  /**
   * Fetches the current size of the ticket pool.
   *
   * @returns Observable with the current pool size as a number
   */
  getCurrentPoolSize(): Observable<number> {
    console.log('Fetching current pool size...'); // Log fetching operation
    return this.http.get<number>('http://localhost:8080/api/ticket-pool/size').pipe(
      tap((size) => console.log('Fetched size:', size)), // Log fetched size
      catchError((error) => {
        console.error('Error fetching pool size:', error); // Log error
        return of(0); // Return 0 if there's an error
      })
    );
  }
}
