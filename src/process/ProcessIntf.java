package process;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProcessIntf extends Remote{
	
	public void send(int i, int lv, ProcessIntf p) throws RemoteException;
	public void recieve(int i, int lv, ProcessIntf p) throws RemoteException;
	public void newNodeJoined(ProcessIntf p) throws RemoteException, NotBoundException, InterruptedException;
	public void notifyOthers()throws RemoteException;
	public int getId() throws RemoteException;
}