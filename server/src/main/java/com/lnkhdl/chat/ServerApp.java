package com.lnkhdl.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ServerApp {
    public static void main(String[] args) throws IOException {
        int port = 9999;

        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                printHelp(port);
            }
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);
        server.start();
    }

    private static void printHelp(int defaultPort) {
        System.err.println("Usage: [port]");
        System.err.println("  port    The port on which the server runs. Defaults to " + defaultPort);
        System.exit(1);
    }
}
