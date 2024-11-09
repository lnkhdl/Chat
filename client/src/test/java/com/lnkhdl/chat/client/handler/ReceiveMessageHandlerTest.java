package com.lnkhdl.chat.client.handler;

import com.lnkhdl.chat.client.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceiveMessageHandlerTest {
    private Client mockClient;
    private BufferedReader mockReceiver;
    private PrintWriter mockSender;
    private BufferedReader mockClientInput;
    private ReceiveMessageHandler handler;

    private void startStopHandler() throws InterruptedException {
        handler.start();
        Thread.sleep(100);
        handler.stop();
    }

    @BeforeEach
    void setUp() {
        mockClient = mock(Client.class);
        mockReceiver = mock(BufferedReader.class);
        mockSender = mock(PrintWriter.class);
        mockClientInput = mock(BufferedReader.class);

        handler = new ReceiveMessageHandler(mockClient, mockReceiver, mockSender, mockClientInput);
    }

    @Test
    void testHandleSetUsername() throws Exception {
        when(mockReceiver.readLine())
                .thenReturn("SET_USERNAME::Please set your username")
                .thenReturn(null);
        when(mockClientInput.readLine())
                .thenReturn("John")
                .thenReturn(null);

        startStopHandler();
        verify(mockClientInput).readLine();
        verify(mockSender).println("SET_USERNAME::john");
    }

    @Test
    void testHandleConfirmUsername() throws Exception {
        when(mockClient.getSendMessageHandler()).thenReturn(mock(SendMessageHandler.class));
        when(mockReceiver.readLine())
                .thenReturn("CONFIRM_USERNAME::testName")
                .thenReturn(null);

        startStopHandler();
        verify(mockClient).setClientUsername("testName"); // verify() ensures setClientUsername("testName") was called
    }

    @Test
    void testWrongUsernameHandling() throws IOException, InterruptedException {
        ByteArrayOutputStream outMessage = new ByteArrayOutputStream();
        PrintStream originalMsg = System.out;
        System.setOut(new PrintStream(outMessage));

        when(mockReceiver.readLine())
                .thenReturn("WRONG_USERNAME::The username is wrong.")
                .thenReturn(null);
        when(mockClientInput.readLine())
                .thenReturn("Jane")
                .thenReturn(null);

        startStopHandler();

        assertEquals("> The username is wrong." + System.lineSeparator(), outMessage.toString());
        System.setOut(originalMsg);

        verify(mockClientInput).readLine();
        verify(mockSender).println("SET_USERNAME::jane");
    }

    @Test
    void testMessageHandling() throws IOException, InterruptedException {
        // Capture the printed content by redirecting System.out to ByteArrayOutputStream
        ByteArrayOutputStream outMessage = new ByteArrayOutputStream();
        PrintStream originalMsg = System.out;
        System.setOut(new PrintStream(outMessage));

        when(mockReceiver.readLine())
                .thenReturn("MESSAGE::This is a test message.")
                .thenReturn(null);
        startStopHandler();
        verify(mockSender, never()).println(anyString());
        assertEquals("> This is a test message." + System.lineSeparator(), outMessage.toString());
        System.setOut(originalMsg);
    }

    @Test
    void testPrivateMessageHandling() throws IOException, InterruptedException {
        ByteArrayOutputStream outMessage = new ByteArrayOutputStream();
        PrintStream originalMsg = System.out;
        System.setOut(new PrintStream(outMessage));

        when(mockReceiver.readLine())
                .thenReturn("PRIVATE_MESSAGE::This is a test private message.")
                .thenReturn(null);
        startStopHandler();
        verify(mockSender, never()).println(anyString());
        assertEquals("> This is a test private message." + System.lineSeparator(), outMessage.toString());
        System.setOut(originalMsg);
    }

    @Test
    void testWrongPrivateMessageUsernameHandling() throws IOException, InterruptedException {
        ByteArrayOutputStream outMessage = new ByteArrayOutputStream();
        PrintStream originalMsg = System.out;
        System.setOut(new PrintStream(outMessage));

        when(mockReceiver.readLine())
                .thenReturn("WRONG_PRIVATE_MESSAGE_USERNAME::This username is wrong.")
                .thenReturn(null);
        startStopHandler();
        verify(mockSender, never()).println(anyString());
        assertEquals("> This username is wrong." + System.lineSeparator(), outMessage.toString());
        System.setOut(originalMsg);
    }

    @Test
    void testWrongMessageFormatHandling() throws IOException, InterruptedException {
        ByteArrayOutputStream outMessage = new ByteArrayOutputStream();
        PrintStream originalMsg = System.out;
        System.setOut(new PrintStream(outMessage));

        when(mockReceiver.readLine())
                .thenReturn("WRONG_MESSAGE_FORMAT::This message format is wrong.")
                .thenReturn(null);
        startStopHandler();
        verify(mockSender, never()).println(anyString());
        assertEquals("> This message format is wrong." + System.lineSeparator(), outMessage.toString());
        System.setOut(originalMsg);
    }

    @Test
    void testInvalidMessageHandling() throws Exception {
        when(mockReceiver.readLine()).thenThrow(new IOException("Invalid message"));
        handler.start();
        Thread.sleep(50);
        verify(mockSender, never()).println(anyString());
        assertFalse(handler.isRunning());
        assertFalse(handler.getWorker().isAlive());
    }
}
