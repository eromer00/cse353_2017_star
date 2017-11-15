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
    public static int sleep_duration = 500;
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
    			buildSwitchTable();
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
    	ListeningThread listener = new ListeningThread(mainPort);
    	listener.start();
    	
    	Frame fr = null;
    	
    	try {
    		while(true) {
    			if(Terminate) {
    				return;
    			}
    		
	    		if(frames.size() == 0) {
	    			System.out.println("No Frames to process...");
	    			Thread.sleep(sleep_duration);
	    		}
	    		fr = frames.get(frames.size()- 1);
	    		
	    		if(switchingTable == null) {
	    			buildSwitchTable();
	    		}
	    		
	    		int nodePort = -1;
	    		if(fr != null && switchingTable.get(fr.dest) != null) {
	    			nodePort = switchingTable.get(fr.dest);
	    		}
	    		
	    		if(nodePort == -1) {
	    			Collections.rotate(frames, 1);
	    			if(fr == null) {
	    				frames.removeAll(Collections.singleton(null));
	    			}
	    			else {
	    				System.out.println(fr.dest + ":Unknown port location");
	    			}
	    			Thread.sleep(sleep_duration);
	    			continue;
	    		}
	    		
	    		Socket sock;
	    		
	    		try {
	    			sock = new Socket(InetAddress.getLocalHost(), nodePort);
	    		}catch(Throwable e) {
	    			Collections.swap(frames, 0, frames.size() - 1);
	    			Thread.sleep(sleep_duration);
	    			continue;
	    		}
	    		
	    		PrintWriter pt = new PrintWriter(sock.getOutputStream(), true);
	    		pt.println(fr.toBinaryString());
	    		pt.close();
	    		sock.close();
	    		frames.remove(fr);
    		}
    	}catch(Throwable e) {
    		System.out.println("Switch error:" + e);
    	}	
    }
    
    private static void buildSwitchTable() {
    	switchingTable = new ArrayList<Integer>();
		for(int i = 0; i < 300; i++) {
			switchingTable.add(-1);
		}
    }
}
