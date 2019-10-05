package peers;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class PeerClient {

    // a well known IP address and port
    final String RSHost = "10.153.10.215";
    final int RSPort = 6000;

    private Peer peer;

    private List<String> downloadTimes = new LinkedList<>();

    private List<FileIndex> newFiles;

    private List<PeerInfo> activePeers; // a list of host name

    public PeerClient(Peer peer) {

        this.peer = peer;
        newFiles = new LinkedList<>();
        activePeers = new LinkedList<>();

    }

    public void printActivePeers() {

        System.out.println("Following are the active peers currently:");
        for(PeerInfo p : activePeers) {

            System.out.println(p.getHostName() + ":" + p.getPort() + " last registered at " + p.getLast_registered());

        }
        System.out.println();

    }

    public void downloadIdxFromAPeer() {

        if(activePeers.size() == 0) {
            System.out.println("No active peer servers");
            return;
        }

        printActivePeers();
        System.out.print("Enter an host you want to retrieve the active list: ");
        Scanner input = new Scanner(System.in);
        String host = input.nextLine();

        // TODO: deal with invalid input

        String[] info = host.split(":");
        getIndexFromPeer(info[0], Integer.parseInt(info[1]));

    }

    public void printNewIdx() {

        System.out.println("Following are the idx you might want to download:");
        for(FileIndex f : newFiles) {

            System.out.println(f.fileName + "." + f.fileType + " at " + f.hostname + ":" + f.port);

        }
        System.out.println();

    }

    public void downloadFileManually() {

//        if(newFiles.size() == 0) {
//            System.out.println("no new file to download");
//            return;
//        }

        printNewIdx();
        Scanner input = new Scanner(System.in);
        System.out.print("Enter the file name you want to download ");
        String row = input.nextLine();
        String[] file = row.split("\\.");

        String type = file[file.length - 1];
        StringBuilder name = new StringBuilder();
        for(int i=0; i<file.length-1; i++) {
            name.append(file[i]);
            if(i != file.length-2)
                name.append(".");
        }

        System.out.print("Enter the host you want to download this file from ");
        row = input.nextLine();
        String[] info = row.split(":");
        getFilesFromPeer(info[0], Integer.parseInt(info[1]), name.toString(), type);

    }

    public void automaticDownload() {

        long totalBeginTime = System.nanoTime();

        long beginTime = totalBeginTime;
        long timeEclipsed;

        getActivePeersFromRS();

        String curIP = peer.peerServerHostname + ":" + peer.serverPort;

        for(PeerInfo peer: activePeers) {

            String ip = peer.getHostName() + ":" + peer.getPort();
            if(ip.equals(curIP))
                continue;

            getIndexFromPeer(peer.getHostName(), peer.getPort());

            // newFile is updateed
            // build a new list to get rid of remove exception
            List<FileIndex> tmp = new LinkedList<>();
            for(FileIndex f : newFiles)
                tmp.add(f);

            for(FileIndex file : tmp) {
                // get each new file
                if(file.ttl > 0) {
                    getFilesFromPeer(file.hostname, file.port, file.fileName, file.fileType);

                    timeEclipsed = System.nanoTime() - beginTime;
                    downloadTimes.add(timeEclipsed / 1000 + " us");
                    beginTime = System.nanoTime();

                }

            }

        }

        long totalTimeEclisped = System.nanoTime() - totalBeginTime;

        downloadTimes.add("The total time is: " + totalTimeEclisped/1000 + " us");

        generateReport();

    }

    private void generateReport() {

        System.out.println("********* report ***********");
        int l = downloadTimes.size();
        for(int i=0; i<l-1; i++) {

            System.out.println(" download " + (i+1) + "_th file needs time: " + downloadTimes.get(i) );

        }

        System.out.println(downloadTimes.get(l-1));

    }

    public void sendRegisterRequest() {

        try {
            Socket socket = new Socket(RSHost, RSPort);
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println("Register NULL P2P_DI_1.0");
            out.println("Host " + InetAddress.getLocalHost().getHostAddress());
            out.println("OS " + System.getProperty("os.name"));
            out.println("");
            out.println("cookie:" + (peer.cookie == -1 ? "" : peer.cookie));
            out.println("port:" + peer.serverPort);
            out.flush();
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            System.out.println("Response for register request: ");

            String row = br.readLine();
            System.out.println(row);
            String status = row.split(" ")[0];

            // print the response
            for(int i=0; i<4; i++) {
                row = br.readLine();
                System.out.println(row);
            }

            String[] cookie = row.split(":");

            System.out.println("End of the response");

            if(status.equals("200") && peer.cookie == -1 && cookie.length > 1) {
                peer.cookie = Integer.parseInt(cookie[1]);
                // write the cookie to the config.txt file
                writeCookieToConfig(peer.cookie);
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeCookieToConfig(int cookie) {
        try {
            // input the (modified) file content to the StringBuffer "input"
            BufferedReader file = new BufferedReader(new FileReader(peer.folderLocation + "config.txt"));
            StringBuffer inputBuffer = new StringBuffer();
            String line;

            boolean firstLine = true;
            while ((line = file.readLine()) != null) {
                if(firstLine) {
                    line = "cookie:" + cookie;
                    firstLine = false;
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();

            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream(peer.folderLocation + "config.txt");
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("cannot write cookie to config.txt");
        }
    }

    public void sendLeaveRequest() {
        try {
            Socket socket = new Socket(RSHost, RSPort);
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println("Leave NULL P2P_DI_1.0");
            out.println("Host " + InetAddress.getLocalHost().getHostAddress());
            out.println("OS " + System.getProperty("os.name"));
            out.println("");
            out.println("cookie:" + peer.cookie);
            out.println("port:" + peer.serverPort);
            out.flush();
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            System.out.println("Response for register request: ");

            String row;

            // print the response
            while((row = br.readLine()) != null)
                System.out.println(row);

            System.out.println("End of the response");

            socket.close();

            requestToShutDownPeerServer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestToShutDownPeerServer() {

        try {
            Socket socket = new Socket(peer.peerServerHostname, peer.serverPort);
            System.out.println("try to shut down the peer server: " + peer.peerServerHostname + ":" + peer.serverPort);
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println("SHUTDOWN NULL P2P_DI_1.0");
            out.println("Host " + InetAddress.getLocalHost().getHostAddress());
            out.println("OS " + System.getProperty("os.name"));
            out.println("");
            out.println("NULL");
            out.println("NULL");
            out.flush();
            System.out.println("shut down request has been sent");
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            System.out.println("Response for shutdown request: ");

            String row;

            // print the response
            while((row = br.readLine()) != null)
                System.out.println(row);

            System.out.println("End of the response");

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendKeepAliveRequest() {
        try {
            Socket socket = new Socket(RSHost, RSPort);
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println("KeepAlive NULL P2P_DI_1.0");
            out.println("Host " + InetAddress.getLocalHost().getHostAddress());
            out.println("OS " + System.getProperty("os.name"));
            out.println("");
            out.println("cookie:" + peer.cookie);
            out.println("port:" + peer.serverPort);
            out.flush();
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            System.out.println("Response for register request: ");

            String row;

            // print the response
            while((row = br.readLine()) != null)
                System.out.println(row);

            System.out.println("End of the response");

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getActivePeersFromRS() {

        try {
            // this information is obtaied from active peer list from server
            Socket socket = new Socket(RSHost, RSPort);

            // send request to the server -------------

            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println("PQuery NULL P2P_DI_1.0");
            out.println("Host " + InetAddress.getLocalHost().getHostAddress());
            out.println("OS " + System.getProperty("os.name"));
            out.println("");
            out.println("cookie:" + peer.cookie);
            out.println("cookie:" + peer.serverPort);
            out.flush();

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            System.out.println("try to read response from the server");

            String row = br.readLine();
            System.out.println(row);

            // print the response

            for(int i=0; i<3; i++) {
                row = br.readLine();
                System.out.println(row);
            }

            activePeers.clear();
            while((row = br.readLine()) != null) {
                System.out.println(row);
                String[] info = row.split(" ");
                activePeers.add(new PeerInfo(info[0], Integer.parseInt(info[1])));
            }

            System.out.println("read has completed");

            System.out.println("client has successfully received the active lists");

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getIndexFromPeer(String hostName, int port) {

        try {

            // this information is obtaied from active peer list from server
            Socket socket = new Socket(hostName, port);

            // send request to the server -------------

            requestIndex(socket);

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            System.out.println("try to read header information from the server");

            String row = br.readLine();
            System.out.println(row);
            String status = row.split(" ")[0];

            // print the response
            while((row = br.readLine()) != null)
                System.out.println(row);

            System.out.println("read has completed");
            socket.close();


            // receive the index --------

            System.out.println("client begins to merge the index");

            if(status.equals("200")) {

                socket = new Socket(hostName, port);

                OutputStream os = socket.getOutputStream();
                PrintWriter out = new PrintWriter(os);
                out.println("INDEX NULL P2P-DI_1.0");
                out.println("Host " + InetAddress.getLocalHost().getHostAddress());
                out.println("OS " + System.getProperty("os.name"));
                out.println("");
                out.println("NULL");
                out.println("NULL");
                out.flush();

                receiveIndex(socket);
                socket.close();

                System.out.println("client has successfully merge the index");

            } else {
                System.out.println("cannot merge index from the another peer");
            }

        } catch (Exception e) {
            System.out.println("The Peer Server you request to connect might have shut down");
            e.printStackTrace();
        }

    }

    private void getIndexFromPeer(String hostName) {

        int port = getPort(hostName);

        try {

            // this information is obtaied from active peer list from server
            Socket socket = new Socket(hostName, port);

            // send request to the server -------------

            requestIndex(socket);

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            System.out.println("try to read header information from the server");

            String row = br.readLine();
            System.out.println(row);
            String status = row.split(" ")[0];

            // print the response
            while((row = br.readLine()) != null)
                System.out.println(row);

            System.out.println("read has completed");
            socket.close();


            // receive the index --------

            System.out.println("client begins to merge the index");

            socket = new Socket(hostName, port);

            if(status.equals("200")) {

                receiveIndex(socket);

                System.out.println("client has successfully merge the index");

            } else {
                System.out.println("cannot merge index from the another peer");
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getFilesFromPeer(String hostName, int port, String fileName, String fileType) {

        try {

            // this information is obtaied from active peer list from server
            Socket socket = new Socket(hostName, port);

            // send request to the server -------------

            reuqestFile(socket, fileName, fileType);

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            System.out.println("try to read header information from the server");

            String row = br.readLine();
            System.out.println(row);
            String status = row.split(" ")[0];

            // print the response
            while((row = br.readLine()) != null)
                System.out.println(row);

            System.out.println("read has completed");

            br.close();
            is.close();
            socket.close();


            // receive and read the file --------

            System.out.println("client begins to download the file");

            if(status.equals("200")) {

                socket = new Socket(hostName, port);

                OutputStream os = socket.getOutputStream();
                PrintWriter out = new PrintWriter(os);
                out.println("FILE NULL P2P-DI_1.0");
                out.println("Host " + InetAddress.getLocalHost().getHostAddress());
                out.println("OS " + System.getProperty("os.name"));
                out.println("");
                out.println(fileName);
                out.println(fileType);
                out.flush();

                receiveFile(socket, fileName, fileType);
                socket.close();

                System.out.println("client has successfully received the file");

            } else {
                System.out.println("cannot download file from the server");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getFilesFromPeer(String hostName, String fileName, String fileType) {

        int port = getPort(hostName);

        try {

            // this information is obtaied from active peer list from server
            Socket socket = new Socket(hostName, port);

            // send request to the server -------------

            reuqestFile(socket, fileName, fileType);

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            System.out.println("try to read header information from the server");

            String row = br.readLine();
            System.out.println(row);
            String status = row.split(" ")[0];

            // print the response
            while((row = br.readLine()) != null)
                System.out.println(row);

            System.out.println("read has completed");

            br.close();
            is.close();
            socket.close();


            // receive and read the file --------

            System.out.println("client begins to download the file");

            socket = new Socket(hostName, port);

            if(status.equals("200")) {

                receiveFile(socket, fileName, fileType);

                System.out.println("client has successfully received the file");

            } else {
                System.out.println("cannot download file from the server");
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getPort(String hostName) {

        for(PeerInfo p : activePeers) {

            if(hostName.equals(p.getHostName()))
                return p.getPort();

        }

        System.out.println("cannot find this host in the active peers");
        return -1;
    }

    private void reuqestFile(Socket socket, String fileName, String fileType) {
        try {
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println("GET RFC P2P-DI_1.0");
            out.println("Host " + InetAddress.getLocalHost().getHostAddress());
            out.println("OS " + System.getProperty("os.name"));
            out.println("");
            out.println(fileName);
            out.println(fileType);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // receive this file into the receive folder
    private void receiveFile(Socket socket, String fileName, String fileType) {

        try {
            InputStream is = socket.getInputStream();

            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(peer.folderLocation + fileName + "." + fileType);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int bytesRead = is.read(buffer, 0, buffer.length);
            while (bytesRead > 0) {
                bos.write(buffer, 0, bytesRead);
                bytesRead = is.read(buffer, 0, buffer.length);
            }

            bos.close();

            System.out.println("file has been downloaded");
            // write the file information to config.txt
            updateConfigFile(fileName, fileType);
            System.out.println("modify the config file to include this newly-downloaded file");

            // remove this item in the newFile list
            String fn = fileName + "." + fileType;
            for(FileIndex f : newFiles) {
                String curFileName = f.fileName + "." + f.fileType;
                if(fn.equals(curFileName)) {
                    newFiles.remove(f);
                    break;
                }
            }

            // add a new node to the head of the linked list
            peer.files.add(0, new FileIndex(fileName, fileType, "???", peer.peerServerHostname, peer.serverPort,7200));


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateConfigFile(String fileName, String fileType) {

        try {
            FileWriter fw = new FileWriter(peer.folderLocation + "config.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            out.println(fileName+","+fileType+",NULL");
            bw.close();
            fw.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
            e.printStackTrace();
        }

    }

    private void requestIndex(Socket socket) {
        try {
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            out.println("GET " + "Index" + " P2P-DI_1.0");
            out.println("Host " + InetAddress.getLocalHost().getHostAddress());
            out.println("OS " + System.getProperty("os.name"));
            out.println("");
            out.println("NULL");
            out.println("NULL");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveIndex(Socket socket) {
        try {

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            List<FileIndex> idxes = (List)ois.readObject();

            // find the files to be added
            // this is inefficient O( m * n )
            newFiles.clear();

            HashSet<FileIndex> set = new HashSet<>();

            for(FileIndex f : idxes) {

                boolean found = false;
                String curName = f.fileName + "." + f.fileType;
                for(FileIndex i : peer.files) {
                    String tmpName = i.fileName + "." + i.fileType;
                    if(tmpName.equals(curName) && i.hostname.equals(peer.peerServerHostname) && i.port == peer.serverPort) {
                        found = true;
                        break;
                    }
                }

                if(!found)
                    set.add(f);

            }

            for(FileIndex fi : set)
                newFiles.add(fi);

            System.out.println(newFiles.size() + " file(s) has been detected");

            // merge the newly get list with local list
            for(FileIndex f : idxes) {
                peer.files.add(f);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
