package com.habeshagram.server.core;

import com.habeshagram.common.model.UserStatus;
import com.habeshagram.common.remote.IClientCallback;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistry {
    private final Map<String, IClientCallback> activeClients;
    private final Map<String, UserStatus> userStatuses;
    
    public ClientRegistry() {
        this.activeClients = new ConcurrentHashMap<>();
        this.userStatuses = new ConcurrentHashMap<>();
    }
    
    public void registerClient(String username, IClientCallback callback) {
        activeClients.put(username, callback);
        userStatuses.put(username, UserStatus.ONLINE);
        notifyStatusChange(username, UserStatus.ONLINE);
    }
    
    public void unregisterClient(String username) {
        activeClients.remove(username);
        userStatuses.put(username, UserStatus.OFFLINE);
        notifyStatusChange(username, UserStatus.OFFLINE);
    }
    
    public IClientCallback getClient(String username) {
        return activeClients.get(username);
    }
    
    public boolean isOnline(String username) {
        return activeClients.containsKey(username);
    }
    
    public Set<String> getOnlineUsers() {
        return activeClients.keySet();
    }
    
    public UserStatus getUserStatus(String username) {
        return userStatuses.getOrDefault(username, UserStatus.OFFLINE);
    }
    
    private void notifyStatusChange(String username, UserStatus status) {
        for (IClientCallback client : activeClients.values()) {
            try {
                client.userStatusChanged(username, status);
            } catch (RemoteException e) {
                // Client might be disconnected, ignore
            }
        }
    }
}