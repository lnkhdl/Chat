package com.lnkhdl.chat.client.handler;

import com.lnkhdl.chat.common.message.Protocol;
import com.lnkhdl.chat.common.message.Type;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class SendMessageHandler extends MessageHandler implements Runnable {
    private final BufferedReader clientInput;
    private final PrintWriter sender;

    public SendMessageHandler(BufferedReader clientInput, PrintWriter sender) {
        this.clientInput = clientInput;
        this.sender = sender;
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                String userInput;
                if ((userInput = clientInput.readLine()) != null) {
                    if (userInput.trim().isEmpty()) {
                        log.warn("Empty input received, skipping...");
                        System.err.println("> The message cannot be empty.");
                    } else if (userInput.equalsIgnoreCase("/exit")) {
                        log.debug("User requested to exit. Stopping client...");
                        // The shutdown hook will be triggered by this exit
                        System.exit(0);
                        break;
                    } else if (userInput.startsWith(Protocol.PRIVATE_MESSAGE_MARK)) {
                        log.debug("Sending message to Server - private: {}", userInput);
                        sender.println(Protocol.formatMessage(Type.PRIVATE_MESSAGE, userInput));
                    } else {
                        log.debug("Sending message to Server: {}", userInput);
                        sender.println(Protocol.formatMessage(Type.MESSAGE, userInput));
                    }
                }
                if (Thread.currentThread().isInterrupted()) {
                    // Exit if interrupted
                    break;
                }
            } catch (IOException e) {
                log.error("Error when sending a message to Server: {}", e.getMessage(), e);
                stop();
            }
        }
    }
}