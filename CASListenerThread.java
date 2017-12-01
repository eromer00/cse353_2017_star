package starofstars;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Base64;

public class CASListenerThread extends Thread {

	private int port = 0;
	private CASSwitch outerSwitch = null;
	public Socket socket = null;
	public ServerSocket listener = null;
	private BufferedReader incoming = null;
	
	public CASListenerThread(int port, CASSwitch outerSwitch) {
		this.port = port;
		this.outerSwitch = outerSwitch;
	}
	
	public void run() {

		//msg("Intializing Listener...");
		try {
			//msg("attempting to connect via port: " + port);
			listener = new ServerSocket(port);
			
			while(true) {
				
				Frame fr = null;
		
				if(this.outerSwitch.Terminate) {
					return;
				}
				//msg("Listening...");
				socket = listener.accept();
				
				incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				while(true) { //if we recieved a connection, then process everything that connection has to offer
					String incomingData = incoming.readLine();
										
					if(incomingData.equals("TERMINATE")) {
						break;
					}
					else if(incomingData.equals("TEST")) {
						//msg("Recieved Test");
						continue;
					}
					else {			
						fr = new Frame(incomingData);
					}

					//msg("Added Frame from " + fr.getSrc() + " to the CAS queue");
					outerSwitch.addFrame(fr);
				}			
			}
			
		}catch (Throwable e) {
			msg("ERROR --> " + e.toString());
		}
	}

	private void msg(String input) {
		System.out.println("\t\tCASListenerThread: "+ input);
	}
}
