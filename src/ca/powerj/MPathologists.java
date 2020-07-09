package ca.powerj;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

class MPathologists {
	private HashMap<Short, String> pathologists = new HashMap<Short, String>();

	MPathologists(LBase parent, PreparedStatement pstm) {
		readDB(parent, pstm);
	}

	void close() {
		pathologists.clear();
	}

	boolean matchPathologist(short id) {
		String s = pathologists.get(id);
		if (s != null) {
			if (s.length() > 0) {
				return true;
			}
		}
		return false;
	}

	private void readDB(LBase pj, PreparedStatement pstm) {
		ResultSet rst = pj.dbPowerJ.getResultSet(pstm);
		try {
			while (rst.next()) {
				if (rst.getString("prcd").trim().equalsIgnoreCase("PT")) {
					if (rst.getString("prac").equalsIgnoreCase("Y")) {
						if (rst.getShort("prid") > 0) {
							pathologists.put(rst.getShort("prid"), rst.getString("prnm").trim());
						}
					}
				}
			}
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Orders Map", e);
		} finally {
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.close(pstm);
		}
	}
}