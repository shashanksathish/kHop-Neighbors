import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server extends Thread{

    private Socket socket;
    private int portId;
    public ServerSocket SerCon;
    private int clientHandlerCount = 0;
    private int neighborCount;
    public Listener listener;
    public NodeInfo val;
    public Server(int portId, Listener listener, int neighborCount, NodeInfo val) throws IOException {
        this.socket = null;
        this.portId = portId;
        this.SerCon = new ServerSocket(portId);
        SerCon.setReuseAddress(true);
        this.listener = listener;
        this.val = val;
        this.neighborCount = neighborCount;
    }

    @Override
    public void run() {
        try {
            while (clientHandlerCount < neighborCount) {
                try {
                    socket = SerCon.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, listener, getClientID(socket));
                    clientHandler.start();
                    clientHandlerCount++;
                    System.out.println("...........Server Connected...........");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NodeID getClientID(Socket socket){
        NodeID value = null;
        for(int i = 0; i < val.numOfNodes; i++){
            if(socket.getInetAddress().getHostName().split("\\.")[0] == val.nodeVal.get(i).hostId){
                value = new NodeID (val.nodeVal.get(i).nodeId);
            }
        }
        return value;
    }
}
