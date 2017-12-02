package starofstars;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


//This listener thread will listen for frames from other switches
public class CCSListenerThread extends Thread {

	private int port;
	public Socket socket = null;
	public ServerSocket listener = null;
	private CCSSwitch reference;
	private BufferedReader incoming = null;
	
	public CCSListenerThread(int port, CCSSwitch outerSwitch) {
		this.port = port;
		this.reference = outerSwitch;
	}
	
	public void run() {
		int i = 0;	
		String incomingData = null;
		
		msg("Intializing Listener...");
		try {
			
			listener = new ServerSocket(port);
			
			while(true) {
		
				if(CCSSwitch.Terminate) {
					return;
				}
				msg("Listening...");
				socket = listener.accept();
				
				incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				//msg("Recieved something...");
				while(true) {
					incomingData = incoming.readLine();
					//msg("Incoming: " + incomingData.toString());

					if(incomingData.equals("TERMINATE")) {
						break;
					}
					
					Frame fr = new Frame(incomingData); //should arrive as a byte array
					
					this.reference.addFrame(fr);
				}		
			}
			
		}catch (Throwable e) {
			msgPort("ERROR --> " + e.toString(), this.port);
			//e.printStackTrace();
		}
		
	}

	private static void msg(String input) {
		System.out.println("CSSListenerThread: "+ input);
	}
	
	private static void msgPort(String input, int port) {
		System.out.println("CSSListenerThread: Port# " + port + " --> " + input);
	}
	
}
