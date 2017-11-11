import java.util.*;


public class Main {
    public static void main(String[] args) {
        //Get the number of nodes to be instantiated from the command line argument
        //int num_nodes = Integer.parseInt(args[0]);
        int num_nodes = 2;
        //Preparation for node management
        int i = 1, serverswitch = 49500;
        
        
        //make sure num of nodes stays within defined range.
        if(num_nodes < 2 || num_nodes > 255) {
        	System.out.println("please define number of nodes such that 2 < num of nodes < 255");
        	return;
        }

        //switch server port will be 49500
        
        //populate the list with node objects to keep organization
        ArrayList<Node2017> nodegroup = new ArrayList<Node2017>();
        while(i <= num_nodes) {
        	nodegroup.add(new Node2017("node" + i ,i, serverswitch));
        	i++;
        }
        
        System.exit(-1); // for testing purposes
        
        
        
        
        
        //Instantiate a single switch (Will it end up needing to be a thread of its own?)
        Switch ourSwitch = new Switch(8080, num_nodes);

        //Start up the switch (Note, according to pdf it must be a thread of its own)
        ourSwitch.run();

        //Instantiate a number of nodes between 0 and 255 from the command line arguments
        for(int j = 1; i <= num_nodes; i++) {
            //Get the name of the file that node will open
            String fileName = "node" + String.valueOf(i) + ".txt";

            //Create the new node
            Node2017 newNode = new Node2017(fileName, 50000+i, i);

            //Run it
            newNode.run();
        }

        //When all nodes are done, shut them all down

        //Terminate the switch
    }
}
