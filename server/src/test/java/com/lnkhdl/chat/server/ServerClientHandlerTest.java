package com.lnkhdl.chat.server;

import com.lnkhdl.chat.server.handler.ServerClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerClientHandlerTest {
    private Socket mockClientSocket;
    private Set<String> clientNames;
    private List<ServerClientHandler> clientHandlers;

    private PrintWriter mockSender;
    private BufferedReader mockReceiver;

    private ServerClientHandler handler;

    private void startStopHandler() throws InterruptedException {
        Thread thread = new Thread(handler);
        thread.start();
        thread.join();
    }

    @BeforeEach
    void setUp() throws IOException {
        mockClientSocket = mock(Socket.class);
        clientNames = new HashSet<>();
        clientNames.add("testuser");
        clientHandlers = new ArrayList<>();
        mockSender = mock(PrintWriter.class);
        mockReceiver = mock(BufferedReader.class);

        handler = new ServerClientHandler(mockClientSocket, clientNames, clientHandlers, mockSender, mockReceiver);
    }

    @Test
    public void testInitMessageHandling() throws InterruptedException, IOException {
        when(mockReceiver.readLine())
                .thenReturn("INIT::This is init message")
                .thenReturn(null);

        startStopHandler();

        verify(mockSender).println("SET_USERNAME::Please set your username.");
    }

    @Test
    public void testWrongUsernameEmptyHandling() throws InterruptedException, IOException {
        when(mockReceiver.readLine())
                .thenReturn("SET_USERNAME::")
                .thenReturn(null);

        startStopHandler();

        verify(mockSender).println("WRONG_USERNAME::Username cannot be empty. Please try another one.");
    }

    @Test
    public void testWrongUsernameShortHandling() throws InterruptedException, IOException {
        when(mockReceiver.readLine())
                .thenReturn("SET_USERNAME::a")
                .thenReturn(null);

        startStopHandler();

        verify(mockSender).println("WRONG_USERNAME::Username must be between 3 and 30 characters.");
    }

    @Test
    public void testWrongUsernameLongHandling() throws InterruptedException, IOException {
        when(mockReceiver.readLine())
                .thenReturn("SET_USERNAME::abcdefghijkl12345mnopqrstuvwxyz")
                .thenReturn(null);

        startStopHandler();

        verify(mockSender).println("WRONG_USERNAME::Username must be between 3 and 30 characters.");
    }

    @Test
    public void testWrongUsernameSpecialHandling() throws InterruptedException, IOException {
        when(mockReceiver.readLine())
                .thenReturn("SET_USERNAME::ab=c")
                .thenReturn(null);

        startStopHandler();

        verify(mockSender).println("WRONG_USERNAME::Username can only contain letters, numbers, and underscores.");
    }

    @Test
    public void testWrongUsernameDuplicateHandling() throws InterruptedException, IOException {
        when(mockReceiver.readLine())
                .thenReturn("SET_USERNAME::testuser")
                .thenReturn(null);

        startStopHandler();

        verify(mockSender).println("WRONG_USERNAME::The username is already used. Please try another one.");
    }

    @Test
    public void testCorrectUsernameHandling() throws InterruptedException, IOException {
        when(mockReceiver.readLine())
                .thenReturn("SET_USERNAME::John")
                .thenReturn(null);

        startStopHandler();

        verify(mockSender).println("CONFIRM_USERNAME::john");
    }

    @Test
    public void testPrivateMessageWrongUsernameHandling() throws InterruptedException, IOException {
        when(mockReceiver.readLine())
                .thenReturn("PRIVATE_MESSAGE::/w testuser2 Hi!")
                .thenReturn(null);

        startStopHandler();

        verify(mockSender).println("WRONG_PRIVATE_MESSAGE_USERNAME::testuser2 is not connected.");
    }

    @Test
    public void testPrivateMessageWrongFormatHandling() throws InterruptedException, IOException {
        when(mockReceiver.readLine())
                .thenReturn("PRIVATE_MESSAGE::/wtestuser Hi!")
                .thenReturn(null);

        startStopHandler();

        verify(mockSender).println("WRONG_MESSAGE_FORMAT::The private message format is wrong. To send a private message, use the following format: /w username message.");
    }

    @Test
    public void testInvalidMessageHandling() throws InterruptedException, IOException {
        // Here we use spy because then in the verify, a mock has to be used instead of a real instance
        ServerClientHandler spyHandler = spy(handler);
        when(mockReceiver.readLine()).thenThrow(new IOException("Test error"));
        Thread thread = new Thread(spyHandler);
        thread.start();
        Thread.sleep(100);

        verify(mockSender, never()).println(anyString());
        verify(spyHandler).stop();
    }

}
