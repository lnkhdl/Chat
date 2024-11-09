package com.lnkhdl.chat.server;

import com.lnkhdl.chat.server.factory.DefaultServerClientHandlerFactory;
import com.lnkhdl.chat.server.factory.ServerClientHandlerFactory;
import com.lnkhdl.chat.server.handler.ServerClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class ServerTest {
    private ServerSocket mockServerSocket;
    private Socket mockClientSocket;
    private ServerClientHandler mockClientHandler;

    private ThreadPoolExecutor mockClientHandlerPool;
    private ServerClientHandlerFactory mockHandlerFactory;

    private Server server;

    private void startStopServer() throws InterruptedException {
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        Thread.sleep(150);

        server.stop();
        serverThread.join();
    }

    @BeforeEach
    void setUp() {
        mockServerSocket = mock(ServerSocket.class);
        mockClientSocket = mock(Socket.class);
        mockClientHandler = mock(ServerClientHandler.class);
        mockClientHandlerPool = mock(ThreadPoolExecutor.class);
        mockHandlerFactory = mock(DefaultServerClientHandlerFactory.class);
        server = new Server(123, mockServerSocket, mockClientHandlerPool, mockHandlerFactory);
    }

    @Test
    void testSocketsAcceptedSubmitted() throws InterruptedException, IOException {
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(2);
        PrintWriter mockPrintWriter = mock(PrintWriter.class);

        when(mockServerSocket.accept())
                .thenReturn(mockClientSocket)
                .thenReturn(mockClientSocket)
                .thenThrow(new IOException("No other test client socket"));

        when(mockHandlerFactory.create(any(Socket.class), anySet(), anyList()))
                .thenReturn(mockClientHandler);

        when(mockClientHandlerPool.getQueue()).thenReturn(queue);

        when(mockClientHandler.getSender()).thenReturn(mockPrintWriter);

        startStopServer();

        verify(mockHandlerFactory, times(2)).create(any(Socket.class), anySet(), anyList());
        verify(mockClientHandlerPool, times(2)).submit(mockClientHandler);
    }
}
