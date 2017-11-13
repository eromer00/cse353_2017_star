import java.io.IOException;
import java.util.*;


public class Main {
    public static void main(String[] args) {
		int nodeNum = Integer.parseInt(args[0]);
    	if (nodeNum < 2 || nodeNum > 255) {
    				return;
		}

    	ArrayList<Node2017> nodes = new ArrayList<Node2017>(); //ArrayList of Nodes
    	for (int i = 1; i <= nodeNum; i++) {
    		nodes.add(new Node2017(i, 50000));
    	}

    	Switch s = new Switch(50000, nodeNum);
    	s.start();

    	ArrayList<Node2017> nodesLeft = nodes;
    	for (;;) {

    		for (int i = 0; i < nodesLeft.size() - 1; i++) {
    			Node2017 n = nodesLeft.get(i);
    			if (n.Sending_Done)
    				System.out.println("node " + i + " done sending...");
    				nodesLeft.remove(i);
    		}
    		if (Switch.frames.size() == 0 && nodesLeft.size() == 0)
    			break;
    		try {
    			Thread.sleep(1000);
    		} catch (Throwable e) {
    			System.out.println("Error sleeping");
    		};
    	}
    	try {
    		Thread.sleep(1000);
    	} catch (Throwable e) {};
    			
    	System.out.println("FINISHED...closing switch and nodes");
    	for (int i = 0; i < nodeNum; i++) {
			nodes.get(i).TerminateNode();
    	}
			s.close();
			System.out.println("Goodbye");
    	}
}