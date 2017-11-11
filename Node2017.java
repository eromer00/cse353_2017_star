
import java.io.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Node2017 extends Thread {

    ArrayList<String> outdata = new ArrayList<>();
    private int portnum;
    private int nodenum;
    private Frame a; //to use frame in run, we should declare it here
    //private Socket sock;
    
    private PrintWriter pipe;
    File output_file;

    //read file, setup frame for all data to be sent (frame: [src][dest][size/ack][data])
    public Node2017(int portnum, int nodenum) {
        setPortnum(portnum);
        setNodenum(nodenum);
        
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

                a = new Frame(nodenum, Integer.parseInt(split[0]), split[1]);
                //outdata.add(a.toBinaryString());

            }
            br.close();
            
        } catch (Exception e) {
            System.out.println("Error setting up node " + nodenum + "\nERROR: " + e);
        }

    }

    @Override
    public void run() {
    	//System.out.println("Hello World!\n");
        /*
        while(!outdata.isEmpty()) {
            System.out.println(outdata.get(0));
            outdata.remove(0);
        }
        */
    	/*int length = outdata.size(); //prep for looping
    	PrintWriter pipe;

        try {
            System.out.println("1");
            System.out.println("PORT: " + getPortNum() + "\nNODE: " + getNodenum());
            Socket sock = new Socket(InetAddress.getLocalHost(), getPortNum());
            //this has to be handled by the switch?
            
            pipe = new PrintWriter(sock.getOutputStream(), true);
            for(int i = 0; i < length; i++) {
            	pipe.println(outdata.get(i));
            }

            System.out.println("2");
            //NodeReceive b = new NodeReceive(this.sock);

            System.out.println("3");
            //NodeSend a = new NodeSend(outdata);

            System.out.println("4");
            //a.run();

            System.out.println("5");
            //b.run();

            System.out.println("6");
            sock.close();
        } catch(IOException e) {
            System.out.println("Error starting node: " + e);
        }*/

    }




    private void setPortnum(int x) {
        this.portnum = x;
    }

    private void setNodenum(int x) {
        this.nodenum = x;
    }

    public int getPortNum() { return portnum; }

    public int getNodenum() { return nodenum; }
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
