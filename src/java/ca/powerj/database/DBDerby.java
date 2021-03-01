package ca.powerj.database;
import java.sql.DriverManager;
import java.sql.SQLException;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;

public class DBDerby extends DBDesktop {

	public DBDerby(LibBase base) {
		super(base);
		dbName = "Derby";
		setConnection();
	}

	@Override
	public void close() {
		super.close();
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if (!e.getSQLState().equals("XJ015")) {
				// Else, normal shut down
				base.log(LibConstants.ERROR_SQL, dbName, e);
			}
		}
	}

	@Override
	public String getSQL2(short id) {
		switch (id) {
		case STM_ADD_SL_YER:
			return "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, YEAR(addt) as yearid, "
			+ "COUNT(caid) AS adca, SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2, "
			+ "SUM(adv3) AS adv3, SUM(adv4) AS adv4 "
			+ "FROM <pjschema>.udvadditionals "
			+ "WHERE addt BETWEEN ? AND ? "
			+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, YEAR(addt) "
			+ "ORDER BY faid, syid, sbid, poid, sgid, yearid";
		case STM_CSE_SELECT:
			return "SELECT * FROM <pjschema>.udvcases _WHERE_ ORDER BY fned DESC FETCH FIRST 10000 ROWS ONLY";
		case STM_CSE_SL_DTE:
			return "SELECT * FROM <pjschema>.udvcases WHERE (fned BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY fned DESC FETCH FIRST 10000 ROWS ONLY";
		case STM_FRZ_SL_YER:
			return "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, "
			+ "YEAR(aced) as yearid, COUNT(spid) AS frsp, "
			+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl, "
			+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2, "
			+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4 "
			+ "FROM <pjschema>.udvfrozens "
			+ "WHERE aced BETWEEN ? AND ? "
			+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, YEAR(aced) "
			+ "ORDER BY faid, syid, sbid, poid, sgid, yearid";
		case STM_SPG_SL_YER:
			return "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, "
					+ "YEAR(c.fned) as yearid, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl, "
					+ "SUM(s.spv1) AS spv1, SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, "
					+ "SUM(s.spv5) AS spv5 FROM <pjschema>.specigroups g "
					+ "INNER JOIN <pjschema>.specimaster m ON g.sgid = m.sgid "
					+ "INNER JOIN <pjschema>.specimens s ON m.smid = s.smid "
					+ "INNER JOIN <pjschema>.cases c ON c.caid = s.caid "
					+ "INNER JOIN <pjschema>.procedures r ON r.poid = g.poid "
					+ "INNER JOIN <pjschema>.subspecial b ON b.sbid = g.sbid "
					+ "INNER JOIN <pjschema>.specialties y ON y.syid = b.syid "
					+ "INNER JOIN <pjschema>.facilities f ON f.faid = c.faid WHERE c.fned BETWEEN ? AND ? "
					+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, YEAR(c.fned) "
					+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid";
		default:
			return super.getSQL2(id);
		}
	}

	/** Opens the Derby connection. */
	private void setConnection() {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			System.getProperties().setProperty("derby.system.home", base.getProperty("pjDir"));
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection("jdbc:derby:"
					+ base.getProperty("pjDir") + "powerj;create=false;");
			execute("SET SCHEMA " + base.getProperty("pjSche"));
			base.log(LibConstants.ERROR_NONE, dbName, "Connected to Derby.");
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} catch (ClassNotFoundException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}
}