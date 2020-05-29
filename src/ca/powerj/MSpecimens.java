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
				if (rst.getShort("SGID") > 1) {
					// Skip Ignore & Reject
					if (groupID != rst.getShort("SGID")) {
						groupID = rst.getShort("SGID");
						specGroup = specGroups.get(groupID);
						if (specGroup == null) {
							specGroup = new OSpecGroup();
							specGroup.subID = rst.getByte("SBID");
							specGroup.proID = rst.getByte("POID");
							specGroup.value5 = rst.getInt("SGV5");
							specGroup.hasLN = (rst.getString("SGLN").toUpperCase().equals("Y"));
							specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_1 - 1] = new OItem(
									rst.getShort("SG1B"), rst.getString("C1NB"));
							specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_2 - 1] = new OItem(
									rst.getShort("SG2B"), rst.getString("C2NB"));
							specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_3 - 1] = new OItem(
									rst.getShort("SG3B"), rst.getString("C2NB"));
							specGroup.codes[LConstants.CODER_B - 1][LConstants.CODER_4 - 1] = new OItem(
									rst.getShort("SG4B"), rst.getString("C4NB"));
							specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_1 - 1] = new OItem(
									rst.getShort("SG1M"), rst.getString("C1NM"));
							specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_2 - 1] = new OItem(
									rst.getShort("SG2M"), rst.getString("C2NM"));
							specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_3 - 1] = new OItem(
									rst.getShort("SG3M"), rst.getString("C3NM"));
							specGroup.codes[LConstants.CODER_M - 1][LConstants.CODER_4 - 1] = new OItem(
									rst.getShort("SG4M"), rst.getString("C4NM"));
							specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_1 - 1] = new OItem(
									rst.getShort("SG1R"), rst.getString("C1NR"));
							specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_2 - 1] = new OItem(
									rst.getShort("SG2R"), rst.getString("C2NR"));
							specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_3 - 1] = new OItem(
									rst.getShort("SG3R"), rst.getString("C3NR"));
							specGroup.codes[LConstants.CODER_R - 1][LConstants.CODER_4 - 1] = new OItem(
									rst.getShort("SG4R"), rst.getString("C4NR"));
							specGroups.put(groupID, specGroup);
						}
					}
					specMaster.put(rst.getShort("SMID"), groupID);
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