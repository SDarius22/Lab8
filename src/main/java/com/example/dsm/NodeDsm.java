package com.example.dsm;

import java.util.*;

/**
 * DSM instance attached to a single process/node.
 * It only stores variables this node is subscribed to.
 */
public class NodeDsm implements Dsm {

    private final String nodeName;
    private final Map<Integer, Integer> localVars = new HashMap<>();
    private final Set<Integer> subscribedVarIds = new HashSet<>();
    private final List<DsmListener> listeners = new ArrayList<>();

    public NodeDsm(String nodeName, Collection<Integer> subscribedVarIds) {
        this.nodeName = nodeName;
        this.subscribedVarIds.addAll(subscribedVarIds);
        for (int varId : subscribedVarIds) {
            localVars.put(varId, 0);
            DsmBus.registerSubscriber(varId, this);
        }
    }

    private void checkSubscribed(int varId) {
        if (!subscribedVarIds.contains(varId)) {
            throw new IllegalArgumentException(
                    nodeName + " is not subscribed to var[" + varId + "]");
        }
    }

    @Override
    public void write(int varId, int value) {
        checkSubscribed(varId);
        synchronized (this) {
            localVars.put(varId, value);
            System.out.printf("[%s] local write var[%d] = %d%n", nodeName, varId, value);
        }
        // outside synchronized(this)
        DsmBus.broadcastUpdate(varId, value, this);
    }

    @Override
    public synchronized int read(int varId) {
        checkSubscribed(varId);
        int value = localVars.get(varId);
        System.out.printf("[%s] read var[%d] -> %d%n", nodeName, varId, value);
        return value;
    }

    @Override
    public boolean compareAndExchange(int varId, int expectedValue, int newValue) {
        checkSubscribed(varId);
        boolean changed;
        synchronized (this) {
            int current = localVars.get(varId);
            if (current != expectedValue) {
                System.out.printf("[%s] CAS var[%d]: expected=%d, actual=%d -> no change%n",
                        nodeName, varId, expectedValue, current);
                return false;
            }
            localVars.put(varId, newValue);
            System.out.printf("[%s] CAS var[%d]: %d -> %d%n",
                    nodeName, varId, current, newValue);
            changed = true;
        }
        if (changed) {
            DsmBus.broadcastUpdate(varId, newValue, this);
        }
        return true;
    }

    @Override
    public synchronized void addListener(DsmListener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void removeListener(DsmListener listener) {
        listeners.remove(listener);
    }

    public synchronized void applyRemoteUpdate(int varId, int newValue, long version) {
        if (!subscribedVarIds.contains(varId)) {
            return;
        }
        localVars.put(varId, newValue);
        System.out.printf("[%s] applyRemoteUpdate var[%d] = %d (version=%d)%n",
                nodeName, varId, newValue, version);
        for (DsmListener listener : listeners) {
            listener.onVariableChanged(varId, newValue, version);
        }
    }

    @Override
    public synchronized String toString() {
        return "NodeDsm{" +
                "nodeName='" + nodeName + '\'' +
                ", localVars=" + localVars +
                '}';
    }
}
