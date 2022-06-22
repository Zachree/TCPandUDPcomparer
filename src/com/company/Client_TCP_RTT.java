package com.company;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client_TCP_RTT {
    private static final long key = 6942069420694206942L;

    public static void main(String[] args)  {
        new Client_TCP_RTT();
    }

    public Client_TCP_RTT()  {
        long elapsed, p8B = Long.MAX_VALUE ^ key;
        long[] p64B = new long[8],
                p256B = new long[32],
                p1024B = new long[128];

        // build message payloads and encrypt with XOR
        for(int i = 0; i < p1024B.length; i++){
            p1024B[i] = Long.MAX_VALUE ^ key;
            if(i < p256B.length)
                p256B[i] = Long.MAX_VALUE ^ key;
            if(i < p64B.length)
                p64B[i] = Long.MAX_VALUE ^ key;
        }
        // break down longs into byte arrays (for sending)
        byte[] b8B = longToBytes(p8B),
                b64B = longArrayToBytes(p64B),
                b256B = longArrayToBytes(p256B),
                b1024B = longArrayToBytes(p1024B);
        Socket socket;

        System.out.println("Which server is running: rho, pi, or moxie?");
        Scanner scan = new Scanner(System.in);
        String serverName = scan.next();

        socket = serverConnect(serverName);
        elapsed = measureRTT(b8B, socket);
        System.out.println("\n8MB TCP RTT Time: " + elapsed + "\n");

        socket = serverConnect(serverName);
        elapsed = measureRTT(b64B, socket);
        System.out.println("\n64MB TCP RTT Time: " + elapsed + "\n");

        socket = serverConnect(serverName);
        elapsed = measureRTT(b256B, socket);
        System.out.println("\n256MB TCP RTT Time: " + elapsed + "\n");

        socket = serverConnect(serverName);
        elapsed = measureRTT(b1024B, socket);
        System.out.println("\n1024MB TCP RTT Time: " + elapsed + "\n");
    }

    private long measureRTT(byte[] msg, Socket socket) {
        byte[] echo = new byte[1024];
        long start, temp;
        byte c;
        int byteCount = 0, j = 0;
        try (   // try-with-resources (auto-closes connections opened as "resources")
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        ) {
            start = System.nanoTime();
            for (byte value : msg) {
                out.writeByte(value);
            }
            out.writeByte(-1);
            out.flush();
            while ((c = in.readByte()) != -1) {
                echo[byteCount] = c;
                byteCount++;
            }
            // validate (by turning 8 byte sequences into a long and then unencrytping that long)
            while(j < byteCount) {
                temp = bytesToLong(new byte[] {echo[j], echo[j+1], echo[j+2], echo[j+3], echo[j+4], echo[j+5], echo[j+6], echo[j+7]});
                if((temp ^ key) != Long.MAX_VALUE) {
                    System.out.println("ERROR: Recieved faulty information.\ntemp = " + temp);
                    break;
                }
                j += 8;
            }

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

    private byte[] longArrayToBytes(long[] pkg) {
        byte[] bytes = new byte[pkg.length * 8],
                temp;
        int tracker = 0;
        for (long l : pkg) {
            temp = longToBytes(l);
            for (byte b : temp) {
                bytes[tracker] = b;
                tracker++;
            }
        }
        return bytes;
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }

    public static long bytesToLong(final byte[] b) {
        long result = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}