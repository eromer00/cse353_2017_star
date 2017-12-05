package starofstars;

import java.nio.charset.StandardCharsets;


//Remastered Frame 2.0 class, everything works here...
public class Frame {

	private int size, crc, isEmpty, acktype;
	private String data, src, dst;
	private Boolean badFrame;
	
	private boolean beVerbose = false;
	
	@SuppressWarnings("unused")
	private byte[] bytes;

	public Frame(int isEmpty, String src, String dst, int size, int acktype, int crc, String data, Boolean badFrame) {
		super();
		this.dst = dst;
		this.size = size;
		this.crc = crc;
		this.isEmpty = isEmpty;
		this.acktype = acktype;
		this.data = data;
		this.src = src;
		this.badFrame = badFrame;
	}
	
	public Frame(String str) {
		String[] parts = str.split("<sep>");
		if(beVerbose)
			msg("splitting:" + str);
		if(beVerbose)
			msg("processing isEmpty");
		isEmpty = Integer.parseInt(parts[0]);
		if (isEmpty == 0) { //make things easier for ACK
			isEmpty = 0;
			src = parts[1]; //[SRC]
			if(beVerbose)
				msg("processing SRC");
			dst = parts[2]; //[DST]
			if(beVerbose)
				msg("processing DST");
			size = Integer.parseInt(parts[3]); //[SIZE/ACK]
			if(beVerbose)
				msg("processing SIZE/ACK");
			acktype = Integer.parseInt(parts[4]); //ACKTYPE
			if(beVerbose)
				msg("processing ACKTYPE");
			data = parts[5];
			if(beVerbose)
				msg("processing DATA");
			crc = Integer.parseInt(parts[6]);

			//integrity check
			badFrame = genCrc() != crc;
		}
		this.bytes = toString().getBytes();
	}
	
	//you can pass in the byte representation to reconstruct the frame as well
	public Frame(byte[] bytes) {
		String str = new String(bytes, StandardCharsets.UTF_8);
		String[] parts = str.split("<sep>");
		isEmpty = Integer.parseInt(parts[0]);
		if (isEmpty == 0) { //make things easier for ACK
			isEmpty = 0;
			src = parts[1]; //[SRC]
			dst = parts[2]; //[DST]
			size = Integer.parseInt(parts[3]); //[SIZE/ACK]
			acktype = Integer.parseInt(parts[4]); //ACKTYPE
			data = parts[5];
			crc = Integer.parseInt(parts[6]);

			//integrity check
			badFrame = genCrc() != crc;
		}
		this.bytes = toString().getBytes();
	}

	public Frame() {
		isEmpty = 1;
		badFrame = false;
	}

	public String toString() {
		if (isEmpty == 1)
			return "1<sep>";

		StringBuilder str = new StringBuilder();
		str.append("0");
		str.append("<sep>");
		str.append(this.src);
		str.append("<sep>");
		str.append(this.dst);
		str.append("<sep>");
		str.append(this.size);
		str.append("<sep>");
		str.append(this.acktype);
		str.append("<sep>");
		str.append(this.data);
		str.append("<sep>");
		str.append(this.crc);
		
		return str.toString();
	}

	
	public int genCrc() {
		int crcByte = 0;
		byte[] crcBytes = data.getBytes();
		for (int i=0; i < crcBytes.length; i++) {
			crcByte += crcBytes[i];
		}
		return crcByte % 255;
	}
	
	public String getDst() {
		return dst;
	}

	public int getSize() {
		return size;
	}

	public int getCrc() {
		return crc;
	}

	public int getIsEmpty() {
		return isEmpty;
	}

	public int getAcktype() {
		return acktype;
	}

	public String getData() {
		return data;
	}

	public String getSrc() {
		return src;
	}

	public Boolean getBadFrame() {
		return badFrame;
	}
	
	public byte[] getBytes() {
		return toString().getBytes();
	}
	
