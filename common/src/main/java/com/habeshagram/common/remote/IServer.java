package com.habeshagram.common.remote;

import com.habeshagram.common.exception.*;
import com.habeshagram.common.model.Group;
import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IServer extends Remote {
    // Authentication
    void registerUser(String username, String password) 
            throws RemoteException, UserAlreadyExistsException;
    
    IClientCallback login(String username, String password, IClientCallback callback) 
            throws RemoteException, AuthenticationException;
    
    void logout(String username) throws RemoteException;
    
    // Messaging
    void sendBroadcast(String sender, String content) throws RemoteException;
    
    void sendPrivate(String sender, String recipient, String content) throws RemoteException;
    
    void sendGroup(String sender, String groupName, String content) throws RemoteException;
    
    // Group management
    void createGroup(String creator, String groupName) throws RemoteException;
    
    void joinGroup(String username, String groupName) 
            throws RemoteException, GroupNotFoundException;
    
    List<String> getGroupMembers(String groupName) 
            throws RemoteException, GroupNotFoundException;
    
    List<Group> getAvailableGroups() throws RemoteException;
    
    // Utility
    List<String> getOnlineUsers() throws RemoteException;
    
    // NEW METHODS FOR MESSAGE HISTORY
    List<Message> getRecentMessages(String username, int limit) throws RemoteException;
    
    List<Message> getPrivateHistory(String user1, String user2, int limit) throws RemoteException;

        List<Message> getGroupHistory(String username, String groupName, int limit) throws RemoteException;

List<User> getAllUsers() throws RemoteException;

// Add to IServer.java
void leaveGroup(String username, String groupName) throws RemoteException, GroupNotFoundException;

// Add method:
void sendTypingIndicator(String username, String recipient) throws RemoteException;

void sendGroupTypingIndicator(String username, String groupName) throws RemoteException;

// Add method for broadcast typing
void sendBroadcastTypingIndicator(String username) throws RemoteException;

}