package ca.powerj.database;
import java.sql.DriverManager;
import java.sql.SQLException;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;

public class DBMaria extends DBServer {

	public DBMaria(LibBase base) {
		super(base);
		dbName = "MySQL";
		setConnection();
	}

	@Override
	public String getSQL2(short id) {
		switch (id) {
		case STM_CSE_SELECT:
			return "SELECT * FROM <pjschema>.udvcases _WHERE_ ORDER BY fned DESC LIMIT 10000";
		case STM_CSE_SL_DTE:
			return "SELECT * FROM <pjschema>.udvcases WHERE (fned BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY fned DESC LIMIT 10000";
		default:
			return super.getSQL2(id);
		}
	}

	/**
	 * Opens mySQL (mariaDB) Server connection.
	 */
	private void setConnection() {
		try {
			String url = "jdbc:mysql://" + base.getProperty("pjHost") + ":"
					+ Integer.parseInt(base.getProperty("pjPort")) + "/powerj"
					+ "?autoReconnect=true&useUnicode=true"
					+ "&useLegacyDatetimeCode=false&serverTimezone=UTC";
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection(url, base.getProperty("pjUser"), base.getProperty("pjPass"));
			execute("USE " + base.getProperty("pjSche"));
			base.log(LibConstants.ERROR_NONE, dbName, "Connected to MariaDB.");
		} catch (IllegalArgumentException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SecurityException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}
}