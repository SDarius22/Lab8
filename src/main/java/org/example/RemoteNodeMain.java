package org.example;

public class RemoteNodeMain {

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 9999;
        String nodeId = "N1"; // must match a subscriber defined on the server

        try (DsmClient client = new DsmClient(host, port)) {
            RemoteNode node = new RemoteNode(nodeId, client);

            System.out.println("Remote node " + nodeId +
                    " connected to " + client.getRemoteAddress());

            // Send a few operations
            node.write("x", 10);
            Thread.sleep(500);

            node.compareAndExchange("x", 10, 20);
            Thread.sleep(500);

            node.compareAndExchange("x", 10, 30); // should fail (no local feedback yet)
            Thread.sleep(500);
        }

        System.out.println("Remote node " + nodeId + " finished.");
    }
}
