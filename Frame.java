

public class Frame {

    private int status = 0;
    private String binaryframe = null;

    private int src;
    private int dest;
    private int size;
    private String data;

    /*
        Constructor for creation/encryption of a frame
     */
    public Frame(int src, int dest, String data) {
        this.src = src;
        this.dest = dest;
        this.data = data;

        int size;
        byte[] bits = data.getBytes();
        size = bits.length;
        this.size = size;
        setStatus(src, dest, size);

        StringBuilder tmp = new StringBuilder();
        tmp.append(toBinary(String.valueOf(src)));
        tmp.append(toBinary(String.valueOf(dest)));
        tmp.append(toBinary(String.valueOf(size)));
        tmp.append(toBinary(data));
        binaryframe = tmp.toString();
    }

    public Frame(String binstring) {
        this.binaryframe = binstring;

        //extract/decrypt src
        int tmp = Integer.parseInt(binstring.substring(0,8), 2);
        setSrc((char)tmp);

        //extract/decrypt dest
        tmp = Integer.parseInt(binstring.substring(8,16), 2);
        setDest((char)tmp);

        //extract/decrypt size
        tmp = Integer.parseInt(binstring.substring(16, 24), 2);
        setSize((char)tmp);

        //extract/decrypt data
        String data = binstring.substring(24);
        setData(data);

        //determine status
        setStatus(getSrc(), getDest(), getSize());

    }

    private void setSrc(char x) {
        this.src = Integer.parseInt(Character.toString(x));
    }

    private void setDest(char x) {
        this.dest = Integer.parseInt(Character.toString(x));
    }

    private void setSize(char x) {
        this.size = Integer.parseInt(Character.toString(x));
    }

    private void setData(String x) {
        String str = "";
        //code taken from: https://stackoverflow.com/questions/8634527/converting-binary-data-to-characters-in-java
        for (int i = 0; i < x.length()/8; i++) {
            int a = Integer.parseInt(x.substring(8*i,(i+1)*8),2);
            str += (char)(a);
        }
        this.data = str;
    }

    public int getSrc() {
        return src;
    }

    public int getDest() {
        return dest;
    }

    public int getSize() {
        return size;
    }

    public String getData() {
        return data;
    }


    private static String toBinary(String a) {
        byte []b = a.getBytes();
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < b.length; i++) {
            int c = b[i];
            for(int j = 0; j < 8; j++) {
                str.append((c & 128) == 0 ? 0 : 1);
                c <<= 1;
            }
        }

        return str.toString();
    }


    /* Determines type of frame being constructed, used for creation/encryption frame
     * key:
     * 0 - regular data
     * 1 - ACK frame
     * 2 - flood frame
     * 3 - flood ACK frame
     * 4 - bad frame (used for error checking)
     */
    private void setStatus(int src, int dest, int size){
        //if size == 0, this is an ACK frame
        if(size == 0 && (src != 0 && dest != 0)) {
            this.status = 1;
        }
        //if src == 0, this is a flood frame from the switch
        else if(src == 0 && (dest != 0 && size != 0)) {
            this.status = 2;
        }
        //if dest == 0, this is a flood response frame to the switch
        else if(dest == 0 && (src != 0 && size != 0)) {
            this.status = 3;
        }
        //regular frame, should be default
        else if(src != 0 && dest != 0 && size != 0) {
            this.status = 0;
        }
        //bad frame, should not occur
        else {
            this.status = 4;
        }
    }

    public String toBinaryString() {
        if(getStatus() != 4) {
            return binaryframe;
        }
        return "bad frame";
    }

    public int getStatus() {
        return status;
    }
}
