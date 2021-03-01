package ca.powerj.app;
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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibCrypto;

public class PJInstall {
	private final byte DB_DERBY = 1;
	private final byte DB_MARIA = 2;
	private final byte DB_MSSQL = 3;
	private final byte DB_POSTG = 4;
	private byte errorID = LibConstants.ERROR_NONE;
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

	public PJInstall(String[] args) {
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
				if (errorID == LibConstants.ERROR_NONE) {
					execute("USE " + dbSchema);
				}
				break;
			case DB_POSTG:
				// Cannot create database and schema in the same connection
				// Must disconnect, then reconnect
				execute("CREATE DATABASE powerj");
				connection.close();
				if (errorID == LibConstants.ERROR_NONE) {
					url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/powerj?user=" + dbUser + "&password="
							+ dbPass;
					connection = DriverManager.getConnection(url);
					stm = connection.createStatement();
				}
				if (errorID == LibConstants.ERROR_NONE) {
					execute("CREATE SCHEMA " + dbSchema);
				}
				if (errorID == LibConstants.ERROR_NONE) {
					execute("SET search_path TO " + dbSchema);
				}
				break;
			case DB_MSSQL:
				execute("CREATE DATABASE powerj");
				if (errorID == LibConstants.ERROR_NONE) {
					execute("USE powerj");
				}
				if (errorID == LibConstants.ERROR_NONE) {
					execute("CREATE SCHEMA " + dbSchema);
				}
				break;
			default:
				execute("CREATE SCHEMA " + dbSchema);
				if (errorID == LibConstants.ERROR_NONE) {
					execute("SET SCHEMA " + dbSchema);
				}
			}
			log(LibConstants.ERROR_NONE, "Created database.");
			Thread.sleep(LibConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (ClassNotFoundException e) {
			log(LibConstants.ERROR_UNEXPECTED, dbName, e);
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
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.println("CREATE ROLE " + sysUserClient + " LOGIN PASSWORD '" + sysPassClient
						+ "' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;");
			}
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.println("GRANT USAGE ON SCHEMA " + dbSchema + " TO " + sysUserClient + ";");
				System.out.println("GRANT SELECT, INSERT, DELETE, UPDATE, EXECUTE ON ALL TABLES IN SCHEMA " + dbSchema
						+ " TO " + sysUserClient + ";");
			}
		} else if (dbID == DB_MARIA) {
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.println("CREATE USER " + sysUserClient + "@'%' IDENTIFIED BY '" + sysPassClient + "';");
			}
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.println("GRANT SELECT, INSERT, DELETE, UPDATE, EXECUTE ON " + dbSchema + ".* TO "
						+ sysUserClient + "@'%';");
			}
			System.out.println("FLUSH PRIVILEGES;");
		} else {
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.println("CREATE USER " + sysUserClient + " WITH PASSWORD = '" + sysPassClient
						+ "', DEFAULT_SCHEMA = " + dbSchema);
			}
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.println("exec sp_addrolemember db_datareader, " + sysUserClient);
			}
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.println("exec sp_addrolemember db_datawriter, " + sysUserClient);
			}
			if (errorID == LibConstants.ERROR_NONE) {
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
			log(LibConstants.ERROR_NONE, "Exiting");
			System.exit(0);
		}
		LibCrypto crypto = new LibCrypto(appDir);
		String[] data = { dbArch, dbHost, dbPort, dbSchema, sysUserClient, sysPassClient };
		if (crypto.setData(data)) {
			log(LibConstants.ERROR_NONE, "Users binary file completed successfully.");
		} else {
			log(LibConstants.ERROR_IO, "Users binary file failed!");
		}
	}

	private void createProcedures() {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 57; i++) {
			switch (i) {
			case 0:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpaccessions()\nBEGIN\n"
						+ "SELECT * FROM <pjschema>.udvaccessions\nORDER BY acnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpaccessions AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.udvaccessions ORDER BY acnm;\nEND";
				}
				break;
			case 1:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpadditionals(param1 INT)\nBEGIN\n"
							+ "SELECT prid, adcd, adv5, adv1, adv2, adv3, adv4, addt, prnm, prls, prfr, cano\n"
							+ "FROM <pjschema>.udvadditionals\nWHERE caid = param1\nORDER BY addt;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpadditionals @param1 INT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT prid, adcd, adv5, adv1, adv2, adv3, adv4, addt, prnm, prls, prfr, cano\n"
							+ "FROM <pjschema>.udvadditionals WHERE caid = @param1 ORDER BY addt;\nEND";
				}
				break;
			case 2:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpaddlast(param1 SMALLINT)\nBEGIN\n"
							+ "SELECT MAX(addt) AS addt\nFROM <pjschema>.additionals\nWHERE adcd = param1;\n"
							+ "END";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpaddlast @param1 SMALLINT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT MAX(addt) AS addt FROM <pjschema>.additionals WHERE adcd = @param1;\nEND";
				}
				break;
			case 3:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpaddspg(param1 DATETIME, param2 DATETIME)\nBEGIN\n"
							+ "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc,\n"
							+ "f.fanm, COUNT(a.caid) AS qty, SUM(a.adv1) AS adv1, SUM(a.adv2) AS adv2,\n"
							+ "SUM(a.adv3) AS adv3, SUM(a.adv4) AS adv4, SUM(a.adv5) AS adv5\n"
							+ "FROM <pjschema>.additionals AS a\nINNER JOIN <pjschema>.cases AS c ON c.caid = a.caid\n"
							+ "INNER JOIN <pjschema>.facilities AS f ON f.faid = c.faid\n"
							+ "INNER JOIN <pjschema>.specimaster AS m ON m.smid = c.smid\n"
							+ "INNER JOIN <pjschema>.specigroups AS g ON g.sgid = m.sgid\n"
							+ "INNER JOIN <pjschema>.procedures r ON r.poid = g.poid\n"
							+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = g.sbid\n"
							+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid\n"
							+ "WHERE c.fned BETWEEN param1 AND param2\n"
							+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\n"
							+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpaddspg @param1 DATETIME, @param2 DATETIME AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\n"
							+ "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc,\n"
							+ "f.fanm, COUNT(a.caid) AS qty, SUM(a.adv1) AS adv1, SUM(a.adv2) AS adv2,\n"
							+ "SUM(a.adv3) AS adv3, SUM(a.adv4) AS adv4, SUM(a.adv5) AS adv5\n"
							+ "FROM <pjschema>.additionals AS a\nINNER JOIN <pjschema>.cases AS c ON c.caid = a.caid\n"
							+ "INNER JOIN <pjschema>.facilities AS f ON f.faid = c.faid\n"
							+ "INNER JOIN <pjschema>.specimaster AS m ON m.smid = c.smid\n"
							+ "INNER JOIN <pjschema>.specigroups AS g ON g.sgid = m.sgid\n"
							+ "INNER JOIN <pjschema>.procedures r ON r.poid = g.poid\n"
							+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = g.sbid\n"
							+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid\n"
							+ "WHERE c.fned BETWEEN @param1 AND @param2\n"
							+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\n"
							+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid;\nEND";
				}
				break;
			case 4:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpaddsum(param1 DATETIME, param2 DATETIME)\nBEGIN\n"
							+ "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(caid) AS adca,\n"
							+ "SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2, SUM(adv3) AS adv3, SUM(adv4) AS adv4\n"
							+ "FROM <pjschema>.udvadditionals\nWHERE addt BETWEEN param1 AND param2\n"
							+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr\n"
							+ "ORDER BY faid, syid, sbid, poid, prid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpaddsum @param1 DATETIME, @param2 DATETIME AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\n"
							+ "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(caid) AS adca,\n"
							+ "SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2, SUM(adv3) AS adv3, SUM(adv4) AS adv4\n"
							+ "FROM <pjschema>.udvadditionals WHERE addt BETWEEN @param1 AND @param2\n"
							+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr\n"
							+ "ORDER BY faid, syid, sbid, poid, prid;\nEND";
				}
				break;
			case 5:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpaddyear(param1 DATETIME, param2 DATETIME)\nBEGIN\n"
							+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, date_part('year', addt) as yearid,\n"
							+ "COUNT(caid) AS adca, SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2,\n"
							+ "SUM(adv3) AS adv3, SUM(adv4) AS adv4\nFROM <pjschema>.udvadditionals\n"
							+ "WHERE addt BETWEEN param1 AND param2\n"
							+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, date_part('year', addt)\n"
							+ "ORDER BY faid, syid, sbid, poid, sgid, yearid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpaddyear @param1 DATETIME, @param2 DATETIME AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\n"
							+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, DATEPART(YEAR, addt) as yearid,\n"
							+ "COUNT(caid) AS adca, SUM(CAST(adv5 as INT)) AS adv5, SUM(adv1) AS adv1, SUM(adv2) AS adv2,\n"
							+ "SUM(adv3) AS adv3, SUM(adv4) AS adv4\nFROM <pjschema>.udvadditionals\n"
							+ "WHERE addt BETWEEN @param1 AND @param2\n"
							+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, DATEPART(YEAR, addt) \n"
							+ "ORDER BY faid, syid, sbid, poid, sgid, yearid;\nEND";
				}
				break;
			case 6:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcoder1()\nBEGIN\n"
							+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc\nFROM <pjschema>.coder1\n"
							+ "ORDER BY conm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcoder1 AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder1 ORDER BY conm;\n"
							+ "END";
				}
				break;
			case 7:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcoder2()\nBEGIN\n"
							+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder2 ORDER BY conm;\n"
							+ "END";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcoder2 AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder2 ORDER BY conm;\n"
							+ "END";
				}
				break;
			case 8:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcoder3()\nBEGIN\n"
							+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder3 ORDER BY conm;\n"
							+ "END";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcoder3 AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder3 ORDER BY conm;\n"
							+ "END";
				}
				break;
			case 9:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcoder4()\nBEGIN\n"
							+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder4 ORDER BY conm;\n"
							+ "END";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcoder4 AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT coid, ruid, coqy, cov1, cov2, cov3, conm, codc FROM <pjschema>.coder4 ORDER BY conm;\n"
							+ "END";
				}
				break;
			case 10:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcmt(param1 INT)\nBEGIN\n"
							+ "SELECT com1, com2, com3, com4 FROM <pjschema>.comments WHERE caid = param1;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcmt @param1 INT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT com1, com2, com3, com4 FROM <pjschema>.comments WHERE caid = @param1;\nEND";
				}
				break;
			case 11:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcseid(param1 CHAR(12))\nBEGIN\n"
							+ "SELECT caid FROM <pjschema>.cases WHERE cano = param1;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcseid @param1 CHAR(12) AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT caid FROM <pjschema>.cases WHERE cano = @param1;\nEND";
				}
				break;
			case 12:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcseno(param1 INT)\nBEGIN\n"
							+ "SELECT cano FROM <pjschema>.cases WHERE caid = param1;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcseno @param1 INT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT cano FROM <pjschema>.cases WHERE caid = @param1;\nEND";
				}
				break;
			case 13:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcsespe(param1 INT)\nBEGIN\n"
							+ "SELECT c.smid, c.fned, c.cano, s.spid\nFROM <pjschema>.cases AS c\n"
							+ "INNER JOIN <pjschema>.specimens AS s ON s.caid = c.caid AND s.smid = c.smid\n"
							+ "WHERE c.caid = param1;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcsespe @param1 INT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT c.smid, c.fned, c.cano, s.spid FROM <pjschema>.cases AS c\n"
							+ "INNER JOIN <pjschema>.specimens AS s ON s.caid = c.caid AND s.smid = c.smid\n"
							+ "WHERE c.caid = @param1;\nEND";
				}
				break;
			case 14:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcsesum(param1 DATETIME, param2 DATETIME)\nBEGIN\n"
							+ "SELECT faid, syid, sbid, poid, fnid, fanm, synm, sbnm, sbdc, ponm, fnnm, fnls, fnfr, COUNT(caid) AS caca,\n"
							+ "SUM(CAST(casp as INT)) AS casp, SUM(CAST(cabl as INT)) AS cabl, SUM(CAST(casl as INT)) AS casl, SUM(CAST(cahe as INT)) AS cahe,\n"
							+ "SUM(CAST(cass as INT)) AS cass, SUM(CAST(caih as INT)) AS caih, SUM(CAST(camo as INT)) AS camo, SUM(CAST(cafs as INT)) AS cafs,\n"
							+ "SUM(CAST(casy as INT)) AS casy, SUM(CAST(grta as INT)) AS grta, SUM(CAST(emta as INT)) AS emta, SUM(CAST(mita as INT)) AS mita,\n"
							+ "SUM(CAST(rota as INT)) AS rota, SUM(CAST(fnta as INT)) AS fnta, SUM(CAST(cav5 as INT)) AS cav5, SUM(cav1) AS cav1, SUM(cav2) AS cav2,\n"
							+ "SUM(cav3) AS cav3, SUM(cav4) AS cav4\nFROM <pjschema>.udvcases\n"
							+ "WHERE (fned BETWEEN param1 AND param2)\n"
							+ "GROUP BY faid, syid, sbid, poid, fnid, fanm, synm, sbnm, sbdc, ponm, fnnm, fnls, fnfr\n"
							+ "ORDER BY faid, syid, sbid, poid, fnid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcsesum @param1 DATETIME, @param2 DATETIME AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\n"
							+ "SELECT faid, syid, sbid, poid, fnid, fanm, synm, sbnm, sbdc, ponm, fnnm, fnls, fnfr, COUNT(caid) AS caca,\n"
							+ "SUM(CAST(casp as INT)) AS casp, SUM(CAST(cabl as INT)) AS cabl, SUM(CAST(casl as INT)) AS casl, SUM(CAST(cahe as INT)) AS cahe,\n"
							+ "SUM(CAST(cass as INT)) AS cass, SUM(CAST(caih as INT)) AS caih, SUM(CAST(camo as INT)) AS camo, SUM(CAST(cafs as INT)) AS cafs,\n"
							+ "SUM(CAST(casy as INT)) AS casy, SUM(CAST(grta as INT)) AS grta, SUM(CAST(emta as INT)) AS emta, SUM(CAST(mita as INT)) AS mita,\n"
							+ "SUM(CAST(rota as INT)) AS rota, SUM(CAST(fnta as INT)) AS fnta, SUM(CAST(cav5 as INT)) AS cav5, SUM(cav1) AS cav1, SUM(cav2) AS cav2,\n"
							+ "SUM(cav3) AS cav3, SUM(cav4) AS cav4\nFROM <pjschema>.udvcases\n"
							+ "WHERE (fned BETWEEN @param1 AND @param2)\n"
							+ "GROUP BY faid, syid, sbid, poid, fnid, fanm, synm, sbnm, sbdc, ponm, fnnm, fnls, fnfr\n"
							+ "ORDER BY faid, syid, sbid, poid, fnid;\nEND";
				}
				break;
			case 15:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udperrselect()\nBEGIN\n"
							+ "SELECT caid, erid, cano FROM <pjschema>.errors WHERE erid > 0 ORDER BY cano;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udperrselect AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT caid, erid, cano FROM <pjschema>.errors WHERE erid > 0 ORDER BY cano;\nEND";
				}
				break;
			case 16:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udperrcmt(param1 INT)\nBEGIN\n"
							+ "SELECT erdc FROM <pjschema>.errors WHERE caid = param1;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udperrcmt @param1 INT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT erdc FROM <pjschema>.errors WHERE caid = @param1;\nEND";
				}
				break;
			case 17:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udperrredo()\nBEGIN\n"
							+ "SELECT caid FROM <pjschema>.errors WHERE erid = 0 ORDER BY caid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udperrredo AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT caid FROM <pjschema>.errors WHERE erid = 0 ORDER BY caid;\nEND";
				}
				break;
			case 18:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpfacility()\nBEGIN\n"
							+ "SELECT faid, fafl, fald, fanm, fadc FROM <pjschema>.facilities ORDER BY fanm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpfacility AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT faid, fafl, fald, fanm, fadc FROM <pjschema>.facilities ORDER BY fanm;\nEND";
				}
				break;
			case 19:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpfrzsid(param1 INT)\nBEGIN\n"
							+ "SELECT prid, frbl, frsl, frv5, frv1, frv2, frv3, frv4, prnm, prls,\n"
							+ "spdc, smnm FROM <pjschema>.udvfrozens WHERE spid = param1;END";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpfrzsid @param1 INT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT prid, frbl, frsl, frv5, frv1, frv2, frv3, frv4, prnm, prls, spdc, smnm\n"
							+ "FROM <pjschema>.udvfrozens WHERE spid = @param1;END";
				}
				break;
			case 20:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpfrzspg(param1 DATETIME, param2 DATETIME)\nBEGIN\n"
							+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, COUNT(spid) AS frsp,\n"
							+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
							+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
							+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\nFROM <pjschema>.udvfrozens\n"
							+ "WHERE (aced BETWEEN param1 AND param2)\n"
							+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc\n"
							+ "ORDER BY faid, syid, sbid, poid, sgid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpfrzspg @param1 DATETIME, @param2 DATETIME AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\n"
							+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, COUNT(spid) AS frsp,\n"
							+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
							+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
							+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\nFROM <pjschema>.udvfrozens\n"
							+ "WHERE (aced BETWEEN @param1 AND @param2)\n"
							+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc\n"
							+ "ORDER BY faid, syid, sbid, poid, sgid;\nEND";
				}
				break;
			case 21:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpFrzSu5(param1 DATETIME, param2 DATETIME)\nBEGIN\n"
							+ "SELECT COUNT(*) AS QTY, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
							+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
							+ "FROM <pjschema>.udvfrozens WHERE aced BETWEEN param1 AND param2;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpFrzSu5 @param1 DATETIME, @param2 DATETIME AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\nSELECT COUNT(*) AS QTY, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
							+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
							+ "FROM <pjschema>.udvfrozens WHERE aced BETWEEN @param1 AND @param2;\nEND";
				}
			case 22:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpfrzsum(param1 DATETIME, param2 DATETIME)\nBEGIN\n"
							+ "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(spid) AS frsp,\n"
							+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
							+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
							+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\nFROM <pjschema>.udvfrozens\n"
							+ "WHERE (aced BETWEEN param1 AND param2)\n"
							+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr\n"
							+ "ORDER BY faid, syid, sbid, poid, prid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpfrzsum @param1 DATETIME, @param2 DATETIME AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\n"
							+ "SELECT faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr, COUNT(spid) AS frsp,\n"
							+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
							+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
							+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\n"
							+ "FROM udvfrozens WHERE (aced BETWEEN @param1 AND @param2)\n"
							+ "GROUP BY faid, syid, sbid, poid, prid, fanm, synm, sbnm, ponm, prnm, prls, prfr\n"
							+ "ORDER BY faid, syid, sbid, poid, prid;\nEND";
				}
				break;
			case 23:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpfrzyear(param1 DATETIME, param2 DATETIME)\nBEGIN\n"
							+ "SELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc,\n"
							+ "date_part('year', aced) as yearid, COUNT(spid) AS frsp,\n"
							+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
							+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
							+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\nFROM <pjschema>.udvfrozens\n"
							+ "WHERE (aced BETWEEN param1 AND param2)\n"
							+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, date_part('year', aced)\n"
							+ "ORDER BY faid, syid, sbid, poid, sgid, yearid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpfrzyear @param1 DATETIME, @param2 DATETIME AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\nSELECT faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc,\n"
							+ "DATEPART(YEAR, aced) as yearid, COUNT(spid) AS frsp,\n"
							+ "SUM(CAST(frbl as INT)) AS frbl, SUM(CAST(frsl as INT)) AS frsl,\n"
							+ "SUM(CAST(frv5 as INT)) AS frv5, SUM(frv1) AS frv1, SUM(frv2) AS frv2,\n"
							+ "SUM(frv3) AS frv3, SUM(frv4) AS frv4\nFROM <pjschema>.udvfrozens\n"
							+ "WHERE aced BETWEEN @param1 AND @param2 \n"
							+ "GROUP BY faid, syid, sbid, poid, sgid, fanm, synm, sbnm, ponm, sgdc, DATEPART(YEAR, aced) \n"
							+ "ORDER BY faid, syid, sbid, poid, sgid, yearid;\nEND";
				}
				break;
			case 24:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udporder(param1 INT)\nBEGIN\n"
							+ "SELECT orqy, orv1, orv2, orv3, orv4, ognm\nFROM <pjschema>.udvorders\n"
							+ "WHERE spid = param1\nORDER BY ognm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udporder @param1 INT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT orqy, orv1, orv2, orv3, orv4, ognm FROM <pjschema>.udvorders WHERE spid = @param1 ORDER BY ognm;\n"
							+ "END";
				}
				break;
			case 25:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpordergroup()\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.udvordergroups ORDER BY ognm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpordergroup AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.udvordergroups ORDER BY ognm;\nEND";
				}
				break;
			case 26:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpordermaster()\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.udvordermaster ORDER BY omnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpordermaster AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.udvordermaster ORDER BY omnm;\nEND";
				}
				break;
			case 27:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udppending()\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.udvpending ORDER BY pnid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udppending AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.udvpending ORDER BY pnid;\nEND";
				}
				break;
			case 28:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udppendingrouted(param1 DATETIME, param2 DATETIME)\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.udvpending\nWHERE (roed BETWEEN param1 AND param2)\n"
							+ "ORDER BY pnid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udppendingrouted @param1 DATETIME, @param2 DATETIME AS \n"
							+ "BEGIN\nSET NOCOUNT ON;\nSELECT * FROM <pjschema>.udvpending\n"
							+ "WHERE (roed BETWEEN @param1 AND @param2)\nORDER BY pnid;\nEND";
				}
				break;
			case 29:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpprocedure()\nBEGIN\n"
							+ "SELECT poid, ponm, podc FROM <pjschema>.procedures ORDER BY ponm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpprocedure AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT poid, ponm, podc FROM <pjschema>.procedures ORDER BY ponm;\nEND";
				}
				break;
			case 30:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpprsname()\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.persons ORDER BY prnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpprsname AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM persons ORDER BY prnm;\nEND";
				}
				break;
			case 31:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpprsid(param1 SMALLINT)\nBEGIN\n"
							+ "SELECT prvl FROM <pjschema>.persons WHERE prid = param1;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpprsid @param1 SMALLINT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT prvl FROM <pjschema>.persons WHERE prid = @param1;\nEND";
				}
				break;
			case 32:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udprule()\nBEGIN\n"
							+ "SELECT ruid, runm, rudc FROM <pjschema>.rules ORDER BY ruid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udprule AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT ruid, runm, rudc FROM <pjschema>.rules ORDER BY ruid;\nEND";
				}
				break;
			case 33:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpschedserv(param1 DATE, param2 DATE)\nBEGIN\n"
							+ "SELECT wdid, srid, prid, prnm, srnm\nFROM <pjschema>.udvschedules\n"
							+ "WHERE wddt BETWEEN param1 AND param2 \nORDER BY srnm, wdid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpschedserv @param1 DATE, @param2 DATE AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\nSELECT wdid, srid, prid, prnm, srnm\n"
							+ "FROM <pjschema>.udvschedules\nWHERE wddt BETWEEN @param1 AND @param2 \n"
							+ "ORDER BY srnm, wdid;\nEND";
				}
				break;
			case 34:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpschedstaff(param1 DATE, param2 DATE)\nBEGIN\n"
							+ "SELECT wdid, srid, prid, prnm, srnm\nFROM <pjschema>.udvschedules\n"
							+ "WHERE wddt BETWEEN param1 AND param2 \nORDER BY prnm, wdid, srnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpschedstaff @param1 DATE, @param2 DATE AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\nSELECT wdid, srid, prid, prnm, srnm\n"
							+ "FROM <pjschema>.udvschedules\nWHERE wddt BETWEEN @param1 AND @param2 \n"
							+ "ORDER BY prnm, wdid, srnm;\nEND";
				}
				break;
			case 35:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpschedsum(param1 DATE, param2 DATE)\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.udvschedules\nWHERE wddt BETWEEN param1 AND param2 \n"
							+ "ORDER BY faid, prid, wdid, srid\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpschedsum @param1 DATE, @param2 DATE AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\nSELECT * FROM <pjschema>.udvschedules\n"
							+ "WHERE wddt BETWEEN @param1 AND @param2 \nORDER BY faid, prid, wdid, srid\nEND";
				}
				break;
			case 36:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpspecimens(param1 INT)\nBEGIN\n"
							+ "SELECT spid, smid, spbl, spsl, spfr, sphe, spss, spih,\n"
							+ "spmo, spv5, spv1, spv2, spv3, spv4, spdc, smnm, smdc, ponm\n"
							+ "FROM <pjschema>.udvspecimens\nWHERE caid = param1\nORDER BY spid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpspecimens @param1 INT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT spid, smid, spbl, spsl, spfr, sphe, spss, spih, spmo,\n"
							+ "spv5, spv1, spv2, spv3, spv4, spdc, smnm, smdc, ponm\n"
							+ "FROM <pjschema>.udvspecimens WHERE caid = @param1 ORDER BY spid;\nEND";
				}
				break;
			case 37:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpspecgroup()\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.udvspecigroups ORDER BY sgdc;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpspecgroup AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.udvspecigroups ORDER BY sgdc;\nEND";
				}
				break;
			case 38:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpspecyear(param1 DATE, param2 DATE)\nBEGIN\n"
							+ "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm,\n"
							+ "date_part('year', c.fned) as yearid, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
							+ "SUM(s.spv1) AS spv1, SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4,\n"
							+ "SUM(s.spv5) AS spv5 FROM <pjschema>.specigroups g\n"
							+ "INNER JOIN <pjschema>.specimaster m ON g.sgid = m.sgid\n"
							+ "INNER JOIN <pjschema>.specimens s ON m.smid = s.smid\n"
							+ "INNER JOIN <pjschema>.cases c ON c.caid = s.caid\n"
							+ "INNER JOIN <pjschema>.procedures r ON r.poid = g.poid\n"
							+ "INNER JOIN <pjschema>.subspecial b ON b.sbid = g.sbid\n"
							+ "INNER JOIN <pjschema>.specialties y ON y.syid = b.syid\n"
							+ "INNER JOIN <pjschema>.facilities f ON f.faid = c.faid WHERE c.fned BETWEEN param1 AND param2 \n"
							+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, date_part('year', c.fned)\n"
							+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpspecyear @param1 DATE, @param2 DATE \nBEGIN\n"
							+ "SET NOCOUNT ON;\n"
							+ "SELECT b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm,\n"
							+ "DATEPART(YEAR, c.fned) as yearid, COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
							+ "SUM(s.spv1) AS spv1, SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4,\n"
							+ "SUM(s.spv5) AS spv5 FROM <pjschema>.specigroups g\n"
							+ "INNER JOIN <pjschema>.specimaster m ON g.sgid = m.sgid\n"
							+ "INNER JOIN <pjschema>.specimens s ON m.smid = s.smid\n"
							+ "INNER JOIN <pjschema>.cases c ON c.caid = s.caid\n"
							+ "INNER JOIN <pjschema>.procedures r ON r.poid = g.poid\n"
							+ "INNER JOIN <pjschema>.subspecial b ON b.sbid = g.sbid\n"
							+ "INNER JOIN <pjschema>.specialties y ON y.syid = b.syid\n"
							+ "INNER JOIN <pjschema>.facilities f ON f.faid = c.faid WHERE c.fned BETWEEN @param1 AND @param2 \n"
							+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm, DATEPART(YEAR, c.fned)\n"
							+ "ORDER BY b.syid, g.sbid, g.poid, g.sgid, c.faid\nEND";
				}
				break;
			case 39:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpspecmaster()\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.udvspecimaster ORDER BY smnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpspecmaster AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.udvspecimaster ORDER BY smnm;\nEND";
				}
				break;
			case 40:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpspecsu5(param1 DATE, param2 DATE)\nBEGIN\n"
							+ "SELECT g.sgid, COUNT(s.spid) AS qty, SUM(s.spv1) AS spv1,\n"
							+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4\n"
							+ "FROM specigroups g INNER JOIN <pjschema>.specimaster m ON g.sgid = m.sgid\n"
							+ "INNER JOIN <pjschema>.specimens s ON m.smid = s.smid\n"
							+ "INNER JOIN <pjschema>.cases c ON c.caid = s.caid\n"
							+ "WHERE c.fned BETWEEN param1 AND param2\nGROUP BY g.sgidEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpspecsu5 @param1 DATE, @param2 DATE AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\nSELECT g.sgid, COUNT(s.spid) AS qty, SUM(s.spv1) AS spv1,\n"
							+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4\n"
							+ "FROM <pjschema>.specigroups g\n"
							+ "INNER JOIN <pjschema>.specimaster m ON g.sgid = m.sgid \n"
							+ "INNER JOIN <pjschema>.specimens s ON m.smid = s.smid\n"
							+ "INNER JOIN <pjschema>.cases c ON c.caid = s.caid\n"
							+ "WHERE c.fned BETWEEN @param1 AND @param2\nGROUP BY g.sgid\nEND";
				}
				break;
			case 41:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpspecsum(param1 DATE, param2 DATE)\nBEGIN\n"
							+ "SELECT b.syid, g.sbid, g.sgid, c.faid, y.synm, b.sbnm, g.sgdc, f.fanm,\n"
							+ "COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
							+ "SUM(s.sphe) AS sphe, SUM(s.spss) AS spss, SUM(s.spih) AS spih, SUM(s.spv1) AS spv1,\n"
							+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, SUM(s.spv5) AS spv5\n"
							+ "FROM <pjschema>.specigroups g\nINNER JOIN <pjschema>.specimaster m ON g.sgid = m.sgid\n"
							+ "INNER JOIN <pjschema>.specimens s ON m.smid = s.smid\n"
							+ "INNER JOIN <pjschema>.cases c ON c.caid = s.caid\n"
							+ "INNER JOIN <pjschema>.procedures r ON r.poid = g.poid\n"
							+ "INNER JOIN <pjschema>.subspecial b ON b.sbid = g.sbid\n"
							+ "INNER JOIN <pjschema>.specialties y ON y.syid = b.syid\n"
							+ "INNER JOIN <pjschema>.facilities f ON f.faid = c.faid\n"
							+ "WHERE c.fned BETWEEN param1 AND param2\n"
							+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\n"
							+ "END";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpspecsum @param1 DATE, @param2 DATE AS \nBEGIN\n"
							+ "SET NOCOUNT ON;\n"
							+ "SELECT b.syid, g.sbid, g.sgid, c.faid, y.synm, b.sbnm, g.sgdc, f.fanm,\n"
							+ "COUNT(s.spid) AS qty, SUM(s.spbl) AS spbl, SUM(s.spsl) AS spsl,\n"
							+ "SUM(s.sphe) AS sphe, SUM(s.spss) AS spss, SUM(s.spih) AS spih, SUM(s.spv1) AS spv1,\n"
							+ "SUM(s.spv2) AS spv2, SUM(s.spv3) AS spv3, SUM(s.spv4) AS spv4, SUM(s.spv5) AS spv5\n"
							+ "FROM <pjschema>.specigroups g\nINNER JOIN <pjschema>.specimaster m ON g.sgid = m.sgid\n"
							+ "INNER JOIN <pjschema>.specimens s ON m.smid = s.smid\n"
							+ "INNER JOIN <pjschema>.cases c ON c.caid = s.caid\n"
							+ "INNER JOIN <pjschema>.procedures r ON r.poid = g.poid\n"
							+ "INNER JOIN <pjschema>.subspecial b ON b.sbid = g.sbid\n"
							+ "INNER JOIN <pjschema>.specialties y ON y.syid = b.syid\n"
							+ "INNER JOIN <pjschema>.facilities f ON f.faid = c.faid\n"
							+ "WHERE c.fned BETWEEN @param1 AND @param2\n"
							+ "GROUP BY b.syid, g.sbid, g.poid, g.sgid, c.faid, y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\n"
							+ "ORDER BY y.synm, b.sbnm, r.ponm, g.sgdc, f.fanm\nEND";
				}
				break;
			case 42:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpspecialty()\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.specialties ORDER BY synm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpspecialty AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.specialties ORDER BY synm;\nEND";
				}
				break;
			case 43:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpservice()\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.udvservices ORDER BY srnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpservice AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.udvservices ORDER BY srnm;\nEND";
				}
				break;
			case 44:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpsetup()\nBEGIN\n"
							+ "SELECT stid, stva FROM <pjschema>.setup ORDER BY stid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpsetup AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT stid, stva FROM <pjschema>.setup ORDER BY stid;\nEND";
				}
				break;
			case 45:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpstpid(param1 SMALLINT)\nBEGIN\n"
							+ "SELECT stva FROM <pjschema>.setup WHERE stid = param1;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpstpid @param1 SMALLINT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT stva FROM <pjschema>.setup WHERE stid = @param1;\nEND";
				}
				break;
			case 46:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpsubspecial()\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.udvsubspecial ORDER BY sbnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpsubspecial AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.udvsubspecial ORDER BY sbnm;\nEND";
				}
				break;
			case 47:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpturnaround()\nBEGIN\n"
							+ "SELECT taid, grss, embd, micr, rout, finl, tanm\n"
							+ "FROM <pjschema>.turnaround ORDER BY tanm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpturnaround AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT taid, grss, embd, micr, rout, finl, tanm\n"
							+ "FROM <pjschema>.turnaround ORDER BY tanm;\nEND";
				}
				break;
			case 48:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpwdy(param1 DATE)\nBEGIN\n"
							+ "SELECT wdid, wdno, wdtp, wddt FROM <pjschema>.workdays\n"
							+ "WHERE wddt >= param1 ORDER BY wddt;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpwdy @param1 DATE AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT wdid, wdno, wdtp, wddt FROM <pjschema>.workdays\n"
							+ "WHERE wddt >= @param1 ORDER BY wddt;\nEND";
				}
				break;
			case 49:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpwdydte(param1 DATE)\nBEGIN\n"
							+ "SELECT wdno FROM <pjschema>.workdays WHERE wddt = param1;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpwdydte @param1 DATE AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT wdno FROM <pjschema>.workdays WHERE wddt = @param1;\nEND";
				}
				break;
			case 50:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpwdynxt(param1 DATE)\nBEGIN\n"
							+ "SELECT MIN(wddt) AS wddt FROM <pjschema>.workdays\n"
							+ "WHERE wddt > param1 AND wdtp = 'D';\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpwdynxt @param1 DATE AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT MIN(wddt) AS wddt FROM <pjschema>.workdays\n"
							+ "WHERE wddt > @param1 AND wdtp = 'D';\nEND";
				}
				break;
			case 51:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udptissues(param1 SMALLINT, "
							+ "param2 SMALLINT, param3 SMALLINT) AS\nBEGIN\n"
							+ "SELECT t.tiid, t.poid, t.ticx, t.tinm, t.tidc,\n"
							+ "t.podc, s.tidc AS tidc2, s.podc AS podc2\n"
							+ "FROM <pjschema>.tissues AS t ON t.tiid = p.tiid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS s\n"
							+ "ON s.tiid = p.tiid AND s.prid = param1\n"
							+ "WHERE t.syid = param2 AND t.onid = param3;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udptissues @param1 SMALLINT, "
							+ "@param2 SMALLINT, @param3 SMALLINT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT t.tiid, t.poid, t.ticx, t.tinm, t.tidc,\n"
							+ "t.podc, s.tidc AS tidc2, s.podc AS podc2\n"
							+ "FROM <pjschema>.tissues AS t ON t.tiid = p.tiid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS s\n"
							+ "ON s.tiid = p.tiid AND s.prid = @param1\n"
							+ "WHERE t.syid = @param2 AND t.onid = @param3;\nEND";
				}
				break;
			case 52:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpwdyprv(param1 DATE)\nBEGIN\n"
							+ "SELECT MAX(wddt) AS wddt"
							+ "FROM workdays\n"
							+ "WHERE wddt < param1 AND wdtp = 'D';\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpwdyprv @param1 DATE AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT MAX(wddt) AS wddt FROM <pjschema>.workdays\n"
							+ "WHERE wddt < @param1 AND wdtp = 'D';\nEND";
				}
				break;
			case 53:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpstyles()\nBEGIN\n"
							+ "SELECT s.prid, p.prnm\n"
							+ "FROM <pjschema>.styles AS s\n"
							+ "INNER JOIN <pjschema>.persons AS p ON p.prid = s.prid"
							+ "ORDER BY p.prnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpstyles AS\nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT s.prid, p.prnm\n"
							+ "FROM <pjschema>.styles AS s\n"
							+ "INNER JOIN <pjschema>.persons AS p ON p.prid = s.prid"
							+ "ORDER BY p.prnm;\nEND";
				}
				break;
			case 54:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpstyleprs(param1 SMALLINT)\nBEGIN\n"
							+ "SELECT * FROM <pjschema>.styles WHERE prid = param1;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpstyleprs @param1 SMALLINT AS\nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT * FROM <pjschema>.styles WHERE prid = @param1;\nEND";
				}
				break;
			case 55:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpcaseumls(param1 INT, param2 SMALLINT)\nBEGIN\n"
							+ "SELECT ud.spid, ud.dgid, ud.paid, ud.dglb, ud.palb, di.dsid, di.dgnm,\n"
							+ "us.tiid, us.splb, us.splo, t1.poid, t1.onid, t1.sbid, t1.syid, t1.ticx,\n"
							+ "t1.tinm AS spnm, t1.tidc AS spdc1, t1.podc AS sppo1, t2.tinm AS tinm,\n"
							+ "t2.tidc AS tidc1, od.dgdc AS dgdc1, od.midc AS midc1, p1.tidc AS spdc2,\n"
							+ "p1.podc AS sppo2, p2.tidc AS tidc2, dp.dgdc AS dgdc2, dp.midc AS midc2\n"
							+ "FROM <pjschema>.umlsdia AS ud\n"
							+ "INNER JOIN <pjschema>.diagnosis AS di ON di.dgid = ud.dgid\n"
							+ "INNER JOIN <pjschema>.umlsspc AS us ON us.spid = ud.spid\n"
							+ "INNER JOIN <pjschema>.tissues AS t1 ON t1.tiid = us.tiid\n"
							+ "INNER JOIN <pjschema>.tissueparts AS tp ON tp.paid = ud.paid\n"
							+ "INNER JOIN <pjschema>.tissues AS t2 ON t2.tiid = tp.tiid\n"
							+ "INNER JOIN <pjschema>.organdiagnosis AS od ON od.onid = t2.onid AND od.dgid = ud.dgid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p1 ON p1.prid = param2 AND p1.tiid = t1.tiid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p2 ON p2.prid = param2 AND p2.tiid = t2.tiid\n"
							+ "LEFT OUTER JOIN <pjschema>.diagnosispersons AS dp ON dp.prid = param2 AND dp.onid = t2.onid\n"
							+ "AND dp.dgid = ud.dgid WHERE us.caid = param1\n"
							+ "ORDER BY us.splb, ud.tilb, ud.dglb;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpcaseumls @param1 INT, @param2 SMALLINT AS\nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT ud.spid, ud.dgid, ud.paid, ud.dglb, ud.palb, di.dsid, di.dgnm,\n"
							+ "us.tiid, us.splb, us.splo, t1.poid, t1.onid, t1.sbid, t1.syid, t1.ticx,\n"
							+ "t1.tinm AS spnm, t1.tidc AS spdc1, t1.podc AS sppo1, t2.tinm AS tinm,\n"
							+ "t2.tidc AS tidc1, od.dgdc AS dgdc1, od.midc AS midc1, p1.tidc AS spdc2,\n"
							+ "p1.podc AS sppo2, p2.tidc AS tidc2, dp.dgdc AS dgdc2, dp.midc AS midc2\n"
							+ "FROM <pjschema>.umlsdia AS ud\n"
							+ "INNER JOIN <pjschema>.diagnosis AS di ON di.dgid = ud.dgid\n"
							+ "INNER JOIN <pjschema>.umlsspc AS us ON us.spid = ud.spid\n"
							+ "INNER JOIN <pjschema>.tissues AS t1 ON t1.tiid = us.tiid\n"
							+ "INNER JOIN <pjschema>.tissueparts AS tp ON tp.paid = ud.paid\n"
							+ "INNER JOIN <pjschema>.tissues AS t2 ON t2.tiid = tp.tiid\n"
							+ "INNER JOIN <pjschema>.organdiagnosis AS od ON od.onid = t2.onid AND od.dgid = ud.dgid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p1 ON p1.prid = @param2 AND p1.tiid = t1.tiid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p2 ON p2.prid = @param2 AND p2.tiid = t2.tiid\n"
							+ "LEFT OUTER JOIN <pjschema>.diagnosispersons AS dp ON dp.prid = @param2 AND dp.onid = t2.onid\n"
							+ "AND dp.dgid = ud.dgid WHERE us.caid = @param1\n"
							+ "ORDER BY us.splb, ud.tilb, ud.dglb;\nEND";
				}
				break;
			case 56:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpsubs()\nBEGIN\n"
							+ "SELECT sbid, sbnm FROM <pjschema>.subs ORDER BY sbnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpsubs AS\nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT sbid, sbnm FROM <pjschema>.subs ORDER BY sbnm;\nEND";
				}
				break;
			case 57:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpdiseases()\nBEGIN\n"
							+ "SELECT dsid, dsnm FROM <pjschema>.diseases ORDER BY dsnm;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpdiseases AS\nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT dsid, dsnm FROM <pjschema>.diseases ORDER BY dsnm;\nEND";
				}
				break;
			case 58:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udporgans()\nBEGIN\n"
							+ "SELECT ssoid, syid, sbid, onid, onnm\n"
							+ "FROM <pjschema>.udvorgans ORDER BY syid, sbid, onid;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udporgans AS\nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT ssoid, syid, sbid, onid, onnm\n"
							+ "FROM <pjschema>.udvorgans ORDER BY syid, sbid, onid;\nEND";
				}
				break;
			default:
				if (dbID == DB_MARIA) {
					sql = "CREATE PROCEDURE <pjschema>.udpspecimentissues(param1 SMALLINT, param2 SMALLINT)\nBEGIN\n"
							+ "SELECT st.tiid, tp.paid, tp.palb, tp.dgid,\n"
							+ "t1.poid, t1.onid, t1.sbid, t1.syid, t1.ticx,\n"
							+ "t1.tinm AS spnm, t1.tidc AS spdc1, t1.podc AS sppo1\n"
							+ "t2.tinm AS tinm, t2.tidc AS tidc1\n"
							+ "p1.tidc AS spdc2, p1.podc AS sppo2\n"
							+ "p2.tidc AS tidc2\n"
							+ "FROM <pjschema>.specimentissues AS st\n"
							+ "INNER JOIN <pjschema>.tissues AS t1 ON t1.tiid = st.tiid\n"
							+ "INNER JOIN <pjschema>.tissueparts AS tp ON tp.tiid = st.tiid\n"
							+ "INNER JOIN <pjschema>.tissues AS t2 ON t2.tiid = tp.paid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p1 ON p1.prid = param2 AND p1.tiid = st.tiid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p2 ON p2.prid = param2 AND p2.tiid = tp.paid\n"
							+ "WHERE st.spid = param1\n"
							+ "ORDER BY tp.palb;\nEND";
				} else {
					sql = "CREATE PROCEDURE <pjschema>.udpspecimentissues @param1 SNALLINT, @param2 SMALLINT AS \nBEGIN\nSET NOCOUNT ON;\n"
							+ "SELECT st.tiid, tp.paid, tp.palb,\n"
							+ "t1.poid, t1.onid, t1.sbid, t1.syid, t1.ticx,\n"
							+ "t1.tinm AS spnm, t1.tidc AS spdc1, t1.podc AS sppo1\n"
							+ "t2.tinm AS tinm, t2.tidc AS tidc1\n"
							+ "p1.tidc AS spdc2, p1.podc AS sppo2\n"
							+ "p2.tidc AS tidc2\n"
							+ "FROM <pjschema>.specimentissues AS st\n"
							+ "INNER JOIN <pjschema>.tissues AS t1 ON t1.tiid = st.tiid\n"
							+ "INNER JOIN <pjschema>.tissueparts AS tp ON tp.tiid = st.tiid\n"
							+ "INNER JOIN <pjschema>.tissues AS t2 ON t2.tiid = tp.paid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p1 ON p1.prid = @param2 AND p1.tiid = st.tiid\n"
							+ "LEFT OUTER JOIN <pjschema>.tissuepersons AS p2 ON p2.prid = @param2 AND p2.tiid = tp.paid\n"
							+ "WHERE st.spid = @param1\n"
							+ "ORDER BY tp.palb;\nEND";
				}
			}
			execute(getSchema(sql));
			if (errorID != LibConstants.ERROR_NONE) {
				break;
			}
			noRows++;
			log(LibConstants.ERROR_NONE, "Created " + noRows + " procedures.");
		}
		try {
			Thread.sleep(LibConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	private void createTables() {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 45; i++) {
			switch (i) {
			case 0:
				sql = "CREATE TABLE <pjschema>.setup (stid SMALLINT PRIMARY KEY, stva VARCHAR(64) NOT NULL)";
				break;
			case 1:
				sql = "CREATE TABLE <pjschema>.workdays (wdid INT PRIMARY KEY, wdno INT NOT NULL,\n"
						+ "wdtp CHAR(1) NOT NULL, wddt DATE UNIQUE NOT NULL)";
				break;
			case 2:
				sql = "CREATE TABLE <pjschema>.errors (caid INT PRIMARY KEY, erid SMALLINT NOT NULL,\n"
						+ "cano CHAR(13) UNIQUE NOT NULL, erdc VARCHAR(2048) NOT NULL)";
				break;
			case 3:
				sql = "CREATE TABLE <pjschema>.facilities (faid SMALLINT PRIMARY KEY,\n"
						+ "fafl CHAR(1) NOT NULL, fald CHAR(1) NOT NULL, fanm VARCHAR(4) UNIQUE NOT NULL,\n"
						+ "fadc VARCHAR(80) NOT NULL)";
				break;
			case 4:
				sql = "CREATE TABLE <pjschema>.persons (prid SMALLINT PRIMARY KEY,\n"
						+ "prvl INT NOT NULL, prdt DATE NOT NULL, prac CHAR(1) NOT NULL,\n"
						+ "prcd CHAR(2) NOT NULL, prnm CHAR(3) NOT NULL, prls VARCHAR(30) NOT NULL,\n"
						+ "prfr VARCHAR(30) NOT NULL)";
				break;
			case 5:
				sql = "CREATE TABLE <pjschema>.procedures (poid SMALLINT PRIMARY KEY,\n"
						+ "ponm VARCHAR(16) UNIQUE NOT NULL, podc VARCHAR(256) NOT NULL)";
				break;
			case 6:
				sql = "CREATE TABLE <pjschema>.rules (ruid SMALLINT PRIMARY KEY,\n"
						+ "runm VARCHAR(16) UNIQUE NOT NULL, rudc VARCHAR(256) NOT NULL)";
				break;
			case 7:
				sql = "CREATE TABLE <pjschema>.specialties (syid SMALLINT PRIMARY KEY,\n"
						+ "syfl CHAR(1) NOT NULL, syld CHAR(1) NOT NULL, sysp CHAR(1) NOT NULL,\n"
						+ "synm VARCHAR(16) UNIQUE NOT NULL)";
				break;
			case 8:
				sql = "CREATE TABLE <pjschema>.turnaround (taid SMALLINT PRIMARY KEY,\n"
						+ "grss SMALLINT NOT NULL, embd SMALLINT NOT NULL, micr SMALLINT NOT NULL,\n"
						+ "rout SMALLINT NOT NULL, finl SMALLINT NOT NULL, tanm VARCHAR(16) UNIQUE NOT NULL)";
				break;
			case 9:
				sql = "CREATE TABLE <pjschema>.coder1 (coid SMALLINT PRIMARY KEY,\n"
						+ "ruid SMALLINT NOT NULL REFERENCES <pjschema>.rules (ruid),\n"
						+ "coqy SMALLINT NOT NULL, cov1 DECIMAL(5, 3) NOT NULL,\n"
						+ "cov2 DECIMAL(5, 3) NOT NULL, cov3 DECIMAL(5, 3) NOT NULL,\n"
						+ "conm VARCHAR(16) UNIQUE NOT NULL, codc VARCHAR(256) NOT NULL)";
				break;
			case 10:
				sql = "CREATE TABLE <pjschema>.coder2 (coid SMALLINT PRIMARY KEY,\n"
						+ "ruid SMALLINT NOT NULL REFERENCES <pjschema>.rules (ruid),\n"
						+ "coqy SMALLINT NOT NULL, cov1 DECIMAL(5, 3) NOT NULL,\n"
						+ "cov2 DECIMAL(5, 3) NOT NULL, cov3 DECIMAL(5, 3) NOT NULL,\n"
						+ "conm VARCHAR(16) UNIQUE NOT NULL, codc VARCHAR(256) NOT NULL)";
				break;
			case 11:
				sql = "CREATE TABLE <pjschema>.coder3 (coid SMALLINT PRIMARY KEY,\n"
						+ "ruid SMALLINT NOT NULL REFERENCES <pjschema>.rules (ruid),\n"
						+ "coqy SMALLINT NOT NULL, cov1 DECIMAL(5, 3) NOT NULL,\n"
						+ "cov2 DECIMAL(5, 3) NOT NULL, cov3 DECIMAL(5, 3) NOT NULL,\n"
						+ "conm VARCHAR(16) UNIQUE NOT NULL, codc VARCHAR(128) NOT NULL)";
				break;
			case 12:
				sql = "CREATE TABLE <pjschema>.coder4 (coid SMALLINT PRIMARY KEY,\n"
						+ "ruid SMALLINT NOT NULL REFERENCES <pjschema>.rules (ruid),\n"
						+ "coqy SMALLINT NOT NULL, cov1 DECIMAL(5, 3) NOT NULL,\n"
						+ "cov2 DECIMAL(5, 3) NOT NULL, cov3 DECIMAL(5, 3) NOT NULL,\n"
						+ "conm VARCHAR(16) UNIQUE NOT NULL, codc VARCHAR(256) NOT NULL)";
				break;
			case 13:
				sql = "CREATE TABLE <pjschema>.accessions (acid SMALLINT PRIMARY KEY,\n"
						+ "syid SMALLINT NOT NULL REFERENCES <pjschema>.specialties (syid),\n"
						+ "acfl CHAR(1) NOT NULL, acld CHAR(1) NOT NULL, acnm VARCHAR(30) UNIQUE NOT NULL)";
				break;
			case 14:
				sql = "CREATE TABLE <pjschema>.subspecial (sbid SMALLINT PRIMARY KEY,\n"
						+ "syid SMALLINT NOT NULL REFERENCES <pjschema>.specialties (syid),\n"
						+ "sbnm VARCHAR(8) UNIQUE NOT NULL, sbdc VARCHAR(32) NOT NULL)";
				break;
			case 15:
				sql = "CREATE TABLE <pjschema>.ordertypes (otid SMALLINT PRIMARY KEY,\n"
						+ "otnm VARCHAR(8) UNIQUE NOT NULL)";
				break;
			case 16:
				sql = "CREATE TABLE <pjschema>.ordergroups (ogid SMALLINT PRIMARY KEY,\n"
						+ "otid SMALLINT NOT NULL REFERENCES <pjschema>.ordertypes (otid),\n"
						+ "ogc1 SMALLINT NOT NULL REFERENCES <pjschema>.coder1 (coid),\n"
						+ "ogc2 SMALLINT NOT NULL REFERENCES <pjschema>.coder2 (coid),\n"
						+ "ogc3 SMALLINT NOT NULL REFERENCES <pjschema>.coder3 (coid),\n"
						+ "ogc4 SMALLINT NOT NULL REFERENCES <pjschema>.coder4 (coid),\n"
						+ "ogc5 INT NOT NULL, ognm VARCHAR(8) UNIQUE NOT NULL, ogdc VARCHAR(64) NOT NULL)";
				break;
			case 17:
				sql = "CREATE TABLE <pjschema>.ordermaster (omid SMALLINT PRIMARY KEY,\n"
						+ "ogid SMALLINT NOT NULL REFERENCES <pjschema>.ordergroups (ogid),\n"
						+ "omnm VARCHAR(15) UNIQUE NOT NULL, omdc VARCHAR(80) NOT NULL)";
				break;
			case 18:
				sql = "CREATE TABLE <pjschema>.specigroups (sgid SMALLINT PRIMARY KEY,\n"
						+ "sbid SMALLINT NOT NULL REFERENCES <pjschema>.subspecial (sbid),\n"
						+ "poid SMALLINT NOT NULL REFERENCES <pjschema>.procedures (poid),\n"
						+ "sg1b SMALLINT NOT NULL REFERENCES <pjschema>.coder1 (coid),\n"
						+ "sg1m SMALLINT NOT NULL REFERENCES <pjschema>.coder1 (coid),\n"
						+ "sg1r SMALLINT NOT NULL REFERENCES <pjschema>.coder1 (coid),\n"
						+ "sg2b SMALLINT NOT NULL REFERENCES <pjschema>.coder2 (coid),\n"
						+ "sg2m SMALLINT NOT NULL REFERENCES <pjschema>.coder2 (coid),\n"
						+ "sg2r SMALLINT NOT NULL REFERENCES <pjschema>.coder2 (coid),\n"
						+ "sg3b SMALLINT NOT NULL REFERENCES <pjschema>.coder3 (coid),\n"
						+ "sg3m SMALLINT NOT NULL REFERENCES <pjschema>.coder3 (coid),\n"
						+ "sg3r SMALLINT NOT NULL REFERENCES <pjschema>.coder3 (coid),\n"
						+ "sg4b SMALLINT NOT NULL REFERENCES <pjschema>.coder4 (coid),\n"
						+ "sg4m SMALLINT NOT NULL REFERENCES <pjschema>.coder4 (coid),\n"
						+ "sg4r SMALLINT NOT NULL REFERENCES <pjschema>.coder4 (coid),\n"
						+ "sgv5 INT NOT NULL, sgln CHAR(1) NOT NULL, sgdc VARCHAR(64) UNIQUE NOT NULL)";
				break;
			case 19:
				sql = "CREATE TABLE <pjschema>.specimaster (smid SMALLINT PRIMARY KEY,\n"
						+ "sgid SMALLINT NOT NULL REFERENCES <pjschema>.specigroups (sgid),\n"
						+ "taid SMALLINT NOT NULL REFERENCES <pjschema>.turnaround (taid),\n"
						+ "smnm VARCHAR(16) UNIQUE NOT NULL, smdc VARCHAR(80) NOT NULL)";
				break;
			case 20:
				sql = "CREATE TABLE <pjschema>.services (srid SMALLINT PRIMARY KEY,\n"
						+ "faid SMALLINT NOT NULL REFERENCES <pjschema>.facilities (faid),\n"
						+ "sbid SMALLINT NOT NULL REFERENCES <pjschema>.subspecial (sbid),\n"
						+ "srcd SMALLINT NOT NULL, srnm VARCHAR(8) UNIQUE NOT NULL, srdc VARCHAR(64) NOT NULL)";
				break;
			case 21:
				sql = "CREATE TABLE <pjschema>.schedules (wdid INT NOT NULL REFERENCES <pjschema>.workdays (wdid),\n"
						+ "srid SMALLINT NOT NULL REFERENCES <pjschema>.services (srid),\n"
						+ "prid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "PRIMARY KEY(wdid, srid, prid))";
				break;
			case 22:
				sql = "CREATE TABLE <pjschema>.cases (caid INT PRIMARY KEY,\n"
						+ "faid SMALLINT NOT NULL REFERENCES <pjschema>.facilities (faid),\n"
						+ "sbid SMALLINT NOT NULL REFERENCES <pjschema>.subspecial (sbid),\n"
						+ "smid SMALLINT NOT NULL REFERENCES <pjschema>.specimaster (smid),\n"
						+ "grid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "emid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "miid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "roid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "fnid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "grta SMALLINT NOT NULL, emta SMALLINT NOT NULL, mita SMALLINT NOT NULL,\n"
						+ "rota SMALLINT NOT NULL, fnta SMALLINT NOT NULL, casp SMALLINT NOT NULL,\n"
						+ "cabl SMALLINT NOT NULL, casl SMALLINT NOT NULL, casy SMALLINT NOT NULL,\n"
						+ "cafs SMALLINT NOT NULL, cahe SMALLINT NOT NULL, cass SMALLINT NOT NULL,\n"
						+ "caih SMALLINT NOT NULL, camo SMALLINT NOT NULL, cav5 INT NOT NULL,\n"
						+ "aced DATETIME NOT NULL, gred DATETIME NOT NULL, emed DATETIME NOT NULL,\n"
						+ "mied DATETIME NOT NULL, roed DATETIME NOT NULL, fned DATETIME NOT NULL,\n"
						+ "cav1 DECIMAL(5, 3) NOT NULL, cav2 DECIMAL(5, 3) NOT NULL, cav3 DECIMAL(5, 3) NOT NULL,\n"
						+ "cav4 DECIMAL(5, 3) NOT NULL, cano CHAR(13) UNIQUE NOT NULL)";
				break;
			case 23:
				sql = "CREATE TABLE <pjschema>.specimens (spid INT PRIMARY KEY,\n"
						+ "caid INT NOT NULL REFERENCES <pjschema>.cases (caid),\n"
						+ "smid SMALLINT NOT NULL REFERENCES <pjschema>.specimaster (smid),\n"
						+ "spbl SMALLINT NOT NULL, spsl SMALLINT NOT NULL, spfr SMALLINT NOT NULL,\n"
						+ "sphe SMALLINT NOT NULL, spss SMALLINT NOT NULL, spih SMALLINT NOT NULL,\n"
						+ "spmo SMALLINT NOT NULL, spv5 INT NOT NULL, spv1 DECIMAL(5, 3) NOT NULL,\n"
						+ "spv2 DECIMAL(5, 3) NOT NULL, spv3 DECIMAL(5, 3) NOT NULL,\n"
						+ "spv4 DECIMAL(5, 3) NOT NULL, spdc VARCHAR(80) NOT NULL)";
				break;
			case 24:
				sql = "CREATE TABLE <pjschema>.orders (spid INT NOT NULL REFERENCES <pjschema>.specimens (spid),\n"
						+ "ogid SMALLINT NOT NULL REFERENCES <pjschema>.ordergroups (ogid),\n"
						+ "orqy SMALLINT NOT NULL, orv1 DECIMAL(5, 3) NOT NULL,\n"
						+ "orv2 DECIMAL(5, 3) NOT NULL, orv3 DECIMAL(5, 3) NOT NULL,\n"
						+ "orv4 DECIMAL(5, 3) NOT NULL, PRIMARY KEY (spid, ogid))";
				break;
			case 25:
				sql = "CREATE TABLE <pjschema>.frozens (spid INT PRIMARY KEY REFERENCES <pjschema>.specimens (spid),\n"
						+ "prid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "frbl SMALLINT NOT NULL, frsl SMALLINT NOT NULL, frv5 INT NOT NULL,\n"
						+ "frv1 DECIMAL(5, 3) NOT NULL, frv2 DECIMAL(5, 3) NOT NULL,\n"
						+ "frv3 DECIMAL(5, 3) NOT NULL, frv4 DECIMAL(5, 3) NOT NULL)";
				break;
			case 26:
				sql = "CREATE TABLE <pjschema>.additionals (caid INT NOT NULL REFERENCES <pjschema>.cases (caid),\n"
						+ "prid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "adcd SMALLINT NOT NULL, adv5 INT NOT NULL, addt DATETIME NOT NULL,\n"
						+ "adv1 DECIMAL(5, 3) NOT NULL, adv2 DECIMAL(5, 3) NOT NULL,\n"
						+ "adv3 DECIMAL(5, 3) NOT NULL, adv4 DECIMAL(5, 3) NOT NULL,\n"
						+ "PRIMARY KEY (caid, prid, adcd, addt))";
				break;
			case 27:
				sql = "CREATE TABLE <pjschema>.comments (caid INT PRIMARY KEY REFERENCES <pjschema>.cases (caid),\n"
						+ "com1 VARCHAR(2048) NOT NULL, com2 VARCHAR(2048) NOT NULL,\n"
						+ "com3 VARCHAR(2048) NOT NULL, com4 VARCHAR(2048) NOT NULL)";
				break;
			case 28:
				sql = "CREATE TABLE <pjschema>.pending (pnid INT PRIMARY KEY,\n"
						+ "faid SMALLINT NOT NULL REFERENCES <pjschema>.facilities (faid),\n"
						+ "sbid SMALLINT NOT NULL REFERENCES <pjschema>.subspecial (sbid),\n"
						+ "smid SMALLINT NOT NULL REFERENCES <pjschema>.specimaster (smid),\n"
						+ "poid SMALLINT NOT NULL REFERENCES <pjschema>.procedures (poid),\n"
						+ "grid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "emid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "miid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "roid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "fnid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "grta SMALLINT NOT NULL, emta SMALLINT NOT NULL, mita SMALLINT NOT NULL,\n"
						+ "rota SMALLINT NOT NULL, fnta SMALLINT NOT NULL, pnst SMALLINT NOT NULL,\n"
						+ "pnsp SMALLINT NOT NULL, pnbl SMALLINT NOT NULL, pnsl SMALLINT NOT NULL,\n"
						+ "pnv5 INT NOT NULL, aced DATETIME NOT NULL, gred DATETIME NOT NULL,\n"
						+ "emed DATETIME NOT NULL, mied DATETIME NOT NULL, roed DATETIME NOT NULL,\n"
						+ "fned DATETIME NOT NULL, pnno CHAR(13) UNIQUE NOT NULL)";
				break;
			case 29:
				sql = "CREATE TABLE <pjschema>.subs (sbid SMALLINT PRIMARY KEY,\n"
						+ "sbnm VARCHAR(8) UNIQUE NOT NULL)";
				break;
			case 30:
				sql = "CREATE TABLE <pjschema>.organs (onid SMALLINT PRIMARY KEY,\n"
						+ "oncu CHAR(8) UNIQUE NOT NULL, onnm VARCHAR(64) UNIQUE NOT NULL)";
				break;
			case 31:
				sql = "CREATE TABLE <pjschema>.diseases (dsid SMALLINT PRIMARY KEY,\n"
						+ "dscu CHAR(8) UNIQUE NOT NULL, dsnm VARCHAR(32) UNIQUE NOT NULL)";
				break;
			case 32:
				sql = "CREATE TABLE <pjschema>.procs (poid SMALLINT PRIMARY KEY,\n"
						+ "syid SMALLINT NOT NULL REFERENCES <pjschema>.specialties (syid),\n"
						+ "ponm VARCHAR(32) UNIQUE NOT NULL)";
				break;
			case 33:
				sql = "CREATE TABLE <pjschema>.sysbon (ssoid SMALLINT PRIMARY KEY,\n"
						+ "syid SMALLINT NOT NULL REFERENCES <pjschema>.specialties (syid),\n"
						+ "sbid SMALLINT NOT NULL REFERENCES <pjschema>.subs (sbid),\n"
						+ "onid SMALLINT NOT NULL REFERENCES <pjschema>.organs (onid),\n"
						+ "UNIQUE KEY (syid, sbid, onid))";
				break;
			case 34:
				sql = "CREATE TABLE <pjschema>.specs (scid SMALLINT PRIMARY KEY,\n"
						+ "ssoid SMALLINT NOT NULL REFERENCES <pjschema>.sysbon (ssoid),\n"
						+ "poid SMALLINT NOT NULL REFERENCES <pjschema>.procs (poid),\n"
						+ "sccu CHAR(8) UNIQUE NOT NULL, scnm VARCHAR(32) UNIQUE NOT NULL,\n"
						+ "scdc VARCHAR(256) NOT NULL, podc VARCHAR(256) NOT NULL)";
				break;
			case 35:
				sql = "CREATE TABLE <pjschema>.specpers (\n"
						+ "prid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "scid SMALLINT NOT NULL REFERENCES <pjschema>.specs (scid),\n"
						+ "scnmpr VARCHAR(32) UNIQUE NOT NULL, scdcpr VARCHAR(256) NOT NULL,\n"
						+ "podcpr VARCHAR(256) NOT NULL, PRIMARY KEY (prid, scid))";
				break;
			case 36:
				sql = "CREATE TABLE <pjschema>.specspec (smid SMALLINT PRIMARY KEY,\n"
						+ "scid SMALLINT NOT NULL REFERENCES <pjschema>.specs (scid))";
				break;
			case 37:
				sql = "CREATE TABLE <pjschema>.tissues (\n"
						+ "tiid SMALLINT NOT NULL REFERENCES <pjschema>.specs (scid),\n"
						+ "scid SMALLINT NOT NULL REFERENCES <pjschema>.specs (scid),\n"
						+ "tior SMALLINT NOT NULL, PRIMARY KEY (tiid, scid))";
				break;
			case 38:
				sql = "CREATE TABLE <pjschema>.diagnosis (dgid INT PRIMARY KEY,\n"
						+ "dsid SMALLINT NOT NULL REFERENCES <pjschema>.diseases (dsid),\n"
						+ "dgcu CHAR(8) UNIQUE NOT NULL, dgnm VARCHAR(64) UNIQUE NOT NULL))";
				break;
			case 39:
				sql = "CREATE TABLE <pjschema>.orgdiag (odid INT PRIMARY KEY,\n"
						+ "onid SMALLINT NOT NULL REFERENCES <pjschema>.organs (onid),\n"
						+ "dgid INT NOT NULL REFERENCES <pjschema>.diagnosis (dgid),\n"
						+ "odcu CHAR(8) UNIQUE NOT NULL, dgdc VARCHAR(2048) NOT NULL,\n"
						+ "midc VARCHAR(2048) NOT NULL, UNIQUE KEY (onid, dgid))";
				break;
			case 40:
				sql = "CREATE TABLE <pjschema>.diagpers (\n"
						+ "prid SMALLINT NOT NULL REFERENCES <pjschema>.persons (prid),\n"
						+ "odid SMALLINT NOT NULL REFERENCES <pjschema>.orgdiag (odid),\n"
						+ "dgdc VARCHAR(2048) NOT NULL, midc VARCHAR(2048) NOT NULL,\n"
						+ "PRIMARY KEY (prid, odid))";
				break;
			case 41:
				sql = "CREATE TABLE <pjschema>.styles ("
						+ "prid SMALLINT PRIMARY KEY REFERENCES <pjschema>.persons (prid),\n"
						+ "slid SMALLINT NOT NULL, cadi SMALLINT NOT NULL,\n"
						+ "calo SMALLINT NOT NULL, cami SMALLINT NOT NULL,\n"
						+ "capo SMALLINT NOT NULL, casp SMALLINT NOT NULL,\n"
						+ "cati SMALLINT NOT NULL, lidi SMALLINT NOT NULL,\n"
						+ "lisp SMALLINT NOT NULL, liti SMALLINT NOT NULL,\n"
						+ "font VARCHAR(256) NOT NULL, tgdi VARCHAR(256) NOT NULL,\n"
						+ "tgmi VARCHAR(256) NOT NULL, tgsp VARCHAR(256) NOT NULL,\n"
						+ "tgti VARCHAR(256) NOT NULL, tgsl VARCHAR(256) NOT NULL,\n"
						+ "txsp VARCHAR(256) NOT NULL, txsl VARCHAR(256) NOT NULL)";
				break;
			case 42:
				sql = "CREATE TABLE <pjschema>.umlsspec (spid INT PRIMARY KEY,\n"
						+ "caid INT NOT NULL, spor SMALLINT NOT NULL,\n"
						+ "scid SMALLINT NOT NULL REFERENCES <pjschema>.specs (scid),\n"
						+ "cano CHAR(13) NOT NULL, spdc VARCHAR(80) NOT NULL)";
				break;
			case 43:
				sql = "CREATE TABLE <pjschema>.umlstiss (tiid INT PRIMARY KEY,\n"
						+ "spid INT NOT NULL REFERENCES <pjschema>.umlsspec (spid),\n"
						+ "scid SMALLINT NOT NULL REFERENCES <pjschema>.umlsspec (scid),\n"
						+ "tisp SMALLINT NOT NULL REFERENCES <pjschema>.specs (scid),\n"
						+ "tior SMALLINT NOT NULL, UNIQUE KEY (spid, scid, tisp))";
				break;
			default:
				sql = "CREATE TABLE <pjschema>.umlsdiag (\n"
						+ "tiid INT NOT NULL REFERENCES <pjschema>.umlstiss (tiid),\n"
						+ "dgid INT NOT NULL REFERENCES <pjschema>.diagnosis (dgid),\n"
						+ "dgor SMALLINT NOT NULL, PRIMARY KEY (tiid, dgid))";
			}
			if (dbID == DB_MARIA) {
				sql += " ENGINE = InnoDB;";
			} else if (dbID == DB_DERBY || dbID == DB_POSTG) {
				if (noRows > 21) {
					sql = sql.replace("DATETIME", "TIMESTAMP");
				}
			}
			execute(getSchema(sql));
			if (errorID != LibConstants.ERROR_NONE) {
				break;
			}
			noRows++;
			log(LibConstants.ERROR_NONE, "Created " + noRows + " tables.");
		}
		try {
			Thread.sleep(LibConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	private void createViews() {
		String sql = "";
		noRows = 0;
		for (int i = 0; i < 21; i++) {
			switch (i) {
			case 0:
				sql = "CREATE VIEW <pjschema>.udvaccessions AS\n"
						+ "SELECT a.acid, a.syid, a.acfl, a.acld, a.acnm, y.syfl, y.syld, y.sysp, y.synm\n"
						+ "FROM <pjschema>.accessions AS a\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = a.syid";
				break;
			case 1:
				sql = "CREATE VIEW <pjschema>.udvadditionals AS\n"
						+ "SELECT a.caid, a.prid, a.adcd, a.adv5, a.adv1, a.adv2,\n"
						+ "a.adv3, a.adv4, a.addt, p.prnm, p.prls, p.prfr, c.faid,\n"
						+ "c.cano, f.fanm, g.poid, g.sbid, g.sgid, r.ponm, b.syid,\n"
						+ "b.sbnm, g.sgdc, y.synm FROM <pjschema>.additionals AS a\n"
						+ "INNER JOIN <pjschema>.persons AS p ON p.prid = a.prid\n"
						+ "INNER JOIN <pjschema>.cases AS c ON c.caid = a.caid\n"
						+ "INNER JOIN <pjschema>.facilities AS f ON f.faid = c.faid\n"
						+ "INNER JOIN <pjschema>.specimaster AS m ON m.smid = c.smid\n"
						+ "INNER JOIN <pjschema>.specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN <pjschema>.procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid";
				break;
			case 2:
				sql = "CREATE VIEW <pjschema>.udvaddlastorder AS\n"
						+ "SELECT MAX(addt) AS addt FROM <pjschema>.additionals\n"
						+ "WHERE adcd > 2";
				break;
			case 3:
				sql = "CREATE VIEW <pjschema>.udvcases AS\n"
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
						+ "FROM <pjschema>.cases AS c\n"
						+ "INNER JOIN <pjschema>.facilities AS f ON f.faid = c.faid\n"
						+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = c.sbid\n"
						+ "INNER JOIN <pjschema>.specimaster AS m ON m.smid = c.smid\n"
						+ "INNER JOIN <pjschema>.persons AS pg ON pg.prid = c.grid\n"
						+ "INNER JOIN <pjschema>.persons AS pe ON pe.prid = c.emid\n"
						+ "INNER JOIN <pjschema>.persons AS pm ON pm.prid = c.miid\n"
						+ "INNER JOIN <pjschema>.persons AS pr ON pr.prid = c.roid\n"
						+ "INNER JOIN <pjschema>.persons AS pf ON pf.prid = c.fnid\n"
						+ "INNER JOIN <pjschema>.specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN <pjschema>.procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid";
				break;
			case 4:
				sql = "CREATE VIEW <pjschema>.udvcaseslast AS\nSELECT MAX(fned) AS fned\n"
						+ "FROM <pjschema>.cases";
				break;
			case 5:
				sql = "CREATE VIEW <pjschema>.udvfrozens AS\nSELECT z.spid, z.prid, z.frbl, z.frsl,\n"
						+ "z.frv5, z.frv1, z.frv2, z.frv3, z.frv4, c.caid, c.faid,\n"
						+ "c.aced, c.cano, p.prnm, p.prls, p.prfr, s.spdc,\n"
						+ "m.smnm, m.smdc, f.fanm, g.poid, g.sbid, g.sgid,\n"
						+ "g.sgdc, r.ponm, b.syid, b.sbnm, y.synm\nFROM <pjschema>.frozens AS z\n"
						+ "INNER JOIN <pjschema>.specimens AS s ON s.spid = z.spid\n"
						+ "INNER JOIN <pjschema>.persons AS p ON p.prid = z.prid\n"
						+ "INNER JOIN <pjschema>.cases AS c ON c.caid = s.caid\n"
						+ "INNER JOIN <pjschema>.specimaster AS m ON m.smid = s.smid\n"
						+ "INNER JOIN <pjschema>.facilities AS f ON f.faid = c.faid\n"
						+ "INNER JOIN <pjschema>.specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN <pjschema>.procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid";
				break;
			case 6:
				sql = "CREATE VIEW <pjschema>.udvordergroups AS\n"
						+ "SELECT g.ogid, g.otid, g.ogc1, g.ogc2, g.ogc3, g.ogc4,\n"
						+ "g.ogc5, g.ognm, g.ogdc, a.conm AS C1NM, b.conm AS C2NM,\n"
						+ "c.conm AS C3NM, d.conm AS C4NM, t.otnm\nFROM <pjschema>.ordergroups AS g\n"
						+ "INNER JOIN <pjschema>.ordertypes AS t ON t.otid = g.otid\n"
						+ "INNER JOIN <pjschema>.coder1 AS a ON a.coid = g.ogc1\n"
						+ "INNER JOIN <pjschema>.coder2 AS b ON b.coid = g.ogc2\n"
						+ "INNER JOIN <pjschema>.coder3 AS c ON c.coid = g.ogc3\n"
						+ "INNER JOIN <pjschema>.coder4 AS d ON d.coid = g.ogc4";
				break;
			case 7:
				sql = "CREATE VIEW <pjschema>.udvordermaster AS\nSELECT m.omid, m.ogid, m.omnm, m.omdc, g.otid,\n"
						+ "g.ogc1, g.ogc2, g.ogc3, g.ogc4, g.ogc5, g.ognm,\n"
						+ "g.ogdc, a.conm AS C1NM, b.conm AS C2NM, c.conm AS C3NM,\nd.conm AS C4NM, t.otnm\n"
						+ "FROM <pjschema>.ordermaster AS m\n"
						+ "INNER JOIN <pjschema>.ordergroups AS g ON g.ogid = m.ogid\n"
						+ "INNER JOIN <pjschema>.ordertypes AS t ON t.otid = g.otid\n"
						+ "INNER JOIN <pjschema>.coder1 AS a ON a.coid = g.ogc1\n"
						+ "INNER JOIN <pjschema>.coder2 AS b ON b.coid = g.ogc2\n"
						+ "INNER JOIN <pjschema>.coder3 AS c ON c.coid = g.ogc3\n"
						+ "INNER JOIN <pjschema>.coder4 AS d ON d.coid = g.ogc4";
				break;
			case 8:
				sql = "CREATE VIEW <pjschema>.udvorders AS\n"
						+ "SELECT o.spid, o.ogid, o.orqy, o.orv1, o.orv2, o.orv3, o.orv4, g.ognm\n"
						+ "FROM <pjschema>.orders AS o\n"
						+ "INNER JOIN <pjschema>.ordergroups AS g ON g.ogid = o.ogid";
				break;
			case 9:
				sql = "CREATE VIEW <pjschema>.udvpending AS\n"
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
						+ "FROM <pjschema>.pending AS p\n"
						+ "INNER JOIN <pjschema>.facilities AS f ON f.faid = p.faid\n"
						+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = p.sbid\n"
						+ "INNER JOIN <pjschema>.specimaster AS m ON m.smid = p.smid\n"
						+ "INNER JOIN <pjschema>.persons AS pg ON pg.prid = p.grid\n"
						+ "INNER JOIN <pjschema>.persons AS pe ON pe.prid = p.emid\n"
						+ "INNER JOIN <pjschema>.persons AS pm ON pm.prid = p.miid\n"
						+ "INNER JOIN <pjschema>.persons AS pr ON pr.prid = p.roid\n"
						+ "INNER JOIN <pjschema>.persons AS pf ON pf.prid = p.fnid\n"
						+ "INNER JOIN <pjschema>.specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN <pjschema>.procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid";
				break;
			case 10:
				sql = "CREATE VIEW <pjschema>.udvpendinglast AS\nSELECT MAX(aced) AS aced "
						+ "FROM <pjschema>.pending";
				break;
			case 11:
				sql = "CREATE VIEW <pjschema>.udvschedules AS\nSELECT s.wdid, s.srid, s.prid,\n"
						+ "v.faid, v.sbid, v.srcd, v.srnm, v.srdc, p.prnm, p.prls, p.prfr,\n"
						+ "w.wddt, b.syid, b.sbnm, y.synm, f.fanm\n"
						+ "FROM <pjschema>.schedules AS s\n"
						+ "INNER JOIN <pjschema>.services AS v ON v.srid = s.srid\n"
						+ "INNER JOIN <pjschema>.persons AS p ON p.prid = s.prid\n"
						+ "INNER JOIN <pjschema>.workdays AS w ON w.wdid = s.wdid\n"
						+ "INNER JOIN <pjschema>.facilities AS f ON f.faid = v.faid\n"
						+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = v.sbid\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid";
				break;
			case 12:
				switch (dbID) {
				case DB_POSTG:
					sql = "CREATE VIEW <pjschema>.udvschedweeks AS\nSELECT DISTINCT s.wdid, w.wddt\n"
							+ "FROM <pjschema>.schedules AS s\n"
							+ "INNER JOIN <pjschema>.workdays AS w ON w.wdid = s.wdid\n"
							+ "WHERE date_part('dow', w.wddt) = 1\n"
							+ "ORDER BY w.wddt";
					break;
				case DB_MSSQL:
					sql = "CREATE VIEW <pjschema>.udvschedweeks AS\nSELECT DISTINCT s.wdid, w.wddt\n"
							+ "FROM <pjschema>.schedules AS s\n"
							+ "INNER JOIN <pjschema>.workdays AS w ON w.wdid = s.wdid\n"
							+ "WHERE DATEPART(weekday, w.wddt) = 1";
					break;
				case DB_MARIA:
					sql = "CREATE VIEW <pjschema>.udvschedweeks AS\nSELECT DISTINCT s.wdid, w.wddt\n"
							+ "FROM <pjschema>.schedules AS s\n"
							+ "INNER JOIN <pjschema>.workdays AS w ON w.wdid = s.wdid\n"
							+ "WHERE DAYOFWEEK(w.wddt) = 2";
					break;
				default:
					sql = "CREATE VIEW <pjschema>.udvschedweeks AS\nSELECT wdid, wddt\n"
							+ "FROM <pjschema>.workdays\n"
							+ "ORDER BY wddt DESC FETCH FIRST 370 ROWS ONLY";
				}
				break;
			case 13:
				sql = "CREATE VIEW <pjschema>.udvservices AS\nSELECT v.srid, v.faid, v.sbid, v.srcd, v.srnm,\n"
						+ "v.srdc, f.fanm, b.syid, b.sbnm, y.synm\nFROM <pjschema>.services AS v\n"
						+ "INNER JOIN <pjschema>.facilities AS f ON f.faid = v.faid\n"
						+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = v.sbid\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid";
				break;
			case 14:
				sql = "CREATE VIEW <pjschema>.udvspecigroups AS\n"
						+ "SELECT g.sgid, g.sbid, g.poid, g.sg1b, g.sg1m, g.sg1r,\n"
						+ "g.sg2b, g.sg2m, g.sg2r, g.sg3b, g.sg3m, g.sg3r, g.sg4b,\n"
						+ "g.sg4m, g.sg4r, g.sgv5, g.sgln, g.sgdc, r.ponm, b.syid,\n"
						+ "b.sbnm, b.sbdc, y.synm, b1.conm AS C1NB, m1.conm AS C1NM,\n"
						+ "r1.conm AS C1NR, b2.conm AS C2NB, m2.conm AS C2NM,\n"
						+ "r2.conm AS C2NR, b3.conm AS C3NB, m3.conm AS C3NM,\n"
						+ "r3.conm AS C3NR, b4.conm AS C4NB, m4.conm AS C4NM,\nr4.conm AS C4NR\n"
						+ "FROM <pjschema>.specigroups AS g\n"
						+ "INNER JOIN <pjschema>.coder1 AS b1 ON b1.coid = g.sg1b\n"
						+ "INNER JOIN <pjschema>.coder2 AS b2 ON b2.coid = g.sg2b\n"
						+ "INNER JOIN <pjschema>.coder3 AS b3 ON b3.coid = g.sg3b\n"
						+ "INNER JOIN <pjschema>.coder4 AS b4 ON b4.coid = g.sg4b\n"
						+ "INNER JOIN <pjschema>.coder1 AS m1 ON m1.coid = g.sg1m\n"
						+ "INNER JOIN <pjschema>.coder2 AS m2 ON m2.coid = g.sg2m\n"
						+ "INNER JOIN <pjschema>.coder3 AS m3 ON m3.coid = g.sg3m\n"
						+ "INNER JOIN <pjschema>.coder4 AS m4 ON m4.coid = g.sg4m\n"
						+ "INNER JOIN <pjschema>.coder1 AS r1 ON r1.coid = g.sg1r\n"
						+ "INNER JOIN <pjschema>.coder2 AS r2 ON r2.coid = g.sg2r\n"
						+ "INNER JOIN <pjschema>.coder3 AS r3 ON r3.coid = g.sg3r\n"
						+ "INNER JOIN <pjschema>.coder4 AS r4 ON r4.coid = g.sg4r\n"
						+ "INNER JOIN <pjschema>.procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid";
				break;
			case 15:
				sql = "CREATE VIEW <pjschema>.udvspecimaster AS\nSELECT m.smid, m.sgid, m.smnm,\n"
						+ "m.smdc, m.taid, t.grss, t.embd, t.micr, t.rout, t.finl, t.tanm,\n"
						+ "g.poid, g.sbid, g.sg1b, g.sg1m, g.sg1r, g.sg2b,\n"
						+ "g.sg2m, g.sg2r, g.sg3b, g.sg3m, g.sg3r, g.sg4b,\n"
						+ "g.sg4m, g.sg4r, g.sgv5, g.sgln, g.sgdc, b.syid,\n"
						+ "b.sbnm, b.sbdc, y.synm, r.ponm, b1.conm AS C1NB,\n"
						+ "m1.conm AS C1NM, r1.conm AS C1NR, b2.conm AS C2NB,\n"
						+ "m2.conm AS C2NM, r2.conm AS C2NR, b3.conm AS C3NB,\n"
						+ "m3.conm AS C3NM, r3.conm AS C3NR, b4.conm AS C4NB,\n"
						+ "m4.conm AS C4NM, r4.conm AS C4NR\n"
						+ "FROM <pjschema>.specimaster AS m\n"
						+ "INNER JOIN <pjschema>.turnaround AS t ON t.taid = m.taid\n"
						+ "INNER JOIN <pjschema>.specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN <pjschema>.coder1 AS b1 ON b1.coid = g.sg1b\n"
						+ "INNER JOIN <pjschema>.coder2 AS b2 ON b2.coid = g.sg2b\n"
						+ "INNER JOIN <pjschema>.coder3 AS b3 ON b3.coid = g.sg3b\n"
						+ "INNER JOIN <pjschema>.coder4 AS b4 ON b4.coid = g.sg4b\n"
						+ "INNER JOIN <pjschema>.coder1 AS m1 ON m1.coid = g.sg1m\n"
						+ "INNER JOIN <pjschema>.coder2 AS m2 ON m2.coid = g.sg2m\n"
						+ "INNER JOIN <pjschema>.coder3 AS m3 ON m3.coid = g.sg3m\n"
						+ "INNER JOIN <pjschema>.coder4 AS m4 ON m4.coid = g.sg4m\n"
						+ "INNER JOIN <pjschema>.coder1 AS r1 ON r1.coid = g.sg1r\n"
						+ "INNER JOIN <pjschema>.coder2 AS r2 ON r2.coid = g.sg2r\n"
						+ "INNER JOIN <pjschema>.coder3 AS r3 ON r3.coid = g.sg3r\n"
						+ "INNER JOIN <pjschema>.coder4 AS r4 ON r4.coid = g.sg4r\n"
						+ "INNER JOIN <pjschema>.procedures AS r ON r.poid = g.poid\n"
						+ "INNER JOIN <pjschema>.subspecial AS b ON b.sbid = g.sbid\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid";
				break;
			case 16:
				sql = "CREATE VIEW <pjschema>.udvspecimens AS\n"
						+ "SELECT s.spid, s.caid, s.smid, s.spbl, s.spsl, s.spfr, s.sphe,\n"
						+ "s.spss, s.spih, s.spmo, s.spv5, s.spv1, s.spv2, s.spv3, s.spv4,\n"
						+ "s.spdc, c.cano, m.sgid, m.smnm, m.smdc, g.sgdc, g.poid, r.ponm\n"
						+ "FROM <pjschema>.specimens AS s\n"
						+ "INNER JOIN <pjschema>.cases AS c ON c.caid = s.caid\n"
						+ "INNER JOIN <pjschema>.specimaster AS m ON m.smid = s.smid\n"
						+ "INNER JOIN <pjschema>.specigroups AS g ON g.sgid = m.sgid\n"
						+ "INNER JOIN <pjschema>.procedures AS r ON r.poid = g.poid";
				break;
			case 17:
				sql = "CREATE VIEW <pjschema>.udvsubspecial AS\n"
						+ "SELECT b.sbid, b.syid, b.sbnm, b.sbdc, y.syfl, y.syld, y.sysp, y.synm\n"
						+ "FROM <pjschema>.subspecial AS b\n"
						+ "INNER JOIN <pjschema>.specialties AS y ON y.syid = b.syid";
				break;
			case 18:
				if (dbID == DB_MARIA) {
					sql = "CREATE VIEW <pjschema>.udvcasesta AS\n"
							+ "SELECT faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "date_part('year', fned) AS fnyear, date_part('month', fned) AS fnmonth,\n"
							+ "COUNT(*) as CASES, SUM(CAST(grta AS INT)) AS grta,\n"
							+ "SUM(CAST(emta AS INT)) AS emta, SUM(CAST(mita AS INT)) AS mita,\n"
							+ "SUM(CAST(rota AS INT)) AS rota, SUM(CAST(fnta AS INT)) AS fnta\n"
							+ "FROM <pjschema>.udvcases\n"
							+ "GROUP BY faid, syid, sbid, poid, fanm, synm, sbnm, ponm, fnyear, fnmonth";
				} else if (dbID == DB_MSSQL) {
					sql = "CREATE VIEW <pjschema>.udvcasesta AS\n"
							+ "SELECT faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "DATEPART(year, fned) AS fnyear, DATEPART(month, fned) AS fnmonth,\n"
							+ "COUNT(*) as CASES, SUM(CAST(grta AS INT)) AS grta,\n"
							+ "SUM(CAST(emta AS INT)) AS emta, SUM(CAST(mita AS INT)) AS mita,\n"
							+ "SUM(CAST(rota AS INT)) AS rota, SUM(CAST(fnta AS INT)) AS fnta\n"
							+ "FROM <pjschema>.udvcases\n"
							+ "GROUP BY faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "DATEPART(year, fned), DATEPART(month, fned)";
				} else if (dbID == DB_POSTG) {
					sql = "CREATE VIEW <pjschema>.udvcasesta AS\n"
							+ "SELECT faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "EXTRACT(YEAR FROM fned) AS fnyear, EXTRACT(MONTH FROM fned) AS fnmonth,\n"
							+ "COUNT(*) as CASES, SUM(CAST(grta AS INT)) AS grta,\n"
							+ "SUM(CAST(emta AS INT)) AS emta, SUM(CAST(mita AS INT)) AS mita,\n"
							+ "SUM(CAST(rota AS INT)) AS rota, SUM(CAST(fnta AS INT)) AS fnta\n"
							+ "FROM <pjschema>.udvcases\n"
							+ "GROUP BY faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "EXTRACT(YEAR FROM fned), EXTRACT(MONTH FROM fned)";
				} else {
					sql = "CREATE VIEW <pjschema>.udvcasesta AS\n"
							+ "SELECT faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\n"
							+ "YEAR(fned) AS fnyear, MONTH(fned) AS fnmonth,\n"
							+ "COUNT(*) as CASES, SUM(CAST(grta AS INT)) AS grta,\n"
							+ "SUM(CAST(emta AS INT)) AS emta, SUM(CAST(mita AS INT)) AS mita,\n"
							+ "SUM(CAST(rota AS INT)) AS rota, SUM(CAST(fnta AS INT)) AS fnta\n"
							+ "FROM <pjschema>.udvcases\n"
							+ "GROUP BY faid, syid, sbid, poid, fanm, synm, sbnm, ponm,\nYEAR(fned), MONTH(fned)";
				}
				break;
			case 19:
				sql = "CREATE VIEW <pjschema>.udvworkdaylast AS\nSELECT wdid, wdno, wdtp, wddt\n"
						+ "FROM <pjschema>.workdays\nWHERE (wddt = (SELECT MAX(wddt) FROM <pjschema>.workdays))";
				break;
			default:
				sql = "CREATE VIEW <pjschema>.udvorgans AS\nSELECT s.ssoid, s.syid, s.sbid, s.onid, o.onnm\n"
						+ "FROM <pjschema>.sysbon AS s\nINNER JOIN <pjschema>.organs AS o ON o.onid = s.onid";
			}
			if (dbID == DB_DERBY) {
				sql = sql.replace("DATETIME", "TIMESTAMP");
			}
			execute(getSchema(sql));
			if (errorID != LibConstants.ERROR_NONE) {
				break;
			}
			noRows++;
			log(LibConstants.ERROR_NONE, "Created " + noRows + " views.");
		}
	}

	private void execute(String sql) {
		try {
			stm.executeUpdate(sql);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, "Exiting");
				System.exit(0);
			}
		} catch (IOException e) {
			log(LibConstants.ERROR_VARIABLE, "Unknown response");
			System.exit(1);
		}
	}

	private boolean getLogin() {
		LibCrypto crypto = new LibCrypto(appDir);
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
					log(LibConstants.ERROR_BINARY_FILE, "PJSetup", "Invalid application binary file");
				}
				return true;
			}
		}
		log(LibConstants.ERROR_NONE, "PJSetup", "No or invalid application binary file!");
		return false;
	}

	public String getSchema(String sql) {
		return sql.replaceAll("<pjschema>", dbSchema);
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
					log(LibConstants.ERROR_NONE, "Exiting");
					System.exit(0);
				}
				sysID = Integer.parseInt(input);
				if (sysID < 1 || sysID > 3) {
					log(LibConstants.ERROR_NONE, "Exiting");
					System.exit(0);
				}
				if (sysID == 2 || sysID == 3) {
					System.out.print("Select database architecture (1 = MariaDB, 2 = MSSql, 3 = PostgreSQL): ");
					input = br.readLine();
					if (input == null || input.trim().length() == 0) {
						log(LibConstants.ERROR_NONE, "Exiting");
						System.exit(0);
					}
					dbID = 1 + Integer.parseInt(input);
				} else {
					dbID = 1;
				}
				if (dbID < 1 || dbID > 4) {
					log(LibConstants.ERROR_NONE, "Exiting");
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
						log(LibConstants.ERROR_NONE, "Exiting");
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
						log(LibConstants.ERROR_NONE, "Exiting");
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
						log(LibConstants.ERROR_NONE, "Exiting");
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
				if (errorID == LibConstants.ERROR_NONE) {
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
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.print("Create the tables (y,n)? ");
				input = br.readLine();
				if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
					createTables();
				}
			}
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.print("Create the views (y,n)? ");
				input = br.readLine();
				if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
					createViews();
				}
			}
			if (errorID == LibConstants.ERROR_NONE) {
				if (dbID == DB_MSSQL || dbID == DB_MARIA) {
					System.out.print("Create the procedures (y,n)? ");
					input = br.readLine();
					if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
						createProcedures();
					}
				}
			}
			if (errorID == LibConstants.ERROR_NONE) {
				if (!getLogin()) {
					System.out.print("Create the autologin (y,n)? ");
					input = br.readLine();
					if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
						if (dbID != DB_DERBY) {
							System.out.print("Select database architecture (1 = MariaDB, 2 = MSSql, 3 = PostgreSQL): ");
							input = br.readLine();
							if (input == null || input.trim().length() == 0) {
								log(LibConstants.ERROR_NONE, "Exiting");
								System.exit(0);
							}
							dbID = 1 + Integer.parseInt(input);
						}
						createLogin();
					} else {
						log(LibConstants.ERROR_NONE, "Exiting");
						System.exit(0);
					}
				}
			}
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.print("Load the data (y,n)? ");
				input = br.readLine();
				if (input != null && input.trim().length() == 1 && input.trim().toLowerCase().equals("y")) {
					loadTables();
				}
			}
			if (errorID == LibConstants.ERROR_NONE) {
				System.out.println("Installation completed successfully.");
			}
		} catch (SecurityException e) {
			log(LibConstants.ERROR_UNEXPECTED, "PJSetup", e);
		} catch (NumberFormatException e) {
			log(LibConstants.ERROR_VARIABLE, "PJSetup", e);
		} catch (IOException e) {
			log(LibConstants.ERROR_VARIABLE, "PJSetup", e);
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
				InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/" + tables[i] + ".txt");
				if (is != null) {
					InputStreamReader ir = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(ir);
					PreparedStatement pstm = connection.prepareStatement("INSERT INTO " + tables[i]
							+ " (coid, ruid, coqy, cov1, cov2, cov3, conm, codc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
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
					log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to %s Table.", noRows, tables[i]));
					pstm.close();
					br.close();
					ir.close();
					is.close();
				}
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadDiagnosis() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/diagnosis.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO diagnosis (dgid, dsid, dgcu, dgnm) VALUES (?, ?, ?, ?)");
				String line;
				String[] columns = null;
				noRows = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 1) {
						if (!line.startsWith("-")) {
							columns = line.split("\t");
							pstm.setInt(1, Integer.valueOf(columns[0]));
							pstm.setShort(2, Short.valueOf(columns[1]));
							pstm.setString(3, columns[2]);
							pstm.setString(4, columns[3]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Diagnosis Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadDiagnosisTissue() {
		int elementID = 0, diagnosisID = 1, tissueID = 2, diagnosisText = 3, microText = 4;
		String strText = "";
		String eventName = "";
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/diagnosistissue.txt");
			if (is != null) {
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO diagnosistissue (dgid, tiid, dgdc, midc) VALUES (?, ?, ?, ?)");
				XMLInputFactory factory = XMLInputFactory.newInstance();
				XMLStreamReader reader = factory.createXMLStreamReader(is);
				for (int event = reader.next(); event != XMLStreamConstants.END_DOCUMENT; event = reader.next()) {
					switch (event) {
					case XMLStreamConstants.START_ELEMENT:
						strText = "";
						eventName = reader.getLocalName();
						if (eventName.equals("DiagnosisID")) {
							elementID = diagnosisID;
						} else if (eventName.equals("TissueID")) {
							elementID = tissueID;
						} else if (eventName.equals("DiagnosisText")) {
							elementID = diagnosisText;
						} else if (eventName.equals("MicroText")) {
							elementID = microText;
						} else {
							elementID = 0;
						}
						break;
					case XMLStreamConstants.END_ELEMENT:
						eventName = reader.getLocalName();
						if (eventName.equals("DiagnosisID") && elementID == diagnosisID) {
							pstm.setInt(1, Integer.valueOf(strText));
						} else if (eventName.equals("TissueID") && elementID == tissueID) {
							pstm.setShort(2, Short.valueOf(strText));
						} else if (eventName.equals("DiagnosisText") && elementID == diagnosisText) {
							if (strText.length() > 2048) {
								strText = strText.substring(0, 2048);
							}
							pstm.setString(3, strText);
						} else if (eventName.equals("MicroText") && elementID == microText) {
							if (strText.length() > 2048) {
								strText = strText.substring(0, 2048);
							}
							pstm.setString(4, strText);
							pstm.executeUpdate();
							noRows++;
						} else {
							elementID = 0;
						}
						break;
					case XMLStreamConstants.CHARACTERS:
						strText += reader.getText();
						break;
					default:
						// Wait for next pass
					}
				}
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to DiagnosisTissue Table.", noRows));
				pstm.close();
				reader.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_FILE_NOT_FOUND, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_NULL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_IO, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (XMLStreamException e) {
			log(LibConstants.ERROR_IMPORT, dbName, e);
		}
	}

	private void loadDiseases() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/diseases.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO diseases (dsid, dscu, dsnm) VALUES (?, ?, ?)");
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Diseases Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Order Groups Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Order Types Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadOrgans() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/organs.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO organs (onid, oncu, onnm) VALUES (?, ?, ?)");
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Organs Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Procedures Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Rules Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Setup Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Specialties Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Specimen Groups Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadSpySubOrgans() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/spysuborg.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO spysuborg (syid, sbid, onid) VALUES (?, ?, ?)");
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
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to SpySubOrgans Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Subspecialties Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadTables() {
		for (int i = 0; i < 18; i++) {
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
			case 10:
				loadSpecimenGroups();
				break;
			case 11:
				loadOrgans();
				break;
			case 12:
				loadSpySubOrgans();
				break;
			case 13:
				loadTissues();
				break;
			case 14:
				loadTissueParts();
				break;
			case 15:
				loadDiseases();
				break;
			case 16:
				loadDiagnosis();
				break;
			default:
				loadDiagnosisTissue();
			}
		}
	}

	private void loadTissueParts() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/tissueparts.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection
						.prepareStatement("INSERT INTO tissueparts (paid, tiid) VALUES (?, ?)");
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
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to TissueParts Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	private void loadTissues() {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("db/tissues.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				PreparedStatement pstm = connection.prepareStatement("INSERT INTO tissues (tiid, syid, "
						+ "onid, poid, ticx, ticu, tinm, tidc, podc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
							pstm.setString(5, columns[4]);
							pstm.setString(6, columns[5]);
							pstm.setString(7, columns[6]);
							pstm.setString(8, columns[7]);
							pstm.setString(9, columns[8]);
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Tissues Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Turnaround Time Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
				log(LibConstants.ERROR_NONE, String.format("Loaded %d rows to Workdays Table.", noRows));
				pstm.close();
				br.close();
				ir.close();
				is.close();
			}
		} catch (FileNotFoundException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (NullPointerException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (IOException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	private void log(byte severity, String message) {
		log(severity, "PJSetup", message);
	}

	private void log(byte severity, String name, String message) {
		if (severity > LibConstants.ERROR_NONE) {
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
			log(LibConstants.ERROR_SQL, dbName, e);
		}
	}

	private void setMaria() {
		try {
			String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbSchema
					+ "?autoReconnect=true&useUnicode=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection(url, dbUser, dbPass);
			stm = connection.createStatement();
			execute("USE " + dbSchema);
			dbName = "Maria";
		} catch (IllegalArgumentException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SecurityException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
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
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SecurityException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		} catch (SQLException e) {
			log(LibConstants.ERROR_SQL, dbName, e);
		}
	}
}