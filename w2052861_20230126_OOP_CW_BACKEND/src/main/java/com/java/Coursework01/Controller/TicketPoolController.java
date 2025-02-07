package com.java.Coursework01.Controller;

// Importing required classes and libraries
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.Coursework01.Class.TicketPool;
import com.java.Coursework01.Repository.TicketPoolRepository;
import com.java.Coursework01.Service.TicketPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Marking this class as a REST controller
@RestController
@CrossOrigin(origins = "http://localhost:4200") // Allowing cross-origin requests from the frontend
@RequestMapping("/api/ticket-pool") // Base path for all endpoints in this controller
public class TicketPoolController {

    // Autowired dependencies for service, ticket pool, messaging template, and repository
    @Autowired
    private TicketPoolService ticketPoolService;

    @Autowired
    private TicketPool ticketPool;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TicketPoolRepository ticketPoolRepository;

    // Sends a log message to the frontend using WebSocket
    public void sendLog(String message) {
        messagingTemplate.convertAndSend("/topic/logs", message);
    }

    // Endpoint to send a test log message to the frontend
    @PostMapping("/send-log")
    public ResponseEntity<String> sendLog() {
        sendLog("Test log message from backend"); // Sends a predefined test log
        return ResponseEntity.ok("Log sent"); // Responds with a success message
    }

