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

        void createGroup(String creator, String groupName) throws RemoteException;

        void joinGroup(String username, String groupName)
                        throws RemoteException, GroupNotFoundException;

        List<String> getGroupMembers(String groupName)
                        throws RemoteException, GroupNotFoundException;

        List<Group> getAvailableGroups() throws RemoteException;

        List<String> getOnlineUsers() throws RemoteException;

        List<Message> getRecentMessages(String username, int limit) throws RemoteException;

        List<Message> getPrivateHistory(String user1, String user2, int limit) throws RemoteException;

        List<Message> getGroupHistory(String username, String groupName, int limit) throws RemoteException;

        List<User> getAllUsers() throws RemoteException;

        void leaveGroup(String username, String groupName) throws RemoteException, GroupNotFoundException;

        void sendTypingIndicator(String username, String recipient) throws RemoteException;

        void sendGroupTypingIndicator(String username, String groupName) throws RemoteException;

        void sendBroadcastTypingIndicator(String username) throws RemoteException;

        void setUserStatus(String username, String status) throws RemoteException;

        String getUserStatus(String username) throws RemoteException;

        void deleteMessage(String messageId, String username) throws RemoteException;

        void sendPrivateReply(String sender, String recipient, String content,
                        String replyToId, String replyToSender, String replyToContent)
                        throws RemoteException;

        void sendGroupReply(String sender, String groupName, String content,
                        String replyToId, String replyToSender, String replyToContent)
                        throws RemoteException;

        List<Message> getRecentPrivateMessages(String username, int limit) throws RemoteException;

        List<Message> getRecentGroupMessages(String username, int limit) throws RemoteException;

void markMessageAsDelivered(String messageId) throws RemoteException;
void markPrivateMessagesAsRead(String reader, String sender) throws RemoteException;
void markGroupMessagesAsRead(String reader, String groupName) throws RemoteException;
}