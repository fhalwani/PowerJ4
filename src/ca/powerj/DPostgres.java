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
				String url = "jdbc:postgresql://" + pj.pjHost + ":" + pj.pjPort + "/powerj";
				DriverManager.setLoginTimeout(15);
				connection = DriverManager.getConnection(url, pj.pjUser, pj.pjPass);
				stm = connection.createStatement();
				execute("SET search_path TO " + pj.pjSchema);
				pj.log(LConstants.ERROR_NONE, dbName, "Connected to PostgreSQL.");
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
		case STM_ADD_SL_YER:
			return "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, date_part('year', addt) as yearid, "
			+ "COUNT(caid) AS adca, SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2, "
			+ "SUM(adv3) AS adv3, SUM(adv4) AS adv4 "
			+ "FROM " + pj.pjSchema + ".udvadditionals "
			+ "WHERE addt BETWEEN ? AND ? "
			+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, date_part('year', addt) "
			+ "ORDER BY faid, syid, sbid, poid, sgid, yearid";
		case STM_CSE_SELECT:
			return "SELECT * FROM " + pj.pjSchema + ".udvcases _WHERE_ ORDER BY fned DESC LIMIT 10000";
		case STM_CSE_SL_DTE:
			return "SELECT * FROM " + pj.pjSchema + ".udvcases WHERE (fned BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY fned DESC LIMIT 10000";
		case STM_FRZ_SL_YER:
			return "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, "
			+ "date_part('year', aced) as yearid, COUNT(spid) AS frsp, "
			+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl, "
			+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2, "
			+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4 "
			+ "FROM " + pj.pjSchema + ".udvfrozens "
			+ "WHERE aced BETWEEN ? AND ? "
			+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, date_part('year', aced) "
			+ "ORDER BY faid, syid, sbid, poid, sgid, yearid";
		case STM_SPG_SL_YER:
			return "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, "
					+ "date_part('year', c.fned) as yearid, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl, "
					+ "SUM(s.spv1) AS spv1, SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, "
					+ "SUM(s.spv5) AS spv5 FROM " + pj.pjSchema + ".specigroups g "
					+ "INNER JOIN " + pj.pjSchema + ".specimaster m ON g.sgid = m.sgid "
					+ "INNER JOIN " + pj.pjSchema + ".specimens s ON m.smid = s.smid "
					+ "INNER JOIN " + pj.pjSchema + ".cases c ON c.caid = s.caid "
					+ "INNER JOIN " + pj.pjSchema + ".procedures r ON r.poid = g.poid "
					+ "INNER JOIN " + pj.pjSchema + ".subspecial b ON b.sbid = g.sbid "
					+ "INNER JOIN " + pj.pjSchema + ".specialties y ON y.syid = b.syid "
					+ "INNER JOIN " + pj.pjSchema + ".facilities f ON f.faid = c.faid WHERE c.fned BETWEEN ? AND ? "
					+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, date_part('year', c.fned) "
					+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid";
		default:
			return super.setSQL(id);
		}
	}
}
