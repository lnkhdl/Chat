package com.lnkhdl.chat.client.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SendMessageHandlerTest {
    private BufferedReader mockClientInput;
    private PrintWriter mockSender;
    private SendMessageHandler handler;

    private void startStopHandler() throws InterruptedException {
        handler.start();
        Thread.sleep(100);
        handler.stop();
    }

    @BeforeEach
    void setUp() {
        mockClientInput = mock(BufferedReader.class);
        mockSender = mock(PrintWriter.class);

        handler = new SendMessageHandler(mockClientInput, mockSender);
    }

    @Test
    void testMessageSending() throws IOException, InterruptedException {
        when(mockClientInput.readLine())
                .thenReturn("Hello World")
                .thenReturn(null); // without this null, it keeps processing the same first message, might be an issue in the if condition in the code
        startStopHandler();
        verify(mockSender).println("MESSAGE::Hello World");
        verifyNoMoreInteractions(mockSender);
    }

    @Test
    void testPrivateMessageSending() throws IOException, InterruptedException {
        when(mockClientInput.readLine())
                .thenReturn("/w john Hi, how are you?")
                .thenReturn(null);
        startStopHandler();
        verify(mockSender).println("PRIVATE_MESSAGE::/w john Hi, how are you?");
        verifyNoMoreInteractions(mockSender);
    }

    @Test
    void testEmptyInputHandling() throws IOException, InterruptedException {
        // Capture the printed content by redirecting System.err to ByteArrayOutputStream
        ByteArrayOutputStream errorMessage = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errorMessage));

        when(mockClientInput.readLine())
                .thenReturn("")
                .thenReturn(null);
        startStopHandler();
        verify(mockSender, never()).println(anyString());
        assertEquals("> The message cannot be empty." + System.lineSeparator(), errorMessage.toString());
        System.setErr(originalErr);
    }

    @Test
    void testOneSpaceInputHandling() throws IOException, InterruptedException {
        // Capture the printed content by redirecting System.err to ByteArrayOutputStream
        ByteArrayOutputStream errorMessage = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errorMessage));

        when(mockClientInput.readLine())
                .thenReturn(" ")
                .thenReturn(null);
        startStopHandler();
        verify(mockSender, never()).println(anyString());
        assertEquals("> The message cannot be empty." + System.lineSeparator(), errorMessage.toString());
        System.setErr(originalErr);
    }

    @Test
    void testIOExceptionHandling() throws IOException, InterruptedException {
        when(mockClientInput.readLine())
                .thenThrow(new IOException("Simulated IO exception"));
        handler.start();
        Thread.sleep(100);
        assertFalse(handler.isRunning());
        assertFalse(handler.getWorker().isAlive());
    }
}