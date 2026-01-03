package org.example;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class DsmClient implements AutoCloseable {

    private final String host;
    private final int port;
    private final Socket socket;
    private final PrintWriter out;

    public DsmClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    public void send(DsmMessage message) {
        String line = DsmMessageCodec.encode(message);
        out.println(line);
    }

    @Override
    public void close() throws IOException {
        out.flush();
        socket.close();
    }

    public String getRemoteAddress() {
        return host + ":" + port;
    }
}
