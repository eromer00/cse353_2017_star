import java.io.IOException;
import java.util.*;


public class Main {
    public static void main(String[] args) {

    			//g("Main: Hello, world.");
    			//int nodeNum = Integer.parseInt(args[0]); //# of nodes to be generated
    			int nodeNum = 25;
    	if (nodeNum < 2 || nodeNum > 255) {
    				//g("Main: Number of nodes must be between 2 and 255");
    				return;
    			}
    			ArrayList<Node2017> nodeRay = new ArrayList<Node2017>(); //ArrayList of Nodes
    			for (int i = 1; i <= nodeNum; i++) {
    				//msg("Main: Starting node "+i);
    				nodeRay.add(new Node2017(i, 49152));
    			}
    			//msg("Main: Creating switch");
    			Switch s = new Switch(49152, nodeNum);
    			s.start();
    			//msg("Main: Switch is running");
    			/* 
    				* I have to to close each node after they have sent data, and then close the switch
    				*/
    			ArrayList<Node2017> nodesLeft = new ArrayList<Node2017>(nodeRay);
    			for (;;) {
    				for (int i = nodesLeft.size()-1; i > -1; i--) {
    					Node2017 n = nodesLeft.get(i);
    					if (n.Sending_Done)
    						System.out.println("SHIT");
    						nodesLeft.remove(i);
    				}
    				//msg("Main: Frames left: "+Switch.frames.size()+" Nodes left: "+nodesLeft.size());
    				if (Switch.frames.size() == 0 && nodesLeft.size() == 0) 
    					break;
    				try {
    					Thread.sleep(1000);
    				} catch (Throwable e) {
    					System.out.println("CAN'T SLEEP");
    				};
    			}
    			try {
    				Thread.sleep(1000);
    			} catch (Throwable e) {};
    			
    			System.out.println("Main closing everything");
    			for (int i = 0; i < nodeNum; i++) {
					nodeRay.get(i).TerminateNode();

    			}
    			s.close();
    			System.out.println("PROGRAM SUCCESSFUL!! :D");
    			System.exit(0);
    		}


        //Get the number of nodes to be instantiated from the command line argument
        //int num_nodes = Integer.parseInt(args[0]);
        /*int num_nodes = 25; //hardcoded for now
        //Preparation for node management
        int i = 1, serverswitch = 49700;
        
        //make sure num of nodes stays within defined range.
        if(num_nodes < 2 || num_nodes > 255) {
        	System.out.println("please define number of nodes such that 2 < num of nodes < 255");
        	return;
        }
        
        Switch ourSwitch = new Switch(serverswitch, num_nodes);
        
        ArrayList<Node2017> nodelist = new ArrayList<Node2017>();
        
        System.out.println("let");
        while(i <= num_nodes) {
        	nodelist.add(new Node2017(50000+i, i, serverswitch));
        }
        ArrayList<Node2017> nodes = new ArrayList<Node2017>(nodelist);
        ourSwitch.start();
        System.out.println("Let the games begin");
        while(true) {
        	for(int k = nodes.size()-1; k != 0; k--) {
        		Node2017 node = nodes.get(i);
        		if(node.Sending_Done == true) {
        			nodes.remove(i);
        		}
        	}
        	if(nodes.size() == 0 && Switch.frames.size()) {
        		break;
        	}
        	try {
        		Thread.sleep(700);
        	}catch (Exception e) {
        		//uhhhhh
        	}
        }
        
        for(int k = 0; k < num_nodes; k++) {
        	try {
				nodelist.get(i).TerminateNode();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        ourSwitch.close();
        

    }*/
}
