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
		case STM_ADD_SL_YER:
			return "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, YEAR(addt) as yearid, "
			+ "COUNT(caid) AS adca, SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2, "
			+ "SUM(adv3) AS adv3, SUM(adv4) AS adv4 "
			+ "FROM " + pj.pjSchema + ".udvadditionals "
			+ "WHERE addt BETWEEN ? AND ? "
			+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, YEAR(addt) "
			+ "ORDER BY faid, syid, sbid, poid, sgid, yearid";
		case STM_CSE_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvcases _WHERE_ ORDER BY fned DESC FETCH FIRST 10000 ROWS ONLY";
		case STM_CSE_SL_DTE:
			return "SELECT * FROM " + pj.pjSchema + ".udvcases WHERE (fned BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY fned DESC FETCH FIRST 10000 ROWS ONLY";
		case STM_FRZ_SL_YER:
			return "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, "
			+ "YEAR(aced) as yearid, COUNT(spid) AS frsp, "
			+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl, "
			+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2, "
			+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4 "
			+ "FROM " + pj.pjSchema + ".udvfrozens "
			+ "WHERE aced BETWEEN ? AND ? "
			+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, YEAR(aced) "
			+ "ORDER BY faid, syid, sbid, poid, sgid, yearid";
		case STM_SPG_SL_YER:
			return "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, "
					+ "YEAR(c.fned) as yearid, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl, "
					+ "SUM(s.spv1) AS spv1, SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, "
					+ "SUM(s.spv5) AS spv5 FROM " + pj.pjSchema + ".specigroups g "
					+ "INNER JOIN " + pj.pjSchema + ".specimaster m ON g.sgid = m.sgid "
					+ "INNER JOIN " + pj.pjSchema + ".specimens s ON m.smid = s.smid "
					+ "INNER JOIN " + pj.pjSchema + ".cases c ON c.caid = s.caid "
					+ "INNER JOIN " + pj.pjSchema + ".procedures r ON r.poid = g.poid "
					+ "INNER JOIN " + pj.pjSchema + ".subspecial b ON b.sbid = g.sbid "
					+ "INNER JOIN " + pj.pjSchema + ".specialties y ON y.syid = b.syid "
					+ "INNER JOIN " + pj.pjSchema + ".facilities f ON f.faid = c.faid WHERE c.fned BETWEEN ? AND ? "
					+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, YEAR(c.fned) "
					+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid";
		default:
			return super.setSQL(id);
		}
	}
}