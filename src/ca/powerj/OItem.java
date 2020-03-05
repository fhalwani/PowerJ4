package ca.powerj;

class OItem {
	boolean newRow = false;
	short id = 0;
	String name = "";
	String descr = "";

	OItem() {}

	OItem(short i, String detail) {
		id = i;
		name = detail;
	}
	public String toString() {
		return name;
	}

	public int compareTo(OItem o) {
		if (o == null) {
			return 1;
		}
		return name.compareTo(o.name);
	}

	public boolean equals(Object o) {
		OItem item = (OItem)o;
		if (item == null) {
			return false;
		}
		return (id == item.id);
	}
}