package com.habeshagram.client.core;

import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.UserStatus;
import com.habeshagram.common.remote.IClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

public class ClientCallbackImpl implements IClientCallback {
    private List<Consumer<Message>> messageListeners = new ArrayList<>();
    private List<Consumer<UserStatus>> statusListeners = new ArrayList<>();
    private List<Consumer<String>> groupUpdateListeners = new ArrayList<>();
    private List<Consumer<StatusChangeEvent>> statusChangeListeners = new ArrayList<>();
    private final List<Consumer<String>> readListeners = new ArrayList<>();

    @Override
    public void receiveMessage(Message msg) throws RemoteException {
        for (Consumer<Message> listener : messageListeners) {
            listener.accept(msg);
        }
    }

    @Override
    public void userStatusChanged(String username, UserStatus status) throws RemoteException {
        for (Consumer<UserStatus> listener : statusListeners) {
            // Create a temporary object to pass both username and status
            listener.accept(status);
        }
    }

    @Override
    public void receiveGroupUpdate(String groupName, String message) throws RemoteException {
        for (Consumer<String> listener : groupUpdateListeners) {
            listener.accept(groupName + ": " + message);
        }
    }

    public void addMessageListener(Consumer<Message> listener) {
        messageListeners.add(listener);
    }

    public void addStatusListener(Consumer<UserStatus> listener) {
        statusListeners.add(listener);
    }

    public void addGroupUpdateListener(Consumer<String> listener) {
        groupUpdateListeners.add(listener);
    }

    private List<Consumer<String>> typingListeners = new ArrayList<>();

    @Override
    public void userTyping(String username, String recipient) throws RemoteException {
        for (Consumer<String> listener : typingListeners) {
            listener.accept(username);
        }
    }

    private List<Consumer<GroupTypingEvent>> groupTypingListeners = new ArrayList<>();

    @Override
    public void groupUserTyping(String username, String groupName) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            for (Consumer<GroupTypingEvent> listener : groupTypingListeners) {
                listener.accept(new GroupTypingEvent(username, groupName));
            }
        });
    }

    public void addGroupTypingListener(Consumer<GroupTypingEvent> listener) {
        groupTypingListeners.add(listener);
    }

    // Inner class for group typing event
    public static class GroupTypingEvent {
        private final String username;
        private final String groupName;

        public GroupTypingEvent(String username, String groupName) {
            this.username = username;
            this.groupName = groupName;
        }

        public String getUsername() {
            return username;
        }

        public String getGroupName() {
            return groupName;
        }
    }

    public void addTypingListener(Consumer<String> listener) {
        typingListeners.add(listener);
    }

    @Override
    public void userStatusMessageChanged(String username, String newStatus) throws RemoteException {
        for (Consumer<StatusChangeEvent> listener : statusChangeListeners) {
            listener.accept(new StatusChangeEvent(username, newStatus));
        }
    }

    public void addStatusChangeListener(Consumer<StatusChangeEvent> listener) {
        statusChangeListeners.add(listener);
    }

    // Inner class for status change event
    public static class StatusChangeEvent {
        private final String username;
        private final String newStatus;

        public StatusChangeEvent(String username, String newStatus) {
            this.username = username;
            this.newStatus = newStatus;
        }

        public String getUsername() {
            return username;
        }

        public String getNewStatus() {
            return newStatus;
        }
    }

    private List<Consumer<String>> deleteListeners = new ArrayList<>();

    @Override
    public void messageDeleted(String messageId) throws RemoteException {
        for (Consumer<String> listener : deleteListeners) {
            listener.accept(messageId);
        }
    }

    public void addDeleteListener(Consumer<String> listener) {
        deleteListeners.add(listener);
    }

    @Override
    public void messagesRead(String reader) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            for (Consumer<String> listener : readListeners) {
                listener.accept(reader);
            }
        });
    }

    public void addReadListener(Consumer<String> listener) {
        readListeners.add(listener);
    }
}