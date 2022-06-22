package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server_TCP_Throughput {
    protected static final int PORT = 27081;

    public static void main(String[] args) {
        new Server_TCP_Throughput();
    }

    public Server_TCP_Throughput() {
        System.out.println("Awaiting connection...");
        while(true) {
            try (   // try-with-resources (auto-closes connections opened as "resources")
                    ServerSocket serverSocket = new ServerSocket(PORT);
                    Socket socket = serverSocket.accept();
                    DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))
            ){
                //System.out.println("Connected.");
                byte[] msg;
                byte c;
                int byteCount;

                while ((c = in.readByte()) != -1) {     // double while loop to control for an abstract amount of message sends,
                    msg = new byte[1024];               // where the end of a message stream is signified by two -1 messages.
                    byteCount = 0;
                    while (c != -1) {
                        msg[byteCount] = c;
                        //System.out.println("Reading from stream: msg[" + byteCount + "]: " + msg[byteCount]);
                        byteCount++;
                        c = in.readByte();
                    }
                }
                out.writeLong(-1);
                out.flush();
                System.out.println("Payload recieved.");
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host ");
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection.");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
