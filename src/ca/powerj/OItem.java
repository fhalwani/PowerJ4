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

	public int compareTo(Object o) {
		if (o instanceof OItem) {
			OItem item = (OItem)o;
			return name.compareTo(item.name);
		}
		return 1;
	}

	public boolean equals(Object o) {
		if (o instanceof OItem) {
			OItem item = (OItem)o;
			return (id == item.id);
		}
		return false;
	}

	public String toString() {
		return name;
	}
}