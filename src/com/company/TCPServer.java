package com.company;

import java.io.*;
import java.net.*;
import java.time.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.logging.*;

/**
 * The TCPServer class creates a multi-threaded server, launching a new thread for each new client connected
 * This allows multiple clients to connect at once.
 */
class TCPServer {

    public static void main(String argv[]) throws Exception {
        int port = 8421;

        ServerSocket welcomeSocket = new ServerSocket(port);

        while (true) {

            Socket newClient = welcomeSocket.accept();

            ClientHandler clientThreaded = new ClientHandler(newClient);

            new Thread(clientThreaded).start();

        }//while(true)
    }//main
}//TCPServer
/**
 * The ClientHandler class creates a thread for a given client, handling all messages from it
 */
class ClientHandler implements Runnable {
    private Socket socket;
    private boolean isConnected;
    private String name;
    private Instant connectTimestamp;
    private Instant disconnectTimestamp;

    /**
    * Initial values
    * isConnected = false
    * socket = s (passed through on creation)
    * connectTimestamp = Instant.now(), aka current instant from system clock
    */
    public ClientHandler(Socket s) {
        this.isConnected = false;
        this.socket = s;
        this.connectTimestamp = Instant.now();
    }//ClientHandler constructor

    /**
     * Prepare for clean disconnect
     */
    public void disconnect()
    {
        isConnected = false;
        disconnectTimestamp = Instant.now();
        //disconnection logic
    }//disconnect

    /**
     * Trim whitespace from a message to prepare to interpret it as a math problem
     */
    public String trimWhitespace(String s)
    {
        String ret = s.trim();
        ret = ret.replaceAll(" ","");
        return ret;
    }//trimWhitespace

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
     * Problem must be in format of a /op/ b.
     * This correctly return NaN for divide by 0
     * Returns solution as string ret
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

    }//solve

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

        }//if(isConnected)
    }//reply

    /**
     * Creates logger and file per connection.
     * Handle all user messages
     * While the client socket is open
     *      We read the client message into clientSentence through a buffer
     *          The first number is the ID type of the message
     *          1- join, 2- disconnect, 3- message, 4- keepAlive, 5- serverReply, 6- default
     */
    public void run() {
        //creates logger object by the name of logFile
        Logger LOGGER = Logger.getLogger("logFile");

        //one file created per user connection.  Handler added to logger.
        Handler fileHandler = null;
        try{
            fileHandler = new FileHandler("./TCPServerLog.log");
            LOGGER.addHandler(fileHandler);
            fileHandler.setLevel(Level.ALL);
        }catch(IOException exception){
            LOGGER.log(Level.SEVERE, "Error in fileHandler");
        }

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


                if (clientSentence != null && !clientSentence.equals("")) {
                    Message m = new Message(clientSentence);
                    //Handle each type of message id
                    switch (m.getId())
                    {
                        case 1:
                            System.out.println("Case 1 - Join");
                            isConnected = true;
                            name = m.getBody();
                            outToClient.writeBytes(Message.makeJoin(name) + '\n');

                            LOGGER.info("Username: " + name + " successfully connected at: " + Instant.now());
                            break;
                        case 2:
                            System.out.println("Case 2 - Disconnect");
                            disconnect();
                            LOGGER.info("Username: " + name + " successfully disconnected at: " + Instant.now());
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
                    }//switch
                }//if

            }//while(true)
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Username: " + name + " had an unclean disconnect at: " + Instant.now());
            e.printStackTrace();
        }
    }//run
}//ClientHandler


