package process;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class OrdinaryProcess extends UnicastRemoteObject implements ProcessIntf{

	private static final long serialVersionUID = 1L;
	int id;
	int level;
	int ownerId;
	ProcessIntf owner;
	ProcessIntf potentialOwner;
	
	private int expectedNetworkSize;
	private Registry registry;
	int nodesJoined;
	
	public OrdinaryProcess(int i, int registryPort, int expectedNetworkSize) throws RemoteException {
		this.id = i;
		this.level = -1;
		this.ownerId = -1;
		this.owner = null;
		this.potentialOwner = null;
		
		this.expectedNetworkSize = expectedNetworkSize;
		this.registry = LocateRegistry.getRegistry(registryPort);		
			
		try {
			this.registry.bind(Integer.toString(this.id), this);
		} catch (AlreadyBoundException e) {
			System.out.println("Daboom: " + e);
		}
		
	}

	@Override
	public void send(int i, int lv, ProcessIntf p) throws RemoteException {
		p.recieve(i, lv, this);		
	}

	@Override
	public void recieve(int i, int lv, ProcessIntf p) throws RemoteException {
		if(lv<this.level || (lv==this.level && i<this.ownerId)){
			
		}else if(lv>this.level || (lv==this.level&&i>this.ownerId)){
			
			this.potentialOwner = p;
			this.level = lv;
			this.ownerId = i;
			if(this.owner == null){
				this.owner = this.potentialOwner;
				System.out.println("Process["+this.id+"] is owned by Process["+p.getId()+"]");}
			send(i,lv,this.owner);
		}else if(lv==this.level && i==this.id){
			this.owner = this.potentialOwner;
			send(i,lv,this.owner);
		}		
	}

	public void notifyOthers() {		
		try
		{
			// Inform the other nodes that you have joined the network by calling their newNodeJoined remote method.
			String[] connectedNodes = this.registry.list();
			//can only iterate from 1->4 but not  4->1
			for (String nodeName : connectedNodes) {
				this.nodesJoined++;			
				
				ProcessIntf remoteNode = (ProcessIntf) this.registry.lookup(nodeName);	
				
				remoteNode.newNodeJoined(this);
				System.out.println("Notified node: " + nodeName);
				System.out.println(this.nodesJoined);
			}
		} catch (Exception e) {
			System.out.println("Kaboom: " + e);
		}
	}
	public void newNodeJoined(ProcessIntf p) throws RemoteException, NotBoundException, InterruptedException {
		// Increase the counter of the nodes already in the network.
		if( !(p.getId()==this.id) ){
			this.nodesJoined++;
		}
		// Start the algorithm if enough nodes have joined the network.
		if ( nodesJoined == expectedNetworkSize){
			System.out.println("Ordinary Process["+this.id+"] Ready");
		}
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
}
