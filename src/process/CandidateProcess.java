package process;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class CandidateProcess extends UnicastRemoteObject implements ProcessIntf{
	
	private static final long serialVersionUID = 1L;
	
	int id;
	int level;
	boolean killed;
	boolean elected;
	ArrayList<ProcessIntf> linkset; 
	
	private int expectedNetworkSize;
	private Registry registry;
	int nodesJoined;
	
	PrintWriter process_info;
	
	public CandidateProcess(int i, int registryPort, int expectedNetworkSize) throws RemoteException, AlreadyBoundException{
		this.id = i;
		this.level = -1;
		this.killed = false;
		this.elected = false;
		this.linkset = new ArrayList<ProcessIntf>();
		
		this.expectedNetworkSize = expectedNetworkSize;
		this.registry = LocateRegistry.getRegistry(registryPort);		
			
		try {
			this.registry.bind(Integer.toString(this.id), this);
		} catch (AlreadyBoundException e) {
			System.out.println("Daboom: " + e);
		}
		
		try{
			File file= new File("process_info_"+id+".txt");
			process_info = new PrintWriter((new FileWriter(file, true)));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void send(int i, int lv, ProcessIntf p) throws RemoteException{
		p.recieve(i, lv, this);	
	}
	
	public void sendOne() throws RemoteException{
		if(!this.linkset.isEmpty()){
			Random rd = new Random();
			ProcessIntf tmp = this.linkset.get(rd.nextInt(linkset.size()));
			System.out.println("Process[" + this.id + "] sends message (Level " + this.level + ", ID " + this.id + ") to Process[" + tmp.getId()+"]");
			process_info.println("Process[" + this.id + "] sends message (Level " + this.level + ", ID " + this.id + ") to Process[" + tmp.getId()+"]");
			send(this.id,this.level,tmp);
		}else{
			if(!this.killed){
				this.elected = true;
				System.out.println("Process ["+this.id+"] has been elected");	
				process_info.println("Process ["+this.id+"] has been elected");
				process_info.flush();
			}
		}
	}

	@Override
	public void recieve(int i, int lv, ProcessIntf p) throws RemoteException {
		System.out.println("Process [" + this.id + "] Level [" + this.level + "] has received message: Level " + lv + ", ID " + i);
		if(i==this.id && !this.killed){//Receive ack
			this.level++;
			this.linkset.remove(p);
			sendOne();
		}else{			
			if(lv<this.level || (lv==this.level && i<=this.id)){//Receive an id that is smaller than itself, block it
				System.out.println("Process [" + i + "] is blocked");
			}else{////Receive a bigger id

				this.killed = true;
				System.out.println("Process [" + this.id + "] is now killed");
				process_info.println("Process [" + this.id + "] is now killed");
				process_info.flush();

				send(i, lv, p);
			}
		}		
	}
	
	public void notifyOthers()throws RemoteException {		
		try
		{
			// Inform the other nodes that you have joined the network by calling their newNodeJoined remote method.
			String[] connectedNodes = this.registry.list();
			//can only iterate from 1->4 but not  4->1
			for (String nodeName : connectedNodes) {
				this.nodesJoined++;			
				
				ProcessIntf remoteNode = (ProcessIntf) this.registry.lookup(nodeName);	
				if(!nodeName.equals(Integer.toString(this.getId())))
					this.linkset.add(remoteNode);
				
				remoteNode.newNodeJoined(this);
				System.out.println("Notified node: " + nodeName);
				System.out.println(this.nodesJoined);				
			}
			if ( nodesJoined == expectedNetworkSize){
				startAlgorithm();
			}
		} catch (Exception e) {
			System.out.println("Kaboom: " + e);
		}

	}
	public void newNodeJoined(ProcessIntf p) throws RemoteException, NotBoundException, InterruptedException {
		// Increase the counter of the nodes already in the network.
		if( !(p.getId()==this.id) ){
			this.nodesJoined++;
			this.linkset.add(p);
		}
		// Start the algorithm if enough nodes have joined the network.
		if ( nodesJoined == expectedNetworkSize){
			startAlgorithm();
		}
			
	}
	
	public void startAlgorithm() throws RemoteException, InterruptedException{
		sendOne();
	}
	public int getId() throws RemoteException{
		return this.id;
	}
	
}