package ca.powerj.data;
import java.util.ArrayList;
import java.util.HashMap;
import ca.powerj.database.DBPowerj;

public class PathologistList {
	private HashMap<Short, String> hashmap = new HashMap<Short, String>();

	public PathologistList(DBPowerj dbPowerJ) {
		getData(dbPowerJ);
	}

	public void close() {
		hashmap.clear();
	}

	private void getData(DBPowerj dbPowerJ) {
		ArrayList<PersonData> temp = dbPowerJ.getPersons();
		PersonData item = new PersonData();
		for (int i = 0; i < temp.size(); i++) {
			item = temp.get(i);
			if (item.isActive() && item.getCode().equalsIgnoreCase("PT")) {
				hashmap.put(item.getPrsID(), item.getInitials());
			}
		}
	}

	public boolean matchPathologist(short id) {
		String s = hashmap.get(id);
		if (s != null) {
			if (s.length() > 0) {
				return true;
			}
		}
		return false;
	}
}