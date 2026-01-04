package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DistributedSharedMemory {

    private static class Variable {
        volatile int value;
        final Set<String> subscribers; // node IDs

        Variable(int initialValue, Set<String> subscribers) {
            this.value = initialValue;
            this.subscribers = Collections.unmodifiableSet(new HashSet<>(subscribers));
        }
    }

    // Maps variable name -> Variable
    private final Map<String, Variable> variables = new ConcurrentHashMap<>();

    // Maps nodeId -> endpoint (callback + request completion)
    private final Map<String, DsmNodeEndpoint> nodeEndpoints = new ConcurrentHashMap<>();

    // Global sequence for total ordering of changes
    private final AtomicLong globalSequence = new AtomicLong(0L);

    public void registerNode(String nodeId, DsmNodeEndpoint endpoint) {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(endpoint, "endpoint");
        nodeEndpoints.put(nodeId, endpoint);
    }

    public void defineVariable(String name, int initialValue, Set<String> subscribers) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(subscribers, "subscribers");
        if (subscribers.isEmpty()) {
            throw new IllegalArgumentException("Variable must have at least one subscriber");
        }
        variables.put(name, new Variable(initialValue, subscribers));
    }

    public void handleMessage(DsmMessage message) {
        Objects.requireNonNull(message, "message");

        boolean success;
        switch (message.getType()) {
            case WRITE -> {
                write(message.getNodeId(), message.getVarName(), message.getValue());
                success = true;
            }
            case COMPARE_AND_EXCHANGE -> {
                Integer expected = message.getExpected();
                if (expected == null) {
                    throw new IllegalArgumentException("Expected value is required for CAS");
                }
                success = compareAndExchange(
                        message.getNodeId(),
                        message.getVarName(),
                        expected,
                        message.getValue()
                );
            }
            default -> throw new IllegalStateException("Unknown message type: " + message.getType());
        }

        DsmNodeEndpoint senderEndpoint = nodeEndpoints.get(message.getNodeId());
        if (senderEndpoint != null) {
            senderEndpoint.onRequestCompleted(message, success);
        }
    }

    public int read(String varName) {
        Variable var = getExistingVariable(varName);
        return var.value;
    }

    public void write(String nodeId, String varName, int newValue) {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(varName, "varName");

        Variable var = getExistingVariable(varName);

        if (!var.subscribers.contains(nodeId)) {
            throw new IllegalStateException("Node " + nodeId +
                    " is not allowed to write variable " + varName);
        }

        long seq;
        synchronized (var) {
            var.value = newValue;
            seq = globalSequence.incrementAndGet();
        }

        notifySubscribers(varName, var, seq, newValue);
    }

    public boolean compareAndExchange(String nodeId, String varName,
                                      int expectedValue, int newValue) {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(varName, "varName");

        Variable var = getExistingVariable(varName);

        if (!var.subscribers.contains(nodeId)) {
            throw new IllegalStateException("Node " + nodeId +
                    " is not allowed to update variable " + varName);
        }

        long seq;
        synchronized (var) {
            if (var.value != expectedValue) {
                return false;
            }
            var.value = newValue;
            seq = globalSequence.incrementAndGet();
        }

        notifySubscribers(varName, var, seq, newValue);
        return true;
    }

    private Variable getExistingVariable(String name) {
        Variable var = variables.get(name);
        if (var == null) {
            throw new IllegalArgumentException("Unknown variable: " + name);
        }
        return var;
    }

    private void notifySubscribers(String varName, Variable var, long seq, int newValue) {
        for (String subscriberId : var.subscribers) {
            DsmNodeEndpoint endpoint = nodeEndpoints.get(subscriberId);
            if (endpoint != null) {
                endpoint.onVariableChanged(seq, varName, newValue);
            }
        }
    }
}
