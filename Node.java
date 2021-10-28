import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

//Object to reprsents a node in the distributed system
class Node
{
	private NodeID identifier;
	private String configFile;
	private NodeID[] neighbor;
	private Server s;
	public int numNodes;
	private Listener listener;
	private HashMap<NodeID, Client> theLinks = new HashMap<>(); //new change
	ArrayList<Integer> nodeNeighbor = new ArrayList<>();

	// constructor
	public Node(NodeID identifier, String configFile, Listener listener)
	{
		this.identifier = identifier;
		this.configFile = configFile;
		this.listener = listener;
		NodeInfo val =  readConfigFile(identifier,configFile);
		this.numNodes = val.numOfNodes;

		//Create Neighboring Socket Connections
		for(int i = 0; i < neighbor.length; i++){
			String HostId = val.nodeVal.get(neighbor[i].getID()).hostId;
			int PortId = val.nodeVal.get(neighbor[i].getID()).port;
			System.out.println(".........Creating Neighboring Connections.........");
			Client t = new Client(HostId, PortId, neighbor[i]);
			t.start();
			theLinks.put(neighbor[i],t);
		}

		// Establishing a Server Connection
		System.out.println(val.nodeVal.get(identifier.getID()).port);
		try {
			s = new Server(val.nodeVal.get(identifier.getID()).port,listener,neighbor.length, val);
			// Server s = new Server(50006,listener);
			System.out.println(val.nodeVal.get(identifier.getID()).port + "Server port number" + "Node file");
			s.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// methods
	public NodeID[] getNeighbors()
	{
		return neighbor;
	}

	public void send(Message message, NodeID destination)
	{
		theLinks.get(destination).addMessages(message);
	}

	public void sendToAll(Message message)
	{
		for (Map.Entry<NodeID, Client> set: theLinks.entrySet()){
			set.getValue().addMessages(message);
		}
	}
	
	public void tearDown()
	{
		for (Map.Entry<NodeID, Client> set: theLinks.entrySet()){
			Socket socket = set.getValue().getClientSocket();
			while(!socket.isClosed()){
				try{
					socket.close();
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		theLinks.clear();
		System.out.println("................Done with the TearDown................");
	}

	public NodeInfo readConfigFile(NodeID id, String configFile){
		NodeInfo file = new NodeInfo();
		file.id = id.getID();
		int nodeCount = 0;
		int next = 0;
		int curNode = 0;
		String fileName = System.getProperty("user.dir") + "/" + configFile;
		String line = null;
		try {
			BufferedReader br=new BufferedReader(new FileReader(fileName));

			while((line = br.readLine()) != null) {

				if(line.length() == 0 || line.startsWith("#"))
					continue;

				String[] config_input;
				if(line.contains("#")){
					String[] config_input_comment = line.split("#.*$");
					config_input = config_input_comment[0].split("\\s+");
				}
				else {
					config_input = line.split("\\s+");
				}



				if(next==0 && config_input.length == 1){

					file.numOfNodes= Integer.parseInt(config_input[0]);
					file.adjMtx = new int[file.numOfNodes][file.numOfNodes];//init adj matrix
					file.ClientConnectionCount= new int[file.numOfNodes];
					next++;
				}
				else if(next == 1 && nodeCount < file.numOfNodes) {

					hostsPorts h = new hostsPorts(Integer.parseInt(config_input[0]),config_input[1],Integer.parseInt(config_input[2]));
					file.nodeVal.add(h);
					nodeCount++;
					if(nodeCount == file.numOfNodes){
						next = 2;
					}
				}
				else if(next == 2) {
					for(String i : config_input){
						if(curNode != Integer.parseInt(i)) {
							file.adjMtx[curNode][Integer.parseInt(i)] = 1; //Initial Connection
							file.adjMtx[Integer.parseInt(i)][curNode] = 1; // Ulta connection
							if(file.id==curNode) {

								file.ClientConnectionCount[file.id]= config_input.length-1;}

						}
					}
					curNode++;
				}

			}

			br.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("Unable to open file '" + fileName + "'");
		}
		catch(IOException e) {
			System.out.println("Error reading file '" + fileName + "'");
		}

		for(int i = 0; i < file.numOfNodes; i++){
			if(file.adjMtx[identifier.getID()][i] == 1){
				nodeNeighbor.add(i);
			}
		}
		neighbor = new NodeID[nodeNeighbor.size()];
		for(int i=0; i<nodeNeighbor.size(); i++){
			neighbor[i] = new NodeID(nodeNeighbor.get(i));
		}
		return file;
	}
}
