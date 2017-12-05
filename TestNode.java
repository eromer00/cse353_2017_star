import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Is supposed to interact with RelayNode.java and MonitorNode.java
 */
public class TestNode {
    private ServerSocket inputSocket;
    private List<Socket> outputSockets;
    private Socket clientSocket;
    private int timeOutPeriod;

    /**
     * Default constructor for a Node
     */
    protected TestNode() {
        super();
        this.outputSockets = new ArrayList<Socket>();
    }

    /**
     * This constructor allows for the specification of the Node's name.
     * @param NodeName The Node Name
     */
    protected TestNode(int NodeName, int timeOutPeriod) {
        //System.out.println("Creating Node: " + NodeName);
        this.timeOutPeriod = timeOutPeriod;
        this.outputSockets = new ArrayList<Socket>();
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
    public Frame readSocket() throws SocketTimeoutException{
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
            System.out.println("Node " + identifcationNumber() + ": Corrupt Frame Detected");
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
