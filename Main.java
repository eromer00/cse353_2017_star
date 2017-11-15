package proj2_take2;

import java.util.*;

public class Main {

	//rewritten from scratch take 3
	
	public static void main(String[] args) {
		int num_nodes = 255;
		
		if(num_nodes > 255 || num_nodes < 2) {
			System.out.println("Number of nodes must be between 2 and 255 total");
			return;
		}
		
		ArrayList<Node2017> node_array = new ArrayList<Node2017>();
		
		for(int i = 1; i <= num_nodes; i++) {
			node_array.add(new Node2017(49152, i));		
		}
		
		Switch s = new Switch(49152, num_nodes);
		s.start();
		
		ArrayList<Node2017> nodes = new ArrayList<Node2017>(node_array);
		
		while(true) {
			
			for(int k = nodes.size() - 1; k > -1; k--) {
				Node2017 nod = nodes.get(k);
				
				if(nod.Sending_Done) {
					nodes.remove(k);
				}
				
			}
			
			if(Switch.frames.size() == 0 && nodes.size() == 0) {
				break;
			}
			
			try {
				Thread.sleep(1200);
			} catch(Throwable e) {
				System.out.println("ERROR: main sleep --> " + e.toString());
			}
		}
		try {
			Thread.sleep(1000);
		} catch (Throwable e) {};
		for(int j = 0; j < num_nodes; j++) {
			node_array.get(j).Terminate_Node();
		}
		
		try {
			Thread.sleep(1200);
		} catch(Throwable e) {
			System.out.println("ERROR: main sleep --> " + e.toString());
		}
		
		s.Terminate_Switch();
		System.out.println("Fully finished!!!....finally");
		System.exit(0);
	}

}
