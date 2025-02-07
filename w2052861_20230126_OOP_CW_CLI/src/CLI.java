import java.util.Scanner;

public class CLI {
    // Shared resources and system components
    private static TicketPool ticketPool; // Manages the pool of tickets
    private static Thread producerThread; // Thread for producing tickets
    private static Thread consumerThread; // Thread for consuming tickets
    private static final String CONFIG_FILE = "config.json"; // Configuration file path
    private static Configuration config; // Configuration object for system settings
    private static Thread monitorThread; // Thread for monitoring system status
    private static boolean systemRunning = false; // Indicates if the system is currently running
    private static boolean configurationUpdated = false; // Fix: Tracks if manual configuration has been done

    public static void main(String[] args) {
        loadConfiguration(); // Load configuration on startup
        initializeSystemComponents(); // Initialize system components

        Scanner inp = new Scanner(System.in); // Scanner for user input
        String input;

        System.out.println("Enter 'start', 'stop', 'check', 'config', or 'exit':");

        // Main loop to handle user input
        while (!(input = inp.nextLine()).equals("exit")) {
            switch (input.toLowerCase().trim()) {
                case "start":
                    handleStart(inp); // Handle start logic
                    break;
                case "stop":
                    stopSystem(); // Stop the system
                    break;
                case "check":
                    handleCheck(); // Check ticket pool status
                    break;
                case "config":
                    configureSystem(inp); // Manually configure the system
                    break;
                default:
                    System.out.println("Invalid command. Please use 'start', 'stop', 'check', 'config', or 'exit'.");
                    break;
            }
        }
        System.out.println("Exiting...");
        stopSystem();
        inp.close();
    }

    // Load configuration from file or initialize with defaults
    private static void loadConfiguration() {
        config = Configuration.loadFromFile(CONFIG_FILE);
        if (config == null) {
            config = new Configuration(100, 10, 5, 200); // Default configuration
            config.saveToFile(CONFIG_FILE); // Save default configuration
        }
    }

    // Initialize ticket pool and threads
    private static void initializeSystemComponents() {
        ticketPool = new TicketPool(config.getTotalTickets(), config.getMaxTicketCapacity());
        producerThread = new Thread(new Producer(ticketPool, config.getTicketReleaseRate()));
        consumerThread = new Thread(new Consumer(ticketPool, config.getCustomerRetrievalRate()));
    }

    // Handle the "start" command
    private static void handleStart(Scanner inp) {
        if (!configurationUpdated) {
            System.out.println("Do you want to load an existing configuration? (yes/no):");
            String answer = inp.nextLine().trim().toLowerCase();

            if (answer.equals("yes")) {
                loadConfiguration(); // Load existing configuration
            } else {
                configureSystem(inp); // Manually configure the system
            }
            config.saveToFile(CONFIG_FILE); // Save configuration
        }

        if (!systemRunning) {
            startSystem(); // Start the system
        } else {
            System.out.println("System is already running.");
        }
    }

    // Check the ticket pool status
    private static void handleCheck() {
        if (ticketPool != null) {
            System.out.println("Tickets available: " + ticketPool.getAvailableTickets());
        } else {
            System.out.println("System not configured yet.");
        }
    }

    // Start the ticketing system
    private static void startSystem() {
        if (systemRunning) {
            System.out.println("System is already running.");
            return;
        }
        System.out.println("Starting Ticketing System...");
        systemRunning = true;

        // Recreate threads
        producerThread = new Thread(new Producer(ticketPool, config.getTicketReleaseRate()));
        consumerThread = new Thread(new Consumer(ticketPool, config.getCustomerRetrievalRate()));

        // Start threads
        startMonitoring();
        producerThread.start();
        consumerThread.start();
    }

    // Stop the ticketing system
    private static void stopSystem() {
        if (!systemRunning) {
            System.out.println("System is not running.");
            return;
        }
        System.out.println("Stopping Ticketing System...");
        systemRunning = false;

        // Interrupt threads to stop them
        if (producerThread != null) {
            producerThread.interrupt();
        }
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
        if (monitorThread != null && monitorThread.isAlive()) {
            monitorThread.interrupt();
        }
    }

    // Manually configure the system
    private static void configureSystem(Scanner inp) {
        int totalTickets = getPositiveInp(inp, "Enter total number of tickets: ");
        int ticketReleaseRate = getPositiveInp(inp, "Enter ticket release rate: ");
        int customerRetrievalRate = getPositiveInp(inp, "Enter customer ticket retrieval rate: ");
        int maxTicketCapacity = getPositiveInp(inp, "Enter maximum ticket capacity: ");

        config = new Configuration(totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);
        config.saveToFile(CONFIG_FILE); // Save updated configuration
        initializeSystemComponents(); // Reinitialize system components with updated configuration
        configurationUpdated = true; // Fix: Set flag to true after manual configuration
        System.out.println("Configuration updated and saved.");
    }

    // Get a positive integer input from the user
    private static int getPositiveInp(Scanner inp, String prompt) {
        int values;
        do {
            System.out.println(prompt);
            while (!inp.hasNextInt()) {
                System.out.println("Please enter a positive integer.");
                inp.next();
            }
            values = inp.nextInt();
            inp.nextLine();
        } while (values < 0);
        return values;
    }

    // Start monitoring thread
    private static void startMonitoring() {
        monitorThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Tickets available: " + ticketPool.getAvailableTickets());
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("Monitoring thread interrupted.");
                Thread.currentThread().interrupt();
            }
        });
        monitorThread.start();
    }
}
