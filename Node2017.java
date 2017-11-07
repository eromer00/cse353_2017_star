import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Node2017 extends Thread {

    ArrayList<String> outdata = new ArrayList<>();
    private int portnum;
    private int nodenum;

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
            System.out.println("Error setting up node " + nodenum);
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

        NodeSend a = new NodeSend(outdata);
        NodeReceive b = new NodeReceive();

        a.run();
        b.run();
    }




    public void setPortnum(int x) {
        this.portnum = x;
    }

    public void setNodenum(int x) {
        this.nodenum = x;
    }
}

class NodeSend extends Thread {

    private ArrayList<String> frames;

    public NodeSend(ArrayList<String> x) {
        this.frames = x;
    }

    @Override
    public void run() {
        while(!frames.isEmpty()) {
            System.out.println(frames.get(0));
            frames.remove(0);
        }
    }
}

class NodeReceive extends Thread {

    @Override
    public void run() {
        for(int i = 0; i < 5; i++) {
            System.out.println("This node is receiving: " + i);
        }
    }

}
