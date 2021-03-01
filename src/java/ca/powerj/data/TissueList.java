package ca.powerj.data;
import java.util.ArrayList;

public class TissueList {
	private ArrayList<TissueData> list = new ArrayList<TissueData>();

	public void add(TissueData item) {
		list.add(item);
	}

	public TissueData get(int key) {
		return list.get(key);
	}

	public ItemData[] getAll() {
		return getArray(list);
	}

	private ItemData[] getArray(ArrayList<TissueData> alist) {
		if (alist.size() > 0) {
			ItemData[] items = new ItemData[alist.size()];
			for (int i = 0; i < alist.size(); i++) {
				items[i] = new ItemData(alist.get(i).getID(), alist.get(i).getName());
			}
			return items;
		} else {
			ItemData[] items = {new ItemData()};
			return items;
		}
	}

	public ItemData[] getFiltered(short proID) {
		ArrayList<TissueData> filtered = new ArrayList<TissueData>();
		TissueData item = new TissueData();
		for (int i = 0; i < list.size(); i++) {
			item = list.get(i);
			if (proID == 0 || proID == item.getProID()) {
				filtered.add(item);
			}
		}
		return getArray(filtered);
	}

	public TissueData getItem(short tisID) {
		TissueData item = new TissueData();
		for (int i = 0; i < list.size(); i++) {
			if (tisID == list.get(i).getID()) {
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