# P2P-file-transfer

This project implements a peer-to-peer file sharing protocol.

Instruction to compile and run

our project contains two main parts, one is RSserver, the other is Peer, which contains both peer Client and peer Server.
the codes of RSserver are in the rsServer package, the codes of Peer are in the peers package.


Our project can be compiled and run in Intellij, where can be easier for you to set arguments, but we will also provide how to compile and run our project by using command line in Mac OS.


How to compile : 

1. compile the RSserver
go to the src file 
An example of how to goto the src folder:
```
    cd /Users/hanzirun/Desktop/P2P\ file\ transfer/src
```
    
and compile the RSserver by running the follow command : 
```
  javac rsServer/RegisterServer.java
```
2. Specify a folder for a peer.
Before joining the P2P-DI system, you need to specify a folder on your computer, where files
downloaded from other peers and the file you might want to transmit to other peers is stored.
You may need to put all the files you want to transfer into this folder. Then you need to create a 'config.txt' file, which is initialized as:
```
  cookie:
  file_1_name,file_1_type,file_1_info
  file_2_name,file_2_type,file_2_info
  file_3_name,file_3_type,file_3_info
```
In the example above, the config file starts with a line with "cookie:", through which we will memorize the cookie received from Register Server. Then it is followed by information about files it holds locally. The first field is the name of the file. The second filed is the type of the file. At last, the third filed is any information you want to keep with this file, it could be the main content of the file or the author of the file or etc. Remember all the field about one single file is written within the same line and they are seperated by a comma.
  
After create the peer folder, you need to specify the location of this exact folder in the Peer.java (which is located in the peers folder in the src). We need to modify the location string of the file that the peer had in their local. which at 18th line of this file. Here's an example:
 ```
 final String folderLocation = "/Users/xuyang/Desktop/peer2/";
```
 3. compile the peer
 Go back to the src file and then compile the peers by running follow command :
```
  javac peers/Peer.java
```
By then, our compilation is complete.


How to run :

first open the RS server with the argument port number, go to the src folder and run this command (port number can be changed)

```
java rsServer.RegisterServer 65423
```

after running this you will see some notifications like that: 

```
registerServer starting at: 65423...
192.168.0.9
OS: Mac OS X
Waiting for client on port 65423...
```


And then goto the PeerClient.java to modify the RSHost and RSport for our peers to connect, which lines at 13 and 14 row.
```
final String RSHost = "192.168.0.9";
final int RSPort = 65423;
```

Then we should run our peer threads also with the argument port

```
java peers.Peer 4396
```

After running this here we will provide some "button", and you don't need the command line any more. just press one to eight to do the requset.

```
fire the server
waiting for the server to be ready ...
the server has been successfully set up, running at 192.168.0.9:4396
******************************************
Press 1: Register to RS
Press 2: PQuery (Query for active peers)
Press 3: Keep alive
Press 4: Download files automatically
Press 5: Download files manually
Press 6: Get file index
Press 7: Leave
Press 8: Help
********************************************
Enter an operation :
```



So first you need to input '1' to register to RS
you need to input '2' to query the active peer list
you need to input '3' to tell RS that you are alive
you need to input '4' to Download files automatically from P2P system
you need to input '5' to Download files manually (then you also need to input host and port on the next step)
you need to input '6' to Get file index
you need to input '7' to Leave the RS
you need to input '8' to Get help



