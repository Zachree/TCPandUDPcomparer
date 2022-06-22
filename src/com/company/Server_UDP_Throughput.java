package com.company;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Server_UDP_Throughput extends Thread {
    protected static final int PORT = 27081;
    protected DatagramSocket socket;
    InetAddress address;
    int port;

    public static void main(String[] args) throws IOException {
        new Server_UDP_Throughput().start();
    }

    public Server_UDP_Throughput() throws IOException {
        this("Server_UDP_Throughput");
    }

    public Server_UDP_Throughput(String name) throws IOException {
        super(name);
        socket = new DatagramSocket(PORT);
    }

    public void run() {
        int meta = 0;
        System.out.println("Awaiting connections...");
        while (true) {
            try {
                boolean allReceived = false;
                byte[] buf = new byte[1024],
                        ack = new byte[8],
                        zero = new byte[4];
                int packetCount, remaining;
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                packetCount = receivePackets(packet, buf);
                System.out.println("Packets received: " + packetCount);
                while(!allReceived) {
                    if (meta == 0) {
                        remaining = 4096 - packetCount;
                        System.out.println(remaining + " packets missing.");
                        if (remaining > 0)
                            packetCount += requestPackets(packet, buf, remaining);
                    } else if (meta == 1) {
                        remaining = 2048 - packetCount;
                        System.out.println(remaining + " packets missing.");
                        if (remaining > 0)
                            packetCount += requestPackets(packet, buf, remaining);
                    } else if (meta == 2) {
                        remaining = 1024 - packetCount;
                        System.out.println(remaining + " packets missing.");
                        if (remaining > 0)
                            packetCount += requestPackets(packet, buf, remaining);
                    }
                    if (packetCount == 4096 || packetCount == 2048 || packetCount == 1024) {
                        System.out.println("All received, packetCount = " + packetCount);
                        allReceived = true;
                        meta++;
                        socket.send(new DatagramPacket(zero, zero.length, address, port));
                    } else
                        System.out.println(packetCount + " Packets received. Rerequesting the missing ones.");
                }

                // send an ACK to the client at "address" and "port"
                packet = new DatagramPacket(ack, ack.length, address, port);
                socket.send(packet);
                System.out.println("ACK sent.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(meta > 2)
                break;
        }
        socket.close();
    }

    private int requestPackets(DatagramPacket packet, byte[] buf, int remaining) {
        byte[] bRemaining = toByteArray(remaining);
        // send request to the client for remaining number of packets (that didn't arrive)
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(bRemaining, bRemaining.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        packet = new DatagramPacket(buf, buf.length, address, port);
        return receivePackets(packet, buf);
    }

    private int receivePackets(DatagramPacket packet, byte[] buf) {
        boolean timedout = false;
        int packetCount = 0;
        try {
            socket.setSoTimeout(0);
            socket.receive(packet);     // wait to receive first packet before setting the socket timeout
            address = packet.getAddress();
            port = packet.getPort();
            while (!timedout) {
                packetCount++;
                // receive request
                try {
                    socket.setSoTimeout(10);
                    socket.receive(packet);
                } catch (SocketTimeoutException ste) {
                    timedout = true;
                }
                packet = new DatagramPacket(buf, buf.length);
                //System.out.println(packetCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return packetCount;
    }

    byte[] toByteArray(int value) {
        return  ByteBuffer.allocate(4).putInt(value).array();
    }
}