	public void setDst(String dst) {
		this.dst = dst;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setCrc(int crc) {
		this.crc = crc;
	}

	public void setIsEmpty(int isEmpty) {
		this.isEmpty = isEmpty;
	}

	public void setAcktype(int acktype) {
		this.acktype = acktype;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public void setBadFrame(Boolean badFrame) {
		this.badFrame = badFrame;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	
	private void msg(String input) {
		System.out.println("\t\tFRAME: "+ input);
	}
	
	  /**
     * This method generates a corrupt frame
     * @return corrupt frame
     */
    public Frame corruptFrame() {
        Random rand = new Random();
        byte[] corruptFrame;
        int omit;
        int i = 0;
        if (rand.nextInt(100) < 0) {
            System.out.println("Corrupt Frame!");
            corruptFrame = new byte[this.bytes.length - 1];
            omit = rand.nextInt(5);
            for (byte b: this.bytes) {
                if (i == omit)
                    continue;
                corruptFrame[i] =  b;
                i++;
            }
            return new Frame(corruptFrame);
        }
        return this;
    }

    /**
     * Generates a Token.
     * @return token.
     */
    public static Frame generateToken() {
        byte[] bytes = new byte[6];
        bytes[0] = 0x8; //Set token bit
        bytes[1] = 0x0;
        bytes[2] = 0x0;
        bytes[3] = 0x0;
        bytes[4] = 0x0;
        bytes[5] = 0x0;
        return new Frame(bytes);
    }

    /**
     * Generates a Kill Signal.
     * @return A kill signal for the Star Network.
     */
    public static Frame generateKillSig() {
        byte[] bytes = new byte[6];
        bytes[0] = 0x0;
        bytes[1] = 0x0;
        bytes[2] = 0x0;
        bytes[3] = 0x0;
        bytes[4] = 0x0;
        bytes[5] = 0x4; //Kill signal
        return new Frame(bytes);
    }

    /**
     * Returns the whole frame in a char byte array.
     * @return Frame.
     */
    public byte[] getFrame () {
        return this.bytes;
    }

    /**
     * Returns status of the Frame Control Byte.
     * @return Returns 1 if Frame is not a Token, and 0 if Frame is a Token<br>
     *
     */
    public int getFrameControl() {
        return (this.bytes[1] & 0xff);
    }

    /**
     * Looks at token bit and determines if the frame passed is a token.
     * @return True, if the frame is a token.
     */
    public boolean isToken() {
        //Checks to see if any of the Token specifiers are true
        if (getFrameControl() == 0 || tokenBit())
            return true;
        return false;
    }

    /**
     * Checks the token bit.
     * @return True if the token bit is flipped, otherwise false.
     */
    private boolean tokenBit() {
        byte bitMask = 0x8;
        byte tmp = (byte) (bytes[0] & bitMask);
        //If token bit is flipped, value of tmp will be 8
        if (tmp == 0x8)
            return true;
        return false;
    }

    /**
     * This method determines if the monitor bit is flipped or not.
     * @return True, if the monitor bit has been flipped.
     */
    public boolean monitorBit() {
        byte bitMask = 0x10;
        byte tmp = (byte) (bytes[0] & bitMask);
        //If monitor bit is flipped, value will be 16, 0 if otherwise
        if (tmp == 0x10)
            return true;
        return false;
    }

    /**
     * This method determines if the finished bit is flipped.
     * @return True, if the finished bit is not flipped.
     */
    public boolean finishedBit() {
        byte bitMask = 0x40;
        byte tmp = (byte) (bytes[5] & bitMask);
        //If finished bit is flipped, value will be 0x24
        if (tmp == 0x40)
            return false;
        return true;
    }

    /**
     * Sets the Monitor Bit to be 1
     */
    public void setMonitorBit() {
        this.bytes[0] = (byte) (this.bytes[0] | 0x10);
    }

    /**
     * Sets the Monitor Bit to be 0
     */
    public void zeroMonitorBit() {
        this.bytes[0] = (byte) (this.bytes[0] & 0x0);
    }

    /**
     * Sets the Finished bit in the Frame Status byte
     */
    public void setFinishedBit() {
        if (this.getSize() != 0) {
            System.out.println("Cannot set Finished Bits on a non-token frame!");
            return;
        }
        byte bitMask = 0x40;
        this.bytes[5] = (byte) (this.bytes[5] | bitMask);
    }

    /**
     * This method returns the Frame Status.
     * @return Return 2 means Frame acceptance
     * Return 3 means Frame rejection
     * Return 4 means Kill Signal
     * Return 4 means a Node has finished all transmissions
     */
    public byte getFrameStatus() {
        int sizeOfFrame = this.bytes.length;
        return this.bytes[sizeOfFrame - 1];
    }

    /**
     * Sets the Frame Status to val.
     * @param val
     */
    public void setFrameStatus(byte val) {
        int sizeOfFrame = this.bytes.length;
        this.bytes[sizeOfFrame - 1] = val;
    }

}
