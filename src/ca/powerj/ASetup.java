package ca.powerj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

class ASetup {
	private final byte DB_DERBY = 1;
	private final byte DB_MARIA = 2;
	private final byte DB_MSSQL = 3;
	private final byte DB_POSTG = 4;
	private byte errorID = LConstants.ERROR_NONE;
	private int noRows = 0;
	private int dbID = 0;
	private int sysID = 0;
	private final String dbSchema = "dbpj";
	private final String sysUserClient = "PJClient";
	private String dbName = "Database";
	private String sysPassClient = "";
	private String appDir = "";
	private String dbArch = "";
	private String dbHost = "";
	private String dbPort = "";
	private String dbUser = "";
	private String dbPass = "";
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private StringBuilder buffer = new StringBuilder();
	private SecureRandom randomNo = new SecureRandom();
	private ArrayList<Object> list = new ArrayList<>();
	private Connection connection = null;
	private Statement stm = null;

	ASetup(String[] args) {
		initialize(args);
	}

	private void close() {
		try {
			if (stm != null) {
				if (!stm.isClosed())
					stm.close();
			}
		} catch (SQLException ignore) {
		}
		try {
			if (connection != null) {
				if (!connection.isClosed()) {
					connection.close();
				}
			}
		} catch (Exception ignore) {
		}
		try {
			if (dbID == DB_DERBY) {
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			}
		} catch (Exception ignore) {
		}
	}

