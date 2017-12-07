//package starofstars;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class NodeListener extends Thread{

	public int port = 0;
	private Node nodeReference = null;
	public Socket socket = null;
	public ServerSocket listener = null;
	private BufferedReader incoming = null;
	
	public NodeListener(int Port, Node nodeReference){
		this.port = Port;
		this.nodeReference = nodeReference;
	}
	
	public void run() {
		String incomingData = null;

		//msg("Intializing Listener...");
		try {
			//msg("attempting to connect via port: " + port);
			listener = new ServerSocket(port);
			msg("NodeListener:" + nodeReference + " connected");

			while(true) {
		
				if(this.nodeReference.Terminate) {
					msg("Stop listening...");
					return;
				}
				//msg("Listening...");
				socket = listener.accept();
				
				incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				while(true) {
					incomingData = incoming.readLine();
					//msg("Incoming: " + incomingData);
					if(incomingData.equals("TERMINATE")) {
						break;
					}
					String test = incomingData.substring(incomingData.length() - 1);
					if(test.equals("\n")) {
						incomingData = incomingData.substring(0, incomingData.length() - 1);
					}
					Frame fr = new Frame(incomingData);

					nodeReference.addFrame(fr);

				}			
			}
			
		}catch (Throwable e) {
			msg("ERROR --> " + e.toString());
		}
	}
	
	private void msg(String input) {
		System.out.println("\t\tNodeListener: "+ input);
	}
}
