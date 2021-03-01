package ca.powerj;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import ca.powerj.data.PersonData;
import ca.powerj.data.WorkdayData;
import ca.powerj.database.DBPowerj;
import ca.powerj.lib.LibBase;
import ca.powerj.lib.LibConstants;

public class Schedule extends LibBase {
	private final byte ALLW = 1;
	private final byte WDAY = 2;
	private final byte BR_2 = 3;
	private final byte BR_3 = 4;
	private final byte BR_H = 5;
	private final byte CY_2 = 6;
	private final byte GI_1 = 7;
	private final byte GI_2 = 8;
	private final byte GI_3 = 9;
	private final byte GI_H = 10;
	private final byte GU_2 = 11;
	private final byte GY_2 = 12;
	private final byte GY_3 = 13;
	private final byte HEME = 14;
	private final byte THOA = 15;
	private final byte EYES = 16;
	private final byte BKLG = 17;
	private byte noDays = 0;
	private byte startLine = 0;
	private short prsID = 0;
	private short srvID = 0;
	private int dateID = 0;
	private byte[] startCol = null;
	private byte[] serviceIDs = null;
	private byte[] dayDistro = null;
	private byte[] dayBrst2 = null;
	private byte[] dayBrst3 = null;
	private byte[] dayCyto1B = null;
	private byte[] dayGI1 = null;
	private byte[] dayGI2 = null;
	private byte[] dayGI3 = null;
	private byte[] dayGU2 = null;
	private byte[] dayGY2 = null;
	private byte[] dayGY3 = null;
	private byte[] dayEye = null;
	private byte[] dayBK1 = null;
	private String filePath = "";
	private String[] rows = null;
	private String[] serviceNames = null;
	private Calendar startDate = Calendar.getInstance();
	private WorkdayData workday = new WorkdayData();
	private ArrayList<WorkdayData> workdays = new ArrayList<WorkdayData>();
	private HashMap<String, Short> staff = new HashMap<String, Short>();
	private DBPowerj dbPowerJ = null;

	public Schedule() {
		super();
	}

