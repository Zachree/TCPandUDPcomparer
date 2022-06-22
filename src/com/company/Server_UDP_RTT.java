package com.company;

import java.io.*;
import java.net.*;

public class Server_UDP_RTT extends Thread {
    public static final int PORT = 27081;

    protected DatagramSocket socket;

    public static void main(String[] args) throws IOException {
        new Server_UDP_RTT().start();
    }

    public Server_UDP_RTT() throws IOException {
        this("Server_UDP_RTT");
    }

    public Server_UDP_RTT(String name) throws IOException {
        super(name);
        socket = new DatagramSocket(PORT);
    }

    public void run() {
        System.out.println("Awaiting connections...");
        while (true) {
            try {
                byte[] buf = new byte[1024];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                System.out.println("Packet received.");

                // send the echo to the client at "address" and "port"
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                socket.send(packet);
                System.out.println("Echo sent.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}