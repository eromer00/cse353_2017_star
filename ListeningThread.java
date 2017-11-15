package proj2_take2;

import java.io.*;
import java.net.*;

public class ListeningThread extends Thread {

	public int mainPort = 0;
	private Socket sock;
	private ServerSocket listener;
	
	public ListeningThread(int mainPort) {
		this.mainPort = mainPort;
	}
	
	public void run() {
		
		try {
			listener = new ServerSocket(mainPort);
			while(true) {
				if(Switch.Terminate) {
					return;
				}
				sock = listener.accept();
				
				BufferedReader input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
				while(true) {
					String data = input.readLine();
					if(data.equals("terminate")) {
						break;
					}
					else {
						Frame fr = new Frame(data);
						Switch.frames.add(fr);
					}
					
				}
			}
		}catch(Throwable e) {
			System.out.println("ERROR: ListeningThread: " + e.toString());
		}
	}
	
}
