package com.habeshagram.common.remote;

import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.UserStatus;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientCallback extends Remote {
    void receiveMessage(Message msg) throws RemoteException;
    
    void userStatusChanged(String username, UserStatus status) throws RemoteException;
    
    void receiveGroupUpdate(String groupName, String message) throws RemoteException;
}