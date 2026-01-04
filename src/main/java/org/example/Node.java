package org.example;

import java.util.ArrayList;
import java.util.List;

public class Node implements DsmNodeEndpoint {

    private final String nodeId;
    private final DistributedSharedMemory dsm;

    private final List<String> eventLog = new ArrayList<>();

    public Node(String nodeId, DistributedSharedMemory dsm) {
        this.nodeId = nodeId;
        this.dsm = dsm;
        this.dsm.registerNode(nodeId, this);
    }

    public String getNodeId() {
        return nodeId;
    }

    public int read(String varName) {
        return dsm.read(varName);
    }

    public void write(String varName, int newValue) {
        DsmMessage msg = DsmMessage.write(nodeId, varName, newValue);
        dsm.handleMessage(msg);
    }

    public boolean compareAndExchange(String varName, int expected, int newValue) {
        DsmMessage msg = DsmMessage.compareAndExchange(nodeId, varName, expected, newValue);
        dsm.handleMessage(msg);
        return dsm.read(varName) == newValue;
    }

    @Override
    public void onVariableChanged(long sequence, String varName, int newValue) {
        String msg = "Node " + nodeId +
                " saw change #" + sequence +
                " on " + varName +
                " -> " + newValue;
        eventLog.add(msg);
        System.out.println(msg);
    }

    @Override
    public void onRequestCompleted(DsmMessage request, boolean success) {
        String msg = "Node " + nodeId +
                " request " + request.getType() +
                " on " + request.getVarName() +
                " completed, success=" + success;
        System.out.println(msg);
    }

    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }
}
