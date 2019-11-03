package peers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Peer implements Runnable{

    private PeerClient client;
    List<FileIndex> files;
    int cookie = -1;

    int serverPort;
    String peerServerHostname;

    final String folderLocation = "/Users/hanzirun/P2P-DI System/peerFiles/peer2/";

    @Override
    public void run() {
        // 每次 peer 开始 run的时候 是 作为 server的
        // set up the server
        PeerServer peerServer = new PeerServer(this);
        peerServer.handler(serverPort);

    }

    private void printInfo() {

        System.out.println("******************************************");
        System.out.println("Press 1: Register to RS");
        System.out.println("Press 2: PQuery (Query for active peers)");
        System.out.println("Press 3: Keep alive");
        System.out.println("Press 4: Download files automatically");
        System.out.println("Press 5: Download files manually");
        System.out.println("Press 6: Get file index");
        System.out.println("Press 7: Leave");
        System.out.println("Press 8: Help");
        System.out.println("********************************************");

    }

    private void construcIdxFromConfig() {

        files = new LinkedList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(folderLocation + "config.txt"));
            String row;

            // read cookie
            row = reader.readLine();
            String[] co = row.split(":");
            if(co.length == 2)
                cookie = Integer.parseInt(co[1]);

            while((row = reader.readLine()) != null) {
                if(row.length() == 0)
                    continue;
                String[] info = row.split(",");
                files.add(new FileIndex(info[0], info[1], info[2], peerServerHostname, serverPort,7200));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws InterruptedException {

        // first create a new server for the PeerServer to run.

        if(args.length != 1) {
            System.out.println("invalid input argument, please specify the port number");
            return;
        }

        Peer tmp = new Peer();

        tmp.serverPort = Integer.parseInt(args[0]);

        Thread serverThread = new Thread(tmp);
        serverThread.start();
        System.out.println("fire the peer server");
        System.out.println("waiting for the server to be ready ...");
        Thread.sleep(1000);

        tmp.construcIdxFromConfig();
        tmp.client = new PeerClient(tmp);
        // then forever loop to wait for user input to decide what to do next
        tmp.printInfo();
        while(true) {

            Scanner input = new Scanner(System.in);

            System.out.print("Enter an operation : ");
            int operation = input.nextInt();

            switch (operation) {

                case 1:
                    tmp.client.sendRegisterRequest();
                    break;
                case 2:
                    tmp.client.getActivePeersFromRS();
                    break;
                case 3:
                    tmp.client.sendKeepAliveRequest();
                    break;
                case 4:
                    tmp.client.automaticDownload();
                    break;
                case 5:
                    tmp.client.downloadFileManually();
                    break;
                case 6:
                    tmp.client.downloadIdxFromAPeer();
                    break;
                case 7:
                    tmp.client.sendLeaveRequest();
                    System.exit(10);
                case 8:
                    tmp.printInfo();
                    break;

            }
        }

    }

}
