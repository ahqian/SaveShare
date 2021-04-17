package com.alexhqi.event;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventProcessor {

    private static final EventProcessor instance = new EventProcessor();
    public static EventProcessor getInstance() {
        return instance;
    }

    private volatile Thread dispatcherThread = null;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private final Semaphore exitLock = new Semaphore(0);

    // can eventually have multiple buses of varying priorities if necessary
    private EventBus bus;

    private EventProcessor() {
    }

    public EventBus getBus() {
        return bus;
    }

    // eventually can add support for multiple buses with varying priorities
    public void registerEventBus(EventBus bus) {
        this.bus = bus;
    }

    public synchronized void startProcessing() {
        stop.set(false);
        if (dispatcherThread != null && dispatcherThread.isAlive()) {
            throw new IllegalStateException("Attempting to start Dispatcher Thread while it is still alive.");
        } else  {
            dispatcherThread = new Thread(new HandlerDispatcher());
            dispatcherThread.setName("Dispatcher Thread");
        }
        dispatcherThread.start();
    }

    public void finishProcessing() {
        stop.set(true);
        try {
            exitLock.tryAcquire(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}
    }

    private class HandlerDispatcher implements Runnable {

        private static final long NO_EVENT_SLEEP_DELAY = 50L;

        @Override
        public void run() {
            // the main event loop takes place here
            while(!stop.get()) {
                if (bus.hasEvents()) {
                    Event next = bus.getNextEvent();
                    EventHandler handler = EventHandlerFactory.getHandlerFor(next.getType());
                    // todo actually dispatch the event to a thread-pool for execution.
                    //  Each bus should have its own dedicated thread-pool of configured size.
                    //  This will allow for eg. GitEventBus with pool 1, to allow for all Git events to be handled
                    //  sequentially in a separate thread.

                    // for now, a single sequential event bus keeps asynchronous event management simple.
                    if (handler != null) {
                        handler.handle(next);
                    }
                } else {
                    try {
                        // consider a better implementation using a signal to avoid just repeatedly sleeping like this
                        Thread.sleep(NO_EVENT_SLEEP_DELAY);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            exitLock.release();
        }
    }
}
