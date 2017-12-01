package starofstars;

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
	
	public CCSSwitch(int port, int startingPort) {
		this.port = port;	
		CCSSwitch.startingPort = startingPort;
		msg("Preparing CCSSwitch...");
	}

	public void run() {
		msg("Started.");
		
		CCSListenerThread listen = new CCSListenerThread(this.port, this);
		listen.start();
		
		msg("Running...");
		
		Frame fr = null;
		
		while(true) {
			
			if(frameList.size() != 0) {
				
				fr = frameList.get(0);
				
				String[] tmp = fr.getDst().split(",");
				
				int dstSwitch = Integer.parseInt(tmp[0].substring(1));
				int dstNode = Integer.parseInt(tmp[1].substring(0, tmp[1].length() - 1));
				
				//msg("Reciev " + fr.toString());
				
				int sendTo = switchingTable.get(dstSwitch);
				
				//msg("WILL SEND TO: " + Integer.toString(sendTo));
				
				sendToSwitch s = new sendToSwitch(sendTo, fr);
				s.start();
				
				frameList.remove(fr);
			}
			else {
				msg("no frames to process waiting...");
				try {
					Thread.sleep(800);
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

	private void msg (String input) {
		System.out.println("\tCCSSwitch: " +  input);

	}
	
	private static void msg_static (String input) {
		System.out.println("\tCCSSwitch: " +  input);

	}
	
	public void addFrame(Frame fr) {
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
	
	public void run() {
		
		Socket nodeSocket = null;
		
		try {
			nodeSocket = new Socket();
			nodeSocket.connect(new InetSocketAddress("localhost", port), 30000); //wait a timeout then it will close itself
			PrintWriter nodeWriter = new PrintWriter(nodeSocket.getOutputStream(), true);	
			msg(fr.toString());
			nodeWriter.println(fr.toString());		
			nodeWriter.println("TERMINATE");
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}
	
	private void msg (String input) {
		System.out.println("sendToSwitch: " + input);
	}
}
