package ca.powerj.data;

public class OrderMasterData {
	private short ordID = 0;
	private short grpID = 0;
	private String name = "";
	private String descr = "";

	public String getDescr() {
		return descr;
	}

	public short getGrpID() {
		return grpID;
	}

	public String getName() {
		return name;
	}

	public short getOrdID() {
		return ordID;
	}

	public void setDescr(String value) {
		value = value.trim();
		if (value.length() > 80) {
			this.descr = value.substring(0, 80);
		} else {
			this.descr = value;
		}
	}

	public void setGrpID(short value) {
		this.grpID = value;
	}

	public void setName(String value) {
		value = value.trim();
		if (value.length() > 15) {
			this.name = value.substring(0, 15);
		} else {
			this.name = value;
		}
	}

	public void setOrdID(short value) {
		this.ordID = value;
	}
}