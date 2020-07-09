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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM accessions ORDER BY acid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("acid"), rst.getShort("syid"), rst.getString("acfl"),
						rst.getString("acld"), formatString(rst.getString("acnm")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM additionals ORDER BY caid, prid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("caid"), rst.getShort("prid"), rst.getShort("adcd"),
						rst.getInt("adv5"), rst.getTimestamp("addt").getTime(), rst.getDouble("adv1"),
						rst.getDouble("adv2"), rst.getDouble("adv3"), rst.getDouble("adv4"));
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Additionals Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM cases ORDER BY caid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("caid"), rst.getShort("faid"), rst.getShort("sbid"),
						rst.getShort("smid"), rst.getShort("grid"), rst.getShort("emid"), rst.getShort("miid"),
						rst.getShort("roid"), rst.getShort("fnid"), rst.getShort("grta"), rst.getShort("emta"),
						rst.getShort("mita"), rst.getShort("rota"), rst.getShort("fnta"), rst.getShort("casp"),
						rst.getShort("cabl"), rst.getShort("casl"), rst.getShort("casy"), rst.getShort("cafs"),
						rst.getShort("cahe"), rst.getShort("cass"), rst.getShort("caih"), rst.getShort("camo"),
						rst.getShort("cav5"), rst.getTimestamp("aced").getTime(), rst.getTimestamp("gred").getTime(),
						rst.getTimestamp("emed").getTime(), rst.getTimestamp("mied").getTime(),
						rst.getTimestamp("roed").getTime(), rst.getTimestamp("fned").getTime(), rst.getDouble("cav1"),
						rst.getDouble("cav2"), rst.getDouble("cav3"), rst.getDouble("cav4"), rst.getString("cano"));
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Cases Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
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
		String[] tables = { "coder1", "coder2", "coder3", "coder4" };
		input = "%d\t%d\t%d\t%f\t%f\t%f\t%s\t%s\n";
		for (int i = 0; i < 4; i++) {
			fileName = dataDir + tables[i].toLowerCase() + ".txt";
			rst = pj.dbPowerJ.getResultSet("SELECT * FROM " + tables[i] + " ORDER BY coid");
			file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				noRows = 0;
				fos = new FileOutputStream(file);
				while (rst.next()) {
					output = String.format(input, rst.getShort("coid"), rst.getShort("ruid"), rst.getShort("coqy"),
							rst.getDouble("cov1"), rst.getDouble("cov2"), rst.getDouble("cov3"), rst.getString("conm"),
							formatLines(rst.getString("codc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM comments ORDER BY caid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("caid"), formatLines(rst.getString("com1")),
						formatLines(rst.getString("com2")), formatLines(rst.getString("com3")),
						formatLines(rst.getString("com4")));
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Comments Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Comments Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupErrors() throws SQLException, IOException {
		fileName = dataDir + "errors.txt";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM errors ORDER BY caid");
		input = "%d\t%d\t%s\t%s\n";
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("caid"), rst.getShort("erid"), rst.getString("cano"),
						formatLines(rst.getString("erdc")));
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Errors Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Errors Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupFacilities() throws SQLException, IOException {
		fileName = dataDir + "facilities.txt";
		input = "%d\t%s\t%s\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM facilities ORDER BY faid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("faid"), rst.getString("fafl"), rst.getString("fald"),
						rst.getString("fanm"), formatString(rst.getString("fadc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM frozens ORDER BY spid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("spid"), rst.getShort("prid"), rst.getShort("frbl"),
						rst.getShort("frsl"), rst.getInt("frv5"), rst.getDouble("frv1"), rst.getDouble("frv2"),
						rst.getDouble("frv3"), rst.getDouble("frv4"));
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Frozens Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Frozens Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupOrderGroups() throws SQLException, IOException {
		fileName = dataDir + "ordergroups.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM ordergroups ORDER BY ogid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("ogid"), rst.getShort("otid"), rst.getShort("ogc1"),
						rst.getShort("ogc2"), rst.getShort("ogc3"), rst.getShort("ogc4"), rst.getInt("ogc5"),
						rst.getString("ognm"), formatString(rst.getString("ogdc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM ordermaster ORDER BY omid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("omid"), rst.getShort("ogid"), rst.getString("omnm"),
						formatString(rst.getString("omdc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM OrderTypes ORDER BY otid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("otid"), rst.getString("otnm"));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM orders ORDER BY spid, ogid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("spid"), rst.getShort("ogid"), rst.getShort("orqy"),
						rst.getDouble("orv1"), rst.getDouble("orv2"), rst.getDouble("orv3"), rst.getDouble("orv4"));
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Orders Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Orders Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupPending() throws SQLException, IOException {
		fileName = dataDir + "pending.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM pending ORDER BY pnid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("pnid"), rst.getShort("faid"), rst.getShort("sbid"),
						rst.getShort("smid"), rst.getShort("poid"), rst.getShort("grid"), rst.getShort("emid"),
						rst.getShort("miid"), rst.getShort("roid"), rst.getShort("fnid"), rst.getShort("grta"),
						rst.getShort("emta"), rst.getShort("mita"), rst.getShort("rota"), rst.getShort("fnta"),
						rst.getShort("pnst"), rst.getShort("pnsp"), rst.getShort("pnbl"), rst.getShort("pnsl"),
						rst.getInt("pnv5"), rst.getTimestamp("aced").getTime(), rst.getTimestamp("gred").getTime(),
						rst.getTimestamp("emed").getTime(), rst.getTimestamp("mied").getTime(),
						rst.getTimestamp("roed").getTime(), rst.getTimestamp("fned").getTime(), rst.getString("pnno"));
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Pending Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Pending Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupPersons() throws SQLException, IOException {
		fileName = dataDir + "persons.txt";
		input = "%d\t%d\t%d\t%s\t%s\t%s\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM persons ORDER BY prid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("prid"), rst.getInt("prvl"), rst.getDate("prdt").getTime(),
						rst.getString("prcd"), rst.getString("prac"), rst.getString("prnm"), formatString(rst.getString("prls")),
						formatString(rst.getString("prfr")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM procedures ORDER BY poid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("poid"), rst.getString("ponm"), formatString(rst.getString("podc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM rules ORDER BY ruid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("ruid"), rst.getString("runm"), formatLines(rst.getString("rudc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM schedules ORDER BY wdid, srid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getInt("wdid"), rst.getShort("prid"), rst.getShort("srid"));
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Schedules Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Schedules Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupServices() throws SQLException, IOException {
		fileName = dataDir + "services.txt";
		input = "%d\t%d\t%d\t%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM services ORDER BY srid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("srid"), rst.getShort("faid"), rst.getShort("sbid"),
						rst.getShort("srcd"), rst.getString("srnm"), formatString(rst.getString("srdc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM setup ORDER BY stid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("stid"), formatLines(rst.getString("stva")));
				fos.write(output.getBytes());
				noRows++;
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from setup Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupSpecialties() throws SQLException, IOException {
		fileName = dataDir + "specialties.txt";
		input = "%d\t%s\t%s\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM specialties ORDER BY syid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("syid"), rst.getString("syfl"), rst.getString("syld"),
						rst.getString("sysp"), formatString(rst.getString("synm")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM specimens ORDER BY spid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getLong("spid"), rst.getLong("caid"), rst.getShort("smid"),
						rst.getShort("spbl"), rst.getShort("spsl"), rst.getShort("spfr"), rst.getShort("sphe"),
						rst.getShort("spss"), rst.getShort("spih"), rst.getShort("spmo"), rst.getInt("spv5"),
						rst.getDouble("spv1"), rst.getDouble("spv2"), rst.getDouble("spv3"), rst.getDouble("spv4"),
						formatString(rst.getString("spdc")));
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Specimens Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Specimens Table.", noRows));
		}
		rst.close();
		fos.close();
	}

	private void backupSpeciGroups() throws SQLException, IOException {
		fileName = dataDir + "specimengroups.txt";
		input = "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%s\n";
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM specigroups ORDER BY sgid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("sgid"), rst.getShort("sbid"), rst.getShort("poid"),
						rst.getShort("sg1b"), rst.getShort("sg1m"), rst.getShort("sg1r"), rst.getShort("sg2b"),
						rst.getShort("sg2m"), rst.getShort("sg2r"), rst.getShort("sg3b"), rst.getShort("sg3m"),
						rst.getShort("sg3r"), rst.getShort("sg4b"), rst.getShort("sg4m"), rst.getShort("sg4r"),
						rst.getInt("sgv5"), rst.getString("sgln"), formatString(rst.getString("sgdc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM specimaster ORDER BY smid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("smid"), rst.getShort("sgid"), rst.getShort("taid"),
						rst.getString("smnm"), formatString(rst.getString("smdc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM subspecial ORDER BY sbid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("sbid"), rst.getShort("syid"), rst.getString("sbnm"),
						formatString(rst.getString("sbdc")));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM turnaround ORDER BY taid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getShort("taid"), rst.getShort("grss"), rst.getShort("embd"),
						rst.getShort("micr"), rst.getShort("rout"), rst.getShort("finl"), rst.getString("tanm"));
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
		rst = pj.dbPowerJ.getResultSet("SELECT * FROM workdays ORDER BY wdid");
		file = new File(fileName);
		if (!file.exists())
			file.createNewFile();
		if (file.exists()) {
			noRows = 0;
			fos = new FileOutputStream(file);
			while (rst.next()) {
				output = String.format(input, rst.getInt("wdid"), rst.getInt("wdno"), rst.getString("wdtp"),
						rst.getDate("wddt").getTime());
				fos.write(output.getBytes());
				noRows++;
				if (noRows % 2000 == 0) {
					pj.log(LConstants.ERROR_NONE, String.format("Saved %d rows from Workdays Table.", noRows));
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
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
		return in.replaceAll("\n", "}]").replaceAll("\t", "]}").replaceAll("\\\\", "]]");
	}

	private String formatString(String in) {
		return in.replaceAll("\n", " ").replaceAll("\t", " ").replaceAll("\\\\", " ");
	}

	void restore() {
		try {
			pj.setBusy(true);
			if (getDir()) {
				for (int i = 23; i < 26; i++) {
					switch (i) {
					case 0:
						restoreSetup();
						break;
					case 1:
						restoreWorkdays();
						break;
					case 2:
						restoreProcedures();
						break;
					case 3:
						restoreRules();
						break;
					case 4:
						restoreSpecialties();
						break;
					case 5:
						restoreSubspecial();
						break;
					case 6:
						restoreCoders();
						break;
					case 7:
						restoreTurnaround();
						break;
					case 8:
						restoreOrderTypes();
						break;
					case 9:
						restoreOrderGroups();
						break;
					case 10:
						restoreSpecimenGroups();
						break;
					case 11:
						restoreFacilities();
						break;
					case 12:
						restorePersons();
						break;
					case 13:
						restoreAccessions();
						break;
					case 14:
						restoreOrderMaster();
						break;
					case 15:
						restoreSpecimenMaster();
						break;
					case 16:
						restoreServices();
						break;
					case 17:
						restoreSchedules();
						break;
					case 18:
						restorePending();
						break;
					case 19:
						restoreCases();
						break;
					case 20:
						restoreSpecimens();
						break;
					case 21:
						restoreOrders();
						break;
					case 22:
						restoreFrozens();
						break;
					case 23:
						restoreAdditionals();
						break;
					case 24:
						restoreComments();
						break;
					default:
						restoreErrors();
					}
					try {
						Thread.sleep(LConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				}
				pj.log(LConstants.ERROR_NONE, "Restore completed successfully.");
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Restore", e);
		} catch (NumberFormatException e) {
			pj.log(LConstants.ERROR_NUMBER_FORMAT, "Restore", e);
		} catch (NullPointerException e) {
			pj.log(LConstants.ERROR_NULL, "Restore", e);
		} catch (FileNotFoundException e) {
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, "Restore", e);
		} catch (Exception e) {
			pj.log(LConstants.ERROR_UNEXPECTED, "Restore", e);
		} finally {
			try {
				pstm.close();
			} catch (Exception e) {
			}
			try {
				scanner.close();
			} catch (Exception e) {
			}
			pj.setBusy(false);
		}
	}

	private void restoreAccessions() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "accessions.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_ACC_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setString(2, columns[2]);
						pstm.setString(3, columns[3]);
						pstm.setString(4, columns[4]);
						pstm.setShort(5, Short.valueOf(columns[0]));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_ADD_INSERT));
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[0]));
						pstm.setShort(2, Short.valueOf(columns[1]));
						pstm.setShort(3, Short.valueOf(columns[2]));
						pstm.setTimestamp(4, new Timestamp(Long.valueOf(columns[4])));
						pstm.setDouble(5, Double.valueOf(columns[5]));
						pstm.setDouble(6, Double.valueOf(columns[6]));
						pstm.setDouble(7, Double.valueOf(columns[7]));
						pstm.setDouble(8, Double.valueOf(columns[8]));
						pstm.setInt(9, Integer.valueOf(columns[3]));
						pstm.executeUpdate();
						noRows++;
						if (noRows % 2000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Additionals Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Additionals Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreCases() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "cases.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_CSE_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[2]));
						pstm.setShort(3, Short.valueOf(columns[3]));
						pstm.setShort(4, Short.valueOf(columns[4]));
						pstm.setShort(5, Short.valueOf(columns[5]));
						pstm.setShort(6, Short.valueOf(columns[6]));
						pstm.setShort(7, Short.valueOf(columns[7]));
						pstm.setShort(8, Short.valueOf(columns[8]));
						pstm.setShort(9, Short.valueOf(columns[9]));
						pstm.setShort(10, Short.valueOf(columns[10]));
						pstm.setShort(11, Short.valueOf(columns[11]));
						pstm.setShort(12, Short.valueOf(columns[12]));
						pstm.setShort(13, Short.valueOf(columns[13]));
						pstm.setShort(14, Short.valueOf(columns[14]));
						pstm.setShort(15, Short.valueOf(columns[15]));
						pstm.setShort(16, Short.valueOf(columns[16]));
						pstm.setShort(17, Short.valueOf(columns[17]));
						pstm.setShort(18, Short.valueOf(columns[18]));
						pstm.setShort(19, Short.valueOf(columns[19]));
						pstm.setShort(20, Short.valueOf(columns[20]));
						pstm.setShort(21, Short.valueOf(columns[21]));
						pstm.setShort(22, Short.valueOf(columns[22]));
						pstm.setShort(23, Short.valueOf(columns[23]));
						pstm.setTimestamp(24, new Timestamp(Long.valueOf(columns[24])));
						pstm.setTimestamp(25, new Timestamp(Long.valueOf(columns[25])));
						pstm.setTimestamp(26, new Timestamp(Long.valueOf(columns[26])));
						pstm.setTimestamp(27, new Timestamp(Long.valueOf(columns[27])));
						pstm.setTimestamp(28, new Timestamp(Long.valueOf(columns[28])));
						pstm.setTimestamp(29, new Timestamp(Long.valueOf(columns[29])));
						pstm.setDouble(30, Double.valueOf(columns[30]));
						pstm.setDouble(31, Double.valueOf(columns[31]));
						pstm.setDouble(32, Double.valueOf(columns[32]));
						pstm.setDouble(33, Double.valueOf(columns[33]));
						pstm.setString(34, columns[34]);
						pstm.setLong(35, Long.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
						if (noRows % 1000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Cases Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Cases Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private void restoreCoders() throws SQLException, FileNotFoundException, NumberFormatException {
		final String[] tables = { "coder1", "coder2", "coder3", "coder4" };
		for (int i = 0; i < 4; i++) {
			fileName = dataDir + tables[i] + ".txt";
			file = new File(fileName);
			if (file.exists()) {
				noRows = 0;
				scanner = new Scanner(new FileReader(fileName));
				switch (i) {
				case 0:
					pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_CD1_INSERT));
					break;
				case 1:
					pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_CD2_INSERT));
					break;
				case 2:
					pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_CD3_INSERT));
					break;
				default:
					pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_CD4_INSERT));
				}
				while (scanner.hasNextLine()) {
					input = scanner.nextLine();
					if (input.length() > 1) {
						if (!input.startsWith("-")) {
							columns = input.split(tab);
							pstm.setShort(1, Short.valueOf(columns[1]));
							pstm.setShort(2, Short.valueOf(columns[2]));
							pstm.setDouble(3, Double.valueOf(columns[3]));
							pstm.setDouble(4, Double.valueOf(columns[4]));
							pstm.setDouble(5, Double.valueOf(columns[5]));
							pstm.setString(6, columns[6]);
							pstm.setString(7, unformatLines(columns[7]));
							pstm.setShort(8, Short.valueOf(columns[0]));
							pstm.executeUpdate();
							noRows++;
						}
					}
				}
				pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to %s Table.", noRows, tables[i]));
				scanner.close();
				pstm.close();
				try {
					Thread.sleep(LConstants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void restoreComments() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "comments.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_CMT_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setString(1, unformatLines(columns[1]));
						pstm.setString(2, unformatLines(columns[2]));
						pstm.setString(3, unformatLines(columns[3]));
						pstm.setString(4, unformatLines(columns[4]));
						pstm.setLong(5, Long.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
						if (noRows % 1000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Comments Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_ERR_INSERT));
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
						if (noRows % 2000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Errors Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Errors Table.", noRows));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_FAC_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setString(1, columns[1]);
						pstm.setString(2, columns[2]);
						pstm.setString(3, columns[3]);
						pstm.setString(4, columns[4]);
						pstm.setShort(5, Short.valueOf(columns[0]));
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
		fileName = dataDir + "frozens.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_FRZ_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[2]));
						pstm.setShort(2, Short.valueOf(columns[3]));
						pstm.setInt(3, Short.valueOf(columns[1]));
						pstm.setInt(4, Short.valueOf(columns[4]));
						pstm.setDouble(5, Double.valueOf(columns[5]));
						pstm.setDouble(6, Double.valueOf(columns[6]));
						pstm.setDouble(7, Double.valueOf(columns[7]));
						pstm.setDouble(8, Double.valueOf(columns[8]));
						pstm.setLong(9, Long.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
						if (noRows % 2000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to frozens Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_ORG_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[2]));
						pstm.setShort(3, Short.valueOf(columns[3]));
						pstm.setShort(4, Short.valueOf(columns[4]));
						pstm.setShort(5, Short.valueOf(columns[5]));
						pstm.setInt(6, Integer.valueOf(columns[6]));
						pstm.setString(7, columns[7]);
						pstm.setString(8, columns[8]);
						pstm.setShort(9, Short.valueOf(columns[0]));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_ORM_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setString(2, columns[2]);
						pstm.setString(3, columns[3]);
						pstm.setShort(4, Short.valueOf(columns[0]));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_ORD_INSERT));
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[2]));
						pstm.setDouble(2, Double.valueOf(columns[3]));
						pstm.setDouble(3, Double.valueOf(columns[4]));
						pstm.setDouble(4, Double.valueOf(columns[5]));
						pstm.setDouble(5, Double.valueOf(columns[6]));
						pstm.setShort(6, Short.valueOf(columns[1]));
						pstm.setLong(7, Long.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
						if (noRows % 2000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Orders Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_ORT_INSERT));
			scanner = new Scanner(new FileReader(fileName));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_PND_INSERT));
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[2]));
						pstm.setShort(3, Short.valueOf(columns[4]));
						pstm.setShort(4, Short.valueOf(columns[3]));
						pstm.setShort(5, Short.valueOf(columns[5]));
						pstm.setShort(6, Short.valueOf(columns[6]));
						pstm.setShort(7, Short.valueOf(columns[7]));
						pstm.setShort(8, Short.valueOf(columns[8]));
						pstm.setShort(9, Short.valueOf(columns[9]));
						pstm.setShort(10, Short.valueOf(columns[10]));
						pstm.setShort(11, Short.valueOf(columns[11]));
						pstm.setShort(12, Short.valueOf(columns[12]));
						pstm.setShort(13, Short.valueOf(columns[13]));
						pstm.setShort(14, Short.valueOf(columns[14]));
						pstm.setShort(15, Short.valueOf(columns[15]));
						pstm.setShort(16, Short.valueOf(columns[16]));
						pstm.setShort(17, Short.valueOf(columns[17]));
						pstm.setShort(18, Short.valueOf(columns[18]));
						pstm.setInt(19, Integer.valueOf(columns[19]));
						pstm.setTimestamp(20, new Timestamp(Long.valueOf(columns[20])));
						pstm.setTimestamp(21, new Timestamp(Long.valueOf(columns[21])));
						pstm.setTimestamp(22, new Timestamp(Long.valueOf(columns[22])));
						pstm.setTimestamp(23, new Timestamp(Long.valueOf(columns[23])));
						pstm.setTimestamp(24, new Timestamp(Long.valueOf(columns[24])));
						pstm.setTimestamp(25, new Timestamp(Long.valueOf(columns[25])));
						pstm.setString(26, columns[26]);
						pstm.setLong(27, Long.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
						if (noRows % 1000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Pending Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_PRS_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setInt(1, Integer.valueOf(columns[1]));
						pstm.setDate(2, new Date(Long.valueOf(columns[2])));
						pstm.setString(3, columns[3]);
						pstm.setString(4, columns[4]);
						pstm.setString(5, columns[5]);
						pstm.setString(6, columns[6]);
						pstm.setString(7, columns[7]);
						pstm.setShort(8, Short.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Persons Table.", noRows));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_PRO_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setString(1, columns[1]);
						pstm.setString(2, columns[2]);
						pstm.setShort(3, Short.valueOf(columns[0]));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_RUL_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setString(1, columns[1]);
						pstm.setString(2, unformatLines(columns[2]));
						pstm.setShort(3, Short.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Persons Table.", noRows));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_SCH_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[2]));
						pstm.setInt(3, Integer.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
						if (noRows % 2000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Schedules Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_SRV_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[2]));
						pstm.setShort(3, Short.valueOf(columns[3]));
						pstm.setString(4, columns[4]);
						pstm.setString(5, columns[5]);
						pstm.setShort(6, Short.valueOf(columns[0]));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_STP_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setString(1, unformatLines(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[0]));
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

	private void restoreSpecialties() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "specialties.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_SPY_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setString(1, columns[1]);
						pstm.setString(2, columns[2]);
						pstm.setString(3, columns[3]);
						pstm.setString(4, columns[4]);
						pstm.setShort(5, Short.valueOf(columns[0]));
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

	private void restoreSpecimenGroups() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "specimengroups.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_SPG_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[2]));
						pstm.setShort(3, Short.valueOf(columns[3]));
						pstm.setShort(4, Short.valueOf(columns[4]));
						pstm.setShort(5, Short.valueOf(columns[5]));
						pstm.setShort(6, Short.valueOf(columns[6]));
						pstm.setShort(7, Short.valueOf(columns[7]));
						pstm.setShort(8, Short.valueOf(columns[8]));
						pstm.setShort(9, Short.valueOf(columns[9]));
						pstm.setShort(10, Short.valueOf(columns[10]));
						pstm.setShort(11, Short.valueOf(columns[11]));
						pstm.setShort(12, Short.valueOf(columns[12]));
						pstm.setShort(13, Short.valueOf(columns[13]));
						pstm.setShort(14, Short.valueOf(columns[14]));
						pstm.setInt(15, Integer.valueOf(columns[15]));
						pstm.setString(16, columns[16]);
						pstm.setString(17, columns[17]);
						pstm.setShort(18, Short.valueOf(columns[0]));
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

	private void restoreSpecimenMaster() throws SQLException, FileNotFoundException, NumberFormatException {
		fileName = dataDir + "specimenmaster.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			scanner = new Scanner(new FileReader(fileName));
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_SPM_INSERT));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[2]));
						pstm.setString(3, columns[3]);
						pstm.setString(4, columns[4]);
						pstm.setShort(5, Short.valueOf(columns[0]));
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
		fileName = dataDir + "specimens.txt";
		file = new File(fileName);
		if (file.exists()) {
			noRows = 0;
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_SPE_INSERT));
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setLong(1, Long.valueOf(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[2]));
						pstm.setShort(3, Short.valueOf(columns[3]));
						pstm.setShort(4, Short.valueOf(columns[4]));
						pstm.setShort(5, Short.valueOf(columns[5]));
						pstm.setShort(6, Short.valueOf(columns[6]));
						pstm.setShort(7, Short.valueOf(columns[7]));
						pstm.setShort(8, Short.valueOf(columns[8]));
						pstm.setShort(9, Short.valueOf(columns[9]));
						pstm.setInt(10, Integer.valueOf(columns[10]));
						pstm.setDouble(11, Double.valueOf(columns[11]));
						pstm.setDouble(12, Double.valueOf(columns[12]));
						pstm.setDouble(13, Double.valueOf(columns[13]));
						pstm.setDouble(14, Double.valueOf(columns[14]));
						pstm.setString(15, columns[15]);
						pstm.setLong(16, Long.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
						if (noRows % 2000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Specimens Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_SUB_INSERT));
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setString(2, columns[2]);
						pstm.setString(3, columns[3]);
						pstm.setShort(4, Short.valueOf(columns[0]));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_TUR_INSERT));
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setShort(1, Short.valueOf(columns[1]));
						pstm.setShort(2, Short.valueOf(columns[2]));
						pstm.setShort(3, Short.valueOf(columns[3]));
						pstm.setShort(4, Short.valueOf(columns[4]));
						pstm.setShort(5, Short.valueOf(columns[5]));
						pstm.setString(6, columns[6]);
						pstm.setShort(7, Short.valueOf(columns[0]));
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
			pstm = pj.dbPowerJ.prepareStatement(pj.dbPowerJ.setSQL(DPowerJ.STM_WDY_INSERT));
			scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.length() > 1) {
					if (!input.startsWith("-")) {
						columns = input.split(tab);
						pstm.setTimestamp(1, new Timestamp(Long.valueOf(columns[3])));
						pstm.setString(2, columns[2]);
						pstm.setInt(3, Integer.valueOf(columns[1]));
						pstm.setInt(4, Integer.valueOf(columns[0]));
						pstm.executeUpdate();
						noRows++;
						if (noRows % 2000 == 0) {
							pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Workdays Table.", noRows));
							try {
								Thread.sleep(LConstants.SLEEP_TIME);
							} catch (InterruptedException e) {
							}
						}
					}
				}
			}
			pj.log(LConstants.ERROR_NONE, String.format("Restored %d rows to Workdays Table.", noRows));
			scanner.close();
			pstm.close();
		}
	}

	private String unformatLines(String in) {
		return in.replaceAll("]]", "\\\\").replaceAll("}]", "\n").replaceAll("]}", "\t");
	}
}