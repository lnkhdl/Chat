package com.lnkhdl.chat.client;

import com.lnkhdl.chat.client.handler.ReceiveMessageHandler;
import com.lnkhdl.chat.client.handler.SendMessageHandler;
import com.lnkhdl.chat.common.message.Protocol;
import com.lnkhdl.chat.common.message.Type;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

@Slf4j
@Getter
@Setter
public class Client {
    private final String serverAddress;
    private final int serverPort;
    private String clientUsername;

    private ReceiveMessageHandler receiveMessageHandler;
    private SendMessageHandler sendMessageHandler;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void start() throws InterruptedException {
        try (
                Socket clientSocket = new Socket(serverAddress, serverPort);
                PrintWriter sender = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            log.info("The client has started.");
            sender.println(Protocol.formatMessage(Type.INIT, "A new client has connected."));

            receiveMessageHandler = new ReceiveMessageHandler(this, receiver, sender, clientInput);
            receiveMessageHandler.start();
            receiveMessageHandler.getWorker().join();
        } catch (UnknownHostException e) {
            log.error("Couldn't connect to {}:{}. Unknown host: {}", serverAddress, serverPort, e.getMessage());
            System.err.println("We are sorry. The server you are trying to connect to is currently unavailable.");
        } catch (IOException e) {
            log.error("Error occurred when communicating with the server {}:{}. Error: {}", serverAddress, serverPort, e.getMessage());
            System.err.println("We are sorry. The server you are trying to connect to is currently unavailable.");
        }
    }

    public void stop() {
        if (receiveMessageHandler != null) {
            receiveMessageHandler.stop();
        }
        log.info("The reading thread has been stopped.");

        if (sendMessageHandler != null) {
            sendMessageHandler.stop();
        }
        log.info("The sending thread has been stopped.");

        System.out.println("> Application has been stopped.");
        log.info("The client has been stopped.");
    }
}