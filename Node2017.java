
import java.io.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Node2017 extends Thread {

    ArrayList<String> outdata = new ArrayList<>();
    public int switchport = 0;
    private int portnum = 0;
    private int nodenum = 0;
    private int sleep_duration = 500; //ms --> 1/2 sec
    private int portnumber = 0;
    
    ServerSocket server;
    Socket data_reciever;
    
    public boolean Sending_Done = false;
    public boolean Terminate = false;
    
    File output_file;

    //read file, setup frame for all data to be sent (frame: [src][dest][size/ack][data])
    public Node2017(int portnum, int nodenum, int switch_port) {
        setPortnum(portnum);
        setNodenum(nodenum);
        switchport = switch_port;
        
        //filename should be of noden for some n
        //directory should be of thisfile'sspot/nodes/output/noden.txt
        
        //CREATE OUTPUT FILES
        try {
        	output_file = new File("./nodes/output/node" + nodenum +"output.txt"); // this is easier
        	
        	if(!output_file.exists()) { //if output doesn't exist make it exist
        		output_file.createNewFile(); //they get created as we process node to node
        	}

        }catch(Exception e) {
        	System.out.println("ERROR: " + e + "\ncouldn't make outputfile for node basically");
        };
        
        //READ IN STUFF
        try {
            BufferedReader br = new BufferedReader(new FileReader("./nodes/node" + nodenum + ".txt"));
            String s;

            //read in lines and convert to padded binary string
            while((s = br.readLine()) != null) {
                String[] split = s.split(":");

                Frame a = new Frame(nodenum, Integer.parseInt(split[0]), split[1]);
                outdata.add(a.toBinaryString());

            }
            br.close();
            
        } catch (Exception e) {
            System.out.println("Error setting up node " + nodenum + "\nERROR: " + e);
        }

    }

    public void run() {
    	
    	//this all runs in a loop from main
    	int numofelements = outdata.size(); //# of frames to send
    	//remember this is outdata is global within this class
    	
    	SendToSwitch(numofelements); //send frames to switch and let it worry about where they go
    	System.out.println("SENDTOSWITCH Done");
    	server = AssignPort(); //get a port from the switch, also inform that this node is done sending
    	System.out.println("ASSIGNPORT Done");
    	Recieve_Write(server); //Now the node can receive and write the data to the corresponding file
    	System.out.println("RECIEVE_WRITE Done");
    }
    
    private void Recieve_Write(ServerSocket server) {
 
    	//keep going until done
    	while(true) {
    		if(Terminate) {
    			return;
    		}
    		else {
    			try {
    				data_reciever = server.accept();
    				System.out.println("NODE: " + nodenum + "accepted on port: " + portnumber);
    				Frame fr = new Frame(new BufferedReader
    						(new InputStreamReader(data_reciever.getInputStream())).readLine());
    				try {
    					//Allow for file appending
    					File output = new File("./nodes/output/node" + fr.getDest() + "output.txt"); 
    					FileWriter filewrite = new FileWriter("./nodes/output/" + output.getName(), true);
    					BufferedWriter writer = new BufferedWriter(filewrite);
    					
    					//Write the frame data built from binary string in the requested format
    					writer.write(fr.getSrc() + ":" + fr.getData() + "\n");
    					
    					writer.close();
    					filewrite.close();
    					
    					System.out.println("Complete Write: node" + fr.getDest() + "output.txt");

    				}catch(Exception x) {
    					System.out.println("ERROR: " + x);
    				}
    				
        		}catch (Throwable e){
        			if(Terminate) {
        				return;
        			}
        			else {
        				System.out.println("Waiting on Switch...\n");
        				try {
        					Thread.sleep(sleep_duration);
        				}catch(InterruptedException err){
        					
        				}
        			}
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
				Sending_Done = true;
				break;
			} catch (Throwable e) {
				System.out.println("ERROR, CANT START SERVER WITH PORT: " + portnum + "\n" + e);
			}
    	}   	
		return server;
    }


    private void SendToSwitch(int numofelements) {
    	
    	PrintWriter pt;
    	String out_data;
    	
    	while(true) { //infinite loop, until we break
    		try {
    			Socket send_out;
    			
    			send_out = new Socket(InetAddress.getLocalHost(), switchport); //send things to the switch
    			
    			pt = new PrintWriter(send_out.getOutputStream(), true);
    			
    			for(int k = 0; k < numofelements; k++) {
    				out_data = outdata.get(k); //get our binary string frame, that is converted already
    				//Frame f = new Frame(out_data);
    				//System.out.println("Sent: " + f.getData());
    				pt.print(out_data); //the switch should handle dest, src, and stuff
    			}
    			
    			pt.close();
    			send_out.close();
    			
    		}catch(Throwable e) {
    			try {
    				System.out.println("Waiting on the switch...");
    				Thread.sleep(sleep_duration);
    			}catch(InterruptedException err){
    				continue;
    			};
    		};
    		break;
    	}
    }

    private void setPortnum(int x) {
        this.portnum = x;
    }

    private void setNodenum(int x) {
        this.nodenum = x;
    }

    public int getPortNum() { 
    	return portnum; 
    }

    public int getNodenum() { 
    	return nodenum; 
    }
    
    private void TerminateNode() throws IOException {
    	server.close();
    	data_reciever.close();
    	Terminate = true;
    }
} 
