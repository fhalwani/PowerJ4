package ca.powerj.data;

import java.util.Calendar;

public class EventData {
	private Calendar time = Calendar.getInstance();
	private String description = "";
	private String location = "";
	private String material = "";

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}

	public String getMaterial() {
		return material;
	}

	public Calendar getTime() {
		return time;
	}

	public void setDescription(String value) {
		this.description = value.trim();
	}

	public void setLocation(String value) {
		this.location = value.trim();
	}

	public void setMaterial(String value, String label) {
		value = value.trim();
		if (value.equals("S")) {
			this.material = "Specimen ";
		} else if (value.equals("B")) {
			this.material = "Block ";
		} else if (value.equals("L")) {
			this.material = "Slide ";
		} else {
			this.material = "";
		}
		this.material += label.trim();
	}

	public void setTime(long value) {
		this.time.setTimeInMillis(value);
	}
}