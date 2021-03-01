package ca.powerj.data;
import java.util.Date;

public class WorkdayData {
	private boolean isOn = false;
	private int dayID = 0;
	private int dayNo = 0;
	private int dow  = 0;
	private Date date = new Date();
	private String name = "";
	private String type = "";

	public int getDayID() {
		return dayID;
	}

	public int getDayNo() {
		return dayNo;
	}

	public Date getDate() {
		return date;
	}

	public int getDow() {
		return dow;
	}

	public String getName() {
		return name;
	}

	public long getTime() {
		return date.getTime();
	}

	public String getType() {
		return type;
	}

	public boolean isOn() {
		return isOn;
	}

	public void setDate(long value) {
		this.date.setTime(value);
	}

	public void setDayID(int value) {
		this.dayID = value;
	}

	public void setDayNo(int value) {
		this.dayNo = value;
	}

	public void setDow(int value) {
		this.dow = value;
	}

	public void setName(String value) {
		this.name = value;
	}

	public void setOn(boolean value) {
		this.isOn = value;
	}

	public void setType(String value) {
		if (value != null) {
			this.type = value.trim().toUpperCase();
		}
	}
}