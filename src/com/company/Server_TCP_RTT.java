package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server_TCP_RTT {
    protected static final int PORT = 27081;

    public static void main(String[] args) {
        new Server_TCP_RTT();
    }
    public Server_TCP_RTT() {

        System.out.println("Awaiting connection...");
        while(true) {
            try (   // try-with-resources (auto-closes connections opened as "resources")
                    ServerSocket serverSocket = new ServerSocket(PORT);
                    Socket socket = serverSocket.accept();
                    DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))
            ){
                //System.out.println("Connected.");
                byte[] msg = new byte[1024];
                byte c;
                int byteCount = 0;

                while ((c = in.readByte()) != -1) {     // read from input stream until a -1 is recieved
                    msg[byteCount] = c;
                    byteCount++;
                }
                for(int i = 0; i < byteCount; i++) {    // echo back the entire message that was recieved
                    out.writeByte(msg[i]);
                }
                out.writeByte(-1);
                out.flush();
                System.out.println("Payload recieved and echoed.");
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection.");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
