package main;

import process.CandidateProcess;
import process.OrdinaryProcess;
import process.ProcessIntf;

public class maintest{
	
	static final int expectedNetworkSize = 5;
	
	public static void main(String args[]) throws Exception{
		
		int registryPort = Integer.parseInt(args[0]);
		int nodePort = Integer.parseInt(args[1]);
		boolean isCandidate = (Integer.parseInt(args[2])!=0);
		ProcessIntf newnode;
		if(isCandidate){
			newnode = new CandidateProcess(nodePort,registryPort,expectedNetworkSize);
		}else{
			newnode = new OrdinaryProcess(nodePort,registryPort,expectedNetworkSize);
		}
		System.out.println("Process["+newnode.getId()+"] is registered");
		newnode.notifyOthers();
				
	}
}