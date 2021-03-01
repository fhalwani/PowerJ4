package ca.powerj.data;
import java.util.Calendar;
import java.util.HashMap;

public class SpecimenData {
	private byte errorID = 0;
	private byte label = 0;
	private byte orgID = 0;
	private byte procID = 0;
	private byte spyID = 0;
	private byte subID = 0;
	private short noFrags = 1;
	private short noBlocks = 0;
	private short noFSBlks = 0;
	private short noSlides = 0;
	private short noFSSlds = 0;
	private short noHE = 0;
	private short noSS = 0;
	private short noIHC = 0;
	private short noMOL = 0;
	private short spmID = 0;
	private short tisID = 0;
	private int value5 = 0;
	private long caseID = 0;
	private long specID = 0;
	private double value1 = 0;
	private double value2 = 0;
	private double value3 = 0;
	private double value4 = 0;
	private Calendar collected = Calendar.getInstance();
	private Calendar received = Calendar.getInstance();
	private String name = "";
	private String descr = "";
	private ItemData master = new ItemData();
	private HashMap<Short, OrderData> lstOrders = new HashMap<Short, OrderData>();

	public long getCaseID() {
		return caseID;
	}

	public Calendar getCollected() {
		return collected;
	}

	public String getDescr() {
		return descr;
	}

	public byte getErrorID() {
		return errorID;
	}

	public byte getLabel() {
		return label;
	}

	public ItemData getMaster() {
		return master;
	}

	public short getMasterID() {
		return (short)master.getID();
	}

	public String getName() {
		return name;
	}

	public short getNoBlocks() {
		return noBlocks;
	}

	public short getNoFrags() {
		return noFrags;
	}

	public short getNoFSBlks() {
		return noFSBlks;
	}

	public short getNoFSSlds() {
		return noFSSlds;
	}

	public short getNoHE() {
		return noHE;
	}

	public short getNoIHC() {
		return noIHC;
	}

	public short getNoMOL() {
		return noMOL;
	}

	public short getNoSlides() {
		return noSlides;
	}

	public short getNoSS() {
		return noSS;
	}

	public OrderData getOrder(short key) {
		return lstOrders.get(key);
	}

	public HashMap<Short, OrderData> getOrders() {
		return lstOrders;
	}

	public byte getOrgID() {
		return orgID;
	}

	public byte getProcID() {
		return procID;
	}

	public Calendar getReceived() {
		return received;
	}

	public long getSpecID() {
		return specID;
	}

	public short getSpmID() {
		return spmID;
	}

	public byte getSpyID() {
		return spyID;
	}

	public byte getSubID() {
		return subID;
	}

	public short getTisID() {
		return tisID;
	}

	public double getValue1() {
		return value1;
	}

	public double getValue2() {
		return value2;
	}

	public double getValue3() {
		return value3;
	}

	public double getValue4() {
		return value4;
	}

	public int getValue5() {
		return value5;
	}

	public void setCaseID(long value) {
		this.caseID = value;
	}

	public void setCollected(long value) {
		this.collected.setTimeInMillis(value);
	}

	public void setDescr(String value) {
		value = value.trim();
		if (value.length() > 64) {
			this.descr = value.substring(0, 64);
		} else {
			this.descr = value;
		}
	}

	public void setErrorID(byte value) {
		this.errorID = value;
	}

	public void setLabel(int value) {
		if (value > Byte.MAX_VALUE) {
			this.label = Byte.MAX_VALUE;
		} else {
			this.label = (byte) value;
		}
	}

	public void setMaster(ItemData value) {
		this.master = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setNoBlocks(int value) {
		if (value > Short.MAX_VALUE) {
			this.noBlocks = Short.MAX_VALUE;
		} else {
			this.noBlocks = (short) value;
		}
	}

	public void setNoFrags(int value) {
		if (value > Short.MAX_VALUE) {
			this.noFrags = Short.MAX_VALUE;
		} else {
			this.noFrags = (short) value;
		}
	}

	public void setNoFSBlks(int value) {
		if (value > Short.MAX_VALUE) {
			this.noFSBlks = Short.MAX_VALUE;
		} else {
			this.noFSBlks = (short) value;
		}
	}

	public void setNoFSSlds(int value) {
		if (value > Short.MAX_VALUE) {
			this.noFSSlds = Short.MAX_VALUE;
		} else {
			this.noFSSlds = (short) value;
		}
	}

	public void setNoHE(int value) {
		if (value > Short.MAX_VALUE) {
			this.noHE = Short.MAX_VALUE;
		} else {
			this.noHE = (short) value;
		}
	}

	public void setNoIHC(int value) {
		if (value > Short.MAX_VALUE) {
			this.noIHC = Short.MAX_VALUE;
		} else {
			this.noIHC = (short) value;
		}
	}

	public void setNoMOL(int value) {
		if (value > Short.MAX_VALUE) {
			this.noMOL = Short.MAX_VALUE;
		} else {
			this.noMOL = (short) value;
		}
	}

	public void setNoSlides(int value) {
		if (value > Short.MAX_VALUE) {
			this.noSlides = Short.MAX_VALUE;
		} else {
			this.noSlides = (short) value;
		}
	}

	public void setNoSS(int value) {
		if (value > Short.MAX_VALUE) {
			this.noSS = Short.MAX_VALUE;
		} else {
			this.noSS = (short) value;
		}
	}

	public void setOrder(short key, OrderData value) {
		this.lstOrders.put(key, value);
	}

	public void setOrders(HashMap<Short, OrderData> lstOrders) {
		this.lstOrders = lstOrders;
	}

	public void setOrgID(byte value) {
		this.orgID = value;
	}

	public void setProcID(byte value) {
		this.procID = value;
	}

	public void setReceived(long value) {
		this.received.setTimeInMillis(value);
	}

	public void setSpecID(long value) {
		this.specID = value;
	}

	public void setSpmID(short value) {
		this.spmID = value;
	}

	public void setSpyID(byte value) {
		this.spyID = value;
	}

	public void setSubID(byte value) {
		this.subID = value;
	}

	public void setTisID(short value) {
		this.tisID = value;
	}

	public void setValue1(double value) {
		this.value1 = value;
	}

	public void setValue2(double value) {
		this.value2 = value;
	}

	public void setValue3(double value) {
		this.value3 = value;
	}

	public void setValue4(double value) {
		this.value4 = value;
	}

	public void setValue5(int value) {
		this.value5 = value;
	}
}