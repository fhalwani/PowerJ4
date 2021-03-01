package ca.powerj.database;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;

public class DBMssql extends DBServer {

	public DBMssql(LibBase base) {
		super(base);
		dbName = "MSSQL";
		setConnection();
	}

	@Override
	public String getSQL2(short id) {
		switch (id) {
		case STM_CSE_SELECT:
			return "SELECT TOP (10000) * FROM <pjschema>.udvcases _WHERE_ ORDER BY fned DESC";
		case STM_CSE_SL_DTE:
			return "SELECT TOP (10000) * FROM <pjschema>.udvcases WHERE (fned BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY fned DESC";
		default:
			return super.getSQL2(id);
		}
	}

	/**
	 * Opens the Microsoft SQL Server connection.
	 */
	private void setConnection() {
		SQLServerDataSource ds = null;
		try {
			ds = new SQLServerDataSource();
			ds.setIntegratedSecurity(false);
			ds.setLoginTimeout(2);
			ds.setPortNumber(Integer.parseInt(base.getProperty("pjPort")));
			ds.setServerName(base.getProperty("pjHost"));
			ds.setUser(base.getProperty("pjUser"));
			ds.setPassword(base.getProperty("pjPass"));
			connection = ds.getConnection();
			execute("USE PowerJ4");
			base.log(LibConstants.ERROR_NONE, dbName, "Connected to MSSQL.");
		} catch (SQLServerException e) {
			base.log(LibConstants.ERROR_SQL, dbName, e);
		}
	}
}