    // Exception handler for missing required request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName(); // Get the missing parameter's name
        return ResponseEntity.badRequest().body("Missing required parameter: " + name); // Respond with an error message
    }

    // Endpoint to initialize the ticket pool with the given parameters
    @PostMapping("/initialize")
    public ResponseEntity<String> initializePool(
            @RequestParam int maxTicketCapacity,
            @RequestParam int totalTickets,
            @RequestParam int ticketReleaseRate,
            @RequestParam int customerTicketRetrievalRate) {

        // Validate input parameters to ensure they are positive
        if (maxTicketCapacity <= 0 || totalTickets <= 0 || ticketReleaseRate <= 0 || customerTicketRetrievalRate <= 0) {
            return ResponseEntity.badRequest().body("All parameters must be positive integers.");
        }

        // Ensure total tickets do not exceed maximum capacity
        if (totalTickets > maxTicketCapacity) {
            return ResponseEntity.badRequest().body("Total tickets cannot exceed max capacity.");
        }

        // Synchronize access to ticket pool to safely update shared data
        synchronized (ticketPool) {
            ticketPool.setMaxTicketCapacity(maxTicketCapacity);
            ticketPool.setTotalTickets(totalTickets);
            ticketPool.setTicketReleaseRate(ticketReleaseRate);
            ticketPool.setCustomerTicketRetrievalRate(customerTicketRetrievalRate);
            ticketPool.setTicketsGenerated(0);
            ticketPool.setAvailable(true);

            // Clear existing tickets and initialize new ones
            ticketPool.getTickets().clear();
            for (int i = 0; i < totalTickets; i++) {
                ticketPool.addTicket();
            }
        }

        // Save the updated configuration and log the operation
        ticketPoolService.saveConfiguration(ticketPool);
        sendLog("Ticket pool initialized with max capacity: " + maxTicketCapacity +
                ", total tickets: " + totalTickets +
                ", release rate: " + ticketReleaseRate +
                ", retrieval rate: " + customerTicketRetrievalRate);

        // Respond with success message
        return ResponseEntity.ok("Ticket pool initialized successfully.");
    }

    // Endpoint to start vendor and consumer threads
    @PostMapping("/start")
    public ResponseEntity<String> startProcesses(@RequestParam int vendorCount,
                                                 @RequestParam int consumerCount) {

        // Validate input parameters to ensure they are positive
        if (vendorCount <= 0 || consumerCount <= 0) {
            return ResponseEntity.badRequest().body("Vendor and Consumer counts must be positive integers.");
        }

        // Synchronize access to safely modify the availability state
        synchronized (ticketPool) {
            if (!ticketPool.isAvailable()) {
                ticketPool.setAvailable(true);
            }
        }

        try {
            // Adjust rates for ticket release and retrieval based on vendor/consumer counts
            int adjustedTicketReleaseRate = Math.max(ticketPool.getTicketReleaseRate() / vendorCount, 1);
            int adjustedTicketRetrievalRate = Math.max(ticketPool.getCustomerTicketRetrievalRate() / consumerCount, 1);

            // Start threads for vendors and consumers
            ticketPoolService.startVendorThreads(vendorCount, adjustedTicketReleaseRate);
            ticketPoolService.startCustomerThreads(consumerCount, adjustedTicketRetrievalRate);

            // Log the operation
            sendLog("Processes resumed or started with " + vendorCount +
                    " vendors and " + consumerCount + " consumers.");
        } catch (Exception e) {
            // Respond with an error message if any exception occurs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting processes: " + e.getMessage());
        }

        // Respond with success message
        return ResponseEntity.ok("Processes started or resumed successfully.");
    }

    // Endpoint to stop all active processes
    @PostMapping("/stop")
    public ResponseEntity<String> stopProcesses() {
        ticketPool.setAvailable(false); // Set ticket pool to unavailable
        return ResponseEntity.ok("Processes stopped successfully."); // Respond with success message
    }

    // Endpoint to reset the ticket pool
    @PostMapping("/reset")
    public ResponseEntity<String> resetPool() {
        synchronized (ticketPool) {
            // Stop all threads and clear ticket pool data
            ticketPool.setAvailable(false);
            ticketPoolService.stopAllThreads();
            ticketPool.getTickets().clear();
            ticketPool.setTicketsGenerated(0);
            ticketPool.setTotalTickets(0);
            ticketPool.setMaxTicketCapacity(0);
            ticketPool.setTicketReleaseRate(0);
            ticketPool.setCustomerTicketRetrievalRate(0);

            // Log the reset operation
            sendLog("Ticket pool reset successfully.");
        }

        // Respond with success message
        return ResponseEntity.ok("Ticket pool has been reset.");
    }

    // Endpoint to stream logs to the frontend using Server-Sent Events (SSE)
    @GetMapping(value = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLogs() {
        SseEmitter emitter = new SseEmitter();

        // Periodically send log messages to the client
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                String logMessage = "Thread: " + Thread.currentThread().getName() +
                        " - Current Pool Size: " + ticketPool.getTickets().size() +
                        " - Time: " + System.currentTimeMillis();
                emitter.send(logMessage); // Send log message
            } catch (IOException e) {
                emitter.completeWithError(e); // Complete emitter with error if exception occurs
            }
        }, 10, 1, TimeUnit.SECONDS);

        return emitter; // Return the emitter for log streaming
    }

    // Endpoint to save the current ticket pool configuration
    @PostMapping("/save")
    public ResponseEntity<String> saveConfiguration(
            @RequestParam int maxTicketCapacity,
            @RequestParam int totalTickets,
            @RequestParam int ticketReleaseRate,
            @RequestParam int customerTicketRetrievalRate) {

        TicketPool configuration = new TicketPool();
        configuration.setMaxTicketCapacity(maxTicketCapacity);
        configuration.setTotalTickets(totalTickets);
        configuration.setTicketReleaseRate(ticketReleaseRate);
        configuration.setCustomerTicketRetrievalRate(customerTicketRetrievalRate);

        ticketPoolRepository.save(configuration);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Save configuration to a JSON file
            File file = new File("ticket-pool-configuration.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, configuration);

            // Respond with success message
            return ResponseEntity.ok("Configuration saved successfully to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error saving configuration: " + e.getMessage());
        }
    }

    // Endpoint to add a new vendor to the ticket pool
    @PostMapping("/addVendor")
    public ResponseEntity<String> addVendor(@RequestParam String vendorId, @RequestParam int ticketReleaseRate) {
        ticketPoolService.addVendor(vendorId, ticketReleaseRate); // Add vendor using service
        return ResponseEntity.ok("Vendor added successfully."); // Respond with success message
    }

    // Endpoint to remove an existing vendor from the ticket pool
    @DeleteMapping("/removeVendor")
    public ResponseEntity<String> removeVendor(@RequestParam String vendorId) {
        ticketPoolService.removeVendor(vendorId); // Remove vendor using service
        return ResponseEntity.ok("Vendor removed successfully."); // Respond with success message
    }

    // Endpoint to add a new customer to the ticket pool
    @PostMapping("/addCustomer")
    public ResponseEntity<String> addCustomer(@RequestParam String customerId, @RequestParam int ticketRetrievalRate) {
        ticketPoolService.addCustomer(customerId, ticketRetrievalRate); // Add customer using service
        return ResponseEntity.ok("Customer added successfully."); // Respond with success message
    }

    // Endpoint to remove an existing customer from the ticket pool
    @DeleteMapping("/removeCustomer")
    public ResponseEntity<String> removeCustomer(@RequestParam String customerId) {
        ticketPoolService.removeCustomer(customerId); // Remove customer using service
        return ResponseEntity.ok("Customer removed successfully."); // Respond with success message
    }

    // Endpoint to clear logs
    @PostMapping("/clear-logs")
    public ResponseEntity<String> clearLogs() {
        return ResponseEntity.ok("Logs cleared."); // Respond with success message
    }

    // Endpoint to get the current size of the ticket pool
    @GetMapping("/size")
    public ResponseEntity<Integer> getPoolSize() {
        int poolSize = ticketPool.getTickets().size(); // Retrieve current pool size
        return ResponseEntity.ok(poolSize); // Respond with the size
    }
}
