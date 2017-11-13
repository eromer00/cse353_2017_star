import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NodeHelper2017 extends Thread {
	
	public Node2017 reference;
	
    public ServerSocket server;
    public Socket data_reciever;
    int nodenum, portnumber, serverport;
    private int sleep_duration = 500; //ms --> 1/2 sec
    File file;
 
    public void run() {
    	
    	 //READ IN STUFF
    	List<String> outdata = new ArrayList<String>();
        try {
        	System.out.println("NODE CURRENTLY: " + nodenum);
        	
        	file = new File("./nodes/node" + nodenum + ".txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s;

            //read in lines and convert to padded binary string
            while((s = br.readLine()) != null) {
                outdata.add(s);
            }
            br.close();
            
        } catch (Exception e) {
            System.out.println("Error setting up node " + nodenum + "\nERROR: " + e);
        }
           
        //actual_node.outdata = outdata;
        
    	
    	//this all runs in a loop from main
    	int numofelements = outdata.size(); //# of frames to send
    	//remember this is outdata is global within this class
    	
    	SendToSwitch(numofelements, outdata); //send frames to switch and let it worry about where they go
    	//System.out.println("SENDTOSWITCH Done");
    	server = AssignPort(); //get a port from the switch, also inform that this node is done sending
    	//System.out.println("ASSIGNPORT Done");
    	Recieve_Write(server); //Now the node can receive and write the data to the corresponding file
    	//System.out.println("RECIEVE_WRITE Done");
    }
    
    private void SendToSwitch(int numofelements, List<String> outdata) {
    	
    	PrintWriter pt;
    	String out_data;
    	
    	while(true) { //infinite loop, until we break
    		try {
    			Socket send_out;
    			
    			send_out = new Socket(InetAddress.getLocalHost(), Node2017.switchport); //send things to the switch
    			
    			pt = new PrintWriter(send_out.getOutputStream(), true);
    			
    			for(int k = 0; k < numofelements; k++) {
    				
    				out_data = outdata.get(k); //get our binary string frame, that is converted already
    				
    				String[] split = out_data.split(":");
                    Frame a = new Frame(nodenum, Integer.parseInt(split[0]), split[1]);

    				//Frame f = new Frame(out_data);
    				//System.out.println(a.toBinaryString());
                    
    				pt.println(a.toBinaryString()); //the switch should handle dest, src, and stuff
    			}
				pt.println("terminate");
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
	    		portnumber = Switch.Port(nodenum);
	    		//setPortnum(Switch.port); //This is dependent on the port static method in switch class, it breaks things actually
				server = new ServerSocket(portnumber);
				reference.Sending_Done = true;
				break;
			} catch (Throwable e) {
				System.out.println("ERROR, CANT START SERVER WITH PORT: " + portnumber + "\n" + e);
			}
    	}   	
		return server;
    }

    private void Recieve_Write(ServerSocket server) {
    	 
    	//keep going until done
    	while(true) {
    		if(reference.Terminate) {
    			return;
    		}
    		else {
    			try {
    				data_reciever = server.accept();
    				System.out.println("NODE: " + nodenum + " accepted on port: " + portnumber);
    				Frame fr = new Frame(new BufferedReader
    						(new InputStreamReader(data_reciever.getInputStream())).readLine());
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
        			if(reference.Terminate) {
        				return;
        			}
        			else {
        				System.out.println("Waiting on Switch...\n");
        				try {
        					Thread.sleep(200);
        				}catch(InterruptedException err){
        					
        				}
        			}
        		}
    		}
    		
    	}
    }
} 