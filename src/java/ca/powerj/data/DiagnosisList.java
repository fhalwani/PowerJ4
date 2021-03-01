package ca.powerj.data;

import java.util.ArrayList;

public class DiagnosisList {
	private ArrayList<DiagnosisData> list = new ArrayList<DiagnosisData>();

	public void add(DiagnosisData item) {
		list.add(item);
	}

	public DiagnosisData get(int key) {
		return list.get(key);
	}

	public ItemData[] getAll() {
		return getArray(list);
	}

	private ItemData[] getArray(ArrayList<DiagnosisData> alist) {
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

	public ItemData[] getFiltered(byte disID) {
		ArrayList<DiagnosisData> filtered = new ArrayList<DiagnosisData>();
		DiagnosisData item = new DiagnosisData();
		for (int i = 0; i < list.size(); i++) {
			item = list.get(i);
			if (disID == 0 || disID == item.getDisID()) {
				filtered.add(item);
			}
		}
		return getArray(filtered);
	}

	public DiagnosisData getItem(int diaID) {
		DiagnosisData item = new DiagnosisData();
		for (int i = 0; i < list.size(); i++) {
			if (diaID == list.get(i).getID()) {
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
