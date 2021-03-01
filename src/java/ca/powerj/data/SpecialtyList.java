package ca.powerj.data;
import java.util.ArrayList;

public class SpecialtyList {
	private boolean isFilter = false;
	private ArrayList<SpecialtyData> list = new ArrayList<SpecialtyData>();

	public SpecialtyList(boolean value) {
		this.isFilter = value;
	}

	public void add(SpecialtyData item) {
		list.add(item);
	}

	public SpecialtyData get(int key) {
		return list.get(key);
	}

	public ItemData[] getAll() {
		int j = 0;
		ItemData[] items;
		if (isFilter) {
			items = new ItemData[list.size() + 1];
			items[0] = new ItemData((short) 0, "** All **");
			j++;
		} else {
			items = new ItemData[list.size()];
		}
		for (int i = 0; i < list.size(); i++) {
			items[j] = new ItemData(list.get(i).getID(), list.get(i).getName());
			j++;
		}
		return items;
	}

	public SpecialtyData getItem(short spyID) {
		SpecialtyData item = new SpecialtyData();
		for (int i = 0; i < list.size(); i++) {
			if (spyID == list.get(i).getID()) {
				item = list.get(i);
				break;
			}
		}
		return item;
	}

	public int getSize() {
		return list.size();
	}
}