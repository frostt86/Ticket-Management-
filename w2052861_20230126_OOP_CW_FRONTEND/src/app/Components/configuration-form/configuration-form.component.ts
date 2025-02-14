import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoggerService } from '../../services/logger.service';
import { TicketPoolService } from '../../services/ticket-pool.service';
import { ChartsComponent } from '../charts/charts.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-configuration-form', // Component selector used in templates
  templateUrl: './configuration-form.component.html', // Template URL
  styleUrls: ['./configuration-form.component.css'], // CSS file URL
  standalone: true, // Marks this component as standalone
  imports: [
    ReactiveFormsModule, // Import for reactive forms
    CommonModule, // Common Angular directives (e.g., ngIf, ngFor)
    ChartsComponent, // Importing ChartsComponent
  ],
})
export class ConfigurationFormComponent implements OnInit, OnDestroy {
  configForm: FormGroup; // Reactive form group for configuration inputs
  logs: string[] = []; // Array to store real-time logs
  vendorCount: number = 1; // Initial number of vendors
  consumerCount: number = 1; // Initial number of consumers

  @ViewChild('logContainer') logContainer!: ElementRef; // Reference to the logs container for scrolling
  @ViewChild(ChartsComponent) chartsComponent!: ChartsComponent; // Reference to the ChartsComponent

  constructor(
    private fb: FormBuilder, // Form builder for creating the reactive form
    private loggerService: LoggerService, // Service for managing logs
    private ticketPoolService: TicketPoolService // Service for ticket pool operations
  ) {
    // Initialize the reactive form with default values and validators
    this.configForm = this.fb.group({
      totalTickets: [100, [Validators.required, Validators.min(1)]], // Total tickets (minimum 1)
      releaseRate: [5, [Validators.required, Validators.min(1)]], // Ticket release rate (minimum 1)
      retrievalRate: [1, [Validators.required, Validators.min(1)]], // Ticket retrieval rate (minimum 1)
      maxCapacity: [200, [Validators.required, Validators.min(1)]], // Maximum capacity (minimum 1)
    });
  }

  // Lifecycle hook: Called after component initialization
  ngOnInit(): void {
    this.loggerService.connect(); // Connect to the logger service

    // Subscribe to the log stream for real-time updates
    this.loggerService.getLogStream().subscribe({
      next: (logs) => {
        this.logs = logs; // Update logs array
        this.scrollToBottom(); // Scroll to the latest log
      },
      error: (error) => console.error('Error receiving logs:', error), // Log errors
    });
  }

  // Lifecycle hook: Called before the component is destroyed
  ngOnDestroy(): void {
    this.loggerService.disconnect(); // Disconnect from the logger service
  }

  // Handle form submission
  onSubmit(): void {
    if (this.configForm.valid) {
      const config = this.configForm.value; // Get form values
      this.loggerService.initializePool(config).subscribe({
        next: () => alert('Pool initialized successfully.'), // Show success alert
        error: (error) => {
          console.error('Initialization Error:', error); // Log error
          alert(`Initialization failed: ${error.message}`); // Show error alert
        },
      });
    } else {
      alert('Please complete all fields correctly.'); // Alert if form is invalid
    }
  }

  // Start ticket generation processes
  startGeneration(): void {
    this.ticketPoolService.startProcesses(this.vendorCount, this.consumerCount).subscribe({
      next: () => {
        this.startChartUpdates(); // Start real-time chart updates
      },
      error: (error) => {
        console.error('Start Processes Error:', error); // Log error
        alert(`Start processes failed: ${error.message}`); // Show error alert
      },
    });
  }

  // Start real-time chart updates
  startChartUpdates(): void {
    if (this.chartsComponent) {
      this.chartsComponent.startUpdates(); // Call the ChartsComponent method
    } else {
      console.error('ChartsComponent is not available!'); // Log error if ChartsComponent is missing
    }
  }

  // Stop real-time chart updates
  stopChartUpdates(): void {
    if (this.chartsComponent) {
      this.chartsComponent.stopUpdates(); // Call the ChartsComponent method
    } else {
      console.error('ChartsComponent is not available!'); // Log error if ChartsComponent is missing
    }
  }

  // Stop ticket generation processes
  stopProcesses(): void {
    this.ticketPoolService.stopProcesses().subscribe({
      next: () => alert('Processes stopped successfully.'), // Show success alert
      error: (error) => {
        console.error('Error stopping processes:', error); // Log error
        alert(`Failed to stop processes: ${error.message}`); // Show error alert
      },
    });
  }

  // Reset the ticket pool processes
  resetProcess(): void {
    this.ticketPoolService.reset().subscribe({
      next: (response) => {
        this.logs.push('Pool reset successfully: ' + response); // Add success log
        this.scrollToBottom(); // Scroll to latest log
      },
      error: (error) => {
        this.logs.push('Error resetting pool: ' + error.message); // Add error log
        this.scrollToBottom(); // Scroll to latest log
      },
    });
  }

  // Save the current configuration
  saveConfiguration(): void {
    if (this.configForm.valid) {
      const config = this.configForm.value;

      this.ticketPoolService.saveConfiguration(config).subscribe({
        next: (response) => {
          console.log('Success:', response);
          alert(response); // Display the success message
        },
        error: (error) => {
          console.error('Save Configuration Error:', error); // Log detailed error
          alert(`Failed to save configuration. Details: ${error.message}`);
        },
      });
    } else {
      alert('Please fill out the form correctly before saving.');
    }
  }


  // Clear all logs
  clearLogs(): void {
    this.loggerService.clearLogs().subscribe({
      next: () => {
        this.logs = []; // Clear logs array
        this.scrollToBottom(); // Scroll to latest log
        alert('Logs cleared successfully.'); // Show success alert
      },
      error: (error) => {
        console.error('Error clearing logs:', error); // Log error
        alert('Failed to clear logs.'); // Show error alert
      },
    });
  }

  // Increment the vendor count
  incrementVendor() {
    this.vendorCount++;
    this.logs.push(`Vendor count incremented to ${this.vendorCount}`); // Add log entry
    this.scrollToBottom(); // Scroll to latest log
  }

  // Decrement the vendor count
  decrementVendor() {
    if (this.vendorCount > 0) {
      this.vendorCount--;
      this.logs.push(`Vendor count decremented to ${this.vendorCount}`); // Add log entry
      this.scrollToBottom(); // Scroll to latest log
    }
  }

  // Increment the consumer count
  incrementConsumer() {
    this.consumerCount++;
    this.logs.push(`Consumer count incremented to ${this.consumerCount}`); // Add log entry
    this.scrollToBottom(); // Scroll to latest log
  }

  // Decrement the consumer count
  decrementConsumer() {
    if (this.consumerCount > 0) {
      this.consumerCount--;
      this.logs.push(`Consumer count decremented to ${this.consumerCount}`); // Add log entry
      this.scrollToBottom(); // Scroll to latest log
    }
  }

  // Scroll the log container to the bottom
  private scrollToBottom(): void {
    if (this.logContainer) {
      const element = this.logContainer.nativeElement;
      setTimeout(() => {
        element.scrollTop = element.scrollHeight; // Scroll to the bottom
      }, 0);
    }
  }
}
