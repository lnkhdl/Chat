package com.lnkhdl.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Server started on port: {}! Waiting for clients to connect...", port);
            while(true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    log.info("New client connected from port {}.", clientSocket.getPort());

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String message = in.readLine();
                    log.info("Received: {}", message);
                    out.println("Message received: " + message);
                } catch (IOException e) {
                    log.warn("Error occurred when a client connected: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            log.warn("Error occurred when waiting for the clients to connect: {}", e.getMessage());
        }
    }
}