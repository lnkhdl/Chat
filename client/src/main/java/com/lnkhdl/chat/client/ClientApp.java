package com.lnkhdl.chat.client;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ClientApp {
    public static void main(String[] args) throws IOException, InterruptedException {
        String serverAddress = "localhost";
        int serverPort = 9999;

        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                printHelp(serverAddress, serverPort);
            }
            serverAddress = args[0];
        }

        if (args.length > 1) {
            serverPort = Integer.parseInt(args[1]);
        }

        Client client = new Client(serverAddress, serverPort);

        // Register a shutdown hook to stop the client gracefully, e.g. when Ctrl+C is pressed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("> Stopping the application.");
                log.debug("Shutdown hook triggered. Stopping the client...");
                client.stop();
            } catch (Exception e) {
                log.error("Error during the client shutdown: {}", e.getMessage());
            }
        }));

        client.start();
    }

    private static void printHelp(String defaultAddress, int defaultPort) {
        System.err.println("Usage: [serverAddress] [serverPort]");
        System.err.println("  serverAddress    The address on which the server runs. Defaults to " + defaultAddress + ".");
        System.err.println("  serverPort    The port on which the server runs. Defaults to " + defaultPort + ".");
        System.exit(1);
    }
}