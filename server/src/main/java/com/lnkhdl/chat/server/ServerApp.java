package com.lnkhdl.chat.server;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerApp {
    public static void main(String[] args) {
        int port = 9999;

        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                printHelp(port);
            }
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);

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
    }

    private static void printHelp(int defaultPort) {
        System.err.println("Usage: [port]");
        System.err.println("  port    The port on which the server runs. Defaults to " + defaultPort + ".");
        System.exit(1);
    }
}