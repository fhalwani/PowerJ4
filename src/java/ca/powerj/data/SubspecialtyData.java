package ca.powerj.data;

public class SubspecialtyData {
	private boolean newRow = false;
	private boolean altered = false;
	private byte subID = 0;
	private byte spyID = 0;
	private String name = "";
	private String descr = "";
	private String specialty = "";

	public SubspecialtyData() {
		newRow = true;
	}

	public SubspecialtyData(byte subID, byte spyID, String name, String descr,
			String specialty) {
		this.subID = subID;
		this.spyID = spyID;
		setName(name);
		setDescr(descr);
		setSpecialty(specialty);
	}

	public String getDescr() {
		return descr;
	}

	public byte getID() {
		return subID;
	}

	public String getName() {
		return name;
	}

	public String getSpecialty() {
		return specialty;
	}

	public byte getSpyID() {
		return spyID;
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

	public void setDescr(String value) {
		if (value == null) {
			this.descr = "";
		} else {
			value = value.trim();
			if (value.length() > 32) {
				this.descr = value.substring(0, 32);
			} else {
				this.descr = value;
			}
		}
		altered = true;
	}

	public void setID(byte value) {
		this.subID = value;
	}

	public void setName(String value) {
		if (value == null) {
			this.name = "";
		} else {
			value = value.trim();
			if (value.length() > 8) {
				this.name = value.substring(0, 8);
			} else {
				this.name = value;
			}
		}
		altered = true;
	}

	public void setNewRow(boolean value) {
		this.newRow = value;
	}

	public void setSpecialty(String value) {
		if (value == null) {
			this.specialty = "";
		} else {
			value = value.trim();
			if (value.length() > 16) {
				this.specialty = value.substring(0, 16);
			} else {
				this.specialty = value;
			}
		}
	}

	public void setSpyID(byte value) {
		this.spyID = value;
		altered = true;
	}

	@Override
	public String toString() {
		return name;
	}
}