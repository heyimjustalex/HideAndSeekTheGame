package Game.ConcurrentCollections;

import java.util.HashMap;
import java.util.Map;

public class CustomConcurrentHashMap<K, V> {
    private final Map<K, V> map;

    public CustomConcurrentHashMap() {
        this.map = new HashMap<>();
    }

    public synchronized int size() {
        return map.size();
    }

    public synchronized boolean isEmpty() {
        return map.isEmpty();
    }

    public synchronized V get(Object key) {
        V temp = null;
        try {
            temp = map.get(key);

        } catch (Exception e) {

        }
        return temp;
    }

    public synchronized V put(K key, V value) {
        return map.put(key, value);
    }

    public synchronized V remove(Object key) {
        return map.remove(key);
    }

    public synchronized V putIfAbsent(K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
            return null;
        } else {
            return map.get(key);
        }
    }
}