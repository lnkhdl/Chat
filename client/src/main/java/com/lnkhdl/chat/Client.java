package com.lnkhdl.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Slf4j
public class Client {
    private static final int TIMEOUT_MS = 10000;
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
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            clientSocket.setSoTimeout(TIMEOUT_MS);

            log.info("Client started.");
            out.println("Hi Server, this is a new client!");
            log.info("Connected to the server and greeted it.");

            try {
                String response = in.readLine();
                if (response != null) {
                    log.info("Received from the server: {}", response);
                } else {
                    log.error("No response received from the server.");
                }
            } catch (SocketTimeoutException e) {
                log.error("Server {}:{} did not respond within the timeout period. Error: {}",
                        serverAddress, serverPort, e.getMessage());
                System.exit(1);
            }

            String input;
            while ((input = stdIn.readLine()) != null) {
                out.println(input);
                log.info("Received from Server: {}", in.readLine());
            }
        } catch (UnknownHostException e) {
            log.error("Couldn't connect to {}:{}. Unknown host: {}",
                    serverAddress, serverPort, e.getMessage());
        } catch (IOException e) {
            log.error("Error occurred when communicating with the server {}:{}. Error: {}",
                    serverAddress, serverPort, e.getMessage());
        }
    }
}