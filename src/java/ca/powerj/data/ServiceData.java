package ca.powerj.data;

public class ServiceData {
	private boolean newRow = false;
	private boolean[] codes = new boolean[16];
	private byte subID  = 0;
	private byte srvID  = 0;
	private short facID  = 0;
	private short codeID  = 0;
	private String name   = "";
	private String descr  = "";

	public boolean getCode(int key) {
		return codes[key];
	}

	public short getCodeID() {
		return codeID;
	}

	public boolean[] getCodes() {
		return codes;
	}

	public String getDescr() {
		return descr;
	}

	public short getFacID() {
		return facID;
	}

	public String getName() {
		return name;
	}

	public byte getSrvID() {
		return srvID;
	}

	public byte getSubID() {
		return subID;
	}

	public boolean isNewRow() {
		return newRow;
	}

	public void setCode(int key, boolean value) {
		this.codes[key] = value;
	}

	public void setCodeID(short value) {
		this.codeID = value;
	}

	public void setCodes(boolean[] value) {
		this.codes = value;
	}

	public void setDescr(String value) {
		value = value.trim();
		if (value.length() > 64) {
			this.descr = value.substring(0, 64);
		} else {
			this.descr = value;
		}
	}

	public void setFacID(short value) {
		this.facID = value;
	}

	public void setName(String value) {
		value = value.trim().toUpperCase();
		if (value.length() > 8) {
			this.name = value.substring(0, 8);
		} else {
			this.name = value;
		}
	}

	public void setNewRow(boolean value) {
		this.newRow = value;
	}

	public void setSrvID(byte value) {
		this.srvID = value;
	}

	public void setSubID(byte value) {
		this.subID = value;
	}
}