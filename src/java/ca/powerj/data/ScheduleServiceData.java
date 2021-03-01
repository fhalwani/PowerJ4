package ca.powerj.data;

public class ScheduleServiceData {
	private boolean isNew = true;
	private boolean isOn = true;
	private short srvID = 0;
	private int wdID = 0;
	private String name = "";
	private String date = "";
	private ItemData person = new ItemData();

	public String getDate() {
		return date;
	}

	public String getName() {
		return name;
	}

	public short getPersonId() {
		return (short)person.getID();
	}

	public ItemData getPersonItem() {
		return person;
	}

	public short getSrvID() {
		return srvID;
	}

	public int getWdID() {
		return wdID;
	}

	public boolean isNew() {
		return isNew;
	}

	public boolean isOn() {
		return isOn;
	}

	public void setDate(String value) {
		this.date = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setNew(boolean value) {
		this.isNew = value;
	}

	public void setOn(boolean value) {
		this.isOn = value;
	}

	public void setPerson(short key, String value) {
		this.person.setID(key);
		this.person.setName(value);
	}

	public void setPerson(ItemData value) {
		this.person = value;
	}

	public void setSrvID(short value) {
		this.srvID = value;
	}

	public void setWdID(int value) {
		this.wdID = value;
	}
}