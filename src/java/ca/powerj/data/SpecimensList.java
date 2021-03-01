package ca.powerj.data;
import java.util.ArrayList;
import java.util.HashMap;
import ca.powerj.database.DBPowerj;

public class SpecimensList {
	private short masterID = 0, groupID = 0;
	private HashMap<Short, Short> specMaster = null;
	private HashMap<Short, SpecimenGroupData> specGroups = null;
	private SpecimenGroupData specGroup = new SpecimenGroupData();

	public SpecimensList(DBPowerj dbPowerJ) {
		getData(dbPowerJ);
	}

	public void close() {
		specMaster.clear();
		specGroups.clear();
	}

	public short getCoderID(int row, int col) {
		return specGroup.getCodeId(row, col);
	}

	private void getData(DBPowerj dbPowerJ) {
		ArrayList<SpecimenMasterData> tempMaster = dbPowerJ.getSpecimenMasters();
		specMaster = new HashMap<Short, Short>();
		for (int i = 0; i < tempMaster.size(); i++) {
			specMaster.put(tempMaster.get(i).getSpmID(), tempMaster.get(i).getSpgID());
		}
		tempMaster.clear();
		ArrayList<SpecimenGroupData> tempGroup = dbPowerJ.getSpecimenGroups();
		specGroups = new HashMap<Short, SpecimenGroupData>();
		for (int i = 0; i < tempGroup.size(); i++) {
			specGroups.put(tempGroup.get(i).getGrpID(), tempGroup.get(i));
		}
		tempGroup.clear();
	}

	public byte getProcedureID() {
		return specGroup.getProID();
	}

	public byte getSubspecialtyID() {
		return specGroup.getSubID();
	}

	public int getValue5() {
		return specGroup.getValue5();
	}

	boolean hasLN() {
		return specGroup.isHasLN();
	}

	/** Match a specimen from PowerPath to one from PowerJ */
	public boolean matchSpecimens(short id) {
		if (masterID != id) {
			masterID = id;
			if (specMaster.get(id) == null) {
				groupID = 0;
				specGroup = null;
			} else {
				groupID = specMaster.get(id);
				specGroup = specGroups.get(groupID);
			}
		}
		if (specGroup != null) {
			return true;
		}
		return false;
	}
}