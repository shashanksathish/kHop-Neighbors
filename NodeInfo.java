
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class NodeInfo {

	int numOfNodes;
	boolean ConnDone;
	int[][] adjMtx;	
	int ClientConnectionCount[];
	int id;
	ArrayList<Integer> neighbors;
	HashMap<Integer,hostsPorts> nodeInfo;
	ArrayList<Socket> channels;
		
		
	//ArrayList which holds the total processes(nodes) 
	ArrayList<hostsPorts> nodeVal;

	public NodeInfo() {
		nodeVal = new ArrayList<hostsPorts>();	
		nodeInfo = new HashMap<Integer,hostsPorts>();
		neighbors = new ArrayList<>();
		//channels= new HashMap<Integer,Socket>();
		channels = new ArrayList<Socket>();
		ConnDone = false;
	}

}


