package com.lnkhdl.chat.client.handler;

import com.lnkhdl.chat.client.Client;
import com.lnkhdl.chat.common.exception.InvalidProtocolException;
import com.lnkhdl.chat.common.message.Protocol;
import com.lnkhdl.chat.common.message.Type;
import com.lnkhdl.chat.common.name.NameValidationResult;
import com.lnkhdl.chat.common.name.NameValidator;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class ReceiveMessageHandler extends MessageHandler implements Runnable {
    private final Client client;
    private final BufferedReader receiver;
    private final PrintWriter sender;
    private final BufferedReader clientInput;

    public ReceiveMessageHandler(Client client, BufferedReader receiver, PrintWriter sender, BufferedReader clientInput) {
        this.client = client;
        this.receiver = receiver;
        this.sender = sender;
        this.clientInput = clientInput;
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                String messageFromServer;

                if ((messageFromServer = receiver.readLine()) != null) {
                    log.debug("Received a message from the server: {}", messageFromServer);

                    Type receivedType = Protocol.parseMessageType(messageFromServer);
                    String receivedText = Protocol.parseMessageText(messageFromServer);

                    switch (receivedType) {
                        case SET_USERNAME:
                            log.debug("SET_USERNAME entered");
                            System.out.println("> Please enter your username.");
                            processInputUsername(sender, clientInput);
                            break;

                        case CONFIRM_USERNAME:
                            log.debug("CONFIRM_USERNAME entered");
                            client.setClientUsername(receivedText);
                            System.out.println("> Your username is set to: " + client.getClientUsername());

                            // Start the sending thread only after username is confirmed
                            client.setSendMessageHandler(new SendMessageHandler(clientInput, sender));
                            client.getSendMessageHandler().start();
                            break;

                        case WRONG_USERNAME:
                            log.debug("WRONG_USERNAME entered");
                            System.out.println("> " + receivedText);
                            processInputUsername(sender, clientInput);
                            break;

                        case MESSAGE:
                            log.debug("MESSAGE entered");
                            System.out.println("> " + receivedText);
                            break;

                        case PRIVATE_MESSAGE:
                            log.debug("PRIVATE_MESSAGE entered");
                            System.out.println("> " + receivedText);
                            break;

                        case WRONG_PRIVATE_MESSAGE_USERNAME:
                            log.debug("WRONG_PRIVATE_MESSAGE_USERNAME entered");
                            System.out.println("> " + receivedText);
                            break;

                        case WRONG_MESSAGE_FORMAT:
                            log.debug("WRONG_MESSAGE_FORMAT entered");
                            System.out.println("> " + receivedText);
                            break;

                        case SERVER_CLOSED:
                            log.debug("SERVER_CLOSED entered");
                            System.out.println("> " + receivedText);
                            System.exit(2);
                            break;

                        default:
                            log.error("Invalid message type: {}", receivedType);
                    }
                }
                if (Thread.currentThread().isInterrupted()) {
                    // Exit if interrupted
                    break;
                }
            } catch (InvalidProtocolException e) {
                log.error("Invalid message received: {}", e.getMessage());
            } catch (IOException e) {
                log.error("Error reading from Server: {}", e.getMessage());
                stop();
            }
        }
    }

    private void processInputUsername(PrintWriter sender, BufferedReader clientInput) throws IOException {
        while (isRunning()) {
            try {
                String usernameInput = clientInput.readLine();
                String sanitizedName = NameValidator.sanitize(usernameInput);
                NameValidationResult result = NameValidator.validate(sanitizedName);

                if (!result.isValid()) {
                    System.err.println("> Error: " + result.getErrorMessage() + " Please try another one.");
                    log.error("Error when setting up the username: {}.", result.getErrorMessage());
                } else {
                    sendUsername(sender, sanitizedName);
                    break;
                }
            } catch (Exception e) {
                log.error("Error when processing the username input: {}", e.getMessage());
            }
        }
    }

    private void sendUsername(PrintWriter sender, String username) {
        sender.println(Protocol.formatMessage(Type.SET_USERNAME, username));
    }
}