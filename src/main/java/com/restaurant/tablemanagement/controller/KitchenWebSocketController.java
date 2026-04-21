package com.restaurant.tablemanagement.controller;

import com.restaurant.tablemanagement.dto.KitchenUpdateMessage;
import com.restaurant.tablemanagement.service.KitchenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket message handler for kitchen real-time updates.
 * Handles STOMP messages from kitchen dashboard clients and broadcasts updates.
 */
@Controller
public class KitchenWebSocketController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    @Lazy
    private KitchenService kitchenService;
    
    /**
     * Handles initial connection requests from kitchen clients.
     * Sends a full refresh message with current state.
     * 
     * @return Full refresh message with current kitchen state
     */
    @MessageMapping("/kitchen/connect")
    @SendTo("/topic/kitchen/updates")
    public KitchenUpdateMessage handleConnect() {
        KitchenUpdateMessage msg = new KitchenUpdateMessage();
        msg.setUpdateType(KitchenUpdateMessage.UPDATE_TYPE_FULL_REFRESH);
        msg.setPendingOrders(kitchenService.getPendingOrders());
        msg.setPreparingOrders(kitchenService.getPreparingOrders());
        msg.setReadyOrders(kitchenService.getReadyOrders());
        msg.setCurrentStrategy(kitchenService.getCurrentStrategy());
        msg.setStatistics(kitchenService.getQueueStatistics());
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }
    
    /**
     * Broadcasts a kitchen update message to all connected clients.
     * This method is called by KitchenService when state changes occur.
     * 
     * @param message The update message to broadcast
     */
    public void broadcastUpdate(KitchenUpdateMessage message) {
        messagingTemplate.convertAndSend("/topic/kitchen/updates", message);
    }
}
