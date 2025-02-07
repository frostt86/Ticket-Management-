public class Consumer implements Runnable {
    // The Consumer class represents a thread that retrieves tickets from a shared ticket pool at a specified rate.

    private final TicketPool ticketPool; // Shared resource that manages tickets
    private final int retrievalRate; // Rate at which tickets are retrieved (in arbitrary units)

    // Constructor to initialize the Consumer with a ticket pool and retrieval rate
    public Consumer(TicketPool ticketPool, int retrievalRate) {
        this.ticketPool = ticketPool; // Assign the shared ticket pool
        this.retrievalRate = retrievalRate; // Set the retrieval rate for ticket consumption
    }

    @Override
    public void run() {
        // The run method defines the behavior of the Consumer thread
        try {
            // Continuously attempt to retrieve tickets until the thread is interrupted
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(retrievalRate * 80L); // Simulate a delay based on the retrieval rate
                ticketPool.buyTicket(); // Attempt to retrieve a ticket from the pool
            }
        } catch (InterruptedException e) {
            // Handle thread interruption gracefully
            System.out.println("Consumer interrupted."); // Print a message indicating interruption
        }
    }
}
