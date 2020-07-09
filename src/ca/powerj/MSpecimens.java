package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

class MSpecimens {
	private short masterID = 0, groupID = 0;
	private OSpecGroup specGroup = new OSpecGroup();
	private HashMap<Short, Short> specMaster = new HashMap<Short, Short>();
	private HashMap<Short, OSpecGroup> specGroups = new HashMap<Short, OSpecGroup>();

	MSpecimens(LBase parent, PreparedStatement pstm) {
		readDB(parent, pstm);
	}

	void close() {
		specMaster.clear();
		specGroups.clear();
	}

	int getValue5() {
		return specGroup.value5;
	}

	short getCoderID(byte row, byte col) {
		return specGroup.codes[col - 1][row - 1].id;
	}

	byte getProcedureID() {
		return specGroup.proID;
	}

	byte getSubspecialtyID() {
		return specGroup.subID;
	}

	boolean hasLN() {
		return specGroup.hasLN;
	}

	/** Match a specimen from PowerPath to one from PowerJ */
	boolean matchSpecimens(short id) {
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

	private void readDB(LBase pj, PreparedStatement pstm) {
		ResultSet rst = pj.dbPowerJ.getResultSet(pstm);
		specGroups.clear();
		specMaster.clear();
		groupID = 0;
		try {
			while (rst.next()) {
				if (rst.getShort("sgid") > 1) {
					// Skip Ignore & Reject
					if (groupID != rst.getShort("sgid")) {
						groupID = rst.getShort("sgid");
						specGroup = specGroups.get(groupID);
						if (specGroup == null) {
							specGroup = new OSpecGroup();
							specGroup.subID = rst.getByte("sbid");
							specGroup.proID = rst.getByte("poid");
							specGroup.value5 = rst.getInt("sgv5");
							specGroup.hasLN = (rst.getString("sgln").toUpperCase().equals("Y"));
							specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_1 - 1] = new OItem(
									rst.getShort("sg1b"), rst.getString("C1NB"));
							specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_2 - 1] = new OItem(
									rst.getShort("sg2b"), rst.getString("C2NB"));
							specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_3 - 1] = new OItem(
									rst.getShort("sg3b"), rst.getString("C2NB"));
							specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_4 - 1] = new OItem(
									rst.getShort("sg4b"), rst.getString("C4NB"));
							specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_1 - 1] = new OItem(
									rst.getShort("sg1m"), rst.getString("C1NM"));
							specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_2 - 1] = new OItem(
									rst.getShort("sg2m"), rst.getString("C2NM"));
							specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_3 - 1] = new OItem(
									rst.getShort("sg3m"), rst.getString("C3NM"));
							specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_4 - 1] = new OItem(
									rst.getShort("sg4m"), rst.getString("C4NM"));
							specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_1 - 1] = new OItem(
									rst.getShort("sg1r"), rst.getString("C1NR"));
							specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_2 - 1] = new OItem(
									rst.getShort("sg2r"), rst.getString("C2NR"));
							specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_3 - 1] = new OItem(
									rst.getShort("sg3r"), rst.getString("C3NR"));
							specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_4 - 1] = new OItem(
									rst.getShort("sg4r"), rst.getString("C4NR"));
							specGroups.put(groupID, specGroup);
						}
					}
					specMaster.put(rst.getShort("smid"), groupID);
				}
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Templates Map", e);
		} finally {
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.close(pstm);
		}
	}
}