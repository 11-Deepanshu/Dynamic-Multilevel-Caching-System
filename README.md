Overview
This Java-based dynamic multi-level caching system efficiently manages data across multiple cache levels, supporting dynamic cache level additions, eviction policies (LRU or LFU), and concurrent reads and writes. The system is designed to handle cache data movement and ensure efficient lookups with minimal cache misses. This document explains the approach, key decisions made during implementation, and instructions for running the application.


Key Features
.Multi-Level Cache: Supports multiple cache levels where each level has its own size and eviction policy (either Least Recently Used (LRU) or Least Frequently Used (LFU)).

.Dynamic Cache Levels: Cache levels can be dynamically added or removed at runtime based on system needs.

.Eviction Policies:

.LRU (Least Recently Used): The least recently used entry is evicted when the cache is full.
.LFU (Least Frequently Used): (Implemented in principle, but currently not used; can be extended as needed).
.Thread-Safety: The system uses thread locks to ensure thread-safe access and modification of the cache across multiple threads, allowing concurrent reads and writes.

.No Data Promotion to Lower Cache Levels: When a cache is full, the evicted items are discarded instead of being moved to a lower cache level.

.Design and Approach


Key Decisions
1 Cache Structure:

2 LinkedHashMap was chosen for cache storage as it provides predictable iteration order, which is crucial for LRU implementation.
3 Thread-safety was implemented using ReentrantLock to support concurrency without compromising performance or correctness.
Eviction Policy:

4 Only LRU eviction is fully supported for now. LFU can be implemented by modifying the eviction logic and tracking access frequency in the cache.
Simplified Cache Design:

5 No movement to lower levels: Once a cache entry is evicted from the highest level, it's discarded instead of moving to lower-level caches.
6 Cache promotion to higher levels occurs when items are accessed from lower levels.
7 Thread-Safety:

8 Locking is used to ensure concurrent access to the cache does not lead to inconsistent or corrupted data.



How it Works:
1 When you insert key-value pairs, they are stored in the L1 cache until it reaches its capacity. Once full, eviction happens according to the LRU policy.
2 Evicted entries from L1 are moved to the L2 cache (if available), and the system keeps track of all levels.
3 The displayCache() method prints the state of each cache level, formatted as per the given requirement.



How to Run the Application
1. Pre-Requisites
 Java Development Kit (JDK) 8+
 A Java IDE (e.g., IntelliJ, Eclipse) or a simple text editor with javac and java commands for compilation and execution.
2. Compilation and Execution
Step 1: Clone the repository

git clone <repository-url>
cd dynamic-multilevel-cache


Step 2: Compile the code In your terminal or command prompt, navigate to the folder containing the Main.java file and run:

javac Main.java


Step 3: Run the application After successful compilation, run the program using:
java Main
3. Expected Output
After running the program with the provided cache operations, you should see the following output:

Get A: 1
Get C: 3
L1 Cache: [A: 1, D: 4, C: 3]
L2 Cache: []
