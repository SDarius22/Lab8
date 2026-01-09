package com.example;

import com.example.dsm.Dsm;
import com.example.dsm.NodeDsm;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int processCount = 3;
        
        List<Integer> p0Vars = Arrays.asList(0, 1);
        List<Integer> p1Vars = Arrays.asList(1, 2);
        List<Integer> p2Vars = Arrays.asList(2, 3, 4);

        NodeDsm[] dsms = new NodeDsm[processCount];
        dsms[0] = new NodeDsm("P0", p0Vars);
        dsms[1] = new NodeDsm("P1", p1Vars);
        dsms[2] = new NodeDsm("P2", p2Vars);

        Thread[] processes = new Thread[processCount];

        for (int pid = 0; pid < processCount; pid++) {
            final int processId = pid;
            final NodeDsm dsm = dsms[pid];
            processes[pid] = new Thread(() -> runProcess(processId, dsm), "Process-" + processId);
            processes[pid].start();
        }

        for (Thread t : processes) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("=== FINAL DSM STATE ===");
        for (NodeDsm dsm : dsms) {
            System.out.println(dsm);
        }
    }

    private static void runProcess(int processId, NodeDsm dsm) {
        Dsm.DsmListener listener = (varId, newValue, version) -> {
            System.out.printf("[CALLBACK P%d] received v%d for var[%d] -> %d%n",
                    processId, version, varId, newValue);
        };
        dsm.addListener(listener);

        switch (processId) {
            case 0 -> {
                System.out.println("[PROC 0] starting");
                dsm.write(0, 10);
                sleepQuietly(2000);
                dsm.compareAndExchange(1, 0, 100);
            }
            case 1 -> {
                System.out.println("[PROC 1] starting");
                dsm.write(1, 20);
                sleepQuietly(2000);
                dsm.compareAndExchange(2, 0, 200);
            }
            case 2 -> {
                System.out.println("[PROC 2] starting");
                dsm.write(2, 30);
                sleepQuietly(2000);
                dsm.compareAndExchange(3, 0, 300);
            }
            default -> {
            }
        }

        dsm.removeListener(listener);
        System.out.printf("[PROC %d] finished%n", processId);
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
