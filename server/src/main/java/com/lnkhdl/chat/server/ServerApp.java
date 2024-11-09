package com.lnkhdl.chat.server;

import com.lnkhdl.chat.server.factory.DefaultServerClientHandlerFactory;
import com.lnkhdl.chat.server.factory.ServerClientHandlerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;

@Slf4j
public class ServerApp {
    private static final int MAX_CLIENTS_CONNECTED = 3;
    private static final int MAX_CLIENTS_QUEUED = 1;

    public static void main(String[] args) throws IOException {
        int port = 9999;

        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                printHelp(port);
            }
            port = Integer.parseInt(args[0]);
        }

        try (ServerSocket serverSocket = new ServerSocket(port);) {
            Server server = getServer(port, serverSocket);

            // Register a shutdown hook to stop the server gracefully, e.g. when Ctrl+C is pressed
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("Shutdown hook triggered. Stopping the server...");
                    server.stop();
                } catch (Exception e) {
                    log.error("Error during the server shutdown: {}", e.getMessage());
                }
            }));

            server.start();
        } catch (IOException e) {
            log.error("Error occurred when starting the server. Error: {}", e.getMessage());
            System.exit(1);
        }
    }

    private static Server getServer(int port, ServerSocket serverSocket) {
        ServerClientHandlerFactory clientHandlerFactory = new DefaultServerClientHandlerFactory();
        ThreadPoolExecutor clientHandlerPool = new ThreadPoolExecutor(
                MAX_CLIENTS_CONNECTED, // core pool size
                MAX_CLIENTS_CONNECTED, // maximum pool size
                0L, TimeUnit.MILLISECONDS, // keep-alive time for extra threads
                new ArrayBlockingQueue<>(MAX_CLIENTS_QUEUED), // limit queue to max additional clients
                new ThreadPoolExecutor.AbortPolicy() // reject new clients if queue is full
        );
        return new Server(port, serverSocket, clientHandlerPool, clientHandlerFactory);
    }

    private static void printHelp(int defaultPort) {
        System.err.println("Usage: [port]");
        System.err.println("  port    The port on which the server runs. Defaults to " + defaultPort + ".");
        System.exit(1);
    }
}