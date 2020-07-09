package ca.powerj;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

class DDerby extends DDesktop {

	DDerby(LBase parent) {
		super(parent);
		dbName = "Derby";
		setConnection();
	}

	@Override
	void close() {
		super.close();
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if (!e.getSQLState().equals("XJ015")) {
				// Else, normal shut down
				pj.log(LConstants.ERROR_SQL, dbName, e);
			}
		}
	}

	/** Opens the Derby connection. */
	private void setConnection() {
		Properties p = System.getProperties();
		try {
			if (connection == null || connection.isClosed()) {
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
				p.setProperty("derby.system.home", pj.appDir);
				DriverManager.setLoginTimeout(15);
				connection = DriverManager.getConnection("jdbc:derby:" + pj.appDir + "powerj;create=false;");
				stm = connection.createStatement();
				execute("SET SCHEMA " + pj.pjSchema);
				pj.log(LConstants.ERROR_NONE, dbName, "Connected to Derby.");
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} catch (ClassNotFoundException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	@Override
	String setSQL(short id) {
		switch (id) {
		case STM_CSE_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvcases _WHERE_ ORDER BY fned DESC FETCH FIRST 10000 ROWS ONLY";
		case STM_CSE_SL_DTE:
			return "SELECT * FROM " + pj.pjSchema + ".udvcases WHERE (fned BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY fned DESC FETCH FIRST 10000 ROWS ONLY";
		case STM_CSE_SL_YER:
			return "SELECT c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, YEAR(c.fned) as fnyear, "
					+ "count(caid) as cases, sum(c.casp) as casp, sum(c.cabl) as cabl, sum(c.casl) as casl, sum(c.cahe) as cahe, "
					+ "sum(c.cass) as cass, sum(c.caih) as caih, sum(c.casy) as casy, sum(c.cafs) as cafs, sum(c.cav1) as cav1, "
					+ "sum(c.cav2) as cav2, sum(c.cav3) as cav3, sum(c.cav4) as cav4, sum(c.cav5) as cav5 "
					+ "FROM " + pj.pjSchema + ".cases c " + "INNER JOIN dbpj.facilities f ON f.faid = c.faid "
					+ "INNER JOIN " + pj.pjSchema + ".subspecial b ON b.sbid = c.sbid "
					+ "INNER JOIN " + pj.pjSchema + ".specimaster m ON m.smid = c.smid "
					+ "INNER JOIN " + pj.pjSchema + ".specigroups g ON g.sgid = m.sgid "
					+ "INNER JOIN " + pj.pjSchema + ".procedures r ON r.poid = g.poid "
					+ "INNER JOIN " + pj.pjSchema + ".specialties y ON y.syid = b.syid "
					+ "WHERE c.fned BETWEEN ? AND ? "
					+ "GROUP BY c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, YEAR(c.fned) "
					+ "ORDER BY c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, fnyear";
		default:
			return super.setSQL(id);
		}
	}
}