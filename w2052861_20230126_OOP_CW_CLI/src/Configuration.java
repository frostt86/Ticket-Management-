import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import java.io.FileReader;

public class Configuration {
    // Represents the configuration settings for the ticketing system

    private final int totalTickets; // Total number of tickets to be managed
    private final int ticketReleaseRate; // Rate at which tickets are added to the pool
    private final int customerRetrievalRate; // Rate at which tickets are retrieved by customers
    private final int maxTicketCapacity; // Maximum capacity of the ticket pool

    // Constructor to initialize the configuration settings
    public Configuration(int totalTickets, int ticketReleaseRate, int customerRetrievalRate, int maxTicketCapacity) {
        this.totalTickets = totalTickets;
        this.ticketReleaseRate = ticketReleaseRate;
        this.customerRetrievalRate = customerRetrievalRate;
        this.maxTicketCapacity = maxTicketCapacity;
    }

    // Getter for totalTickets
    public int getTotalTickets() {
        return totalTickets;
    }

    // Getter for ticketReleaseRate
    public int getTicketReleaseRate() {
        return ticketReleaseRate;
    }

    // Getter for customerRetrievalRate
    public int getCustomerRetrievalRate() {
        return customerRetrievalRate;
    }

    // Getter for maxTicketCapacity
    public int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }


    // Saves the configuration settings to a file in JSON format
    public void saveToFile(String filename) {
        Gson gson = new Gson(); // Create a Gson instance for JSON conversion
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(this, writer); // Serialize the configuration object to JSON
            System.out.println("Configuration saved successfully."); // Confirmation message
        } catch (IOException e) {
            System.out.println(String.format("Error writing to file: %s", e.getMessage())); // Error handling
        }
    }

    // Loads the configuration settings from a JSON file
    public static Configuration loadFromFile(String filename) {
        Gson gson = new Gson(); // Create a Gson instance for JSON parsing
        try (FileReader reader = new FileReader(filename)) {
            return gson.fromJson(reader, Configuration.class); // Deserialize the JSON into a Configuration object
        } catch (IOException e) {
            System.out.println(String.format("Error loading configuration: %s", e.getMessage())); // Error handling
            return null; // Return null if an error occurs
        }
    }
}
