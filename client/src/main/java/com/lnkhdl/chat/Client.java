package com.lnkhdl.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class Client {
    private final String serverAddress;
    private final int serverPort;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void start() {
        try (
                Socket clientSocket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            log.info("Client started.");

            out.println("Hi Server, this is a new client!");
            log.info("Connected to the server and greeted it.");
        } catch (IOException e) {
            log.warn("Error occurred when connecting the server: {}", e.getMessage());
        }

        log.info("Client ended.");
    }
}