import java.io.IOException;
import java.util.*;


public class Main {
    public static void main(String[] args) {
        //Get the number of nodes to be instantiated from the command line argument
        int num_nodes = Integer.parseInt(args[0]);

        //Preparation for node management
        int i = 1, serverswitch = 50000;

        //make sure num of nodes stays within defined range.
        if(num_nodes < 2 || num_nodes > 255) {
        	System.out.println("please define number of nodes such that 2 < num of nodes < 255");
        	return;
        }

        //Instantiate a single switch (Will it end up needing to be a thread of its own?)
        Switch ourSwitch = new Switch(serverswitch, num_nodes);

        //Construct a new Thread
        Thread switchThread = new Thread(ourSwitch);

        //Start up the switch (Note, according to pdf it must be a thread of its own)
        switchThread.start();

        //populate the list with node objects to keep organization
        ArrayList<Node2017> nodegroup = new ArrayList<Node2017>();

        for(i = 1; i <= num_nodes; i++) {
            //Create a new node
            Node2017 newNode = new Node2017(50000, i, serverswitch);

            //Add it to the list of nodes for later cleanup?
        	nodegroup.add(newNode);

            //Construct a new Thread for each node
            Thread nodeThread = new Thread(newNode);

            //Start up the thread for the new node
            nodeThread.start();

        }

        //When all nodes are done, shut them all down (had to comment out due to race condition stuff)
        
        //Call Terminate_Node for each node
        /*for(int i2 = 1; i2 <= num_nodes; i2++) {
            try {
                nodegroup.get(i2).TerminateNode();
            } catch (IOException e) {
                System.out.println("didn't terminate node");
            }
        }
        //Terminate the switch
        ourSwitch.close();*/
    }
}