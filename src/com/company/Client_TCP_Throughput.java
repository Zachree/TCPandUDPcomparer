package com.company;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client_TCP_Throughput {

    public static void main(String[] args) {
        new Client_TCP_Throughput();
    }

    public Client_TCP_Throughput() {
        long elapsed;
        byte[] b256B = new byte[256],
                b512B = new byte[512],
                b1024B = new byte[1024];

        System.out.println("Which server is running: rho, pi, or moxie?");
        Scanner scan = new Scanner(System.in);
        String serverName = scan.next();
        Socket socket;

        socket = serverConnect(serverName);
        elapsed = measureThroughput(b256B, socket);
        System.out.println("\n256B TCP Throughput Time: " + elapsed + "\n");

        socket = serverConnect(serverName);
        elapsed = measureThroughput(b512B, socket);
        System.out.println("\n512B TCP Throughput Time: " + elapsed + "\n");

        socket = serverConnect(serverName);
        elapsed = measureThroughput(b1024B, socket);
        System.out.println("\n1024B TCP Throughput Time: " + elapsed + "\n");
    }

    private static long measureThroughput(byte[] msg, Socket socket) {
        long start;
        try (   // try-with-resources (auto-closes connections opened as "resources")
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))
        ) {
            start = System.nanoTime();
            if (msg.length == 256) {
                for (int i = 0; i < 4096; i++) {
                    for (byte value : msg) {
                        out.writeByte(value);
                    }
                    out.writeByte(-1);   // signal end of a single message
                }
                System.out.println("4096x256B messages sent.");
            } else if (msg.length == 512) {
                for (int i = 0; i < 2048; i++) {
                    for (byte value : msg) {
                        out.writeByte(value);
                    }
                    out.writeByte(-1);   // signal end of a single message
                }
                System.out.println("2048x512B messages sent.");
            } else if(msg.length == 1024) {
                for (int i = 0; i < 1024; i++) {
                    for (byte value : msg) {
                        out.writeByte(value);
                    }
                    out.writeByte(-1);   // signal end of a single message
                }
                System.out.println("1024x1024B messages sent.");
            }
            out.writeByte(-1);   // second -1 'message end' signifies the end of the test
            out.flush();
            System.out.println("Waiting for ACK...");
            while (in.readLong() != -1) {
                // spin
            }
            System.out.println("ACK recieved.");

            return (System.nanoTime() - start);

        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection. responseTimer");
            e.printStackTrace();
            System.exit(1);
        }
        return 0;
    }

    private Socket serverConnect(String name) {
        int i = 1;
        while (i > 0){
            i--;
            try {
                if (name.equalsIgnoreCase("rho"))
                    return new Socket("129.3.20.24", Server_TCP_Throughput.PORT);
                else if (name.equalsIgnoreCase("pi"))
                    return new Socket("129.3.20.26", Server_TCP_Throughput.PORT);
                else if (name.equalsIgnoreCase("moxie"))
                    return new Socket("129.3.20.3", Server_TCP_Throughput.PORT);
                else {
                    System.out.println("Invalid name, try again.");
                    i++;
                }
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection. responseTimer");
                e.printStackTrace();
                System.exit(1);
            }
        }

        return null;
    }
}
