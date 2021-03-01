package ca.powerj.data;

public class RuleData {
	private boolean altered = false;
	private short ruleID = 0;
	private String name = "";
	private String description = "";

	public RuleData() {
	}

	public RuleData(short ruleID, String name, String description) {
		this.ruleID = ruleID;
		setName(name);
		setDescription(description);
	}

	public boolean isAltered() {
		return altered;
	}

	public short getID() {
		return ruleID;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setAltered(boolean value) {
		this.altered = value;
	}

	public void setID(short value) {
		this.ruleID = value;
	}

	public void setName(String value) {
		if (value == null) {
			this.name = "";
		} else {
			this.name = value.trim();
		}
	}

	public void setDescription(String value) {
		if (value == null) {
			this.description = "";
		} else {
			this.description = value.trim();
		}
	}

	@Override
	public String toString() {
		return name;
	}
}