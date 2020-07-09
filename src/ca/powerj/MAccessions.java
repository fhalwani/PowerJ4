package ca.powerj;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

class MAccessions {
	private short accID = 0;
	private OAccession accession = new OAccession();
	private HashMap<Short, OAccession> accessions = new HashMap<Short, OAccession>();

	MAccessions(LBase parent, PreparedStatement pstm) {
		readDB(parent, pstm);
	}

	void close() {
		accessions.clear();
	}

	boolean codeSpecimen() {
		if (accession != null) {
			// Else, tables not sync'ed
			return accession.codeSpec;
		}
		return false;
	}

	boolean doworkflow(short id) {
		if (accID != id) {
			accession = accessions.get(id);
			accID = id;
		}
		if (accession != null) {
			// Else, tables not sync'ed
			return accession.workflow;
		}
		return false;
	}

	boolean doWorkload(short id) {
		if (accID != id) {
			accession = accessions.get(id);
			accID = id;
		}
		if (accession != null) {
			// Else, tables not sync'ed
			return accession.workload;
		}
		return false;
	}

	byte getSpecialty() {
		if (accession != null) {
			// Else, tables not sync'ed
			return accession.spyID;
		}
		return 0;
	}

	private void readDB(LBase pj, PreparedStatement pstm) {
		ResultSet rst = pj.dbPowerJ.getResultSet(pstm);
		try {
			while (rst.next()) {
				accession = new OAccession();
				accession.spyID = rst.getByte("syid");
				accession.codeSpec = (rst.getString("sysp").toUpperCase().equals("Y"));
				// Both Accessions and Specialties must be active
				accession.workflow = (rst.getString("acfl").toUpperCase().equals("Y")
						&& rst.getString("syfl").toUpperCase().equals("Y"));
				accession.workload = (rst.getString("acld").toUpperCase().equals("Y")
						&& rst.getString("syld").toUpperCase().equals("Y"));
				accessions.put(rst.getShort("acid"), accession);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Accessions Map", e);
		} finally {
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.close(pstm);
		}
	}
}