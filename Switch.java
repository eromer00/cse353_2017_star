public class Switch {
    public static int port = 50000;         //hand out ports at 50,000
    public int serverPort;                  //port for serverSocket
    public static int nodes = 0;
    public static ArrayList<Integer> switchingTable;
    public static ArrayList<Frame> frames;
    public static Boolean terminate = false;
    public static int sleep = 100;
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



    /* Need to implement

    public void run() {

    From Frame Class
    Determines type of frame being constructed, used for creation/encryption frame
     * key:
     * 0 - regular data
     * 1 - ACK frame
     * 2 - flood frame
     * 3 - flood ACK frame
     * 4 - bad frame (used for error checking)


    } */


    /*
     * Establish a connection to the switch
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
