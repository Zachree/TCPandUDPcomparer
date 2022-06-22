package com.company;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client_UDP_RTT {
    private static final long key = 6942069420694206942L;

    public static void main(String[] args) throws IOException {
        new Client_UDP_RTT();
    }

    public Client_UDP_RTT() throws IOException {
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

        System.out.println("Which server is running: rho, pi, or moxie?");
        Scanner scan = new Scanner(System.in);
        String serverName = scan.next();
        String hostname = serverConnect(serverName);

        elapsed = measureRTT(b8B, hostname);
        System.out.println("\n8MB UDP RTT Time: " + elapsed + "\n");

        elapsed = measureRTT(b64B, hostname);
        System.out.println("\n64MB UDP RTT Time: " + elapsed + "\n");

        elapsed = measureRTT(b256B, hostname);
        System.out.println("\n256MB UDP RTT Time: " + elapsed + "\n");

        elapsed = measureRTT(b1024B, hostname);
        System.out.println("\n1024MB UDP RTT Time: " + elapsed + "\n");
    }

    private long measureRTT(byte[] msg, String hostname) throws IOException {
        int j = 0;
        InetAddress address;
        DatagramPacket packet;
        boolean echoReceived = false;
        long start, temp;
        byte[] echo;

        address = InetAddress.getByName(hostname);
        packet = new DatagramPacket(msg, msg.length, address, Server_UDP_RTT.PORT);

        start = System.nanoTime();
        while (!echoReceived) {
            echoReceived = true;
            try (
                    DatagramSocket socket = new DatagramSocket()
            ) {
                socket.send(packet);
                socket.setSoTimeout(1000);
                //packet = new DatagramPacket(msg, msg.length);   // allocating can trigger garbage collector, so doing it while timed is bad
                socket.receive(packet);
            }  catch (SocketTimeoutException ste) {
                echoReceived = false;
                System.out.println("Socket timed out. Retrying...");
            } catch (SocketException se) {
                System.out.println("Socket exception.");
                se.printStackTrace();
                System.exit(1);
            } catch (UnknownHostException e) {
                System.out.println("Don't know about host.");
                e.printStackTrace();
                System.exit(1);
            }
            echo = packet.getData();
            // validate (by turning 8 byte sequences into a long and then unencrytping that long)
            while(j < echo.length) {
                temp = bytesToLong(new byte[] {echo[j], echo[j+1], echo[j+2], echo[j+3], echo[j+4], echo[j+5], echo[j+6], echo[j+7]});
                if((temp ^ key) != Long.MAX_VALUE) {
                    System.out.println("ERROR: Recieved faulty information.\ntemp = " + temp);
                    break;
                }
                j += 8;
            }
        }

        return (System.nanoTime() - start);
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
