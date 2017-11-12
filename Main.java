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
        Thread ourThread = new Thread(ourSwitch);

        //Start up the switch (Note, according to pdf it must be a thread of its own)
        ourThread.start();

        //switch server port will be 50000
        //System.out.println("it works");

        //populate the list with node objects to keep organization
        ArrayList<Node2017> nodegroup = new ArrayList<Node2017>();
        i = 1;
        while(i <= num_nodes) {
        	//System.out.println("YAS\n");

            Node2017 newNode = new Node2017(50000, i, serverswitch);

            //You do in fact need the .txt, it's in the PDF very clearly. Also, worked fine when I re-added the .txt in my copy.
        	nodegroup.add(newNode);

            //Construct a new Thread for each node
            Thread nodeThread = new Thread(newNode);

            //Start up the switch (Note, according to pdf it must be a thread of its own)
            nodeThread.start();

        	i++;
        }

        //When all nodes are done, shut them all down
        
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