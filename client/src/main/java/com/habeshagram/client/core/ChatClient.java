package com.habeshagram.client.core;

import com.habeshagram.common.exception.*;
import com.habeshagram.common.model.*;
import com.habeshagram.common.remote.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    private static final String SERVICE_NAME = "HabeshagramChatService";
    
    private IServer server;
    private String username;
    private ClientCallbackImpl callbackImpl;
    
    public boolean connect() {
        try {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            server = (IServer) registry.lookup(SERVICE_NAME);
            return true;
        } catch (Exception e) {
            System.err.println("Client connection error: " + e.getMessage());
            return false;
        }
    }
    
    public boolean register(String username, String password) throws RemoteException, UserAlreadyExistsException {
        server.registerUser(username, password);
        return true;
    }
    
    public boolean login(String username, String password) throws RemoteException, AuthenticationException {
        this.username = username;
        this.callbackImpl = new ClientCallbackImpl();
        IClientCallback callback = (IClientCallback) UnicastRemoteObject.exportObject(callbackImpl, 0);
        server.login(username, password, callback);
        return true;
    }
    
    public void logout() throws RemoteException {
        if (server != null && username != null) {
            server.logout(username);
        }
    }
    
    public void sendBroadcast(String content) throws RemoteException {
        server.sendBroadcast(username, content);
    }
    
    public void sendPrivate(String recipient, String content) throws RemoteException {
        server.sendPrivate(username, recipient, content);
    }
    
    public void sendGroup(String groupName, String content) throws RemoteException {
        server.sendGroup(username, groupName, content);
    }
    
    public void createGroup(String groupName) throws RemoteException {
        server.createGroup(username, groupName);
    }
    
    public void joinGroup(String groupName) throws RemoteException, GroupNotFoundException {
        server.joinGroup(username, groupName);
    }
    
    public List<String> getGroupMembers(String groupName) throws RemoteException, GroupNotFoundException {
        return server.getGroupMembers(groupName);
    }
    
    public List<Group> getAvailableGroups() throws RemoteException {
        return server.getAvailableGroups();
    }
    
    public List<String> getOnlineUsers() throws RemoteException {
        return server.getOnlineUsers();
    }
    
    public List<Message> getRecentMessages(String username, int limit) throws RemoteException {
        return server.getRecentMessages(username, limit);
    }
    
    public List<Message> getPrivateHistory(String user1, String user2, int limit) throws RemoteException {
        return server.getPrivateHistory(user1, user2, limit);
    }
    
   public List<Message> getGroupHistory(String username, String groupName, int limit) throws RemoteException {
    return server.getGroupHistory(username, groupName, limit);
    }
    
    public String getUsername() { return username; }
    public ClientCallbackImpl getCallbackImpl() { return callbackImpl; }

    public List<User> getAllUsers() throws RemoteException {
    return server.getAllUsers();
}

public void leaveGroup(String username, String groupName) 
    throws RemoteException, GroupNotFoundException {
    server.leaveGroup(username, groupName);
}

public void sendTypingIndicator(String username, String recipient) throws RemoteException {
    server.sendTypingIndicator(username, recipient);
}

public void sendGroupTypingIndicator(String username, String groupName) throws RemoteException {
    server.sendGroupTypingIndicator(username, groupName);
}

public void sendBroadcastTypingIndicator(String username) throws RemoteException {
    server.sendBroadcastTypingIndicator(username);
}

public void setUserStatus(String username, String status) throws RemoteException {
    server.setUserStatus(username, status);
}

public String getUserStatus(String username) throws RemoteException {
    return server.getUserStatus(username);
}

public void deleteMessage(String messageId, String username) throws RemoteException {
    server.deleteMessage(messageId, username);
}

public void sendPrivateReply(String recipient, String content,
                             String replyToId, String replyToSender, String replyToContent) 
                             throws RemoteException {
    server.sendPrivateReply(username, recipient, content, replyToId, replyToSender, replyToContent);
}

public void sendGroupReply(String groupName, String content,
                           String replyToId, String replyToSender, String replyToContent) 
                           throws RemoteException {
    server.sendGroupReply(username, groupName, content, replyToId, replyToSender, replyToContent);
}

public List<Message> getRecentPrivateMessages(String username, int limit) throws RemoteException {
    return server.getRecentPrivateMessages(username, limit);
}

public List<Message> getRecentGroupMessages(String username, int limit) throws RemoteException {
    return server.getRecentGroupMessages(username, limit);
}
}