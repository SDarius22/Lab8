package com.example.dsm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        System.out.printf("[BUS] register subscriber %s for var[%d]%n",
                nodeNameOf(node), varId);
    }

    public static void broadcastUpdate(int varId, int newValue, NodeDsm sourceNode) {
        final long version;
        final Set<NodeDsm> subsSnapshot;

        synchronized (DsmBus.class) {
            version = ++globalVersion;
            Set<NodeDsm> subs = subscribersByVar.get(varId);
            if (subs == null || subs.isEmpty()) {
                System.out.printf("[BUS] v%d update var[%d]=%d from %s -> no subscribers%n",
                        version, varId, newValue, nodeNameOf(sourceNode));
                return;
            }
            subsSnapshot = new HashSet<>(subs);
            System.out.printf("[BUS] v%d broadcast var[%d]=%d from %s to %d subscriber(s)%n",
                    version, varId, newValue, nodeNameOf(sourceNode), subsSnapshot.size());
        }

        for (NodeDsm node : subsSnapshot) {
            node.applyRemoteUpdate(varId, newValue, version);
        }
    }

    private static String nodeNameOf(NodeDsm node) {
        return node.toString();
    }
}
