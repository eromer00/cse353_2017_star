import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Random;


/**
 * Relays data between Nodes
 */
public class RelayNode extends Node implements Runnable {

     private List<Frame> frameBuffer;
    private List<Frame> waitingFrames;
    private boolean sentComplete;
    private PrintWriter outputFile;
    private BufferedReader inputFile;
    private int THT;

    public RelayNode(int identification, int switchIdentification, CASSwitch switchReference, int switchPort) {
        super(identification, switchIdentification, switchReference, switchPort);
    }

    /**
     * This method represents the Listen state of the Node.
     * @return Returns 0 upon reception of a Token Frame, or 1 upon Kill Signal
     */
    public Frame Listen() {
        Frame inputFrame;

         while(true) {
            //Read from input socket a new frame
            try {
                inputFrame = readSocket();
            } catch (SocketTimeoutException e) {
                System.out.println("Node " + this.getIdentificationNumber() + " timed out...");
                continue;
            }
            //Check to see if the incoming frame is empty
            if (inputFrame == null) {
                //Bad frame
                continue;
            }
            //Check to see if frame is good
            //This should pass any corrupt frames
            if (!MonitorNode.isGoodFrame(inputFrame)) {
                System.out.println("Node " + this.getIdentificationNumber() + " found a bad frame!");
                writeToSocket(inputFrame);
                continue;
            }
            //Check to see if Frame is Kill Signal
            if (inputFrame.getFrameStatus() == 4) {
                //Kill Network Signal has been received
                writeToSocket(inputFrame); //Pass Kill Signal
                return null;
            }

            //If Frame was intended for this Node
            if (inputFrame.getDst() == this.getIdentificationNumber() && inputFrame.getFrameStatus() == 0) {
                //Frame has reached its dst
                inputFrame.getFrameStatus();
                //Determine if frame needs to be received or rejected
                if(inputFrame.getFrameStatus() == 3) {
                    //Reject the Frame
                    System.out.println("Node " + this.getIdentificationNumber() + " Rejecting Frame...");
                    inputFrame.zeroMonitorBit();
                    writeToSocket(inputFrame);
                    continue;
                } else if (inputFrame.getFrameStatus() == 2) {
                    inputFrame.zeroMonitorBit();
                    writeToSocket(inputFrame); //Pass Frame to return back to Sender
                }
            }
            //Check to see if Frame was rejected
            this.waitingFrames.remove(inputFrame); //Frame has not been lost in the network
            if (inputFrame.getSrc() == this.getIdentificationNumber()) {
                if (inputFrame.getFrameStatus() == 3) {
                    //Frame was rejected
                    inputFrame.setFrameStatus((byte) 0);
                    inputFrame.zeroMonitorBit();
                    this.frameBuffer.add(inputFrame);
                    continue;
                }
                //Check to see if Frame was accepted
                if (inputFrame.getFrameStatus() == 2) {
                    Random rand = new Random();
                    if (rand.nextInt(100) < 0) {
                        writeToSocket(inputFrame); //Create Orphan Frame
                    }
                    continue;
                }

            }
            //Check if Frame is Token
            if(inputFrame.isToken()) {
                return inputFrame; //Go to Transmit State
            }
            writeToSocket(inputFrame);
        }
    }

    /**
     * This method represents the transmission state of the Node. 
     */
    public int Transmit(Frame token){
        int currentTHT = 0;
        Frame currentFrame;
        String buffer = null;
        //While the THT has not been surpassed
        while (currentTHT < this.THT) {
            try {
                //Retransmit any frames that have failed to reach their destinations
                if (!this.frameBuffer.isEmpty()){
                    //Frames in buffer for retransmission
                    currentFrame = this.frameBuffer.remove(0);
                    currentTHT += currentFrame.getSize();
                    writeToSocket(currentFrame.corruptFrame());
                    this.waitingFrames.add(currentFrame);
                    continue;
                }
                //If no frames need retransmission, continue reading input file
                if (this.frameBuffer.isEmpty()) {
                    buffer = this.inputFile.readLine();
                    //Check see if at EOF
                    if (buffer == null) {
                        //Node has successfully transmitted all of its data
                        if (this.sentComplete == false && this.waitingFrames.isEmpty()) {
                            //Notify monitor of completion
                            this.sentComplete = true;
                        }
                        //No more transmission needed
                        if (!this.waitingFrames.isEmpty())
                            token.setFinishedBit();
                        writeToSocket(token);
                        return 1;
                    }
                    //Transmit the next line in the file
                    currentFrame = new Frame(buffer, (byte) this.getIdentificationNumber());
                    currentTHT += currentFrame.getSize();
                    writeToSocket(currentFrame.corruptFrame());
                    this.waitingFrames.add(currentFrame);
                    continue;
                }
            } catch (IOException e) {
                //Either no data file or no data to transmit
                System.out.println("No data to transmit");
                writeToSocket(Frame.generateToken()); //Pass the Token
                return 1;
            }
        }
        Random rand = new Random();
        //5% chance of failing to transmit the token
        if (rand.nextInt(100) < 100) {
            token.setFinishedBit();
            writeToSocket(token); //Pass the Token
        }
//		else
//			System.out.println("Node " + this.getIdentificationNumber() + " lost the token!");
        return 0;
    }

    /**
     * This functions is implementing the required Runnable method
     */
    @Override
    public void run() {
        Frame token;
        this.run();
        if (this.getIdentificationNumber() == 1) {
            //Node 1 generates the token and passes it to the neighboring node
            writeToSocket(Frame.generateToken());
        }
        while(true) {
            token = Listen();
            if (token == null) {
                //Kill Signal has been received
                //System.out.println("Node: " + this.getIdentificationNumber() + " has received Kill Sig");
                this.closeNode();
                this.outputFile.close();
                return;
            }
            //Any frames that have not been ACK are added for retransmission
            this.frameBuffer.addAll(waitingFrames);
            this.waitingFrames.clear();
            Transmit(token);
        }
    }
}

