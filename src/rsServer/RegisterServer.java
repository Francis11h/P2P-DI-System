package rsServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class RegisterServer implements Runnable {

    private ServerSocket serverSocket;

    private List<Peer> peerList;

    private static final String version = "P2P-CI/1.0";

    public RegisterServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        peerList = new LinkedList<>();

        System.out.println("registerServer starting at: " + serverSocket.getLocalPort() + "...");
        System.out.println(InetAddress.getLocalHost().getHostAddress());
        System.out.println("OS: " + System.getProperty("os.name"));
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...\n");
                Socket server = serverSocket.accept();

                String client = server.getRemoteSocketAddress().toString();
                String[] clientInfo = client.split(":");    // clientInfo[0] inet address, clientInfo[1] port
                System.out.println("Connection Established with  " + clientInfo[0] + " client at " + clientInfo[1] + " port\n");

                InputStreamReader in = new InputStreamReader(server.getInputStream());
                BufferedReader input = new BufferedReader(in);
                //BufferedReader input = new BufferedReader(new InputStreamReader(server.getInputStream()));

                OutputStream out= server.getOutputStream();
                PrintWriter output = new PrintWriter(out, true);
                //PrintWriter output = new PrintWriter(server.getOutputStream(), true);

                executeRequest(input, output);

                //output.println("Thank you for connecting to "+ server.getLocalSocketAddress() + "\nGoodbye!");
                output.flush();

                server.close();
            } catch (SocketTimeoutException s) {
                System.out.println("socket time out");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Whoops, IO ... Exception Happened");
                break;
            }
        }
    }


    private void executeRequest(BufferedReader input, PrintWriter output) throws UnknownHostException {
        List<String> message = new ArrayList<>();
        String line;
        int i = 0;

        try {
            while (i <= 5) {
                line = input.readLine();
                i++;
                System.out.println(line);
                message.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] header = message.get(0).split(" ");
        String hostName = message.get(1).split(" ")[1];
        String OS = message.get(2).split(" ")[1];
        Integer cookie = null;
        String[] cookies = message.get(4).split(":");
        if (cookies.length == 2) {
            cookie = Integer.parseInt(cookies[1]);
        }
        String[] ports = message.get(5).split(":");
        Integer port = Integer.parseInt(ports[1]);

        switch(header[0]) {
            case "Register" : {
                System.out.println("---------------register------------");
                registerPeer(hostName, cookie, port, output);
                break;
            }
            case "Leave" : {
                System.out.println("---------------leave------------");
                leavePeer(cookie, port, output);
                break;
            }
            case "PQuery" : {
                System.out.println("---------------PQuery------------");
                pQuery(cookie, port, output);
                break;
            }
            case "KeepAlive" : {
                System.out.println("---------------KeepAlive------------");
                keepAlive(cookie, port, output);
                break;
            }
        }
    }

    private void registerPeer(String hostName, Integer cookie, Integer port, PrintWriter output) throws UnknownHostException {
        Peer now = null;
        if (cookie == null) {
            now = new Peer(hostName, port);
            peerList.add(now);
            System.out.println("new client has been accepted!\n");
        } else {
            for (Peer peer : peerList) {
                if (peer.getHostName().equals(hostName) && peer.getCookie().equals(cookie)) {
                    peer.renew_register();
                    peer.initialize_ttl();
                    now = peer;
                    System.out.println("this client has already registered!\n, The cookie of this client is " + now.getCookie());
                    break;
                }
            }
        }
        output.println(MessageEnum.OK.getCode() + " " + MessageEnum.OK.getMessage() + " " + version);
        output.println("Host: " + InetAddress.getLocalHost().getHostAddress());
        output.println("OS: " + System.getProperty("os.name"));
        output.println("");
        output.println("cookie:" + now.getCookie());
        //output.println("cookie = " + now.getCookie() + "  " + "TTL = " + now.getTTL());
    }

    private void leavePeer(Integer cookie, Integer port, PrintWriter output) throws UnknownHostException {
        boolean isValid = false;
        for (Peer peer : peerList) {
            if (peer.getCookie().equals(cookie) && peer.getPort().equals(port) && peer.is_active()) {
                System.out.println("Peer has been unregistered from the RS server!");
                isValid = true;
                peer.mark_inactive();
                break;
            }
        }
        if (isValid) {
            output.println(MessageEnum.OK.getCode() + " " + MessageEnum.OK.getMessage() + " " + version);
            output.println("Host: " + InetAddress.getLocalHost().getHostAddress());
            output.println("OS: " + System.getProperty("os.name"));
            output.println("");
            output.println("you have been unregistered from the RS server!");
        } else {
            output.println(MessageEnum.FORBIDDEN.getCode() + " " + MessageEnum.FORBIDDEN.getMessage() + " " + version);
            output.println("Host: " + InetAddress.getLocalHost().getHostAddress());
            output.println("OS: " + System.getProperty("os.name"));
            output.println("");
            output.println("Error! Not registered or Not active yet");
        }
    }

    private void keepAlive(Integer cookie, Integer port, PrintWriter output) throws UnknownHostException {
        for (Peer peer : peerList) {
            if (peer.getCookie().equals(cookie) && peer.getPort().equals(port)) {
                peer.initialize_ttl();
                peer.renew_register();

                output.println(MessageEnum.OK.getCode() + " " + MessageEnum.OK.getMessage() + " " + version);
                output.println("Host: " + InetAddress.getLocalHost().getHostAddress());
                output.println("OS: " + System.getProperty("os.name"));
                output.println("");
                output.println("you have been renew! TTL is 7200 seconds");
                return;
            }
        }
        output.println(MessageEnum.FORBIDDEN.getCode() + " " + MessageEnum.FORBIDDEN.getMessage() + " " + version);
        output.println("Host: " + InetAddress.getLocalHost().getHostAddress());
        output.println("OS: " + System.getProperty("os.name"));
        output.println("");
        output.println("Error! Please register first");
    }

    private void pQuery(Integer cookie, Integer port , PrintWriter output) throws UnknownHostException {
        List<Peer> activePeerList = null;

        for (Peer peer : peerList) {
            if (peer.getCookie().equals(cookie) && peer.getPort().equals(port)) {
                int decrement_value = (int) peer.getTimeDiff(peer.getLast_registered());
                peer.decrementTTL(decrement_value);

                if (peer.is_active()) {
                    peer.initialize_ttl();
                    peer.renew_register();
                    activePeerList = new LinkedList<>();

                    for (Peer candidate : peerList) {
                        decrement_value = (int) candidate.getTimeDiff(candidate.getLast_registered());
                        candidate.decrementTTL(decrement_value);

                        if (candidate.is_active()) {
                            System.out.println("Peer: " + candidate.getCookie() + "  now TTL is: " + candidate.getTTL());
                            activePeerList.add(candidate);
                        } else {
                            System.out.println("Peer: " + candidate.getCookie() + "  has expired ");
                        }
                    }
                }
                break;
            }
        }
        if (activePeerList != null && activePeerList.size() > 0) {
            output.println(MessageEnum.OK.getCode() + " " + MessageEnum.OK.getMessage() + " " + version);
            output.println("Host: " + InetAddress.getLocalHost().getHostAddress());
            output.println("OS: " + System.getProperty("os.name"));
            output.println("");
            for (Peer activePeer : activePeerList) {
                output.println(activePeer.getHostName() + " " + activePeer.getPort());
            }
            return;
        }
        output.println(MessageEnum.FORBIDDEN.getCode() + " " + MessageEnum.FORBIDDEN.getMessage() + " " + version);
        output.println("Host: " + InetAddress.getLocalHost().getHostAddress());
        output.println("OS: " + System.getProperty("os.name"));
        output.println("");
        output.println("Error! activePeerList is empty");
    }




    public static void main(String[] args) {
        if (args.length == 1) {
            int port = Integer.parseInt(args[0]);
            try {
//                // just new a RS server
//                RegisterServer a = new RegisterServer(port);
//                Thread rs = new Thread(a, "registerServerThread");  // a is an obj ref variable of class RegisterServer which implements Runnable
//                rs.start();
                new Thread(new RegisterServer(port)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error! Please Run as: java RegisterSocket #port\n");
        }
    }
}
