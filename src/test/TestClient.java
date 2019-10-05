package test;

import peers.FileIndex;

import java.io.*;
import java.net.Socket;

public class TestClient {

    static String folderLocation = "/Users/xuyang/Desktop/peer2/";

    public static void main(String[] args) {
        try {

            // this information is obtaied from active peer list from server
            Socket socket = new Socket("127.0.0.1", 4396);

            System.out.println("connection eatablished");
            InputStream is = socket.getInputStream();

            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(folderLocation + "959.txt");
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int bytesRead = is.read(buffer, 0, buffer.length);
            while (bytesRead > 0) {
                bos.write(buffer, 0, bytesRead);
                bytesRead = is.read(buffer, 0, buffer.length);
            }

//            int bytesRead;
//
//            while((bytesRead = is.read(buffer, 0, buffer.length)) > 0) {
//                bos.write(buffer, 0, bytesRead);
//            }

            bos.close();

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
