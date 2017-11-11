public class Main {
    public static void main(String[] args) {
        //Get the number of nodes to be instantiated from the command line argument
        int num_nodes = Integer.parseInt(args[0]);

        //Instantiate a single switch (Will it end up needing to be a thread of its own?)
        Switch ourSwitch = new Switch(8080, num_nodes);

        //Start up the switch (Note, according to pdf it must be a thread of its own)
        ourSwitch.run();

        //Instantiate a number of nodes between 0 and 255 from the command line arguments
        for(int i = 1; i <= num_nodes; i++) {
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
