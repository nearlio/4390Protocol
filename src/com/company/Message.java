package com.company;

/**
 * The Message class handles the communication protocol for the client and server
 */
public class Message {
    public int getId() {
        return id;
    }

    public int getMeta() {
        return meta;
    }

    public String getBody() {
        return body;
    }

    /**
     * The id is type of Message the object is
     * 1 = User Join
     * 2 = User Disconnect
     * 3 = User Message
     * 4 = Keepalive
     * 5 = Server Reply
     */
    private int id;

    /**
     * meta is extra information, used for debugging connection issues
     * 0 = Clean disconnect
     */
    private int meta;

    /**
     * body is the text sent with the message.
     * For Joins and disconnects, it is the username.
     * For User and Server Messages, it is the text.
     */
    private String body;

    /**
     * The constructor converts a String to a Message object
     */
    public Message(String line) {
        this.id = line.charAt(0) - '0';
        this.meta = line.charAt(1) - '0';
        this.body = line.substring(2);
    }

    /**
     * encode converts a Message object to a String
     */
    public String encode()
    {
        String ret = "";
        ret = ret + (char)(id + '0');
        ret = ret + (char)(meta + '0');
        ret = ret + body;
        return ret;
    }

    /**
     * makeJoin creates a Join Message String
     */
    public static String makeJoin(String name)
    {
        String ret = "";
        ret = ret + '1';
        ret = ret + '0';
        ret = ret + name;
        return ret;
    }

    /**
     * makeDisconnect creates a Disconnect Message String
     */
    public static String makeDisconnect(int disconnectReason,String name)
    {
        String ret = "";
        ret = ret + '2';
        ret = ret + (char)(disconnectReason + '0');
        ret = ret + name;
        return ret;
    }

    /**
     * makeUserMessage creates a User-Message String
     */
    public static String makeUserMessage(String msg)
    {
        String ret = "";
        ret = ret + '3';
        ret = ret + '0';
        ret = ret + msg;
        return ret;
    }

    /**
     * makeKeepAlive creates a Keepalive String
     */
    public static String makeKeepAlive()
    {
        String ret = "";
        ret = ret + '4';
        ret = ret + '0';
        return ret;
    }

    /**
     * makeServerReply creates a Server-Reply String
     */
    public static String makeServerReply(String msg)
    {
        String ret = "";
        ret = ret + '5';
        ret = ret + '0';
        ret = ret + msg;
        return ret;
    }



}