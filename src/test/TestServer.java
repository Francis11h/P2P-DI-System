package test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {

    static String folderLocation = "/Users/xuyang/Desktop/peer1/";

    public static void main(String[] args) {

        try {

            ServerSocket serverSocket = new ServerSocket(4396);

            while(true) {

                // read the request from the server
                Socket socket = serverSocket.accept();

                OutputStream os = socket.getOutputStream();

                System.out.println("server has detected a client");

                byte[] buffer = new byte[1024];
                File file = new File(folderLocation + "959.txt");
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

                int bytesRead = bis.read(buffer, 0, buffer.length);
                while (bytesRead > 0) {
                    os.write(buffer, 0, bytesRead);
                    bytesRead = bis.read(buffer, 0, buffer.length);
                }

                bis.close();

                socket.close();


                System.out.println("the file has been sent");


                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
