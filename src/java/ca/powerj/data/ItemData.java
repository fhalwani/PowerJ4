package ca.powerj.data;

public class ItemData {
	private int id = 0;
	private String name;

	public ItemData() {
	}

	public ItemData(int id, String value) {
		this.id = id;
		if (value != null) {
			this.name = value.trim();
		}
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public short getShortID() {
		return (short) id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setName(String value) {
		if (value != null) {
			this.name = value.trim();
		}
	}

	@Override
	public String toString() {
		return name;
	}
}