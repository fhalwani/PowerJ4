package ca.powerj;
import java.sql.DriverManager;
import java.sql.SQLException;

class DPostgres extends DDesktop {

	public DPostgres(LBase parent) {
		super(parent);
		dbName = "PostgreSQL";
		setConnection();
	}

	/**
	 * Opens PostgreSQL Server connection.
	 */
	private void setConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				String url = "jdbc:postgresql://" + pj.pjHost + ":" +
						pj.pjPort + "/" + pj.pjSchema;
				DriverManager.setLoginTimeout(15);
				connection = DriverManager.getConnection(url, pj.pjUser, pj.pjPass);
				stm = connection.createStatement();
				execute("SET search_path TO " + pj.pjSchema);
				prepareBase();
			}
		} catch (IllegalArgumentException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} catch (SecurityException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	@Override
	String setSQL(short id) {
		switch (id) {
		case STM_CSE_SELECT:
			return "SELECT * FROM udvCases _WHERE_ ORDER BY FNED DESC LIMIT 10000";
		case STM_CSE_SL_DTE:
			return "SELECT * FROM udvCases WHERE (FNED BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY FNED DESC LIMIT 10000";
		case STM_CSE_SL_YER:
			return "SELECT c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, date_part('year', c.fned) as fnyear, " +
					"count(caid) as cases, sum(c.casp) as casp, sum(c.cabl) as cabl, sum(c.casl) as casl, sum(c.cahe) as cahe, " +
					"sum(c.cass) as cass, sum(c.caih) as caih, sum(c.casy) as casy, sum(c.cafs) as cafs, sum(c.cav1) as cav1, " +
					"sum(c.cav2) as cav2, sum(c.cav3) as cav3, sum(c.cav4) as cav4, sum(c.cav5) as cav5 " +
					"FROM dbpj.cases c " +
					"INNER JOIN dbpj.facilities f ON f.faid = c.faid " +
					"INNER JOIN dbpj.subspecial b ON b.sbid = c.sbid " +
					"INNER JOIN dbpj.specimaster m ON m.smid = c.smid " +
					"INNER JOIN dbpj.specigroups g ON g.sgid = m.sgid " +
					"INNER JOIN dbpj.procedures r ON r.poid = g.poid " +
					"INNER JOIN dbpj.specialties y ON y.syid = b.syid " +
					"WHERE c.fned BETWEEN ? AND ? " +
					"GROUP BY c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, fnyear " +
					"ORDER BY c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, fnyear";
		default:
			return super.setSQL(id);
		}
	}
}
