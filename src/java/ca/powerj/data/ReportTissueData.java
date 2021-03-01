package ca.powerj.data;

public class ReportTissueData {
	private final char NODE_TYPE = 'T';
	private byte ordID = 0;
	private byte parent = 0;
	private short tisID = 0;
	private String tissue = "";
	private String name = "";

	ReportTissueData(byte parent) {
		this.parent = parent;
	}

	byte getOrderID() {
		return ordID;
	}

	byte getParent() {
		return parent;
	}

	String getTissue() {
		return tissue;
	}

	short getTissueID() {
		return tisID;
	}

	char getType() {
		return NODE_TYPE;
	}

	private void setName() {
		if (name.length() > 0) {
			if (name.length() > 10) {
				name = name.substring(0, 10);
			}
		} else if (tissue.length() > 0) {
			if (tissue.length() > 10) {
				name = tissue.substring(0, 10);
			} else {
				name = tissue;
			}
		}
	}

	void setParent(byte parent) {
		this.parent = parent;
	}

	void setTissue(byte orderID, short tissueID, String tissue, String name) {
		this.ordID = orderID;
		this.tisID = tissueID;
		if (tissue == null) {
			this.tissue = "";
		} else {
			this.tissue = tissue.trim();
		}
		if (name == null) {
			this.name = "";
		} else {
			this.name = name.trim();
		}
		setName();
	}

	@Override
	public String toString() {
		return name;
	}
}