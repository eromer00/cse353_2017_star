package proj2_take2;

import java.io.*;
import java.net.*;
import java.util.*;

public class NodeHelper2017 extends Thread{

	public Node2017 ref_node;
	public int node_num;
	public int port_number;
	
	public ServerSocket send_out;
	public Socket reciever;
	
	public PrintWriter pt;
	public BufferedReader br;
	
	File in_file;
	String temp;
	int sleep_duration = 500;
	
	public void run() {
		List<Frame> outdata = new ArrayList<Frame>();
		
		int dest;
		String data;
		//read in the data from corresponding node txt
		try {
			in_file = new File("./nodes/node" + node_num + ".txt");
			br = new BufferedReader(new FileReader(in_file));
			
			while((temp = br.readLine()) != null) {
				String[] tmp = temp.split(":");
				dest = Integer.parseInt(tmp[0].toString());
				data = temp.substring(Integer.toString(dest).length()+1);
				
				Frame fr = new Frame(node_num, dest, data);
				//System.out.println("SRC: " + fr.getSrc() +"\nDEST: " + fr.getDest() + "\nDATA: " + fr.getData() +"\nBIN: " + fr.toBinaryString());
				outdata.add(fr); //prepare the frame itself this time
			}
		}catch(Throwable e) {
			System.out.println("ERROR: file read in node#" + node_num +" : " + e.toString());
		}
		
		//this all runs in a loop from main
    	int numofelements = outdata.size(); //# of frames to send
    	//remember this is outdata is global within this class
    	
    	SendToSwitch(numofelements, outdata); //send frames to switch and let it worry about where they go
    	//System.out.println("SENDTOSWITCH Done");
    	send_out = AssignPort(); //get a port from the switch, also inform that this node is done sending
    	//System.out.println("ASSIGNPORT Done");
    	Recieve_Write(send_out); //Now the node can receive and write the data to the corresponding file
    	//System.out.println("RECIEVE_WRITE Done");
    }
    
    private void SendToSwitch(int numofelements, List<Frame> outdata) {
    	
    	PrintWriter pt;
    	Frame out_frame;
    	
    	while(true) { //infinite loop, until we break
    		try {
    			Socket send_out;
    			
    			send_out = new Socket(InetAddress.getLocalHost(), Node2017.switch_port); //send things to the switch
    			
    			pt = new PrintWriter(send_out.getOutputStream(), true);
    			
    			for(int k = 0; k < numofelements; k++) {
    				
    				out_frame = outdata.get(k); //get our binary string frame, that is converted already
    				
    				//Frame f = new Frame(out_data);
    				//System.out.println(a.toBinaryString());
                    
    				pt.println(out_frame.toBinaryString()); //the switch should handle dest, src, and stuff
    			}
				pt.println("terminate"); //so the switch knows when to stop
    			pt.close();
    			send_out.close();
    			break;
    			
    		}catch(Throwable e) {
    			try {
    				System.out.println("Waiting on the switch...");
    				Thread.sleep(sleep_duration);
    			}catch(InterruptedException err){
    				continue;
    			}
    		}
    		
    	}	
    }
   
    private ServerSocket AssignPort() {
    	
    	ServerSocket server;
    	while(true) {
	    	try {
	    		port_number = Switch.Port(node_num);
	    		//setPortnum(Switch.port); //This is dependent on the port static method in switch class, it breaks things actually
				server = new ServerSocket(port_number);
				ref_node.Sending_Done = true;
				break;
			} catch (Throwable e) {
				System.out.println("ERROR, CANT START SERVER WITH PORT: " + port_number + "\n" + e);
			}
    	}   	
		return server;
    }

    private void Recieve_Write(ServerSocket server) {
    	 
    	//keep going until done
    	while(true) {
    		if(ref_node.Terminate) {
    			return;
    		}
    		else {
    			try {
    				reciever = server.accept();
    				System.out.println("NODE: " + node_num + " accepted on port: " + port_number);
    				Frame fr = new Frame(new BufferedReader
    						(new InputStreamReader(reciever.getInputStream())).readLine());
    				try {
    					//Allow for file appending
    					File output = new File("./nodes/output/node" + fr.getDest() + "output.txt"); 
    					FileWriter filewrite = new FileWriter("./nodes/output/" + output.getName(), true);
    					BufferedWriter writer = new BufferedWriter(filewrite);
    					
    					//Write the frame data built from binary string in the requested format
    					writer.write(fr.getSrc() + ":" + fr.getData());
    					writer.newLine();
    					
    					writer.close();
    					filewrite.close();
    					
    					System.out.println("Complete Write: node" + fr.getDest() + "output.txt");

    				}catch(Exception x) {
    					System.out.println("ERROR: " + x);
    				}
    				
        		}catch (Throwable e){
        			if(ref_node.Terminate) {
        				return;
        			}
        			else {
        				System.out.println("Waiting on Switch...\n");
        				try {
        					Thread.sleep(500);
        				}catch(InterruptedException err){
        					
        				}
        			}
        		}
    		}
    		
    	}
    }
}
