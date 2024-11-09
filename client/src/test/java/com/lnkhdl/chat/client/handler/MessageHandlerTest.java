package com.lnkhdl.chat.client.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageHandlerTest {
    private TestMessageHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestMessageHandler();
    }

    @Test
    void testStart() {
        handler.start();
        assertTrue(handler.isRunning());
        assertNotNull(handler.getWorker());
        assertTrue(handler.getWorker().isAlive());
    }

    @Test
    void testStop() {
        handler.start();
        handler.stop();

        // Wait to ensure the thread has time to terminate
        try {
            // The join(100) method tells the current thread (in this case, the test thread) to wait for up to 100 milliseconds for the handler.getWorker() thread to finish its execution.
            // If handler.getWorker() finishes within this 100 milliseconds, the test proceeds immediately.
            // If it does not, the test moves on after waiting for this period.
            handler.getWorker().join(100);
        } catch (InterruptedException e) {
            // The join method can throw an InterruptedException if the waiting thread (the test thread) is interrupted before the worker thread finishes or the timeout period elapses.
            Thread.currentThread().interrupt();
        }

        assertFalse(handler.isRunning());
        assertFalse(handler.getWorker().isAlive());
    }

    @Test
    void testMultipleStarts() {
        handler.start();
        Thread firstWorker = handler.getWorker();
        handler.start();

        // Check that the worker thread has not been reinitialized, see the condition in the actual code
        assertSame(firstWorker, handler.getWorker());
        assertTrue(handler.isRunning());
    }

    @Test
    void testStopWithoutStart() {
        handler.stop();
        assertFalse(handler.isRunning());
        assertNull(handler.getWorker());
    }
}
