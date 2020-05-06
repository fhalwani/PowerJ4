package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class NSchedule extends NBase {
	private boolean byService = true;
	private short facID = 0;
	private int rowIndex = -1;
	private OService service = new OService();
	private OWorkday workday = new OWorkday();
	private ArrayList<ArrayList<OScheduleService>> scheduleServices = new ArrayList<ArrayList<OScheduleService>>();
	private ArrayList<OScheduleStaff> scheduleStaff = new ArrayList<OScheduleStaff>();
	private ArrayList<OWorkday> workdays = new ArrayList<OWorkday>();
	private ArrayList<OService> services = new ArrayList<OService>();
	private HashMap<Short, String> persons = new HashMap<Short, String>();
	private ArrayList<Date> dates = new ArrayList<Date>();
	private ModelSchedule modelSchedule;
	private ModelDates modelDates;
	private ITable tblList, tblSchedule;
	private IComboBox cboPersons;

	NSchedule(AClient parent) {
		super(parent);
		setName("Schedule");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_SCHEDULE);
		getWeeks();
		getServices();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			persons.clear();
			services.clear();
			workdays.clear();
			scheduleServices.clear();
			dates.clear();
		}
		return !altered;
	}

	private void createPanel() {
		modelDates = new ModelDates();
		tblList = new ITable(pj, modelDates);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tblList.addAncestorListener(new IFocusListener());
		tblList.addFocusListener(this);
		tblList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				setRow(tblList.convertRowIndexToModel(index));
			}
		});
		tblList.getColumnModel().getColumn(0).setMinWidth(120);
		JScrollPane scrollList = IGUI.createJScrollPane(tblList);
		scrollList.setMinimumSize(new Dimension(200, 900));
		modelSchedule = new ModelSchedule();
		cboPersons = new IComboBox();
		cboPersons.setModel(getPersons());
		tblSchedule = new ITable(pj, modelSchedule) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int c = columnAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					if (c == 0) {
						return (byService ? services.get(m).descr : persons.get(scheduleStaff.get(m).prsID));
					} else {
						return (byService ? persons.get(scheduleServices.get(m).get(c - 1).person.id)
								: scheduleStaff.get(m).services[c - 1]);
					}
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblSchedule.addFocusListener(this);
		tblSchedule.setDefaultEditor(OItem.class, new DefaultCellEditor(cboPersons));
		JScrollPane scrollSchedule = IGUI.createJScrollPane(tblSchedule);
		scrollSchedule.setMinimumSize(new Dimension(800, 900));
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollList);
		pnlSplit.setBottomComponent(scrollSchedule);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		add(new IToolBar(this), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	private void getDays() {
		Calendar calMonday = Calendar.getInstance();
		Calendar calStart = pj.dates.setMidnight(null);
		ResultSet rst = null;
		try {
			workdays.clear();
			calMonday.setTimeInMillis(dates.get(rowIndex).getTime());
			pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_WDY_SELECT), 1, calMonday.getTimeInMillis());
			rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_WDY_SELECT));
			while (rst.next() && workdays.size() < 7) {
				workday = new OWorkday();
				calStart.setTimeInMillis(rst.getDate("WDDT").getTime());
				workday.date.setTime(calStart.getTimeInMillis());
				workday.name = pj.dates.formatter(workday.date, LDates.FORMAT_SCHED);
				workday.dow = calStart.get(Calendar.DAY_OF_WEEK);
				workday.wdID = rst.getInt("WDID");
				workday.isOn = (rst.getString("WDTP").equalsIgnoreCase("D"));
				workdays.add(workday);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	private Object[] getPersons() {
		ArrayList<OItem> list = new ArrayList<OItem>();
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_PRS_SELECT));
		try {
			persons.clear();
			while (rst.next()) {
				if (rst.getString("PRCD").trim().equalsIgnoreCase("PT")
						&& rst.getString("PRAC").trim().equalsIgnoreCase("Y")) {
					list.add(new OItem(rst.getShort("PRID"), rst.getString("PRNM").trim()));
					persons.put(rst.getShort("PRID"),
							rst.getString("PRFR").trim() + " " + rst.getString("PRLS").trim());

				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
		return list.toArray();
	}

	private void getScheduleService() {
		short srvID = 0;
		short dateID = 0;
		short rowID = -1;
		short colID = -1;
		Calendar calMonday = Calendar.getInstance();
		ResultSet rst = null;
		try {
			scheduleServices.clear();
			scheduleStaff.clear();
			OScheduleService schedule = new OScheduleService();
			ArrayList<OScheduleService> cols = new ArrayList<OScheduleService>();
			for (int row = 0; row < services.size(); row++) {
				service = services.get(row);
				cols = new ArrayList<OScheduleService>();
				for (int col = 0; col < workdays.size(); col++) {
					workday = workdays.get(col);
					schedule = new OScheduleService();
					schedule.isNew = true;
					schedule.wdID = workday.wdID;
					schedule.date = workday.name;
					schedule.srvID = service.srvID;
					schedule.servce = service.name;
					// On call or (regular work day and on that specific day)
					schedule.isOn = (service.codes[0] || (workday.isOn && service.codes[workday.dow]));
					if (!schedule.isOn) {
						schedule.person = new OItem((short) -1, "   ");
					}
					cols.add(schedule);
				}
				scheduleServices.add(cols);
			}
			calMonday.setTimeInMillis(dates.get(rowIndex).getTime());
			pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SCH_SL_SRV), 1, calMonday.getTimeInMillis());
			calMonday.add(Calendar.DAY_OF_YEAR, 7);
			pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SCH_SL_SRV), 2, calMonday.getTimeInMillis());
			rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SCH_SL_SRV));
			while (rst.next()) {
				if (srvID != rst.getShort("SRID")) {
					srvID = rst.getShort("SRID");
					rowID = -1;
					for (short row = 0; row < services.size(); row++) {
						if (srvID == services.get(row).srvID) {
							rowID = row;
							break;
						}
					}
				}
				if (dateID != rst.getShort("WDID")) {
					dateID = rst.getShort("WDID");
					colID = -1;
					for (short col = 0; col < workdays.size(); col++) {
						if (dateID == workdays.get(col).wdID) {
							colID = col;
							break;
						}
					}
				}
				if (rowID > -1 && colID > -1) {
					scheduleServices.get(rowID).get(colID).isNew = false;
					scheduleServices.get(rowID).get(colID).person = new OItem(rst.getShort("PRID"),
							rst.getString("PRNM"));
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	private void getScheduleStaff() {
		short prsID = -1;
		short dateID = -1;
		short rowID = -1;
		short colID = -1;
		Calendar calMonday = Calendar.getInstance();
		OScheduleStaff staff = new OScheduleStaff();
		ResultSet rst = null;
		try {
			scheduleServices.clear();
			scheduleStaff.clear();
			calMonday.setTimeInMillis(dates.get(rowIndex).getTime());
			pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SCH_SL_STA), 1, calMonday.getTimeInMillis());
			calMonday.add(Calendar.DAY_OF_YEAR, 6);
			pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SCH_SL_STA), 2, calMonday.getTimeInMillis());
			rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SCH_SL_STA));
			while (rst.next()) {
				if (prsID != rst.getShort("PRID")) {
					prsID = rst.getShort("PRID");
					rowID = -1;
					dateID = -1;
					colID = -1;
					for (short row = 0; row < scheduleStaff.size(); row++) {
						if (prsID == scheduleStaff.get(row).prsID) {
							staff = scheduleStaff.get(row);
							rowID = row;
							break;
						}
					}
					if (rowID == -1) {
						staff = new OScheduleStaff();
						staff.prsID = prsID;
						staff.name = rst.getString("PRNM");
						for (int i = 0; i < 7; i++) {
							staff.services[i] = "";
						}
						scheduleStaff.add(staff);
						rowID = (short) (scheduleStaff.size() - 1);
					}
				}
				if (dateID != rst.getShort("WDID")) {
					dateID = rst.getShort("WDID");
					colID = -1;
					for (short col = 0; col < workdays.size(); col++) {
						if (dateID == workdays.get(col).wdID) {
							colID = col;
							break;
						}
					}
				}
				if (colID > -1) {
					if (staff.services[colID].length() > 0) {
						staff.services[colID] += "/";
					}
					staff.services[colID] += rst.getString("SRNM");
				} else {
					pj.log(LConstants.ERROR_UNEXPECTED, getName(), "getScheduleStaff - INVALID dateID - " + dateID);
				}
			}
			Collections.sort(scheduleStaff, new Comparator<OScheduleStaff>() {
				@Override
				public int compare(OScheduleStaff o1, OScheduleStaff o2) {
					return o1.name.compareTo(o2.name);
				}
			});
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	private void getServices() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SRV_SELECT));
		try {
			services.clear();
			while (rst.next()) {
				if (facID == rst.getShort("FAID") || facID == 0) {
					service = new OService();
					service.srvID = rst.getByte("SRID");
					service.name = rst.getString("SRNM");
					service.descr = rst.getString("SRDC");
					service.codes = pj.numbers.shortToBoolean(rst.getShort("SRCD"));
					services.add(service);
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	private void getWeeks() {
		Calendar isMonday = Calendar.getInstance();
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SCH_SL_MON));
		try {
			while (rst.next()) {
				isMonday.setTimeInMillis(rst.getDate("WDDT").getTime());
				if (isMonday.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
					Date date = new Date();
					date.setTime(rst.getDate("WDDT").getTime());
					dates.add(date);
				}
			}
			Collections.sort(dates, new Comparator<Date>() {
				@Override
				public int compare(Date o1, Date o2) {
					return (o1.getTime() > o2.getTime() ? -1 : (o1.getTime() == o2.getTime() ? 0 : 1));
				}
			});
			for (int i = dates.size() - 1; dates.size() > 52; i--) {
				dates.remove(i);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("schedule.pdf").trim();
		if (fileName.length() == 0)
			return;
		float[] widths = new float[workdays.size() + 1];
		for (int col = 0; col < workdays.size() + 1; col++) {
			widths[col] = 1;
		}
		String str = "Schedule - " + pj.dates.formatter(dates.get(rowIndex).getTime(), LDates.FORMAT_DATELONG);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER, 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable table = new PdfPTable(widths.length);
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			PdfWriter.getInstance(document, fos);
			document.open();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.add(new Chunk(pj.setup.getString(LSetup.VAR_LAB_NAME)));
			document.add(paragraph);
			paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
			document.add(paragraph);
			document.add(Chunk.NEWLINE);
			table.setWidthPercentage(100);
			table.setWidths(widths);
			for (int col = 0; col < widths.length; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				if (col == 0) {
					str = (byService ? "Service" : "Staff");
				} else {
					str = workdays.get(col - 1).name;
				}
				paragraph.add(new Chunk(str));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data rows
			int i = 0;
			for (int row = 0; row < tblSchedule.getRowCount(); row++) {
				i = tblSchedule.convertRowIndexToModel(row);
				for (int col = 0; col < widths.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					str = "";
					if (byService) {
						if (scheduleServices.size() > i) {
							if (col > 0) {
								if (scheduleServices.get(i).size() > col) {
									str = scheduleServices.get(i).get(col - 1).person.name;
								}
							} else {
								str = services.get(i).name;
							}
						}
					} else {
						if (scheduleStaff.size() > i) {
							if (col > 0) {
								str = scheduleStaff.get(i).services[col - 1];
							} else {
								str = scheduleStaff.get(i).name;
							}
						}
					}
					paragraph.add(new Chunk(str));
					paragraph.setAlignment(Element.ALIGN_LEFT);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.addElement(paragraph);
					table.addCell(cell);
				}
			}
			document.add(table);
			document.close();
		} catch (DocumentException e) {
			pj.log(LConstants.ERROR_IO, getName(), e);
		} catch (FileNotFoundException e) {
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		}
	}

	void save(OScheduleService schedule) {
		if (schedule.wdID > 0 && schedule.srvID > 0) {
			if (schedule.person.id > 0) {
				if (schedule.wdID > 0 && schedule.srvID > 0 && schedule.person.id > 0) {
					byte index = (schedule.isNew ? DPowerJ.STM_SCH_INSERT : DPowerJ.STM_SCH_UPDATE);
					pj.dbPowerJ.setShort(pjStms.get(index), 1, schedule.person.id);
					pj.dbPowerJ.setShort(pjStms.get(index), 2, schedule.srvID);
					pj.dbPowerJ.setInt(pjStms.get(index), 3, schedule.wdID);
					if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
						altered = false;
						if (schedule.isNew) {
							schedule.isNew = false;
						}
					}
				}
			} else if (!schedule.isNew) {
				pj.dbPowerJ.setShort(pjStms.get(DPowerJ.STM_SCH_DELETE), 1, schedule.srvID);
				pj.dbPowerJ.setInt(pjStms.get(DPowerJ.STM_SCH_DELETE), 2, schedule.wdID);
				if (pj.dbPowerJ.execute(pjStms.get(DPowerJ.STM_SCH_DELETE)) > 0) {
					altered = false;
				}
			}
		}
	}

	@Override
	void setFilter(short id, short value) {
		switch (id) {
		case IToolBar.TB_FAC:
			facID = value;
			getServices();
			break;
		default:
			byService = !byService;
			if (byService) {
				getScheduleService();
			} else {
				getScheduleStaff();
			}
			modelSchedule.fireTableStructureChanged();
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
			if (altered)
				return;
		}
		if (rowIndex != index) {
			if (index < 0 || index >= dates.size()) {
				// Selection got filtered away.
				return;
			}
			rowIndex = index;
			getDays();
			if (byService) {
				getScheduleService();
			} else {
				getScheduleStaff();
			}
			modelSchedule.fireTableStructureChanged();
			pj.statusBar.setMessage(pj.dates.formatter(dates.get(rowIndex).getTime(), LDates.FORMAT_DATELONG));
		}
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("schedule.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Schedule");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					"Schedule - " + pj.dates.formatter(dates.get(rowIndex).getTime(), LDates.FORMAT_DATELONG));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, workdays.size()));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			String str = "";
			for (int col = 0; col < workdays.size() + 1; col++) {
				xlsCell = xlsRow.createCell(col);
				if (col == 0) {
					str = (byService ? "Service" : "Staff");
				} else {
					str = workdays.get(col - 1).name;
				}
				xlsCell.setCellValue(str);
				xlsCell.setCellStyle(styles.get("header"));
				sheet.setColumnWidth(col, 15 * 256); // 15 characters
				sheet.setDefaultColumnStyle(col, styles.get("text"));
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < tblSchedule.getRowCount(); row++) {
				i = tblSchedule.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < workdays.size() + 1; col++) {
					xlsCell = xlsRow.createCell(col);
					str = "";
					if (byService) {
						if (scheduleServices.size() > i) {
							if (col > 0) {
								if (scheduleServices.get(i).size() > col) {
									str = scheduleServices.get(i).get(col - 1).person.name;
								}
							} else {
								str = services.get(i).name;
							}
						}
					} else {
						if (scheduleStaff.size() > i) {
							if (col > 0) {
								str = scheduleStaff.get(i).services[col - 1];
							} else {
								str = scheduleStaff.get(i).name;
							}
						}
					}
					xlsCell.setCellValue(str);
				}
			}
			sheet.createFreezePane(1, 2);
			// Write the output to a file
			FileOutputStream out = new FileOutputStream(fileName);
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		} catch (IOException e) {
			pj.log(LConstants.ERROR_IO, getName(), e);
		} catch (Exception e) {
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), e);
		}
	}

	private class ModelDates extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			return Date.class;
		}

		@Override
		public String getColumnName(int col) {
			return "Week";
		}

		@Override
		public int getRowCount() {
			return dates.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (dates.size() > 0 && row < dates.size()) {
				value = dates.get(row);
			}
			return value;
		}
	}

	private class ModelSchedule extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0 || !byService) {
				return String.class;
			}
			return OItem.class;
		}

		@Override
		public int getColumnCount() {
			return workdays.size() + 1;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) {
				return (byService ? "Service" : "Staff");
			} else {
				return workdays.get(col - 1).name;
			}
		}

		@Override
		public int getRowCount() {
			return (byService ? services.size() : scheduleStaff.size());
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (byService) {
				if (col > 0 && scheduleServices.size() > 0 && row < scheduleServices.size()) {
					if (scheduleServices.get(row).size() >= col) {
						value = scheduleServices.get(row).get(col - 1).person;
					}
				} else if (services.size() > 0 && row < services.size()) {
					value = services.get(row).name;
				}
			} else {
				if (col > 0 && scheduleStaff.size() > 0 && row < scheduleStaff.size()) {
					if (scheduleStaff.get(row).services.length >= col) {
						value = scheduleStaff.get(row).services[col - 1];
					}
				} else if (scheduleStaff.size() > 0 && row < scheduleStaff.size()) {
					value = scheduleStaff.get(row).name;
				}
			}
			return value;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			// Only if view is byService is editable
			return (byService && col > 0 && pj.userAccess[LConstants.ACCESS_STP_SC]
					&& scheduleServices.get(row).get(col - 1).isOn);
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (byService && col > 0 && pj.userAccess[LConstants.ACCESS_STP_SC]
					&& scheduleServices.get(row).get(col - 1).isOn) {
				altered = true;
				scheduleServices.get(row).get(col - 1).person = (OItem) value;
				save(scheduleServices.get(row).get(col - 1));
			}
		}
	}
}