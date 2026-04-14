package com.habeshagram.server.router;

import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.MessageType;
import com.habeshagram.common.remote.IClientCallback;
import com.habeshagram.server.core.ClientRegistry;
import com.habeshagram.server.persistence.GroupDAO;

import java.rmi.RemoteException;
import java.util.Set;

public class MessageRouter {
    private final ClientRegistry clientRegistry;
    private final GroupDAO groupDAO;
    
    public MessageRouter(ClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
        this.groupDAO = new GroupDAO();
    }
    
    public void routeBroadcast(Message message) {
        Set<String> onlineUsers = clientRegistry.getOnlineUsers();
        
        for (String username : onlineUsers) {
            // Don't send broadcast back to sender
            if (!username.equals(message.getSender())) {
                deliverToUser(username, message);
            }
        }
    }
    
    public void routePrivate(Message message) {
        String recipient = message.getRecipient();
        
        // Deliver to recipient
        deliverToUser(recipient, message);
        
        // Also deliver to sender (as sent confirmation)
        if (!message.getSender().equals(recipient)) {
            deliverToUser(message.getSender(), message);
        }
    }
    
public void routeGroup(Message message) {
    String groupName = message.getRecipient();
    com.habeshagram.common.model.Group group = groupDAO.getGroup(groupName);
    
    if (group != null) {
        for (String member : group.getMembers()) {
            // Send to all group members except sender
            if (!member.equals(message.getSender())) {
                deliverToUser(member, message);
            }
        }
        
        // Deliver to sender as confirmation (they're already a member)
        deliverToUser(message.getSender(), message);
    }
}
    
    private void deliverToUser(String username, Message message) {
        IClientCallback client = clientRegistry.getClient(username);
        if (client != null) {
            try {
                client.receiveMessage(message);
            } catch (RemoteException e) {
                System.err.println("Failed to deliver message to " + username + ": " + e.getMessage());
                clientRegistry.unregisterClient(username);
            }
        }
    }
}