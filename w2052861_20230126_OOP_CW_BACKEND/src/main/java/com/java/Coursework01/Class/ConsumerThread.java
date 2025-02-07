package com.java.Coursework01.Class;

import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Represents a thread responsible for retrieving tickets from the ticket pool.
 * Implements the Runnable interface to allow running this class as a thread.
 */
public class ConsumerThread implements Runnable {

    // Reference to the ticket pool
    private final TicketPool ticketPool;

    // The rate at which tickets are retrieved, in milliseconds
    private final int ticketRetrievalRate;

    // Template for sending messages/logs to the frontend via WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructor to initialize the ConsumerThread with required dependencies.
     *
     * @param ticketPool         The ticket pool from which tickets will be retrieved.
     * @param ticketRetrievalRate The interval in milliseconds between ticket retrievals.
     * @param messagingTemplate   The messaging template for sending logs to the frontend.
     */
    public ConsumerThread(TicketPool ticketPool, int ticketRetrievalRate, SimpMessagingTemplate messagingTemplate) {
        this.ticketPool = ticketPool;
        this.ticketRetrievalRate = ticketRetrievalRate;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * The run method defines the logic executed by this thread.
     * Continuously retrieves tickets from the pool while it is available.
     */
    @Override
    public void run() {
        // Log the start of ticket retrieval
        messagingTemplate.convertAndSend("/topic/logs", "Starting ticket retrieval...");

        // Continue running as long as the ticket pool is available
        while (ticketPool.isAvailable()) {
            // Remove a ticket from the pool
            ticketPool.removeTicket();

            // Send a log message to the frontend with the current pool size
            messagingTemplate.convertAndSend("/topic/logs",
                    "Retrieved ticket. Remaining Pool Size: " + ticketPool.getTickets().size());
            try {
                // Sleep for the specified retrieval rate to control ticket consumption speed
                Thread.sleep(ticketRetrievalRate * 100L);
            } catch (InterruptedException e) {
                // Handle thread interruption gracefully
                Thread.currentThread().interrupt();
                messagingTemplate.convertAndSend("/topic/logs", "Ticket retrieval interrupted.");
            }
        }
    }
}
