package ca.powerj.data;

public class ScheduleSumData {
	private short facID = 0;
	private short prsID = 0;
	private short srvID = 0;
	private short subID = 0;
	private int dayID = 0;
	private String prsFull = "";
	private String prsName = "";
	private String srvName = "";
	private String subName = "";

	public int getDayID() {
		return dayID;
	}

	public short getFacID() {
		return facID;
	}

	public short getPrsID() {
		return prsID;
	}

	public String getPrsFull() {
		return prsFull;
	}

	public String getPrsName() {
		return prsName;
	}

	public short getSrvID() {
		return srvID;
	}

	public String getSrvName() {
		return srvName;
	}

	public short getSubID() {
		return subID;
	}

	public String getSubName() {
		return subName;
	}

	public void setDayID(int value) {
		this.dayID = value;
	}

	public void setFacID(short value) {
		this.facID = value;
	}

	public void setPrsID(short value) {
		this.prsID = value;
	}

	public void setPrsFull(String first, String last) {
		this.prsFull = first.trim() + " " + last.trim();
	}

	public void setPrsName(String value) {
		this.prsName = value;
	}

	public void setSrvID(short value) {
		this.srvID = value;
	}

	public void setSrvName(String value) {
		this.srvName = value;
	}

	public void setSubID(short value) {
		this.subID = value;
	}

	public void setSubName(String value) {
		this.subName = value;
	}
}