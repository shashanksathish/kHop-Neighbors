import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class Application implements Listener
{
	NodeID id;
	Node myNode;
	String config;
	ArrayList<ArrayList<Integer>> kHop;
	HashSet duplicate;
	boolean terminated, nextTerminate, change;
	int totalNodes, terminateCount, round, currNodeCount, nextNodeCount ;


	Application(NodeID id, String config){
		this.id = id;
		this.config = config;
		terminateCount = 0;
		nextTerminate = false;
		terminated = false;
		change = false;
		round = currNodeCount = nextNodeCount = 0;
	}

	public void run(){
		Node node = new Node(id, config, this);
		this.totalNodes = node.numNodes;
		this.kHop = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < this.totalNodes; i++){
			if(i == this.id.getID()){
				kHop.put(i, 0);
			}else{
				kHop.put(i, -1);
			}
		}

	}

	@Override
	public synchronized void receive(Message message) {
		//Extract payload from message
		Payload p = Payload.getPayload(message.data);

		if (!terminated) {
			// Messagetype 1 is terminate
			if (p.messageType == 1) {
				terminateCount += 1;
				nextTerminate = true;
			}

			if (p.messageType == 2) {
				for (Integer pNeighbors : p.payloadNeighbors) {
					if (!duplicate.contains(pNeighbors)) {
						change = true;
						if (kHop.size() < round + 1) {
							kHop.add(new ArrayList<Integer>());
						}
						kHop.get(round).add(pNeighbors);
						duplicate.add(pNeighbors);
					}
				}
				currNodeCount++;
			} else if (p.messageType == 3 || p.messageType == 1) {
				nextNodeCount += 1;
				if (nextNodeCount == currNodeCount) {
					Payload p1 = Payload.getPayload(message.data);
					if (!terminated) {
						p1.payloadNeighbors = kHop.get(round - 1);
						System.out.println("Sending message to the neighbors " + p1.payloadNeighbors);
						myNode.sendToAll(p1);
					}
					nextNodeCount = 0;
				}
			}
			// currentphasereceived == nextNodecount?
			if (nextNodeCount + terminateCount == currNodeCount) {
				if (!change) {
					//Terminate
					terminated = true;
				} else {
					System.out.println("kHopNeighbors round " + round + "; neighbors " + kHop.get(round));
				}
				if (terminated && !change) {
					sendTerminate();//Terminate message also acts like okay message. It's like okay and terminate
				} else {
					sendOkay();
				}
				round++;
				nextNodeCount = 0;//reset
				change = false;
			}
		}
	}

	@Override
	public void broken(NodeID neighbor) {

	}

	void sendOkay(){
		System.out.println("Sending Okay");
		Payload okayMessage = new Payload(2);
		Message m = new Message(id, okayMessage.toBytes());
		myNode.sendToAll(m);
	}

	void sendTerminate(){
		terminated = true;
		System.out.println("Sending Terminate");
		Payload okayMessage = new Payload(1);
		Message m = new Message(id, okayMessage.toBytes());
		myNode.sendToAll(m);
	}
}
