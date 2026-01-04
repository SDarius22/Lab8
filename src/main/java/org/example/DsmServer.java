package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class DsmServer {

    private final DistributedSharedMemory dsm;
    private final int port;

    public DsmServer(DistributedSharedMemory dsm, int port) {
        this.dsm = dsm;
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("DSM server listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    DsmMessage msg = DsmMessageCodec.decode(line);
                    dsm.handleMessage(msg);
                } catch (Exception e) {
                    System.err.println("Failed to process message '" + line + "': " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        }
    }
}
