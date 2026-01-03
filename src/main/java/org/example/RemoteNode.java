package org.example;

public class RemoteNode {

    private final String nodeId;
    private final DsmClient client;

    public RemoteNode(String nodeId, DsmClient client) {
        this.nodeId = nodeId;
        this.client = client;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void write(String varName, int newValue) {
        DsmMessage msg = DsmMessage.write(nodeId, varName, newValue);
        client.send(msg);
    }

    public void compareAndExchange(String varName, int expected, int newValue) {
        DsmMessage msg = DsmMessage.compareAndExchange(nodeId, varName, expected, newValue);
        client.send(msg);
    }
}
