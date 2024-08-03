package com.lnkhdl.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class Client {
    public static void main(String[] args) throws IOException {
        log.info("Client started!");

        Socket socket = new Socket("localhost", 9999);
    }
}