	private void createDB(int dbID) {
		String url = "";
		try {
			switch (dbID) {
			case DB_DERBY:
				// Define physical location of Derby Database
				Properties p = System.getProperties();
				p.setProperty("derby.system.home", appDir);
				url = "jdbc:derby:" + dbSchema + ";create=true;";
				break;
			case DB_MARIA:
				url = "jdbc:mariadb://" + dbHost + ":" + dbPort + "?user=" + dbUser + "&password=" + dbPass;
				break;
			case DB_POSTG:
				url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/?user=" + dbUser + "&password=" + dbPass;
				break;
			default:
				// Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				url = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";user=" + dbUser + ";password=" + dbPass + ";";
			}
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection(url);
			stm = connection.createStatement();
			switch (dbID) {
			case DB_MARIA:
				execute("CREATE SCHEMA " + dbSchema + " DEFAULT CHARACTER SET utf8");
				if (errorID == LConstants.ERROR_NONE) {
					execute("USE " + dbSchema);
				}
				break;
			case DB_POSTG:
				// Cannot create database and schema in the same connection
				// Must disconnect, then reconnect
				execute("CREATE DATABASE " + dbSchema);
				if (errorID == LConstants.ERROR_NONE) {
					connection.close();
				}
				if (errorID == LConstants.ERROR_NONE) {
					url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbSchema + "?user=" + dbUser
							+ "&password=" + dbPass;
					connection = DriverManager.getConnection(url);
					stm = connection.createStatement();
				}
				if (errorID == LConstants.ERROR_NONE) {
					execute("CREATE SCHEMA " + dbSchema);
				}
				if (errorID == LConstants.ERROR_NONE) {
					execute("SET search_path TO " + dbSchema);
				}
				break;
			case DB_MSSQL:
				execute("CREATE DATABASE " + dbSchema);
				if (errorID == LConstants.ERROR_NONE) {
					execute("USE " + dbSchema);
				}
				break;
			default:
				execute("CREATE SCHEMA " + dbSchema);
				if (errorID == LConstants.ERROR_NONE) {
					execute("SET SCHEMA " + dbSchema);
				}
			}
			log(LConstants.ERROR_NONE, "Created database.");
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void createProcMaria() {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 47; i++) {
			switch (i) {
			case 0:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpAccessions()\n" + "BEGIN\n"
						+ "SELECT * FROM udvAccessions ORDER BY ACNM;\n" + "END";
				break;
			case 1:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpAdditionals(param1 INT)\n" + "BEGIN\n"
						+ "SELECT PRID, ADCD, ADV5, ADV1, ADV2, ADV3, ADV4, ADDT, PRNM, PRLS, PRFR, CANO\n"
						+ "FROM udvAdditionals WHERE CAID = param1 ORDER BY ADDT;\n" + "END";
				break;
			case 2:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpAddSum(param1 DATETIME, param2 DATETIME)\n" + "BEGIN\n"
						+ "SELECT FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR, COUNT(CAID) AS ADCA,\n"
						+ "SUM(CAST(ADV5 as INT)) AS ADV5, SUM(ADV1) AS ADV1, SUM(ADV2) AS ADV2, SUM(ADV3) AS ADV3, SUM(ADV4) AS ADV4\n"
						+ "FROM udvAdditionals WHERE (ADDT BETWEEN param1 AND param2)\n"
						+ "GROUP BY FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR\n"
						+ "ORDER BY FAID, SYID, SBID, POID, PRID;\n" + "END";
				break;
			case 3:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCoder1()\n" + "BEGIN\n"
						+ "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder1 ORDER BY CONM;\n" + "END";
				break;
			case 4:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCoder2()\n" + "BEGIN\n"
						+ "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder2 ORDER BY CONM;\n" + "END";
				break;
			case 5:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCoder3()\n" + "BEGIN\n"
						+ "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder3 ORDER BY CONM;\n" + "END";
				break;
			case 6:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCoder4()\n" + "BEGIN\n"
						+ "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder4 ORDER BY CONM;\n" + "END";
				break;
			case 7:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCmt(param1 INT)\n" + "BEGIN\n"
						+ "SELECT COM1, COM2, COM3, COM4 FROM Comments WHERE CAID = param1;\n" + "END";
				break;
			case 8:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCseID(param1 CHAR(12))\n" + "BEGIN\n"
						+ "SELECT CAID FROM Cases WHERE CANO = param1;\n" + "END";
				break;
			case 9:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCseNo(param1 INT)\n" + "BEGIN\n"
						+ "SELECT CANO FROM Cases WHERE CAID = param1;\n" + "END";
				break;
			case 10:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCseSpe(param1 INT)\n" + "BEGIN\n"
						+ "SELECT c.SMID, c.FNED, c.CANO, s.SPID FROM Cases AS c INNER JOIN Specimens AS s ON s.CAID = c.CAID\n"
						+ "AND s.SMID = c.SMID WHERE c.CAID = param1;\n" + "END";
				break;
			case 11:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCseSum(param1 DATETIME, param2 DATETIME)\n" + "BEGIN\n"
						+ "SELECT FAID, SYID, SBID, POID, FNID, FANM, SYNM, SBNM, SBDC, PONM, FNNM, FNLS, FNFR, COUNT(CAID) AS CACA,\n"
						+ "SUM(CAST(CASP as INT)) AS CASP, SUM(CAST(CABL as INT)) AS CABL, SUM(CAST(CASL as INT)) AS CASL, SUM(CAST(CAHE as INT)) AS CAHE,\n"
						+ "SUM(CAST(CASS as INT)) AS CASS, SUM(CAST(CAIH as INT)) AS CAIH, SUM(CAST(CAMO as INT)) AS CAMO, SUM(CAST(CAFS as INT)) AS CAFS,\n"
						+ "SUM(CAST(CASY as INT)) AS CASY, SUM(CAST(GRTA as INT)) AS GRTA, SUM(CAST(EMTA as INT)) AS EMTA, SUM(CAST(MITA as INT)) AS MITA,\n"
						+ "SUM(CAST(ROTA as INT)) AS ROTA, SUM(CAST(FNTA as INT)) AS FNTA, SUM(CAST(CAV5 as INT)) AS CAV5, SUM(CAV1) AS CAV1, SUM(CAV2) AS CAV2,\n"
						+ "SUM(CAV3) AS CAV3, SUM(CAV4) AS CAV4 FROM udvCases WHERE (FNED BETWEEN param1 AND param2)\n"
						+ "GROUP BY FAID, SYID, SBID, POID, FNID, FANM, SYNM, SBNM, SBDC, PONM, FNNM, FNLS, FNFR\n"
						+ "ORDER BY FAID, SYID, SBID, POID, FNID;\n" + "END";
				break;
			case 12:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpCseYear(param1 DATETIME, param2 DATETIME)\n" + "BEGIN\n"
						+ "SELECT c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, date_part('year', c.fned) as yearID\n"
						+ "count(caid) as cases, sum(c.casp) as casp, sum(c.cabl) as cabl, sum(c.casl) as casl, sum(c.cahe) as cahe,\n"
						+ "sum(c.cass) as cass, sum(c.caih) as caih, sum(c.casy) as casy, sum(c.cafs) as cafs, sum(c.cav1) as cav1,\n"
						+ "sum(c.cav2) as cav2, sum(c.cav3) as cav3, sum(c.cav4) as cav4, sum(c.cav5) as cav5\n"
						+ "FROM dbcases c\n" + "INNER JOIN dbfacilities f ON f.faid = c.faid\n"
						+ "INNER JOIN dbsubspecial b ON b.sbid = c.sbid\n"
						+ "INNER JOIN dbspecimaster m ON m.smid = c.smid\n"
						+ "INNER JOIN dbspecigroups g ON g.sgid = m.sgid\n"
						+ "INNER JOIN dbprocedures r ON r.poid = g.poid\n"
						+ "INNER JOIN dbspecialties y ON y.syid = b.syid\n"
						+ "WHERE c.fned BETWEEN param1 AND param1 \n"
						+ "GROUP BY c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, yearID\n"
						+ "ORDER BY c.faid, b.syid, c.sbid, g.poid, yearID\n" + "END";
				break;
			case 13:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpErrSelect()\n" + "BEGIN\n"
						+ "SELECT CAID, ERID, CANO FROM Errors WHERE ERID > 0 ORDER BY CANO;\n" + "END";
				break;
			case 14:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpErrCmt(param1 INT)\n" + "BEGIN\n"
						+ "SELECT ERDC FROM Errors WHERE CAID = param1;\n" + "END";
				break;
			case 15:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpErrRedo()\n" + "BEGIN\n"
						+ "SELECT CAID FROM Errors WHERE ERID = 0 ORDER BY CAID;\n" + "END";
				break;
			case 16:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpFacility()\n" + "BEGIN\n"
						+ "SELECT FAID, FAFL, FALD, FANM, FADC FROM Facilities ORDER BY FANM;\n" + "END";
				break;
			case 17:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpFrzSID(param1 INT)\n" + "BEGIN\n"
						+ "SELECT PRID, FRBL, FRSL, FRV5, FRV1, FRV2, FRV3, FRV4, PRNM, PRLS,\n"
						+ "SPDC, SMNM FROM udvFrozens WHERE SPID = param1;" + "END";
				break;
			case 18:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpFrzSu5(param1 DATETIME, param2 DATETIME)\n" + "BEGIN\n"
						+ "SELECT COUNT(*) AS QTY, SUM(FRV1) AS FRV1, SUM(FRV2) AS FRV2,\n"
						+ "SUM(FRV3) AS FRV3, SUM(FRV4) AS FRV4\n"
						+ "FROM udvFrozens WHERE ACED BETWEEN param1 AND param2;\n" + "END";
			case 19:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpFrzSum(param1 DATETIME, param2 DATETIME)\n" + "BEGIN\n"
						+ "SELECT FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR, COUNT(SPID) AS FRSP,\n"
						+ "SUM(CAST(FRBL as INT)) AS FRBL, SUM(CAST(FRSL as INT)) AS FRSL, SUM(CAST(FRV5 as INT)) AS FRV5, SUM(FRV1) AS FRV1, SUM(FRV2) AS FRV2,\n"
						+ "SUM(FRV3) AS FRV3, SUM(FRV4) AS FRV4 FROM udvFrozens WHERE (ACED BETWEEN param1 AND param2)\n"
						+ "GROUP BY FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR\n"
						+ "ORDER BY FAID, SYID, SBID, POID, PRID;\n" + "END";
				break;
			case 20:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpOrder(param1 INT)\n" + "BEGIN\n"
						+ "SELECT ORQY, ORV1, ORV2, ORV3, ORV4, OGNM FROM udvOrders WHERE SPID = param1 ORDER BY OGNM;\n"
						+ "END";
				break;
			case 21:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpOrderGroup()\n" + "BEGIN\n"
						+ "SELECT * FROM udvOrderGroups ORDER BY OGNM;\n" + "END";
				break;
			case 22:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpOrderMaster()\n" + "BEGIN\n"
						+ "SELECT * FROM udvOrderMaster ORDER BY OMNM;\n" + "END";
				break;
			case 23:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpPending()\n" + "BEGIN\n"
						+ "SELECT * FROM udvPending ORDER BY PNID;\n" + "END";
				break;
			case 24:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpPendingRouted(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n" + "SELECT * FROM udvPending\n" + "WHERE (ROED BETWEEN param1 AND param2)\n"
						+ "ORDER BY PNID;\n" + "END";
				break;
			case 25:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpProcedure()\n" + "BEGIN\n"
						+ "SELECT POID, PONM, PODC FROM Procedures ORDER BY PONM;\n" + "END";
				break;
			case 26:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpPrsName()\n" + "BEGIN\n"
						+ "SELECT * FROM Persons ORDER BY PRNM;\n" + "END";
				break;
			case 27:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpPrsID(param1 SMALLINT)\n" + "BEGIN\n"
						+ "SELECT PRVL FROM Persons WHERE PRID = param1;\n" + "END";
				break;
			case 28:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpRule()\n" + "BEGIN\n"
						+ "SELECT RUID, RUNM, RUDC FROM Rules ORDER BY RUID;\n" + "END";
				break;
			case 29:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSchedServ(param1 DATE, param2 DATE)\n" + "BEGIN\n"
						+ "SELECT WDID, SRID, PRID, PRNM, SRNM\n" + "FROM udvSchedules\n"
						+ "WHERE WDDT BETWEEN param1 AND param2 \n" + "ORDER BY SRNM, WDID;\n" + "END";
				break;
			case 30:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSchedStaff(param1 DATE, param2 DATE)\n" + "BEGIN\n"
						+ "SELECT WDID, SRID, PRID, PRNM, SRNM\n" + "FROM udvSchedules\n"
						+ "WHERE WDDT BETWEEN param1 AND param2 \n" + "ORDER BY PRNM, WDID, SRNM;\n" + "END";
				break;
			case 31:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSchedSum(param1 DATE, param2 DATE)\n" + "BEGIN\n"
						+ "SELECT * FROM udvSchedules\n" + "WHERE WDDT BETWEEN param1 AND param2 \n"
						+ "ORDER BY FAID, PRID, WDID, SRID\n" + "END";
				break;
			case 32:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSpecimens(param1 INT)\n" + "BEGIN\n"
						+ "SELECT SPID, SMID, SPBL, SPSL, SPFR, SPHE, SPSS, SPIH, \n"
						+ "SPMO, SPV5, SPV1, SPV2, SPV3, SPV4, SPDC, SMNM, SMDC, PONM\n"
						+ "FROM udvSpecimens WHERE CAID = param1 ORDER BY SPID;\n" + "END";
				break;
			case 33:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSpecGroup()\n" + "BEGIN\n"
						+ "SELECT * FROM udvSpeciGroups ORDER BY SGDC;\n" + "END";
				break;
			case 34:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSpecMaster()\n" + "BEGIN\n"
						+ "SELECT * FROM udvSpeciMaster ORDER BY SMNM;\n" + "END";
				break;
			case 35:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSpecSu5(param1 DATE, param2 DATE)\n" + "BEGIN\n"
						+ "SELECT g.sgid, COUNT(s.spid) AS qty, SUM(s.spv1) AS spv1,\n"
						+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4\n"
						+ "FROM specigroups g INNER JOIN specimaster m ON g.sgid = m.sgid\n"
						+ "INNER JOIN specimens s ON m.smid = s.smid INNER JOIN cases c ON c.caid = s.caid\n"
						+ "WHERE c.fned BETWEEN param1 AND param2\n" + "GROUP BY g.sgid";
				break;
			case 36:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSpecSum(param1 DATE, param2 DATE)\n" + "BEGIN\n"
						+ "SELECT b.syid, g.sbid, g.sgid, c.faid, c.fnid, y.synm, b.sbnm, b.sbdc, g.sgdc, f.fanm,\n"
						+ "p.prnm, p.prls, p.prfr, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
						+ "SUM(s.sphe) AS sphe, SUM(s.spss) AS spss, SUM(s.spih) AS spih, SUM(s.spv1) AS spv1,\n"
						+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, SUM(s.spv5) AS spv5\n"
						+ "FROM specigroups g INNER JOIN specimaster m ON g.sgid = m.sgid\n"
						+ "INNER JOIN specimens s ON m.smid = s.smid INNER JOIN cases c ON c.caid = s.caid\n"
						+ "INNER JOIN subspecial b ON b.sbid = g.sbid INNER JOIN specialties y ON y.syid = b.syid\n"
						+ "INNER JOIN facilities f ON f.faid = c.faid INNER JOIN persons p ON p.prid = c.fnid\n"
						+ "WHERE c.fned BETWEEN param1 AND param2\n"
						+ "GROUP BY b.syid, g.sbid, g.sgid, c.faid, c.fnid, y.synm, b.sbnm, b.sbdc, g.sgdc, f.fanm,\n"
						+ "p.prnm, p.prls, p.prfr";
				break;
			case 37:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSpecialty()\n" + "BEGIN\n"
						+ "SELECT * FROM Specialties ORDER BY SYNM;\n" + "END";
				break;
			case 38:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpService()\n" + "BEGIN\n"
						+ "SELECT * FROM udvServices ORDER BY SRNM;\n" + "END";
				break;
			case 39:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSetup()\n" + "BEGIN\n"
						+ "SELECT STID, STVA FROM Setup ORDER BY STID;\n" + "END";
				break;
			case 40:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpStpID(param1 SMALLINT)\n" + "BEGIN\n"
						+ "SELECT STVA FROM Setup WHERE STID = param1;\n" + "END";
				break;
			case 41:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpSubspecial()\n" + "BEGIN\n"
						+ "SELECT * FROM udvSubspecial ORDER BY SBNM;\n" + "END";
				break;
			case 42:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpTurnaround()\n" + "BEGIN\n"
						+ "SELECT TAID, GRSS, EMBD, MICR, ROUT, FINL, TANM\n" + "FROM Turnaround ORDER BY TANM;\n"
						+ "END";
				break;
			case 43:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpWdy(param1 DATE)\n" + "BEGIN\n"
						+ "SELECT WDID, WDNO, WDTP, WDDT FROM Workdays\n" + "WHERE WDDT >= param1 ORDER BY WDDT;\n"
						+ "END";
				break;
			case 44:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpWdyDte(param1 DATE)\n" + "BEGIN\n"
						+ "SELECT WDNO FROM Workdays WHERE WDDT = param1;\n" + "END";
				break;
			case 45:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpWdyNxt(param1 DATE)\n" + "BEGIN\n"
						+ "SELECT MIN(WDDT) AS WDDT FROM Workdays\n" + "WHERE WDDT > param1 AND WDTP = 'D';\n" + "END";
				break;
			default:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpWdyPrv(param1 DATE)\n" + "BEGIN\n"
						+ "SELECT MAX(WDDT) AS WDDT FROM Workdays\n" + "WHERE WDDT < param1 AND WDTP = 'D';\n" + "END";
			}
			execute(sql);
			if (errorID != LConstants.ERROR_NONE) {
				break;
			}
			noRows++;
			log(LConstants.ERROR_NONE, "Created " + noRows + " procedures.");
		}
		try {
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	private void createProcMSSQL() {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 47; i++) {
			switch (i) {
			case 0:
				sql = "CREATE PROCEDURE udpAccessions \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM udvAccessions ORDER BY ACNM;\n" + "END";
				break;
			case 1:
				sql = "CREATE PROCEDURE udpAdditionals @param1 INT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT PRID, ADCD, ADV5, ADV1, ADV2, ADV3, ADV4, ADDT, PRNM, PRLS, PRFR, CANO\n"
						+ "FROM udvAdditionals WHERE CAID = @param1 ORDER BY ADDT;\n" + "END";
				break;
			case 2:
				sql = "CREATE PROCEDURE udpAddSum @param1 DATETIME, @param2 DATETIME \n" + "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR, COUNT(CAID) AS ADCA,\n"
						+ "SUM(CAST(ADV5 as INT)) AS ADV5, SUM(ADV1) AS ADV1, SUM(ADV2) AS ADV2, SUM(ADV3) AS ADV3, SUM(ADV4) AS ADV4\n"
						+ "FROM udvAdditionals WHERE (ADDT BETWEEN @param1 AND @param2 \n"
						+ "GROUP BY FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR\n"
						+ "ORDER BY FAID, SYID, SBID, POID, PRID;\n" + "END";
				break;
			case 3:
				sql = "CREATE PROCEDURE udpCoder1 \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder1 ORDER BY CONM;\n" + "END";
				break;
			case 4:
				sql = "CREATE PROCEDURE udpCoder2 \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder2 ORDER BY CONM;\n" + "END";
				break;
			case 5:
				sql = "CREATE PROCEDURE udpCoder3 \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder3 ORDER BY CONM;\n" + "END";
				break;
			case 6:
				sql = "CREATE PROCEDURE udpCoder4 \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC FROM Coder4 ORDER BY CONM;\n" + "END";
				break;
			case 7:
				sql = "CREATE PROCEDURE udpCmt @param1 INT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT COM1, COM2, COM3, COM4 FROM Comments WHERE CAID = @param1;\n" + "END";
				break;
			case 8:
				sql = "CREATE PROCEDURE udpCseID @param1 CHAR(12) \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT CAID FROM Cases WHERE CANO = @param1;\n" + "END";
				break;
			case 9:
				sql = "CREATE PROCEDURE udpCseNo @param1 INT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT CANO FROM Cases WHERE CAID = @param1;\n" + "END";
				break;
			case 10:
				sql = "CREATE PROCEDURE udpCseSpe @param1 INT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT c.SMID, c.FNED, c.CANO, s.SPID FROM Cases AS c INNER JOIN Specimens AS s ON s.CAID = c.CAID\n"
						+ "AND s.SMID = c.SMID WHERE c.CAID = @param1;\n" + "END";
				break;
			case 11:
				sql = "CREATE PROCEDURE udpCseSum @param1 DATETIME, @param2 DATETIME \n" + "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT FAID, SYID, SBID, POID, FNID, FANM, SYNM, SBNM, SBDC, PONM, FNNM, FNLS, FNFR, COUNT(CAID) AS CACA,\n"
						+ "SUM(CAST(CASP as INT)) AS CASP, SUM(CAST(CABL as INT)) AS CABL, SUM(CAST(CASL as INT)) AS CASL, SUM(CAST(CAHE as INT)) AS CAHE,\n"
						+ "SUM(CAST(CASS as INT)) AS CASS, SUM(CAST(CAIH as INT)) AS CAIH, SUM(CAST(CAMO as INT)) AS CAMO, SUM(CAST(CAFS as INT)) AS CAFS,\n"
						+ "SUM(CAST(CASY as INT)) AS CASY, SUM(CAST(GRTA as INT)) AS GRTA, SUM(CAST(EMTA as INT)) AS EMTA, SUM(CAST(MITA as INT)) AS MITA,\n"
						+ "SUM(CAST(ROTA as INT)) AS ROTA, SUM(CAST(FNTA as INT)) AS FNTA, SUM(CAST(CAV5 as INT)) AS CAV5, SUM(CAV1) AS CAV1, SUM(CAV2) AS CAV2,\n"
						+ "SUM(CAV3) AS CAV3, SUM(CAV4) AS CAV4 FROM udvCases WHERE (FNED BETWEEN @param1 AND @param2)\n"
						+ "GROUP BY FAID, SYID, SBID, POID, FNID, FANM, SYNM, SBNM, SBDC, PONM, FNNM, FNLS, FNFR\n"
						+ "ORDER BY FAID, SYID, SBID, POID, FNID;\n" + "END";
				break;
			case 12:
				sql = "CREATE PROCEDURE udpCseYear @param1 DATETIME, @param2 DATETIME \n" + "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, DATEPART(YEAR, c.fned) as yearID\n"
						+ "count(caid) as cases, sum(c.casp) as casp, sum(c.cabl) as cabl, sum(c.casl) as casl, sum(c.cahe) as cahe,\n"
						+ "sum(c.cass) as cass, sum(c.caih) as caih, sum(c.casy) as casy, sum(c.cafs) as cafs, sum(c.cav1) as cav1,\n"
						+ "sum(c.cav2) as cav2, sum(c.cav3) as cav3, sum(c.cav4) as cav4, sum(c.cav5) as cav5\n"
						+ "FROM dbcases c\n" + "INNER JOIN dbfacilities f ON f.faid = c.faid\n"
						+ "INNER JOIN dbsubspecial b ON b.sbid = c.sbid\n"
						+ "INNER JOIN dbspecimaster m ON m.smid = c.smid\n"
						+ "INNER JOIN dbspecigroups g ON g.sgid = m.sgid\n"
						+ "INNER JOIN dbprocedures r ON r.poid = g.poid\n"
						+ "INNER JOIN dbspecialties y ON y.syid = b.syid\n"
						+ "WHERE c.fned BETWEEN @param1 AND @param1 \n"
						+ "GROUP BY c.faid, b.syid, c.sbid, g.poid, f.fanm, y.synm, b.sbnm, r.ponm, yearID\n"
						+ "ORDER BY c.faid, b.syid, c.sbid, g.poid, yearID\n" + "END";
				break;
			case 13:
				sql = "CREATE PROCEDURE udpErrSelect \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT CAID, ERID, CANO FROM Errors WHERE ERID > 0 ORDER BY CANO;\n" + "END";
				break;
			case 14:
				sql = "CREATE PROCEDURE udpErrCmt @param1 INT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT ERDC FROM Errors WHERE CAID = @param1;\n" + "END";
				break;
			case 15:
				sql = "CREATE PROCEDURE udpErrRedo \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT CAID FROM Errors WHERE ERID = 0 ORDER BY CAID;\n" + "END";
				break;
			case 16:
				sql = "CREATE PROCEDURE udpFacility \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT FAID, FAFL, FALD, FANM, FADC FROM Facilities ORDER BY FANM;\n" + "END";
				break;
			case 17:
				sql = "CREATE PROCEDURE udpFrzSID @param1 INT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT PRID, FRBL, FRSL, FRV5, FRV1, FRV2, FRV3, FRV4, PRNM, PRLS,\n"
						+ "SPDC, SMNM FROM udvFrozens WHERE SPID = @param1;" + "END";
				break;
			case 18:
				sql = "CREATE PROCEDURE udpFrzSu5 @param1 DATETIME, @param2 DATETIME \n" + "BEGIN\n"
						+ "SELECT COUNT(*) AS QTY, SUM(FRV1) AS FRV1, SUM(FRV2) AS FRV2,\n"
						+ "SUM(FRV3) AS FRV3, SUM(FRV4) AS FRV4\n"
						+ "FROM udvFrozens WHERE ACED BETWEEN @param1 AND @param2;\n" + "END";
			case 19:
				sql = "CREATE PROCEDURE udpFrzSum @param1 DATETIME, @param2 DATETIME \n" + "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR, COUNT(SPID) AS FRSP,\n"
						+ "SUM(CAST(FRBL as INT)) AS FRBL, SUM(CAST(FRSL as INT)) AS FRSL, SUM(CAST(FRV5 as INT)) AS FRV5, SUM(FRV1) AS FRV1, SUM(FRV2) AS FRV2,\n"
						+ "SUM(FRV3) AS FRV3, SUM(FRV4) AS FRV4 FROM udvFrozens WHERE (ACED BETWEEN @param1 AND @param2)\n"
						+ "GROUP BY FAID, SYID, SBID, POID, PRID, FANM, SYNM, SBNM, PONM, PRNM, PRLS, PRFR\n"
						+ "ORDER BY FAID, SYID, SBID, POID, PRID;\n" + "END";
				break;
			case 20:
				sql = "CREATE PROCEDURE udpOrder @param1 INT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT ORQY, ORV1, ORV2, ORV3, ORV4, OGNM FROM udvOrders WHERE SPID = @param1 ORDER BY OGNM;\n"
						+ "END";
				break;
			case 21:
				sql = "CREATE PROCEDURE udpOrderGroup \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM udvOrderGroups ORDER BY OGNM;\n" + "END";
				break;
			case 22:
				sql = "CREATE PROCEDURE udpOrderMaster \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM udvOrderMaster ORDER BY OMNM;\n" + "END";
				break;
			case 23:
				sql = "CREATE PROCEDURE udpPending \n" + "BEGIN\n" + "SELECT * FROM udvPending ORDER BY PNID;\n"
						+ "END";
				break;
			case 24:
				sql = "CREATE PROCEDURE udpPendingRouted @param1 DATETIME, @param2 DATETIME \n" + "BEGIN\n"
						+ "SET NOCOUNT ON;\n" + "SELECT * FROM udvPending\n"
						+ "WHERE (ROED BETWEEN @param1 AND @param2)\n" + "ORDER BY PNID;\n" + "END";
				break;
			case 25:
				sql = "CREATE PROCEDURE udpProcedure \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT POID, PONM, PODC FROM Procedures ORDER BY PONM;\n" + "END";
				break;
			case 26:
				sql = "CREATE PROCEDURE udpPrsName \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM Persons ORDER BY PRNM;\n" + "END";
				break;
			case 27:
				sql = "CREATE PROCEDURE udpPrsID @param1 SMALLINT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT PRVL FROM Persons WHERE PRID = @param1;\n" + "END";
				break;
			case 28:
				sql = "CREATE PROCEDURE udpRule \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT RUID, RUNM, RUDC FROM Rules ORDER BY RUID;\n" + "END";
				break;
			case 29:
				sql = "CREATE PROCEDURE udpSchedServ @param1 DATE, @param2 DATE \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT WDID, SRID, PRID, PRNM, SRNM\n" + "FROM udvSchedules\n"
						+ "WHERE WDDT BETWEEN @param1 AND @param2 \n" + "ORDER BY SRNM, WDID;\n" + "END";
				break;
			case 30:
				sql = "CREATE PROCEDURE udpSchedStaff @param1 DATE, @param2 DATE \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT WDID, SRID, PRID, PRNM, SRNM\n" + "FROM udvSchedules\n"
						+ "WHERE WDDT BETWEEN @param1 AND @param2 \n" + "ORDER BY PRNM, WDID, SRNM;\n" + "END";
				break;
			case 31:
				sql = "CREATE PROCEDURE udpSchedSum @param1 DATE, @param2 DATE \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM udvSchedules\n" + "WHERE WDDT BETWEEN @param1 AND @param2 \n"
						+ "ORDER BY FAID, PRID, WDID, SRID\n" + "END";
				break;
			case 32:
				sql = "CREATE PROCEDURE udpSpecimens @param1 INT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT SPID, SMID, SPBL, SPSL, SPFR, SPHE, SPSS, SPIH, SPMO,\n"
						+ "SPV5, SPV1, SPV2, SPV3, SPV4, SPDC, SMNM, SMDC, PONM\n"
						+ "FROM udvSpecimens WHERE CAID = @param1 ORDER BY SPID;\n" + "END";
				break;
			case 33:
				sql = "CREATE PROCEDURE udpSpecGroup \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM udvSpeciGroups ORDER BY SGDC;\n" + "END";
				break;
			case 34:
				sql = "CREATE PROCEDURE udpSpecMaster \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM udvSpeciMaster ORDER BY SMNM;\n" + "END";
				break;
			case 35:
				sql = "CREATE PROCEDURE udpSpecSu5 @param1 DATE, @param2 DATE \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT g.sgid, COUNT(s.spid) AS qty, SUM(s.spv1) AS spv1,\n"
						+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4\n"
						+ "FROM specigroups g INNER JOIN specimaster m ON g.sgid = m.sgid\n"
						+ "INNER JOIN specimens s ON m.smid = s.smid INNER JOIN cases c ON c.caid = s.caid\n"
						+ "WHERE c.fned BETWEEN @param1 AND @param2\n" + "GROUP BY g.sgid";
				break;
			case 36:
				sql = "CREATE PROCEDURE udpSpecSum @param1 DATE, @param2 DATE \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT b.syid, g.sbid, g.sgid, c.faid, c.fnid, y.synm, b.sbnm, b.sbdc, g.sgdc, f.fanm,\n"
						+ "p.prnm, p.prls, p.prfr, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
						+ "SUM(s.sphe) AS sphe, SUM(s.spss) AS spss, SUM(s.spih) AS spih, SUM(s.spv1) AS spv1,\n"
						+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, SUM(s.spv5) AS spv5\n"
						+ "FROM specigroups g INNER JOIN specimaster m ON g.sgid = m.sgid\n"
						+ "INNER JOIN specimens s ON m.smid = s.smid INNER JOIN cases c ON c.caid = s.caid\n"
						+ "INNER JOIN subspecial b ON b.sbid = g.sbid INNER JOIN specialties y ON y.syid = b.syid\n"
						+ "INNER JOIN facilities f ON f.faid = c.faid INNER JOIN persons p ON p.prid = c.fnid\n"
						+ "WHERE c.fned BETWEEN @param1 AND @param2\n"
						+ "GROUP BY b.syid, g.sbid, g.sgid, c.faid, c.fnid, y.synm, b.sbnm, b.sbdc, g.sgdc, f.fanm,\n"
						+ "p.prnm, p.prls, p.prfr\n"
						+ "ORDER BY y.synm, b.sbnm, b.sbdc, g.sgdc, f.fanm, p.prnm, p.prls, p.prfr";
				break;
			case 37:
				sql = "CREATE PROCEDURE udpSpecialty \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM Specialties ORDER BY SYNM;\n" + "END";
				break;
			case 38:
				sql = "CREATE PROCEDURE udpService \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM udvServices ORDER BY SRNM;\n" + "END";
				break;
			case 39:
				sql = "CREATE PROCEDURE udpSetup \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT STID, STVA FROM Setup ORDER BY STID;\n" + "END";
				break;
			case 40:
				sql = "CREATE PROCEDURE udpStpID @param1 SMALLINT \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT STVA FROM Setup WHERE STID = @param1;\n" + "END";
				break;
			case 41:
				sql = "CREATE PROCEDURE udpSubspecial \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT * FROM udvSubspecial ORDER BY SBNM;\n" + "END";
				break;
			case 42:
				sql = "CREATE PROCEDURE udpTurnaround \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT TAID, GRSS, EMBD, MICR, ROUT, FINL, TANM\n" + "FROM Turnaround ORDER BY TANM;\n"
						+ "END";
				break;
			case 43:
				sql = "CREATE PROCEDURE udpWdy @param1 DATE \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT WDID, WDNO, WDTP, WDDT FROM Workdays\n" + "WHERE WDDT >= @param1 ORDER BY WDDT;\n"
						+ "END";
				break;
			case 44:
				sql = "CREATE PROCEDURE udpWdyDte @param1 DATE \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT WDNO FROM Workdays WHERE WDDT = @param1;\n" + "END";
				break;
			case 45:
				sql = "CREATE PROCEDURE udpWdyNxt @param1 DATE \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT MIN(WDDT) AS WDDT FROM Workdays\n" + "WHERE WDDT > @param1 AND WDTP = 'D';\n" + "END";
				break;
			default:
				sql = "CREATE PROCEDURE udpWdyPrv @param1 DATE \n" + "BEGIN\n" + "SET NOCOUNT ON;\n"
						+ "SELECT MAX(WDDT) AS WDDT FROM Workdays\n" + "WHERE WDDT < @param1 AND WDTP = 'D';\n" + "END";
			}
			execute(sql);
			if (errorID != LConstants.ERROR_NONE) {
				break;
			}
			noRows++;
			log(LConstants.ERROR_NONE, "Created " + noRows + " procedures.");
		}
		try {
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	private void createTables(int dbID) {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 29; i++) {
			switch (i) {
			case 0:
				sql = "CREATE TABLE " + dbSchema + ".Setup (" + "STID SMALLINT PRIMARY KEY,\n"
						+ "STVA VARCHAR(64) NOT NULL)";
				break;
			case 1:
				sql = "CREATE TABLE " + dbSchema + ".Workdays (" + "WDID INT PRIMARY KEY,\n" + "WDNO INT NOT NULL,\n"
						+ "WDTP CHAR(1) NOT NULL,\n" + "WDDT DATE UNIQUE NOT NULL)";
				break;
			case 2:
				sql = "CREATE TABLE " + dbSchema + ".Errors (" + "CAID INT PRIMARY KEY,\n" + "ERID SMALLINT NOT NULL,\n"
						+ "CANO CHAR(13) UNIQUE NOT NULL,\n" + "ERDC VARCHAR(2048) NOT NULL)";
				break;
			case 3:
				sql = "CREATE TABLE " + dbSchema + ".Facilities (" + "FAID SMALLINT PRIMARY KEY,\n"
						+ "FAFL CHAR(1) NOT NULL,\n" + "FALD CHAR(1) NOT NULL,\n" + "FANM VARCHAR(4) UNIQUE NOT NULL,\n"
						+ "FADC VARCHAR(80) NOT NULL)";
				break;
			case 4:
				sql = "CREATE TABLE " + dbSchema + ".Persons (" + "PRID SMALLINT PRIMARY KEY,\n"
						+ "PRVL INT NOT NULL,\n" + "PRDT DATE NOT NULL,\n" + "PRAC CHAR(1) NOT NULL,\n"
						+ "PRCD CHAR(2) NOT NULL,\n" + "PRNM CHAR(3) NOT NULL,\n" + "PRLS VARCHAR(30) NOT NULL,\n"
						+ "PRFR VARCHAR(30) NOT NULL)";
				break;
			case 5:
				sql = "CREATE TABLE " + dbSchema + ".Procedures (" + "POID SMALLINT PRIMARY KEY,\n"
						+ "PONM VARCHAR(16) UNIQUE NOT NULL,\n" + "PODC VARCHAR(256) UNIQUE NOT NULL)";
				break;
			case 6:
				sql = "CREATE TABLE " + dbSchema + ".Rules (" + "RUID SMALLINT PRIMARY KEY,\n"
						+ "RUNM VARCHAR(16) UNIQUE NOT NULL,\n" + "RUDC VARCHAR(256) NOT NULL)";
				break;
			case 7:
				sql = "CREATE TABLE " + dbSchema + ".Specialties (" + "SYID SMALLINT PRIMARY KEY,\n"
						+ "SYFL CHAR(1) NOT NULL,\n" + "SYLD CHAR(1) NOT NULL,\n" + "SYSP CHAR(1) NOT NULL,\n"
						+ "SYNM VARCHAR(16) UNIQUE NOT NULL)";
				break;
			case 8:
				sql = "CREATE TABLE " + dbSchema + ".Turnaround (" + "TAID SMALLINT PRIMARY KEY,\n"
						+ "GRSS SMALLINT NOT NULL,\n" + "EMBD SMALLINT NOT NULL,\n" + "MICR SMALLINT NOT NULL,\n"
						+ "ROUT SMALLINT NOT NULL,\n" + "FINL SMALLINT NOT NULL,\n"
						+ "TANM VARCHAR(16) UNIQUE NOT NULL)";
				break;
			case 9:
				sql = "CREATE TABLE " + dbSchema + ".Coder1 (" + "COID SMALLINT PRIMARY KEY,\n"
						+ "RUID SMALLINT NOT NULL REFERENCES Rules (RUID),\n" + "COQY SMALLINT NOT NULL,\n"
						+ "COV1 DECIMAL(5, 3) NOT NULL,\n" + "COV2 DECIMAL(5, 3) NOT NULL,\n"
						+ "COV3 DECIMAL(5, 3) NOT NULL,\n" + "CONM VARCHAR(16) UNIQUE NOT NULL,\n"
						+ "CODC VARCHAR(256) NOT NULL)";
				break;
			case 10:
				sql = "CREATE TABLE " + dbSchema + ".Coder2 (" + "COID SMALLINT PRIMARY KEY,\n"
						+ "RUID SMALLINT NOT NULL REFERENCES Rules (RUID),\n" + "COQY SMALLINT NOT NULL,\n"
						+ "COV1 DECIMAL(5, 3) NOT NULL,\n" + "COV2 DECIMAL(5, 3) NOT NULL,\n"
						+ "COV3 DECIMAL(5, 3) NOT NULL,\n" + "CONM VARCHAR(16) UNIQUE NOT NULL,\n"
						+ "CODC VARCHAR(256) NOT NULL)";
				break;
			case 11:
				sql = "CREATE TABLE " + dbSchema + ".Coder3 (" + "COID SMALLINT PRIMARY KEY,\n"
						+ "RUID SMALLINT NOT NULL REFERENCES Rules (RUID),\n" + "COQY SMALLINT NOT NULL,\n"
						+ "COV1 DECIMAL(5, 3) NOT NULL,\n" + "COV2 DECIMAL(5, 3) NOT NULL,\n"
						+ "COV3 DECIMAL(5, 3) NOT NULL,\n" + "CONM VARCHAR(16) UNIQUE NOT NULL,\n"
						+ "CODC VARCHAR(128) NOT NULL)";
				break;
			case 12:
				sql = "CREATE TABLE " + dbSchema + ".Coder4 (" + "COID SMALLINT PRIMARY KEY,\n"
						+ "RUID SMALLINT NOT NULL REFERENCES Rules (RUID),\n" + "COQY SMALLINT NOT NULL,\n"
						+ "COV1 DECIMAL(5, 3) NOT NULL,\n" + "COV2 DECIMAL(5, 3) NOT NULL,\n"
						+ "COV3 DECIMAL(5, 3) NOT NULL,\n" + "CONM VARCHAR(16) UNIQUE NOT NULL,\n"
						+ "CODC VARCHAR(256) NOT NULL)";
				break;
			case 13:
				sql = "CREATE TABLE " + dbSchema + ".Accessions (" + "ACID SMALLINT PRIMARY KEY,\n"
						+ "SYID SMALLINT NOT NULL REFERENCES Specialties (SYID),\n" + "ACFL CHAR(1) NOT NULL,\n"
						+ "ACLD CHAR(1) NOT NULL,\n" + "ACNM VARCHAR(30) UNIQUE NOT NULL)";
				break;
			case 14:
				sql = "CREATE TABLE " + dbSchema + ".Subspecial (" + "SBID SMALLINT PRIMARY KEY,\n"
						+ "SYID SMALLINT NOT NULL REFERENCES Specialties (SYID),\n"
						+ "SBNM VARCHAR(8) UNIQUE NOT NULL,\n" + "SBDC VARCHAR(32) NOT NULL)";
				break;
			case 15:
				sql = "CREATE TABLE " + dbSchema + ".OrderTypes (" + "OTID SMALLINT PRIMARY KEY,\n"
						+ "OTNM VARCHAR(8) UNIQUE NOT NULL)";
				break;
			case 16:
				sql = "CREATE TABLE " + dbSchema + ".OrderGroups (" + "OGID SMALLINT PRIMARY KEY,\n"
						+ "OTID SMALLINT NOT NULL REFERENCES OrderTypes (OTID),\n"
						+ "OGC1 SMALLINT NOT NULL REFERENCES Coder1 (COID),\n"
						+ "OGC2 SMALLINT NOT NULL REFERENCES Coder2 (COID),\n"
						+ "OGC3 SMALLINT NOT NULL REFERENCES Coder3 (COID),\n"
						+ "OGC4 SMALLINT NOT NULL REFERENCES Coder4 (COID),\n"
						+ "OGC5 INT NOT NULL, OGNM VARCHAR(8) UNIQUE NOT NULL,\n" + "OGDC VARCHAR(64) NOT NULL)";
				break;
			case 17:
				sql = "CREATE TABLE " + dbSchema + ".OrderMaster (" + "OMID SMALLINT PRIMARY KEY,\n"
						+ "OGID SMALLINT NOT NULL REFERENCES OrderGroups (OGID),\n"
						+ "OMNM VARCHAR(15) UNIQUE NOT NULL,\n" + "OMDC VARCHAR(80) NOT NULL)";
				break;
			case 18:
				sql = "CREATE TABLE " + dbSchema + ".SpeciGroups (" + "SGID SMALLINT PRIMARY KEY,\n"
						+ "SBID SMALLINT NOT NULL REFERENCES Subspecial (SBID),\n"
						+ "POID SMALLINT NOT NULL REFERENCES Procedures (POID),\n"
						+ "SG1B SMALLINT NOT NULL REFERENCES Coder1 (COID),\n"
						+ "SG1M SMALLINT NOT NULL REFERENCES Coder1 (COID),\n"
						+ "SG1R SMALLINT NOT NULL REFERENCES Coder1 (COID),\n"
						+ "SG2B SMALLINT NOT NULL REFERENCES Coder2 (COID),\n"
						+ "SG2M SMALLINT NOT NULL REFERENCES Coder2 (COID),\n"
						+ "SG2R SMALLINT NOT NULL REFERENCES Coder2 (COID),\n"
						+ "SG3B SMALLINT NOT NULL REFERENCES Coder3 (COID),\n"
						+ "SG3M SMALLINT NOT NULL REFERENCES Coder3 (COID),\n"
						+ "SG3R SMALLINT NOT NULL REFERENCES Coder3 (COID),\n"
						+ "SG4B SMALLINT NOT NULL REFERENCES Coder4 (COID),\n"
						+ "SG4M SMALLINT NOT NULL REFERENCES Coder4 (COID),\n"
						+ "SG4R SMALLINT NOT NULL REFERENCES Coder4 (COID),\n"
						+ "SGV5 INT NOT NULL, SGLN CHAR(1) NOT NULL,\n" + "SGDC VARCHAR(64) UNIQUE NOT NULL)";
				break;
			case 19:
				sql = "CREATE TABLE " + dbSchema + ".SpeciMaster (" + "SMID SMALLINT PRIMARY KEY,\n"
						+ "SGID SMALLINT NOT NULL REFERENCES SpeciGroups (SGID),\n"
						+ "TAID SMALLINT NOT NULL REFERENCES Turnaround (TAID),\n"
						+ "SMNM VARCHAR(16) UNIQUE NOT NULL,\n" + "SMDC VARCHAR(80) NOT NULL)";
				break;
			case 20:
				sql = "CREATE TABLE " + dbSchema + ".Services (" + "SRID SMALLINT PRIMARY KEY,\n"
						+ "FAID SMALLINT NOT NULL REFERENCES Facilities (FAID),\n"
						+ "SBID SMALLINT NOT NULL REFERENCES Subspecial (SBID),\n" + "SRCD SMALLINT NOT NULL,\n"
						+ "SRNM VARCHAR(8) UNIQUE NOT NULL,\n" + "SRDC VARCHAR(64) NOT NULL)";
				break;
			case 21:
				sql = "CREATE TABLE " + dbSchema + ".Schedules (" + "WDID INT NOT NULL REFERENCES Workdays (WDID),\n"
						+ "SRID SMALLINT NOT NULL REFERENCES Services (SRID),\n"
						+ "PRID SMALLINT NOT NULL REFERENCES Persons (PRID),\n" + "PRIMARY KEY(WDID, SRID, PRID))";
				break;
			case 22:
				sql = "CREATE TABLE " + dbSchema + ".Cases (" + "CAID INT PRIMARY KEY,\n"
						+ "FAID SMALLINT NOT NULL REFERENCES Facilities (FAID),\n"
						+ "SBID SMALLINT NOT NULL REFERENCES Subspecial (SBID),\n"
						+ "SMID SMALLINT NOT NULL REFERENCES SpeciMaster (SMID),\n"
						+ "GRID SMALLINT NOT NULL REFERENCES Persons (PRID),\n"
						+ "EMID SMALLINT NOT NULL REFERENCES Persons (PRID),\n"
						+ "MIID SMALLINT NOT NULL REFERENCES Persons (PRID),\n"
						+ "ROID SMALLINT NOT NULL REFERENCES Persons (PRID),\n"
						+ "FNID SMALLINT NOT NULL REFERENCES Persons (PRID), GRTA SMALLINT NOT NULL,\n"
						+ "EMTA SMALLINT NOT NULL, MITA SMALLINT NOT NULL, ROTA SMALLINT NOT NULL,\n"
						+ "FNTA SMALLINT NOT NULL, CASP SMALLINT NOT NULL, CABL SMALLINT NOT NULL,\n"
						+ "CASL SMALLINT NOT NULL, CASY SMALLINT NOT NULL, CAFS SMALLINT NOT NULL,\n"
						+ "CAHE SMALLINT NOT NULL, CASS SMALLINT NOT NULL, CAIH SMALLINT NOT NULL,\n"
						+ "CAMO SMALLINT NOT NULL, CAV5 INT NOT NULL, ACED DATETIME NOT NULL,\n"
						+ "GRED DATETIME NOT NULL, EMED DATETIME NOT NULL, MIED DATETIME NOT NULL,\n"
						+ "ROED DATETIME NOT NULL, FNED DATETIME NOT NULL, CAV1 DECIMAL(5, 3) NOT NULL,\n"
						+ "CAV2 DECIMAL(5, 3) NOT NULL, CAV3 DECIMAL(5, 3) NOT NULL,\n"
						+ "CAV4 DECIMAL(5, 3) NOT NULL, CANO CHAR(13) UNIQUE NOT NULL)";
				break;
			case 23:
				sql = "CREATE TABLE " + dbSchema + ".Specimens (" + "SPID INT PRIMARY KEY,\n"
						+ "CAID INT NOT NULL REFERENCES Cases (CAID),\n"
						+ "SMID SMALLINT NOT NULL REFERENCES SpeciMaster (SMID), SPBL SMALLINT NOT NULL,\n"
						+ "SPSL SMALLINT NOT NULL, SPFR SMALLINT NOT NULL, SPHE SMALLINT NOT NULL,\n"
						+ "SPSS SMALLINT NOT NULL, SPIH SMALLINT NOT NULL, SPMO SMALLINT NOT NULL,\n"
						+ "SPV5 INT NOT NULL, SPV1 DECIMAL(5, 3) NOT NULL,\n"
						+ "SPV2 DECIMAL(5, 3) NOT NULL, SPV3 DECIMAL(5, 3) NOT NULL,\n"
						+ "SPV4 DECIMAL(5, 3) NOT NULL, SPDC VARCHAR(80) NOT NULL)";
				break;
			case 24:
				sql = "CREATE TABLE " + dbSchema + ".Orders (" + "SPID INT NOT NULL REFERENCES Specimens (SPID),\n"
						+ "OGID SMALLINT NOT NULL REFERENCES OrderGroups (OGID), ORQY SMALLINT NOT NULL,\n"
						+ "ORV1 DECIMAL(5, 3) NOT NULL, ORV2 DECIMAL(5, 3) NOT NULL,\n"
						+ "ORV3 DECIMAL(5, 3) NOT NULL, ORV4 DECIMAL(5, 3) NOT NULL,\n" + "PRIMARY KEY (SPID, OGID))";
				break;
			case 25:
				sql = "CREATE TABLE " + dbSchema + ".Frozens (" + "SPID INT PRIMARY KEY REFERENCES Specimens (SPID),\n"
						+ "PRID SMALLINT NOT NULL REFERENCES Persons (PRID), FRBL SMALLINT NOT NULL,\n"
						+ "FRSL SMALLINT NOT NULL, FRV5 INT NOT NULL, FRV1 DECIMAL(5, 3) NOT NULL,\n"
						+ "FRV2 DECIMAL(5, 3) NOT NULL, FRV3 DECIMAL(5, 3) NOT NULL,\n"
						+ "FRV4 DECIMAL(5, 3) NOT NULL)";
				break;
			case 26:
				sql = "CREATE TABLE " + dbSchema + ".Additionals (" + "CAID INT NOT NULL REFERENCES Cases (CAID),\n"
						+ "PRID SMALLINT NOT NULL REFERENCES Persons (PRID), ADCD SMALLINT NOT NULL,\n"
						+ "ADV5 INT NOT NULL, ADDT DATETIME NOT NULL, ADV1 DECIMAL(5, 3) NOT NULL,\n"
						+ "ADV2 DECIMAL(5, 3) NOT NULL, ADV3 DECIMAL(5, 3) NOT NULL,\n"
						+ "ADV4 DECIMAL(5, 3) NOT NULL, PRIMARY KEY (CAID, PRID, ADCD, ADDT))";
				break;
			case 27:
				sql = "CREATE TABLE " + dbSchema + ".Comments (" + "CAID INT PRIMARY KEY REFERENCES Cases (CAID),\n"
						+ "COM1 VARCHAR(2048) NOT NULL, COM2 VARCHAR(2048) NOT NULL,\n"
						+ "COM3 VARCHAR(2048) NOT NULL, COM4 VARCHAR(2048) NOT NULL)";
				break;
			default:
				sql = "CREATE TABLE " + dbSchema + ".Pending (" + "PNID INT PRIMARY KEY,\n"
						+ "FAID SMALLINT NOT NULL REFERENCES Facilities (FAID),\n"
						+ "SBID SMALLINT NOT NULL REFERENCES Subspecial (SBID),\n"
						+ "SMID SMALLINT NOT NULL REFERENCES SpeciMaster (SMID),\n"
						+ "POID SMALLINT NOT NULL REFERENCES Procedures (POID),\n"
						+ "GRID SMALLINT NOT NULL REFERENCES Persons (PRID),\n"
						+ "EMID SMALLINT NOT NULL REFERENCES Persons (PRID),\n"
						+ "MIID SMALLINT NOT NULL REFERENCES Persons (PRID),\n"
						+ "ROID SMALLINT NOT NULL REFERENCES Persons (PRID),\n"
						+ "FNID SMALLINT NOT NULL REFERENCES Persons (PRID), GRTA SMALLINT NOT NULL,\n"
						+ "EMTA SMALLINT NOT NULL, MITA SMALLINT NOT NULL, ROTA SMALLINT NOT NULL,\n"
						+ "FNTA SMALLINT NOT NULL, PNST SMALLINT NOT NULL, PNSP SMALLINT NOT NULL,\n"
						+ "PNBL SMALLINT NOT NULL, PNSL SMALLINT NOT NULL, PNV5 INT NOT NULL,\n"
						+ "ACED DATETIME NOT NULL, GRED DATETIME NOT NULL, EMED DATETIME NOT NULL,\n"
						+ "MIED DATETIME NOT NULL, ROED DATETIME NOT NULL, FNED DATETIME NOT NULL,\n"
						+ "PNNO CHAR(13) UNIQUE NOT NULL)";
			}
			if (dbID == DB_MARIA) {
				sql += " ENGINE = InnoDB;";
			} else if (dbID == DB_DERBY || dbID == DB_POSTG) {
				if (noRows > 21) {
					sql = sql.replace("DATETIME", "TIMESTAMP");
				}
			}
			execute(sql);
			if (errorID != LConstants.ERROR_NONE) {
				break;
			}
			noRows++;
			log(LConstants.ERROR_NONE, "Created " + noRows + " tables.");
		}
		try {
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	private void createUsers(int dbID) {
		if (dbID == DB_POSTG) {
			if (errorID == LConstants.ERROR_NONE) {
				execute("CREATE ROLE " + sysUserClient + " LOGIN PASSWORD '" + sysPassClient
						+ "' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;");
			}
			if (errorID == LConstants.ERROR_NONE) {
//				execute("GRANT SELECT, INSERT, DELETE, UPDATE, EXECUTE ON ALL TABLES IN SCHEMA " +
//				dbSchema + " TO " + sysUserServer + ";");
				execute("GRANT USAGE ON SCHEMA " + dbSchema + " TO " + sysUserClient + ";");
			}
		} else if (dbID == DB_MARIA) {
			if (errorID == LConstants.ERROR_NONE) {
				execute("CREATE USER " + sysUserClient + "@'%' IDENTIFIED BY '" + sysPassClient + "';");
			}
			if (errorID == LConstants.ERROR_NONE) {
				execute("GRANT SELECT, INSERT, DELETE, UPDATE, EXECUTE ON " + dbSchema + ".* TO " + sysUserClient
						+ "@'%';");
			}
			execute("FLUSH PRIVILEGES;");
		} else {
			if (errorID == LConstants.ERROR_NONE) {
				execute("CREATE USER " + sysUserClient + " WITH PASSWORD = '" + sysPassClient + "', DEFAULT_SCHEMA = "
						+ dbSchema);
			}
			if (errorID == LConstants.ERROR_NONE) {
				execute("exec sp_addrolemember db_datareader, " + sysUserClient);
			}
			if (errorID == LConstants.ERROR_NONE) {
				execute("exec sp_addrolemember db_datawriter, " + sysUserClient);
			}
			if (errorID == LConstants.ERROR_NONE) {
				execute("GRANT EXECUTE TO " + sysUserClient);
			}
		}
	}

	private void createViews(int dbID) {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 19; i++) {
			switch (i) {
			case 0:
				sql = "CREATE VIEW " + dbSchema + ".udvAccessions AS\n"
						+ "SELECT a.ACID, a.SYID, a.ACFL, a.ACLD, a.ACNM,\n" + "y.SYFL, y.SYLD, y.SYSP, y.SYNM\n"
						+ "FROM Accessions AS a\n" + "INNER JOIN Specialties AS y ON y.SYID = a.SYID";
				break;
			case 1:
				sql = "CREATE VIEW " + dbSchema + ".udvAdditionals AS\n"
						+ "SELECT a.CAID, a.PRID, a.ADCD, a.ADV5, a.ADV1, a.ADV2,\n"
						+ "a.ADV3, a.ADV4, a.ADDT, p.PRNM, p.PRLS, p.PRFR, c.FAID,\n"
						+ "c.CANO, f.FANM, g.POID, g.SBID, r.PONM, b.SYID, b.SBNM, y.SYNM\n" + "FROM Additionals AS a\n"
						+ "INNER JOIN Persons AS p ON p.PRID = a.PRID\n" + "INNER JOIN Cases AS c ON c.CAID = a.CAID\n"
						+ "INNER JOIN Facilities AS f ON f.FAID = c.FAID\n"
						+ "INNER JOIN SpeciMaster AS m ON m.SMID = c.SMID\n"
						+ "INNER JOIN SpeciGroups AS g ON g.SGID = m.SGID\n"
						+ "INNER JOIN Procedures AS r ON r.POID = g.POID\n"
						+ "INNER JOIN Subspecial AS b ON b.SBID = g.SBID\n"
						+ "INNER JOIN Specialties AS y ON y.SYID = b.SYID";
				break;
			case 2:
				sql = "CREATE VIEW " + dbSchema + ".udvCases AS\n"
						+ "SELECT c.CAID, c.FAID, c.SBID, c.SMID, c.GRID, c.EMID,\n"
						+ "c.MIID, c.ROID, c.FNID, c.GRTA, c.EMTA, c.MITA, c.ROTA,\n"
						+ "c.FNTA, c.CASP, c.CABL, c.CASL, c.CASY, c.CAFS, c.CAHE,\n"
						+ "c.CASS, c.CAIH, c.CAMO, c.CAV5, c.ACED, c.GRED, c.EMED,\n"
						+ "c.MIED, c.ROED, c.FNED, c.CAV1, c.CAV2, c.CAV3, c.CAV4,\n"
						+ "c.CANO, f.FANM, m.SMNM, m.SMDC, g.POID, r.PONM, b.SYID,\n"
						+ "b.SBNM, b.SBDC, y.SYNM, pg.PRNM AS GRNM, pg.PRLS AS GRLS,\n"
						+ "pg.PRFR AS GRFR, pe.PRNM AS EMNM, pe.PRLS AS EMLS,\n"
						+ "pe.PRFR AS EMFR, pm.PRNM AS MINM, pm.PRLS AS MILS,\n"
						+ "pm.PRFR AS MIFR, pr.PRNM AS RONM, pr.PRLS AS ROLS,\n"
						+ "pr.PRFR AS ROFR, pf.PRNM AS FNNM, pf.PRLS AS FNLS,\n" + "pf.PRFR AS FNFR FROM Cases AS c\n"
						+ "INNER JOIN Facilities AS f ON f.FAID = c.FAID\n"
						+ "INNER JOIN Subspecial AS b ON b.SBID = c.SBID\n"
						+ "INNER JOIN SpeciMaster AS m ON m.SMID = c.SMID\n"
						+ "INNER JOIN Persons AS pg ON pg.PRID = c.GRID\n"
						+ "INNER JOIN Persons AS pe ON pe.PRID = c.EMID\n"
						+ "INNER JOIN Persons AS pm ON pm.PRID = c.MIID\n"
						+ "INNER JOIN Persons AS pr ON pr.PRID = c.ROID\n"
						+ "INNER JOIN Persons AS pf ON pf.PRID = c.FNID\n"
						+ "INNER JOIN SpeciGroups AS g ON g.SGID = m.SGID\n"
						+ "INNER JOIN Procedures AS r ON r.POID = g.POID\n"
						+ "INNER JOIN Specialties AS y ON y.SYID = b.SYID";
				break;
			case 3:
				sql = "CREATE VIEW " + dbSchema + ".udvCasesLast AS\n" + "SELECT MAX(FNED) AS FNED FROM Cases";
				break;
			case 4:
				sql = "CREATE VIEW " + dbSchema + ".udvFrozens AS\n"
						+ "SELECT z.SPID, z.PRID, z.FRBL, z.FRSL, z.FRV5,\n"
						+ "z.FRV1, z.FRV2, z.FRV3, z.FRV4, c.CAID, c.FAID,\n"
						+ "c.ACED, c.CANO, p.PRNM, p.PRLS, p.PRFR, s.SPDC,\n"
						+ "m.SMNM, m.SMDC, f.FANM, g.POID, g.SBID, r.PONM,\n" + "b.SYID, b.SBNM, y.SYNM\n"
						+ "FROM Frozens AS z\n" + "INNER JOIN Specimens AS s ON s.SPID = z.SPID\n"
						+ "INNER JOIN Persons AS p ON p.PRID = z.PRID\n" + "INNER JOIN Cases AS c ON c.CAID = s.CAID\n"
						+ "INNER JOIN SpeciMaster AS m ON m.SMID = s.SMID\n"
						+ "INNER JOIN Facilities AS f ON f.FAID = c.FAID\n"
						+ "INNER JOIN SpeciGroups AS g ON g.SGID = m.SGID\n"
						+ "INNER JOIN Procedures AS r ON r.POID = g.POID\n"
						+ "INNER JOIN Subspecial AS b ON b.SBID = g.SBID\n"
						+ "INNER JOIN Specialties AS y ON y.SYID = b.SYID";
				break;
			case 5:
				sql = "CREATE VIEW " + dbSchema + ".udvOrderGroups AS\n"
						+ "SELECT g.OGID, g.OTID, g.OGC1, g.OGC2, g.OGC3, g.OGC4,\n"
						+ "g.OGC5, g.OGNM, g.OGDC, a.CONM AS C1NM, b.CONM AS C2NM,\n"
						+ "c.CONM AS C3NM, d.CONM AS C4NM, t.OTNM\n" + "FROM OrderGroups AS g\n"
						+ "INNER JOIN OrderTypes AS t ON t.OTID = g.OTID\n"
						+ "INNER JOIN Coder1 AS a ON a.COID = g.OGC1\n" + "INNER JOIN Coder2 AS b ON b.COID = g.OGC2\n"
						+ "INNER JOIN Coder3 AS c ON c.COID = g.OGC3\n" + "INNER JOIN Coder4 AS d ON d.COID = g.OGC4";
				break;
			case 6:
				sql = "CREATE VIEW " + dbSchema + ".udvOrderMaster AS\n"
						+ "SELECT m.OMID, m.OGID, m.OMNM, m.OMDC, g.OTID,\n"
						+ "g.OGC1, g.OGC2, g.OGC3, g.OGC4, g.OGC5, g.OGNM,\n"
						+ "g.OGDC, a.CONM AS C1NM, b.CONM AS C2NM, c.CONM AS C3NM,\n"
						+ "d.CONM AS C4NM, t.OTNM FROM OrderMaster AS m\n"
						+ "INNER JOIN OrderGroups AS g ON g.OGID = m.OGID\n"
						+ "INNER JOIN OrderTypes AS t ON t.OTID = g.OTID\n"
						+ "INNER JOIN Coder1 AS a ON a.COID = g.OGC1\n" + "INNER JOIN Coder2 AS b ON b.COID = g.OGC2\n"
						+ "INNER JOIN Coder3 AS c ON c.COID = g.OGC3\n" + "INNER JOIN Coder4 AS d ON d.COID = g.OGC4";
				break;
			case 7:
				sql = "CREATE VIEW " + dbSchema + ".udvOrders AS\n" + "SELECT o.SPID, o.OGID, o.ORQY, o.ORV1,\n"
						+ "o.ORV2, o.ORV3, o.ORV4, g.OGNM\n" + "FROM Orders AS o\n"
						+ "INNER JOIN OrderGroups AS g ON g.OGID = o.OGID";
				break;
			case 8:
				sql = "CREATE VIEW " + dbSchema + ".udvPending AS\n"
						+ "SELECT p.PNID, p.FAID, p.SBID, p.SMID, p.GRID, p.EMID,\n"
						+ "p.MIID, p.ROID, p.FNID, p.GRTA, p.EMTA, p.MITA, p.ROTA,\n"
						+ "p.FNTA, p.PNST, p.PNSP, p.PNBL, p.PNSL, p.PNV5, p.ACED,\n"
						+ "p.GRED, p.EMED, p.MIED, p.ROED, p.FNED, p.PNNO, f.FANM,\n"
						+ "m.SGID, m.TAID, m.SMNM, m.SMDC, g.POID, r.PONM, b.SBNM,\n"
						+ "b.SBDC, b.SYID, y.SYNM, pg.PRNM AS GRNM, pg.PRLS AS GRLS,\n"
						+ "pg.PRFR AS GRFR, pe.PRNM AS EMNM, pe.PRLS AS EMLS,\n"
						+ "pe.PRFR AS EMFR, pm.PRNM AS MINM, pm.PRLS AS MILS,\n"
						+ "pm.PRFR AS MIFR, pr.PRNM AS RONM, pr.PRLS AS ROLS,\n"
						+ "pr.PRFR AS ROFR, pf.PRNM AS FNNM, pf.PRLS AS FNLS,\n" + "pf.PRFR AS FNFR FROM Pending AS p\n"
						+ "INNER JOIN Facilities AS f ON f.FAID = p.FAID\n"
						+ "INNER JOIN Subspecial AS b ON b.SBID = p.SBID\n"
						+ "INNER JOIN SpeciMaster AS m ON m.SMID = p.SMID\n"
						+ "INNER JOIN Persons AS pg ON pg.PRID = p.GRID\n"
						+ "INNER JOIN Persons AS pe ON pe.PRID = p.EMID\n"
						+ "INNER JOIN Persons AS pm ON pm.PRID = p.MIID\n"
						+ "INNER JOIN Persons AS pr ON pr.PRID = p.ROID\n"
						+ "INNER JOIN Persons AS pf ON pf.PRID = p.FNID\n"
						+ "INNER JOIN SpeciGroups AS g ON g.SGID = m.SGID\n"
						+ "INNER JOIN Procedures AS r ON r.POID = g.POID\n"
						+ "INNER JOIN Specialties AS y ON y.SYID = b.SYID";
				break;
			case 9:
				sql = "CREATE VIEW " + dbSchema + ".udvPendingLast AS\n" + "SELECT MAX(ACED) AS ACED FROM Pending";
				break;
			case 10:
				sql = "CREATE VIEW " + dbSchema + ".udvSchedules AS\n"
						+ "SELECT s.WDID, s.SRID, s.PRID, v.FAID, v.SBID,\n"
						+ "v.SRCD, v.SRNM, v.SRDC, p.PRNM, p.PRLS, p.PRFR,\n"
						+ "w.WDDT, b.SYID, b.SBNM, y.SYNM, f.FANM\n" + "FROM Schedules AS s\n"
						+ "INNER JOIN Services AS v ON v.SRID = s.SRID\n"
						+ "INNER JOIN Persons AS p ON p.PRID = s.PRID\n"
						+ "INNER JOIN Workdays AS w ON w.WDID = s.WDID\n"
						+ "INNER JOIN Facilities AS f ON f.faid = v.faid\n"
						+ "INNER JOIN Subspecial AS b ON b.sbid = v.sbid\n"
						+ "INNER JOIN Specialties AS y ON y.syid = b.syid";
				break;
			case 11:
				switch (dbID) {
				case DB_POSTG:
					sql = "CREATE VIEW " + dbSchema + ".udvSchedWeeks AS\n" + "SELECT DISTINCT s.wdid, w.wddt\n"
							+ "FROM Schedules AS s\n  INNER JOIN Workdays AS w ON w.WDID = s.WDID\n"
							+ "WHERE date_part('dow', w.wddt) = 1\n" + "ORDER BY w.wddt";
					break;
				case DB_MSSQL:
					sql = "CREATE VIEW " + dbSchema + ".udvSchedWeeks AS\n" + "SELECT DISTINCT s.wdid, w.wddt\n"
							+ "FROM Schedules AS s\n  INNER JOIN Workdays AS w ON w.WDID = s.WDID\n"
							+ "WHERE DATEPART(weekday, w.wddt) = 1\n";
					break;
				case DB_MARIA:
					sql = "CREATE VIEW " + dbSchema + ".udvSchedWeeks AS\n" + "SELECT DISTINCT s.wdid, w.wddt\n"
							+ "FROM Schedules AS s\n  INNER JOIN Workdays AS w ON w.WDID = s.WDID\n"
							+ "WHERE DAYOFWEEK(w.wddt) = 2";
					break;
				default:
					sql = "CREATE VIEW " + dbSchema + ".udvSchedWeeks AS\n" + "SELECT wdid, wddt\n" + "FROM Workdays\n"
							+ "ORDER BY wddt DESC FETCH FIRST 370 ROWS ONLY";
				}
				break;
			case 12:
				sql = "CREATE VIEW " + dbSchema + ".udvServices AS\n"
						+ "SELECT v.SRID, v.FAID, v.SBID, v.SRCD, v.SRNM,\n"
						+ "v.SRDC, f.FANM, b.SYID, b.SBNM, y.SYNM\n" + "FROM Services AS v\n"
						+ "INNER JOIN Facilities AS f ON f.FAID = v.FAID\n"
						+ "INNER JOIN Subspecial AS b ON b.SBID = v.SBID\n"
						+ "INNER JOIN Specialties AS y ON y.SYID = b.SYID";
				break;
			case 13:
				sql = "CREATE VIEW " + dbSchema + ".udvSpeciGroups AS\n"
						+ "SELECT g.SGID, g.SBID, g.POID, g.SG1B, g.SG1M, g.SG1R,\n"
						+ "g.SG2B, g.SG2M, g.SG2R, g.SG3B, g.SG3M, g.SG3R, g.SG4B,\n"
						+ "g.SG4M, g.SG4R, g.SGV5, g.SGLN, g.SGDC, r.PONM, b.SYID,\n"
						+ "b.SBNM, b.SBDC, y.SYNM, b1.CONM AS C1NB, m1.CONM AS C1NM,\n"
						+ "r1.CONM AS C1NR, b2.CONM AS C2NB, m2.CONM AS C2NM,\n"
						+ "r2.CONM AS C2NR, b3.CONM AS C3NB, m3.CONM AS C3NM,\n"
						+ "r3.CONM AS C3NR, b4.CONM AS C4NB, m4.CONM AS C4NM,\n"
						+ "r4.CONM AS C4NR\n FROM SpeciGroups AS g\n" + "INNER JOIN Coder1 AS b1 ON b1.COID = g.SG1B\n"
						+ "INNER JOIN Coder2 AS b2 ON b2.COID = g.SG2B\n"
						+ "INNER JOIN Coder3 AS b3 ON b3.COID = g.SG3B\n"
						+ "INNER JOIN Coder4 AS b4 ON b4.COID = g.SG4B\n"
						+ "INNER JOIN Coder1 AS m1 ON m1.COID = g.SG1M\n"
						+ "INNER JOIN Coder2 AS m2 ON m2.COID = g.SG2M\n"
						+ "INNER JOIN Coder3 AS m3 ON m3.COID = g.SG3M\n"
						+ "INNER JOIN Coder4 AS m4 ON m4.COID = g.SG4M\n"
						+ "INNER JOIN Coder1 AS r1 ON r1.COID = g.SG1R\n"
						+ "INNER JOIN Coder2 AS r2 ON r2.COID = g.SG2R\n"
						+ "INNER JOIN Coder3 AS r3 ON r3.COID = g.SG3R\n"
						+ "INNER JOIN Coder4 AS r4 ON r4.COID = g.SG4R\n"
						+ "INNER JOIN Procedures AS r ON r.POID = g.POID\n"
						+ "INNER JOIN Subspecial AS b ON b.SBID = g.SBID\n"
						+ "INNER JOIN Specialties AS y ON y.SYID = b.SYID";
				break;
			case 14:
				sql = "CREATE VIEW " + dbSchema + ".udvSpeciMaster AS\n"
						+ "SELECT m.SMID, m.SGID, m.SMNM, m.SMDC, m.TAID,\n"
						+ "t.GRSS, t.EMBD, t.MICR, t.ROUT, t.FINL, t.TANM,\n"
						+ "g.POID, g.SBID, g.SG1B, g.SG1M, g.SG1R, g.SG2B,\n"
						+ "g.SG2M, g.SG2R, g.SG3B, g.SG3M, g.SG3R, g.SG4B,\n"
						+ "g.SG4M, g.SG4R, g.SGV5, g.SGLN, g.SGDC, b.SYID,\n"
						+ "b.SBNM, b.SBDC, y.SYNM, r.PONM, b1.CONM AS C1NB,\n"
						+ "m1.CONM AS C1NM, r1.CONM AS C1NR, b2.CONM AS C2NB,\n"
						+ "m2.CONM AS C2NM, r2.CONM AS C2NR, b3.CONM AS C3NB,\n"
						+ "m3.CONM AS C3NM, r3.CONM AS C3NR, b4.CONM AS C4NB,\n"
						+ "m4.CONM AS C4NM, r4.CONM AS C4NR FROM SpeciMaster AS m\n"
						+ "INNER JOIN Turnaround AS t ON t.TAID = m.TAID\n"
						+ "INNER JOIN SpeciGroups AS g ON g.SGID = m.SGID\n"
						+ "INNER JOIN Coder1 AS b1 ON b1.COID = g.SG1B\n"
						+ "INNER JOIN Coder2 AS b2 ON b2.COID = g.SG2B\n"
						+ "INNER JOIN Coder3 AS b3 ON b3.COID = g.SG3B\n"
						+ "INNER JOIN Coder4 AS b4 ON b4.COID = g.SG4B\n"
						+ "INNER JOIN Coder1 AS m1 ON m1.COID = g.SG1M\n"
						+ "INNER JOIN Coder2 AS m2 ON m2.COID = g.SG2M\n"
						+ "INNER JOIN Coder3 AS m3 ON m3.COID = g.SG3M\n"
						+ "INNER JOIN Coder4 AS m4 ON m4.COID = g.SG4M\n"
						+ "INNER JOIN Coder1 AS r1 ON r1.COID = g.SG1R\n"
						+ "INNER JOIN Coder2 AS r2 ON r2.COID = g.SG2R\n"
						+ "INNER JOIN Coder3 AS r3 ON r3.COID = g.SG3R\n"
						+ "INNER JOIN Coder4 AS r4 ON r4.COID = g.SG4R\n"
						+ "INNER JOIN Procedures AS r ON r.POID = g.POID\n"
						+ "INNER JOIN Subspecial AS b ON b.SBID = g.SBID\n"
						+ "INNER JOIN Specialties AS y ON y.SYID = b.SYID";
				break;
			case 15:
				sql = "CREATE VIEW " + dbSchema + ".udvSpecimens AS\n"
						+ "SELECT s.SPID, s.CAID, s.SMID, s.SPBL, s.SPSL, s.SPFR, s.SPHE,\n"
						+ "s.SPSS, s.SPIH, s.SPMO, s.SPV5, s.SPV1, s.SPV2, s.SPV3, s.SPV4,\n"
						+ "s.SPDC, c.CANO, m.SGID, m.SMNM, m.SMDC, g.SGDC, g.POID, r.PONM\n" + "FROM Specimens AS s\n"
						+ "INNER JOIN Cases AS c ON c.CAID = s.CAID\n"
						+ "INNER JOIN SpeciMaster AS m ON m.SMID = s.SMID\n"
						+ "INNER JOIN SpeciGroups AS g ON g.SGID = m.SGID\n"
						+ "INNER JOIN Procedures AS r ON r.POID = g.POID";
				break;
			case 16:
				sql = "CREATE VIEW " + dbSchema + ".udvSubspecial AS\n"
						+ "SELECT b.SBID, b.SYID, b.SBNM, b.SBDC, y.SYFL, y.SYLD, y.SYSP, y.SYNM\n"
						+ "FROM Subspecial AS b\n" + "INNER JOIN Specialties AS y ON y.SYID = b.SYID";
				break;
			case 17:
				if (dbID == DB_MARIA) {
					sql = "CREATE VIEW " + dbSchema + ".udvCasesTA AS\n"
							+ "SELECT FAID, SYID, SBID, POID, FANM, SYNM, SBNM, PONM,\n"
							+ "date_part('year', FNED) AS FNYEAR, date_part('month', FNED) AS FNMONTH,\n"
							+ "COUNT(*) as CASES, SUM(CAST(GRTA AS INT)) AS GRTA,\n"
							+ "SUM(CAST(EMTA AS INT)) AS EMTA, SUM(CAST(MITA AS INT)) AS MITA,\n"
							+ "SUM(CAST(ROTA AS INT)) AS ROTA, SUM(CAST(FNTA AS INT)) AS FNTA\n" + "FROM udvCases\n"
							+ "GROUP BY FAID, SYID, SBID, POID, FANM, SYNM, SBNM, PONM,\n" + "FNYEAR, FNMONTH";
				} else if (dbID == DB_MSSQL || dbID == DB_POSTG) {
					sql = "CREATE VIEW " + dbSchema + ".udvCasesTA AS\n"
							+ "SELECT FAID, SYID, SBID, POID, FANM, SYNM, SBNM, PONM,\n"
							+ "date_part('year', FNED) AS FNYEAR, date_part('month', FNED) AS FNMONTH,\n"
							+ "COUNT(*) as CASES, SUM(CAST(GRTA AS INT)) AS GRTA,\n"
							+ "SUM(CAST(EMTA AS INT)) AS EMTA, SUM(CAST(MITA AS INT)) AS MITA,\n"
							+ "SUM(CAST(ROTA AS INT)) AS ROTA, SUM(CAST(FNTA AS INT)) AS FNTA\n" + "FROM udvCases\n"
							+ "GROUP BY FAID, SYID, SBID, POID, FANM, SYNM, SBNM, PONM,\n" + "FNYEAR, FNMONTH";
				} else {
					sql = "CREATE VIEW " + dbSchema + ".udvCasesTA AS\n"
							+ "SELECT FAID, SYID, SBID, POID, FANM, SYNM, SBNM, PONM,\n"
							+ "YEAR(FNED) AS FNYEAR, MONTH(FNED) AS FNMONTH,\n"
							+ "COUNT(*) as CASES, SUM(CAST(GRTA AS INT)) AS GRTA,\n"
							+ "SUM(CAST(EMTA AS INT)) AS EMTA, SUM(CAST(MITA AS INT)) AS MITA,\n"
							+ "SUM(CAST(ROTA AS INT)) AS ROTA, SUM(CAST(FNTA AS INT)) AS FNTA\n" + "FROM udvCases\n"
							+ "GROUP BY FAID, SYID, SBID, POID, FANM, SYNM, SBNM, PONM,\n" + "YEAR(FNED), MONTH(FNED)";
				}
				break;
			default:
				sql = "CREATE VIEW " + dbSchema + ".udvWorkdayLast AS\n" + "SELECT WDID, WDNO, WDTP, WDDT\n"
						+ "FROM Workdays\n" + "WHERE (WDDT = (SELECT MAX(WDDT) FROM Workdays))";
			}
			if (dbID == DB_DERBY) {
				sql = sql.replace("DATETIME", "TIMESTAMP");
			}
			execute(sql);
			if (errorID != LConstants.ERROR_NONE) {
				break;
			}
			noRows++;
			log(LConstants.ERROR_NONE, "Created " + noRows + " views.");
		}
	}

	private void execute(String sql) {
		try {
			stm.executeUpdate(sql);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private String generatePassword() {
		buffer.setLength(0);
		// Password length should be 23 characters
		for (int length = 0; length < 23; length++) {
			char c = (char) list.get(randomNo.nextInt(list.size()));
			buffer.append(c);
		}
		return buffer.toString();
	}

	private void getDbParameters() {
		try {
			System.out.printf("Database Host (%s): ", dbHost);
			String input = br.readLine();
			if (!(input == null || input.trim().length() == 0)) {
				dbHost = input.trim();
			}
			System.out.printf("Database Port (%s): ", dbPort);
			input = br.readLine();
			if (!(input == null || input.trim().length() == 0)) {
				dbPort = input.trim();
			}
			System.out.printf("Login Name (%s): ", dbUser);
			input = br.readLine();
			if (!(input == null || input.trim().length() == 0)) {
				dbUser = input.trim();
			}
			System.out.print("Password: ");
			input = br.readLine();
			if (!(input == null || input.trim().length() == 0)) {
				dbPass = input.trim();
			}
			if (dbPass.trim().length() == 0) {
				log(LConstants.ERROR_NONE, "Exiting");
				System.exit(0);
			}
		} catch (IOException e) {
			log(LConstants.ERROR_VARIABLE, "Unknown response");
			System.exit(1);
		}
	}

	private void initialize(String[] args) {
		for (String s : args) {
			s = s.trim();
			if (s.length() > 6 && s.substring(0, 6).toLowerCase().equals("--path")) {
				appDir = s.substring(6).trim();
			}
		}
		if (appDir.length() == 0) {
			appDir = System.getProperty("user.home") + System.getProperty("file.separator") + "PowerJ"
					+ System.getProperty("file.separator");
		} else if (!appDir.endsWith(System.getProperty("file.separator"))) {
			appDir += System.getProperty("file.separator");
		}
		// Add ASCII Decimal values to ArrayList
		list.add((char) 32);
		list.add((char) 33);
		list.add((char) 35);
		list.add((char) 36);
		list.add((char) 40);
		list.add((char) 44);
		list.add((char) 45);
		list.add((char) 46);
		list.add((char) 61);
		list.add((char) 63);
		// Do not use 85 (not compatible)
		for (int i = 48; i < 85; i++) {
			list.add((char) i);
		}
		for (int i = 86; i < 91; i++) {
			list.add((char) i);
		}
		for (int i = 97; i < 123; i++) {
			list.add((char) i);
		}
		// rotate the elements in the list by the specified distance. This will create
		// strong password
		Collections.rotate(list, 5);
		try {
			File theDir = new File(appDir);
			if (!theDir.exists()) {
				theDir.mkdir();
			}
			File subDir = new File(appDir + "bin" + System.getProperty("file.separator"));
			if (!subDir.exists()) {
				subDir.mkdir();
			}
			subDir = new File(appDir + "data" + System.getProperty("file.separator"));
			if (!subDir.exists()) {
				subDir.mkdir();
			}
			subDir = new File(appDir + "logs" + System.getProperty("file.separator"));
			if (!subDir.exists()) {
				subDir.mkdir();
			}
		} catch (SecurityException e) {
			log(LConstants.ERROR_VARIABLE, "PJSetup", e);
			System.exit(1);
		}
		try {
			System.out.print("Select installation type (1 = Desktop, 2 = Client, 3 = Server): ");
			String input = br.readLine();
			if (input == null || input.trim().length() == 0) {
				log(LConstants.ERROR_NONE, "Exiting");
				System.exit(0);
			}
			sysID = Integer.parseInt(input);
			if (sysID < 1 || sysID > 3) {
				log(LConstants.ERROR_NONE, "Exiting");
				System.exit(0);
			}
			if (sysID == 2 || sysID == 3) {
				System.out.print("Select database architecture (1 = MariaDB, 2 = MSSql, 3 = PostgreSQL): ");
				input = br.readLine();
				if (input == null || input.trim().length() == 0) {
					log(LConstants.ERROR_NONE, "Exiting");
					System.exit(0);
				}
				dbID = 1 + Integer.parseInt(input);
			} else {
				dbID = 1;
			}
			if (dbID < 1 || dbID > 4) {
				log(LConstants.ERROR_NONE, "Exiting");
				System.exit(0);
			}
			LCrypto crypto = new LCrypto(appDir);
			System.out.print("Install the database (y,n)? ");
			input = br.readLine();
			if (input == null || input.trim().length() == 0) {
				log(LConstants.ERROR_NONE, "Exiting");
				System.exit(0);
			} else if (input.trim().toLowerCase().startsWith("y")) {
				sysPassClient = generatePassword();
				dbUser = System.getProperty("user.name");
				switch (dbID) {
				case DB_MARIA:
					dbArch = "MARIADB";
					dbHost = "localhost";
					dbPort = "3306";
					getDbParameters();
					if (dbUser.trim().length() == 0 || dbPass.trim().length() == 0) {
						log(LConstants.ERROR_NONE, "Exiting");
						System.exit(0);
					}
					setDB(dbID);
					if (errorID == 0) {
						String[] data = { dbArch, dbHost, dbPort, dbSchema, sysUserClient, sysPassClient };
						if (crypto.setData(data)) {
							log(LConstants.ERROR_NONE, "Setup completed successfully.");
						} else {
							log(LConstants.ERROR_IO, "Setup did not complete!");
						}
					}
					break;
				case DB_MSSQL:
					dbArch = "MSSQL";
					dbHost = "localhost";
					dbPort = "1433";
					getDbParameters();
					if (dbUser.trim().length() == 0 || dbPass.trim().length() == 0) {
						log(LConstants.ERROR_NONE, "Exiting");
						System.exit(0);
					}
					setDB(dbID);
					if (errorID == 0) {
						String[] data = { dbArch, dbHost, dbPort, dbSchema, sysUserClient, sysPassClient };
						if (crypto.setData(data)) {
							log(LConstants.ERROR_NONE, "Setup completed successfully.");
						} else {
							log(LConstants.ERROR_IO, "Setup did not complete!");
						}
					}
					break;
				case DB_POSTG:
					dbArch = "POSTGRES";
					dbHost = "localhost";
					dbPort = "5432";
					getDbParameters();
					if (dbUser.trim().length() == 0 || dbPass.trim().length() == 0) {
						log(LConstants.ERROR_NONE, "Exiting");
						System.exit(0);
					}
					setDB(dbID);
					if (errorID == 0) {
						String[] data = { dbArch, dbHost, dbPort, dbSchema, sysUserClient, sysPassClient };
						if (crypto.setData(data)) {
							log(LConstants.ERROR_NONE, "Setup completed successfully.");
						} else {
							log(LConstants.ERROR_IO, "Setup did not complete!");
						}
					}
					break;
				default:
					dbArch = "DERBY";
					dbHost = "localhost";
					dbPort = "2313";
					setDB(dbID);
					if (errorID == 0) {
						String[] data = { dbArch, dbHost, dbPort, dbSchema, sysUserClient, sysPassClient };
						if (crypto.setData(data)) {
							log(LConstants.ERROR_NONE, "Setup completed successfully.");
						} else {
							log(LConstants.ERROR_IO, "Setup did not complete!");
						}
					}
				}
			} else {
				String[] data = crypto.getData();
				if (data != null) {
					// Else, Derby
					if (data.length == 6) {
						dbArch = data[0].toUpperCase();
						dbHost = data[1];
						dbPort = data[2];
						dbUser = data[4];
						dbPass = data[5];
					} else {
						log(LConstants.ERROR_BINARY_FILE, "Variables", "Invalid application binary file");
					}
				}
			}
		} catch (NumberFormatException e) {
			log(LConstants.ERROR_VARIABLE, "Unknown response");
		} catch (IOException e) {
			log(LConstants.ERROR_VARIABLE, "Unknown response");
		} finally {
			close();
			System.exit(errorID);
		}
	}

	private void loadCoders() {
		final String[] tables = { "Coder1", "Coder2", "Coder3", "Coder4" };
		try {
			for (int i = 0; i < 4; i++) {
				InputStream is = ClassLoader.getSystemClassLoader()
						.getResourceAsStream("db/" + tables[i].toLowerCase() + ".txt");
				if (is != null) {
					InputStreamReader ir = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(ir);
					PreparedStatement pstm = connection.prepareStatement("INSERT INTO " + tables[i]
							+ " (COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
					String line;
					String[] columns = null;
					noRows = 0;
					while ((line = br.readLine()) != null) {
						line = line.trim();
						if (line.length() > 1) {
							if (!line.startsWith("-")) {
								columns = line.split("\t");
								pstm.setShort(1, Short.valueOf(columns[0]));
								pstm.setShort(2, Short.valueOf(columns[1]));
								pstm.setShort(3, Short.valueOf(columns[2]));
								pstm.setDouble(4, Double.valueOf(columns[3]));
								pstm.setDouble(5, Double.valueOf(columns[4]));
								pstm.setDouble(6, Double.valueOf(columns[5]));
								pstm.setString(7, columns[6]);
								pstm.setString(8, columns[7]);
								pstm.executeUpdate();
								noRows++;
							}
						}
					}
					log(LConstants.ERROR_NONE, String.format("Loaded %d rows to %s Table.", noRows, tables[i]));
					pstm.close();
					br.close();
					ir.close();
					is.close();
				}
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadOrderGroups() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/ordergroups.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO OrderGroups (OGID, OTID, OGC1, OGC2, OGC3, "
								+ "OGC4, OGC5, OGNM, OGDC) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setShort(1, Short.valueOf(columns[0]));
							pstm.setShort(2, Short.valueOf(columns[1]));
							pstm.setShort(3, Short.valueOf(columns[2]));
							pstm.setShort(4, Short.valueOf(columns[3]));
							pstm.setShort(5, Short.valueOf(columns[4]));
							pstm.setShort(6, Short.valueOf(columns[5]));
							pstm.setInt(7, Integer.valueOf(columns[6]));
							pstm.setString(8, columns[7]);
							pstm.setString(9, columns[8]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Order Groups Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadOrderTypes() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/ordertypes.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO OrderTypes (OTID, OTNM) VALUES (?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setShort(1, Short.valueOf(columns[0]));
							pstm.setString(2, columns[1]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Order Types Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadProcedures() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/procedures.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO Procedures (POID, PONM, PODC) VALUES (?, ?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setShort(1, Short.valueOf(columns[0]));
							pstm.setString(2, columns[1]);
							pstm.setString(3, columns[2]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Procedures Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadRules() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/rules.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO Rules (RUID, RUNM, RUDC) VALUES (?, ?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setShort(1, Short.valueOf(columns[0]));
							pstm.setString(2, columns[1]);
							pstm.setString(3, columns[2]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Rules Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadSetup() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/setup.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection.prepareStatement("INSERT INTO Setup (STID, STVA) VALUES (?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setShort(1, Short.valueOf(columns[0]));
							pstm.setString(2, columns[1]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Setup Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadSpecialties() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/specialties.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection.prepareStatement(
						"INSERT INTO Specialties (SYID, SYFL, SYLD, SYSP, SYNM) VALUES (?, ?, ?, ?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setShort(1, Short.valueOf(columns[0]));
							pstm.setString(2, columns[1]);
							pstm.setString(3, columns[2]);
							pstm.setString(4, columns[3]);
							pstm.setString(5, columns[4]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Specialties Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadSpecimenGroups() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/specimengroups.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO SpeciGroups (SGID, SBID, POID, SG1B, SG1M, "
								+ "SG1R, SG2B, SG2M, SG2R, SG3B, SG3M, SG3R, SG4B, SG4M, SG4R, SGV5, SGLN, SGDC) VALUES "
								+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setShort(1, Short.valueOf(columns[0]));
							pstm.setShort(2, Short.valueOf(columns[1]));
							pstm.setShort(3, Short.valueOf(columns[2]));
							pstm.setShort(4, Short.valueOf(columns[3]));
							pstm.setShort(5, Short.valueOf(columns[4]));
							pstm.setShort(6, Short.valueOf(columns[5]));
							pstm.setShort(7, Short.valueOf(columns[6]));
							pstm.setShort(8, Short.valueOf(columns[7]));
							pstm.setShort(9, Short.valueOf(columns[8]));
							pstm.setShort(10, Short.valueOf(columns[9]));
							pstm.setShort(11, Short.valueOf(columns[10]));
							pstm.setShort(12, Short.valueOf(columns[11]));
							pstm.setShort(13, Short.valueOf(columns[12]));
							pstm.setShort(14, Short.valueOf(columns[13]));
							pstm.setShort(15, Short.valueOf(columns[14]));
							pstm.setInt(16, Integer.valueOf(columns[15]));
							pstm.setString(17, columns[16]);
							pstm.setString(18, columns[17]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Specimen Groups Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadSubspecial() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/subspecialties.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection.prepareStatement(
						"INSERT INTO Specialties (SYID, SYFL, SYLD, SYSP, SYNM) VALUES (?, ?, ?, ?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setShort(1, Short.valueOf(columns[0]));
							pstm.setString(2, columns[1]);
							pstm.setString(3, columns[2]);
							pstm.setString(4, columns[3]);
							pstm.setString(5, columns[4]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Subspecialties Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadTables() {
		for (int i = 0; i < 11; i++) {
			switch (i) {
			case 0:
				loadSetup();
				break;
			case 1:
				loadWorkdays();
				break;
			case 2:
				loadProcedures();
				break;
			case 3:
				loadRules();
				break;
			case 4:
				loadSpecialties();
				break;
			case 5:
				loadSubspecial();
				break;
			case 6:
				loadCoders();
				break;
			case 7:
				loadTurnaround();
				break;
			case 8:
				loadOrderTypes();
				break;
			case 9:
				loadOrderGroups();
				break;
			default:
				loadSpecimenGroups();
			}
		}
	}

	private void loadTurnaround() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/turnaround.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection.prepareStatement(
						"INSERT INTO Turnaround (TAID, GRSS, EMBD, MICR, ROUT, FINL, TANM) VALUES (?, ?, ?, ?, ?, ?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setShort(1, Short.valueOf(columns[0]));
							pstm.setShort(2, Short.valueOf(columns[1]));
							pstm.setShort(3, Short.valueOf(columns[2]));
							pstm.setShort(4, Short.valueOf(columns[3]));
							pstm.setShort(5, Short.valueOf(columns[4]));
							pstm.setShort(6, Short.valueOf(columns[5]));
							pstm.setString(7, columns[6]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Turnaround Time Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadWorkdays() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/workdays.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO Workdays (WDID, WDNO, WDTP, WDDT) VALUES (?, ?, ?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setInt(1, Integer.valueOf(columns[0]));
							pstm.setInt(2, Integer.valueOf(columns[1]));
							pstm.setString(3, columns[2]);
							pstm.setTimestamp(4, new Timestamp(Long.valueOf(columns[3])));
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LConstants.ERROR_NONE, String.format("Loaded %d rows to Workdays Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void log(byte severity, String message) {
		log(severity, "PJSetup", message);
	}

	private void log(byte severity, String name, String message) {
		if (severity > LConstants.ERROR_NONE) {
			errorID = severity;
			System.err.println(name + " Error No " + errorID);
			System.err.println(name + ": " + message);
		} else {
			System.out.println(name + ": " + message);
		}
	}

	private void log(byte severity, String name, Throwable e) {
		log(severity, name, e.getLocalizedMessage());
		if (e.getCause() != null) {
			log(severity, name, e.getCause().getLocalizedMessage());
		}
	}

	private void setDB(int dbID) {
		createDB(dbID);
		if (errorID == LConstants.ERROR_NONE) {
			createTables(dbID);
		}
		if (errorID == LConstants.ERROR_NONE) {
			createViews(dbID);
		}
		if (errorID == LConstants.ERROR_NONE) {
			switch (dbID) {
			case DB_MSSQL:
				createProcMSSQL();
				if (errorID == LConstants.ERROR_NONE) {
					createUsers(dbID);
				}
				break;
			case DB_MARIA:
				createProcMaria();
				if (errorID == LConstants.ERROR_NONE) {
					createUsers(dbID);
				}
				break;
			case DB_POSTG:
				// No procedures in postgreSql 10
				createUsers(dbID);
				break;
			default:
				// No procedures or users in Derby
			}
		}
		if (errorID == LConstants.ERROR_NONE) {
			loadTables();
		}
	}
}