//package starofstars;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class CASSwitch implements Runnable {
	
	public ArrayList<Integer> switchingTable;
	
	public int Port = 0;
	public int startingPort;
	public int identificationNumber;
	private int nodes_per_switch;
	public ArrayList<Frame> frameList = new ArrayList<Frame>();
	
	public Socket CCSSocket;
	
	private ReentrantLock lock = new ReentrantLock();

	public boolean Terminate = false;
	
	public int tracker = 0;
	
	public CASSwitch(int identification, int nodes_per_switch, int startingPort) {
		this.identificationNumber = identification;
		this.nodes_per_switch = nodes_per_switch;
		this.startingPort = startingPort;
		this.Port = CCSSwitch.allocPort(this.identificationNumber); //statically obtain a port from the CSSSwitch
		//msg("Setting up Switch...");
	}

	@SuppressWarnings("unused")
	@Override //this runs when thread executor is ran
	public void run() {
		
		PrintWriter CCSWriter = null;
		PrintWriter nodeWriter = null;
		
		//msg("I was given port: " + this.Port);
		CASListenerThread Listener = new CASListenerThread(this.Port, this);
		Listener.start();
		
		
		//Establish a connection to the CCSSwitch
		try {
			//msg("Attempting to connect to CCSSwitch...");
			this.CCSSocket = new Socket("localhost", CCSSwitch.port); //connection to CCSSwitch
			//msg("Successful connection.");
			CCSWriter = new PrintWriter(CCSSocket.getOutputStream(), true);	
		} catch (Throwable e) {
			msg("ERROR CCSSOCKET: --> " + e.toString());
			e.printStackTrace();
		}
		
		Frame fr = null;
		
		while(true) {
			
			//just in case
			fr = null;
			//nodeWriter = null;
			
			try {			
				//System.out.println("still going...");
				if(frameList.size() != 0) {
					//msg("Ive got frames to process, idk how yet...");
				
					fr = frameList.get(0); //this is ok since it acts like a queue
					
					msg("Recieved Frame: " + fr.toString());
			
					//String[] tmp = fr.getDest().split(",");


					
					int dstSwitch = fr.getSdst();
					int dstNode = fr.getDst();
					
					String check = "(" + Integer.toString(dstSwitch) + "," + Integer.toString(dstNode) + ") :local";
					
					//msg("(" + dstSwitch + "," + dstNode + ")");
					
					//SEND TO CCS SWITCH CASE
					if(dstSwitch != this.identificationNumber) {
						//msg("This frame is not mean't for here. I'll send it off when I know how");
						//use CCSSocket to send it to CCSSwitch
						if(CCSWriter == null) {
							msg("something is wrong with the CCSWriter.");
						}
						else {
							if(fr == null) {
								msg("Frame got corrupted somehow");
							}
							else { //send it to the CCSSwitch if things are good
								CCSWriter.println(fr.getBinaryString());
								CCSWriter.println("TERMINATE");
							}
						}
					}
					
					//SEND TO A NODE CASE
					else if(dstSwitch == this.identificationNumber) {
						
						//firewall check
						if(Main.getRules().contains(check) && Main.isFirewallEnabled) {
							//String[] tmp2 = fr.getSrce().split(",");

							int srcSwitch = fr.getSSrc();
							int srcNode = fr.getSrc();
							//String internalCheck = "(" + Integer.toString(srcSwitch) + "," + Integer.toString(srcNode) + ")";
							//if(srcSwitch != this.identificationNumber) {
								msg("Firewall --> this node: " + fr.getDst() + " is only accepting local traffic");
								msg("Firewall --> draining the frame...");

								Frame fw = new Frame("1", String.valueOf(srcSwitch), "2", String.valueOf(srcSwitch), "10");

								frameList.remove(fr);
								frameList.add(fw);
								continue;
							//}
						}
						//if it passes then it can be sent to the node
						
						//msg("This frame is meant for me, I'll just send it to the node in my network");
						int send = this.switchingTable.get(dstNode);

						sendToNode cs = new sendToNode(send, fr);
						cs.start();
							
					}
					
					//WHEN DONE REMOVE FRAME FROM QUEUE
					frameList.remove(fr);
				}
				else {
					msg("no frames to process waiting...");
					Thread.sleep(900);
					/*if(this.tracker > Main.numOfLines) {
						this.Terminate = true;
						this.CCSSocket.close();
						return;
					}
					tracker++;*/
				}			
			}catch(Throwable e) {
				msg("ERROR --> " + e.toString());
			}
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public int allocPort(int nodeIdentification) {
		lock.lock();
		int give = 0;
		try {
			if (switchingTable == null) {
				switchingTable = new ArrayList<Integer>();
				for (int i = 0; i < 300; i++) {
					switchingTable.add(-1);
				}
			}
			give = this.startingPort;
			this.startingPort++;
			msg("Giving port: " + give + " to node# "+ nodeIdentification);
			switchingTable.set(nodeIdentification, give);
		} finally {
			lock.unlock();
		}
		return give;
	}
	
	//make reporting soooooo much nicer
	private void msg (String input) {
		System.out.println("\tCASSwitch #" + this.identificationNumber + ": " + input);
	}
	
	public int getPort() {
		return this.Port;
	}
	
	public void addFrame(Frame fr) {
		//msg("adding frame...");
		this.frameList.add(fr);
	}

}

class sendToNode extends Thread{
	
	public int port;
	public Frame fr;
	
	sendToNode(int port, Frame fr) {
		this.port = port;
		this.fr = fr;
	}
	
	public synchronized void run() {
		
		Socket nodeSocket = null;
		
		try {
			nodeSocket = new Socket();
			nodeSocket.connect(new InetSocketAddress("localhost", port), 30000); //wait a timeout then it will close itself
			PrintWriter nodeWriter = new PrintWriter(nodeSocket.getOutputStream(), true);	
			msg(fr.getBinaryString());
			nodeWriter.println(fr.getBinaryString());
			nodeWriter.println("TERMINATE");
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}
	
	private synchronized void msg (String input) {
		System.out.println("\tsendToCSS: " + input);
	}
	
}
