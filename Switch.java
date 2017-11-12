package networks.project2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;


public class Switch extends Thread {
    public static int port = 50000;         //hand out ports at 50,000
    public int serverPort;                  //port for serverSocket
    public static int nodes = 0;
    public static ArrayList<Integer> switchingTable;
    public static ArrayList<Frame> frames;
    public static Boolean terminate = false;
    public static int sleep = 100;
    public static int processedFrames = 0;
    public static ReentrantLock lock = new ReentrantLock();

    /*
     * Switch Constructor
     * @serverPort is the port the switch should listen to
     * @nodes
     */
    public Switch(int serverPort, int nodes) {
        this.serverPort = serverPort;
        this.nodes = nodes;
        frames = new ArrayList<Frame>();
    }

    /*
     * Close any sockets or pipes
     */
    public void close() {
        terminate = true;
    }

    /*
     * Track which Ports go to what Nodes
     * @nodeNum is the port the switch should listen to
     */
    public static int Port(int node) {
        lock.lock();
        int assignPortNum = 0;
        try {
            if (switchingTable == null) {
                switchingTable = new ArrayList<Integer>();
                for (int i = 0; i < 256; i++) switchingTable.add(-1);
            }
            assignPortNum = Switch.port;
            Switch.port++;
            System.out.println("Port: " + assignPortNum + " goes to" + node);
            switchingTable.set(node, assignPortNum);
        } finally {
            lock.unlock();
        }
        return assignPortNum;
    }


    public void run() {
        System.out.println("Start");

        ListenerThread listen = new ListenerThread(serverPort);
        listen.start();

        Frame frame = null;

        try {
            for (;;) {
                if (terminate) return;
                if (frames.size() == 0) {
                    System.out.println("Waiting to process frames.");
                    Thread.sleep(sleep);
                    continue;
                }

                frame = frames.get(frames.size() - 1);

                if (switchingTable == null) {
                    switchingTable = new ArrayList<Integer>();
                    for (int i = 0; i < 256; i++) switchingTable.add(-1);
                }

                int nodePort = -1;

                /*
                ******* Need to handle the node port number, i.e. if location is not known ******
                */
                
                System.out.println("Node " +frame.getDest()+ "connects to port " +nodePort+".");

                Socket socket;
                try {
                    socket = new Socket("Localhost:", nodePort);
                }catch (Throwable e) {
                    Collections.swap(frames, 0, frames.size() - 1);
                    System.out.println("Node " +frame.getDest()+ "failed to connect to port " +nodePort+".");
                    Thread.sleep(sleep);
                    continue;
                }

                System.out.println("Connection to node successful, now transmitting. ");

                PrintWriter pipe = new PrintWriter(socket.getOutputStream(), true);
                pipe.println(frame.toString());
                pipe.close();
                socket.close();
                frames.remove(frame);
                processedFrames++;
                //Check to see how many frames have been processed
                System.out.println(+processedFrames+ " frames processed so far");

            }
        } catch (Throwable e) {
            System.out.println("Frame: " +frame);
            System.out.println("Table: " +switchingTable);
            System.out.println("Error:"+e.toString());
        }

    }


    /*
     * Establish a connection
     */
    class ListenerThread extends Thread {
        public int serverPort;

        public ListenerThread(int serverPort) {
            this.serverPort = serverPort;
        }

        public void run() {
            try {
                System.out.println("Running");
                Socket socket;
                ServerSocket listener = new ServerSocket(serverPort);

                for (;;) {
                    if (Switch.terminate) return;
                    System.out.println("Trying to establish a connection.");
                    socket = listener.accept();

                    System.out.println("Connection established.");
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );

                    for (;;) {
                        String data = input.readLine();
                        if (data.equals("terminate")) break;

                        Frame frame = new Frame(data);
                        Switch.frames.add(frame);
                    }
                }
            } catch (Throwable e) {
                System.out.println("Warning:" + e.toString());
            }
        }
    }
}
