package ca.powerj;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;

import javax.swing.JFileChooser;

class LBackup {
	private int noRows = 0;
	private int fileNo = 1;
	private final String tab = "\t";
	private String dataDir = "";
	private String fileName = "";
	private String input = "";
	private String output = "";
	private String[] columns = null;
	private File file = null;
	private FileOutputStream fos = null;
	private Scanner scanner = null;
	private ResultSet rst = null;
	private PreparedStatement pstm = null;
	protected AClient pj;

	LBackup(AClient pj) {
		this.pj = pj;
	}

	void backup() {
		try {
			pj.setBusy(true);
			if (getDir()) {
				for (int i = 0; i < 26; i++) {
					switch (i) {
					case 0:
						backupSetup();
						break;
					case 1:
						backupWorkdays();
						break;
					case 2:
						backupFacilities();
						break;
					case 3:
						backupPersons();
						break;
					case 4:
						backupProcedures();
						break;
					case 5:
						backupRules();
						break;
					case 6:
						backupSpecialties();
						break;
					case 7:
						backupTurnaround();
						break;
					case 8:
						backupCoders();
						break;
					case 9:
						backupSubspecial();
						break;
					case 10:
						backupOrderTypes();
						break;
					case 11:
						backupOrderGroups();
						break;
					case 12:
						backupSpeciGroups();
						break;
					case 13:
						backupAccessions();
						break;
					case 14:
						backupOrderMaster();
						break;
					case 15:
						backupSpeciMaster();
						break;
					case 16:
						backupServices();
						break;
					case 17:
						backupSchedules();
						break;
					case 18:
						backupPending();
						break;
					case 19:
						backupCases();
						break;
					case 20:
						backupSpecimens();
						break;
					case 21:
						backupOrders();
						break;
					case 22:
						backupFrozens();
						break;
					case 23:
						backupAdditionals();
						break;
					case 24:
						backupComments();
						break;
					default:
						backupErrors();
					}
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
				pj.log(LConstants.ERROR_NONE, "Backup completed successfully.");
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Backup", e);
		} catch (IOException e) {
			pj.log(LConstants.ERROR_IO, "Backup", e);
		} catch (Exception e) {
			pj.log(LConstants.ERROR_UNEXPECTED, "Backup", e);
		} finally {
			try {
				rst.close();
			} catch (Exception e) {
			}
			try {
				fos.close();
			} catch (Exception e) {
			}
			pj.setBusy(false);
		}
	}

	private void backupAccessions() throws SQLException, IOException {
		fileName = dataDir + "accessions.txt";
		input = "%d\t%d\t%s\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Accessions ORDER BY ACID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("ACID"), rst.getShort("SYID"), rst.getString("ACFL"),
						rst.getString("ACLD"), rst.getString("ACNM"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Accessions Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupAdditionals() throws SQLException, IOException {
		fileName = dataDir + "additionals.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%f\t%f\t%f\t%f\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Additionals ORDER BY CAID, PRID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("CAID"), rst.getShort("PRID"), rst.getShort("ADCD"),
						rst.getInt("ADV5"), rst.getTimestamp("ADDT").getTime(), rst.getDouble("ADV1"),
						rst.getDouble("ADV2"), rst.getDouble("ADV3"), rst.getDouble("ADV4"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Additionals Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupCases() throws SQLException, IOException {
		fileName = dataDir + "cases.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t"
				+ "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%f\t%f\t%f\t%f\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Cases ORDER BY CAID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("CAID"), rst.getShort("FAID"), rst.getShort("SBID"),
						rst.getShort("SMID"), rst.getShort("GRID"), rst.getShort("EMID"), rst.getShort("MIID"),
						rst.getShort("ROID"), rst.getShort("FNID"), rst.getShort("GRTA"), rst.getShort("EMTA"),
						rst.getShort("MITA"), rst.getShort("ROTA"), rst.getShort("FNTA"), rst.getShort("CASP"),
						rst.getShort("CABL"), rst.getShort("CASL"), rst.getShort("CASY"), rst.getShort("CAFS"),
						rst.getShort("CAHE"), rst.getShort("CASS"), rst.getShort("CAIH"), rst.getShort("CAMO"),
						rst.getShort("CAV5"), rst.getTimestamp("ACED").getTime(), rst.getTimestamp("GRED").getTime(),
						rst.getTimestamp("EMED").getTime(), rst.getTimestamp("MIED").getTime(),
						rst.getTimestamp("ROED").getTime(), rst.getTimestamp("FNED").getTime(), rst.getDouble("CAV1"),
						rst.getDouble("CAV2"), rst.getDouble("CAV3"), rst.getDouble("CAV4"), rst.getString("CANO"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Cases Table.", noRows));
		}
		rst.close();
		fos.close();
		try {
			Thread.sleep(LConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	private void backupCoders() throws SQLException, IOException {
		String[] tables = { "Coder1", "Coder2", "Coder3", "Coder4" };
		input = "%d\t%d\t%d\t%f\t%f\t%f\t%s\t%s\n";
		for (int i = 0; i < 4; i++) {
			fileName = dataDir + tables[i].toLowerCase() + ".txt";
			rst = pj.dbPowerJ.getResultSet("SELECT * FROM " + tables[i] + " ORDER BY COID");
			file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				while (rst.next()) {
					output = String.format(input, rst.getShort("COID"), rst.getShort("RUID"), rst.getShort("COQY"),
							rst.getDouble("COV1"), rst.getDouble("COV2"), rst.getDouble("COV3"), rst.getString("CONM"),
							rst.getString("CODC"));
					fos.write(output.getBytes());
					noRows++;
				}
				pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from %s Table.", noRows, tables[i]));
			}
			rst.close();
			fos.close();
		}
	}

	private void backupComments() throws SQLException, IOException {
		fileName = dataDir + "comments.txt";
		input = "%d\t%s\t%s\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Comments ORDER BY CAID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("CAID"), formatLines(rst.getString("COM1")),
						formatLines(rst.getString("COM2")), formatLines(rst.getString("COM3")),
						formatLines(rst.getString("COM4")));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Comments Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupErrors() throws SQLException, IOException {
		fileName = dataDir + "errors.txt";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Errors ORDER BY CAID");
		input = "%d\t%d\t%s\t%s\n";
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("CAID"), rst.getShort("ERID"), rst.getString("CANO"),
						formatLines(rst.getString("ERDC")));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Errors Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupFacilities() throws SQLException, IOException {
		fileName = dataDir + "facilities.txt";
		input = "%d\t%s\t%s\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Facilities ORDER BY FAID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("FAID"), rst.getString("FAFL"), rst.getString("FALD"),
						rst.getString("FANM"), rst.getString("FADC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Facilities Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupFrozens() throws SQLException, IOException {
		fileName = dataDir + "frozens.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%f\t%f\t%f\t%f\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Frozens ORDER BY SPID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("SPID"), rst.getShort("PRID"), rst.getShort("FRBL"),
						rst.getShort("FRSL"), rst.getInt("FRV5"), rst.getDouble("FRV1"), rst.getDouble("FRV2"),
						rst.getDouble("FRV3"), rst.getDouble("FRV4"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Frozens Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupOrderGroups() throws SQLException, IOException {
		fileName = dataDir + "ordergroups.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM OrderGroups ORDER BY OGID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("OGID"), rst.getShort("OTID"), rst.getShort("OGC1"),
						rst.getShort("OGC2"), rst.getShort("OGC3"), rst.getShort("OGC4"), rst.getInt("OGC5"),
						rst.getString("OGNM"), rst.getString("OGDC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Order Groups Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupOrderMaster() throws SQLException, IOException {
		fileName = dataDir + "ordermaster.txt";
		input = "%d\t%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM OrderMaster ORDER BY OMID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("OMID"), rst.getShort("OGID"), rst.getString("OMNM"),
						rst.getString("OMDC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Order Master Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupOrderTypes() throws SQLException, IOException {
		fileName = dataDir + "ordertypes.txt";
		input = "%d\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM OrderTypes ORDER BY OTID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("OTID"), rst.getString("OTNM"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Order Types Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupOrders() throws SQLException, IOException {
		fileName = dataDir + "orders.txt";
		input = "%d\t%d\t%d\t%f\t%f\t%f\t%f\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Orders ORDER BY SPID, OGID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("SPID"), rst.getShort("OGID"), rst.getShort("ORQY"),
						rst.getDouble("ORV1"), rst.getDouble("ORV2"), rst.getDouble("ORV3"), rst.getDouble("ORV4"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Orders Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupPending() throws SQLException, IOException {
		fileName = dataDir + "pending.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Pending ORDER BY PNID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("PNID"), rst.getShort("FAID"), rst.getShort("SBID"),
						rst.getShort("SMID"), rst.getShort("POID"), rst.getShort("GRID"), rst.getShort("EMID"),
						rst.getShort("MIID"), rst.getShort("ROID"), rst.getShort("FNID"), rst.getShort("GRTA"),
						rst.getShort("EMTA"), rst.getShort("MITA"), rst.getShort("ROTA"), rst.getShort("FNTA"),
						rst.getShort("PNST"), rst.getShort("PNSP"), rst.getShort("PNBL"), rst.getShort("PNSL"),
						rst.getInt("PNV5"), rst.getTimestamp("ACED").getTime(), rst.getTimestamp("GRED").getTime(),
						rst.getTimestamp("EMED").getTime(), rst.getTimestamp("MIED").getTime(),
						rst.getTimestamp("ROED").getTime(), rst.getTimestamp("FNED").getTime(), rst.getString("PNNO"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Pending Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupPersons() throws SQLException, IOException {
		fileName = dataDir + "persons.txt";
		input = "%d\t%d\t%d\t%s\t%s\t%s\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Persons ORDER BY PRID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("PRID"), rst.getInt("PRVL"), rst.getDate("PRDT").getTime(),
						rst.getString("PRCD"), rst.getString("PRAC"), rst.getString("PRNM"), rst.getString("PRLS"),
						rst.getString("PRFR"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Persons Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupProcedures() throws SQLException, IOException {
		fileName = dataDir + "procedures.txt";
		input = "%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Procedures ORDER BY POID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("POID"), rst.getString("PONM"), rst.getString("PODC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Procedures Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupRules() throws SQLException, IOException {
		fileName = dataDir + "rules.txt";
		input = "%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Rules ORDER BY RUID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("RUID"), rst.getString("RUNM"), rst.getString("RUDC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Rules Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupSchedules() throws SQLException, IOException {
		fileName = dataDir + "schedules.txt";
		input = "%d\t%d\t%d\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Schedules ORDER BY WDID, SRID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getInt("WDID"), rst.getShort("PRID"), rst.getShort("SRID"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Schedules Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupServices() throws SQLException, IOException {
		fileName = dataDir + "services.txt";
		input = "%d\t%d\t%d\t%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Services ORDER BY SRID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("SRID"), rst.getShort("FAID"), rst.getShort("SBID"),
						rst.getShort("SRCD"), rst.getString("SRNM"), rst.getString("SRDC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Services Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupSetup() throws SQLException, IOException {
		fileName = dataDir + "setup.txt";
		input = "%d\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Setup ORDER BY STID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("STID"), rst.getString("STVA"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Setup Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupSpecialties() throws SQLException, IOException {
		fileName = dataDir + "specialties.txt";
		input = "%d\t%s\t%s\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Specialties ORDER BY SYID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("SYID"), rst.getString("SYFL"), rst.getString("SYLD"),
						rst.getString("SYSP"), rst.getString("SYNM"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Specialties Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupSpecimens() throws SQLException, IOException {
		fileName = dataDir + "specimens.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%f\t%f\t%f\t%f\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Specimens ORDER BY SPID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("SPID"), rst.getLong("CAID"), rst.getShort("SMID"),
						rst.getShort("SPBL"), rst.getShort("SPSL"), rst.getShort("SPFR"), rst.getShort("SPHE"),
						rst.getShort("SPSS"), rst.getShort("SPIH"), rst.getShort("SPMO"), rst.getInt("SPV5"),
						rst.getDouble("SPV1"), rst.getDouble("SPV2"), rst.getDouble("SPV3"), rst.getDouble("SPV4"),
						rst.getString("SPDC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Specimens Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupSpeciGroups() throws SQLException, IOException {
		fileName = dataDir + "specimengroups.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM SpeciGroups ORDER BY SGID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("SGID"), rst.getShort("SBID"), rst.getShort("POID"),
						rst.getShort("SG1B"), rst.getShort("SG1M"), rst.getShort("SG1R"), rst.getShort("SG2B"),
						rst.getShort("SG2M"), rst.getShort("SG2R"), rst.getShort("SG3B"), rst.getShort("SG3M"),
						rst.getShort("SG3R"), rst.getShort("SG4B"), rst.getShort("SG4M"), rst.getShort("SG4R"),
						rst.getInt("SGV5"), rst.getString("SGLN"), rst.getString("SGDC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Specimen Groups Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupSpeciMaster() throws SQLException, IOException {
		fileName = dataDir + "specimenmaster.txt";
		input = "%d\t%d\t%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM SpeciMaster ORDER BY SMID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("SMID"), rst.getShort("SGID"), rst.getShort("TAID"),
						rst.getString("SMNM"), rst.getString("SMDC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Specimen Master Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupSubspecial() throws SQLException, IOException {
		fileName = dataDir + "subspecialties.txt";
		input = "%d\t%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Subspecial ORDER BY SBID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("SBID"), rst.getShort("SYID"), rst.getString("SBNM"),
						rst.getString("SBDC"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Subspecialties Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupTurnaround() throws SQLException, IOException {
		fileName = dataDir + "turnaround.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%d\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Turnaround ORDER BY TAID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("TAID"), rst.getShort("GRSS"), rst.getShort("EMBD"),
						rst.getShort("MICR"), rst.getShort("ROUT"), rst.getShort("FINL"), rst.getString("TANM"));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Turnaround Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupWorkdays() throws SQLException, IOException {
		fileName = dataDir + "workdays.txt";
		input = "%d\t%d\t%s\t%d\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM Workdays ORDER BY WDID");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getInt("WDID"), rst.getInt("WDNO"), rst.getString("WDTP"),
						rst.getDate("WDDT").getTime());
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Workdays Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private boolean getDir() {
		boolean success = false;
		final String fs = System.getProperty("file.separator");
		final JFileChooser fc = new JFileChooser();
		try {
			dataDir = pj.defaults.getString("datadir", pj.appDir);
			File file = new File(dataDir);
			fc.setSelectedFile(file);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int val = fc.showOpenDialog(pj.frame);
			if (val == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				if (file.isDirectory()) {
					if (!dataDir.equals(file.getAbsolutePath())) {
						dataDir = file.getAbsolutePath();
						pj.defaults.setString("datadir", dataDir);
					}
					if (!dataDir.endsWith(fs)) {
						dataDir += fs;
					}
					success = true;
				}
			}
		} catch (HeadlessException ignore) {
		}
		return success;
	}

	private String formatLines(String in) {
		return in.replaceAll("\\", "\\\\");
	}

	private void delete() throws SQLException, FileNotFoundException, NumberFormatException {
		pj.log(LConstants.ERROR_NONE, "Packing tables.");
		for (int i = 0; i < 29; i++) {
			switch (i) {
			case 0:
				input = "DELETE FROM Errors";
				break;
			case 1:
				input = "DELETE FROM Comments";
				break;
			case 2:
				input = "DELETE FROM Additionals";
				break;
			case 3:
				input = "DELETE FROM Frozens";
				break;
			case 4:
				input = "DELETE FROM Orders";
				break;
			case 5:
				input = "DELETE FROM Specimens";
				break;
			case 6:
				input = "DELETE FROM Cases";
				break;
			case 7:
				input = "DELETE FROM Pending";
				break;
			case 8:
				input = "DELETE FROM Schedules";
				break;
			case 9:
				input = "DELETE FROM Services";
				break;
			case 10:
				input = "DELETE FROM SpeciMaster";
				break;
			case 11:
				input = "DELETE FROM OrderMaster";
				break;
			case 12:
				input = "DELETE FROM OrderGroups";
				break;
			case 13:
				input = "DELETE FROM OrderTypes";
				break;
			case 14:
				input = "DELETE FROM SpeciGroups";
				break;
			case 15:
				input = "DELETE FROM Accessions";
				break;
			case 16:
				input = "DELETE FROM Subspecial";
				break;
			case 17:
				input = "DELETE FROM Coder4";
				break;
			case 18:
				input = "DELETE FROM Coder3";
				break;
			case 19:
				input = "DELETE FROM Coder2";
				break;
			case 20:
				input = "DELETE FROM Coder1";
				break;
			case 21:
				input = "DELETE FROM Turnaround";
				break;
			case 22:
				input = "DELETE FROM Specialties";
				break;
			case 23:
				input = "DELETE FROM Rules";
				break;
			case 24:
				input = "DELETE FROM Procedures";
				break;
			case 25:
				input = "DELETE FROM Persons";
				break;
			case 26:
				input = "DELETE FROM Facilities";
				break;
			case 27:
				input = "DELETE FROM Workdays";
				break;
			default:
				input = "DELETE FROM Setup";
			}
			pj.dbPowerJ.execute(input);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
		}
	}

	void restore() {
		try {
			pj.setBusy(true);
			if (getDir()) {
//				delete();
				for (int i = 0; i < 26; i++) {
					switch (i) {
					case 0:
//						restoreSetup();
						break;
					case 1:
//						restoreWorkdays();
						break;
					case 2:
						restoreFacilities();
						break;
					case 3:
						restorePersons();
						break;
					case 4:
//						restoreProcedures();
						break;
					case 5:
//						restoreRules();
						break;
					case 6:
//						restoreSpecialties();
						break;
					case 7:
//						restoreTurnaround();
						break;
					case 8:
//						restoreCoders();
						break;
					case 9:
//						restoreSubspecial();
						break;
					case 10:
//						restoreOrderTypes();
						break;
					case 11:
//						restoreOrderGroups();
						break;
					case 12:
//						restoreSpeciGroups();
						break;
					case 13:
						restoreAccessions();
						break;
					case 14:
						restoreOrderMaster();
						break;
					case 15:
						restoreSpeciMaster();
						break;
					case 16:
						restoreServices();
						break;
					case 17:
						restoreSchedules();
						break;
					case 18:
//						restorePending();
						break;
					case 19:
//						restoreCases();
						break;
					case 20:
//						restoreSpecimens();
						break;
					case 21:
//						restoreOrders();
						break;
					case 22:
//						restoreFrozens();
						break;
					case 23:
//						restoreAdditionals();
						break;
					case 24:
//						restoreComments();
						break;
					default:
//						restoreErrors();
					}
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
				pj.log(LConstants.ERROR_NONE, "Restore completed successfully.");
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Backup", e);
		} catch (NumberFormatException e) {
			pj.log(LConstants.ERROR_NUMBER_FORMAT, "Backup", e);
		} catch (NullPointerException e) {
			pj.log(LConstants.ERROR_NULL, "Backup", e);
		} catch (FileNotFoundException e) {
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, "Backup", e);
		} catch (Exception e) {
			pj.log(LConstants.ERROR_UNEXPECTED, "Backup", e);
		} finally {
			try {
				pstm.close();
			} catch (Exception e) {
			}
			scanner.close();
			pj.setBusy(false);
		}
	}

	private void restoreAccessions() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "accessions.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ
					.prepareStatement("INSERT INTO Accessions (ACID, SYID, ACFL, ACLD, ACNM) VALUES (?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setString(3, columns[2]);
						pstm.setString(4, columns[3]);
						pstm.setString(5, columns[4]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Accessions Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreAdditionals() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "additionals.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Additionals (CAID, PRID, ADCD, ADV5, ADDT, ADV1, "
					+ "ADV2, ADV3, ADV4) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setShort(3, Short.valueOf(columns[2]));
						pstm.setInt(4, Integer.valueOf(columns[3]));
						pstm.setTimestamp(5, new Timestamp(Long.valueOf(columns[4])));
						pstm.setDouble(6, Double.valueOf(columns[5]));
						pstm.setDouble(7, Double.valueOf(columns[6]));
						pstm.setDouble(8, Double.valueOf(columns[7]));
						pstm.setDouble(9, Double.valueOf(columns[8]));
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %1$8d rows to Additionals Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreCases() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "cases.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Cases (CAID, FAID, SBID, SMID, GRID, "
					+ "EMID, MIID, ROID, FNID, GRTA, EMTA, MITA, ROTA, FNTA, CASP, CABL, CASL, CASY, "
					+ "CAFS, CAHE, CASS, CAIH, CAMO, CAV5, ACED, GRED, EMED, MIED, ROED, FNED, CAV1, "
					+ "CAV2, CAV3, CAV4, CANO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[0]));
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
						pstm.setShort(16, Short.valueOf(columns[15]));
						pstm.setShort(17, Short.valueOf(columns[16]));
						pstm.setShort(18, Short.valueOf(columns[17]));
						pstm.setShort(19, Short.valueOf(columns[18]));
						pstm.setShort(20, Short.valueOf(columns[19]));
						pstm.setShort(21, Short.valueOf(columns[20]));
						pstm.setShort(22, Short.valueOf(columns[21]));
						pstm.setShort(23, Short.valueOf(columns[22]));
						pstm.setShort(24, Short.valueOf(columns[23]));
						pstm.setTimestamp(25, new Timestamp(Long.valueOf(columns[24])));
						pstm.setTimestamp(26, new Timestamp(Long.valueOf(columns[25])));
						pstm.setTimestamp(27, new Timestamp(Long.valueOf(columns[26])));
						pstm.setTimestamp(28, new Timestamp(Long.valueOf(columns[27])));
						pstm.setTimestamp(29, new Timestamp(Long.valueOf(columns[28])));
						pstm.setTimestamp(30, new Timestamp(Long.valueOf(columns[29])));
						pstm.setDouble(31, Double.valueOf(columns[30]));
						pstm.setDouble(32, Double.valueOf(columns[31]));
						pstm.setDouble(33, Double.valueOf(columns[32]));
						pstm.setDouble(34, Double.valueOf(columns[33]));
						pstm.setString(35, columns[34]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Cases Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreCoders() throws SQLException, FileNotFoundException, NumberFormatException {
		final String[] tables = { "Coder1", "Coder2", "Coder3", "Coder4" };
		for (int i = 0; i < 4; i++) {
			fileName = dataDir + tables[i].toLowerCase() + ".txt";
			file = new File(fileName);
			if (file.exists()) {
				noRows = 0;
				scanner = new Scanner(new FileReader(fileName));
				pstm = pj.dbPowerJ.prepareStatement("INSERT INTO " + tables[i]
						+ " (COID, RUID, COQY, COV1, COV2, COV3, CONM, CODC) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
				while (scanner.hasNextLine()) {
					input = scanner.nextLine();
					if (input.length() > 1) {
						if (!input.startsWith("-")) {
							columns = input.split(tab);
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
				pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to %s Table.", noRows, tables[i]));
				scanner.close();
				pstm.close();
			}
		}
	}

	private void restoreComments() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "comments.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			pstm = pj.dbPowerJ.prepareStatement(
					"INSERT INTO Comments (CAID, COM1, COM2, COM3, COM4) " + "VALUES (?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[0]));
						pstm.setString(2, unformatLines(columns[1]));
						pstm.setString(3, unformatLines(columns[2]));
						pstm.setString(4, unformatLines(columns[3]));
						pstm.setString(5, unformatLines(columns[4]));
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Comments Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreErrors() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "errors.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Errors (CAID, ERID, CANO, ERDC) VALUES (?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setString(3, columns[2]);
						pstm.setString(4, unformatLines(columns[3]));
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %1$4d rows to Errors Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreFacilities() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "facilities.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ
					.prepareStatement("INSERT INTO Facilities (FAID, FAFL, FALD, FANM, FADC) VALUES (?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
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
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Facilities Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreFrozens() throws SQLException, FileNotFoundException, NumberFormatException {
		fileNo = 1;
		fileName = dataDir + "frozens" + fileNo + ".txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Frozens (SPID, PRID, FRBL, FRSL, FRV5, "
					+ "FRV1, FRV2, FRV3, FRV4) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[0]));
						pstm.setInt(2, Short.valueOf(columns[1]));
						pstm.setShort(3, Short.valueOf(columns[2]));
						pstm.setShort(4, Short.valueOf(columns[3]));
						pstm.setInt(5, Short.valueOf(columns[4]));
						pstm.setDouble(6, Double.valueOf(columns[5]));
						pstm.setDouble(7, Double.valueOf(columns[6]));
						pstm.setDouble(8, Double.valueOf(columns[7]));
						pstm.setDouble(9, Double.valueOf(columns[8]));
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to frozens Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreOrderGroups() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "ordergroups.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO OrderGroups (OGID, OTID, OGC1, OGC2, OGC3, "
					+ "OGC4, OGC5, OGNM, OGDC) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
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
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Order Groups Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreOrderMaster() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "ordermaster.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO OrderMaster (OMID, OGID, OMNM, OMDC) VALUES (?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setString(3, columns[2]);
						pstm.setString(4, columns[3]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Order Master Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreOrders() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "orders.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			pstm = pj.dbPowerJ.prepareStatement(
					"INSERT INTO Orders (SPID, OGID, ORQY, ORV1, ORV2, " + "ORV3, ORV4) VALUES (?, ?, ?, ?, ?, ?, ?)");
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setShort(3, Short.valueOf(columns[2]));
						pstm.setDouble(4, Double.valueOf(columns[3]));
						pstm.setDouble(5, Double.valueOf(columns[4]));
						pstm.setDouble(6, Double.valueOf(columns[5]));
						pstm.setDouble(7, Double.valueOf(columns[6]));
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Orders Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreOrderTypes() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "ordertypes.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO OrderTypes (OTID, OTNM) VALUES (?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setString(2, columns[1]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Order Types Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restorePending() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "pending.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Pending (PNID, FAID, SBID, SMID, POID, "
					+ "GRID, EMID, MIID, ROID, FNID, GRTA, EMTA, MITA, ROTA, FNTA, PNST, PNSP, PNBL, "
					+ "PNSL, PNV5, ACED, GRED, EMED, MIED, ROED, FNED, PNNO) VALUES (?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[0]));
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
						pstm.setShort(16, Short.valueOf(columns[15]));
						pstm.setShort(17, Short.valueOf(columns[16]));
						pstm.setShort(18, Short.valueOf(columns[17]));
						pstm.setShort(19, Short.valueOf(columns[18]));
						pstm.setInt(20, Integer.valueOf(columns[19]));
						pstm.setTimestamp(21, new Timestamp(Long.valueOf(columns[20])));
						pstm.setTimestamp(22, new Timestamp(Long.valueOf(columns[21])));
						pstm.setTimestamp(23, new Timestamp(Long.valueOf(columns[22])));
						pstm.setTimestamp(24, new Timestamp(Long.valueOf(columns[23])));
						pstm.setTimestamp(25, new Timestamp(Long.valueOf(columns[24])));
						pstm.setTimestamp(26, new Timestamp(Long.valueOf(columns[25])));
						pstm.setString(27, columns[26]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Pending Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restorePersons() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "persons.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Persons (PRID, PRVL, PRDT, PRCD, PRAC, "
					+ "PRNM, PRLS, PRFR) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setInt(2, Integer.valueOf(columns[1]));
						pstm.setDate(3, new Date(Long.valueOf(columns[2])));
						pstm.setString(4, columns[3]);
						pstm.setString(5, columns[4]);
						pstm.setString(6, columns[5]);
						pstm.setString(7, columns[6]);
						pstm.setString(8, columns[7]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %1$3d rows to Persons Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreProcedures() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "procedures.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Procedures (POID, PONM, PODC) VALUES (?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setString(2, columns[1]);
						pstm.setString(3, columns[2]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Procedures Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreRules() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "rules.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Rules (RUID, RUNM, RUDC) VALUES (?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setString(2, columns[1]);
						pstm.setString(3, columns[2]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %1$d rows to Rules Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreSchedules() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "schedules.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Schedules (WDID, PRID, SRID) VALUES (?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setInt(1, Integer.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setShort(3, Short.valueOf(columns[2]));
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Schedules Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreServices() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "services.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement(
					"INSERT INTO Services (SRID, FAID, SBID, SRCD, SRNM, SRDC) VALUES (?, ?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setShort(3, Short.valueOf(columns[2]));
						pstm.setShort(4, Short.valueOf(columns[3]));
						pstm.setString(5, columns[4]);
						pstm.setString(6, columns[5]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Services Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreSetup() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "setup.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Setup (STID, STVA) VALUES (?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setString(2, columns[1]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Setup Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreSpecialties() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "specialties.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ
					.prepareStatement("INSERT INTO Specialties (SYID, SYFL, SYLD, SYSP, SYNM) VALUES (?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
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
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Specialties Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreSpeciGroups() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "specimengroups.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO SpeciGroups (SGID, SBID, POID, SG1B, SG1M, "
					+ "SG1R, SG2B, SG2M, SG2R, SG3B, SG3M, SG3R, SG4B, SG4M, SG4R, SGV5, SGLN, SGDC) VALUES "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
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
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Specimen Groups Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreSpeciMaster() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "specimenmaster.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ
					.prepareStatement("INSERT INTO SpeciMaster (SMID, SGID, TAID, SMNM, SMDC) VALUES (?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setShort(3, Short.valueOf(columns[2]));
						pstm.setString(4, columns[3]);
						pstm.setString(5, columns[4]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Specimen Master Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreSpecimens() throws SQLException, FileNotFoundException, NumberFormatException {
		fileNo = 1;
		fileName = dataDir + "specimens" + fileNo + ".txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Specimens (SPID, CAID, SMID, SPBL, SPSL, "
					+ "SPFR, SPHE, SPSS, SPIH, SPMO, SPV5, SPV1, SPV2, SPV3, SPV4, SPDC) VALUES (?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[0]));
						pstm.setLong(2, Long.valueOf(columns[1]));
						pstm.setShort(3, Short.valueOf(columns[2]));
						pstm.setShort(4, Short.valueOf(columns[3]));
						pstm.setShort(5, Short.valueOf(columns[4]));
						pstm.setShort(6, Short.valueOf(columns[5]));
						pstm.setShort(7, Short.valueOf(columns[6]));
						pstm.setShort(8, Short.valueOf(columns[7]));
						pstm.setShort(9, Short.valueOf(columns[8]));
						pstm.setShort(10, Short.valueOf(columns[9]));
						pstm.setInt(11, Integer.valueOf(columns[10]));
						pstm.setDouble(12, Double.valueOf(columns[11]));
						pstm.setDouble(13, Double.valueOf(columns[12]));
						pstm.setDouble(14, Double.valueOf(columns[13]));
						pstm.setDouble(15, Double.valueOf(columns[14]));
						pstm.setString(16, columns[15]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Specimens Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreSubspecial() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "subspecialties.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Subspecial (SBID, SYID, SBNM, SBDC) VALUES (?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setString(3, columns[2]);
						pstm.setString(4, columns[3]);
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Subspecialties Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreTurnaround() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "turnaround.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement(
					"INSERT INTO Turnaround (TAID, GRSS, EMBD, MICR, ROUT, FINL, TANM) VALUES (?, ?, ?, ?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
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
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Turnaround Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreWorkdays() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "workdays.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement("INSERT INTO Workdays (WDID, WDNO, WDTP, WDDT) VALUES (?, ?, ?, ?)");
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setInt(1, Integer.valueOf(columns[0]));
						pstm.setInt(2, Integer.valueOf(columns[1]));
						pstm.setString(3, columns[2]);
						pstm.setTimestamp(4, new Timestamp(Long.valueOf(columns[3])));
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Workdays Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private String unformatLines(String in) {
		return in.replaceAll("\\\\", "\\");
	}
}