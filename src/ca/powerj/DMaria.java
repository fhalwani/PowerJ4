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
				String url = "jdbc:mysql://" + pj.pjHost + ":" +
						pj.pjPort + "/" + pj.pjSchema +
						"?autoReconnect=true&useUnicode=true" +
						"&useLegacyDatetimeCode=false&serverTimezone=UTC";
				DriverManager.setLoginTimeout(15);
				connection = DriverManager.getConnection(url, pj.pjUser, pj.pjPass);
				stm = connection.createStatement();
				execute("USE " + pj.pjSchema);
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
		default:
			return super.setSQL(id);
		}
	}
}