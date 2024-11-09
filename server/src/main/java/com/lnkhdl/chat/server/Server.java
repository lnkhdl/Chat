package com.lnkhdl.chat.server;

import com.lnkhdl.chat.common.message.Protocol;
import com.lnkhdl.chat.common.message.Type;
import com.lnkhdl.chat.server.factory.ServerClientHandlerFactory;
import com.lnkhdl.chat.server.handler.ServerClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class Server {
    private final int port;
    private final ServerSocket serverSocket;
    private final ThreadPoolExecutor clientHandlerPool;
    private final ServerClientHandlerFactory handlerFactory;

    private final List<ServerClientHandler> clientHandlers;
    private final Set<String> clientNames;

    // Marking "running" as volatile ensures that when stop() sets it to false, this change is immediately visible to the thread executing run().
    private volatile boolean running;

    public Server(int port, ServerSocket serverSocket, ThreadPoolExecutor clientHandlerPool, ServerClientHandlerFactory handlerFactory) {
        this.port = port;
        this.serverSocket = serverSocket;
        this.clientHandlerPool = clientHandlerPool;
        this.handlerFactory = handlerFactory;
        this.clientHandlers = new CopyOnWriteArrayList<>();
        this.clientNames = Collections.synchronizedSet(new HashSet<>());
    }

    public void start() {
        running = true;
        try {
            log.info("Server started on port: {}! Waiting for clients to connect...", port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    //ServerClientHandler clientHandler = new ServerClientHandler(clientSocket, clientNames, clientHandlers);
                    ServerClientHandler clientHandler = handlerFactory.create(clientSocket, clientNames, clientHandlers);
                    log.info("Client connected: {}", clientSocket.getRemoteSocketAddress());

                    try {
                        if ((clientHandlerPool.getActiveCount() >= clientHandlerPool.getMaximumPoolSize()) && clientHandlerPool.getQueue().remainingCapacity() > 0) {
                            log.info("Server Executor Pool full. Notifying the new client.");
                            clientHandler.getSender().println(Protocol.formatMessage(Type.IN_SERVER_QUEUE, "We are sorry, the server you are trying to connect reaches its maximum capacity. You are waiting in a queue..."));
                        }
                        clientHandlerPool.submit(clientHandler);
                    } catch (RejectedExecutionException e) {
                        /* TODO: Enhance the handling of client connections by implementing a custom queue for the ThreadPoolExecutor.
                            The custom queue should periodically or dynamically validate the connection status of clients waiting in the queue.
                            If a client disconnects while waiting, it should be removed from the queue to ensure it is not incorrectly counted towards the queue's capacity.
                            This will prevent scenarios where disconnected clients block space, potentially leading to unnecessary rejection of new connections. */
                        log.error("Server is at maximum capacity. Rejecting the new client.");
                        clientHandler.getSender().println(Protocol.formatMessage(Type.SERVER_MAX_CAPACITY, "We are sorry, the server you are trying to connect reaches its maximum capacity. Please try it later."));

                        // Small delay to allow message to be sent, a better solution to be found
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }

                        clientHandler.stop();
                    }

                    // A "list" is a reference data type, so reference to the same object is used by all clients
                    clientHandlers.add(clientHandler);
                } catch (IOException e) {
                    if (!running) break; // Stop loop if server is shutting down
                    log.error("Error accepting client connection: {}", e.getMessage());
                }
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        if (!running) {
            log.debug("Server stop was already executed by another thread.");
            return;
        }

        running = false;

        for (ServerClientHandler clientHandler : clientHandlers) {
            clientHandler.getSender().println(Protocol.formatMessage(Type.SERVER_CLOSED, "Server is shutting down. You will be disconnected."));
        }

        for (ServerClientHandler clientHandler : clientHandlers) {
            clientHandler.stop();
        }

        clientHandlers.clear();

        shutdownAndAwaitTermination(clientHandlerPool);

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                log.debug("Server socket closed.");
            }
        } catch (IOException e) {
            log.error("Error while closing server socket: {}", e.getMessage());
        }

        log.info("Server stopped successfully.");
    }

    // https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    log.error("Pool did not terminate.");
            }
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            log.error("Pool shutdown with an exception: {}", e.getMessage());
            // Preserve interrupt status
            //Thread.currentThread().interrupt();
        }
    }
}