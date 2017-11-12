
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
    //private Socket sock;
    
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
        		output_file.createNewFile();
        	}
        	
        	FileWriter new_file = new FileWriter("./nodes/output/" + output_file.getName(), false);

        	//BufferedWriter writer = new BufferedWriter(new_file);
        	//new_file.close();

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
    	
    	int numofelements = outdata.size(); //# of frames to send
    	//remember this is global within this class
    	ServerSocket server;
    	
    	SendToSwitch(numofelements); //send frames to switch and let it worry about where they go
    	server = AssignPort(); //get a port from the switch, also inform that this node is done sending
    	
    	//then receive data to write to file
    }
    
    private ServerSocket AssignPort() {
    	
    	ServerSocket server;
    	while(true) {
	    	try {
	    		setPortnum(Switch.port); //This is dependent on the port static method in switch class
				server = new ServerSocket(portnum);
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
    				out_data = outdata.get(k); //get our binary string, that is converted already
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
}

class NodeSend extends Thread {

    private ArrayList<String> frames;

    public NodeSend(ArrayList<String> x) {
        this.frames = x;
    }

    @Override
    public void run() {

    }
}

class NodeReceive extends Thread {

    Socket sock;

    public NodeReceive(Socket sck) {
        this.sock = sck;
    }

    @Override
    public void run() {
        try {
            //placeholder
            int i = 0;
            sleep(1000);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    
}
