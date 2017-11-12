import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;


public class Switch implements Runnable {
    public static int port = 50001;         //hand out ports at 50,000
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


        int i;
        ArrayList<ListenerThread> servers = new ArrayList<>();
        for(i = 0; i <= this.nodes; i++) {
            servers.add(new ListenerThread(this.serverPort + i));
        }

        for(i = 0; i < servers.size(); i++) {
            servers.get(i).run();
        }


        Frame frame = null;

        System.out.println("test a");
        try {
            for (;;) {
                if (terminate) return;
                if (frames.size() == 0) {
                    System.out.println("Waiting to process frames.");
                    Thread.sleep(sleep);
                    continue;
                }

                frame = frames.get(0);

                if (switchingTable == null) {
                    switchingTable = new ArrayList<Integer>();
                    for (i = 0; i < 256; i++) switchingTable.add(-1);
                }

                int nodePort = -1;
                if(switchingTable.get(frame.getDest()) != -1) nodePort = switchingTable.get(frame.getDest());


                //Need to handle the node port number, i.e. if location is not known

                System.out.println("Node " +frame.getDest()+ "connects to port " +nodePort+".");
                for(i = 0; i < servers.size(); i++) {
                    if(servers.get(i).getServerPort() == nodePort) {
                        Socket tmp = servers.get(i).getSocket();
                        PrintWriter pw = new PrintWriter(tmp.getOutputStream());
                        pw.println(frame.toBinaryString());
                        frames.remove(0);
                        processedFrames++;
                        //Check to see how many frames have been processed
                        System.out.println(+processedFrames+ " frames processed so far");
                        pw.close();
                    }
                }



            }
        } catch (Throwable e) {
            System.out.println("Frame: " +frame);
            System.out.println("Table: " +switchingTable);
            System.out.println("Error:"+e.toString());
        }
    }
    /*
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


                //Need to handle the node port number, i.e. if location is not known

                */
                /*
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
    */

    /*
     * Establish a connection
     */
    class ListenerThread extends Thread {
        private int serverPort;
        private Socket socket;
        private boolean done = false;

        public ListenerThread(int serverPort) {
            this.serverPort = serverPort;
        }

        public Socket getSocket() {
            return this.socket;
        }

        public int getServerPort() {
            return this.serverPort;
        }

        public boolean isDone() {
            return this.done;
        }

        public void run() {
            try {
                System.out.println("Running with port " + this.serverPort);
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
                        if(this.done = true) continue;

                        String data = input.readLine();
                        if (data.equals("terminate")) {
                            this.done = true;
                            continue;
                        }

                        Frame frame = new Frame(data);

                        boolean inTable = false;
                        if(Switch.switchingTable != null) {
                            for (int i = 0; i < Switch.switchingTable.size(); i++) {
                                if (Switch.switchingTable.get(frame.getSrc()) != -1) {
                                    inTable = true;
                                    break;
                                }
                            }
                            if (!inTable) {
                                Switch.Port(frame.getSrc());
                                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                                pw.println(new Frame(5, 0, String.valueOf(Switch.switchingTable.get(frame.getSrc()))).toBinaryString());
                                pw.close();
                            }
                        }
                        Switch.frames.add(frame);
                    }
                }
            } catch (Throwable e) {
                System.out.println("Warning:" + e.toString());
            }
        }
    }
}