	private void deleteMonth() {
		int start = 99999;
		int end = 0;
		for (int i = 0; i < workdays.size(); i++) {
			if (start > workdays.get(i).getDayID()) {
				start = workdays.get(i).getDayID();
			}
			if (end < workdays.get(i).getDayID()) {
				end = workdays.get(i).getDayID();
			}
		}
		start--;
		end++;
		System.out.printf("delete: %d to %d.%n", start, end);
		String sql = "DELETE FROM schedules WHERE wdid between ? AND ?";
		PreparedStatement pstm = null;
		try {
			pstm = dbPowerJ.prepareStatement(sql);
			pstm.setInt(1, start);
			pstm.setInt(2, end);
			int n = pstm.executeUpdate();
			if (n > 0) {
				System.out.printf("Deleted %d rows.%n", n);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			errorID = LibConstants.ERROR_SQL;
		} finally {
			dbPowerJ.close(pstm);
		}
	}

	private void getData() {
		try {
			PdfReader reader = new PdfReader(filePath);
			String page = PdfTextExtractor.getTextFromPage(reader, 1);
			rows = page.split("\n");
			reader.close();
			if (rows.length < 11) {
				errorID = LibConstants.ERROR_IMPORT;
				System.out.println("pdf lines: " + rows.length + " is less than expected.\nImport aborted.");
			}
			Thread.sleep(LibConstants.SLEEP_TIME);
		} catch (InterruptedException e) {
		} catch (IOException e) {
			e.printStackTrace();
			errorID = LibConstants.ERROR_IO;
		}
	}

	private void getDates() {
		SimpleDateFormat formatter = new SimpleDateFormat("(EEE MMM d)");
		workdays = dbPowerJ.getWorkdays(startDate.getTimeInMillis(), noDays);
		for (int i = 0; i < workdays.size(); i++) {
			workdays.get(i).setName(formatter.format(workdays.get(i).getTime()));
		}
		if (workdays.size() < noDays || noDays < 11) {
			errorID = LibConstants.ERROR_IMPORT;
			System.out.println("Workdays size: " + workdays.size() + " is less than expected: " + noDays + "\nImport aborted.");
		}
	}

	private void getStaff() {
		ArrayList<PersonData> temp = dbPowerJ.getPersons();
		PersonData item = new PersonData();
		for (int i = 0; i < temp.size(); i++) {
			item = temp.get(i);
			if (item.isActive() && item.getCode().equalsIgnoreCase("PT")) {
				staff.put(item.getInitials(), item.getPrsID());
			}
		}
		if (staff.size() < 10) {
			errorID = LibConstants.ERROR_IMPORT;
			System.out.println("Staff size: " + staff.size() + " is less than expected.\nImport aborted.");
		}
	}

	@Override
	public void init(String[] args) {
		super.init(args);
		dbPowerJ = getDBPJ();
		// TODO Fix before each file /home/fawaz/Downloads
		filePath = "C:\\Users\\fhalwani\\Downloads\\schedule-2021-03.pdf";
//		filePath = "/home/fawaz/Downloads/schedule-2020-05.pdf";
		startLine = 7; // Usually starts at line 6 (On call General)
		noDays = 35; // 28 (4 weeks) or 35 (5 weeks)
		startDate.set(Calendar.YEAR, 2021);
		startDate.set(Calendar.MONTH, Calendar.MARCH);
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		if (errorID == LibConstants.ERROR_NONE) {
			dbPowerJ.setStatements(LibConstants.ACTION_SCHEDULE);
		}
		if (errorID == LibConstants.ERROR_NONE) {
			getDates();
		}
		if (errorID == LibConstants.ERROR_NONE) {
			getStaff();
		}
		if (errorID == LibConstants.ERROR_NONE) {
			getData();
		}
		if (errorID == LibConstants.ERROR_NONE) {
			if (args.length > 0) {
				if (args[0].equals("--delete")) {
					// Delete old schedule if it exists (revised schedule)
					deleteMonth();
				}
			}
		}
		if (errorID == LibConstants.ERROR_NONE) {
			serviceNames = new String[rows.length];
			serviceIDs = new byte[rows.length];
			startCol = new byte[rows.length];
			dayDistro = new byte[rows.length];
			for (byte rowID = 0; rowID < rows.length; rowID++) {
				switch (rowID) {
				case 7:
					serviceNames[rowID] = "OC-G";
					serviceIDs[rowID] = 36;
					dayDistro[rowID] = ALLW;
					startCol[rowID] = 3;
					break;
				case 8:
					serviceNames[rowID] = "OC-N";
					serviceIDs[rowID] = 37;
					dayDistro[rowID] = ALLW;
					startCol[rowID] = 3;
					break;
				// Forensic on call is sometimes 1 line, others split in 2 lines!!
				case 14:
					serviceNames[rowID] = "FS-N";
					serviceIDs[rowID] = 16;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 2;
					break;
				case 15:
					serviceNames[rowID] = "AUTOPSY";
					serviceIDs[rowID] = 1;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				case 16:
					serviceNames[rowID] = "FS-G";
					serviceIDs[rowID] = 15;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 2;
					break;
				case 17:
					serviceNames[rowID] = "FS-C";
					serviceIDs[rowID] = 14;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 2;
					break;
				case 18:
					serviceNames[rowID] = "BR-1";
					serviceIDs[rowID] = 2;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 2;
					break;
				case 19:
					serviceNames[rowID] = "BR-2";
					serviceIDs[rowID] = 3;
					dayDistro[rowID] = BR_2;
					startCol[rowID] = 2;
					break;
				case 20:
					serviceNames[rowID] = "BR-3";
					serviceIDs[rowID] = 4;
					dayDistro[rowID] = BR_3;
					startCol[rowID] = 2;
					break;
				case 21:
					serviceNames[rowID] = "CARD";
					serviceIDs[rowID] = 6;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				case 22:
					serviceNames[rowID] = "CY-1A";
					serviceIDs[rowID] = 8;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 2;
					break;
				case 23:
					serviceNames[rowID] = "CY-2";
					serviceIDs[rowID] = 10;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 2;
					break;
				case 24:
					serviceNames[rowID] = "CY-1B";
					serviceIDs[rowID] = 9;
					dayDistro[rowID] = CY_2;
					startCol[rowID] = 2;
					break;
				case 25:
					serviceNames[rowID] = "DERM";
					serviceIDs[rowID] = 11;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				case 26:
					serviceNames[rowID] = "GI-S";
					serviceIDs[rowID] = 23;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 2;
					break;
				case 27:
					serviceNames[rowID] = "GI-R";
					serviceIDs[rowID] = 22;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 3;
					break;
				case 28:
					serviceNames[rowID] = "GI-1";
					serviceIDs[rowID] = 17;
					dayDistro[rowID] = GI_1;
					startCol[rowID] = 4;
					break;
				case 29:
					serviceNames[rowID] = "GI-2";
					serviceIDs[rowID] = 18;
					dayDistro[rowID] = GI_2;
					startCol[rowID] = 4;
					break;
				case 30:
					serviceNames[rowID] = "GI-3";
					serviceIDs[rowID] = 19;
					dayDistro[rowID] = GI_3;
					startCol[rowID] = 4;
					break;
				case 31:
					serviceNames[rowID] = "GU-1";
					serviceIDs[rowID] = 24;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				case 32:
					serviceNames[rowID] = "GU-2";
					serviceIDs[rowID] = 25;
					dayDistro[rowID] = GU_2;
					startCol[rowID] = 1;
					break;
				case 33:
					serviceNames[rowID] = "GYN-1";
					serviceIDs[rowID] = 26;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 2;
					break;
				case 34:
					serviceNames[rowID] = "GYN-2";
					serviceIDs[rowID] = 27;
					dayDistro[rowID] = GY_2;
					startCol[rowID] = 2;
					break;
				case 35:
					serviceNames[rowID] = "GYN-3";
					serviceIDs[rowID] = 28;
					dayDistro[rowID] = GY_3;
					startCol[rowID] = 2;
					break;
				case 36:
					serviceNames[rowID] = "BR-H";
					serviceIDs[rowID] = 5;
					dayDistro[rowID] = BR_H;
					startCol[rowID] = 2;
					break;
				case 37:
					serviceNames[rowID] = "GI-H";
					serviceIDs[rowID] = 20;
					dayDistro[rowID] = GI_H;
					startCol[rowID] = 2;
					break;
				case 38:
					serviceNames[rowID] = "ENT-END";
					serviceIDs[rowID] = 12;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				case 39:
					serviceNames[rowID] = "GI-L";
					serviceIDs[rowID] = 21;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				case 40:
					serviceNames[rowID] = "THOR";
					serviceIDs[rowID] = 34;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				case 41:
					serviceNames[rowID] = "THOR-A";
					serviceIDs[rowID] = 35;
					dayDistro[rowID] = THOA;
					startCol[rowID] = 2;
					break;
				case 42:
					serviceNames[rowID] = "LYMPH";
					serviceIDs[rowID] = 30;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 2;
					break;
				case 43:
					serviceNames[rowID] = "MSK";
					serviceIDs[rowID] = 31;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				case 44:
					serviceNames[rowID] = "EYES";
					serviceIDs[rowID] = 13;
					dayDistro[rowID] = EYES;
					startCol[rowID] = 2;
					break;
				case 45:
					serviceNames[rowID] = "RENAL";
					serviceIDs[rowID] = 33;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				case 46:
					serviceNames[rowID] = "CORN";
					serviceIDs[rowID] = 7;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
//				case 47:
//					serviceNames[rowID] = "BK-1";
//					serviceIDs[rowID] = 38;
//					dayDistro[rowID] = BKLG;
//					startCol[rowID] = 2;
//					break;
				case 48:
					serviceNames[rowID] = "NEURO";
					serviceIDs[rowID] = 32;
					dayDistro[rowID] = WDAY;
					startCol[rowID] = 1;
					break;
				default:
					serviceNames[rowID] = "Ignore";
					serviceIDs[rowID] = 0;
					dayDistro[rowID] = 0;
					startCol[rowID] = 0;
				}
			}
			dayBrst2 = new byte[noDays];
			dayBrst3 = new byte[noDays];
			dayCyto1B = new byte[noDays];
			dayGI1 = new byte[noDays];
			dayGI2 = new byte[noDays];
			dayGI3 = new byte[noDays];
			dayGU2 = new byte[noDays];
			dayGY2 = new byte[noDays];
			dayGY3 = new byte[noDays];
			dayEye = new byte[noDays];
			dayBK1 = new byte[noDays];
			// Breast 2
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 7:
					case 8:
					case 9:
					case 10:
					case 11:
					case 14:
					case 15:
					case 16:
					case 17:
					case 18:
					case 21:
					case 22:
					case 23:
					case 24:
					case 25:
					case 28:
					case 29:
					case 30:
					case 31:
					case 32:
						dayBrst2[dayID] = 1;
						break;
					default:
						dayBrst2[dayID] = 0;
					}
				} else {
					dayBrst2[dayID] = 0;
				}
			}
			// Breast 3
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
//					case 1:
					case 2:
//					case 3:
//					case 4:
//					case 7:
//					case 8:
//					case 9:
					case 10:
//					case 11:
//					case 14:
//					case 15:
//					case 16:
					case 17:
					case 18:
//					case 21:
//					case 22:
//					case 23:
//					case 24:
//					case 25:
//					case 28:
//					case 29:
					case 30:
//					case 31:
//					case 32:
						dayBrst3[dayID] = 1;
						break;
					default:
						dayBrst3[dayID] = 0;
					}
				} else {
					dayBrst3[dayID] = 0;
				}
			}
			// Cytology 1B
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 7:
					case 8:
					case 9:
					case 10:
					case 11:
					case 14:
					case 15:
					case 16:
					case 17:
					case 18:
					case 21:
					case 22:
					case 23:
					case 24:
					case 25:
					case 28:
					case 29:
					case 30:
					case 31:
					case 32:
						dayCyto1B[dayID] = 1;
						break;
					default:
						dayCyto1B[dayID] = 0;
					}
				} else {
					dayCyto1B[dayID] = 0;
				}
			}
			// GI-1
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 7:
					case 8:
					case 9:
					case 10:
					case 11:
					case 14:
					case 15:
					case 16:
					case 17:
					case 18:
					case 21:
					case 22:
					case 23:
					case 24:
					case 25:
					case 28:
					case 29:
					case 30:
					case 31:
					case 32:
						dayGI1[dayID] = 1;
						break;
					default:
						dayGI1[dayID] = 0;
					}
				} else {
					dayGI1[dayID] = 0;
				}
			}
			// GI no star 2
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 7:
					case 8:
					case 9:
					case 10:
					case 11:
					case 14:
					case 15:
					case 16:
					case 17:
					case 18:
