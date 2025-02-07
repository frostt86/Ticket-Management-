import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TicketPool {
    // Manages a pool of tickets with synchronized methods for adding and removing tickets.

    private int ticketsAvailable; // Tracks the number of tickets currently available
    private int generatedTickets = 0; // Tracks the total number of tickets generated
    private final int maxCapacity; // Maximum capacity of the ticket pool
    private static final Logger logger = Logger.getLogger(TicketPool.class.getName()); // Logger for logging events
    private final List<Integer> tickets; // List to store ticket IDs

    // Static block to configure the logger
    static {
        try {
            // Set up a FileHandler to save logs to a file
            FileHandler fileHandler = new FileHandler("ticket_pool.log", true);
            fileHandler.setFormatter(new SimpleFormatter()); // Format logs for readability
            fileHandler.setLevel(Level.ALL); // Capture all log levels
            logger.addHandler(fileHandler);

            // Set up a ConsoleHandler for console output
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL); // Capture all log levels
            logger.addHandler(consoleHandler);

            // Ensure all logs are captured by setting the logger level
            logger.setLevel(Level.ALL);

            // Disable parent handlers to prevent duplicate log entries
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            // Handle logging setup failure
            System.err.println("Failed to initialize logger for TicketPool: " + e.getMessage());
        }
    }

    // Constructor to initialize the TicketPool with available tickets and maximum capacity
    public TicketPool(int ticketsAvailable, int maxCapacity) {
        this.ticketsAvailable = ticketsAvailable; // Set initial available tickets
        this.maxCapacity = maxCapacity; // Set maximum pool capacity
        this.tickets = new ArrayList<>(); // Initialize the ticket list
        this.generatedTickets = ticketsAvailable;

        // Populate the ticket list with initial tickets
        for (int i = 0; i < ticketsAvailable; i++) {
            tickets.add(i + 1); // Assign integer IDs to tickets
        }
    }

    // Synchronized method to add tickets to the pool
    public synchronized void addTickets(int amount) throws InterruptedException {
        // Wait if the pool is at maximum capacity
        while (tickets.size() >= maxCapacity || generatedTickets >= maxCapacity) {
            logger.warning("Cannot add ticket. Pool is at maximum capacity."); // Log capacity warning
            wait(); // Wait for capacity to become available
        }

        // Add tickets to the pool up to the maximum capacity
        for (int i = 0; i < amount && tickets.size() < maxCapacity; i++) {
            generatedTickets++; // Increment the total generated tickets count
            tickets.add(tickets.size() + 1); // Add a new ticket to the pool
        }

        ticketsAvailable = tickets.size(); // Update the count of available tickets
        logger.info(String.format("Tickets added: %d. Total tickets now: %d. Total generated tickets: %d",
                amount, ticketsAvailable, generatedTickets)); // Log the addition of tickets

        notifyAll(); // Notify waiting threads that tickets are available
    }

    // Synchronized method to purchase (remove) a ticket from the pool
    public synchronized void buyTicket() throws InterruptedException {
        // Wait if no tickets are available
        while (tickets.isEmpty()) {
            logger.warning("Tickets not available, Please Wait...."); // Log ticket unavailability
            wait(); // Wait for tickets to become available
        }

        Integer purchasedTicket = tickets.remove(0); // Remove the first ticket from the list
        ticketsAvailable = tickets.size(); // Update the count of available tickets
        logger.info(String.format("Ticket purchased. Remaining tickets now: %d", ticketsAvailable)); // Log the purchase
    }

    // Synchronized method to get the number of available tickets
    public synchronized int getAvailableTickets() {
        return tickets.size(); // Return the size of the ticket list
    }
}
