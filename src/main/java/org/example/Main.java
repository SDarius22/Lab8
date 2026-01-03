package org.example;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        DistributedSharedMemory dsm = new DistributedSharedMemory();

        // Define a variable `x` with initial value 0 and two subscribers: N1 and N2
        dsm.defineVariable("x", 0, Set.of("N1", "N2"));

        // Create two nodes, both subscribed to `x`
        Node node1 = new Node("N1", dsm);
        Node node2 = new Node("N2", dsm);

        // Node1 writes to x
        node1.write("x", 10);

        // Node2 does a compare-and-exchange on x: 10 -> 20
        boolean exchanged = node2.compareAndExchange("x", 10, 20);
        System.out.println("Node2 CAS result: " + exchanged);

        // Final read (from either node)
        int finalValue = node1.read("x");
        System.out.println("Final value of x: " + finalValue);
    }
}
