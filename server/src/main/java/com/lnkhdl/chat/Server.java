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
                try (
                        Socket clientSocket = serverSocket.accept();
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ) {
                    log.info("New client connected from port {}.", clientSocket.getPort());

                    if (in.readLine().startsWith("Hi")) {
                        out.println("Hi Client, you are connected.");
                        log.info("Greeted the new client.");
                    }

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        log.info("Received from Client: {}", inputLine);
                        out.println(inputLine);
                        log.info("Server sent response.");
                    }
                } catch (IOException e) {
                    log.error("Error occurred when communicating with the client: {}", e.getMessage());
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when waiting for the clients to connect: IO error when opening the socket. Error: {}",
                    e.getMessage());
            System.exit(1);
        }
    }
}