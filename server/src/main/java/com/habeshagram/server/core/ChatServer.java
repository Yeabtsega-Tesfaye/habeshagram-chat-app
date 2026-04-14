package com.habeshagram.server.core;

import com.habeshagram.common.remote.IServer;
import com.habeshagram.server.persistence.DatabaseManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ChatServer {
    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "HabeshagramChatService";
    
    public static void main(String[] args) {
        try {
            // Initialize database
            DatabaseManager.getInstance().initializeDatabase();
            System.out.println("Database initialized successfully.");
            
            // Create server instance
            ServerImpl server = new ServerImpl();
            IServer stub = (IServer) UnicastRemoteObject.exportObject(server, 0);
            
            // Create or get registry
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(RMI_PORT);
                System.out.println("RMI Registry created on port " + RMI_PORT);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(RMI_PORT);
                System.out.println("Using existing RMI Registry on port " + RMI_PORT);
            }
            
            // Bind server to registry
            registry.rebind(SERVICE_NAME, stub);
            System.out.println("Habeshagram Chat Server is running...");
            System.out.println("Service bound to: " + SERVICE_NAME);
            
            // Add this line to keep the server running!
            System.out.println("Press Ctrl+C to stop the server.");
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}