package com.lnkhdl.chat.server;

import com.lnkhdl.chat.common.message.Type;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class Server {
    private final int port;

    // "list" is passed by reference, so it is updated for each existing ServerClientHandler when a new instance is created
    private final List<ServerClientHandler> clients = new CopyOnWriteArrayList<>();
    Set<String> sharedClientNames = Collections.synchronizedSet(new HashSet<>());

    private ServerSocket serverSocket;

    // Marking "running" as volatile ensures that when stop() sets it to false, this change is immediately visible to the thread executing run().
    private volatile boolean running;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            log.info("Server started on port: {}! Waiting for clients to connect...", port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ServerClientHandler client = new ServerClientHandler(clientSocket, sharedClientNames, clients);
                    log.info("Client connected: {}", clientSocket.getRemoteSocketAddress());
                    clients.add(client);
                    new Thread(client).start();
                } catch (IOException e) {
                    if (!running) break; // Stop loop if server is shutting down
                    log.error("Error accepting client connection: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when waiting for the clients to connect: IO error when opening the socket. Error: {}", e.getMessage());
            System.exit(1);
        } finally {
            stop();
        }
    }

    public void stop() {
        if (!running) {
            log.debug("Server stop was already executed by another thread.");
            return;
        }

        running = false;

        for (ServerClientHandler client : clients) {
            client.sendMessageToEveryone("Server is shutting down. You will be disconnected.", Type.SERVER_CLOSED, true, false);
        }

        for (ServerClientHandler client : clients) {
            client.stop();
        }

        clients.clear();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                log.debug("Server socket closed.");
            }
        } catch (IOException e) {
            log.error("Error while closing server socket: {}", e.getMessage());
        }

        log.info("Server stopped successfully.");
    }
}