//package starofstars;

import java.nio.charset.StandardCharsets;

public class Frame {


















/*

old frame

*/




//Remastered Frame 2.0 class, everything works here...


	private String size, crc, acktype;
	private int isEmpty;
	private String data, src, dst;
	private String src_sw, dst_sw;
	private Boolean badFrame, ack = false;
	private String str;

	private boolean beVerbose = false;

	@SuppressWarnings("unused")
	private byte[] bytes;

/*
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
*/

	private static String toBinaryInt(int i) {
		StringBuilder sb = new StringBuilder();

		String bin = "";
		for(int j = 0; j < 8; j++) {
			switch(i % 2) {
				case 0:
					sb.append("0");
					break;
				case 1:
					sb.append("1");
					break;
			}
			i /= 2;
		}
		return sb.reverse().toString();
	}
	public Frame(String src, String dst, String src_sw, String dst_sw, String acktype) {
		super();
		this.src = toBinaryInt(Integer.valueOf(src));
		this.dst = toBinaryInt(Integer.valueOf(dst));
		this.src_sw = toBinaryInt(Integer.valueOf(src_sw));
		this.dst_sw = toBinaryInt(Integer.valueOf(dst_sw));
		this.ack = true;
		this.size = toBinaryInt(0);
		this.data = acktype;
		this.crc = size;

		this.isEmpty = 0;
		this.badFrame = false;
		setStr(getBinaryString());
	}

	public Frame(int isEmpty, String src, String dst, String src_sw, String dst_sw, String data, Boolean badFrame) {
		super();
		this.src = toBinaryInt(Integer.valueOf(src));
		this.dst = toBinaryInt(Integer.valueOf(dst));
		this.src_sw = toBinaryInt(Integer.valueOf(src_sw));
		this.dst_sw = toBinaryInt(Integer.valueOf(dst_sw));
		this.size = toBinaryInt(data.length());
		System.out.println("asdf:" + this.size);
		this.data = toBinary(data);
		this.crc = size;

		this.isEmpty = isEmpty;
		this.badFrame = badFrame;

		setStr(getBinaryString());
		if(genCrc() == 0) {
			this.badFrame = true;
		} else {
			this.badFrame = false;
		}
	}

	public String getBinaryString() {
		return this.src + this.src_sw + this.dst + this.dst_sw + this.size + this.data + this.crc;
	}
	/*
	public Frame(String str) {
		String[] parts = str.split("<sep>");
		if (beVerbose)
			msg("splitting:" + str);
		if (beVerbose)
			msg("processing isEmpty");
		isEmpty = Integer.parseInt(parts[0]);
		if (isEmpty == 0) { //make things easier for ACK
			isEmpty = 0;
			src = parts[1]; //[SRC]
			if (beVerbose)
				msg("processing SRC");
			dst = parts[2]; //[DST]
			if (beVerbose)
				msg("processing DST");
			size = Integer.parseInt(parts[3]); //[SIZE/ACK]
			if (beVerbose)
				msg("processing SIZE/ACK");
			acktype = Integer.parseInt(parts[4]); //ACKTYPE
			if (beVerbose)
				msg("processing ACKTYPE");
			data = parts[5];
			if (beVerbose)
				msg("processing DATA");
			crc = Integer.parseInt(parts[6]);

			//integrity check
			badFrame = genCrc() != crc;
		}
		this.bytes = toString().getBytes();
	}
	*/
	public Frame(String str) {
		this.src = str.substring(0, 8);
		this.src_sw = str.substring(8, 16);
		this.dst = str.substring(16, 24);
		this.dst_sw = str.substring(24, 32);
		this.size = str.substring(32, 40);
		this.data = str.substring(40);
		this.crc = data.substring(data.length() - 8);

		this.data = data.substring(0, data.length()-8);
		setStr(getBinaryString());
		if(genCrc() == 0) {
			this.badFrame = true;
		} else {
			this.badFrame = false;
		}
	}

	private void setStr(String s) {
		this.str = s;
	}

	private boolean getACK() {
		return ack;
	}

	/*

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
	*/

	public Frame() {
		isEmpty = 1;
		badFrame = false;
	}
	/*
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
	*/

	public int genCrc() {
		String sc = getStr();
		if(sc.length() < 40) {
			System.out.println("not long enough");
			return 0;
		}
		String sc_size = sc.substring(32, 40);
		String sc_crc = sc.substring(sc.length() - 8);
		int x, y;
		x = Integer.parseInt(sc_size);
		y = Integer.parseInt(sc_crc);
		if(x == y) {
			return 1;
		}
		return 0;
	}

	public int getSrc() {
		String sc = src;
		return Integer.parseInt(sc, 2);
	}

	public int getDst() {
		String ds = dst;
		return Integer.parseInt(ds, 2);
	}

	public int getSSrc() {
		String sc = src_sw;
		return Integer.parseInt(sc, 2);
	}

	public int getSdst() {
		String ds = dst_sw;
		return Integer.parseInt(ds, 2);
	}

	public int getSize() {
		String sz = size;
		return Integer.parseInt(sz, 2);
	}

	public int getCRC() {
		String cc = crc;
		return Integer.parseInt(cc, 2);
	}

	public String getStr() {
		return str;
	}

	public String parseData() {
		String sc = getStr();
		sc = sc.substring(40, sc.length()-8);

		String ret = "";
		for(int i = 0; i < sc.length(); i+=8) {
			ret += (char)Integer.parseInt(sc.substring(i, i+8), 2);
		}
		return ret;
	}

	public int getIsEmpty() {
		return isEmpty;
	}

	public int getAcktype() {
		return Integer.parseInt(acktype);
	}

	public String getData() {
		return data;
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
		this.size = String.valueOf(size);
	}

	public void setCrc(int crc) {
		this.crc = String.valueOf(crc);
	}

	public void setIsEmpty(int isEmpty) {
		this.isEmpty = isEmpty;
	}

	public void setAcktype(int acktype) {
		this.acktype = String.valueOf(acktype);
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
		System.out.println("\t\tFRAME: " + input);
	}

	public String toBinary(String a) {
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
}
