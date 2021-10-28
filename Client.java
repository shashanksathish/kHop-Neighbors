import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Client extends Thread {

    private Socket socket;
    private String hostName;
    private NodeID serverID;
    private Queue<Message> messageList = new LinkedList<Message>();// Since server is single threaded, we need to Queue the messages
    private int portId;
    boolean connDone;

    public Client(String hostName, int portId, NodeID serverID) {
        this.socket = null;
        this.hostName = hostName;
        this.serverID = serverID;
        this.portId = portId;
        this.connDone = false;
    }

    @Override
    public void run() {
        while (!connDone) {
            if (socket == null || socket.isClosed()) {
                try {
                    InetAddress ip_add = InetAddress.getByName(hostName);
                    socket = new Socket(ip_add, portId);
                } catch (Exception e) {
                    try {
                        System.out.println("Waiting for Connection to: " + hostName + " RETRYING!!");
                        Thread.sleep(1000);
                    } catch (Exception ea) {
                        ea.printStackTrace();
                        break;
                    }
                }
            }
            else{
                // Socket not null, there are some messages in the queue
                if(messageList.size() != 0){
                    try {
                        Message message = (Message) messageList.remove(); // Single client
                        socket.getOutputStream().write(toBytes(message));
                        System.out.println("............Client sent the Message............");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public Socket getClientSocket() {
        return this.socket;
    }

    public void addMessages(Message mess){
        messageList.add(mess);
    }

    public byte[] toBytes(Message mess) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(mess);
        o.flush();
        return b.toByteArray();
    }
}
