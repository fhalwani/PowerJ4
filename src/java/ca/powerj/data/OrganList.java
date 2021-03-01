package ca.powerj.data;
import java.util.ArrayList;

public class OrganList {
	private ArrayList<OrganData> list = new ArrayList<OrganData>();

	public OrganList() {
	}

	public void add(OrganData item) {
		list.add(item);
	}

	public OrganData get(int key) {
		return list.get(key);
	}

	public ItemData[] getAll() {
		return getArray(list);
	}

	private ItemData[] getArray(ArrayList<OrganData> alist) {
		ItemData[] items = new ItemData[alist.size()];
		for (int i = 0; i < alist.size(); i++) {
			items[i] = new ItemData(alist.get(i).getSubID(), alist.get(i).getName());
		}
		return items;
	}

	public ItemData[] getFiltered(int spyID, int subID) {
		ArrayList<OrganData> filtered = new ArrayList<OrganData>();
		OrganData item = new OrganData();
		for (int i = 0; i < list.size(); i++) {
			item = list.get(i);
			if (spyID == 0 || spyID == item.getSpyID()) {
				if (subID == 0 || subID == item.getSubID()) {
					filtered.add(item);
				}
			}
		}
		return getArray(filtered);
	}

	public OrganData getItem(short ssoID) {
		OrganData item = new OrganData();
		for (int i = 0; i < list.size(); i++) {
			if (ssoID == list.get(i).getID()) {
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