package com.java.Coursework01.Class;

import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Represents a thread responsible for generating tickets in the ticket pool.
 * Implements the Runnable interface to allow running this class as a thread.
 */
public class VendorThread implements Runnable {

    // Reference to the ticket pool
    private final TicketPool ticketPool;

    // The rate at which tickets are released, in milliseconds
    private final int ticketReleaseRate;

    // Template for sending messages/logs to the frontend via WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructor to initialize the VendorThread with required dependencies.
     *
     * @param ticketPool       The ticket pool to which tickets will be added.
     * @param ticketReleaseRate The interval in milliseconds between ticket additions.
     * @param messagingTemplate The messaging template for sending logs to the frontend.
     */
    public VendorThread(TicketPool ticketPool, int ticketReleaseRate, SimpMessagingTemplate messagingTemplate) {
        this.ticketPool = ticketPool;
        this.ticketReleaseRate = ticketReleaseRate;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * The run method defines the logic executed by this thread.
     * Continuously adds tickets to the pool while it is available and within capacity.
     */
    @Override
    public void run() {
        // Continue running as long as the pool is available and has not reached max capacity
        while (ticketPool.isAvailable() && ticketPool.getTicketsGenerated() != ticketPool.getMaxTicketCapacity()) {
            // Add a ticket to the pool
            ticketPool.addTicket();

            // Send a log message to the frontend with the current pool size and total tickets generated
            messagingTemplate.convertAndSend("/topic/logs",
                    "Generated ticket. Pool Size: " + ticketPool.getTickets().size() +
                            " total generated tickets: " + ticketPool.getTicketsGenerated());
            try {
                // Sleep for the specified release rate to control ticket generation speed
                Thread.sleep(ticketReleaseRate * 100L);
            } catch (InterruptedException e) {
                // Handle thread interruption gracefully
                Thread.currentThread().interrupt();
                messagingTemplate.convertAndSend("/topic/logs", "Ticket generation interrupted.");
            }
        }
    }
}
