package ca.powerj.data;

public class ProcedureData {
	private boolean altered = false;
	private boolean newRow = false;
	private byte proID = 0;
	private byte spyID = 0;
	private String name = "";
	private String description = "";

	public ProcedureData() {
		newRow = true;
	}

	public ProcedureData(byte proID, String name, String description) {
		this.proID = proID;
		setName(name);
		setDescription(description);
	}

	public ProcedureData(byte proID, byte spyID, String name) {
		this.proID = proID;
		this.spyID = proID;
		setName(name);
		setDescription(description);
	}

	public String getDescription() {
		return description;
	}

	public byte getID() {
		return proID;
	}

	public byte getSpyID() {
		return spyID;
	}

	public String getName() {
		return name;
	}

	public boolean isAltered() {
		return altered;
	}

	public boolean isNewRow() {
		return newRow;
	}

	public void setAltered(boolean value) {
		this.altered = value;
	}

	public void setDescription(String value) {
		if (value == null) {
			this.description = "";
		} else {
			this.description = value.trim();
		}
	}

	public void setID(byte value) {
		this.proID = value;
	}

	public void setName(String value) {
		if (value == null) {
			this.name = "";
		} else {
			this.name = value.trim();
		}
	}

	public void setNewRow(boolean value) {
		this.newRow = value;
	}

	@Override
	public String toString() {
		return name;
	}
}