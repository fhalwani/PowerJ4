package ca.powerj.data;

public class AccessionData {
	private boolean workflow = false;
	private boolean workload = false;
	private boolean codeSpec = true;
	private byte spyID = 0;
	private short accID = 0;
	private String name = "";

	public short getAccID() {
		return accID;
	}

	public String getName() {
		return name;
	}

	public byte getSpyID() {
		return spyID;
	}

	public boolean isCodeSpec() {
		return codeSpec;
	}

	public boolean isWorkflow() {
		return workflow;
	}

	public boolean isWorkload() {
		return workload;
	}

	public void setAccID(short value) {
		this.accID = value;
	}

	public void setCodeSpec(boolean value) {
		this.codeSpec = value;
	}

	public void setName(String value) {
		value = value.trim();
		if (value.length() > 30) {
			this.name = value.substring(0, 30);
		} else {
			this.name = value;
		}
	}

	public void setSpyID(byte value) {
		this.spyID = value;
	}

	public void setWorkflow(boolean value) {
		this.workflow = value;
	}

	public void setWorkload(boolean value) {
		this.workload = value;
	}

	@Override
	public String toString() {
		return name;
	}
}