

public class testNode {
    public static void main(String[] args) {
        Node2017 anode = new Node2017("nodea.txt", 5000, 1);
        Node2017 bnode = new Node2017("nodeb.txt", 5001, 2);
        Node2017 cnode = new Node2017("nodec.txt", 5002, 3);

        anode.run();
        bnode.run();
        cnode.run();

        //test for github

        /*
        Frame a = new Frame(1, 2, "succ");
        Frame b = new Frame(0, 4, "ayy lmao");
        Frame c = new Frame(5, 0, "moira = opaf");
        Frame d = new Frame(3, 2, "");

        System.out.println(a.toBinaryString());
        System.out.println(b.toBinaryString());
        System.out.println(c.toBinaryString());
        System.out.println(d.toBinaryString());

        System.out.println(a.getStatus());
        System.out.println(b.getStatus());
        System.out.println(c.getStatus());
        System.out.println(d.getStatus());


        Frame e, f, g, h;
        e = new Frame("00110101001100000011000100110010011011010110111101101001011100100110000100100000001111010010000001101111011100000110000101100110");
        f = new Frame("001100110011001000110000");
        g = new Frame("0011000000110100001110000110000101111001011110010010000001101100011011010110000101101111");
        h = new Frame("00110001001100100011010001110011011101010110001101100011");

        System.out.println(e.getStatus() + ", " + e.getSrc() + e.getDest() + e.getSize() + e.getData());
        System.out.println(f.getStatus() + ", " + f.getSrc() + f.getDest() + f.getSize() + f.getData());
        System.out.println(g.getStatus() + ", " + g.getSrc() + g.getDest() + g.getSize() + g.getData());
        System.out.println(h.getStatus() + ", " + h.getSrc() + h.getDest() + h.getSize() + h.getData());
        */
    }
}
