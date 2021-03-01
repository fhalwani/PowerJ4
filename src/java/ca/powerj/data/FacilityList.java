package ca.powerj.data;
import java.util.ArrayList;
import java.util.HashMap;
import ca.powerj.database.DBPowerj;

public class FacilityList {
	private short facID = 0;
	private FacilityData facility = new FacilityData();
	private HashMap<Short, FacilityData> facilities = null;

	public FacilityList(DBPowerj dbPowerJ) {
		getData(dbPowerJ);
	}

	public void close() {
		facilities.clear();
	}

	public boolean doworkflow(short id) {
		if (facID != id) {
			facility = facilities.get(id);
			facID = id;
		}
		if (facility != null) {
			// Else, tables not sync'ed
			return facility.isWorkflow();
		}
		facID = 0;
		return false;
	}

	public boolean doWorkload(short id) {
		if (facID != id) {
			facility = facilities.get(id);
			facID = id;
		}
		if (facility != null) {
			// Else, tables not sync'ed
			return facility.isWorkload();
		}
		facID = 0;
		return false;
	}

	private void getData(DBPowerj dbPowerJ) {
		ArrayList<FacilityData> temp = dbPowerJ.getFacilities(false);
		facilities = new HashMap<Short, FacilityData>();
		for (int i = 0; i < temp.size(); i++) {
			facility = temp.get(i);
			facilities.put(facility.getFacID(), facility);
		}
	}
}