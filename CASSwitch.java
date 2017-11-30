package starofstars;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class CASSwitch implements Runnable {
	
	private ReentrantLock lock;


	public int Port = 0;
	public int identificationNumber;
	public int nodes_per_switch;
	
	private ExecutorService nodeExecutor = null;
	
	public CASSwitch(int Port, int identification, int nodes_per_switch) {
		
		this.Port = Port;
		this.identificationNumber = identification;
		this.nodes_per_switch = nodes_per_switch;
		msg("Setting up...");

	}
	
	public int getPort() {
		return this.Port;
	}

	@Override //this runs when thread executor is ran
	public void run() {
		
	}
	
	//make reporting soooooo much nicer
	private synchronized void msg (String input) {
		System.out.println("Switch #" + this.identificationNumber + ": " + input);
	}

}
