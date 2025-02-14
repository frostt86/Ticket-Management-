package com.java.Coursework01.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for setting up WebSocket messaging in the application.
 * Enables a simple in-memory message broker and configures WebSocket endpoints.
 */
@Configuration
@EnableWebSocketMessageBroker // Enables WebSocket message handling, backed by a message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker for handling WebSocket messages.
     *
     * @param config The MessageBrokerRegistry to configure message broker options.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Enable a simple in-memory message broker with "/topic" prefix
        config.setApplicationDestinationPrefixes("/app"); // Prefix for messages sent from clients to server-side application
    }

    /**
     * Registers STOMP (Simple Text Oriented Messaging Protocol) endpoints for WebSocket connections.
     *
     * @param registry The StompEndpointRegistry to register WebSocket endpoints.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-logs") // Define the WebSocket endpoint for logging
                .setAllowedOrigins("http://localhost:4200") // Allow connections from the Angular frontend
                .withSockJS(); // Enable SockJS fallback options for browsers that do not support WebSocket
    }
}
