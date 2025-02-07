package com.java.Coursework01.Class;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Component // Marks this class as a Spring-managed component
@Entity // Indicates that this class is a JPA entity mapped to a database table
public class TicketPool {
    // Logger for logging information and errors
    private static final Logger logger = LoggerFactory.getLogger(TicketPool.class);

    // Primary key for the TicketPool entity with auto-generated value
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Total number of tickets in the pool
    private int totalTickets;

    // Rate at which tickets are released by vendors
    private int ticketReleaseRate;

    // Rate at which tickets are retrieved by customers
    private int customerTicketRetrievalRate;

    // Maximum capacity of the ticket pool
    private int maxTicketCapacity;

    // Number of tickets generated so far, with default value 0
    @Column(name = "tickets_generated", nullable = false, columnDefinition = "integer default 0")
    private int ticketsGenerated;

    // Boolean to indicate whether the ticket pool is available
    private boolean Available = false;

    // A list to store tickets, mapped to a separate table in the database
    @ElementCollection
    @CollectionTable(name = "tickets", joinColumns = @JoinColumn(name = "ticket_pool_id"))
    @Column(name = "ticket")
    private List<Integer> tickets;

    // Default constructor to initialize the ticket list as a synchronized list
    public TicketPool() {
        this.tickets = Collections.synchronizedList(new ArrayList<>());
    }

    // Parameterized constructor to initialize the ticket pool with specified attributes
    public TicketPool(int totalTickets, int ticketReleaseRate, int customerTicketRetrievalRate, int maxTicketCapacity) {
        this.totalTickets = totalTickets;
        this.ticketReleaseRate = ticketReleaseRate;
        this.customerTicketRetrievalRate = customerTicketRetrievalRate;
        this.maxTicketCapacity = maxTicketCapacity;
        this.tickets = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Adds a ticket to the pool.
     * If the pool is at maximum capacity, waits until space is available.
     */
    public synchronized void addTicket() {
        try {
            while (tickets.size() >= maxTicketCapacity) {
                wait(); // Wait until there is space in the pool
            }
            tickets.add(tickets.size() + 1); // Add a ticket to the pool
            ticketsGenerated++; // Increment the generated ticket count
            logger.info("Added ticket. Pool size: {}, Total generated: {}", tickets.size(), ticketsGenerated);
            notifyAll(); // Notify consumers waiting for tickets
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            logger.error("Thread interrupted while waiting to add ticket.", e);
        }
    }

    /**
     * Removes a ticket from the pool.
     * If the pool is empty, waits until tickets are available.
     */
    public synchronized void removeTicket() {
        try {
            while (tickets.isEmpty()) {
                wait(); // Wait until there are tickets to remove
            }
            tickets.remove(tickets.size() - 1); // Remove a ticket from the pool
            logger.info("Ticket removed. Remaining tickets: {}", tickets.size());
            notifyAll(); // Notify producers waiting to add tickets
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            logger.error("Thread interrupted while waiting for tickets.", e);
        }
    }

    /**
     * Checks if the ticket pool is available.
     *
     * @return true if the pool is available, false otherwise.
     */
    public synchronized boolean isAvailable() {
        return Available;
    }
}
