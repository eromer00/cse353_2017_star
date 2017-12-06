//package starofstars;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Node implements Runnable {
	//Variables
	private int Port = 0;
	private int identificationNumber;
	private int switchIdentification;
	
	private CASSwitch switchReference;

	private File directory;
	private File mainfile;
	private File in_file;
	
	private ArrayList<Frame> framesToSend;
	private ArrayList<Frame> framesRecieved = new ArrayList<Frame>();
	private Socket socket = null;
	
	private int numofLines = 0;
	
	private boolean Terminate = false;
	private boolean ackFound = false;

	/**
	 * Constructor for the Node
	 * @param identification 		Global dentification number for the node
	 * @param switchIdentification 	The ID# of the switch this node connects to
	 * @param switchReference 		Essentially a pointer to the switch this node connects to
	 * @param switchPort 			Legacy, does nothing. Didn't remove so as not to break compat.
	 */
	public Node(int identification, int switchIdentification, CASSwitch switchReference, int switchPort) {
		this.identificationNumber = identification; //hold the node number make it global to object
		this.switchIdentification = switchIdentification;
		this.switchReference = switchReference;
		this.Port = this.switchReference.allocPort(this.identificationNumber); //reference the switch that this node is connecting to
		
		
		msg("Preparing Node...");
				
		prepOutputFiles();
		readInputFiles();
		
		if(framesToSend != null) {
			msg("Successfully assembled Frames for sending!");		
		}
		else if (framesToSend.size() == 0) {
			msg("This Node has no frames to send...");
		}
		
	}

	@SuppressWarnings("unused")
	@Override
	/**
	 * Run by a ThreadExecutor
	 * Sends an arrayList of frames as bytes
	 * Switch handles/routes frames
	 * Bytes can be converted back to frames using the frame class
	 */
	public void run() {
		//msg("I was given port: " + this.Port);
		//msg("My switch's port is: " + this.switchPort);
		
		NodeListener listener = new NodeListener(this.Port, this);
		listener.start();
		
		try {
			//Thread.sleep(200);
			socket = new Socket("localhost", this.switchReference.getPort()); 
			//msg("Accepting..");
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			String data = null;
			int sourceSwitch = 0, sourceNode = 0; 
			
			
			//msg("Waiting 5 seconds before starting...");
			
			try {
				Thread.sleep(2000); //wait 2 seconds before starting just in case socket isn't ready yet...
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			writer.println("TERMINATE"); //so the CASSwitch will know to stop listening to this particular node
			
			Frame fr = null;
			String g = null;
			int k = 0;

			//Main running loop for the node where it both sends and receives frames
			while(true) {
				//Get the first frame in the list of frames we want to send, and send it
				fr = framesToSend.get(0);

				//Report what frame we're sending out
				msg("sending: " + fr.toString());

				//Write to the port, thus "sending" the frame
				writer.println(fr.toString()); //uses a string at the moment, find a way to

				//Wait around for acknowledgement
				//Note, tried doing a "while(true)" loop that just did a break; on the same condition, but apparently that isn't acceptable to either Java or InteliJ, but this does the same thing
				while(ackFound == false) {
					//Loop through all the frames received, look for a size of 0. That one is an ACK, and we can move on.
					for(Frame frame : framesRecieved) {
						if(frame.getSize() == 0) {
							ackFound = true;
						}
						//Remove the ACK from the frames received buffer.
						framesRecieved.remove(frame);
					}
				}

				//Once we've sent the frame, we no longer need it in the list (the next should become the zeroth frame)
				framesToSend.remove(0);

				//Reset boolean condition for next time around
				ackFound = false;

				fr = null;
				g = null;

				//If there are frames being recieved
				if(framesRecieved.size() != 0) {
					
					fr = framesRecieved.get(0);
					msg("Recieved Frame: " + fr.toString());

					//Error check before any processing is done
					//Use the checksum/CRC
					if(fr.genCrc() != fr.getSize()) {
						//Remove the frame from the queue. Don't need to send anything back, just don't send an ack.
						framesRecieved.remove(fr);
						System.out.println("Frame was erroneous"); //This line is temporary and should be updated
					}
					else {
						System.out.println("Frame was not erroneous");
						String[] tmp = fr.getSrc().split(",");

						int srcSwitch = Integer.parseInt(tmp[0].substring(1));
						int srcNode = Integer.parseInt(tmp[1].substring(0, tmp[1].length() - 1));

						if (fr.getAcktype() == 2) {
							msg("(" + srcSwitch + "," + srcNode + ")" + " Has been firewalled");
						}

						g = Integer.toString(srcSwitch) + "_" + Integer.toString(srcNode) + "," + fr.getData();
						//msg("Writing: " + g);
						writeToTxt(g);

						framesRecieved.remove(fr);
					}
					
				}
				else {
					msg("no frames to process waiting...");
					Thread.sleep(1300);
				}
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
		}catch(Throwable e) {
			msgPort("ERROR -> " + e.toString(), this.Port);
			//e.printStackTrace();
		}
		
		
		//allow 0.05 failure, needs ACK and CRC system functional
		/*if (Main.rand.nextFloat() < 0.05) {
			msg("Corrupting data...");
			frame.data = frame.data.substring(0, frame.data.length()/2);
		}
		if (Main.rand.nextFloat() < 0.05) {
			msg("Losing frame...");
			continue;
		}*/
		
	}
	
	//Create output files for corresponding node, make directory if it doesn't exist
	//working correctly
	private void prepOutputFiles() {
		String dir = "./nodes/output";
		try {
			mainfile = new File("./nodes/output/node"+ this.switchIdentification + "_" + this.identificationNumber + "output.txt");
			directory = new File(dir);
				
			//create the file if it doesn't exist
			if(directory.mkdir()) {
				msg("directory: \"" + dir +"\" didn't exist so I made it :)");
			}
			if(!mainfile.exists()) {	
				msg("Creating Output File");
				mainfile.createNewFile();
			}	
		}catch(Throwable e) {
			System.out.println("ERROR: create output file failed:" + e.toString());
		}		
	}

	/**
	 * Writes a single string to the output file for this node
	 * @param str the string to be written to the output file
	 */
	public synchronized void writeToTxt(String str) {
		try {
			BufferedWriter txtWriter = new BufferedWriter(new FileWriter(new File("./nodes/output/node"+ this.switchIdentification + "_" + this.identificationNumber +"output.txt"), true));
			txtWriter.write(str);
			txtWriter.newLine();
			txtWriter.close();
		} catch (IOException e) {
			msg("Failed to write to output txt file");
			e.printStackTrace();
		}
	}

	/**
	 * Reads in input files for this node.
	 */
	private synchronized void readInputFiles() {
		//Variables
		BufferedReader br;
		String inputLine;
		String data;
		int switch_dest = 0;
		int node_dest = 0;
		
		framesToSend = new ArrayList<Frame>();

		try {	
			msg("Reading input files and storing the information...");
			in_file = new File("./nodes/node" + this.switchIdentification + "_" + this.identificationNumber + ".txt");
			br = new BufferedReader(new FileReader(in_file));

			//Walk through the entire input file
			while((inputLine = br.readLine()) != null) {
				//Get the destination switch, which is always the first number (character) in the string
				int dstSwitch = Integer.parseInt(inputLine.substring(0, 1));
				//msg("DSTSWITCH: " + dstSwitch);

				//Get the destination node, which is always the third character in the string
				int dstNode = Integer.parseInt(inputLine.substring(2, 3));
				//msg("DSTNode: " + dstNode);

				if(inputLine.substring(4).length() == 1) {
					data = ""; //there are some cases where the generator would give blank data, this fixes it
				}
				else {
					//The comma is at index 3. The real data starts at index 4.
					data = inputLine.substring(4);
				}
				
				String src_string = "(" + this.switchIdentification + "," + this.identificationNumber + ")";
				String dst_string = "(" + dstSwitch + "," + dstNode + ")";
				
				Frame fr = new Frame(); //there are multiple way to reconstruct the frame
				//new Frame(bytes of frame), reconstructs frame from bytes
				//new Frame(string generated by frame), also reconstructs
				//new Frame(), like how I did here...put everything in yourself
				
				fr.setIsEmpty(0); //be sure to set this to 0 when filling frame
				//this makes for an easier ACK setup
				
				fr.setSrc(src_string);
				//(switchnumber, this node number)
					
				fr.setDst(dst_string);
				//(destinationswitch, destination node)
				
				fr.setSize(data.length());
				//if 0 then frame is an ACK frame
				
				fr.setAcktype(3); 
				//0 no response in time out (resend again).
				//1 CRC error (resend again).
				//2 firewalled (no need to resend).
				//3 positive ACK. (successful delivery)
				
				fr.setData(data); //obvious so here's a joke/quote
				//"Be the change you wish to see in the world"
				//--50 cent
				
				fr.setCrc(fr.genCrc());
				//sum of byte values of the frame.
				//a checksum for destination node to check if data
				//arrived intact :)

				//Testing Frame Strings
				//msg(fr.toString());
				//msg(fr.getBytes().toString());
				//Frame fra = new Frame(fr.getBytes());
				//msg("PIMPSWAG: " + fra.toString());
				//Frame fr = new Frame(node_num, dest, data);
				
				framesToSend.add(fr);
				
				//all this is assembled right btw...
				this.numofLines++;
			}
			
			if(Main.numOfLines == 0) {
				Main.numOfLines = this.numofLines;
			}
			
			br.close();
			
		}catch (Exception e) {
			msg("ERROR READING IN FILE --> " + e.toString());
			e.printStackTrace();
		}
	}

	//Getters
	public int getPort() {
		return this.Port;
	}
	public int getIdentificationNumber() {
		return identificationNumber;
	}
	public int getSwitchIdentification() {
		return switchIdentification;
	}

	//Method for printing out to STDOut cleanly with nice formatting
	private void msg (String input) {
		System.out.println("\t\t(" + (this.switchIdentification) + "," + this.identificationNumber +"): "
				+ "Switch #" + this.switchIdentification + ": ---> Node#" + this.identificationNumber + ": " + input);
	}
	
	private void msgPort(String input, int port) {
		System.out.println("\t\t(" + (this.switchIdentification) + "," + this.identificationNumber +"): " 
				+ "Switch #" + this.switchIdentification + ": ---> Node#" + this.identificationNumber + " ERROR: Port# " + this.Port + "-->" + input);
	}

	public void addFrame(Frame fr) {
		this.framesRecieved.add(fr);
		
	}
}
