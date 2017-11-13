
import java.io.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Node2017 extends Thread {

    ArrayList<String> outdata = new ArrayList<>();
    public int switchport = 0;
    private int portnum = 50000;
    private int nodenum = 0;
    public static int sleep_duration = 500; //ms --> 1/2 sec
    private int portnumber = 0;
    public boolean floodReceived = false;

    public Socket data_reciever;
    
    public boolean Sending_Done = false;
    public static boolean Terminate = false;
    
    File output_file;

    //read file, setup frame for all data to be sent (frame: [src][dest][size/ack][data])
    public Node2017(int portnum, int nodenum, int switch_port) {
        setPortnum(portnum);
        setNodenum(nodenum);
        switchport = switch_port;
        
        //filename should be of noden for some n
        //directory should be of thisfile'sspot/nodes/output/noden.txt
        
        //CREATE OUTPUT FILES
        try {
        	output_file = new File("./nodes/output/node" + nodenum +"output.txt"); // this is easier
        	
        	if(!output_file.exists()) { //if output doesn't exist make it exist
        		output_file.createNewFile(); //they get created as we process node to node
        	}

        }catch(Exception e) {
        	System.out.println("ERROR: " + e + "\ncouldn't make outputfile for node basically");
        };
        
        //READ IN STUFF
        try {
            BufferedReader br = new BufferedReader(new FileReader("./nodes/node" + nodenum + ".txt"));
            String s;

            //read in lines and convert to padded binary string
            while((s = br.readLine()) != null) {
                String[] split = s.split(":");

                Frame a = new Frame(nodenum, Integer.parseInt(split[0]), split[1]);
                outdata.add(a.toBinaryString());

            }
            br.close();
            
        } catch (Exception e) {
            System.out.println("Error setting up node " + nodenum + "\nERROR: " + e);
        }

    }

    public void run() {
    	
    	//this all runs in a loop from main
    	int numofelements = outdata.size(); //# of frames to send
    	//remember this is outdata is global within this class

		try {
			data_reciever = new Socket("127.0.0.1", portnum);
			System.out.println("NODE #" + nodenum + " accepted on port: " + portnum);

			//Recieve_Write(data_reciever); //Now the node can receive and write the data to the corresponding file
			NodeReceive rw = new NodeReceive(portnum, nodenum, this.data_reciever);
			Thread read = new Thread(rw);
			read.start();
			System.out.println("RECIEVE_WRITE Done");
			NodeSend ns = new NodeSend(numofelements, switchport, outdata, this.data_reciever);
			Thread send = new Thread(ns); //send frames to switch and let it worry about where they go
			send.run();
			System.out.println("SENDTOSWITCH Done");
			//server = AssignPort(); //get a port from the switch, also inform that this node is done sending
			//System.out.println("ASSIGNPORT Done");
		} catch(IOException e) {
			System.out.println("Error running node socket");
		}

    }
    /*
    private void Recieve_Write(Socket sockout) {
 
    	//keep going until done
    	while(true) {
    		if(Terminate) {
    			return;
    		}
    		else {
    			try {
    				data_reciever = new Socket("127.0.0.1", portnum);
    				System.out.println("NODE: " + nodenum + "accepted on port: " + portnum);
    				BufferedReader br = new BufferedReader(new InputStreamReader(data_reciever.getInputStream()));


    				try {
						//System.out.println("a");
						String x = br.readLine();
						Frame fr = new Frame(x);

						System.out.println("found frame: " + x);
    					//Allow for file appending
    					File output = new File("../nodes/output/node" + fr.getDest() + "output.txt");
    					FileWriter filewrite = new FileWriter("../nodes/output/" + output.getName(), true);

    					if(fr.getDest() == 0) {
    						System.out.println("flooded");
    						//flood frame, reset socket
							portnum = Integer.parseInt(fr.getData());
						} else {
							BufferedWriter writer = new BufferedWriter(filewrite);

							//Write the frame data built from binary string in the requested format
							writer.write(fr.getSrc() + ":" + fr.getData() + "\n");

							writer.close();
							filewrite.close();

							System.out.println("Complete Write: node" + fr.getDest() + "output.txt");
						}
    				}catch(Exception x) {
    					System.out.println("ERROR: " + x);
    				}
    				
        		}catch (Throwable e){
        			if(Terminate) {
        				return;
        			}
        			else {
        				System.out.println("Waiting on Switch...\n");
        				try {
        					Thread.sleep(sleep_duration);
        				}catch(InterruptedException err){
        					
        				}
        			}
        		}
    		}
    		
    	}
    }
    */
    
    private ServerSocket AssignPort() {
    	
    	ServerSocket server;
    	while(true) {
	    	try {
	    		portnumber = Switch.Port(nodenum);
	    		//setPortnum(Switch.port); //This is dependent on the port static method in switch class, it breaks things actually
				server = new ServerSocket(portnumber);
				Sending_Done = true;
				break;
			} catch (Throwable e) {
				System.out.println("ERROR, CANT START SERVER WITH PORT: " + portnum + "\n" + e);
			}
    	}   	
		return server;
    }
	/*

    private void SendToSwitch(int numofelements) {
    	
    	PrintWriter pt;
    	String out_data;
    	
    	while(true) { //infinite loop, until we break
    		try {
    			Socket send_out;
    			
    			send_out = new Socket(InetAddress.getLocalHost(), switchport); //send things to the switch
    			
    			pt = new PrintWriter(send_out.getOutputStream(), true);
    			
    			for(int k = 0; k < numofelements; k++) {
    				out_data = outdata.get(k); //get our binary string frame, that is converted already
    				//Frame f = new Frame(out_data);
    				//System.out.println("Sent: " + f.getData());
    				pt.print(out_data); //the switch should handle dest, src, and stuff
    			}
    			
    			pt.close();
    			send_out.close();
    			
    		}catch(Throwable e) {
    			try {
    				System.out.println("Waiting on the switch...");
    				Thread.sleep(sleep_duration);
    			}catch(InterruptedException err){
    				continue;
    			};
    		};
    		break;
    	}
    }
	*/
    private void setPortnum(int x) {
        this.portnum = x;
    }

    private void setNodenum(int x) {
        this.nodenum = x;
    }

    public int getPortNum() { 
    	return portnum; 
    }

    public int getNodenum() { 
    	return nodenum; 
    }
    
    public void TerminateNode() throws IOException {
    	//server.close();
    	data_reciever.close();
    	Terminate = true;
    }



}

