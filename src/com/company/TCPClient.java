package com.company;

import java.io.*;
import java.net.*;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The TCPClient class creates a client and connects to a server
 */
class TCPClient {

    /**
     * The main method gathers user data, then branches into Threads to send messages, receive messages, and to accept user input.
     */
    public static boolean receivedGoAhead = false;

    public static void main(String argv[]) throws Exception {
        boolean receivedGoAhead = false;
        int port = 8421;
        String username;
        String IP;
        String msg;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        //Request IP of server from user
        System.out.println("Enter Server IP: (or # for 127.0.0.1)");
        IP = inFromUser.readLine();

        if (IP.equals("#")) {
            IP = "127.0.0.1";
        }

        //Request username
        System.out.println("Enter username: ");
        username = inFromUser.readLine();

        //Create connection
        Socket clientSocket = new Socket(IP, port);

        //Create message sending and receiving threads
        MessageReader r = new MessageReader(clientSocket);
        MessageSender w = new MessageSender(clientSocket);

        new Thread(r).start();
        new Thread(w).start();

        //Automatically send join request to server

        w.addMessage(Message.makeJoin(username) + '\n');

        int tries = 0;
        while(!r.receivedGoAhead)
        {
            Thread.sleep(100);
            System.out.println("Connecting...");
            tries ++;
            if(tries > 100)
            {
                System.out.println("Failed to connect.");
                clientSocket.close();
                return;
            }
        }
        System.out.println("Connected!");

        //Send as many messages as the user wants
        while (true) {
            if(clientSocket.isClosed())
            {
                System.out.println("Socket closed from server side. Disconnected.");
                return;
            }
            System.out.println("Enter message: (of the form ....) or # to disconnect");

            msg = inFromUser.readLine();

            //Disconnect or sent user message
            if (msg.equals("#")) {
                w.addMessage(Message.makeDisconnect(0, username));
                break;
            } else {
                w.addMessage(Message.makeUserMessage(msg));
            }
        }


        //Wait for disconnect message to send, then close the socket.
        Thread.sleep(200);
        clientSocket.close();

        System.out.println("Clean Disconnect.");
    }

}
/**
 * The MessageReader class runs a thread that reads and prints messages from the server
 */
class MessageReader implements Runnable {
    public boolean receivedGoAhead;
    private Socket socket;
    private BufferedReader dataIn;

    /**
     * The MessageReader constructor takes a Socket and creates a Buffer to read from it
     */
    public MessageReader(Socket sock) throws IOException {
        this.receivedGoAhead = false;
        this.socket = sock;
        this.dataIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    /**
     * The run method reads lines from the socket until it closes, and prints any Server-Replies it receives
     */
    public void run() {
        while (true) {
            if(socket.isClosed())
            {
                return;
            }

            String reply = null;

            try {
                reply = dataIn.readLine();
            } catch (IOException e) {
                break;
            }

            if (reply != null) {
                Message m = new Message(reply);
                if(m.getId() == 5)
                {
                    System.out.println("Reply from server: " + m.getBody());
                }
                else if(m.getId() == 1)
                {
                    receivedGoAhead = true;
                }
            }


        }
    }
}

/**
 * The MessageSender class runs a thread that sends any messages put in its thread-safe Queue
 */
class MessageSender implements Runnable {
    //Send keepalives to keep the connection enabled every tenth of a second
    private final static int keepaliveTimer = 100;
    private Socket socket;
    private ConcurrentLinkedQueue<String> q;
    private DataOutputStream dataOut;

    /**
     * The MessageSender constructor takes a Socket and creates a Stream to send data across it
     */
    public MessageSender(Socket sock) throws IOException {
        this.socket = sock;
        this.dataOut = new DataOutputStream(this.socket.getOutputStream());
        this.q = new ConcurrentLinkedQueue<String>();
    }

    /**
     * addMessage adds a String message to a queue to be sent to the server
     */
    public void addMessage(String msg) {
        q.add(msg);
    }

    /**
     * The run method sends all enqueued messages every keepaliveTimer milliseconds, adding a keepalive if no messages are queued
     */
    public void run() {
        try {
            while (true) {
                if(socket.isClosed())
                {
                    return;
                }

                Thread.sleep(keepaliveTimer);

                if (q.size() == 0) {
                    q.add(Message.makeKeepAlive());
                }

                while (q.size() > 0) {
                    dataOut.writeBytes(q.poll() + '\n');
                }


            }
        } catch (InterruptedException e) {

        } catch (IOException e) {

        }
    }


}