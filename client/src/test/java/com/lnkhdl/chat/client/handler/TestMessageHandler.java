package com.lnkhdl.chat.client.handler;

/*  Since MessageHandler is abstract, we need to create a simple subclass that implements run().
    For testing purposes, it just loops while running is true, which will allow us to observe the thread's behavior.
 */
public class TestMessageHandler extends MessageHandler {
    @Override
    public void run() {
        while (isRunning()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