class NodeReceive implements Runnable {
	public Socket data_reciever;
	public int portnum, nodenum;

	public NodeReceive(int portnum, int nodenum, Socket sock) {
		this.portnum = portnum;
		this.nodenum = nodenum;
		this.data_reciever = sock;
	}

	public void run() {
		try {
			System.out.println("a");
			BufferedReader br = new BufferedReader(new InputStreamReader(data_reciever.getInputStream()));
			System.out.println("b");

			//keep going until done
			while (true) {
				if (Node2017.Terminate) {
					return;
				} else {
					try {
						if (br.ready()) {
							String x = br.readLine();
							Frame fr = new Frame(x);

							if (x.equals("terminate")) {
								Node2017.Terminate = true;
								continue;
							}

							System.out.println("found frame: " + x);
							//Allow for file appending
							File output = new File("../nodes/output/node" + fr.getDest() + "output.txt");
							FileWriter filewrite = new FileWriter("../nodes/output/" + output.getName(), true);
								/*
								if(fr.getDest() == 0) {
								System.out.println("flooded");
								//flood frame, reset socket
								portnum = Integer.parseInt(fr.getData());
								} else { */
							BufferedWriter writer = new BufferedWriter(filewrite);

							//Write the frame data built from binary string in the requested format
							writer.write(fr.getSrc() + ":" + fr.getData());
							writer.newLine();
							writer.flush();

							writer.close();
							filewrite.close();

							System.out.println("Complete Write: node" + fr.getDest() + "output.txt");
							//}
						}
					} catch (Exception x) {
						System.out.println("ERROR: " + x);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error receiving data");
		}
	}
}

class NodeSend implements Runnable {
	public int numofelements;
	public int port;
	public ArrayList<String> data;
	public Socket data_reciever;

	public NodeSend(int num, int switchport, ArrayList<String> outdata, Socket sock) {
		this.numofelements = num;
		this.port = switchport;
		this.data = outdata;
		this.data_reciever = sock;
	}

	public void run() {

		PrintWriter pt;
		String out_data;

			try {

				pt = new PrintWriter(data_reciever.getOutputStream());

				for(int k = 0; k < numofelements; k++) {
					out_data = data.get(k); //get our binary string frame, that is converted already
					//Frame f = new Frame(out_data);
					//System.out.println("Sent: " + f.getData());
					pt.println(out_data); //the switch should handle dest, src, and stuff
					pt.flush();

				}
				pt.flush();
				pt.println("terminate\n");
				pt.close();


			}catch(Throwable e) {
				try {
					System.out.println("Waiting on the switch...");
					Thread.sleep(Node2017.sleep_duration);
				}catch(InterruptedException err){
					System.out.println("error waiting for switch");
				}
			}
	}
}