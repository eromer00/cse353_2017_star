package proj2_take2;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.locks.*;


public class Switch extends Thread{
    public static int port = 49154;
    public int number_of_nodes = 0;
    public static ArrayList<Integer> switchingTable;
    public static ArrayList<Frame> frames;
    public static Boolean terminate = false;
    public static int sleep_duration = 200;
    public static ReentrantLock lock = new ReentrantLock();
    public static boolean Terminate = false;
    public int mainPort = 0;

    public Switch(int mainPort, int number_of_node) {
    	this.mainPort = mainPort;
    	this.number_of_nodes = number_of_node;
    	frames = new ArrayList<Frame>();
    }
    
    public static int Port(int node_num) {
    	
    	int alloced_port = 0;
    	
    	lock.lock();
    	
    	//Switching Table Logic
    	try {
    		//if it's not there, create it
    		if(switchingTable == null) {
    			switchingTable = new ArrayList<Integer>();
    			for(int i = 0; i < 300; i++) {
    				//System.out.println("ADD");
    				switchingTable.add(-1);
    			}
    		}
    		alloced_port = Switch.port;
    		Switch.port++;
    		switchingTable.set(node_num, alloced_port);
    	} finally {
    		lock.unlock();
    	}
    	return alloced_port;
    }
    
    public void Terminate_Switch() {
    	Terminate = true;
    }
    
    public void run() {
    	ListeningThread listen = new ListeningThread(mainPort);
		listen.start();

		Frame frame = null;
		try {
			while(true) {
				if (terminate) {
					return;
				}
				if (frames.size() == 0) {
					Thread.sleep(sleep_duration);
					continue;
				}
				frame = frames.get(frames.size()-1);
				if (switchingTable == null) {
					buildSwitchTable();
				}
				int nodePort = -1;
				if (frame != null && switchingTable.get(frame.dest) != null) {
					nodePort = switchingTable.get(frame.dest);
				}
					
				if (nodePort == -1) {
					Collections.rotate(frames, 1);
					if (frame != null) 
						System.out.println("Unknown port location: node "+frame.dest);
					else {
						frames.removeAll(Collections.singleton(null));
					}
					Thread.sleep(sleep_duration);
					continue;
				}

				Socket sock;
				try {
					sock = new Socket(InetAddress.getLocalHost(), nodePort); 
				} catch (Throwable e) {
					Collections.swap(frames, 0, frames.size()-1);
					Thread.sleep(sleep_duration);
					continue;
				}

				PrintWriter pt = new PrintWriter(sock.getOutputStream(), true);
				pt.println(frame.toBinaryString());
				pt.close();
				sock.close();
				frames.remove(frame);
			}
		} catch (Throwable e) {
			System.out.println("ERROR: switch, src:" + frame.src + "dest:" + frame.dest);
		}
    }
    
    private static void buildSwitchTable() {
    	switchingTable = new ArrayList<Integer>();
		for(int i = 0; i < 300; i++) {
			switchingTable.add(-1);
		}
    }
}


