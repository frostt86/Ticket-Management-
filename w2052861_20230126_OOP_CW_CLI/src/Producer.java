public class Producer implements Runnable {
    // The Producer class represents a thread that continuously adds tickets to a shared ticket pool at a specified rate.

    private final TicketPool ticketPool; // Shared resource that manages the pool of tickets
    private final int releaseRate; // Time in milliseconds between each ticket addition

    // Constructor to initialize the Producer with a ticket pool and release rate
    public Producer(TicketPool ticketPool, int releaseRate) {
        this.ticketPool = ticketPool; // Assign the shared ticket pool
        this.releaseRate = releaseRate; // Set the rate at which tickets are released
    }

    @Override
    public void run() {
        // The run method defines the behavior of the Producer thread
        try {
            // Continuously produce tickets until the thread is interrupted
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(releaseRate * 50L); // Simulate a delay based on the release rate
                ticketPool.addTickets(1); // Add one ticket to the pool
            }
        } catch (InterruptedException e) {
            // Handle thread interruption gracefully
            Thread.currentThread().interrupt(); // Restore the thread's interrupted status
            System.out.println("Producer interrupted."); // Print a message indicating interruption
        }
    }
}
