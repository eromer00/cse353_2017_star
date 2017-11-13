
import java.io.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Node2017 extends Thread {

    public static int switchport = 0;

    private int nodenum = 0;
    
    public NodeHelper2017 Helper_reference;
    
    public boolean Sending_Done = false;
    public boolean Terminate = false;
    
    public ArrayList<String> outdata = new ArrayList<>();
    
    File output_file;

    //read file, setup frame for all data to be sent (frame: [src][dest][size/ack][data])
    public Node2017(int nodenum, int switch_port) {
        this.nodenum = nodenum;
        Node2017.switchport = switch_port;
        
        NodeHelper2017 actual_node = new NodeHelper2017();
        Helper_reference = actual_node;
        actual_node.reference = this;
        actual_node.nodenum = nodenum;

        
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
           
        actual_node.outdata = outdata;
        actual_node.start();
        
    }

    private void setNodenum(int x) {
        this.nodenum = x;
    }

    public int getNodenum() { 
    	return nodenum; 
    }
    
   public void TerminateNode() {
	   try {
		   Helper_reference.server.close();
		   Helper_reference.data_reciever.close();
		   Terminate = true;
	   }catch (Throwable e) {
		   System.out.println("");
	   }
    	
    }
}
