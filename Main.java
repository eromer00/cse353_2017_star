package starofstars;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//Requirements:
//	Node input files to be located in: "./nodes"
//	Node output files will be placed in "./nodes/output" (created if it doesn't exist)

public class Main {
	
	public static CCSSwitch Shadow = null; //keep a backup of current CASSwitch in case it fails

	public static void main(String[] args) {
		
		ExecutorService switchExecutor = null;
		ExecutorService nodeExecutor = null;

		ArrayList<CASSwitch> CASList = new ArrayList<CASSwitch>();
		ArrayList<Node> NodeList = new ArrayList<Node>();
		
		int CAS_count = 0, nodesPerSwitch = 0;
		
		//User interaction, makes things easier when testing too.
		System.out.println("Star of Stars");
		Scanner scan = new Scanner(System.in);
		System.out.printf("How many CAS's?: ");
		CAS_count = scan.nextInt();
		System.out.printf("How many nodes per CAS switch?: ");
		nodesPerSwitch = scan.nextInt();
		
		if(nodesPerSwitch > 16) {
			msg("So uh...");
			msg("\"You are not going to have more than 16 nodes connected to any CAS at any given time\"");
			msg("--The PDF");
			msg("Tone it down a bit would you? :) thx");
			System.exit(1);
		}
		
		int CSS_Port = 50000;
		CCSSwitch CCS = new CCSSwitch(CSS_Port);
		Shadow = CCS; //create a backup copy of switch
		
		CCS.run(); //it only needs one thread to do its work
		
		switchExecutor = Executors.newFixedThreadPool(CAS_count); //create a pool of switch ports
		nodeExecutor = Executors.newFixedThreadPool(nodesPerSwitch * CAS_count); //create a pool of switch ports

		System.out.println(); //just for new line
		int CAS_startPort = 52000;
		for(int i = 1; i <= CAS_count; i++) {
			CASList.add(new CASSwitch(CAS_startPort + i, i, nodesPerSwitch));
			for(int k = 1; k <= nodesPerSwitch; k++) {
				NodeList.add(new Node(k, i));
				msg("created (" + k + "," + i + ") (switch,node) pair");
			}
		}
		
		//Create Objects ----------------------------------------------
		
		int Node_startPort = 55000;
		//set up the nodes, NOTE THAT PORTS WILL BE ASSIGNED BY SWITCHES
		
		//End Creation-------------------------------------------------
		
		//Crank their Engines...get them started
		//for(int i = 0; i < CASList.size(); i++) {
		//	switchExecutor.execute(CASList.get(i));
		//}
		
	}
	
	private static void msg (String input) {
		System.out.println("\tMain: " +  input);

	}

}
