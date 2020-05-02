package ca.powerj;

import java.sql.SQLException;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

class DMicrosoft extends DServer {

	DMicrosoft(LBase pjcore) {
		super(pjcore);
		dbName = "MSSQL";
		setConnection();
	}

	/**
	 * Opens the Microsoft SQL Server connection.
	 */
	private void setConnection() {
		SQLServerDataSource ds = new SQLServerDataSource();
		try {
			if (connection == null || connection.isClosed()) {
				ds.setIntegratedSecurity(false);
				ds.setLoginTimeout(2);
				ds.setPortNumber(Integer.parseInt(pj.pjPort));
				ds.setServerName(pj.pjHost);
//				ds.setDatabaseName("PowerJ4");
				ds.setUser(pj.pjUser);
				ds.setPassword(pj.pjPass);
				connection = ds.getConnection();
				stm = connection.createStatement();
				execute("USE PowerJ4");
				pj.log(LConstants.ERROR_NONE, dbName, "Connected to MSSQL.");
			}
		} catch (SQLServerException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	@Override
	String setSQL(short id) {
		switch (id) {
		case STM_CSE_SELECT:
			return "SELECT TOP (10000) * FROM udvCases _WHERE_ ORDER BY FNED DESC";
		case STM_CSE_SL_DTE:
			return "SELECT TOP (10000) * FROM udvCases WHERE (FNED BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY FNED DESC";
		default:
			return super.setSQL(id);
		}
	}
}