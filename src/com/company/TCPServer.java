package com.company;

import java.io.*;
import java.net.*;
import java.time.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TCPServer class creates a multi-threaded server, launching a new thread for each new client connected
 */
class TCPServer {

    public static void main(String argv[]) throws Exception {
        int port = 8421;

        ServerSocket welcomeSocket = new ServerSocket(port);

        while (true) {

            Socket newClient = welcomeSocket.accept();

            ClientHandler clientThreaded = new ClientHandler(newClient);

            new Thread(clientThreaded).start();

        }
    }
}
/**
 * The ClientHandler class creates a thread for a given client, handling all messages from it
 */
class ClientHandler implements Runnable {
    private Socket socket;
    private boolean isConnected;
    private String name;
    private Instant connectTimestamp;
    private Instant disconnectTimestamp;

    public ClientHandler(Socket s) {
        this.isConnected = false;
        this.socket = s;
        this.connectTimestamp = Instant.now();
    }

    /**
     * Prepare for clean disconnect
     */
    public void disconnect()
    {
        isConnected = false;
        disconnectTimestamp = Instant.now();
        //disconnection logic
    }

    /**
     * Trim whitespace from a message to prepare to interpret it as a math problem
     */
    public String trimWhitespace(String s)
    {
        String ret = s.trim();
        ret = ret.replaceAll(" ","");
        return ret;
    }

    /**
     * Regex match a valid math problem
     */
    public boolean validate(String s)
    {
        Pattern p = Pattern.compile("^[0-9]+[+\\-*/][0-9]+$");
        Matcher m = p.matcher(s);
        return m.find();
    }

    /**
     * Solve a given (trimmed and valid) string math problem
     */
    public String solve(String s)
    {
        int ret = 0;
        Pattern p = Pattern.compile("[0-9]+|[+\\-*/]");
        Matcher m = p.matcher(s);
        m.find();
        int a = Integer.parseInt(m.group());

        m.find();
        String op = m.group();

        m.find();
        int b = Integer.parseInt(m.group());

        if(op.equals("+"))
        {
            ret = a + b;
        }
        else if(op.equals("-"))
        {
            ret = a - b;
        }
        else if(op.equals("*"))
        {
            ret = a * b;
        }
        else if(op.equals("/"))
        {
            if(b == 0)
            {
                return "NaN";
            }
            ret = a / b;
        }
        else
        {
            return "ERROR";
        }
        return "" + ret;



    }

    /**
     * Reply to a user math problem request
     */
    public void reply(String message, DataOutputStream d) throws IOException {
        if(isConnected)
        {
            String reply = "";
            String trim_message = trimWhitespace(message);
            if(!validate(trim_message))
            {
                reply = "ERROR";
            }
            else
            {
                reply = solve(trim_message);
            }
            d.writeBytes(Message.makeServerReply(message + "=" + reply) + '\n');

        }
    }

    /**
     * Handle all user messages
     */
    public void run() {
        try {
            while (true) {
                if(socket.isClosed())
                {
                    return;
                }

                //Prepare I/O from socket
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());

                //read message from client
                String clientSentence = inFromClient.readLine();


                if (clientSentence != null) {
                    Message m = new Message(clientSentence);
                    //Handle each type of message id
                    switch (m.getId())
                    {
                        case 1:
                            System.out.println("Case 1 - Join");
                            isConnected = true;
                            name = m.getBody();
                            break;
                        case 2:
                            System.out.println("Case 2 - Disconnect");
                            disconnect();
                            return;
                        case 3:
                            System.out.println("Case 3 - Message");
                            reply(m.getBody(),outToClient);
                            break;
                        case 4:
                            //System.out.println("Case 4 - KeepAlive");
                            break;
                        case 5:
                            System.out.println("Case 5 - ServerReply");
                            break;
                        default:
                            System.out.println("Default");
                            break;
                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


