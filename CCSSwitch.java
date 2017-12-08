//package starofstars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

//GLOBAL SWITCH
public class CCSSwitch extends Thread{

	public static int port;
	
	public static ReentrantLock lock = new ReentrantLock();
	public static ArrayList<Integer> switchingTable;
	
	public static int startingPort;

	public static boolean Terminate = false;

	public ArrayList<Frame> frameList = new ArrayList<Frame>();
	
	private int tracker = 0;
	private int timer = 0;
	
	private String firewallFile = "./firewall.txt";
	public static ArrayList<String> fireWallRules = new ArrayList<String>();
	
	public CCSSwitch(int port, int startingPort) {
		this.port = port;	
		CCSSwitch.startingPort = startingPort;
		msg("Preparing CCSSwitch...");
		
		//perpare firewall file
		if(Main.isFirewallEnabled) {
			prepFireWall();
		}
		else {
			msg("Firewall function is disabled.");
		}
		//else check if empty
	}
	
	private void prepFireWall() {
			
		File in_file = null;
		BufferedReader br = null;
		String temp = null;
		int switch_dest = 0, node_dest = 0; 
		String data = null;
		
		String switchValue = null;
		String nodeValue = null;
		String manage = null;

		try {	
			msg("Reading in firewall rules from file...");
			in_file = new File(this.firewallFile);
			br = new BufferedReader(new FileReader(in_file));
			
			while((temp = br.readLine()) != null) {
				switchValue = null;
				nodeValue = null;
				
				//maintained as string because of the possible * char
				//msg("firewall in: " + temp);
				String[] tmp = temp.split(",");
				String[] tmp2 = temp.split("_");
				
				switchValue = tmp2[0];
				
				String[] tmp3 = tmp2[1].split(",");
				
				nodeValue = tmp3[0];
				
				if(tmp[1] == null) {
					manage = "Invalid";
				}
				else {
					manage = tmp[1].substring(1, tmp[1].length());
				}
				
				data = "(" + switchValue + "," + nodeValue + ") :" + manage;
				//msg ("DATA: " + data);
				Main.globalRules.add(data);
			}
			
			for(int i = 0; i < Main.globalRules.size(); i++) {
				msg("Enforcing Rule: " + Main.globalRules.get(i));
			}
			
			br.close();
			
		}catch (Exception e) {
			msg("ERROR READING IN FILE --> " + e.toString());
			e.printStackTrace();
		}
		
	}

	public void run() {
		msg("Started.");
		
		CCSListenerThread listen = new CCSListenerThread(this.port, this);
		listen.start();
		
		msg("Running...");
		
		Frame fr = null;
		

		
		while(true) {
			timer++;
			if(frameList.size() != 0) {
					
				fr = frameList.get(0);
				msg("Recieved Frame: " + fr.toString());
				
				//String[] tmp = fr.getDest().split(",");
				
				int dstSwitch = fr.getSdst();
				int dstNode = fr.getDst();
				//(x,*) :local
				String check = "(" + Integer.toString(dstSwitch) + "," + "*) :local";
				
				
				
				if(Main.globalRules.contains(check) && Main.isFirewallEnabled) {
					msg("Firewall --> blocked traffic to CASSwitch #" + Integer.toString(dstSwitch) + " is enforced");
					msg("Firewall --> draining frame from queue...");
					frameList.remove(fr);
					continue;
				}	
				
				int sendTo = switchingTable.get(dstSwitch);
				
				//msg("WILL SEND TO: " + Integer.toString(sendTo));
				
				sendToSwitch s = new sendToSwitch(sendTo, fr);
				s.start();
				
				frameList.remove(fr);
			}
			else {
				msg("no frames to process waiting...");
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//if(this.tracker > Main.numOfLines * 2) {
				//	CCSSwitch.Terminate = true;
				//	return;
				//}
				//tracker++;*/
			}
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			msg("Shadow backup complete.");
			Main.Shadow = this;

			if(timer > 350) {
				this.Terminate = true;
				break;
			}
		}
		
	}
	
	public static int allocPort(int switchIdentification) {
		lock.lock();
		int give = 0;
		try {
			if (switchingTable == null) {
				switchingTable = new ArrayList<Integer>();
				for (int i = 0; i < 300; i++) switchingTable.add(-1);
			}
			give = CCSSwitch.startingPort;
			CCSSwitch.startingPort++;
			msg_static("Giving port: " + give + " to Switch# "+ switchIdentification); //so it can statically report the port given to switch
			switchingTable.set(switchIdentification, give);
		} finally {
			lock.unlock();
		}
		return give;
	}

	private synchronized void msg (String input) {
		System.out.println("CCSSwitch: " +  input);

	}
	
	private synchronized static void msg_static (String input) {
		System.out.println("CCSSwitch: " +  input);

	}
	
	public synchronized void addFrame(Frame fr) {
		frameList.add(fr);
	}
}

class sendToSwitch extends Thread {
	
	public int port;
	public Frame fr;
	
	sendToSwitch(int port, Frame fr) {
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
		System.out.println("sendToCASSwitch: " + input);
	}
}
