package ca.powerj.data;

public class ScheduleStaffData {
	private short prsID = 0;
	private int wdID = 0;
	private String name = "";
	private String[] services = new String[7];

	public String getName() {
		return name;
	}

	public short getPrsID() {
		return prsID;
	}

	public String getService(int key) {
		return services[key];
	}

	public String[] getServices() {
		return services;
	}

	public int getWdID() {
		return wdID;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setPrsID(short value) {
		this.prsID = value;
	}

	public void setService(int key, String value) {
		this.services[key] = value;
	}

	public void setServices(String[] value) {
		this.services = value;
	}

	public void setWdID(int value) {
		this.wdID = value;
	}
}