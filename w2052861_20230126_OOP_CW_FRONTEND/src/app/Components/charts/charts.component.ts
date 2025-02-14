import { Component, OnDestroy, OnInit } from '@angular/core';
import Chart from 'chart.js/auto';
import { Subscription, interval } from 'rxjs';
import { TicketPoolService } from '../../services/ticket-pool.service';

@Component({
  selector: 'app-charts', // Component selector for usage in templates
  templateUrl: './charts.component.html', // HTML template for the component
  styleUrls: ['./charts.component.css'], // CSS file for styling the component
  standalone: true, // Indicates this component is standalone
})
export class ChartsComponent implements OnInit, OnDestroy {
  private chart: Chart | undefined; // Chart.js instance for rendering the chart
  private updateSubscription: Subscription | undefined; // Subscription for real-time updates

  // Constructor to inject the TicketPoolService dependency
  constructor(private ticketPoolService: TicketPoolService) {}

  // Lifecycle hook called on component initialization
  ngOnInit(): void {
    this.initializeChart(); // Initialize the chart when the component is created
  }

  // Lifecycle hook called when the component is destroyed
  ngOnDestroy(): void {
    this.stopUpdates(); // Stop real-time updates and clean up subscriptions
  }

  // Method to initialize the Chart.js chart
  initializeChart(): void {
    const ctx = document.getElementById('ticketPoolChart') as HTMLCanvasElement; // Get the canvas element by ID
    if (this.chart) {
      this.chart.destroy(); // Destroy existing chart instance if it exists
    }

    if (!ctx) {
      console.error("Canvas element 'ticketPoolChart' not found."); // Log an error if canvas is not found
      return;
    }

    // Create a new Chart.js instance with configuration
    this.chart = new Chart(ctx, {
      type: 'line', // Type of chart (line chart)
      data: {
        labels: [], // Initially, no labels
        datasets: [
          {
            label: 'Ticket Pool Size', // Label for the dataset
            data: [], // Initially, no data
            borderColor: 'rgba(106, 27, 154, 1)', // Line color
            backgroundColor: 'rgba(106, 27, 154, 0.1)', // Fill color
            tension: 0.4, // Line tension for smoothing
            fill: true, // Enable area fill under the line
          },
        ],
      },
      options: {
        responsive: true, // Ensure chart adjusts to screen size
        scales: {
          x: {
            title: { display: true, text: 'Time' }, // X-axis title
          },
          y: {
            title: { display: true, text: 'Pool Size' }, // Y-axis title
            beginAtZero: true, // Ensure Y-axis starts at zero
          },
        },
      },
    });

    console.log("Chart initialized successfully."); // Log success message
  }

  // Method to start real-time updates for the chart
  startUpdates(): void {
    if (this.updateSubscription) {
      console.warn('Real-time updates are already running.'); // Warn if updates are already active
      return;
    }

    console.log('Starting real-time updates...'); // Log message for starting updates
    this.updateSubscription = interval(2000).subscribe(() => {
      // Fetch the current ticket pool size every 2 seconds
      this.ticketPoolService.getCurrentPoolSize().subscribe({
        next: (poolSize) => {
          if (!this.chart) {
            console.error('Chart is not initialized.'); // Log error if chart is undefined
            return;
          }

          console.log('Fetched pool size:', poolSize); // Log the fetched pool size

          const currentTime = new Date().toLocaleTimeString(); // Get the current time as a label

          // Add the new data point to the chart
          this.chart.data.labels?.push(currentTime); // Add the time label
          this.chart.data.datasets[0].data.push(poolSize); // Add the pool size data

          // Limit the number of data points to 20
          if ((this.chart?.data.labels?.length ?? 0) > 20) {
            this.chart.data.labels?.shift(); // Remove the oldest label
            this.chart.data.datasets[0].data?.shift(); // Remove the oldest data point
          }

          this.chart.update(); // Refresh the chart to show updated data
          console.log('Chart updated.'); // Log update success
        },
        error: (error) => {
          console.error('Error fetching ticket pool size:', error); // Log error if data fetching fails
        },
      });
    });
  }

  // Method to stop real-time updates for the chart
  stopUpdates(): void {
    if (this.updateSubscription) {
      console.log('Stopping real-time updates...'); // Log message for stopping updates
      this.updateSubscription.unsubscribe(); // Unsubscribe from the interval
      this.updateSubscription = undefined; // Clear the subscription reference
    } else {
      console.warn('No active updates to stop.'); // Warn if there are no active updates
    }
  }
}
