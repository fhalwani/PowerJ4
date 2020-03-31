package ca.powerj;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

class MFacilities {
	private short facID = 0;
	private OFacility facility = new OFacility();
	private HashMap<Short, OFacility> facilities = new HashMap<Short, OFacility>();

	MFacilities(LBase parent, PreparedStatement pstm) {
		readDB(parent, pstm);
	}

	void close() {
		facilities.clear();
	}

	boolean doworkflow(short id) {
		if (facID != id) {
			facility = facilities.get(id);
			facID = id;
		}
		if (facility != null) {
			// Else, tables not sync'ed
			return facility.workflow;
		}
		facID = 0;
		return false;
	}

	boolean doWorkload(short id) {
		if (facID != id) {
			facility = facilities.get(id);
			facID = id;
		}
		if (facility != null) {
			// Else, tables not sync'ed
			return facility.workload;
		}
		facID = 0;
		return false;
	}

	private void readDB(LBase pj, PreparedStatement pstm) {
		ResultSet rst = pj.dbPowerJ.getResultSet(pstm);
		try {
			while (rst.next()) {
				facility = new OFacility();
				facility.workflow = (rst.getString("FAFL").toUpperCase().equals("Y"));
				facility.workload = (rst.getString("FALD").toUpperCase().equals("Y"));
				facilities.put(rst.getShort("FAID"), facility);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Facilities Map", e);
		} finally {
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.close(pstm);
		}
	}
}