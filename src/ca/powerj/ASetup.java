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

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

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
	private String input = "";
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
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

	private void createDB() {
		String url = "";
		try {
			switch (dbID) {
			case DB_DERBY:
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
				// Define physical location of Derby Database
				Properties p = System.getProperties();
				p.setProperty("derby.system.home", appDir);
				url = "jdbc:derby:" + appDir + "powerj;create=true;";
				break;
			case DB_MARIA:
				url = "jdbc:mariadb://" + dbHost + ":" + dbPort + "?user=" + dbUser + "&password=" + dbPass;
				break;
			case DB_POSTG:
				Class.forName("org.postgresql.Driver");
				url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/postgres?user=" + dbUser + "&password=" + dbPass;
				break;
			default:
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
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
				execute("CREATE DATABASE powerj");
				connection.close();
				if (errorID == LConstants.ERROR_NONE) {
					url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/powerj?user=" + dbUser + "&password=" + dbPass;
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
				execute("CREATE DATABASE powerj");
				if (errorID == LConstants.ERROR_NONE) {
					execute("USE powerj");
				}
				if (errorID == LConstants.ERROR_NONE) {
					execute("CREATE SCHEMA " + dbSchema);
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
		} catch (ClassNotFoundException e) {
			log(LConstants.ERROR_UNEXPECTED, dbName, e);
		}
	}

	private void createLogin() {
		SecureRandom randomNo = new SecureRandom();
		ArrayList<Object> list = new ArrayList<Object>();
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
		// rotate the elements by a distance to create a strong password
		Collections.rotate(list, 5);
		StringBuilder buffer = new StringBuilder();
		buffer.setLength(0);
		// Password length should be 23 characters
		for (int length = 0; length < 23; length++) {
			String s = (String) list.get(randomNo.nextInt(list.size()));
			buffer.append(s);
		}
		sysPassClient = buffer.toString();
		if (dbID == DB_POSTG) {
			if (errorID == LConstants.ERROR_NONE) {
				System.out.println("CREATE ROLE " + sysUserClient + " LOGIN PASSWORD '" + sysPassClient
						+ "' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;");
			}
			if (errorID == LConstants.ERROR_NONE) {
				System.out.println("GRANT USAGE ON SCHEMA " + dbSchema + " TO " + sysUserClient + ";");
				System.out.println("GRANT SELECT, INSERT, DELETE, UPDATE, EXECUTE ON ALL TABLES IN SCHEMA " + dbSchema
						+ " TO " + sysUserClient + ";");
			}
		} else if (dbID == DB_MARIA) {
			if (errorID == LConstants.ERROR_NONE) {
				System.out.println("CREATE USER " + sysUserClient + "@'%' IDENTIFIED BY '" + sysPassClient + "';");
			}
			if (errorID == LConstants.ERROR_NONE) {
				System.out.println("GRANT SELECT, INSERT, DELETE, UPDATE, EXECUTE ON " + dbSchema + ".* TO "
						+ sysUserClient + "@'%';");
			}
			System.out.println("FLUSH PRIVILEGES;");
		} else {
			if (errorID == LConstants.ERROR_NONE) {
				System.out.println("CREATE USER " + sysUserClient + " WITH PASSWORD = '" + sysPassClient
						+ "', DEFAULT_SCHEMA = " + dbSchema);
			}
			if (errorID == LConstants.ERROR_NONE) {
				System.out.println("exec sp_addrolemember db_datareader, " + sysUserClient);
			}
			if (errorID == LConstants.ERROR_NONE) {
				System.out.println("exec sp_addrolemember db_datawriter, " + sysUserClient);
			}
			if (errorID == LConstants.ERROR_NONE) {
				System.out.println("GRANT EXECUTE TO " + sysUserClient);
			}
		}
		switch (dbID) {
		case DB_MARIA:
			dbArch = "MARIADB";
			dbHost = "localhost";
			dbPort = "3306";
			break;
		case DB_MSSQL:
			dbArch = "MSSQL";
			dbHost = "localhost";
			dbPort = "1433";
			break;
		case DB_POSTG:
			dbArch = "POSTGRES";
			dbHost = "localhost";
			dbPort = "5432";
			break;
		default:
			dbArch = "DERBY";
			dbHost = "localhost";
			dbPort = "2313";
		}
		getDbParameters();
		if (dbHost.trim().length() == 0 || dbArch.trim().length() == 0) {
			log(LConstants.ERROR_NONE, "Exiting");
			System.exit(0);
		}
		LCrypto crypto = new LCrypto(appDir);
		String[] data = { dbArch, dbHost, dbPort, dbSchema, sysUserClient, sysPassClient };
		if (crypto.setData(data)) {
			log(LConstants.ERROR_NONE, "Users binary file completed successfully.");
		} else {
			log(LConstants.ERROR_IO, "Users binary file failed!");
		}
	}

	private void createProcMaria() {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 52; i++) {
			switch (i) {
			case 0:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaccessions()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvaccessions\n"
						+ "ORDER BY acnm;\n"
						+ "END";
				break;
			case 1:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpadditionals(param1 INT)\n"
						+ "BEGIN\n"
						+ "SELECT prid, adcd, adv5, adv1, adv2, adv3, adv4, addt, prnm, prls, prfr, cano\n"
						+ "FROM " + dbSchema + ".udvadditionals\n"
						+ "WHERE caid = param1\n"
						+ "ORDER BY addt;\n"
						+ "END";
				break;
			case 2:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaddlast(param1 SMALLINT)\n"
						+ "BEGIN\n"
						+ "SELECT MAX(addt) AS addt\n"
						+ "FROM " + dbSchema + ".additionals\n"
						+ "WHERE adcd = param1;\n"
						+ "END";
				break;
			case 3:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaddspg(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n"
						+ "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc,\n"
						+ "f.fanm, COUNT(a.caid) AS qty, SUM(a.adv1) AS adv1, SUM(a.adv2) AS adv2,\n" 
						+ "SUM(a.adv3) AS adv3, SUM(a.adv4) AS adv4, SUM(a.adv5) AS adv5\n"
						+ "FROM " + dbSchema + ".additionals AS a\n"
						+ "INNER JOIN " + dbSchema + ".cases AS c ON c.caid = a.caid\n"
						+ "INNER JOIN " + dbSchema + ".facilities AS f ON f.faid = c.faid\n"
						+ "INNER JOIN " + dbSchema + ".specimaster AS m ON m.smid = c.smid\n"
						+ "INNER JOIN " + dbSchema + ".specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".procedures r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid\n"
						+ "WHERE c.fned BETWEEN param1 AND param2\n"
						+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\n" 
						+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid\n"
						+ "END";
				break;
			case 4:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaddsum(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n"
						+ "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(caid) AS adca,\n"
						+ "SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2, SUM(adv3) AS adv3, SUM(adv4) AS adv4\n"
						+ "FROM " + dbSchema + ".udvadditionals\n"
						+ "WHERE addt BETWEEN param1 AND param2\n"
						+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr\n"
						+ "ORDER BY faid, syid, sbid, poid, prid;\n"
						+ "END";
				break;
			case 5:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaddyear(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n"
						+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, date_part('year', addt) as yearid,\n"
						+ "COUNT(caid) AS adca, SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2,\n"
						+ "SUM(adv3) AS adv3, SUM(adv4) AS adv4\n"
						+ "FROM " + dbSchema + ".udvadditionals\n"
						+ "WHERE addt BETWEEN param1 AND param2\n"
						+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, date_part('year', addt)\n"
						+ "ORDER BY faid, syid, sbid, poid, sgid, yearid;\n"
						+ "END";
				break;
			case 6:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcoder1()\n"
						+ "BEGIN\n"
						+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc\n"
						+ "FROM " + dbSchema + ".coder1\n"
						+ "ORDER BY conm;\n"
						+ "END";
				break;
			case 7:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcoder2()\n"
						+ "BEGIN\n"
						+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + dbSchema + ".coder2 ORDER BY conm;\n"
						+ "END";
				break;
			case 8:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcoder3()\n"
						+ "BEGIN\n"
						+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + dbSchema + ".coder3 ORDER BY conm;\n"
						+ "END";
				break;
			case 9:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcoder4()\n"
						+ "BEGIN\n"
						+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + dbSchema + ".coder4 ORDER BY conm;\n"
						+ "END";
				break;
			case 10:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcmt(param1 INT)\n"
						+ "BEGIN\n"
						+ "SELECT com1, com2, com3, com4 FROM " + dbSchema + ".comments WHERE caid = param1;\n"
						+ "END";
				break;
			case 11:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcseid(param1 CHAR(12))\n"
						+ "BEGIN\n"
						+ "SELECT caid FROM " + dbSchema + ".cases WHERE cano = param1;\n"
						+ "END";
				break;
			case 12:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcseno(param1 INT)\n"
						+ "BEGIN\n"
						+ "SELECT cano FROM " + dbSchema + ".cases WHERE caid = param1;\n"
						+ "END";
				break;
			case 13:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcsespe(param1 INT)\n"
						+ "BEGIN\n"
						+ "SELECT c.smid, c.fned, c.cano, s.spid\n"
						+ "FROM " + dbSchema + ".cases AS c\n"
						+ "INNER JOIN " + dbSchema + ".specimens AS s ON s.caid = c.caid AND s.smid = c.smid\n"
						+ "WHERE c.caid = param1;\n"
						+ "END";
				break;
			case 14:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcsesum(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n"
						+ "SELECT faid, syid, sbid, poid, fnid, fanm, synm, sbnm, sbdc, ponm, fnnm, fnls, fnfr, COUNT(caid) AS caca,\n"
						+ "SUM(CAST(casp as INT)) AS casp, SUM(CAST(cabl as INT)) AS cabl, SUM(CAST(casl as INT)) AS casl, SUM(CAST(cahe as INT)) AS cahe,\n"
						+ "SUM(CAST(cass as INT)) AS cass, SUM(CAST(caih as INT)) AS caih, SUM(CAST(camo as INT)) AS camo, SUM(CAST(cafs as INT)) AS cafs,\n"
						+ "SUM(CAST(casy as INT)) AS casy, SUM(CAST(grta as INT)) AS grta, SUM(CAST(emta as INT)) AS emta, SUM(CAST(mita as INT)) AS mita,\n"
						+ "SUM(CAST(rota as INT)) AS rota, SUM(CAST(fnta as INT)) AS fnta, SUM(CAST(cav5 as INT)) AS cav5, SUM(cav1) AS cav1, SUM(cav2) AS cav2,\n"
						+ "SUM(cav3) AS cav3, SUM(cav4) AS cav4\n"
						+ "FROM " + dbSchema + ".udvcases\n"
						+ "WHERE (fned BETWEEN param1 AND param2)\n"
						+ "GROUP BY faid, syid, sbid, poid, fnid, fanm, synm, sbnm, sbdc, ponm, fnnm, fnls, fnfr\n"
						+ "ORDER BY faid, syid, sbid, poid, fnid;\n"
						+ "END";
				break;
			case 15:
				sql = "CREATE PROCEDURE " + dbSchema + ".udperrselect()\n"
						+ "BEGIN\n"
						+ "SELECT caid, erid, cano FROM " + dbSchema + ".errors WHERE erid > 0 ORDER BY cano;\n"
						+ "END";
				break;
			case 16:
				sql = "CREATE PROCEDURE " + dbSchema + ".udperrcmt(param1 INT)\n"
						+ "BEGIN\n"
						+ "SELECT erdc FROM " + dbSchema + ".errors WHERE caid = param1;\n"
						+ "END";
				break;
			case 17:
				sql = "CREATE PROCEDURE " + dbSchema + ".udperrredo()\n"
						+ "BEGIN\n"
						+ "SELECT caid FROM " + dbSchema + ".errors WHERE erid = 0 ORDER BY caid;\n"
						+ "END";
				break;
			case 18:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfacility()\n" + "BEGIN\n"
						+ "SELECT faid, fafl, fald, fanm, fadc FROM " + dbSchema + ".facilities ORDER BY fanm;\n" + "END";
				break;
			case 19:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfrzsid(param1 INT)\n"
						+ "BEGIN\n"
						+ "SELECT prid, frbl, frsl, frv5, frv1, frv2, frv3, frv4, prnm, prls,\n"
						+ "spdc, smnm FROM " + dbSchema + ".udvfrozens WHERE spid = param1;"
						+ "END";
				break;
			case 20:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfrzspg(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n"
						+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, COUNT(spid) AS frsp,\n"
						+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
						+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
						+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
						+ "FROM " + dbSchema + ".udvfrozens\n"
						+ "WHERE (aced BETWEEN param1 AND param2)\n"
						+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc\n"
						+ "ORDER BY faid, syid, sbid, poid, sgid;\n"
						+ "END";
				break;
			case 21:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpFrzSu5(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n"
						+ "SELECT COUNT(*) AS QTY, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
						+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
						+ "FROM " + dbSchema + ".udvfrozens WHERE aced BETWEEN param1 AND param2;\n"
						+ "END";
			case 22:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfrzsum(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n"
						+ "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(spid) AS frsp,\n"
						+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
						+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
						+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
						+ "FROM " + dbSchema + ".udvfrozens\n"
						+ "WHERE (aced BETWEEN param1 AND param2)\n"
						+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr\n"
						+ "ORDER BY faid, syid, sbid, poid, prid;\n"
						+ "END";
				break;
			case 23:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfrzyear(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n"
						+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc,\n"
						+ "date_part('year', aced) as yearid, COUNT(spid) AS frsp,\n"
						+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
						+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
						+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
						+ "FROM " + dbSchema + ".udvfrozens\n"
						+ "WHERE (aced BETWEEN param1 AND param2)\n"
						+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, date_part('year', aced)\n"
						+ "ORDER BY faid, syid, sbid, poid, sgid, yearid;\n"
						+ "END";
				break;
			case 24:
				sql = "CREATE PROCEDURE " + dbSchema + ".udporder(param1 INT)\n"
						+ "BEGIN\n"
						+ "SELECT orqy, orv1, orv2, orv3, orv4, ognm\n"
						+ "FROM " + dbSchema + ".udvorders\n"
						+ "WHERE spid = param1\n"
						+ "ORDER BY ognm;\n"
						+ "END";
				break;
			case 25:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpordergroup()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvordergroups ORDER BY ognm;\n"
						+ "END";
				break;
			case 26:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpordermaster()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvordermaster ORDER BY omnm;\n"
						+ "END";
				break;
			case 27:
				sql = "CREATE PROCEDURE " + dbSchema + ".udppending()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvpending ORDER BY pnid;\n"
						+ "END";
				break;
			case 28:
				sql = "CREATE PROCEDURE " + dbSchema + ".udppendingrouted(param1 DATETIME, param2 DATETIME)\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvpending\n"
						+ "WHERE (roed BETWEEN param1 AND param2)\n"
						+ "ORDER BY pnid;\n"
						+ "END";
				break;
			case 29:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpprocedure()\n"
						+ "BEGIN\n"
						+ "SELECT poid, ponm, podc FROM " + dbSchema + ".procedures ORDER BY ponm;\n"
						+ "END";
				break;
			case 30:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpprsname()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".persons ORDER BY prnm;\n"
						+ "END";
				break;
			case 31:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpprsid(param1 SMALLINT)\n"
						+ "BEGIN\n"
						+ "SELECT prvl FROM " + dbSchema + ".persons WHERE prid = param1;\n"
						+ "END";
				break;
			case 32:
				sql = "CREATE PROCEDURE " + dbSchema + ".udprule()\n"
						+ "BEGIN\n"
						+ "SELECT ruid, runm, rudc FROM " + dbSchema + ".rules ORDER BY ruid;\n"
						+ "END";
				break;
			case 33:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpschedserv(param1 DATE, param2 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT wdid, srid, prid, prnm, srnm\n"
						+ "FROM " + dbSchema + ".udvschedules\n"
						+ "WHERE wddt BETWEEN param1 AND param2 \n"
						+ "ORDER BY srnm, wdid;\n"
						+ "END";
				break;
			case 34:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpschedstaff(param1 DATE, param2 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT wdid, srid, prid, prnm, srnm\n"
						+ "FROM " + dbSchema + ".udvschedules\n"
						+ "WHERE wddt BETWEEN param1 AND param2 \n"
						+ "ORDER BY prnm, wdid, srnm;\n"
						+ "END";
				break;
			case 35:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpschedsum(param1 DATE, param2 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvschedules\n"
						+ "WHERE wddt BETWEEN param1 AND param2 \n"
						+ "ORDER BY faid, prid, wdid, srid\n"
						+ "END";
				break;
			case 36:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecimens(param1 INT)\n"
						+ "BEGIN\n"
						+ "SELECT spid, smid, spbl, spsl, spfr, sphe, spss, spih,\n"
						+ "spmo, spv5, spv1, spv2, spv3, spv4, spdc, smnm, smdc, ponm\n"
						+ "FROM " + dbSchema + ".udvspecimens\n"
						+ "WHERE caid = param1\n"
						+ "ORDER BY spid;\n"
						+ "END";
				break;
			case 37:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecgroup()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvspecigroups ORDER BY sgdc;\n"
						+ "END";
				break;
			case 38:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecyear(param1 DATE, param2 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm,\n"
						+ "date_part('year', c.fned) as yearid, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
						+ "SUM(s.spv1) AS spv1, SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4,\n"
						+ "SUM(s.spv5) AS spv5 FROM " + dbSchema + ".specigroups g\n"
						+ "INNER JOIN " + dbSchema + ".specimaster m ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".specimens s ON m.smid = s.smid\n"
						+ "INNER JOIN " + dbSchema + ".cases c ON c.caid = s.caid\n"
						+ "INNER JOIN " + dbSchema + ".procedures r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties y ON y.syid = b.syid\n"
						+ "INNER JOIN " + dbSchema + ".facilities f ON f.faid = c.faid WHERE c.fned BETWEEN param1 AND param2 \n"
						+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, date_part('year', c.fned)\n"
						+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid\n"
						+ "END";
			case 39:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecmaster()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvspecimaster ORDER BY smnm;\n"
						+ "END";
				break;
			case 40:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecsu5(param1 DATE, param2 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT g.sgid, COUNT(s.spid) AS qty, SUM(s.spv1) AS spv1,\n"
						+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4\n"
						+ "FROM specigroups g INNER JOIN " + dbSchema + ".specimaster m ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".specimens s ON m.smid = s.smid\n"
						+ "INNER JOIN " + dbSchema + ".cases c ON c.caid = s.caid\n"
						+ "WHERE c.fned BETWEEN param1 AND param2\n"
						+ "GROUP BY g.sgid"
						+ "END";
				break;
			case 41:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecsum(param1 DATE, param2 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT b.syid, g.sbid, g.sgid, c.faid, y.synm, b.sbnm, g.sgdc, f.fanm,\n"
						+ "COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
						+ "SUM(s.sphe) AS sphe, SUM(s.spss) AS spss, SUM(s.spih) AS spih, SUM(s.spv1) AS spv1,\n"
						+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, SUM(s.spv5) AS spv5\n"
						+ "FROM " + dbSchema + ".specigroups g\n"
						+ "INNER JOIN " + dbSchema + ".specimaster m ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".specimens s ON m.smid = s.smid\n"
						+ "INNER JOIN " + dbSchema + ".cases c ON c.caid = s.caid\n"
						+ "INNER JOIN " + dbSchema + ".procedures r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties y ON y.syid = b.syid\n"
						+ "INNER JOIN " + dbSchema + ".facilities f ON f.faid = c.faid\n"
						+ "WHERE c.fned BETWEEN param1 AND param2\n"
						+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\n"
						+ "END";
				break;
			case 42:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecialty()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".specialties ORDER BY synm;\n"
						+ "END";
				break;
			case 43:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpservice()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvservices ORDER BY srnm;\n"
						+ "END";
				break;
			case 44:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpsetup()\n"
						+ "BEGIN\n"
						+ "SELECT stid, stva FROM " + dbSchema + ".setup ORDER BY stid;\n"
						+ "END";
				break;
			case 45:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpstpid(param1 SMALLINT)\n"
						+ "BEGIN\n"
						+ "SELECT stva FROM " + dbSchema + ".setup WHERE stid = param1;\n"
						+ "END";
				break;
			case 46:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpsubspecial()\n"
						+ "BEGIN\n"
						+ "SELECT * FROM " + dbSchema + ".udvsubspecial ORDER BY sbnm;\n"
						+ "END";
				break;
			case 47:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpturnaround()\n"
						+ "BEGIN\n"
						+ "SELECT taid, grss, embd, micr, rout, finl, tanm\n"
						+ "FROM " + dbSchema + ".turnaround ORDER BY tanm;\n"
						+ "END";
				break;
			case 48:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpwdy(param1 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT wdid, wdno, wdtp, wddt FROM " + dbSchema + ".workdays\n"
						+ "WHERE wddt >= param1 ORDER BY wddt;\n"
						+ "END";
				break;
			case 49:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpwdydte(param1 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT wdno FROM " + dbSchema + ".workdays WHERE wddt = param1;\n"
						+ "END";
				break;
			case 50:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpwdynxt(param1 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT MIN(wddt) AS wddt FROM " + dbSchema + ".workdays\n"
						+ "WHERE wddt > param1 AND wdtp = 'D';\n"
						+ "END";
				break;
			default:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpwdyprv(param1 DATE)\n"
						+ "BEGIN\n"
						+ "SELECT MAX(wddt) AS wddt FROM workdays\n"
						+ "WHERE wddt < param1 AND wdtp = 'D';\n"
						+ "END";
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
		for (int i = 0; i < 52; i++) {
			switch (i) {
			case 0:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaccessions AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvaccessions ORDER BY acnm;\n"
						+ "END";
				break;
			case 1:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpadditionals @param1 INT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT prid, adcd, adv5, adv1, adv2, adv3, adv4, addt, prnm, prls, prfr, cano\n"
						+ "FROM " + dbSchema + ".udvadditionals WHERE caid = @param1 ORDER BY addt;\n"
						+ "END";
				break;
			case 2:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaddlast @param1 SMALLINT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT MAX(addt) AS addt FROM " + dbSchema + ".additionals WHERE adcd = @param1;\n"
						+ "END";
				break;
			case 3:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaddspg @param1 DATETIME, @param2 DATETIME AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc,\n"
						+ "f.fanm, COUNT(a.caid) AS qty, SUM(a.adv1) AS adv1, SUM(a.adv2) AS adv2,\n" 
						+ "SUM(a.adv3) AS adv3, SUM(a.adv4) AS adv4, SUM(a.adv5) AS adv5\n"
						+ "FROM " + dbSchema + ".additionals AS a\n"
						+ "INNER JOIN " + dbSchema + ".cases AS c ON c.caid = a.caid\n"
						+ "INNER JOIN " + dbSchema + ".facilities AS f ON f.faid = c.faid\n"
						+ "INNER JOIN " + dbSchema + ".specimaster AS m ON m.smid = c.smid\n"
						+ "INNER JOIN " + dbSchema + ".specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".procedures r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid\n"
						+ "WHERE c.fned BETWEEN @param1 AND @param2\n"
						+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\n" 
						+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid;\n"
						+ "END";
				break;
			case 4:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaddsum @param1 DATETIME, @param2 DATETIME AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(caid) AS adca,\n"
						+ "SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2, SUM(adv3) AS adv3, SUM(adv4) AS adv4\n"
						+ "FROM " + dbSchema + ".udvadditionals WHERE addt BETWEEN @param1 AND @param2\n"
						+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr\n"
						+ "ORDER BY faid, syid, sbid, poid, prid;\n"
						+ "END";
				break;
			case 5:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpaddyear @param1 DATETIME, @param2 DATETIME AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, DATEPART(YEAR, addt) as yearid,\n"
						+ "COUNT(caid) AS adca, SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2,\n"
						+ "SUM(adv3) AS adv3, SUM(adv4) AS adv4\n"
						+ "FROM " + dbSchema + ".udvadditionals\n"
						+ "WHERE addt BETWEEN @param1 AND @param2\n"
						+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, DATEPART(YEAR, addt) \n"
						+ "ORDER BY faid, syid, sbid, poid, sgid, yearid;\n"
						+ "END";
				break;
			case 6:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcoder1 AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + dbSchema + ".coder1 ORDER BY conm;\n"
						+ "END";
				break;
			case 7:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcoder2 AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + dbSchema + ".coder2 ORDER BY conm;\n"
						+ "END";
				break;
			case 8:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcoder3 AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + dbSchema + ".coder3 ORDER BY conm;\n"
						+ "END";
				break;
			case 9:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcoder4 AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM " + dbSchema + ".coder4 ORDER BY conm;\n"
						+ "END";
				break;
			case 10:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcmt @param1 INT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT com1, com2, com3, com4 FROM " + dbSchema + ".comments WHERE caid = @param1;\n"
						+ "END";
				break;
			case 11:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcseid @param1 CHAR(12) AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT caid FROM " + dbSchema + ".cases WHERE cano = @param1;\n"
						+ "END";
				break;
			case 12:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcseno @param1 INT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT cano FROM " + dbSchema + ".cases WHERE caid = @param1;\n"
						+ "END";
				break;
			case 13:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcsespe @param1 INT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT c.smid, c.fned, c.cano, s.spid FROM " + dbSchema + ".cases AS c\n"
						+ "INNER JOIN " + dbSchema + ".specimens AS s ON s.caid = c.caid AND s.smid = c.smid\n"
						+ "WHERE c.caid = @param1;\n"
						+ "END";
				break;
			case 14:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpcsesum @param1 DATETIME, @param2 DATETIME AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT faid, syid, sbid, poid, fnid, fanm, synm, sbnm, sbdc, ponm, fnnm, fnls, fnfr, COUNT(caid) AS caca,\n"
						+ "SUM(CAST(casp as INT)) AS casp, SUM(CAST(cabl as INT)) AS cabl, SUM(CAST(casl as INT)) AS casl, SUM(CAST(cahe as INT)) AS cahe,\n"
						+ "SUM(CAST(cass as INT)) AS cass, SUM(CAST(caih as INT)) AS caih, SUM(CAST(camo as INT)) AS camo, SUM(CAST(cafs as INT)) AS cafs,\n"
						+ "SUM(CAST(casy as INT)) AS casy, SUM(CAST(grta as INT)) AS grta, SUM(CAST(emta as INT)) AS emta, SUM(CAST(mita as INT)) AS mita,\n"
						+ "SUM(CAST(rota as INT)) AS rota, SUM(CAST(fnta as INT)) AS fnta, SUM(CAST(cav5 as INT)) AS cav5, SUM(cav1) AS cav1, SUM(cav2) AS cav2,\n"
						+ "SUM(cav3) AS cav3, SUM(cav4) AS cav4\n"
						+ "FROM " + dbSchema + ".udvcases\n"
						+ "WHERE (fned BETWEEN @param1 AND @param2)\n"
						+ "GROUP BY faid, syid, sbid, poid, fnid, fanm, synm, sbnm, sbdc, ponm, fnnm, fnls, fnfr\n"
						+ "ORDER BY faid, syid, sbid, poid, fnid;\n"
						+ "END";
				break;
			case 15:
				sql = "CREATE PROCEDURE " + dbSchema + ".udperrselect AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT caid, erid, cano FROM " + dbSchema + ".errors WHERE erid > 0 ORDER BY cano;\n"
						+ "END";
				break;
			case 16:
				sql = "CREATE PROCEDURE " + dbSchema + ".udperrcmt @param1 INT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT erdc FROM " + dbSchema + ".errors WHERE caid = @param1;\n"
						+ "END";
				break;
			case 17:
				sql = "CREATE PROCEDURE " + dbSchema + ".udperrredo AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT caid FROM " + dbSchema + ".errors WHERE erid = 0 ORDER BY caid;\n"
						+ "END";
				break;
			case 18:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfacility AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT faid, fafl, fald, fanm, fadc FROM " + dbSchema + ".facilities ORDER BY fanm;\n"
						+ "END";
				break;
			case 19:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfrzsid @param1 INT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT prid, frbl, frsl, frv5, frv1, frv2, frv3, frv4, prnm, prls, spdc, smnm\n"
						+ "FROM " + dbSchema + ".udvfrozens WHERE spid = @param1;"
						+ "END";
				break;
			case 20:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfrzspg @param1 DATETIME, @param2 DATETIME AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, COUNT(spid) AS frsp,\n"
						+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
						+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
						+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
						+ "FROM " + dbSchema + ".udvfrozens\n"
						+ "WHERE (aced BETWEEN @param1 AND @param2)\n"
						+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc\n"
						+ "ORDER BY faid, syid, sbid, poid, sgid;\n"
						+ "END";
				break;
			case 21:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpFrzSu5 @param1 DATETIME, @param2 DATETIME AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT COUNT(*) AS QTY, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
						+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
						+ "FROM " + dbSchema + ".udvfrozens WHERE aced BETWEEN @param1 AND @param2;\n"
						+ "END";
				break;
			case 22:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfrzsum @param1 DATETIME, @param2 DATETIME AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(spid) AS frsp,\n"
						+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
						+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
						+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
						+ "FROM udvfrozens WHERE (aced BETWEEN @param1 AND @param2)\n"
						+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr\n"
						+ "ORDER BY faid, syid, sbid, poid, prid;\n"
						+ "END";
				break;
			case 23:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpfrzyear @param1 DATETIME, @param2 DATETIME AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc,\n"
						+ "DATEPART(YEAR, aced) as yearid, COUNT(spid) AS frsp,\n"
						+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
						+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
						+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
						+ "FROM " + dbSchema + ".udvfrozens\n"
						+ "WHERE aced BETWEEN @param1 AND @param2 \n"
						+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, DATEPART(YEAR, aced) \n"
						+ "ORDER BY faid, syid, sbid, poid, sgid, yearid;\n"
						+ "END";
				break;
			case 24:
				sql = "CREATE PROCEDURE " + dbSchema + ".udporder @param1 INT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT orqy, orv1, orv2, orv3, orv4, ognm FROM " + dbSchema + ".udvorders WHERE spid = @param1 ORDER BY ognm;\n"
						+ "END";
				break;
			case 25:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpordergroup AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvordergroups ORDER BY ognm;\n"
						+ "END";
				break;
			case 26:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpordermaster AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvordermaster ORDER BY omnm;\n"
						+ "END";
				break;
			case 27:
				sql = "CREATE PROCEDURE " + dbSchema + ".udppending AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvpending ORDER BY pnid;\n"
						+ "END";
				break;
			case 28:
				sql = "CREATE PROCEDURE " + dbSchema + ".udppendingrouted @param1 DATETIME, @param2 DATETIME AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvpending\n"
						+ "WHERE (roed BETWEEN @param1 AND @param2)\n"
						+ "ORDER BY pnid;\n"
						+ "END";
				break;
			case 29:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpprocedure AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT poid, ponm, podc FROM " + dbSchema + ".procedures ORDER BY ponm;\n"
						+ "END";
				break;
			case 30:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpprsname AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM persons ORDER BY prnm;\n"
						+ "END";
				break;
			case 31:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpprsid @param1 SMALLINT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT prvl FROM " + dbSchema + ".persons WHERE prid = @param1;\n"
						+ "END";
				break;
			case 32:
				sql = "CREATE PROCEDURE " + dbSchema + ".udprule AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT ruid, runm, rudc FROM " + dbSchema + ".rules ORDER BY ruid;\n"
						+ "END";
				break;
			case 33:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpschedserv @param1 DATE, @param2 DATE AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT wdid, srid, prid, prnm, srnm\n"
						+ "FROM " + dbSchema + ".udvschedules\n"
						+ "WHERE wddt BETWEEN @param1 AND @param2 \n"
						+ "ORDER BY srnm, wdid;\n"
						+ "END";
				break;
			case 34:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpschedstaff @param1 DATE, @param2 DATE AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT wdid, srid, prid, prnm, srnm\n"
						+ "FROM " + dbSchema + ".udvschedules\n"
						+ "WHERE wddt BETWEEN @param1 AND @param2 \n"
						+ "ORDER BY prnm, wdid, srnm;\n"
						+ "END";
				break;
			case 35:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpschedsum @param1 DATE, @param2 DATE AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvschedules\n"
						+ "WHERE wddt BETWEEN @param1 AND @param2 \n"
						+ "ORDER BY faid, prid, wdid, srid\n"
						+ "END";
				break;
			case 36:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecimens @param1 INT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT spid, smid, spbl, spsl, spfr, sphe, spss, spih, spmo,\n"
						+ "spv5, spv1, spv2, spv3, spv4, spdc, smnm, smdc, ponm\n"
						+ "FROM " + dbSchema + ".udvspecimens WHERE caid = @param1 ORDER BY spid;\n"
						+ "END";
				break;
			case 37:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecgroup AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvspecigroups ORDER BY sgdc;\n"
						+ "END";
				break;
			case 38:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecyear @param1 DATE, @param2 DATE \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm,\n"
						+ "DATEPART(YEAR, c.fned) as yearid, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
						+ "SUM(s.spv1) AS spv1, SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4,\n"
						+ "SUM(s.spv5) AS spv5 FROM " + dbSchema + ".specigroups g\n"
						+ "INNER JOIN " + dbSchema + ".specimaster m ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".specimens s ON m.smid = s.smid\n"
						+ "INNER JOIN " + dbSchema + ".cases c ON c.caid = s.caid\n"
						+ "INNER JOIN " + dbSchema + ".procedures r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties y ON y.syid = b.syid\n"
						+ "INNER JOIN " + dbSchema + ".facilities f ON f.faid = c.faid WHERE c.fned BETWEEN @param1 AND @param2 \n"
						+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, DATEPART(YEAR, c.fned)\n"
						+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid\n"
						+ "END";
			case 39:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecmaster AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvspecimaster ORDER BY smnm;\n"
						+ "END";
				break;
			case 40:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecsu5 @param1 DATE, @param2 DATE AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT g.sgid, COUNT(s.spid) AS qty, SUM(s.spv1) AS spv1,\n"
						+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4\n"
						+ "FROM " + dbSchema + ".specigroups g\n"
						+ "INNER JOIN " + dbSchema + ".specimaster m ON g.sgid = m.sgid \n"
						+ "INNER JOIN " + dbSchema + ".specimens s ON m.smid = s.smid\n"
						+ "INNER JOIN " + dbSchema + ".cases c ON c.caid = s.caid\n"
						+ "WHERE c.fned BETWEEN @param1 AND @param2\n"
						+ "GROUP BY g.sgid\n"
						+ "END";
				break;
			case 41:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecsum @param1 DATE, @param2 DATE AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT b.syid, g.sbid, g.sgid, c.faid, y.synm, b.sbnm, g.sgdc, f.fanm,\n"
						+ "COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
						+ "SUM(s.sphe) AS sphe, SUM(s.spss) AS spss, SUM(s.spih) AS spih, SUM(s.spv1) AS spv1,\n"
						+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, SUM(s.spv5) AS spv5\n"
						+ "FROM " + dbSchema + ".specigroups g\n"
						+ "INNER JOIN " + dbSchema + ".specimaster m ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".specimens s ON m.smid = s.smid\n"
						+ "INNER JOIN " + dbSchema + ".cases c ON c.caid = s.caid\n"
						+ "INNER JOIN " + dbSchema + ".procedures r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties y ON y.syid = b.syid\n"
						+ "INNER JOIN " + dbSchema + ".facilities f ON f.faid = c.faid\n"
						+ "WHERE c.fned BETWEEN @param1 AND @param2\n"
						+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\n"
						+ "ORDER BY y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\n"
						+ "END";
				break;
			case 42:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpspecialty AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".specialties ORDER BY synm;\n"
						+ "END";
				break;
			case 43:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpservice AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvservices ORDER BY srnm;\n"
						+ "END";
				break;
			case 44:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpsetup AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT stid, stva FROM " + dbSchema + ".setup ORDER BY stid;\n"
						+ "END";
				break;
			case 45:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpstpid @param1 SMALLINT AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT stva FROM " + dbSchema + ".setup WHERE stid = @param1;\n"
						+ "END";
				break;
			case 46:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpsubspecial AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT * FROM " + dbSchema + ".udvsubspecial ORDER BY sbnm;\n"
						+ "END";
				break;
			case 47:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpturnaround AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT taid, grss, embd, micr, rout, finl, tanm\n"
						+ "FROM " + dbSchema + ".turnaround ORDER BY tanm;\n"
						+ "END";
				break;
			case 48:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpwdy @param1 DATE AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT wdid, wdno, wdtp, wddt FROM " + dbSchema + ".workdays\n"
						+ "WHERE wddt >= @param1 ORDER BY wddt;\n"
						+ "END";
				break;
			case 49:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpwdydte @param1 DATE AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT wdno FROM " + dbSchema + ".workdays WHERE wddt = @param1;\n"
						+ "END";
				break;
			case 50:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpwdynxt @param1 DATE AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT MIN(wddt) AS wddt FROM " + dbSchema + ".workdays\n"
						+ "WHERE wddt > @param1 AND wdtp = 'D';\n"
						+ "END";
				break;
			default:
				sql = "CREATE PROCEDURE " + dbSchema + ".udpwdyprv @param1 DATE AS \n"
						+ "BEGIN\n"
						+ "SET NOCOUNT ON;\n"
						+ "SELECT MAX(wddt) AS wddt FROM " + dbSchema + ".workdays\n"
						+ "WHERE wddt < @param1 AND wdtp = 'D';\n"
						+ "END";
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

	private void createTables() {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 29; i++) {
			switch (i) {
			case 0:
				sql = "CREATE TABLE " + dbSchema + ".setup (stid SMALLINT PRIMARY KEY, "
						+ "stva VARCHAR(64) NOT NULL)";
				break;
			case 1:
				sql = "CREATE TABLE " + dbSchema + ".workdays (wdid INT PRIMARY KEY, wdno INT NOT NULL, "
						+ "wdtp CHAR(1) NOT NULL, wddt DATE UNIQUE NOT NULL)";
				break;
			case 2:
				sql = "CREATE TABLE " + dbSchema + ".errors (caid INT PRIMARY KEY, erid SMALLINT NOT NULL, "
						+ "cano CHAR(13) UNIQUE NOT NULL, erdc VARCHAR(2048) NOT NULL)";
				break;
			case 3:
				sql = "CREATE TABLE " + dbSchema + ".facilities (faid SMALLINT PRIMARY KEY, "
						+ "fafl CHAR(1) NOT NULL, fald CHAR(1) NOT NULL, fanm VARCHAR(4) UNIQUE NOT NULL, "
						+ "fadc VARCHAR(80) NOT NULL)";
				break;
			case 4:
				sql = "CREATE TABLE " + dbSchema + ".persons (prid SMALLINT PRIMARY KEY, "
						+ "prvl INT NOT NULL, prdt DATE NOT NULL, prac CHAR(1) NOT NULL, "
						+ "prcd CHAR(2) NOT NULL, prnm CHAR(3) NOT NULL, prls VARCHAR(30) NOT NULL, "
						+ "prfr VARCHAR(30) NOT NULL)";
				break;
			case 5:
				sql = "CREATE TABLE " + dbSchema + ".procedures (poid SMALLINT PRIMARY KEY, "
						+ "ponm VARCHAR(16) UNIQUE NOT NULL, podc VARCHAR(256) NOT NULL)";
				break;
			case 6:
				sql = "CREATE TABLE " + dbSchema + ".rules (ruid SMALLINT PRIMARY KEY, "
						+ "runm VARCHAR(16) UNIQUE NOT NULL, rudc VARCHAR(256) NOT NULL)";
				break;
			case 7:
				sql = "CREATE TABLE " + dbSchema + ".specialties (syid SMALLINT PRIMARY KEY, "
						+ "syfl CHAR(1) NOT NULL, syld CHAR(1) NOT NULL, sysp CHAR(1) NOT NULL, "
						+ "synm VARCHAR(16) UNIQUE NOT NULL)";
				break;
			case 8:
				sql = "CREATE TABLE " + dbSchema + ".turnaround (taid SMALLINT PRIMARY KEY, "
						+ "grss SMALLINT NOT NULL, embd SMALLINT NOT NULL, micr SMALLINT NOT NULL, "
						+ "rout SMALLINT NOT NULL, finl SMALLINT NOT NULL, "
						+ "tanm VARCHAR(16) UNIQUE NOT NULL)";
				break;
			case 9:
				sql = "CREATE TABLE " + dbSchema + ".coder1 (coid SMALLINT PRIMARY KEY, "
						+ "ruid SMALLINT NOT NULL REFERENCES " + dbSchema + ".rules (ruid), "
						+ "coqy SMALLINT NOT NULL, cov1 DECIMAL(5, 3) NOT NULL, "
						+ "cov2 DECIMAL(5, 3) NOT NULL, cov3 DECIMAL(5, 3) NOT NULL, "
						+ "conm VARCHAR(16) UNIQUE NOT NULL, codc VARCHAR(256) NOT NULL)";
				break;
			case 10:
				sql = "CREATE TABLE " + dbSchema + ".coder2 (coid SMALLINT PRIMARY KEY, "
						+ "ruid SMALLINT NOT NULL REFERENCES " + dbSchema + ".rules (ruid), "
						+ "coqy SMALLINT NOT NULL, cov1 DECIMAL(5, 3) NOT NULL, "
						+ "cov2 DECIMAL(5, 3) NOT NULL, cov3 DECIMAL(5, 3) NOT NULL, "
						+ "conm VARCHAR(16) UNIQUE NOT NULL, codc VARCHAR(256) NOT NULL)";
				break;
			case 11:
				sql = "CREATE TABLE " + dbSchema + ".coder3 (coid SMALLINT PRIMARY KEY, "
						+ "ruid SMALLINT NOT NULL REFERENCES " + dbSchema + ".rules (ruid), "
						+ "coqy SMALLINT NOT NULL, cov1 DECIMAL(5, 3) NOT NULL, "
						+ "cov2 DECIMAL(5, 3) NOT NULL, cov3 DECIMAL(5, 3) NOT NULL, "
						+ "conm VARCHAR(16) UNIQUE NOT NULL, codc VARCHAR(128) NOT NULL)";
				break;
			case 12:
				sql = "CREATE TABLE " + dbSchema + ".coder4 (coid SMALLINT PRIMARY KEY, "
						+ "ruid SMALLINT NOT NULL REFERENCES " + dbSchema + ".rules (ruid), "
						+ "coqy SMALLINT NOT NULL, cov1 DECIMAL(5, 3) NOT NULL, "
						+ "cov2 DECIMAL(5, 3) NOT NULL, cov3 DECIMAL(5, 3) NOT NULL, "
						+ "conm VARCHAR(16) UNIQUE NOT NULL, codc VARCHAR(256) NOT NULL)";
				break;
			case 13:
				sql = "CREATE TABLE " + dbSchema + ".accessions (acid SMALLINT PRIMARY KEY, "
						+ "syid SMALLINT NOT NULL REFERENCES " + dbSchema + ".specialties (syid), "
						+ "acfl CHAR(1) NOT NULL, acld CHAR(1) NOT NULL, acnm VARCHAR(30) UNIQUE NOT NULL)";
				break;
			case 14:
				sql = "CREATE TABLE " + dbSchema + ".subspecial (sbid SMALLINT PRIMARY KEY, "
						+ "syid SMALLINT NOT NULL REFERENCES " + dbSchema + ".specialties (syid), "
						+ "sbnm VARCHAR(8) UNIQUE NOT NULL, sbdc VARCHAR(32) NOT NULL)";
				break;
			case 15:
				sql = "CREATE TABLE " + dbSchema + ".ordertypes (otid SMALLINT PRIMARY KEY, "
						+ "otnm VARCHAR(8) UNIQUE NOT NULL)";
				break;
			case 16:
				sql = "CREATE TABLE " + dbSchema + ".ordergroups (ogid SMALLINT PRIMARY KEY, "
						+ "otid SMALLINT NOT NULL REFERENCES " + dbSchema + ".ordertypes (otid), "
						+ "ogc1 SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder1 (coid), "
						+ "ogc2 SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder2 (coid), "
						+ "ogc3 SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder3 (coid), "
						+ "ogc4 SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder4 (coid), "
						+ "ogc5 INT NOT NULL, ognm VARCHAR(8) UNIQUE NOT NULL, ogdc VARCHAR(64) NOT NULL)";
				break;
			case 17:
				sql = "CREATE TABLE " + dbSchema + ".ordermaster (omid SMALLINT PRIMARY KEY, "
						+ "ogid SMALLINT NOT NULL REFERENCES " + dbSchema + ".ordergroups (ogid), "
						+ "omnm VARCHAR(15) UNIQUE NOT NULL, omdc VARCHAR(80) NOT NULL)";
				break;
			case 18:
				sql = "CREATE TABLE " + dbSchema + ".specigroups (sgid SMALLINT PRIMARY KEY, "
						+ "sbid SMALLINT NOT NULL REFERENCES " + dbSchema + ".subspecial (sbid), "
						+ "poid SMALLINT NOT NULL REFERENCES " + dbSchema + ".procedures (poid), "
						+ "sg1b SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder1 (coid), "
						+ "sg1m SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder1 (coid), "
						+ "sg1r SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder1 (coid), "
						+ "sg2b SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder2 (coid), "
						+ "sg2m SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder2 (coid), "
						+ "sg2r SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder2 (coid), "
						+ "sg3b SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder3 (coid), "
						+ "sg3m SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder3 (coid), "
						+ "sg3r SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder3 (coid), "
						+ "sg4b SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder4 (coid), "
						+ "sg4m SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder4 (coid), "
						+ "sg4r SMALLINT NOT NULL REFERENCES " + dbSchema + ".coder4 (coid), "
						+ "sgv5 INT NOT NULL, sgln CHAR(1) NOT NULL, sgdc VARCHAR(64) UNIQUE NOT NULL)";
				break;
			case 19:
				sql = "CREATE TABLE " + dbSchema + ".specimaster (smid SMALLINT PRIMARY KEY, "
						+ "sgid SMALLINT NOT NULL REFERENCES " + dbSchema + ".specigroups (sgid), "
						+ "taid SMALLINT NOT NULL REFERENCES " + dbSchema + ".turnaround (taid), "
						+ "smnm VARCHAR(16) UNIQUE NOT NULL, smdc VARCHAR(80) NOT NULL)";
				break;
			case 20:
				sql = "CREATE TABLE " + dbSchema + ".services (srid SMALLINT PRIMARY KEY, "
						+ "faid SMALLINT NOT NULL REFERENCES " + dbSchema + ".facilities (faid), "
						+ "sbid SMALLINT NOT NULL REFERENCES " + dbSchema + ".subspecial (sbid), "
						+ "srcd SMALLINT NOT NULL, srnm VARCHAR(8) UNIQUE NOT NULL, "
						+ "srdc VARCHAR(64) NOT NULL)";
				break;
			case 21:
				sql = "CREATE TABLE " + dbSchema + ".schedules (wdid INT NOT NULL REFERENCES " + dbSchema
						+ ".workdays (wdid), srid SMALLINT NOT NULL REFERENCES " + dbSchema
						+ ".services (srid), prid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid), "
						+ "PRIMARY KEY(wdid, srid, prid))";
				break;
			case 22:
				sql = "CREATE TABLE " + dbSchema + ".cases (caid INT PRIMARY KEY, "
						+ "faid SMALLINT NOT NULL REFERENCES " + dbSchema + ".facilities (faid), "
						+ "sbid SMALLINT NOT NULL REFERENCES " + dbSchema + ".subspecial (sbid), "
						+ "smid SMALLINT NOT NULL REFERENCES " + dbSchema + ".specimaster (smid), "
						+ "grid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid), "
						+ "emid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid), "
						+ "miid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid), "
						+ "roid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid), "
						+ "fnid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid), "
						+ "grta SMALLINT NOT NULL, emta SMALLINT NOT NULL, mita SMALLINT NOT NULL, "
						+ "rota SMALLINT NOT NULL, fnta SMALLINT NOT NULL, casp SMALLINT NOT NULL, "
						+ "cabl SMALLINT NOT NULL, casl SMALLINT NOT NULL, casy SMALLINT NOT NULL, "
						+ "cafs SMALLINT NOT NULL, cahe SMALLINT NOT NULL, cass SMALLINT NOT NULL, "
						+ "caih SMALLINT NOT NULL, camo SMALLINT NOT NULL, cav5 INT NOT NULL, "
						+ "aced DATETIME NOT NULL, gred DATETIME NOT NULL, emed DATETIME NOT NULL, "
						+ "mied DATETIME NOT NULL, roed DATETIME NOT NULL, fned DATETIME NOT NULL, "
						+ "cav1 DECIMAL(5, 3) NOT NULL, cav2 DECIMAL(5, 3) NOT NULL, cav3 DECIMAL(5, 3) NOT NULL, "
						+ "cav4 DECIMAL(5, 3) NOT NULL, cano CHAR(13) UNIQUE NOT NULL)";
				break;
			case 23:
				sql = "CREATE TABLE " + dbSchema + ".specimens (spid INT PRIMARY KEY, "
						+ "caid INT NOT NULL REFERENCES " + dbSchema + ".cases (caid), "
						+ "smid SMALLINT NOT NULL REFERENCES " + dbSchema + ".specimaster (smid), "
						+ "spbl SMALLINT NOT NULL, spsl SMALLINT NOT NULL, spfr SMALLINT NOT NULL, "
						+ "sphe SMALLINT NOT NULL, spss SMALLINT NOT NULL, spih SMALLINT NOT NULL, "
						+ "spmo SMALLINT NOT NULL, spv5 INT NOT NULL, spv1 DECIMAL(5, 3) NOT NULL, "
						+ "spv2 DECIMAL(5, 3) NOT NULL, spv3 DECIMAL(5, 3) NOT NULL, "
						+ "spv4 DECIMAL(5, 3) NOT NULL, spdc VARCHAR(80) NOT NULL)";
				break;
			case 24:
				sql = "CREATE TABLE " + dbSchema + ".orders (" + "spid INT NOT NULL REFERENCES " + dbSchema
						+ ".specimens (spid), ogid SMALLINT NOT NULL REFERENCES " + dbSchema
						+ ".ordergroups (ogid), orqy SMALLINT NOT NULL,\n"
						+ "orv1 DECIMAL(5, 3) NOT NULL, orv2 DECIMAL(5, 3) NOT NULL,\n"
						+ "orv3 DECIMAL(5, 3) NOT NULL, orv4 DECIMAL(5, 3) NOT NULL, PRIMARY KEY (spid, ogid))";
				break;
			case 25:
				sql = "CREATE TABLE " + dbSchema + ".frozens (" + "spid INT PRIMARY KEY REFERENCES " + dbSchema
						+ ".specimens (spid),\n" + "prid SMALLINT NOT NULL REFERENCES " + dbSchema
						+ ".persons (prid), frbl SMALLINT NOT NULL,\n"
						+ "frsl SMALLINT NOT NULL, frv5 INT NOT NULL, frv1 DECIMAL(5, 3) NOT NULL,\n"
						+ "frv2 DECIMAL(5, 3) NOT NULL, frv3 DECIMAL(5, 3) NOT NULL,\n"
						+ "frv4 DECIMAL(5, 3) NOT NULL)";
				break;
			case 26:
				sql = "CREATE TABLE " + dbSchema + ".additionals (" + "caid INT NOT NULL REFERENCES " + dbSchema
						+ ".cases (caid),\n" + "prid SMALLINT NOT NULL REFERENCES " + dbSchema
						+ ".persons (prid), adcd SMALLINT NOT NULL,\n"
						+ "adv5 INT NOT NULL, addt DATETIME NOT NULL, adv1 DECIMAL(5, 3) NOT NULL,\n"
						+ "adv2 DECIMAL(5, 3) NOT NULL, adv3 DECIMAL(5, 3) NOT NULL,\n"
						+ "adv4 DECIMAL(5, 3) NOT NULL, PRIMARY KEY (caid, prid, adcd, addt))";
				break;
			case 27:
				sql = "CREATE TABLE " + dbSchema + ".comments (caid INT PRIMARY KEY REFERENCES " + dbSchema
						+ ".cases (caid), com1 VARCHAR(2048) NOT NULL, com2 VARCHAR(2048) NOT NULL,\n"
						+ "com3 VARCHAR(2048) NOT NULL, com4 VARCHAR(2048) NOT NULL)";
				break;
			default:
				sql = "CREATE TABLE " + dbSchema + ".pending (" + "pnid INT PRIMARY KEY,\n"
						+ "faid SMALLINT NOT NULL REFERENCES " + dbSchema + ".facilities (faid),\n"
						+ "sbid SMALLINT NOT NULL REFERENCES " + dbSchema + ".subspecial (sbid),\n"
						+ "smid SMALLINT NOT NULL REFERENCES " + dbSchema + ".specimaster (smid),\n"
						+ "poid SMALLINT NOT NULL REFERENCES " + dbSchema + ".procedures (poid),\n"
						+ "grid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid),\n"
						+ "emid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid),\n"
						+ "miid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid),\n"
						+ "roid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid),\n"
						+ "fnid SMALLINT NOT NULL REFERENCES " + dbSchema + ".persons (prid),\n"
						+ "grta SMALLINT NOT NULL, emta SMALLINT NOT NULL, mita SMALLINT NOT NULL,\n"
						+ "rota SMALLINT NOT NULL, fnta SMALLINT NOT NULL, pnst SMALLINT NOT NULL,\n"
						+ "pnsp SMALLINT NOT NULL, pnbl SMALLINT NOT NULL, pnsl SMALLINT NOT NULL,\n"
						+ "pnv5 INT NOT NULL, aced DATETIME NOT NULL, gred DATETIME NOT NULL,\n"
						+ "emed DATETIME NOT NULL, mied DATETIME NOT NULL, roed DATETIME NOT NULL,\n"
						+ "fned DATETIME NOT NULL, pnno CHAR(13) UNIQUE NOT NULL)";
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

	private void createViews() {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 20; i++) {
			switch (i) {
			case 0:
				sql = "CREATE VIEW " + dbSchema + ".udvaccessions AS\n"
						+ "SELECT a.acid, a.syid, a.acfl, a.acld, a.acnm, y.syfl, y.syld, y.sysp, y.synm\n"
						+ "FROM " + dbSchema + ".accessions AS a\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = a.syid";
				break;
			case 1:
				sql = "CREATE VIEW " + dbSchema + ".udvadditionals AS\n"
						+ "SELECT a.caid, a.prid, a.adcd, a.adv5, a.adv1, a.adv2,\n"
						+ "a.adv3, a.adv4, a.addt, p.prnm, p.prls, p.prfr, c.faid,\n"
						+ "c.cano, f.fanm, g.poid, g.sbid, g.sgid, r.ponm, b.syid,\n"
						+ "b.sbnm, g.sgdc, y.synm FROM " + dbSchema + ".additionals AS a\n"
						+ "INNER JOIN " + dbSchema + ".persons AS p ON p.prid = a.prid\n"
						+ "INNER JOIN " + dbSchema + ".cases AS c ON c.caid = a.caid\n"
						+ "INNER JOIN " + dbSchema + ".facilities AS f ON f.faid = c.faid\n"
						+ "INNER JOIN " + dbSchema + ".specimaster AS m ON m.smid = c.smid\n"
						+ "INNER JOIN " + dbSchema + ".specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid";
				break;
			case 2:
				sql = "CREATE VIEW " + dbSchema + ".udvaddlastorder AS\n"
						+ "SELECT MAX(addt) AS addt FROM " + dbSchema + ".additionals WHERE adcd > 2";
				break;
			case 3:
				sql = "CREATE VIEW " + dbSchema + ".udvcases AS\n"
						+ "SELECT c.caid, c.faid, c.sbid, c.smid, c.grid, c.emid,\n"
						+ "c.miid, c.roid, c.fnid, c.grta, c.emta, c.mita, c.rota,\n"
						+ "c.fnta, c.casp, c.cabl, c.casl, c.casy, c.cafs, c.cahe,\n"
						+ "c.cass, c.caih, c.camo, c.cav5, c.aced, c.gred, c.emed,\n"
						+ "c.mied, c.roed, c.fned, c.cav1, c.cav2, c.cav3, c.cav4,\n"
						+ "c.cano, f.fanm, m.smnm, m.smdc, g.poid, r.ponm, b.syid,\n"
						+ "b.sbnm, b.sbdc, y.synm, pg.prnm AS GRNM, pg.prls AS GRLS,\n"
						+ "pg.prfr AS GRFR, pe.prnm AS EMNM, pe.prls AS EMLS,\n"
						+ "pe.prfr AS EMFR, pm.prnm AS MINM, pm.prls AS MILS,\n"
						+ "pm.prfr AS MIFR, pr.prnm AS RONM, pr.prls AS ROLS,\n"
						+ "pr.prfr AS ROFR, pf.prnm AS fnnm, pf.prls AS fnls, pf.prfr AS fnfr\n"
						+ "FROM " + dbSchema + ".cases AS c\n"
						+ "INNER JOIN " + dbSchema + ".facilities AS f ON f.faid = c.faid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = c.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specimaster AS m ON m.smid = c.smid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pg ON pg.prid = c.grid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pe ON pe.prid = c.emid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pm ON pm.prid = c.miid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pr ON pr.prid = c.roid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pf ON pf.prid = c.fnid\n"
						+ "INNER JOIN " + dbSchema + ".specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid";
				break;
			case 4:
				sql = "CREATE VIEW " + dbSchema + ".udvcaseslast AS\n" + "SELECT MAX(fned) AS fned FROM " + dbSchema + ".cases";
				break;
			case 5:
				sql = "CREATE VIEW " + dbSchema + ".udvfrozens AS\n"
						+ "SELECT z.spid, z.prid, z.frbl, z.frsl, z.frv5,\n"
						+ "z.frv1, z.frv2, z.frv3, z.frv4, c.caid, c.faid,\n"
						+ "c.aced, c.cano, p.prnm, p.prls, p.prfr, s.spdc,\n"
						+ "m.smnm, m.smdc, f.fanm, g.poid, g.sbid, g.sgid,\n"
						+ "g.sgdc, r.ponm, b.syid, b.sbnm, y.synm\n"
						+ "FROM " + dbSchema + ".frozens AS z\n"
						+ "INNER JOIN " + dbSchema + ".specimens AS s ON s.spid = z.spid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS p ON p.prid = z.prid\n"
						+ "INNER JOIN " + dbSchema + ".cases AS c ON c.caid = s.caid\n"
						+ "INNER JOIN " + dbSchema + ".specimaster AS m ON m.smid = s.smid\n"
						+ "INNER JOIN " + dbSchema + ".facilities AS f ON f.faid = c.faid\n"
						+ "INNER JOIN " + dbSchema + ".specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid";
				break;
			case 6:
				sql = "CREATE VIEW " + dbSchema + ".udvordergroups AS\n"
						+ "SELECT g.ogid, g.otid, g.ogc1, g.ogc2, g.ogc3, g.ogc4,\n"
						+ "g.ogc5, g.ognm, g.ogdc, a.conm AS C1NM, b.conm AS C2NM,\n"
						+ "c.conm AS C3NM, d.conm AS C4NM, t.otnm\n"
						+ "FROM " + dbSchema + ".ordergroups AS g\n"
						+ "INNER JOIN " + dbSchema + ".ordertypes AS t ON t.otid = g.otid\n"
						+ "INNER JOIN " + dbSchema + ".coder1 AS a ON a.coid = g.ogc1\n"
						+ "INNER JOIN " + dbSchema + ".coder2 AS b ON b.coid = g.ogc2\n"
						+ "INNER JOIN " + dbSchema + ".coder3 AS c ON c.coid = g.ogc3\n"
						+ "INNER JOIN " + dbSchema + ".coder4 AS d ON d.coid = g.ogc4";
				break;
			case 7:
				sql = "CREATE VIEW " + dbSchema + ".udvordermaster AS\n"
						+ "SELECT m.omid, m.ogid, m.omnm, m.omdc, g.otid,\n"
						+ "g.ogc1, g.ogc2, g.ogc3, g.ogc4, g.ogc5, g.ognm,\n"
						+ "g.ogdc, a.conm AS C1NM, b.conm AS C2NM, c.conm AS C3NM,\n"
						+ "d.conm AS C4NM, t.otnm\n"
						+ "FROM " + dbSchema + ".ordermaster AS m\n"
						+ "INNER JOIN " + dbSchema + ".ordergroups AS g ON g.ogid = m.ogid\n"
						+ "INNER JOIN " + dbSchema + ".ordertypes AS t ON t.otid = g.otid\n"
						+ "INNER JOIN " + dbSchema + ".coder1 AS a ON a.coid = g.ogc1\n"
						+ "INNER JOIN " + dbSchema + ".coder2 AS b ON b.coid = g.ogc2\n"
						+ "INNER JOIN " + dbSchema + ".coder3 AS c ON c.coid = g.ogc3\n"
						+ "INNER JOIN " + dbSchema + ".coder4 AS d ON d.coid = g.ogc4";
				break;
			case 8:
				sql = "CREATE VIEW " + dbSchema + ".udvorders AS\n"
						+ "SELECT o.spid, o.ogid, o.orqy, o.orv1, o.orv2, o.orv3, o.orv4, g.ognm\n"
						+ "FROM " + dbSchema + ".orders AS o\n"
						+ "INNER JOIN " + dbSchema + ".ordergroups AS g ON g.ogid = o.ogid";
				break;
			case 9:
				sql = "CREATE VIEW " + dbSchema + ".udvpending AS\n"
						+ "SELECT p.pnid, p.faid, p.sbid, p.smid, p.grid, p.emid,\n"
						+ "p.miid, p.roid, p.fnid, p.grta, p.emta, p.mita, p.rota,\n"
						+ "p.fnta, p.pnst, p.pnsp, p.pnbl, p.pnsl, p.pnv5, p.aced,\n"
						+ "p.gred, p.emed, p.mied, p.roed, p.fned, p.pnno, f.fanm,\n"
						+ "m.sgid, m.taid, m.smnm, m.smdc, g.poid, r.ponm, b.sbnm,\n"
						+ "b.sbdc, b.syid, y.synm, pg.prnm AS GRNM, pg.prls AS GRLS,\n"
						+ "pg.prfr AS GRFR, pe.prnm AS EMNM, pe.prls AS EMLS,\n"
						+ "pe.prfr AS EMFR, pm.prnm AS MINM, pm.prls AS MILS,\n"
						+ "pm.prfr AS MIFR, pr.prnm AS RONM, pr.prls AS ROLS,\n"
						+ "pr.prfr AS ROFR, pf.prnm AS fnnm, pf.prls AS fnls, pf.prfr AS fnfr\n"
						+ "FROM " + dbSchema + ".pending AS p\n"
						+ "INNER JOIN " + dbSchema + ".facilities AS f ON f.faid = p.faid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = p.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specimaster AS m ON m.smid = p.smid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pg ON pg.prid = p.grid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pe ON pe.prid = p.emid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pm ON pm.prid = p.miid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pr ON pr.prid = p.roid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS pf ON pf.prid = p.fnid\n"
						+ "INNER JOIN " + dbSchema + ".specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid";
				break;
			case 10:
				sql = "CREATE VIEW " + dbSchema + ".udvpendinglast AS\n" + "SELECT MAX(aced) AS aced FROM " + dbSchema + ".pending";
				break;
			case 11:
				sql = "CREATE VIEW " + dbSchema + ".udvschedules AS\n"
						+ "SELECT s.wdid, s.srid, s.prid, v.faid, v.sbid,\n"
						+ "v.srcd, v.srnm, v.srdc, p.prnm, p.prls, p.prfr,\n"
						+ "w.wddt, b.syid, b.sbnm, y.synm, f.fanm\n"
						+ "FROM " + dbSchema + ".schedules AS s\n"
						+ "INNER JOIN " + dbSchema + ".services AS v ON v.srid = s.srid\n"
						+ "INNER JOIN " + dbSchema + ".persons AS p ON p.prid = s.prid\n"
						+ "INNER JOIN " + dbSchema + ".workdays AS w ON w.wdid = s.wdid\n"
						+ "INNER JOIN " + dbSchema + ".facilities AS f ON f.faid = v.faid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = v.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid";
				break;
			case 12:
				switch (dbID) {
				case DB_POSTG:
					sql = "CREATE VIEW " + dbSchema + ".udvschedweeks AS\n"
							+ "SELECT DISTINCT s.wdid, w.wddt\n"
							+ "FROM " + dbSchema + ".schedules AS s\n"
							+ "INNER JOIN " + dbSchema + ".workdays AS w ON w.wdid = s.wdid\n"
							+ "WHERE date_part('dow', w.wddt) = 1\n"
							+ "ORDER BY w.wddt";
					break;
				case DB_MSSQL:
					sql = "CREATE VIEW " + dbSchema + ".udvschedweeks AS\n"
							+ "SELECT DISTINCT s.wdid, w.wddt\n"
							+ "FROM " + dbSchema + ".schedules AS s\n"
							+ "INNER JOIN " + dbSchema + ".workdays AS w ON w.wdid = s.wdid\n"
							+ "WHERE DATEPART(weekday, w.wddt) = 1";
					break;
				case DB_MARIA:
					sql = "CREATE VIEW " + dbSchema + ".udvschedweeks AS\n"
							+ "SELECT DISTINCT s.wdid, w.wddt\n"
							+ "FROM " + dbSchema + ".schedules AS s\n"
							+ "INNER JOIN " + dbSchema + ".workdays AS w ON w.wdid = s.wdid\n"
							+ "WHERE DAYOFWEEK(w.wddt) = 2";
					break;
				default:
					sql = "CREATE VIEW " + dbSchema + ".udvschedweeks AS\n"
							+ "SELECT wdid, wddt\n"
							+ "FROM " + dbSchema + ".workdays\n"
							+ "ORDER BY wddt DESC FETCH FIRST 370 ROWS ONLY";
				}
				break;
			case 13:
				sql = "CREATE VIEW " + dbSchema + ".udvservices AS\n"
						+ "SELECT v.srid, v.faid, v.sbid, v.srcd, v.srnm,\n"
						+ "v.srdc, f.fanm, b.syid, b.sbnm, y.synm\n"
						+ "FROM " + dbSchema + ".services AS v\n"
						+ "INNER JOIN " + dbSchema + ".facilities AS f ON f.faid = v.faid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = v.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid";
				break;
			case 14:
				sql = "CREATE VIEW " + dbSchema + ".udvspecigroups AS\n"
						+ "SELECT g.sgid, g.sbid, g.poid, g.sg1b, g.sg1m, g.sg1r,\n"
						+ "g.sg2b, g.sg2m, g.sg2r, g.sg3b, g.sg3m, g.sg3r, g.sg4b,\n"
						+ "g.sg4m, g.sg4r, g.sgv5, g.sgln, g.sgdc, r.ponm, b.syid,\n"
						+ "b.sbnm, b.sbdc, y.synm, b1.conm AS C1NB, m1.conm AS C1NM,\n"
						+ "r1.conm AS C1NR, b2.conm AS C2NB, m2.conm AS C2NM,\n"
						+ "r2.conm AS C2NR, b3.conm AS C3NB, m3.conm AS C3NM,\n"
						+ "r3.conm AS C3NR, b4.conm AS C4NB, m4.conm AS C4NM,\n"
						+ "r4.conm AS C4NR\n"
						+ "FROM " + dbSchema + ".specigroups AS g\n"
						+ "INNER JOIN " + dbSchema + ".coder1 AS b1 ON b1.coid = g.sg1b\n"
						+ "INNER JOIN " + dbSchema + ".coder2 AS b2 ON b2.coid = g.sg2b\n"
						+ "INNER JOIN " + dbSchema + ".coder3 AS b3 ON b3.coid = g.sg3b\n"
						+ "INNER JOIN " + dbSchema + ".coder4 AS b4 ON b4.coid = g.sg4b\n"
						+ "INNER JOIN " + dbSchema + ".coder1 AS m1 ON m1.coid = g.sg1m\n"
						+ "INNER JOIN " + dbSchema + ".coder2 AS m2 ON m2.coid = g.sg2m\n"
						+ "INNER JOIN " + dbSchema + ".coder3 AS m3 ON m3.coid = g.sg3m\n"
						+ "INNER JOIN " + dbSchema + ".coder4 AS m4 ON m4.coid = g.sg4m\n"
						+ "INNER JOIN " + dbSchema + ".coder1 AS r1 ON r1.coid = g.sg1r\n"
						+ "INNER JOIN " + dbSchema + ".coder2 AS r2 ON r2.coid = g.sg2r\n"
						+ "INNER JOIN " + dbSchema + ".coder3 AS r3 ON r3.coid = g.sg3r\n"
						+ "INNER JOIN " + dbSchema + ".coder4 AS r4 ON r4.coid = g.sg4r\n"
						+ "INNER JOIN " + dbSchema + ".procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid";
				break;
			case 15:
				sql = "CREATE VIEW " + dbSchema + ".udvspecimaster AS\n"
						+ "SELECT m.smid, m.sgid, m.smnm, m.smdc, m.taid,\n"
						+ "t.grss, t.embd, t.micr, t.rout, t.finl, t.tanm,\n"
						+ "g.poid, g.sbid, g.sg1b, g.sg1m, g.sg1r, g.sg2b,\n"
						+ "g.sg2m, g.sg2r, g.sg3b, g.sg3m, g.sg3r, g.sg4b,\n"
						+ "g.sg4m, g.sg4r, g.sgv5, g.sgln, g.sgdc, b.syid,\n"
						+ "b.sbnm, b.sbdc, y.synm, r.ponm, b1.conm AS C1NB,\n"
						+ "m1.conm AS C1NM, r1.conm AS C1NR, b2.conm AS C2NB,\n"
						+ "m2.conm AS C2NM, r2.conm AS C2NR, b3.conm AS C3NB,\n"
						+ "m3.conm AS C3NM, r3.conm AS C3NR, b4.conm AS C4NB,\n"
						+ "m4.conm AS C4NM, r4.conm AS C4NR\n"
						+ "FROM " + dbSchema + ".specimaster AS m\n"
						+ "INNER JOIN " + dbSchema + ".turnaround AS t ON t.taid = m.taid\n"
						+ "INNER JOIN " + dbSchema + ".specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".coder1 AS b1 ON b1.coid = g.sg1b\n"
						+ "INNER JOIN " + dbSchema + ".coder2 AS b2 ON b2.coid = g.sg2b\n"
						+ "INNER JOIN " + dbSchema + ".coder3 AS b3 ON b3.coid = g.sg3b\n"
						+ "INNER JOIN " + dbSchema + ".coder4 AS b4 ON b4.coid = g.sg4b\n"
						+ "INNER JOIN " + dbSchema + ".coder1 AS m1 ON m1.coid = g.sg1m\n"
						+ "INNER JOIN " + dbSchema + ".coder2 AS m2 ON m2.coid = g.sg2m\n"
						+ "INNER JOIN " + dbSchema + ".coder3 AS m3 ON m3.coid = g.sg3m\n"
						+ "INNER JOIN " + dbSchema + ".coder4 AS m4 ON m4.coid = g.sg4m\n"
						+ "INNER JOIN " + dbSchema + ".coder1 AS r1 ON r1.coid = g.sg1r\n"
						+ "INNER JOIN " + dbSchema + ".coder2 AS r2 ON r2.coid = g.sg2r\n"
						+ "INNER JOIN " + dbSchema + ".coder3 AS r3 ON r3.coid = g.sg3r\n"
						+ "INNER JOIN " + dbSchema + ".coder4 AS r4 ON r4.coid = g.sg4r\n"
						+ "INNER JOIN " + dbSchema + ".procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN " + dbSchema + ".subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid";
				break;
			case 16:
				sql = "CREATE VIEW " + dbSchema + ".udvspecimens AS\n"
						+ "SELECT s.spid, s.caid, s.smid, s.spbl, s.spsl, s.spfr, s.sphe,\n"
						+ "s.spss, s.spih, s.spmo, s.spv5, s.spv1, s.spv2, s.spv3, s.spv4,\n"
						+ "s.spdc, c.cano, m.sgid, m.smnm, m.smdc, g.sgdc, g.poid, r.ponm\n"
						+ "FROM " + dbSchema + ".specimens AS s\n"
						+ "INNER JOIN " + dbSchema + ".cases AS c ON c.caid = s.caid\n"
						+ "INNER JOIN " + dbSchema + ".specimaster AS m ON m.smid = s.smid\n"
						+ "INNER JOIN " + dbSchema + ".specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN " + dbSchema + ".procedures AS r ON r.poid = g.poid";
				break;
			case 17:
				sql = "CREATE VIEW " + dbSchema + ".udvsubspecial AS\n"
						+ "SELECT b.sbid, b.syid, b.sbnm, b.sbdc, y.syfl, y.syld, y.sysp, y.synm\n"
						+ "FROM " + dbSchema + ".subspecial AS b\n"
						+ "INNER JOIN " + dbSchema + ".specialties AS y ON y.syid = b.syid";
				break;
			case 18:
				if (dbID == DB_MARIA) {
					sql = "CREATE VIEW " + dbSchema + ".udvcasesta AS\n"
							+ "SELECT faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "date_part('year', fned) AS fnyear, date_part('month', fned) AS fnmonth,\n"
							+ "COUNT(*) as CASES, SUM(CAST(grta AS INT)) AS grta,\n"
							+ "SUM(CAST(emta AS INT)) AS emta, SUM(CAST(mita AS INT)) AS mita,\n"
							+ "SUM(CAST(rota AS INT)) AS rota, SUM(CAST(fnta AS INT)) AS fnta\n"
							+ "FROM " + dbSchema + ".udvcases\n"
							+ "GROUP BY faid, syid, sbid, poid, fanm, synm, sbnm, ponm, fnyear, fnmonth";
				} else if (dbID == DB_MSSQL) {
					sql = "CREATE VIEW " + dbSchema + ".udvcasesta AS\n"
							+ "SELECT faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "DATEPART(year, fned) AS fnyear, DATEPART(month, fned) AS fnmonth,\n"
							+ "COUNT(*) as CASES, SUM(CAST(grta AS INT)) AS grta,\n"
							+ "SUM(CAST(emta AS INT)) AS emta, SUM(CAST(mita AS INT)) AS mita,\n"
							+ "SUM(CAST(rota AS INT)) AS rota, SUM(CAST(fnta AS INT)) AS fnta\n"
							+ "FROM " + dbSchema + ".udvcases\n"
							+ "GROUP BY faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "DATEPART(year, fned), DATEPART(month, fned)";
				} else if (dbID == DB_POSTG) {
					sql = "CREATE VIEW " + dbSchema + ".udvcasesta AS\n" + 
							"SELECT faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n" + 
							"EXTRACT(YEAR FROM fned) AS fnyear, EXTRACT(MONTH FROM fned) AS fnmonth,\n" + 
							"COUNT(*) as CASES, SUM(CAST(grta AS INT)) AS grta,\n" + 
							"SUM(CAST(emta AS INT)) AS emta, SUM(CAST(mita AS INT)) AS mita,\n" + 
							"SUM(CAST(rota AS INT)) AS rota, SUM(CAST(fnta AS INT)) AS fnta\n" + 
							"FROM " + dbSchema + ".udvcases\n" + 
							"GROUP BY faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n" + 
							"EXTRACT(YEAR FROM fned), EXTRACT(MONTH FROM fned)";
				} else {
					sql = "CREATE VIEW " + dbSchema + ".udvcasesta AS\n"
							+ "SELECT faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "YEAR(fned) AS fnyear, MONTH(fned) AS fnmonth,\n"
							+ "COUNT(*) as CASES, SUM(CAST(grta AS INT)) AS grta,\n"
							+ "SUM(CAST(emta AS INT)) AS emta, SUM(CAST(mita AS INT)) AS mita,\n"
							+ "SUM(CAST(rota AS INT)) AS rota, SUM(CAST(fnta AS INT)) AS fnta\n"
							+ "FROM " + dbSchema + ".udvcases\n"
							+ "GROUP BY faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "YEAR(fned), MONTH(fned)";
				}
				break;
			default:
				sql = "CREATE VIEW " + dbSchema + ".udvworkdaylast AS\n" + "SELECT wdid, wdno, wdtp, wddt\n"
						+ "FROM " + dbSchema + ".workdays\n"
						+ "WHERE (wddt = (SELECT MAX(wddt) FROM " + dbSchema + ".workdays))";
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

	private boolean getLogin() {
		LCrypto crypto = new LCrypto(appDir);
		String[] data = crypto.getData();
		if (data != null) {
			if (data.length == 6) {
				dbArch = data[0].toUpperCase();
				dbHost = data[1];
				dbPort = data[2];
				dbUser = data[4];
				dbPass = data[5];
				if (dbArch.equals("DERBY")) {
					dbID = DB_DERBY;
				} else if (dbArch.equals("MSSQL")) {
					dbID = DB_MSSQL;
				} else if (dbArch.equals("MARIADB")) {
					dbID = DB_MARIA;
				} else if (dbArch.equals("POSTGRES")) {
					dbID = DB_POSTG;
				} else {
					log(LConstants.ERROR_BINARY_FILE, "PJSetup", "Invalid application binary file");
				}
				return true;
			}
		}
		log(LConstants.ERROR_NONE, "PJSetup", "No or invalid application binary file!");
		return false;
	}

	private void initialize(String[] args) {
		try {
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
			System.out.println("Creating directory (if not exists) in: " + appDir);
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
			System.out.print("Install the database (y,n)? ");
			input = br.readLine();
			if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
				System.out.print("Select installation type (1 = Desktop, 2 = Client, 3 = Server): ");
				input = br.readLine();
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
					createDB();
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
					createDB();
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
					createDB();
					break;
				default:
					dbArch = "DERBY";
					dbHost = "localhost";
					dbPort = "2313";
					createDB();
				}
			} else {
				if (!getLogin()) {
					System.out.print("Create the autologin (y,n)? ");
					input = br.readLine();
					if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
						System.out.print("Select database architecture (1 = MariaDB, 2 = MSSql, 3 = PostgreSQL): ");
						input = br.readLine();
						if (input == null || input.trim().length() == 0) {
							log(LConstants.ERROR_NONE, "Exiting");
							System.exit(0);
						}
						dbID = 1 + Integer.parseInt(input);
						createLogin();
					} else {
						log(LConstants.ERROR_NONE, "Exiting");
						System.exit(0);
					}
				}
				if (errorID == LConstants.ERROR_NONE) {
					switch (dbID) {
					case DB_MARIA:
						setMaria();
						break;
					case DB_MSSQL:
						setMSSQL();
						break;
					case DB_POSTG:
						setPostgres();
						break;
					default:
						setDerby();
					}
				}
			}
			if (errorID == LConstants.ERROR_NONE) {
				System.out.print("Create the tables (y,n)? ");
				input = br.readLine();
				if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
					createTables();
				}
			}
			if (errorID == LConstants.ERROR_NONE) {
				System.out.print("Create the views (y,n)? ");
				input = br.readLine();
				if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
					createViews();
				}
			}
			if (errorID == LConstants.ERROR_NONE) {
				if (dbID == DB_MSSQL || dbID == DB_MARIA) {
					System.out.print("Create the procedures (y,n)? ");
					input = br.readLine();
					if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
						if (dbID == DB_MSSQL) {
							createProcMSSQL();
						} else {
							createProcMaria();
						}
					}
				}
			}
			if (errorID == LConstants.ERROR_NONE) {
				System.out.print("Create the autologin (y,n)? ");
				input = br.readLine();
				if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
					createLogin();
				}
			}
			if (errorID == LConstants.ERROR_NONE) {
				System.out.print("Load the data (y,n)? ");
				input = br.readLine();
				if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
					loadTables();
				}
			}
		} catch (SecurityException e) {
			log(LConstants.ERROR_UNEXPECTED, "PJSetup", e);
		} catch (NumberFormatException e) {
			log(LConstants.ERROR_VARIABLE, "PJSetup", e);
		} catch (IOException e) {
			log(LConstants.ERROR_VARIABLE, "PJSetup", e);
		} finally {
			close();
			System.out.print("Goodbye...");
			System.exit(errorID);
		}
	}

	private void loadCoders() {
		final String[] tables = { "coder1", "coder2", "coder3", "coder4" };
		try {
			for (int i = 0; i < 4; i++) {
				InputStream is = ClassLoader.getSystemClassLoader()
						.getResourceAsStream("db/" + tables[i] + ".txt");
				if (is != null) {
					InputStreamReader ir = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(ir);
					PreparedStatement pstm = connection.prepareStatement("INSERT INTO " + tables[i]
							+ " (coid, ruid, coqy, cov1, cov2, cov3, conm, codc) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
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
						.prepareStatement("INSERT INTO ordergroups (ogid, otid, ogc1, ogc2, ogc3, "
								+ "ogc4, ogc5, ognm, ogdc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
						.prepareStatement("INSERT INTO ordertypes (otid, otnm) VALUES (?, ?)");
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
						.prepareStatement("INSERT INTO procedures (poid, ponm, podc) VALUES (?, ?, ?)");
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
						.prepareStatement("INSERT INTO rules (ruid, runm, rudc) VALUES (?, ?, ?)");
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
				PreparedStatement pstm = connection.prepareStatement("INSERT INTO setup (stid, stva) VALUES (?, ?)");
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
						"INSERT INTO specialties (syid, syfl, syld, sysp, synm) VALUES (?, ?, ?, ?, ?)");
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
						.prepareStatement("INSERT INTO specigroups (sgid, sbid, poid, sg1b, sg1m, "
								+ "sg1r, sg2b, sg2m, sg2r, sg3b, sg3m, sg3r, sg4b, sg4m, sg4r, sgv5, sgln, sgdc) VALUES "
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
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO subspecial (sbid, syid, sbnm, sbdc) VALUES (?, ?, ?, ?)");
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
							pstm.setString(3, columns[2]);
							pstm.setString(4, columns[3]);
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
						"INSERT INTO turnaround (taid, grss, embd, micr, rout, finl, tanm) VALUES (?, ?, ?, ?, ?, ?, ?)");
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
						.prepareStatement("INSERT INTO workdays (wdid, wdno, wdtp, wddt) VALUES (?, ?, ?, ?)");
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

	private void setDerby() {
		try {
			Properties p = System.getProperties();
			p.setProperty("derby.system.home", appDir);
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection("jdbc:derby:" + dbSchema + ";create=false;");
			stm = connection.createStatement();
			execute("SET SCHEMA " + dbSchema);
			dbName = "Derby";
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void setMaria() {
		try {
			String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbSchema
					+ "?autoReconnect=true&useUnicode=true" + "&useLegacyDatetimeCode=false&serverTimezone=UTC";
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection(url, dbUser, dbPass);
			stm = connection.createStatement();
			execute("USE " + dbSchema);
			dbName = "Maria";
		} catch (IllegalArgumentException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SecurityException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void setMSSQL() {
		SQLServerDataSource ds = new SQLServerDataSource();
		try {
			ds.setIntegratedSecurity(false);
			ds.setLoginTimeout(2);
			ds.setPortNumber(Integer.parseInt(dbPort));
			ds.setServerName(dbHost);
			ds.setDatabaseName(dbSchema);
			ds.setUser(dbUser);
			ds.setPassword(dbPass);
			connection = ds.getConnection();
			stm = connection.createStatement();
			execute("USE " + dbSchema);
			dbName = "MSSQL";
		} catch (SQLServerException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}

	private void setPostgres() {
		try {
			String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbSchema;
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection(url, dbUser, dbPass);
			stm = connection.createStatement();
			execute("SET search_path TO " + dbSchema);
			dbName = "Postgres";
		} catch (IllegalArgumentException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SecurityException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LConstants.ERROR_SQL, dbName, e);
		}
	}
}