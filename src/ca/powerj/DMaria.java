package ca.powerj;

import java.sql.DriverManager;
import java.sql.SQLException;

class DMaria extends DServer {

	DMaria(LBase pjcore) {
		super(pjcore);
		dbName = "MySQL";
		setConnection();
	}

	/**
	 * Opens mySQL (mariaDB) Server connection.
	 */
	private void setConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				String url = "jdbc:mysql://" + pj.pjHost + ":" + pj.pjPort + "/powerj"
						+ "?autoReconnect=true&useUnicode=true" + "&useLegacyDatetimeCode=false&serverTimezone=UTC";
				DriverManager.setLoginTimeout(15);
				connection = DriverManager.getConnection(url, pj.pjUser, pj.pjPass);
				stm = connection.createStatement();
				execute("USE " + pj.pjSchema);
				pj.log(LConstants.ERROR_NONE, dbName, "Connected to MariaDB.");
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
			return "SELECT * FROM " + pj.pjSchema + ".udvcases _WHERE_ ORDER BY fned DESC LIMIT 10000";
		case STM_CSE_SL_DTE:
			return "SELECT * FROM " + pj.pjSchema + ".udvcases WHERE (fned BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY fned DESC LIMIT 10000";
		default:
			return super.setSQL(id);
		}
	}
}