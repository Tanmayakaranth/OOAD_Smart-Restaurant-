package com.restaurant.tablemanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time kitchen updates.
 * Enables STOMP (Simple Text Oriented Messaging Protocol) for bidirectional communication
 * between server and kitchen dashboard clients.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker for handling subscriptions and broadcasting messages.
     * 
     * @param config Message broker registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker for /topic destinations
        config.enableSimpleBroker("/topic");
        
        // Set the prefix for messages from client to server
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers STOMP endpoints for WebSocket connection.
     * 
     * @param registry STOMP endpoint registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the WebSocket endpoint for kitchen updates
        registry.addEndpoint("/ws/kitchen")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // Enable SockJS fallback for browsers that don't support WebSocket
    }
}
