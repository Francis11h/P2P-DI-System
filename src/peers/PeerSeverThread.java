package peers;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class PeerSeverThread implements Runnable{

    Socket socket;
    PeerServer peerServer;

    public PeerSeverThread(PeerServer peerServer, Socket socket) {

        this.socket = socket;
        this.peerServer = peerServer;
    }

    @Override
    public void run() {

        try {
            InputStream is = socket.getInputStream();

            // read the request from the server
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            System.out.println();

            String row = br.readLine();

            String status;

            if (row == null) {
                status = "300";
                sendResponse(socket, status);
            } else {
                String[] firstLine = row.split(" ");

                if (firstLine[0].equals("GET")) {
                    status = "200";
                    sendResponse(socket, status);
                } else if (firstLine[0].equals("SHUTDOWN")) {
                    status = "200";
                    sendResponse(socket, status);
                    peerServer.shutDown();
                } else if (firstLine[0].equals("FILE")) {
                    for(int i=0; i<4; i++)
                        row = br.readLine();
                    String fileName = row;
                    String fileType = br.readLine();
                    sendFile(socket, fileName, fileType);
                } else if (firstLine[0].equals("INDEX")) {
                    sendIndex(socket);
                } else {
                    status = "300";
                    sendResponse(socket, status);
                }
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendResponse(Socket socket, String status) {

        OutputStream os;
        try {
            os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println(status + " " + peerServer.statusMap.get(status) + " P2P-DI_1.0");
            out.println("Host " + InetAddress.getLocalHost().getHostAddress());
            out.println("OS " + System.getProperty("os.name"));
            out.println("");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendIndex(Socket socket) {


        try {
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(peerServer.peer.files);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendFile(Socket socket, String RFCName, String fileType) {

        try {
            OutputStream os = socket.getOutputStream();

            System.out.println("server has detected a client");

            byte[] buffer = new byte[1024];
            File file = new File(peerServer.folderLocation + RFCName + "." + fileType);
            System.out.println(peerServer.folderLocation + RFCName + "." + fileType);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

            int bytesRead = bis.read(buffer, 0, buffer.length);
            while (bytesRead > 0) {
                os.write(buffer, 0, bytesRead);
                bytesRead = bis.read(buffer, 0, buffer.length);
            }

            bis.close();
            os.close();
            socket.close();
            System.out.println("the file has been sent");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
