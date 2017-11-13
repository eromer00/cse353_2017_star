import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

public class Switch extends Thread{
    public static int port = 50000;         //hand out ports at 50,000 (I'm confused, since the Main class instantiates the nodes, shouldn't it be handing on their port numbers, not the switch class?)
    public int serverPort;                  //port for serverSocket
    public static int nodes = 0;
    public static ArrayList<Integer> switchingTable;
    public static ArrayList<Frame> frames;
    public static Boolean terminate = false;
    public static int sleep = 100;
    public static ReentrantLock lock = new ReentrantLock();
    public static boolean Terminate = false;

    /*
     * Switch Constructor
     * @serverPort is the port the switch should listen to
     * @nodes
     */
    public Switch(int serverPort, int nodes) {
        this.serverPort = serverPort;
        this.nodes = nodes;
        frames = new ArrayList<Frame>();
    }

    /*
     * Close any sockets or pipes
     */
    public void close() {
        terminate = true;
    }

    /*
     * Track which Ports go to what Nodes
     * @nodeNum is the port the switch should listen to
     */
    public static int Port(int node) {
        lock.lock(); //help maintain thread synchronization
        int assignPortNum = 0;
        try {
            if (switchingTable == null) {
                switchingTable = new ArrayList<Integer>();
                for (int i = 0; i < 300; i++) {
                	switchingTable.add(-1);
                }
            }
            assignPortNum = Switch.port;
            Switch.port++;
            System.out.println("Port: " + assignPortNum + " goes to" + node);
            switchingTable.set(node, assignPortNum);
        } finally {
            lock.unlock();
        }
        return assignPortNum;
    }



     //Need to implement

    public void run() {

    /*From Frame Class
    Determines type of frame being constructed, used for creation/encryption frame
     * key:
     * 0 - regular data
     * 1 - ACK frame
     * 2 - flood frame
     * 3 - flood ACK frame
     * 4 - bad frame (used for error checking)*/
    	
    	ListenerThread listen = new ListenerThread(serverPort);
    	listen.start();
    	
    	
    	int nodeport = -1;
    	Frame frame = null;
    	try {
    		while(true) {
    			
    			nodeport = -1;
    			if(Terminate) {
    				return;
    			}
    			if(frames.size() == 0) {
    				System.out.println("Currently have no frames");
    				Thread.sleep(500);
    				continue;
    			}
    			
    			frame = frames.get(frames.size() - 1);
    			
    			if(switchingTable == null) {
    				switchingTable = new ArrayList<Integer>();
    				for(int k = 0; k < 300; k++) {
    					switchingTable.add(-1);
    				}
    			}
    			
    			if(switchingTable.get(frame.getDest()) != null && frame != null) {
    				nodeport = switchingTable.get(frame.getDest());
    			}
    			
    			if(nodeport == -1) {
    				java.util.Collections.rotate(frames, 1);
    				if(frame == null) {
    					frames.removeAll(java.util.Collections.singleton(null));
    				}
    				Thread.sleep(500);
    				continue;
    			}
    			
    			Socket sock;
    			try {
    				sock = new Socket(InetAddress.getLocalHost(), nodeport);
    			}catch(Throwable e) {
    				java.util.Collections.swap(frames, 0, frame.getSize()-1);
    				Thread.sleep(500);
    				continue;
    			}
    			
    			PrintWriter pt = new PrintWriter(sock.getOutputStream(), true);
    			pt.print(frame.toBinaryString());
    			pt.close();
    			sock.close();
    		}	
    		
    	}catch(Exception e) {
    		
    	}
    	
    }


    /*
     * Establish a connection to the switch (Why do we need a thread here?)
     */
    class ListenerThread extends Thread {
        public int serverPort;

        public ListenerThread(int serverPort) {
            this.serverPort = serverPort;
        }

        public void run() {
            try {
                System.out.println("Running");
                Socket socket;
                ServerSocket listener = new ServerSocket(serverPort);

                while(true) {
                    if (Switch.Terminate) 
                    	return;
                    System.out.println("Trying to establish a connection.");
                    socket = listener.accept();

                    System.out.println("Connection established.");
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );

                    while(true) {
                        String data = input.readLine();
                        
                        if (data.equals("terminate")) 
                        	break;

                        Frame frame = new Frame(data);
                        Switch.frames.add(frame);
                    }
                }
            } catch (Throwable e) {
                System.out.println("Warning:" + e);
            }
        }
    }
}
