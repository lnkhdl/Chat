package com.lnkhdl.chat.client.handler;

import lombok.Getter;

@Getter
public abstract class MessageHandler implements Runnable {
    private Thread worker;
    // Marking "running" as volatile ensures that when stop() sets it to false, this change is immediately visible to the thread executing run().
    private volatile boolean running;

    public void start() {
        if (worker == null) {
            worker = new Thread(this);
            worker.start();
            running = true;
        }
    }

    public void stop() {
        if (worker != null && worker.isAlive()) {
            worker.interrupt();
            running = false;
        }
    }
}