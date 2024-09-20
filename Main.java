import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class CacheLevel<K, V> {
    private int capacity;
    private String evictionPolicy;
    private LinkedHashMap<K, V> cache;
    private ReentrantLock lock = new ReentrantLock();

    public CacheLevel(int capacity, String evictionPolicy) {
        this.capacity = capacity;
        this.evictionPolicy = evictionPolicy;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, evictionPolicy.equals("LRU"));
    }

    // Retrieve value by key
    public V get(K key) {
        lock.lock();
        try {
            if (cache.containsKey(key)) {
                V value = cache.get(key);
                if (evictionPolicy.equals("LRU")) {
                    cache.remove(key); // Reinsert to update LRU order
                    cache.put(key, value);
                }
                return value;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    // Insert key-value pair
    public void put(K key, V value) {
        lock.lock();
        try {
            if (cache.containsKey(key)) {
                cache.remove(key); // Remove the key if it already exists
            } else if (cache.size() >= capacity) {
                evict();
            }
            cache.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    // Eviction based on the policy
    private void evict() {
        lock.lock();
        try {
            Iterator<K> it = cache.keySet().iterator();
            if (it.hasNext()) {
                K oldestKey = it.next();
                cache.remove(oldestKey); // Evict the oldest entry (for LRU or LFU)
            }
        } finally {
            lock.unlock();
        }
    }

    // Return the cache content as a formatted string for display
    public String formatCache() {
        lock.lock();
        try {
            StringBuilder result = new StringBuilder("[");
            for (Map.Entry<K, V> entry : cache.entrySet()) {
                result.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
            if (result.length() > 1) {
                result.setLength(result.length() - 2); // Remove the last ", "
            }
            result.append("]");
            return result.toString();
        } finally {
            lock.unlock();
        }
    }
}

class MultiLevelCache<K, V> {
    private List<CacheLevel<K, V>> cacheLevels;
    private ReentrantLock lock = new ReentrantLock();

    public MultiLevelCache() {
        this.cacheLevels = new ArrayList<>();
    }

    // Add a new cache level
    public void addCacheLevel(int size, String evictionPolicy) {
        lock.lock();
        try {
            cacheLevels.add(new CacheLevel<>(size, evictionPolicy));
        } finally {
            lock.unlock();
        }
    }

    // Get value by key, promote value if found in a lower level
    public V get(K key) {
        lock.lock();
        try {
            for (int i = 0; i < cacheLevels.size(); i++) {
                V value = cacheLevels.get(i).get(key);
                if (value != null) {
                    // Promote the found value to higher cache levels
                    promote(key, value, i);
                    return value;
                }
            }
            return null; // If key not found
        } finally {
            lock.unlock();
        }
    }

    // Insert key-value pair into L1 cache
    public void put(K key, V value) {
        lock.lock();
        try {
            if (!cacheLevels.isEmpty()) {
                cacheLevels.get(0).put(key, value); // Always insert into L1
            }
        } finally {
            lock.unlock();
        }
    }

    // Remove a specific cache level
    public void removeCacheLevel(int level) {
        lock.lock();
        try {
            if (level >= 0 && level < cacheLevels.size()) {
                cacheLevels.remove(level);
            } else {
                throw new IndexOutOfBoundsException("Cache level does not exist.");
            }
        } finally {
            lock.unlock();
        }
    }

    // Promote key-value pair to higher cache levels
    private void promote(K key, V value, int currentLevel) {
        for (int i = currentLevel - 1; i >= 0; i--) {
            cacheLevels.get(i).put(key, value); // Move to higher levels (L1)
        }
    }

    // Display the contents of all cache levels in the required format
    public void displayCache() {
        lock.lock();
        try {
            for (int i = 0; i < cacheLevels.size(); i++) {
                System.out.println("L" + (i + 1) + " Cache: " + cacheLevels.get(i).formatCache());
            }
        } finally {
            lock.unlock();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        MultiLevelCache<String, String> cacheSystem = new MultiLevelCache<>();

        // Add cache levels
        cacheSystem.addCacheLevel(3, "LRU"); // L1 cache with size 3, LRU eviction
        cacheSystem.addCacheLevel(2, "LFU"); // L2 cache with size 2, LFU eviction

        // Put data into L1 cache
        cacheSystem.put("A", "1");
        cacheSystem.put("B", "2");
        cacheSystem.put("C", "3");

        // Retrieve and promote data
        System.out.println("Get A: " + cacheSystem.get("A")); // A becomes most recently used in L1

        // Add more data to cause eviction in L1
        cacheSystem.put("D", "4"); // Evicts B (LRU) from L1, B moves to L2 if space is available

        // Retrieve data from L1
        System.out.println("Get C: " + cacheSystem.get("C"));

        // Display the current state of the cache
        cacheSystem.displayCache();
    }
}
