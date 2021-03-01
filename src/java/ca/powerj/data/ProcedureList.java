package ca.powerj.data;
import java.util.ArrayList;

public class ProcedureList {
	private boolean isFilter = false;
	private ArrayList<ProcedureData> list = new ArrayList<ProcedureData>();

	public ProcedureList(boolean value) {
		this.isFilter = value;
	}

	public void add(ProcedureData item) {
		list.add(item);
	}

	public ProcedureData get(int key) {
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

	private ItemData[] getArray(ArrayList<ProcedureData> alist) {
		ItemData[] items = new ItemData[alist.size()];
		for (int i = 0; i < alist.size(); i++) {
			items[i] = new ItemData(alist.get(i).getID(), alist.get(i).getName());
		}
		return items;
	}

	public ItemData[] getFiltered(int spyID) {
		ArrayList<ProcedureData> filtered = new ArrayList<ProcedureData>();
		ProcedureData item = new ProcedureData();
		for (int i = 0; i < list.size(); i++) {
			item = list.get(i);
			if (spyID == 0 || spyID == item.getSpyID()) {
				filtered.add(item);
			}
		}
		return getArray(filtered);
	}

	public ProcedureData getItem(byte proID) {
		ProcedureData item = new ProcedureData();
		for (int i = 0; i < list.size(); i++) {
			if (proID == list.get(i).getID()) {
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