package com.alexhqi.saveshare.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class QueuedEventBus implements EventBus {

    private Queue<Event> queue = new ConcurrentLinkedDeque<>();

    @Override
    public void registerEvent(Event event) {
        queue.add(event);
    }

    @Override
    public Event getNextEvent() {
        return queue.poll();
    }

    @Override
    public boolean hasEvents() {
        return !queue.isEmpty();
    }
}