//					case 21:
//					case 22:
					case 23:
//					case 24:
					case 25:
					case 28:
					case 29:
					case 30:
					case 31:
					case 32:
						dayGI2[dayID] = 1;
						break;
					default:
						dayGI2[dayID] = 0;
					}
				} else {
					dayGI2[dayID] = 0;
				}
			}
			// GI no star 3
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
//					case 1:
					case 2:
//					case 3:
//					case 4:
					case 7:
					case 8:
					case 9:
//					case 10:
					case 11:
					case 14:
					case 15:
					case 16:
					case 17:
					case 18:
					case 21:
					case 22:
					case 23:
					case 24:
					case 25:
					case 28:
					case 29:
					case 30:
					case 31:
					case 32:
						dayGI3[dayID] = 1;
						break;
					default:
						dayGI3[dayID] = 0;
					}
				} else {
					dayGI3[dayID] = 0;
				}
			}
			// GU 2
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 7:
					case 8:
					case 9:
					case 10:
					case 11:
					case 14:
					case 15:
					case 16:
					case 17:
					case 18:
					case 21:
					case 22:
					case 23:
					case 24:
//					case 25:
					case 28:
					case 29:
					case 30:
					case 31:
					case 32:
						dayGU2[dayID] = 1;
						break;
					default:
						dayGU2[dayID] = 0;
					}
				} else {
					dayGU2[dayID] = 0;
				}
			}
			// GYN 2
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 7:
					case 8:
					case 9:
					case 10:
					case 11:
					case 14:
					case 15:
					case 16:
					case 17:
					case 18:
					case 21:
					case 22:
					case 23:
					case 24:
					case 25:
					case 28:
					case 29:
					case 30:
					case 31:
					case 32:
						dayGY2[dayID] = 1;
						break;
					default:
						dayGY2[dayID] = 0;
					}
				} else {
					dayGY2[dayID] = 0;
				}
			}
			// GYN 3
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
//					case 1:
					case 2:
