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
    }//Message constructor

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
    }//encode

    /**
     * makeJoin creates a Join Message String
     * ID = 1
     */
    public static String makeJoin(String name)
    {
        String ret = "";
        ret = ret + '1';
        ret = ret + '0';
        ret = ret + name;
        return ret;
    }//makeJoin

    /**
     * makeDisconnect creates a Disconnect Message String
     * ID = 2
     */
    public static String makeDisconnect(int disconnectReason,String name)
    {
        String ret = "";
        ret = ret + '2';
        ret = ret + (char)(disconnectReason + '0');
        ret = ret + name;
        return ret;
    }//makeDisconnect

    /**
     * makeUserMessage creates a User-Message String
     * ID = 3
     */
    public static String makeUserMessage(String msg)
    {
        String ret = "";
        ret = ret + '3';
        ret = ret + '0';
        ret = ret + msg;
        return ret;
    }//makeUserMessage

    /**
     * makeKeepAlive creates a Keepalive String
     * ID = 4
     */
    public static String makeKeepAlive()
    {
        String ret = "";
        ret = ret + '4';
        ret = ret + '0';
        return ret;
    }//makeKeepAlive

    /**
     * makeServerReply creates a Server-Reply String
     * ID = 5
     */
    public static String makeServerReply(String msg)
    {
        String ret = "";
        ret = ret + '5';
        ret = ret + '0';
        ret = ret + msg;
        return ret;
    }//makeServerReply



}//Message