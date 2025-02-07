package com.java.Coursework01.Service;

import com.java.Coursework01.Class.ConsumerThread;
import com.java.Coursework01.Class.TicketPool;
import com.java.Coursework01.Class.VendorThread;
import com.java.Coursework01.Repository.TicketPoolRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicketPoolService {
    // Logger for logging information and errors
    private static final Logger logger = LoggerFactory.getLogger(TicketPoolService.class);

    // Maps to keep track of vendor and customer threads
    private final ConcurrentHashMap<String, Thread> vendorThreads = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Thread> customerThreads = new ConcurrentHashMap<>();

    // Autowired dependencies
    @Autowired
    private TicketPool ticketPool;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TicketPoolRepository ticketPoolRepository;

    /**
     * Starts vendor threads with the specified count and ticket release rate.
     * Each vendor thread is identified by a unique vendor ID.
     *
     * @param vendorCount      Number of vendor threads to start
     * @param ticketReleaseRate Rate at which tickets are released by vendors
     */
    public void startVendorThreads(int vendorCount, int ticketReleaseRate) {
        for (int i = 0; i < vendorCount; i++) {
            String vendorId = "vendor-" + i; // Unique vendor identifier
            if (!vendorThreads.containsKey(vendorId) || !vendorThreads.get(vendorId).isAlive()) {
                VendorThread vendorThread = new VendorThread(ticketPool, ticketReleaseRate, messagingTemplate);
                Thread thread = new Thread(vendorThread);
                vendorThreads.put(vendorId, thread);
                thread.start();
            }
        }
    }

    /**
     * Starts customer threads with the specified count and ticket retrieval rate.
     * Each customer thread is identified by a unique customer ID.
     *
     * @param consumerCount       Number of customer threads to start
     * @param ticketRetrievalRate Rate at which tickets are retrieved by customers
     */
    public void startCustomerThreads(int consumerCount, int ticketRetrievalRate) {
        for (int i = 0; i < consumerCount; i++) {
            String customerId = "customer-" + i; // Unique customer identifier
            if (!customerThreads.containsKey(customerId) || !customerThreads.get(customerId).isAlive()) {
                ConsumerThread consumerThread = new ConsumerThread(ticketPool, ticketRetrievalRate, messagingTemplate);
                Thread thread = new Thread(consumerThread);
                customerThreads.put(customerId, thread);
                thread.start();
            }
        }
    }

    /**
     * Saves the current configuration of the ticket pool to the database.
     *
     * @param configuration The ticket pool configuration to save
     */
    @Transactional
    public void saveConfiguration(TicketPool configuration) {
        try {
            ticketPoolRepository.save(configuration);
            logger.info("Configuration saved successfully to the database.");
        } catch (Exception e) {
            logger.error("Error saving configuration to the database: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Adds a new vendor thread to the ticket pool.
     *
     * @param vendorId         Unique ID for the vendor
     * @param ticketReleaseRate Rate at which the vendor releases tickets
     */
    public void addVendor(String vendorId, int ticketReleaseRate) {
        VendorThread vendorThread = new VendorThread(ticketPool, ticketReleaseRate, messagingTemplate);
        Thread thread = new Thread(vendorThread);
        vendorThreads.put(vendorId, thread);
        thread.start();
    }

    /**
     * Removes an existing vendor thread from the ticket pool.
     *
     * @param vendorId Unique ID of the vendor to remove
     */
    public void removeVendor(String vendorId) {
        Thread thread = vendorThreads.get(vendorId);
        if (thread != null) {
            thread.interrupt();
            vendorThreads.remove(vendorId);
        }
    }

    /**
     * Adds a new customer thread to the ticket pool.
     *
     * @param customerId         Unique ID for the customer
     * @param ticketRetrievalRate Rate at which the customer retrieves tickets
     */
    public void addCustomer(String customerId, int ticketRetrievalRate) {
        ConsumerThread consumerThread = new ConsumerThread(ticketPool, ticketRetrievalRate, messagingTemplate);
        Thread thread = new Thread(consumerThread);
        customerThreads.put(customerId, thread);
        thread.start();
    }

    /**
     * Removes an existing customer thread from the ticket pool.
     *
     * @param customerId Unique ID of the customer to remove
     */
    public void removeCustomer(String customerId) {
        Thread thread = customerThreads.get(customerId);
        if (thread != null) {
            thread.interrupt();
            customerThreads.remove(customerId);
        }
    }

    /**
     * Stops all running threads (both vendors and customers) and clears the thread maps.
     */
    public void stopAllThreads() {
        // Interrupt all vendor threads
        for (Thread thread : vendorThreads.values()) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        }
        vendorThreads.clear(); // Clear the map

        // Interrupt all customer threads
        for (Thread thread : customerThreads.values()) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        }
        customerThreads.clear(); // Clear the map

        // Log the action
        logger.info("All threads have been stopped.");
    }
}