//					case 3:
//					case 4:
					case 7:
					case 8:
					case 9:
					case 10:
//					case 11:
//					case 14:
//					case 15:
//					case 16:
//					case 17:
//					case 18:
//					case 21:
//					case 22:
//					case 23:
//					case 24:
//					case 25:
//					case 28:
//					case 29:
//					case 30:
					case 31:
//					case 32:
						dayGY3[dayID] = 1;
						break;
					default:
						dayGY3[dayID] = 0;
					}
				} else {
					dayGY3[dayID] = 0;
				}
			}
			// Eyes
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
					case 0:
//					case 1:
//					case 2:
//					case 3:
//					case 4:
//					case 7:
//					case 8:
//					case 9:
//					case 10:
					case 11:
					case 14:
//					case 15:
//					case 16:
//					case 17:
//					case 18:
//					case 21:
//					case 22:
//					case 23:
//					case 24:
//					case 25:
					case 28:
					case 29:
					case 30:
					case 31:
					case 32:
						dayEye[dayID] = 1;
						break;
					default:
						dayEye[dayID] = 0;
					}
				} else {
					dayEye[dayID] = 0;
				}
			}
			// Back log 1
			for (byte dayID = 0; dayID < noDays; dayID++) {
				workday = workdays.get(dayID);
				if (workday.getType().equalsIgnoreCase("D")) {
					switch (dayID) {
//					case 0:
//					case 1:
//					case 2:
//					case 3:
//					case 4:
//					case 7:
//					case 8:
//					case 9:
//					case 10:
//					case 11:
//					case 14:
//					case 15:
//					case 16:
//					case 17:
//					case 18:
//					case 21:
//					case 22:
//					case 23:
//					case 24:
//					case 25:
//					case 28:
//					case 29:
//					case 30:
//					case 31:
//					case 32:
//						dayBK1[dayID] = 1;
//						break;
					default:
						dayBK1[dayID] = 0;
					}
				} else {
					dayBK1[dayID] = 0;
				}
			}
			for (byte i = startLine; i < rows.length; i++) {
				String row = rows[i];
				System.out.println("Line " + i + ": " + row);
				srvID = serviceIDs[i];
				if (srvID > 0) {
					System.out.println("Service: " + serviceNames[i]);
					parse(i, row);
					try {
						Thread.sleep(LibConstants.SLEEP_TIME);
					} catch (InterruptedException e) {
					}
				} else {
					System.out.println("Ignore...");
				}
			}
		}
		quit();
	}

	public static void main(String[] args) {
		final String[] a = args;
		Schedule scheduler = new Schedule();
		scheduler.init(a);
	}

	private void parse(byte rowID, String row) {
		byte nextDateID = 0;
		String prsName = "";
		String[] columns = row.split(" ");
		System.out.println("No columns: " + (columns.length));
		for (int i = startCol[rowID]; i < columns.length; i++) {
			if (columns[i].length() > 1) {
				if (!prsName.equals(columns[i])) {
					prsName = columns[i];
					if (staff.get(prsName) == null) {
						nextDateID++;
						continue;
					} else {
						prsID = staff.get(prsName);
					}
				}
			} else {
				nextDateID++;
				continue;
			}
			switch (dayDistro[rowID]) {
			case ALLW:
				workday = workdays.get(nextDateID);
				dateID = workday.getDayID();
				nextDateID++;
				break;
			case WDAY:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					workday = workdays.get(j);
					if (workday.getType().equalsIgnoreCase("D")) {
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case HEME:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					workday = workdays.get(j);
					if (workday.getDow() == Calendar.WEDNESDAY) {
						if (workday.getType().equalsIgnoreCase("D")) {
							dateID = workday.getDayID();
							nextDateID = ++j;
							break;
						}
					}
				}
				break;
			case GI_H:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					workday = workdays.get(j);
					if (workday.getDow() == Calendar.THURSDAY) {
						if (workday.getType().equalsIgnoreCase("D")) {
							dateID = workday.getDayID();
							nextDateID = ++j;
							break;
						}
					}
				}
				break;
			case BR_H:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					workday = workdays.get(j);
					if (workday.getDow() == Calendar.WEDNESDAY
							|| workday.getDow() == Calendar.FRIDAY) {
						if (workday.getType().equalsIgnoreCase("D")) {
							dateID = workday.getDayID();
							nextDateID = ++j;
							break;
						}
					}
				}
				break;
			case THOA:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					workday = workdays.get(j);
					if (workday.getDow() == Calendar.TUESDAY
							|| workday.getDow() == Calendar.THURSDAY) {
						if (workday.getType().equalsIgnoreCase("D")) {
							dateID = workday.getDayID();
							nextDateID = ++j;
							break;
						}
					}
				}
				break;
			case BR_2:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayBrst2[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case BR_3:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayBrst3[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case CY_2:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayCyto1B[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case GI_1:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayGI1[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case GI_2:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayGI2[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case GI_3:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayGI3[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case GU_2:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayGU2[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case GY_2:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayGY2[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case GY_3:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayGY3[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case EYES:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayEye[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			case BKLG:
				for (byte j = nextDateID; j < workdays.size(); j++) {
					if (dayBK1[j] > 0) {
						workday = workdays.get(j);
						dateID = workday.getDayID();
						nextDateID = ++j;
						break;
					}
				}
				break;
			default:
				dateID = 0;
			}
			if (dateID > 0 && prsID > 0) {
				System.out.printf("Saving %1$s, %2$s, %3$s (%4$d, %5$d, %6$d).%n", prsName,
						workday.getName(), serviceNames[rowID], prsID, srvID, dateID);
				dbPowerJ.setSchedule(prsID, srvID, dateID);
			}
		}
	}

	private void quit() {
		if (dbPowerJ != null) {
			dbPowerJ.close();
		}
		super.close();
	}
}