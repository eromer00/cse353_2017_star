//package starofstars;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Random;

public class Node implements Runnable {

	private int tracker = 0;
	private int Port = 0;
	public int identificationNumber;
	public int switchIdentification;    public int switchPort;
	public int random = new Random().nextInt(20);
	private ServerSocket inputSocket;
	private List<Socket> outputSockets;
	private Socket clientSocket;
	private int timeOutPeriod;

	
	private CASSwitch switchReference;

	private File directory;
	private File mainfile;
	private File in_file;
	
	private ArrayList<Frame> framesToSend;
	private ArrayList<Frame> framesRecieved = new ArrayList<Frame>();
	private Socket socket = null;
	private ArrayList<Frame> buf = new ArrayList<>();


	private int numofLines = 0;
	
	public boolean Terminate = false;

	
	public Node(int identification, int switchIdentification, CASSwitch switchReference, int switchPort) {
		this.identificationNumber = identification; //hold the node number make it global to object
		this.switchIdentification = switchIdentification;
		this.switchReference = switchReference;
		this.switchPort = switchPort;
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
	public void run() { //when ran by the executor, it will send the arraylist of frames on their way, each as a series of bytes
						//which the switch will handle (can be converted back using the frame class)
		
		
		
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


			for(int i = 0; i < framesToSend.size(); i++) {
				Frame fr = framesToSend.get(i);
				msg("sending: " + fr.getBinaryString());

				writer.println(fr.getBinaryString()); //uses a string at the moment, find a way to
				//convert frame to bytes and bytes back to frames while not breaking the 
				//TERMINATE logic that the listener look for
				//it was faster to implement it with strings honestly

				Thread.sleep(50);
			}
			for(int i = 0; i < framesToSend.size(); i++) {
				Frame fr = framesToSend.get(i);
				msg("sending: " + fr.getBinaryString());

				writer.println(fr.getBinaryString()); //uses a string at the moment, find a way to
				//convert frame to bytes and bytes back to frames while not breaking the
				//TERMINATE logic that the listener look for
				//it was faster to implement it with strings honestly

				Thread.sleep(50);
			}
			writer.println("TERMINATE"); //so the CASSwitch will know to stop listening to this particular node

			Frame fr = null;
			String g = null;
			int k = 0;
			int timer = 0;

			while(true) {
				timer++;
				fr = null;
				g = null;
				int x = 0;
				/*
				if(!framesToSend.isEmpty()) {
					if(buf.isEmpty()) {
						buf.add(framesToSend.get(0));
						writer.println(framesToSend.get(0).getBinaryString());
						framesToSend.remove(0);
					}
				} */

				if(framesRecieved.size() != 0) {

					fr = framesRecieved.get(0);
					msg("Recieved Frame: " + fr.getBinaryString());



					//String[] tmp = fr.getSrce().split(",");
					
					int srcSwitch = fr.getSSrc();
					int srcNode = fr.getSrc();

					if(fr.parseData().equals("10") && fr.getACK()) {
						msg("(" + srcSwitch + "," + srcNode + ")" + " Has been firewalled");
					} else if(fr.parseData().equals("11") && fr.getACK()) {
						msg("(" + srcSwitch + "," + srcNode + ")" + " successful acknowledgement");
					} else if(fr.parseData().equals("01") && fr.getACK()) {
						msg("(" + srcSwitch + "," + srcNode + ")" + " CRC error, resending");
						//buf.clear();
						for(int i = 0; i < framesToSend.size(); i++) {
							Frame frm = framesToSend.get(i);
							msg("sending: " + frm.getBinaryString());
							writer.println(frm.getBinaryString());
							Thread.sleep(250);
						}
					} else if(fr.parseData().equals("00") && fr.getACK()) {
						msg("(" + srcSwitch + "," + srcNode + ")" + " timeout, resend");
						//buf.clear();
						for(int i = 0; i < framesToSend.size(); i++) {
							Frame frm = framesToSend.get(i);
							msg("sending: " + frm.getBinaryString());
							writer.println(frm.getBinaryString());
							Thread.sleep(250);
						}
					}

					g = Integer.toString(srcSwitch) + "_" + Integer.toString(srcNode) + "," + fr.parseData();
					//msg("Writing: " + g);
					writeToTxt(g);
					framesRecieved.remove(fr);
					Thread.sleep(500);
				}
				else {
					msg("no frames to process waiting...");
					Thread.sleep(500);
					/*if(this.tracker > (Main.numOfLines / 2) - 20) {
						this.Terminate = true;
						this.socket.close();
						
						return;
					}
					tracker++;*/
				}
				//then do something with the frames
				//k++;	
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(timer > 100) {
					break;
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
	
	//working correctly
	private synchronized void readInputFiles() {
		BufferedReader br = null;
		String temp = null;
		int switch_dest = 0, node_dest = 0; 
		
		framesToSend = new ArrayList<Frame>();
		if(random == 10) {
			//add erroneous frame
			framesToSend.add(new Frame("11111111111111111111111111111111111111111111111110101010"));
		}
		String data = null;
		try {	
			msg("Reading input files and storing the information...");
			in_file = new File("./nodes/node" + this.switchIdentification + "_" + this.identificationNumber + ".txt");
			br = new BufferedReader(new FileReader(in_file));
			
			while((temp = br.readLine()) != null) {
				String[] tmp = temp.split("_");
				String[] tmp2 = temp.split(",");
				String[] tmp3 = tmp[1].split(",");
				
				int dstSwitch = Integer.parseInt(tmp[0].substring(0));
				//msg("DSTSWITCH: " + dstSwitch);
				
				int dstNode = Integer.parseInt(tmp3[0]);
				//msg("DSTNode: " + dstNode);
				
				//switch_dest = Integer.parseInt(tmp[0].toString());
				//node_dest = Character.getNumericValue(tmp[1].charAt(0));
				if(tmp2.length == 1) {
					data = ""; //there are some cases where the generator would give blank data, this fixes it
				}
				else {
					data = tmp2[1];		
				}
				
				//
				// String src_string = "(" + this.switchIdentification + "," + this.identificationNumber + ")";
				//String dst_string = "(" + dstSwitch + "," + dstNode + ")";
				Frame fr = new Frame(1, String.valueOf(this.identificationNumber), String.valueOf(this.switchIdentification),
						tmp3[0], tmp[0].substring(0),
						data, false);
				/*
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
				*/
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
	
	public int getPort() {
		return this.Port;
	}
	
	public int getIdentificationNumber() {
		return identificationNumber;
	}

	public int getSwitchIdentification() {
		return switchIdentification;
	}

	//make reporting soooooo much nicer
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




	/**
	 * Should kill the connection to the server and close all streams and sockets.
	 * @throws IOException
	 */

	public void killServerConnection() throws IOException {
		Socket outputSocket = this.outputSockets.get(0);
		if(outputSocket.isClosed()) {
			System.err.println(this.identificationNumber + ": Error! Could not close Socket: Socket is already closed...");
		} else {
			if(outputSocket.isConnected()) {
				outputSocket.shutdownOutput();
			}
			outputSocket.close();
		}
	}

	/**
	 * This method 'closes' the Node by closing all used resources
	 */

	public void closeNode() {
		Socket outputSocket = this.outputSockets.get(0);
		if(!outputSocket.isClosed()) {
			try {
				outputSocket.shutdownOutput();
				this.inputSocket.close();
			} catch (IOException e) {
				System.err.println("Severe error, Node" + this.identificationNumber + " could not properly close sockets");
			}
		}
	}


	/**
	 * This method listens to the first inputSocket for accept calls.
	 * @return A socket to the connected client
	 */
	public void acceptClient() {
		if (!this.inputSocket.isBound()) {
			System.err.println(identificationNumber + " Call not accepted, sockets don't exist!");
			return;
		}
		this.clientSocket = new Socket();
		while(!this.clientSocket.isBound()) {
			try {
				//	System.out.println("Node " + identificationNumber + " listening to connection requests...");
				clientSocket = this.inputSocket.accept();
				//System.out.println("Node " + identificationNumber + ": Client accepted on Socket: " + clientSocket.toString());
				//Connection established
				return;
			} catch (SocketTimeoutException T){
				System.out.println("Server listen timeout...");
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		return;
	}

	/**
	 * This method returns a read Frame from the socket
	 * @return The received Frame
	 */
	public Frame readSocket() throws SocketTimeoutException {
		DataInputStream input = null;
		//System.out.println("Node " + identification + " is now Reading...");
		try {
			//System.out.println("\tNode " + identificationNumber() + " attempting to accept socket...");
			clientSocket.setSoTimeout(this.timeOutPeriod);
			input = new DataInputStream(this.clientSocket.getInputStream());
			//System.out.println("\tNode " + identificationNumber() + " got signal");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		Frame frame = null;
		//Create buffer of max possible frame size
		byte[] header = new byte[5];
		byte[] buffer = null;
		byte[] lastFrame = null;
		int dataSize;
		int bytesRead;
		try {
			//Read in header
			input.read(header, 0, 5);
			dataSize = (header[4] & 0xff);
			buffer = new byte[dataSize + 1];
			bytesRead = input.read(buffer, 0, dataSize + 1);
			lastFrame = new byte[dataSize + 6];
			for (int i = 0; i < bytesRead + 5; i++) {
				if (i < 5) {
					lastFrame[i] = header[i];
				} else if (i < bytesRead + 5) {
					//Read in data
					lastFrame[i] = buffer[i - 5];
				}
			}
			frame = new Frame(lastFrame);
		} catch(EOFException eof) {
			//Corrupt Frame Detected
			System.out.println("Node " + identificationNumber + ": Corrupt Frame Detected");
			frame = new Frame(header);
		} catch (IOException e) {
			//System.out.println("\tBad Frame");
			return frame;
		}
		return frame;
	}

	/**
	 * Attempts to drain the input data stream of data.
	 */
	public void drainInputSocket() {
		DataInputStream input = null;
		//System.out.println("Attempting to drain socket....");
		try {
			input = new DataInputStream(this.clientSocket.getInputStream());
			System.out.println("Draining socket...");
			while(input.skip(1) != 0);
			System.out.println("Drained Socket...");
		} catch (EOFException e) {
			return;
		} catch (IOException e) {
			System.out.println("Could not drain socket");
			return;
		}
	}


	/**
	 * This method attempts to write a Frame to the output socket.
	 * @param frame The frame to be transmitted
	 * @return Returns 0 upon successful transmission
	 */
	public int writeToSocket(Frame frame) {
		Socket outputSocket = this.outputSockets.get(0);
		DataOutputStream outputStream;
		try {
			//Create output stream
			outputStream = new DataOutputStream(outputSocket.getOutputStream());
			//Send message
			//System.out.println("Node " + identificationNumber + " Output Frame: ");
			//System.out.println(frame.toString());
			outputStream.write(frame.getFrame());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error! Node: " + identificationNumber + " could not write to socket!");
			return 1;
		}
		return 0;
	}

}
