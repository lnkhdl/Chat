package com.lnkhdl.chat.server;

import com.lnkhdl.chat.common.exception.InvalidProtocolException;
import com.lnkhdl.chat.common.message.Protocol;
import com.lnkhdl.chat.common.message.Type;

import com.lnkhdl.chat.common.name.NameValidationResult;
import com.lnkhdl.chat.common.name.NameValidator;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Set;

@Slf4j
public class ServerClientHandler implements Runnable {

    private final Socket clientSocket;
    private final List<ServerClientHandler> clients;
    private final Set<String> clientNames;
    private String clientName;

    private final PrintWriter sender;
    private final BufferedReader receiver;

    // Marking "running" as volatile ensures that when stop() sets it to false, this change is immediately visible to the thread executing run().
    private volatile boolean running;

    // Once username is successfully set, a client is marked as active, and it can receive messages.
    private boolean isActive;

    public ServerClientHandler(Socket clientSocket, Set<String> sharedClientNames, List<ServerClientHandler> clients) throws IOException {
        this.clientSocket = clientSocket;
        this.clientNames = sharedClientNames;
        this.clients = clients;
        this.sender = new PrintWriter(this.clientSocket.getOutputStream(), true);
        this.receiver = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        this.running = true;
        this.isActive = false;
    }

    @Override
    public void run() {
        log.info("New client connected {}.", clientSocket.getRemoteSocketAddress());

        try {
            String messageFromClient;
            while (running && (messageFromClient = receiver.readLine()) != null) {
                log.info("Received from Client{}: {}", clientName != null ? " " + clientName : "", messageFromClient);

                Type receivedType = Protocol.parseMessageType(messageFromClient);
                String receivedText = Protocol.parseMessageText(messageFromClient);

                switch (receivedType) {
                    case INIT:
                        log.debug("INIT entered");
                        sender.println(Protocol.formatMessage(Type.SET_USERNAME, "Please set your username."));
                        break;

                    case SET_USERNAME:
                        log.debug("SET_USERNAME entered");

                        String sanitizedName = NameValidator.sanitize(receivedText);
                        NameValidationResult validationResult = NameValidator.validate(sanitizedName);

                        if (!validationResult.isValid()) {
                            log.debug("Issue when validating the username: {}. {}", sanitizedName, validationResult.getErrorMessage());
                            sender.println(Protocol.formatMessage(Type.WRONG_USERNAME, validationResult.getErrorMessage()));
                        } else if (doesUsernameExist(sanitizedName)) {
                            log.debug("The username is not unique: {}", sanitizedName);
                            sender.println(Protocol.formatMessage(Type.WRONG_USERNAME, "The username is already used. Please try another one."));
                        } else if (!saveUsername(sanitizedName)) {
                            log.debug("Issue when saving the username: {}", sanitizedName);
                            sender.println(Protocol.formatMessage(Type.WRONG_USERNAME, "The username could not be saved. Please try another one."));
                        } else {
                            isActive = true;
                            sender.println(Protocol.formatMessage(Type.CONFIRM_USERNAME, sanitizedName));
                            sender.println(Protocol.formatMessage(Type.MESSAGE, getMessageTextWithClientNames()));
                            sender.println(Protocol.formatMessage(Type.MESSAGE, "All messages are sent publicly unless you specify that a message is private."));
                            sender.println(Protocol.formatMessage(Type.MESSAGE, "To send a private message, use the following format: /w username message."));
                            sender.println(Protocol.formatMessage(Type.MESSAGE, "Type /exit to close the application."));
                            log.info("The client has been added to the list. The list of clients: {}", clientNames);
                            sendMessageToEveryone("New user connected: " + sanitizedName, Type.MESSAGE, false, false);
                        }
                        break;

                    case MESSAGE:
                        log.debug("MESSAGE entered");
                        sendMessageToEveryone(receivedText, Type.MESSAGE, true, true);
                        break;

                    case PRIVATE_MESSAGE:
                        // expected format: /w username private
                        log.debug("PRIVATE_MESSAGE entered");

                        try {
                            String[] privateMessageParts = Protocol.parsePrivateMessageText(receivedText);

                            if (doesUsernameExist(privateMessageParts[1])) {
                                sendPrivateMessage(
                                        privateMessageParts[1],
                                        privateMessageParts[2]);
                            } else {
                                sender.println(Protocol.formatMessage(Type.WRONG_PRIVATE_MESSAGE_USERNAME, privateMessageParts[1] + " is not connected."));
                                sender.println(Protocol.formatMessage(Type.MESSAGE, getMessageTextWithClientNames()));
                            }
                        } catch (InvalidProtocolException e) {
                            log.error("Invalid private message format received: {}", e.getMessage());
                            sender.println(Protocol.formatMessage(Type.WRONG_MESSAGE_FORMAT, "The private message format is wrong. To send a private message, use the following format: /w username message."));
                        }
                        break;

                    default:
                        log.error("Invalid message type: {}", receivedType);
                }
            }
        } catch (InvalidProtocolException e) {
            log.error("Invalid message received: {}", e.getMessage());
            sender.println(Protocol.formatMessage(Type.WRONG_MESSAGE_FORMAT, "The message format is wrong."));
        } catch (IOException e) {
            log.error("Error occurred when communicating with the client: {}", e.getMessage());
            stop();
        }
    }

    // "synchronized" is used so stop() is invoked only once for each instance
    public synchronized void stop() {
        if (!running) {
            log.debug("Client disconnection was already executed by another thread.");
            return;
        }

        running = false;

        try {
            if (sender != null) {
                sender.close();
            }
            if (receiver != null) {
                receiver.close();
            }
        } catch (IOException e) {
            log.error("Error closing resources: {}", e.getMessage());
        }

        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                log.info("Client connection closed: {}", clientSocket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            log.error("Error closing client socket: {}", e.getMessage());
        }

        if (clientName != null && clientNames.contains(clientName)) {
            clientNames.remove(clientName);
            log.info("Client '{}' has been removed from the list. Updated list: {}", clientName, clientNames);
        }

        // This uses threads of other ServerClientHandler
        sendMessageToEveryone(clientName + " has left the chat.", Type.MESSAGE, false, false);

        clients.remove(this);

        log.info("ServerClientHandler stopped for client: {}", clientName);
    }

    protected void sendMessageToEveryone(String messageText, Type messageType, boolean sendToCurrentClient, boolean includeUsername) {
        String message = includeUsername ? clientName + " " + messageText : messageText;
        String formattedMessage = Protocol.formatMessage(messageType, message);
        for (ServerClientHandler client : clients) {
            if (!sendToCurrentClient && client == this) {
                continue;
            }
            if (client.isActive) {
                client.sender.println(formattedMessage);
            }
        }
    }

    private void sendPrivateMessage(String targetClientName, String messageText) {
        for (ServerClientHandler client : clients) {
            if (targetClientName.equals(client.clientName) && client.isActive) {
                client.sender.println(Protocol.formatMessage(Type.PRIVATE_MESSAGE, "[private] " + clientName + ": " + messageText));
                break;
            }
        }
    }

    private boolean doesUsernameExist(String username) {
        return clientNames.contains(username);
    }

    private boolean saveUsername(String username) {
        if (clientNames.add(username)) {
            clientName = username;
            return true;
        }
        return false;
    }

    private String getMessageTextWithClientNames() {
        return "Connected users: " + String.join(", ", clientNames);
    }
}