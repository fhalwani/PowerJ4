package ca.powerj.data;

public class SpecialtyData {
	private boolean newRow = false;
	private boolean altered = false;
	private boolean workflow = false;
	private boolean workload = false;
	private boolean codeSpecimen = false;
	private byte spyID = 0;
	private String name  = "";

	public SpecialtyData() {
		newRow = true;
	}

	public SpecialtyData(byte spyID, boolean workflow, boolean workload, boolean codeSpecimen, String name) {
		newRow = false;
		this.spyID = spyID;
		this.workflow = workflow;
		this.workload = workload;
		this.codeSpecimen = codeSpecimen;
		setName(name);
	}

	public boolean codeSpecimen() {
		return codeSpecimen;
	}

	public byte getID() {
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

	public boolean isWorkflow() {
		return workflow;
	}

	public boolean isWorkload() {
		return workload;
	}

	public void setAltered(boolean value) {
		this.altered = value;
	}

	public void setCodeSpecimen(boolean value) {
		this.codeSpecimen = value;
		altered = true;
	}

	public void setID(byte value) {
		this.spyID = value;
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

	public void setWorkflow(boolean value) {
		this.workflow = value;
		altered = true;
	}

	public void setWorkload(boolean value) {
		this.workload = value;
		altered = true;
	}

	@Override
	public String toString() {
		return name;
	}
}