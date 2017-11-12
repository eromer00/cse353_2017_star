import java.io.IOException;
import java.util.*;


public class Main {
    public static void main(String[] args) {
        //Get the number of nodes to be instantiated from the command line argument
        int num_nodes = Integer.parseInt(args[0]);
        //int num_nodes = 25; //hardcoded for now
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
        System.out.println("it works");
        //THIS IS CURRENTLY EXPERIMENTAL
        //populate the list with node objects to keep organization
        ArrayList<Node2017> nodegroup = new ArrayList<Node2017>();
        while(i <= num_nodes) {
        	System.out.println("YAS\n");
            //You do in fact need the .txt, it's in the PDF very clearly. Also, worked fine when I re-added the .txt in my copy.
        	nodegroup.add(new Node2017(50000, i, serverswitch));
        	i++;

        }



        //I"M TRYING TO WORK WITH THIS
        //Instantiate a number of nodes between 0 and 255 from the command line arguments
        for(int i1 = 1; i1 <= num_nodes; i1++) {
            //Get the name of the file that node will open
            /*
            String fileName = "node" + String.valueOf(i1);

            //Create the new node
            Node2017 newNode = new Node2017(50000+i1, i1, serverswitch); */
            // /*

            //Run it
            nodegroup.get(i1).run();
        }

        //When all nodes are done, shut them all down
        
        //Call Terminate_Node for each node
        for(int i2 = 1; i2 <= num_nodes; i2++) {
            try {
                nodegroup.get(i2).TerminateNode();
            } catch (IOException e) {
                System.out.println("didn't terminate node");
            }
        }
        //Terminate the switch
        ourSwitch.close();
    }
}