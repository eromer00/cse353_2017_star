package proj2_take2;

import java.io.*;

public class Node2017 {

	private NodeHelper2017 ref_helper;
	private int node_num;
	public static int switch_port;
	
	public boolean Terminate = false;
	public boolean Sending_Done = false;
	
	File mainfile;
		
	public Node2017(int switch_port, int node_num) {
		
		this.node_num = node_num;
		Node2017.switch_port = switch_port;
	
		//setup helper references for this node
		NodeHelper2017 cur_node = new NodeHelper2017();
		ref_helper = cur_node;
		
		cur_node.ref_node = this;
		cur_node.node_num = node_num;
		
		//setup,prepare the output files
		try {
			mainfile = new File("./nodes/output/" + node_num + "output.txt");
			
			//create the file if it doesn't exist
			if(!mainfile.exists()) {		
				mainfile.createNewFile();
			}
			
		}catch(Throwable e) {
			System.out.println("ERROR: create output file failed:" + e.toString());
		}
			
		//start processing infomation
		cur_node.start();
	}
	
	public void Terminate_Node() {
		try {
			ref_helper.send_out.close();
			ref_helper.reciever.close();
			Terminate = true;
		}catch(Throwable e) {
			System.out.println("ERROR: node#:" + node_num + " where:Terminate_Node -> " + e );
		}
	}
}
