package ca.powerj.data;

public class FacilityData {
	private boolean workflow = false;
	private boolean workload = false;
	private short facID = 0;
	private String name = "";
	private String descr = "";

	public String getDescr() {
		return descr;
	}

	public short getFacID() {
		return facID;
	}

	public String getName() {
		return name;
	}

	public boolean isWorkflow() {
		return workflow;
	}

	public boolean isWorkload() {
		return workload;
	}

	public void setDescr(String value) {
		value = value.trim();
		if (value.length() > 80) {
			this.descr = value.substring(0, 80);
		} else {
			this.descr = value;
		}
	}

	public void setFacID(short value) {
		this.facID = value;
	}

	public void setName(String value) {
		value = value.trim();
		if (value.length() > 4) {
			this.name = value.substring(0, 4);
		} else {
			this.name = value;
		}
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