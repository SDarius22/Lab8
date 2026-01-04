package org.example;

import java.io.IOException;
import java.util.Set;

public class DsmServerMain {

    public static void main(String[] args) throws IOException {
        int port = 9999;

        DistributedSharedMemory dsm = new DistributedSharedMemory();

        dsm.defineVariable("x", 0, Set.of("N1", "N2"));

        Node n1 = new Node("N1", dsm);
        Node n2 = new Node("N2", dsm);

        System.out.println("DSM server starting on port " + port);
        System.out.println("Subscribers: " + n1.getNodeId() + ", " + n2.getNodeId());

        DsmServer server = new DsmServer(dsm, port);
        server.start();
    }
}
