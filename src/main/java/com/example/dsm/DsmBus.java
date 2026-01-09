package com.example.dsm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates the network between nodes and provides a global total order
 * of updates across all variables.
 */
public final class DsmBus {

    private static final Map<Integer, Set<NodeDsm>> subscribersByVar =
            new ConcurrentHashMap<>();
    private static long globalVersion = 0L;

    private DsmBus() {
    }

    public static synchronized void registerSubscriber(int varId, NodeDsm node) {
        subscribersByVar
                .computeIfAbsent(varId, k -> new HashSet<>())
                .add(node);
    }

    public static void broadcastUpdate(int varId, int newValue, NodeDsm sourceNode) {
        final long version;
        final Set<NodeDsm> subsSnapshot;

        // Get version and a snapshot of subscribers under lock
        synchronized (DsmBus.class) {
            version = ++globalVersion;
            Set<NodeDsm> subs = subscribersByVar.get(varId);
            if (subs == null || subs.isEmpty()) {
                return;
            }
            subsSnapshot = new HashSet<>(subs);
        }

        // Deliver outside the bus lock to avoid nested locking
        for (NodeDsm node : subsSnapshot) {
            node.applyRemoteUpdate(varId, newValue, version);
        }
    }
}
