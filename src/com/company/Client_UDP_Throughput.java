package com.company;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Client_UDP_Throughput {
    public static void main(String[] args) {
        new Client_UDP_Throughput();
    }

    public Client_UDP_Throughput () {
        long elapsed;
        byte[] b256B = new byte[256],
                b512B = new byte[512],
                b1024B = new byte[1024];

        System.out.println("Which server is running: rho, pi, or moxie?");
        Scanner scan = new Scanner(System.in);
        String serverName = scan.next();
        String hostname = serverConnect(serverName);

        elapsed = measureThroughput(b256B, hostname);
        System.out.println("256B UDP Throughput Time: " + elapsed + "\n");

        elapsed = measureThroughput(b512B, hostname);
        System.out.println("512B UDP Throughput Time: " + elapsed + "\n");

        elapsed = measureThroughput(b1024B, hostname);
        System.out.println("1024B UDP Throughput Time: " + elapsed + "\n");
    }

    private long measureThroughput(byte[] msg, String hostname) {
        InetAddress address;
        DatagramPacket packet, mpPacket;
        long start;
        byte[] ack = new byte[8], bMissing = new byte[4];
        int maxTries = 0, missingPackets = 0;
        boolean trySending = true, allReceived = false;

        while (trySending) {
            if(maxTries > 5)
                break;
            trySending = false;
            try (
                    DatagramSocket socket = new DatagramSocket()
            ) {
                address = InetAddress.getByName(hostname);
                packet = new DatagramPacket(msg, msg.length, address, Server_UDP_Throughput.PORT);
                mpPacket = new DatagramPacket(bMissing, bMissing.length, address, Server_UDP_Throughput.PORT);

                start = System.nanoTime();
                if (msg.length == 256) {
                    for (int i = 0; i < 4096; i++) {
                        socket.send(packet);
                    }
                    //System.out.println("4096x256B messages sent.");
                } else if (msg.length == 512) {
                    for (int i = 0; i < 2048; i++) {
                        socket.send(packet);
                    }
                    //System.out.println("2048x512B messages sent.");
                } else if (msg.length == 1024) {
                    for (int i = 0; i < 1024; i++) {
                        socket.send(packet);
                    }
                    //System.out.println("1024x1024B messages sent.");
                }
                // receive message back from server containing the difference between
                // how many packets were expected and how many actually arrived
                socket.receive(mpPacket);
                missingPackets = fromByteArray(mpPacket.getData());
                // if any packets didn't arrive, resend exactly the amount that were missing
                // until the server receives the full expected amount (checking for and updating
                // how many packets were missed on each loop)
                if (missingPackets > 0) {
                    System.out.println(missingPackets + " packets were missing, sending this amount.");
                    while(!allReceived) {
                        //packet = new DatagramPacket(msg, msg.length, address, Server_UDP_Throughput.PORT);      // allocating could trigger garbage collector, so doing it while timed is bad
                        for (int i = 0; i < missingPackets; i++) {
                            socket.send(packet);
                        }
                        socket.receive(mpPacket);
                        missingPackets = fromByteArray(mpPacket.getData());
                        if(missingPackets == 0)
                            allReceived = true;
                        else
                            System.out.println(missingPackets + " Still weren't received. Going agane.");
                    }
                }
                socket.setSoTimeout(10);
                packet = new DatagramPacket(ack, ack.length);
                // receive ack from server
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException ste) {
                    System.out.println("ACK wait timed out");
                    trySending = true;
                }

                if(trySending) {
                    System.out.println("Socket timed out. Retrying...");
                    maxTries++;
                } else
                    return (System.nanoTime() - start);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private String serverConnect(String name) {
        int i = 1;
        while (i > 0){
            i--;
            if (name.equalsIgnoreCase("rho"))
                return "rho.cs.oswego.edu";
            else if (name.equalsIgnoreCase("pi"))
                return "pi.cs.oswego.edu";
            else if (name.equalsIgnoreCase("moxie"))
                return "moxie.cs.oswego.edu";
            else {
                System.out.println("Invalid name, try again.");
                i++;
            }
        }

        return null;
    }

    int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}
