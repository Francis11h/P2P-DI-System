package peers;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class PeerServer{

    Peer peer;

    String folderLocation;

    private boolean shutDown = false;
    Map<String, String> statusMap = new HashMap<>();

    public PeerServer(Peer peer) {
        this.peer = peer;
        folderLocation = peer.folderLocation;
    }

    public void shutDown() {
        shutDown = true;
    }

    // a peer server need to handle 2 requests from another peer:
    // 1. RFCQUery: return its current RFC index
    // 2. GetRFC: send the specific RFC the the requesting server.
    public void handler(int port) {

        try {

            // build status map first
            statusMap.put("100", "FILE_NOT_FOUND");
            statusMap.put("200", "OK");
            statusMap.put("300", "BAD_REQUEST");

            ServerSocket serverSocket = new ServerSocket(port);

            peer.peerServerHostname = InetAddress.getLocalHost().getHostAddress();

            System.out.println("the server has been successfully set up, running at " + peer.peerServerHostname + ":" + port);

            while(!shutDown) {
                // read the request from the server
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new PeerSeverThread(this, socket));
                thread.start();
            }


            System.out.println("server has been shut down");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
