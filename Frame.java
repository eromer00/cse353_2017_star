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
}
