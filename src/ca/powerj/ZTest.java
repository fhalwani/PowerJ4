package ca.powerj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

class ZTest {
	final static String _SCHEMA = "dbpj";
	final static String _TAB = "\t";
	final static String NEW_LINE = System.getProperty("line.separator");
	final static String FILE_SEPARATOR = System.getProperty("file.separator");
	static int noRows = 0;
	static String appDir = "";
	static String sql = "";
	static Connection connection = null;
	static Statement stm = null;
	static PreparedStatement pstm = null;
	static ResultSet rst = null;

	ZTest() {
		super();
	}

	public static void main(String[] args) {
		int db = 0;
		int cmd = 0;
		String s = "";
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("--path")) {
				appDir = args[i].substring(6);
			} else if (args[i].startsWith("--db")) {
				s = args[i].substring(4);
				db = Integer.parseInt(s);
			} else if (args[i].startsWith("--cmd")) {
				s = args[i].substring(5);
				cmd = Integer.parseInt(s);
			} else {
				System.out.println("Unknown arguement!");
				System.exit(1);
			}
		}
		if (db == 0) {
			System.out.println("Database not specified!");
			// not required, but alert
		}
		if (cmd == 0) {
			System.out.println("Command not specified!");
			System.exit(1);
		}
		System.out.println("appDir: " + appDir);
		switch (db) {
		case 1:
			connectDerby();
			break;
		case 2:
			connectMSSQL();
			break;
		case 3:
			connectMaria();
			break;
		case 4:
			connectPostgres();
			break;
		default:
			// Not required
		}
		switch (cmd) {
		case 1:
			deleteCases();
			break;
		case 2:
			exportFacilities();
			break;
		case 3:
			exportServices();
			break;
		case 4:
			importServices();
			break;
		case 5:
			listPending();
			break;
		case 6:
			exportServices();
			break;
		case 7:
			listWorkdays();
			break;
		case 8:
			updateComment();
			break;
		case 9:
			updateMasterSpec();
			break;
		case 10:
			updateView();
			break;
		case 11:
			updateSpecGroups();
			break;
		case 12:
			testToolbar();
			toShort();
			break;
		case 13:
			predict();
			break;
		case 14:
			interpolate();
			break;
		case 15:
			extrapolate();
			break;
		case 16:
			updatePersonnel();
			break;
		case 17:
			updatePassword();
			break;
		case 18:
			getDate();
			break;
		case 19:
			updateSetup();
			break;
		case 20:
			compressDerby();
			break;
		case 21:
			backupDerby();
			break;
		default:
		}
		switch (db) {
		case 1:
			closeDerby();
			break;
		case 2:
		case 3:
			closeDB();
			break;
		default:
		}
		System.exit(0);
	}

	static void backupDerby() {
		try {
			CallableStatement cs = connection.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
			cs.setString(1, "/data/fawaz/PowerJ/data");
			cs.execute();
			cs.close();
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static int booleanToInt(boolean[] bits) {
		int value = 0;
		for (byte i = 0; i < 32; i++) {
			if (bits[i]) {
				value |= (1 << i);
			}
		}
		return value;
	}

	static void closeDB() {
		try {
			stm.close();
		} catch (Exception e) {
		}
		try {
			if (pstm != null)
				pstm.close();
		} catch (Exception e) {
		}
		try {
			connection.close();
		} catch (Exception e) {
		}
	}

	static void closeDerby() {
		try {
			stm.close();
		} catch (Exception e) {
		}
		try {
			if (pstm != null)
				pstm.close();
		} catch (Exception e) {
		}
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (Exception e) {
		}
	}

	static void compressDerby() {
		try {
			CallableStatement cs = connection.prepareCall("CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?, ?, ?)");
			cs.setString(1, "DBPJ");
			cs.setString(2, "PENDING");
			cs.setShort(3, (short) 1);
			cs.execute();
			cs.setString(2, "CASES");
			cs.execute();
			cs.setString(2, "SPECIMENS");
			cs.execute();
			cs.close();
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void connectDerby() {
		try {
			Properties p = System.getProperties();
			p.setProperty("derby.system.home", appDir);
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection("jdbc:derby:" + _SCHEMA + ";create=false;");
			stm = connection.createStatement();
//			stm.executeUpdate("SET SCHEMA " + _SCHEMA);
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void connectMaria() {
		try {
			String url = "jdbc:mysql://localhost:3306/dbpj" + "?autoReconnect=true&useUnicode=true"
					+ "&useLegacyDatetimeCode=false&serverTimezone=UTC";
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection(url, "fawaz", "Dorval2019");
			stm = connection.createStatement();
			stm.executeUpdate("USE dbpj");
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void connectMSSQL() {
		SQLServerDataSource ds = new SQLServerDataSource();
		try {
			ds.setIntegratedSecurity(false);
			ds.setLoginTimeout(2);
			ds.setPortNumber(1433);
			ds.setServerName("clpathpjdb01");
			ds.setDatabaseName("dbpj");
			ds.setUser("PJClient");
			ds.setPassword("V0wTkl!P92PY$URe34vbnRL");
			connection = ds.getConnection();
			stm = connection.createStatement();
			stm.executeUpdate("USE dbpj");
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void connectPostgres() {
		try {
			String url = "jdbc:postgresql://localhost:5432/dbpj";
			String schema = "dbpj";
			String user = "PJClient";
			String password = "Jk98b1d,8d<:rj;b(3-6OCG";
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection(url, user, password);
			stm = connection.createStatement();
			stm.execute("SET search_path TO " + schema);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static void deleteCases() {
		try {
			stm.executeUpdate("DELETE FROM " + _SCHEMA + ".Pending");
			stm.executeUpdate("DELETE FROM " + _SCHEMA + ".Errors");
			stm.executeUpdate("DELETE FROM " + _SCHEMA + ".Comments");
			stm.executeUpdate("DELETE FROM " + _SCHEMA + ".Frozens");
			stm.executeUpdate("DELETE FROM " + _SCHEMA + ".Additionals");
			stm.executeUpdate("DELETE FROM " + _SCHEMA + ".Orders");
			stm.executeUpdate("DELETE FROM " + _SCHEMA + ".Specimens");
			stm.executeUpdate("DELETE FROM " + _SCHEMA + ".Cases");
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void exportFacilities() {
		String strFileName = appDir + "/data/facilities.txt";
		String output = "";
		File file = null;
		FileOutputStream fos = null;
		try {
			System.out.println("Exporting Facilities...");
			rst = stm.executeQuery("SELECT * FROM Facilities ORDER BY FAID");
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "FAID" + _TAB + "FAFL" + _TAB + "FALD" + _TAB + "FANM" + _TAB + "FADC" + NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("FAID") + _TAB + rst.getString("FAFL") + _TAB + rst.getString("FALD")
							+ _TAB + rst.getString("FANM") + _TAB + rst.getString("FADC") + NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				System.out.printf("Exported %1$3d rows from Facilities Table.", noRows);
			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} finally {
			try {
				rst.close();
			} catch (SQLException e) {
			}
			try {
				fos.close();
			} catch (IOException e) {
			}
		}
	}

	static void exportServices() {
		String strFileName = appDir + "/data/services.txt";
		String output = "";
		File file = null;
		FileOutputStream fos = null;
		try {
			System.out.println("Exporting Services...");
			rst = stm.executeQuery("SELECT SRID, FAID, SBID, SRCD, SRNM FROM Services ORDER BY SRID");
			file = new File(strFileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				output = "SRID" + _TAB + "FAID" + _TAB + "SBID" + _TAB + "SRCD" + _TAB + "SRNM" + NEW_LINE;
				fos.write(output.getBytes());
				while (rst.next()) {
					output = "" + rst.getShort("SRID") + _TAB + rst.getShort("FAID") + _TAB + rst.getShort("SBID")
							+ _TAB + rst.getShort("SRCD") + _TAB + rst.getString("SRNM") + NEW_LINE;
					fos.write(output.getBytes());
					noRows++;
				}
				System.out.printf("Exported %1$6d rows from Services Table.", noRows);
			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} finally {
			try {
				rst.close();
			} catch (SQLException e) {
			}
			try {
				fos.close();
			} catch (IOException e) {
			}
		}
	}

	static void getDate() {
		Calendar calNow = Calendar.getInstance();
		calNow.set(Calendar.MILLISECOND, 1);
		calNow.set(Calendar.SECOND, 0);
		calNow.set(Calendar.MINUTE, 0);
		calNow.set(Calendar.HOUR_OF_DAY, 0);
		calNow.set(Calendar.DAY_OF_MONTH, 1);
		calNow.set(Calendar.MONTH, Calendar.JANUARY);
		calNow.set(Calendar.YEAR, 2019);
		System.out.printf("Date is %1$12d.", calNow.getTimeInMillis());
	}

	static void importServices() {
		String strFileName = appDir + "/data/services.txt";
		int val = 0;
		String[] columns = null;
		File file = null;
		Scanner scanner = null;
		try {
			noRows = 0;
			file = new File(strFileName);
			if (file.exists()) {
				scanner = new Scanner(new FileReader(strFileName));
				pstm = connection
						.prepareStatement("INSERT INTO Services (SRID, FAID, SBID, SRCD, SRNM) VALUES (?, ?, ?, ?, ?)");
				while (scanner.hasNextLine()) {
					noRows++;
					columns = scanner.nextLine().split(_TAB);
					if (noRows == 1) {
						continue;
					}
					if (columns.length == 5) {
						for (int x = 0; x < 4; x++) {
							val = Integer.valueOf(columns[x]);
							if (val < 0) {
								val = 0;
							} else if (val > Short.MAX_VALUE) {
								val = Short.MAX_VALUE;
							}
							pstm.setInt((1 + x), val);
						}
						columns[4] = columns[4].trim();
						if (columns[4].length() > 8) {
							columns[4] = columns[4].substring(0, 8);
						}
						pstm.setString(5, columns[4]);
						if (pstm.executeUpdate() > 0) {
							noRows++;
						}
					} else {
						System.out.println("Setup Import Error No Columns = " + columns.length + " at line " + noRows);
						break;
					}
				}
				noRows--;
				System.out.println("Inserted " + noRows + " rows in Services Table.");
			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		} catch (NumberFormatException e) {
			e.printStackTrace(System.out);
		} catch (NullPointerException e) {
			e.printStackTrace(System.out);
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.out);
		} finally {
			scanner.close();
			try {
				pstm.close();
			} catch (SQLException e) {
			}
		}
	}

	static boolean[] intToBoolean(int value) {
		boolean[] bits = new boolean[32];
		for (byte i = 0; i < 32; i++) {
			bits[i] = (value & (1 << i)) != 0;
		}
		return bits;
	}

	static void listMasterSpec() {
		try {
			rst = stm.executeQuery("SELECT SMID, SGID, TAID, SMNM, SMDC FROM SpeciMaster WHERE SGID = 1 ORDER BY SMID");
			System.out.println("SMID\tSGID\tTAID\tSMNM\t\tSMDC");
			while (rst.next()) {
				System.out.printf("%1$6d %2$6d %3$3d %4$16s %5$24s", rst.getInt("SMID"), rst.getInt("SGID"),
						rst.getInt("TAID"), rst.getString("SMNM"), rst.getString("SMDC"));
				System.out.println();
				noRows++;
			}
			System.out.printf("%1$6d rows", noRows);
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		} finally {
			try {
				rst.close();
			} catch (SQLException e) {
			}
		}
	}

	static void listOrders() {
		try {
			sql = "SELECT o.SPID, o.OGID, o.ORQY, o.ORV1, o.ORV2, o.ORV3,\n" + "o.ORV4, g.OGNM\n" + "FROM Orders AS o\n"
					+ "INNER JOIN OrderGroups AS g ON g.OGID = o.OGID\n" + "WHERE SPID = ? ORDER BY OGNM";
			pstm = connection.prepareStatement(sql);
			pstm.setInt(1, 0);
			pstm.setInt(2, 6);
			rst = pstm.executeQuery();
			ResultSetMetaData rsmd = rst.getMetaData();
			int noCols = rsmd.getColumnCount();
			String name = "";
			for (int i = 1; i <= noCols; i++) {
				name = rsmd.getColumnName(i);
				System.out.printf(" Col %1$2d : %2$8s", i, name);
				System.out.println();
			}
			System.out.println("PNID\tFAID\tSBID\tSMID\tSGID\tPOID\tPNNO\tSMNM");
			while (rst.next()) {
				System.out.printf("%1$6d %2$6d %3$6d %4$4d %5$11s %6$35s", rst.getInt("PNID"), rst.getShort("FAID"),
						rst.getInt("SBID"), rst.getShort("SMID"), rst.getInt("SGID"), rst.getShort("POID"),
						rst.getString("PNNO"), rst.getString("SMNM"));
				System.out.println();
				noRows++;
			}
			System.out.printf("%1$6d rows", noRows);
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		} finally {
			try {
				rst.close();
			} catch (SQLException e) {
			}
		}
	}

	static void listPending() {
		try {
			sql = "SELECT * FROM udvPending\n" + "WHERE (PNST BETWEEN ? AND ?) ORDER BY PNID";
			pstm = connection.prepareStatement(sql);
			pstm.setInt(1, 0);
			pstm.setInt(2, 6);
			rst = pstm.executeQuery();
			ResultSetMetaData rsmd = rst.getMetaData();
			int noCols = rsmd.getColumnCount();
			String name = "";
			for (int i = 1; i <= noCols; i++) {
				name = rsmd.getColumnName(i);
				System.out.printf(" Col %1$2d : %2$8s", i, name);
				System.out.println();
			}
			System.out.println("PNID\tFAID\tSBID\tSMID\tSGID\tPOID\tPNNO\tSMNM");
			while (rst.next()) {
				System.out.printf("%1$6d %2$6d %3$6d %4$4d %5$11s %6$35s", rst.getInt("PNID"), rst.getShort("FAID"),
						rst.getInt("SBID"), rst.getShort("SMID"), rst.getInt("SGID"), rst.getShort("POID"),
						rst.getString("PNNO"), rst.getString("SMNM"));
				System.out.println();
				noRows++;
			}
			System.out.printf("%1$6d rows", noRows);
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		} finally {
			try {
				rst.close();
			} catch (SQLException e) {
			}
		}
	}

	static void listWorkdays() {
		try {
			rst = stm.executeQuery("SELECT WDID, WDNO, WDTP, WDDT FROM Workdays ORDER BY WDID");
			System.out.println("WDID\tWDNO\tWDTP\tWDDT");
			Calendar c = Calendar.getInstance();
			while (rst.next()) {
				c.setTimeInMillis(rst.getTimestamp("WDDT").getTime());
				System.out.printf("%1$6d %2$6d %3$6s   %5$te/%4$tm/%6$tY", rst.getInt("WDID"), rst.getInt("WDNO"),
						rst.getString("WDTP"), c, c, c);
				System.out.println();
				noRows++;
			}
			System.out.printf("%1$6d rows", noRows);
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		} finally {
			try {
				rst.close();
			} catch (SQLException e) {
			}
		}
	}

	static void predict() {
		// Collect data.
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		obs.add(2011.00, 1000.00);
		obs.add(2012.00, 1020.00);
		obs.add(2013.00, 1040.00);
		obs.add(2014.00, 1030.00);
		obs.add(2015.00, 1050.00);
		obs.add(2016.00, 1060.00);
		obs.add(2017.00, 1070.00);
		obs.add(2018.00, 1080.00);
		obs.add(2019.00, 1070.00);
		// Instantiate a third-degree polynomial fitter.
		final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
		// Retrieve fitted parameters (coefficients of the polynomial function).
		final double[] coeff = fitter.fit(obs.toList());
		for (int i = 0; i < coeff.length; i++) {
			System.out.println("Coeff " + i + ": " + coeff[i]);
		}
		int slope = (int) Math.round(coeff[1]);
		System.out.println("slope: " + slope);
		int year = 2011;
		int count = 1000;
		for (int i = 0; i < 12; i++) {
			year++;
			count += slope;
			System.out.println("Year " + year + ": " + count);
		}
//		BigDecimal bd = new BigDecimal(coeff[1]);
//	    BigDecimal slope = bd.setScale(3, RoundingMode.HALF_UP);
//		System.out.println("Slope: " + slope);
//	    double dSlope = slope.doubleValue();
//		System.out.println("dSlope: " + dSlope);
//		double d2020 = 1160.00 + dSlope;
//		System.out.println("2020: " + d2020);
	}

	static void interpolate() {
		double x[] = { 2011.0, 2012.0, 2013.0, 2014.0, 2015.0, 2016.0 };
		double y[] = { 1000.0, 1010.0, 1020.0, 1030.0, 1040.0, 1050.0 };
		UnivariateInterpolator interpolator = new SplineInterpolator();
		UnivariateFunction function = interpolator.interpolate(x, y);
		double interpolationX[] = { 2017.0, 2018.0, 2010.0 };
		for (int i = 0; i < 3; i++) {
			double interpolatedY = function.value(interpolationX[i]);
			System.out.println("f(" + interpolationX[i] + ") = " + interpolatedY);
		}
	}

	static void extrapolate() {
		int[] years = { 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019 };
		int[] count = { 2000, 2200, 2400, 2300, 2500, 2600, 2700, 2800, 2900 };
		int[] nYears = { 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022 };
		PolynomialFunction func = extrapolate2(years, count);
		System.out.println("Year, Count, Orig");
		int origIndex = 0;
		for (int i = 0; i < nYears.length; i++) {
			double xpol = func.value(nYears[i]);
			int result = (int) Math.round(xpol);
			String out = "" + nYears[i] + ",  " + result;
			if (origIndex < count.length && nYears[i] == years[origIndex]) {
				out += ", " + count[origIndex];
				origIndex++;
			}
			System.out.print(out + "\n");
		}
		;
	}

	static PolynomialFunction extrapolate2(int[] years, int[] count) {
		WeightedObservedPoints obs = new WeightedObservedPoints();
		for (int i = 0; i < years.length; i++) {
			obs.add(years[i], count[i]);
		}
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
		double[] coeff = fitter.fit(obs.toList());
		// make polynomial
		return new PolynomialFunction(coeff);
	}

	static void testToolbar() {
		final short TC_NONE = 0;
		// final short TC_FAC = 1;
		// final short TC_FYB = 321;
		// final short TC_FYBR = 329;
		// final short TC_FYBRS = 457;
		// final short TC_ORG = 2;
		// final short TC_ORT = 4;
		// final short TC_PRS = 16;
		// final short TC_YBR = 328;
		final short TC_FFTG = -31231;
		toByte(TC_NONE);
		toByte(TC_FFTG);
	}

	static void toByte(short codes) {
		final byte TB_FAC = 0;
		final byte TB_ORG = 1;
		final byte TB_ORT = 2;
		final byte TB_PRO = 3;
		final byte TB_PRS = 4;
		final byte TB_SPG = 5;
		final byte TB_SPY = 6;
		final byte TB_STA = 7;
		final byte TB_SUB = 8;
		final byte TB_FROM = 9;
		final byte TB_TO = 10;
		final byte TB_GO = 16;
		System.out.println("Codes: " + codes);
		System.out.println("Facilities:     " + ((codes & (1 << TB_FAC)) != 0));
		System.out.println("OrderGroups:    " + ((codes & (1 << TB_ORG)) != 0));
		System.out.println("OrderTypes:     " + ((codes & (1 << TB_ORT)) != 0));
		System.out.println("Procedures:     " + ((codes & (1 << TB_PRO)) != 0));
		System.out.println("Persons:        " + ((codes & (1 << TB_PRS)) != 0));
		System.out.println("SpecimenGroups: " + ((codes & (1 << TB_SPG)) != 0));
		System.out.println("Specialties:    " + ((codes & (1 << TB_SPY)) != 0));
		System.out.println("Status:         " + ((codes & (1 << TB_STA)) != 0));
		System.out.println("Subspecialties: " + ((codes & (1 << TB_SUB)) != 0));
		System.out.println("From Date:      " + ((codes & (1 << TB_FROM)) != 0));
		System.out.println("To Date:        " + ((codes & (1 << TB_TO)) != 0));
		System.out.println("Go Button:      " + ((codes & (1 << TB_GO)) != 0));
	}

	static void toShort() {
		boolean[] bits = { true, false, false, false, false, false, false, false, false, true, true, false, false,
				false, false, true };
		short value = 0;
		for (byte i = 0; i < 16; i++) {
			if (bits[i]) {
				value |= (1 << i);
			}
		}
		System.out.println("Codes: " + value);
	}

	static void updateComment() {
		try {
			stm.executeUpdate("DROP TABLE " + _SCHEMA + ".Comments");
			sql = "CREATE TABLE " + _SCHEMA + ".Comments (" + "CAID INT NOT NULL REFERENCES Cases (CAID),\n"
					+ "COM1 VARCHAR(2048) NOT NULL,\n" + "COM2 VARCHAR(2048) NOT NULL,\n"
					+ "COM3 VARCHAR(2048) NOT NULL,\n" + "COM4 VARCHAR(2048) NOT NULL,\n" + "PRIMARY KEY (CAID))";
			stm.executeUpdate(sql);
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void updateMasterSpec() {
		try {
			for (int i = 0; i < 21; i++) {
				switch (i) {
				case 0:
					sql = "Update SpeciMaster SET SGID = 49, TAID = 4 WHERE SMID = 2611";
				case 1:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2611";
				case 2:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2801";
				case 3:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2805";
				case 4:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2806";
				case 5:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2810";
				case 6:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2811";
				case 7:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2812";
				case 8:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2813";
				case 9:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2816";
				case 10:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2819";
				case 11:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2820";
				case 12:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2821";
				case 13:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2822";
				case 14:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2823";
				case 15:
					sql = "Update SpeciMaster SET SGID = 241, TAID = 4 WHERE SMID = 2824";
				case 16:
					sql = "Update SpeciMaster SET SGID = 174, TAID = 3 WHERE SMID = 2795";
				case 17:
					sql = "Update SpeciMaster SET SGID = 242, TAID = 7 WHERE SMID = 2803";
				case 18:
					sql = "Update SpeciMaster SET SGID = 243, TAID = 7 WHERE SMID = 2804";
				case 19:
					sql = "Update SpeciMaster SET SGID = 243, TAID = 7 WHERE SMID = 2808";
				default:
					sql = "Update SpeciMaster SET SGID = 243, TAID = 7 WHERE SMID = 2809";
				}
				noRows = stm.executeUpdate(sql);
				System.out.println(sql);
				System.out.printf("%1$6d rows updates", noRows);
				System.out.println();
			}
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void updatePassword() {
		LCrypto crypto = new LCrypto(appDir);
		String[] data = crypto.getData();
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				System.out.println("data " + i + " = " + data[i]);
				// data 0 = mysql
				// data 1 = localhost
				// data 2 = 3306
				// data 3 = PowerJ
				// data 4 = PJClient
				// data 5 = xCyaO06ma
				// private final String[] data = {"MSSQL", "CLPATHPJDB01.eorla.tohss.on.ca",
				// "1433", "PowerJ", "PJClient", "V0wTkl!P92PY$URe34vbnRL"};
				// private final String[] data = {"MSSQL", "CLPATHPJDB01", "1433", "PowerJ",
				// "PJServer", "xCyaO06ma$KO25uFiF!sZrE"};
			}
			if (data.length == 6) {
				data[1] = "CLPATHPJDB01.eorla.tohss.on.ca";
				data[5] = "r3eI;NYn0eS9Lp:XC]_[t";
				crypto.setData(data);
			} else {
				System.out.println("Invalid application binary file");
			}
		}
	}

	static void updatePersonnel() {
		// User access codes (0-31)
		final byte ACCESS_DAILY = 0;
		final byte ACCESS_DIAGN = 1;
		final byte ACCESS_DISTR = 2;
		final byte ACCESS_EMBED = 3;
		final byte ACCESS_FOREC = 4;
		final byte ACCESS_GROSS = 5;
		final byte ACCESS_HISTO = 6;
		final byte ACCESS_NAMES = 7;
		final byte ACCESS_ROUTE = 8;
		final byte ACCESS_SCHED = 9;
		final byte ACCESS_SPECI = 10;
		final byte ACCESS_TURNA = 11;
		final byte ACCESS_WORKD = 12;
		final byte ACCESS_WORKL = 13;
		final byte ACCESS_AUD_ED = 22;
		final byte ACCESS_AUD_ER = 23;
		final byte ACCESS_AUD_FN = 24;
		final byte ACCESS_AUD_PN = 25;
		final byte ACCESS_STP_IE = 26;
		final byte ACCESS_STP_PJ = 27;
		final byte ACCESS_STP_PP = 28;
		final byte ACCESS_STP_PR = 29;
		final byte ACCESS_STP_SC = 30;
		final byte ACCESS_STP_TR = 31;
		// Old access codes
		final byte OLD_Gross = 0;
		final byte OLD_Histology = 1;
		final byte OLD_Diagnosis = 2;
		final byte OLD_ViewNames = 3;
		final byte OLD_Workload = 4;
		final byte OLD_Cases = 9;
		final byte OLD_STATS = 10;
		final byte OLD_REP_WL = 15;
		final byte OLD_SetupDash = 30;
		final byte OLD_SetupWorkload = 31;
		boolean[] oldBits = new boolean[32];
		boolean[] newBits = new boolean[32];
		int oldAccess = 0, newAccess = 0;
		final String sqlUpdate = "UPDATE Persons SET PRVL = ? WHERE PRID = ?";
		try {
			pstm = connection.prepareStatement(sqlUpdate);
			ResultSet rst = stm.executeQuery("SELECT PRID, PRVL FROM dbpj.Persons WHERE PRVL <> 0");
			while (rst.next()) {
				oldAccess = rst.getInt("PRVL");
				if (oldAccess != 0) {
					oldBits = intToBoolean(oldAccess);
					for (int i = 0; i < 32; i++) {
						switch (i) {
						case ACCESS_DAILY:
							newBits[i] = oldBits[OLD_Diagnosis];
							break;
						case ACCESS_DIAGN:
							newBits[i] = oldBits[OLD_Diagnosis];
							break;
						case ACCESS_DISTR:
							newBits[i] = oldBits[OLD_Diagnosis];
							break;
						case ACCESS_EMBED:
							newBits[i] = oldBits[OLD_Histology];
							break;
						case ACCESS_FOREC:
							newBits[i] = oldBits[OLD_REP_WL];
							break;
						case ACCESS_GROSS:
							newBits[i] = oldBits[OLD_Gross];
							break;
						case ACCESS_HISTO:
							newBits[i] = oldBits[OLD_Histology];
							break;
						case ACCESS_NAMES:
							newBits[i] = oldBits[OLD_ViewNames];
							break;
						case ACCESS_ROUTE:
							newBits[i] = (oldBits[OLD_Histology] || oldBits[OLD_Diagnosis]);
							break;
						case ACCESS_SCHED:
							newBits[i] = (oldBits[OLD_Histology] || oldBits[OLD_Diagnosis] || oldBits[OLD_Gross]);
							break;
						case ACCESS_SPECI:
							newBits[i] = oldBits[OLD_STATS];
							break;
						case ACCESS_TURNA:
							newBits[i] = oldBits[OLD_STATS];
							break;
						case ACCESS_WORKD:
							newBits[i] = oldBits[OLD_Diagnosis];
							break;
						case ACCESS_WORKL:
							newBits[i] = oldBits[OLD_Workload];
							break;
						case ACCESS_AUD_ED:
							newBits[i] = oldBits[OLD_SetupWorkload];
							break;
						case ACCESS_AUD_ER:
							newBits[i] = oldBits[OLD_SetupWorkload];
							break;
						case ACCESS_AUD_FN:
							newBits[i] = oldBits[OLD_Cases];
							break;
						case ACCESS_AUD_PN:
							newBits[i] = oldBits[OLD_SetupDash];
							break;
						case ACCESS_STP_IE:
							newBits[i] = oldBits[OLD_SetupWorkload];
							break;
						case ACCESS_STP_PJ:
							newBits[i] = oldBits[OLD_SetupWorkload];
							break;
						case ACCESS_STP_PP:
							newBits[i] = oldBits[OLD_SetupDash];
							break;
						case ACCESS_STP_PR:
							newBits[i] = oldBits[OLD_SetupWorkload];
							break;
						case ACCESS_STP_SC:
							newBits[i] = oldBits[OLD_SetupWorkload];
							break;
						case ACCESS_STP_TR:
							newBits[i] = oldBits[OLD_SetupDash];
							break;
						default:
							newBits[i] = false;
						}
					}
					newAccess = booleanToInt(newBits);
					pstm.setInt(1, newAccess);
					pstm.setInt(2, rst.getShort("PRID"));
					noRows += pstm.executeUpdate();
				}
			}
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void updateSpecGroups() {
		final int[] sgv5 = { 217, 149, 1908, 851, 1081, 1519, 224, 191, 251, 165, 851, 1908, 266, 271, 225, 266, 267,
				238, 341, 265, 1081, 560, 839, 266, 266, 1230, 545, 189, 1519, 1691, 2139, 3681, 217, 2156, 851, 1574,
				861, 369, 1840, 304, 734, 839, 274, 514, 300, 1519, 995, 642, 1088, 1088, 1088, 3846, 1139, 1353, 464,
				753, 3041, 1414, 851, 472, 854, 569, 359, 424, 816, 840, 1008, 344, 545, 2362, 4543, 265, 1670, 956,
				851, 267, 839, 614, 316, 217, 512, 522, 1081, 414, 2189, 1555, 1679, 2057, 2546, 2636, 165, 1532, 1245,
				1304, 381, 2193, 731, 839, 1727, 751, 571, 2294, 4513, 1715, 2904, 1263, 1263, 1134, 1134, 851, 1908,
				839, 1341, 1898, 1170, 1106, 2939, 1519, 1987, 2939, 2939, 161, 786, 1908, 851, 539, 1863, 1131, 296,
				296, 839, 255, 1131, 1081, 268, 238, 206, 1519, 1066, 2114, 638, 2211, 2592, 2682, 3548, 2773, 3605,
				174, 804, 716, 851, 806, 919, 1974, 1065, 806, 691, 580, 688, 580, 612, 215, 786, 839, 1184, 1085, 619,
				373, 1081, 227, 1519, 1236, 1181, 1181, 1181, 1787, 2448, 2990, 1787, 3302, 413, 576, 772, 1908, 2892,
				2892, 851, 413, 324, 209, 280, 1238, 1433, 519, 648, 427, 252, 1081, 316, 316, 1519, 1626, 2322, 3102,
				2096, 2096, 2631, 3384, 3418, 165, 1088, 580, 570, 485, 1088, 839, 825, 737, 281, 498, 1831, 3266, 2509,
				8438, 8237, 1226, 4968, 3723, 2258, 283, 839, 263, 391, 406, 202, 650, 2241, 518, 745, 642, 745, 884,
				1521, 0, 0, 0, 0, 0, 7600, 12619, 16150, 0, 0, 0, 0, 0, 570, 0, 0, 0 };
		final int[] sgid = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
				27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
				53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78,
				79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103,
				104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124,
				125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145,
				146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166,
				167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187,
				188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208,
				209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229,
				230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250,
				251, 252, 253, 254, 255, 256, 257, 258, 259, 260, 261 };
		noRows = 0;
		try {
			sql = "UPDATE SpeciGroups SET SGV5 = ? WHERE SGID = ?";
			pstm = connection.prepareStatement(sql);
			for (int i = 0; i < sgid.length; i++) {
				pstm.setInt(1, sgv5[i]);
				pstm.setInt(2, sgid[i]);
				noRows += pstm.executeUpdate();
			}
			System.out.printf("Updated %1$6d rows in SpeciGroups", noRows);
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void updateSetup() {
		Calendar calNow = Calendar.getInstance();
		calNow.set(Calendar.YEAR, 2019);
		calNow.set(Calendar.MONTH, Calendar.JANUARY);
		calNow.set(Calendar.DAY_OF_MONTH, 1);
		calNow.set(Calendar.HOUR_OF_DAY, 0);
		calNow.set(Calendar.MINUTE, 0);
		calNow.set(Calendar.SECOND, 0);
		calNow.set(Calendar.MILLISECOND, 0);
		Long value = calNow.getTimeInMillis();
		try {
			sql = "UPDATE Setup SET STVA = ? WHERE STID = ?";
			pstm = connection.prepareStatement(sql);
			pstm.setString(1, String.valueOf(value));
			pstm.setInt(2, LSetup.VAR_MIN_WL_DATE);
			noRows += pstm.executeUpdate();
			calNow.set(Calendar.YEAR, 2021);
			value = calNow.getTimeInMillis();
			pstm.setString(1, String.valueOf(value));
			pstm.setInt(2, LSetup.VAR_V5_LAST);
			noRows += pstm.executeUpdate();
			System.out.printf("Updated %1$6d rows in Setup", noRows);
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}

	static void updateView() {
		try {
			stm.executeUpdate("DROP VIEW " + _SCHEMA + ".udvSpecimens");
			sql = "CREATE VIEW " + _SCHEMA + ".udvSpecimens AS\n"
					+ "SELECT s.SPID, s.CAID, s.SMID, s.SPBL, s.SPSL, s.SPFR, s.SPHE,\n"
					+ "s.SPSS, s.SPIH, s.SPMO, s.SPV5, s.SPV1, s.SPV2, s.SPV3, s.SPV4,\n"
					+ "s.SPDC, c.CANO, m.SGID, m.SMNM, m.SMDC, g.SGDC, g.POID, r.PONM\n" + "FROM Specimens AS s\n"
					+ "INNER JOIN Cases AS c ON c.CAID = s.CAID\n" + "INNER JOIN SpeciMaster AS m ON m.SMID = s.SMID\n"
					+ "INNER JOIN SpeciGroups AS g ON g.SGID = m.SGID\n"
					+ "INNER JOIN Procedures AS r ON r.POID = g.POID";
			stm.executeUpdate(sql);
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}
}