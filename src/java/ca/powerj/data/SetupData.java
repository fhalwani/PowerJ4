package ca.powerj.data;

public class SetupData {
	private boolean altered = false;
	private byte type = 0;
	private String value = "";

	public byte getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public boolean isAltered() {
		return altered;
	}

	public void setAltered(boolean value) {
		this.altered = value;
	}

	public void setType(byte value) {
		this.type = value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}