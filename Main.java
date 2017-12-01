package starofstars;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//Requirements:
//	Node input files to be located in: "./nodes"
//	Node output files will be placed in "./nodes/output" (created if it doesn't exist)
//  CSS Start Port 30000 -> each (i+1)th node becomes a port
public class Main {
	
	public static CCSSwitch Shadow = null; //keep a backup of current CASSwitch in case it fails
	public static CCSSwitch CCS = null; //keep it out here so other switches can see it
	public static SecureRandom rand = new SecureRandom();
	public static int numOfLines = 0;
	
	public static void main(String[] args) {
		
		ExecutorService switchExecutor = null;
		ExecutorService nodeExecutor = null;

		ArrayList<CASSwitch> CASList = new ArrayList<CASSwitch>();
		ArrayList<Node> NodeList = new ArrayList<Node>();
		
		int CAS_count = 0, nodesPerSwitch = 0;
		
		//User interaction, makes things easier when testing too.
		System.out.println("Star of Stars");
		Scanner scan = new Scanner(System.in);
		System.out.printf("How many CAS Switches are there?: ");
		CAS_count = scan.nextInt();
		System.out.printf("How many nodes per CAS switch?: ");
		nodesPerSwitch = scan.nextInt();
		
		//some user interation, just for the robustness of the network
		int z = 0;
		while((nodesPerSwitch > 16 || nodesPerSwitch < 2) && (CAS_count < 2 || CAS_count > 16)) {
				
			if(CAS_count < 2 || CAS_count > 16) {
				if(CAS_count < 2) {
					System.out.printf("\tMain: There must be at least 2 CAS Switches, try again: ");
					CAS_count = scan.nextInt();
					continue;
				}
				if(CAS_count > 16) {
					System.out.printf("\tMain: There must be no more than 16 CAS Switches, try again: ");
					CAS_count = scan.nextInt();
					continue;
				}
			}	
			if((nodesPerSwitch > 16 || nodesPerSwitch < 2)) {
				
				if(nodesPerSwitch < 2) {
					System.out.printf("\tMain: There must be at least 2 nodes per switch.\n\tMain: Try Again: ");
					nodesPerSwitch = scan.nextInt();
					continue;
				}
				
				if(z > 0) {
					System.out.printf("\tMain: Try again: ");
					nodesPerSwitch = scan.nextInt();
				}
				else {
					msg("So uh...");
					msg("\"You are not going to have more than 16 nodes connected to any CAS at any given time\"");
					msg("--The PDF");
					msg("Tone it down a bit would you? :) thx");
					System.out.printf("\tMain: How many nodes per CAS switch this time?: ");
					nodesPerSwitch = scan.nextInt();
					z++;
				}	
			}
		}
		
		int CSS_Port = 49152;
		int CSS_startPort = CSS_Port + 1; //start assigning switch ports from here (startPort + i)
		CCS = new CCSSwitch(CSS_Port, CSS_startPort);
		Shadow = CCS; //create a backup copy of switch
		
		if(Shadow != null) {
			msg("Shadow backup is working.");
		}
		
		CCS.start(); //it only needs one thread to do its work, but it will have buddy threads to send to other switches
		
		switchExecutor = Executors.newFixedThreadPool(CAS_count); //create a pool of switch ports
		nodeExecutor = Executors.newFixedThreadPool(nodesPerSwitch * CAS_count); //create a pool of switch ports

		System.out.println(); //just for new line
		//int CAS_nodeDeviation =  (CSS_startPort + 1) + CAS_count; //start from the CSS Port + the number of CAS switch
		int CAS_nodeDeviation = 52000;
		
		for(int i = 0; i < CAS_count; i++) {
			
			CASList.add(new CASSwitch(i + 1, nodesPerSwitch, (CAS_nodeDeviation + (16 * (i+1)))));
			
			msg("NodeDeviation: " + Integer.toString(CAS_nodeDeviation + (16 * (i+1)))); //because each switch will have at most 16 nodes
			
			for(int k = 0; k < nodesPerSwitch; k++) {
				
				NodeList.add(new Node(k + 1, i + 1, CASList.get(i), CASList.get(i).getPort())); //because arrays start at 0
				msg("created (" + (k+1) + "," + (i+1) + ") (switch,node)");
				
			}
		}
		//Create Objects ----------------------------------------------
		//set up the nodes, NOTE THAT PORTS WILL BE ASSIGNED BY SWITCHES
		
		//End Creation-------------------------------------------------
		
		try {
			msg("Waiting 5 seconds before running switchs to show what is happening...");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		//Crank their Engines...get them started
		for(int i = 0; i < CASList.size(); i++) {
			switchExecutor.execute(CASList.get(i));
		}
		
		try {
			msg("Waiting 5 seconds before running nodes...");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		for(int i = 0; i < NodeList.size(); i++) {
			nodeExecutor.execute(NodeList.get(i));
		}
		
		while(true){
			
			/*if(NodeList.isEmpty()) {
				nodeExecutor.shutdownNow();
			}
			else {
				if(NodeList.get(0).Terminate) {
					NodeList.remove(NodeList.get(0));
				}
			}
			if(CASList.isEmpty()) {
				switchExecutor.shutdownNow();
			}
			else {
				if(CASList.get(0).Terminate) {
					NodeList.remove(NodeList.get(0));
				}
			}*/
			
			if(switchExecutor.isShutdown() && nodeExecutor.isShutdown()) {
				msg("All Switches and nodes have been terminated...");
				break;
			}
		}
		
		msg("finished all transfers, exitting...");
		System.exit(-1);
	}
	
	private static void msg (String input) {
		System.out.println("\tMain: " +  input);

	}

}
