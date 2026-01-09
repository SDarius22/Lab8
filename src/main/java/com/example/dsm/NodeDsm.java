package com.example.dsm;

import java.util.*;


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
            System.out.printf("[NODE %s] subscribe to var[%d], initial value = 0%n",
                    nodeName, varId);
            DsmBus.registerSubscriber(varId, this);
        }
    }

    private void checkSubscribed(int varId) {
        if (!subscribedVarIds.contains(varId)) {
            throw new IllegalArgumentException(
                    "[NODE " + nodeName + "] access to var[" + varId + "] denied (not subscribed)");
        }
    }

    @Override
    public void write(int varId, int value) {
        checkSubscribed(varId);
        synchronized (this) {
            int old = localVars.get(varId);
            localVars.put(varId, value);
            System.out.printf("[NODE %s] local WRITE var[%d]: %d -> %d%n",
                    nodeName, varId, old, value);
        }
        // outside synchronized(this)
        DsmBus.broadcastUpdate(varId, value, this);
    }

    @Override
    public synchronized int read(int varId) {
        checkSubscribed(varId);
        int value = localVars.get(varId);
        System.out.printf("[NODE %s] READ var[%d] -> %d%n",
                nodeName, varId, value);
        return value;
    }

    @Override
    public boolean compareAndExchange(int varId, int expectedValue, int newValue) {
        checkSubscribed(varId);
        boolean changed;
        synchronized (this) {
            int current = localVars.get(varId);
            if (current != expectedValue) {
                System.out.printf("[NODE %s] CAS FAILED var[%d]: expected=%d, actual=%d%n",
                        nodeName, varId, expectedValue, current);
                return false;
            }
            localVars.put(varId, newValue);
            System.out.printf("[NODE %s] CAS SUCCESS var[%d]: %d -> %d%n",
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
        System.out.printf("[NODE %s] listener registered%n", nodeName);
    }

    @Override
    public synchronized void removeListener(DsmListener listener) {
        listeners.remove(listener);
        System.out.printf("[NODE %s] listener removed%n", nodeName);
    }
    
    public synchronized void applyRemoteUpdate(int varId, int newValue, long version) {
        if (!subscribedVarIds.contains(varId)) {
            return;
        }
        int old = localVars.get(varId);
        localVars.put(varId, newValue);
        System.out.printf("[NODE %s] APPLY UPDATE v%d var[%d]: %d -> %d%n",
                nodeName, version, varId, old, newValue);
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
