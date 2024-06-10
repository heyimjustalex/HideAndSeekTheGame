package Game.ConcurrentCollections;

import java.util.LinkedList;
import java.util.Queue;

public class CustomConcurrentQueue<E> {
    private final Queue<E> queue;

    public CustomConcurrentQueue() {
        this.queue = new LinkedList<>();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized boolean add(E element) {
        return queue.add(element);
    }

    public synchronized E poll() {
        return queue.poll();
    }

}