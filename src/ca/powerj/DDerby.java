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
		try {
			if (connection == null || connection.isClosed()) {
				Properties p = System.getProperties();
				p.setProperty("derby.system.home", pj.appDir);
				DriverManager.setLoginTimeout(15);
				connection = DriverManager.getConnection("jdbc:derby:" + pj.pjSchema + ";create=false;");
				stm = connection.createStatement();
				execute("SET SCHEMA " + pj.pjSchema);
				prepareBase();
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	@Override
	String setSQL(short id) {
		switch (id) {
		case STM_CSE_SELECT:
			return "SELECT * FROM udvCases _WHERE_ ORDER BY FNED DESC FETCH FIRST 10000 ROWS ONLY";
		case STM_CSE_SL_DTE:
			return "SELECT * FROM udvCases WHERE (FNED BETWEEN _FROM_ AND _TO_) _AND_ ORDER BY FNED DESC FETCH FIRST 10000 ROWS ONLY";
		default:
			return super.setSQL(id);
		}
	}
}