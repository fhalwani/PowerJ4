package ca.powerj.data;
import java.util.ArrayList;

public class SubspecialtyList {
	private boolean isFilter = false;
	private ArrayList<SubspecialtyData> list = new ArrayList<SubspecialtyData>();

	public SubspecialtyList(boolean isFilter) {
		this.isFilter = isFilter;
	}

	public void add(SubspecialtyData item) {
		list.add(item);
	}

	public SubspecialtyData get(int key) {
		return list.get(key);
	}

	public ItemData[] getAll() {
		return getArray(list);
	}

	private ItemData[] getArray(ArrayList<SubspecialtyData> alist) {
		if (alist.size() > 0) {
			int j = 0;
			ItemData[] items;
			if (isFilter) {
				items = new ItemData[alist.size() + 1];
				items[0] = new ItemData((short) 0, "** All **");
				j++;
			} else {
				items = new ItemData[alist.size()];
			}
			for (int i = 0; i < alist.size(); i++) {
				items[j] = new ItemData(alist.get(i).getID(), alist.get(i).getName());
				j++;
			}
			return items;
		} else {
			ItemData[] items = {new ItemData()};
			return items;
		}
	}

	public ItemData[] getFiltered(byte spyID) {
		ArrayList<SubspecialtyData> filtered = new ArrayList<SubspecialtyData>();
		SubspecialtyData item = new SubspecialtyData();
		for (int i = 0; i < list.size(); i++) {
			item = list.get(i);
			if (spyID == 0 || spyID == item.getSpyID()) {
				filtered.add(item);
			}
		}
		return getArray(filtered);
	}

	public SubspecialtyData getItem(short subID) {
		SubspecialtyData item = new SubspecialtyData();
		for (int i = 0; i < list.size(); i++) {
			if (subID == list.get(i).getID()) {
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