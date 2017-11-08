import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Node2017 extends Thread {

    ArrayList<String> outdata = new ArrayList<>();
    private int portnum;
    private int nodenum;
    //private Socket sock;
    

    //read file, setup frame for all data to be sent (frame: [src][dest][size/ack][data])
    public Node2017(String filename, int portnum, int nodenum) {
        setPortnum(portnum);
        setNodenum(nodenum);
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String s;

            //read in lines and convert to padded binary string
            while((s = br.readLine()) != null) {
                String[] split = s.split(":");

                Frame a = new Frame(nodenum, Integer.parseInt(split[0]), split[1]);
                outdata.add(a.toBinaryString());

            }
        } catch (Exception e) {
            System.out.println("Error setting up node " + nodenum + "\nERROR: " + e);
        }


    }

    @Override
    public void run() {
        /*
        while(!outdata.isEmpty()) {
            System.out.println(outdata.get(0));
            outdata.remove(0);
        }
        */

        try {
            System.out.println("1");
            Socket sock = new Socket(InetAddress.getLocalHost(), getPortNum());

            System.out.println("2");
            //NodeReceive b = new NodeReceive(this.sock);

            System.out.println("3");
            //NodeSend a = new NodeSend(outdata);

            System.out.println("4");
            //a.run();

            System.out.println("5");
            //b.run();

            System.out.println("6");
            sock.close();
        } catch(IOException e) {
            System.out.println("Error starting node: " + e.getStackTrace());
        }

    }




    private void setPortnum(int x) {
        this.portnum = x;
    }

    private void setNodenum(int x) {
        this.nodenum = x;
    }

    public int getPortNum() { return portnum; }

    public int getNodenum() { return nodenum; }
}

class NodeSend extends Thread {

    private ArrayList<String> frames;

    public NodeSend(ArrayList<String> x) {
        this.frames = x;
    }

    @Override
    public void run() {

    }
}

class NodeReceive extends Thread {

    Socket sock;

    public NodeReceive(Socket sck) {
        this.sock = sck;
    }

    @Override
    public void run() {
        try {
            //placeholder
            int i = 0;
            sleep(1000);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
