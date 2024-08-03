package com.lnkhdl.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;

@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        log.info("Server started! Waiting for clients to connect...");
        serverSocket.accept();
    }
}