package com.habeshagram.server.core;

import com.habeshagram.common.exception.*;
import com.habeshagram.common.model.*;
import com.habeshagram.common.remote.IClientCallback;
import com.habeshagram.common.remote.IServer;
import com.habeshagram.server.persistence.*;
import com.habeshagram.server.router.MessageRouter;
import com.habeshagram.server.util.PasswordHasher;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class ServerImpl implements IServer {
    private final ClientRegistry clientRegistry;
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final MessageDAO messageDAO;
    private final MessageRouter messageRouter;
    
    public ServerImpl() {
        this.clientRegistry = new ClientRegistry();
        this.userDAO = new UserDAO();
        this.groupDAO = new GroupDAO();
        this.messageDAO = new MessageDAO();
        this.messageRouter = new MessageRouter(clientRegistry);
    }
    
    @Override
    public void registerUser(String username, String password) 
            throws RemoteException, UserAlreadyExistsException {
        if (userDAO.userExists(username)) {
            throw new UserAlreadyExistsException("Username already taken: " + username);
        }
        
        String passwordHash = PasswordHasher.hash(password);
        User user = new User(username, passwordHash);
        userDAO.saveUser(user);
        
        System.out.println("New user registered: " + username);
    }
    
    @Override
    public IClientCallback login(String username, String password, IClientCallback callback) 
            throws RemoteException, AuthenticationException {
        User user = userDAO.getUser(username);
        
        if (user == null) {
            throw new AuthenticationException("User not found: " + username);
        }
        
        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid password for user: " + username);
        }
        
        if (clientRegistry.isOnline(username)) {
            throw new AuthenticationException("User already logged in: " + username);
        }
        
        // Register client
        clientRegistry.registerClient(username, callback);
        userDAO.updateLastSeen(username);
        
        // Send system welcome message
        Message welcomeMsg = new Message(MessageType.SYSTEM, "System", 
                                        "Welcome to Habeshagram, " + username + "!");
        try {
            callback.receiveMessage(welcomeMsg);
        } catch (RemoteException e) {
            // Ignore
        }
        
        // Broadcast user joined message
        broadcastSystemMessage(username + " has joined the chat");
        
        // Deliver any pending offline messages
        List<Message> pendingMessages = messageDAO.getPendingMessages(username);
        for (Message msg : pendingMessages) {
            try {
                callback.receiveMessage(msg);
                messageDAO.markAsDelivered(msg.getId(), username);
            } catch (RemoteException e) {
                break; // Stop if client disconnects
            }
        }
        
        System.out.println("User logged in: " + username);
        return callback;
    }
    
    @Override
    public void logout(String username) throws RemoteException {
        clientRegistry.unregisterClient(username);
        userDAO.updateLastSeen(username);
        broadcastSystemMessage(username + " has left the chat");
        System.out.println("User logged out: " + username);
    }
    
@Override
public void sendBroadcast(String sender, String content) throws RemoteException {
    Message message = new Message(MessageType.BROADCAST, sender, content);
    
    // Deliver to all online users (including sender)
    Set<String> onlineUsers = clientRegistry.getOnlineUsers();
    for (String username : onlineUsers) {
        IClientCallback callback = clientRegistry.getClient(username);
        if (callback != null) {
            try {
                callback.receiveMessage(message);
            } catch (RemoteException e) {
                System.err.println("Failed to deliver broadcast to " + username + ": " + e.getMessage());
            }
        }
    }
    
    // Save to database for offline users
    messageDAO.saveMessage(message);
}

    @Override
public void sendPrivate(String sender, String recipient, String content) throws RemoteException {
    Message message = new Message(MessageType.PRIVATE, sender, recipient, content);
    
    // Always route to sender (so they see their own message)
    IClientCallback senderCallback = clientRegistry.getClient(sender);
    if (senderCallback != null) {
        try {
            senderCallback.receiveMessage(message);
        } catch (RemoteException e) {
            System.err.println("Failed to deliver to sender: " + e.getMessage());
        }
    }
    
    // Route to recipient if online
    if (clientRegistry.isOnline(recipient)) {
        IClientCallback recipientCallback = clientRegistry.getClient(recipient);
        if (recipientCallback != null) {
            try {
                recipientCallback.receiveMessage(message);
            } catch (RemoteException e) {
                System.err.println("Failed to deliver to recipient: " + e.getMessage());
            }
        }
    }
    
    // Always save to database
    messageDAO.saveMessage(message);
}
    
