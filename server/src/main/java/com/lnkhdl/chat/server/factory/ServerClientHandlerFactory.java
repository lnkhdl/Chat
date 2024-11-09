package com.lnkhdl.chat.server.factory;

import com.lnkhdl.chat.server.handler.ServerClientHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;

public interface ServerClientHandlerFactory {
    ServerClientHandler create(Socket clientSocket, Set<String> clientNames, List<ServerClientHandler> clientHandlers) throws IOException;
}