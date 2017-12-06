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

			socket = listener.accept();
			while(true) {
		
				if(this.nodeReference.Terminate) {
					msg("Stop listening...");
					return;
				}
				//msg("Listening...");
				
				incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				while(true) {
					incomingData = incoming.readLine();
					//msg("Incoming: " + incomingData);
					if(incomingData.equals("TERMINATE")) {
						break;
					} else if(incomingData.equals("TERMINATE\n")) {
						break;
					}
					String test = incomingData.substring(incomingData.length() - 1);
					if(test.equals("\n")) {
						incomingData = incomingData.substring(0, incomingData.length() - 1);
					}
					Frame fr = new Frame(incomingData);
					//handle ack
					int dst = fr.getSrc();
					int sdst = fr.getSSrc();
					int src = nodeReference.identificationNumber;
					int ssrc = nodeReference.switchIdentification;
					Frame ack = new Frame(String.valueOf(ssrc), String.valueOf(src),
							String.valueOf(sdst), String.valueOf(dst), "11");
					msg("received frame:" + fr.getSSrc() + "_" + fr.getSrc() + fr.parseData() +", sending ack:"
					+ ssrc + src + "11"
					);
					nodeReference.addFrame(fr);

					//fail to acknowledge 5%
					int rand = new Random().nextInt(20);
					if(rand != 10) {
						nodeReference.addFrame(ack);
					}
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
