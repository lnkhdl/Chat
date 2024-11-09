package com.lnkhdl.chat.server.factory;

import com.lnkhdl.chat.server.handler.ServerClientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Set;

public class DefaultServerClientHandlerFactory implements ServerClientHandlerFactory {

    @Override
    public ServerClientHandler create(Socket clientSocket, Set<String> clientNames, List<ServerClientHandler> clientHandlers) throws IOException {
        PrintWriter sender = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        return new ServerClientHandler(clientSocket, clientNames, clientHandlers, sender, receiver);
    }
}