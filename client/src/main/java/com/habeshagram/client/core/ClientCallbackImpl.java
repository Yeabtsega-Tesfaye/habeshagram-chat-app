package com.habeshagram.client.core;

import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.UserStatus;
import com.habeshagram.common.remote.IClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClientCallbackImpl implements IClientCallback {
    private List<Consumer<Message>> messageListeners = new ArrayList<>();
    private List<Consumer<UserStatus>> statusListeners = new ArrayList<>();
    private List<Consumer<String>> groupUpdateListeners = new ArrayList<>();
    
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
}