package ca.powerj.data;

public class SpecimenMasterData {
	private byte spyID = 0;
	private byte subID = 0;
	private byte proID = 0;
	private byte turID = 0;
	private short spmID = 0;
	private short spgID = 0;
	private String name = "";
	private String descr = "";

	public String getDescr() {
		return descr;
	}

	public String getName() {
		return name;
	}

	public byte getProID() {
		return proID;
	}

	public short getSpgID() {
		return spgID;
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

	public byte getTurID() {
		return turID;
	}

	public void setDescr(String value) {
		value = value.trim();
		if (value.length() > 80) {
			this.descr = value.substring(0, 80);
		} else {
			this.descr = value;
		}
	}

	public void setName(String value) {
		value = value.trim();
		if (value.length() > 15) {
			this.name = value.substring(0, 15);
		} else {
			this.name = value;
		}
	}

	public void setProID(byte value) {
		this.proID = value;
	}

	public void setSpgID(short value) {
		this.spgID = value;
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

	public void setTurID(byte value) {
		this.turID = value;
	}
}