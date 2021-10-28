import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler extends Thread {
    DataInputStream in;
    DataOutputStream out;
    Socket socket;
    Listener listener;
    Message message;
    NodeID Identifier;

    public ClientHandler(Socket socket, Listener listener, NodeID Identifier) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.listener = listener;
        this.Identifier = Identifier;
    }

    @Override
    public void run(){
        while(true){
            try{
                byte message[] = new byte[1000];
                int totalSize = in.read(message);
                if(totalSize > 0 ){
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    output.write(message, 0, totalSize);
                    listener.receive(toMessageType(output.toByteArray()));
                    System.out.println("........Server received the Message........");
                }
            } catch (SocketException e) {
                System.out.println("........Connection Broken........");
                listener.broken(Identifier);
                e.printStackTrace();
                break;
            } catch (IOException | ClassNotFoundException ae){
                ae.printStackTrace();
            }
        }
    }

    public Message toMessageType(byte[] m) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(m));
        Message message = (Message) in.readObject();
        in.close();
        return message;
    }
}
