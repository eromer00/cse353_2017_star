import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class monitors the node behavior
 */
public class MonitorNode extends Node implements Runnable {

    private Map<Integer, Integer> myNetwork;
    private InetAddress myAddress;
    private int port;

    public MonitorNode(int identification, int switchIdentification, CASSwitch switchReference, int switchPort) {
        super(identification, switchIdentification, switchReference, switchPort);
        this.myNetwork = new HashMap<Integer, Integer>();
    }


    /**
     * Places each node into myNetwork. This is to keep track of who has completed their transfers.
     * @param myNetwork List of network number and node number in the Network
     */
    public void placeNetwork(List<RelayNode> myNetwork) {
        for (RelayNode node : myNetwork) {
            this.myNetwork.put(this.getSwitchIdentification(), this.getIdentificationNumber());
        }
    }


    /**
     * This method monitors the Star Network for Corrupt and Lost Frames. Lost tokens are handled
     * by the enclosing method.
     */
    private int MonitorNetwork() {
        Frame inputFrame = null;
        try {
            inputFrame = readSocket();
            //Check for Corrupt Frame
            if (!isGoodFrame(inputFrame)) {
                //Frame is Corrupt
                if (inputFrame == null) {
                    writeToSocket(Frame.generateToken());
                    return 0;
                }
                this.drainInputSocket();
                writeToSocket(Frame.generateToken());
                return 0;
            }
            //Check for Lost Frame
            if (!inputFrame.isToken() && inputFrame.monitorBit()) {
                System.out.println("MONITOR NODE: Removing orphan frames.");
                inputFrame = null; //'Drain' the frame
                return 0;
            } else if (!inputFrame.isToken()) {
                //This is the Frame's first encounter with Monitor
                //Set the monitor bit
                inputFrame.setMonitorBit();
                writeToSocket(inputFrame);
                return 0;
            }
            //Check for Transmission Completed Signals
            if (inputFrame.isToken() && inputFrame.getFrameStatus() == 0) {
                writeToSocket(Frame.generateKillSig());
                return 0;
            }
            //Check for Kill Signal
            if (inputFrame.getFrameStatus() == 4) {
                this.closeNode();
                return 1;
            }
            //Check for token
            if (inputFrame.isToken()) {
                writeToSocket(Frame.generateToken());
                return 0;
            }
            writeToSocket(inputFrame);
            return 0;

        } catch (SocketTimeoutException e) {
            System.out.println("MONITOR NODE: Timeout");
            writeToSocket(Frame.generateToken());
            return 0;
        }
    }

    /**
     * This method checks if it's a good Frame or not.
     * @param frame Frame to be checked.
     * @return If true Frame is good, if False Frame is corrupt.
     */
    public static boolean isGoodFrame(Frame frame) {
        //Check Frame Control
        try {
            if (frame.getFrameControl() > 1) {
                //System.out.println("FRAME Error: Frame out of bounds: " + frame.getFrameControl());
                return false; //Values above 5 on Frame Control are not accepted
            }
            //Check if Data Length is correct
            int frameLength = frame.getFrame().length;
            int dataSize = frame.getSize();
            //System.out.println("\tFrame Length  " + frameLength + " Frame Data Size + 6: " + (dataSize + 6));
            if (frameLength != dataSize + 6) {
                //System.out.println("FRAME Error: Incorrect Data Size");
                return false;
            }
            if (frame.getFrameStatus() > 0x40) {
                //sSystem.out.println("FRAME Error: Incorrect Frame Status Byte");
                return false;
            }
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Returns the Port this node is listening to.
     * @return The Port Number.
     */
    public int getPort() {
        return this.port;
    }

    @Override
    public void run(){
        //System.out.println("Initialized Monitor Node");
        long simTime;
        simTime = System.currentTimeMillis();
        this.acceptClient();
        while (true) {
            if(MonitorNetwork() == 1) {
                System.out.println(((System.currentTimeMillis() - simTime) / 1000));
                return;
            }
        }
    }
}