@Override
public void sendGroup(String sender, String groupName, String content) throws RemoteException {
    // Verify sender is a member of the group
    Group group = groupDAO.getGroup(groupName);
    if (group == null || !group.hasMember(sender)) {
        System.err.println("User " + sender + " attempted to send message to group " + groupName + " but is not a member");
        return;
    }
    
    Message message = new Message(MessageType.GROUP, sender, groupName, content);
    
    // Always deliver to sender first (so they see their own message immediately)
    IClientCallback senderCallback = clientRegistry.getClient(sender);
    if (senderCallback != null) {
        try {
            senderCallback.receiveMessage(message);
        } catch (RemoteException e) {
            System.err.println("Failed to deliver to sender: " + e.getMessage());
        }
    }
    
    // Route to all online group members (except sender, already got it)
    for (String member : group.getMembers()) {
        if (!member.equals(sender) && clientRegistry.isOnline(member)) {
            IClientCallback memberCallback = clientRegistry.getClient(member);
            if (memberCallback != null) {
                try {
                    memberCallback.receiveMessage(message);
                } catch (RemoteException e) {
                    System.err.println("Failed to deliver to group member " + member + ": " + e.getMessage());
                }
            }
        }
    }
    
    // Save to database for offline members
    messageDAO.saveMessage(message);
}
    
    @Override
    public void createGroup(String creator, String groupName) throws RemoteException {
        Group group = new Group(groupName, creator);
        groupDAO.saveGroup(group);
        broadcastSystemMessage("New group created: " + groupName + " by " + creator);
    }
    
    @Override
    public void joinGroup(String username, String groupName) 
            throws RemoteException, GroupNotFoundException {
        Group group = groupDAO.getGroup(groupName);
        if (group == null) {
            throw new GroupNotFoundException("Group not found: " + groupName);
        }
        
        group.addMember(username);
        groupDAO.updateGroup(group);
        
        // Notify group members
        notifyGroupMembers(groupName, username + " has joined the group");
    }
    
    @Override
    public List<String> getGroupMembers(String groupName) 
            throws RemoteException, GroupNotFoundException {
        Group group = groupDAO.getGroup(groupName);
        if (group == null) {
            throw new GroupNotFoundException("Group not found: " + groupName);
        }
        return List.copyOf(group.getMembers());
    }
    
    @Override
    public List<Group> getAvailableGroups() throws RemoteException {
        return groupDAO.getAllGroups();
    }
    
    @Override
    public List<String> getOnlineUsers() throws RemoteException {
        return List.copyOf(clientRegistry.getOnlineUsers());
    }
    
    private void broadcastSystemMessage(String content) {
        Message message = new Message(MessageType.SYSTEM, "System", content);
        messageRouter.routeBroadcast(message);
        messageDAO.saveMessage(message);
    }
    
    private void notifyGroupMembers(String groupName, String notification) {
        Group group = groupDAO.getGroup(groupName);
        if (group != null) {
            for (String member : group.getMembers()) {
                IClientCallback client = clientRegistry.getClient(member);
                if (client != null) {
                    try {
                        client.receiveGroupUpdate(groupName, notification);
                    } catch (RemoteException e) {
                        // Ignore
                    }
                }
            }
        }
    }

    // Add to ServerImpl.java

@Override
public List<Message> getRecentMessages(String username, int limit) throws RemoteException {
    return messageDAO.getRecentBroadcastMessages(limit);
}

@Override
public List<Message> getPrivateHistory(String user1, String user2, int limit) throws RemoteException {
    return messageDAO.getPrivateConversation(user1, user2, limit);
}

@Override
public List<Message> getGroupHistory(String username, String groupName, int limit) throws RemoteException {
    // Verify user is a member of the group
    Group group = groupDAO.getGroup(groupName);
    if (group == null || !group.hasMember(username)) {
        System.err.println("User " + username + " attempted to access group history for " + groupName + " but is not a member");
        return new ArrayList<>(); // Return empty list
    }
    
    return messageDAO.getGroupConversation(groupName, limit);
}


@Override
public List<User> getAllUsers() throws RemoteException {
    List<User> allUsers = userDAO.getAllUsers();
    
    // Update status for each user
    for (User user : allUsers) {
        if (clientRegistry.isOnline(user.getUsername())) {
            user.setStatus(UserStatus.ONLINE);
        } else {
            user.setStatus(UserStatus.OFFLINE);
        }
    }
    
    return allUsers;
}
}