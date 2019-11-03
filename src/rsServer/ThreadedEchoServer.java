package rsServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// 同时处理 多个 的写法
public class ThreadedEchoServer {

    static final int PORT = 1978;

    public static void main(String args[]) {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                assert serverSocket != null;
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            // For every client you need to start separate thread.
            new EchoThread(socket).start();
        }
    }
}