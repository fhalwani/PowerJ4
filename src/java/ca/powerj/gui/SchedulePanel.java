package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import ca.powerj.data.ItemData;
import ca.powerj.data.PersonData;
import ca.powerj.data.ScheduleServiceData;
import ca.powerj.data.ScheduleStaffData;
import ca.powerj.data.ServiceData;
import ca.powerj.data.WorkdayData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IComboEditor;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class SchedulePanel extends BasePanel {
	private boolean byService = true;
	private short facID = 0;
	private int rowIndex = -1;
	private ServiceData service = new ServiceData();
	private WorkdayData workday = new WorkdayData();
	private ArrayList<ArrayList<ScheduleServiceData>> scheduleServices = new ArrayList<ArrayList<ScheduleServiceData>>();
	private ArrayList<ScheduleStaffData> scheduleStaff = new ArrayList<ScheduleStaffData>();
	private ArrayList<WorkdayData> workdays = new ArrayList<WorkdayData>();
	private ArrayList<ServiceData> services = new ArrayList<ServiceData>();
	private HashMap<Short, String> persons = new HashMap<Short, String>();
	private ArrayList<Date> dates = new ArrayList<Date>();
	private ModelSchedule modelSchedule;
	private ModelDates modelDate;
	private ITable tableList, tableSchedule;
	private IComboBox cboPersons;

	SchedulePanel(AppFrame application) {
		super(application);
		setName("Schedule");
		application.dbPowerJ.setStatements(LibConstants.ACTION_SCHEDULE);
		getWeeks();
		getServices();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
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
		modelDate = new ModelDates();
		tableList = new ITable(modelDate, application.dates, application.numbers);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tableList.addAncestorListener(new IFocusListener());
		tableList.addFocusListener(this);
		tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				setRow(tableList.convertRowIndexToModel(index));
			}
		});
		tableList.getColumnModel().getColumn(0).setMinWidth(120);
		JScrollPane scrollList = IUtilities.createJScrollPane(tableList);
		scrollList.setMinimumSize(new Dimension(200, 900));
		modelSchedule = new ModelSchedule();
		cboPersons = new IComboBox();
		cboPersons.setItems(getPersons());
		tableSchedule = new ITable(modelSchedule, application.dates, application.numbers) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int c = columnAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					if (c == 0) {
						return (byService ? services.get(m).getDescr() : persons.get(scheduleStaff.get(m).getPrsID()));
					} else {
						return (byService ? persons.get(scheduleServices.get(m).get(c -1).getPersonId())
								: scheduleStaff.get(m).getService(c -1));
					}
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tableSchedule.addFocusListener(this);
		tableSchedule.setDefaultEditor(ItemData.class, new IComboEditor(cboPersons));
		JScrollPane scrollSchedule = IUtilities.createJScrollPane(tableSchedule);
		scrollSchedule.setMinimumSize(new Dimension(800, 900));
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollList);
		pnlSplit.setBottomComponent(scrollSchedule);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		add(new IToolBar(application), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	private void getDays() {
		Calendar calStart = application.dates.setMidnight(null);
		workdays.clear();
		workdays = application.dbPowerJ.getWorkdays(dates.get(rowIndex).getTime(), 7);
		for (int i = 0; i < workdays.size(); i++) {
			workday = workdays.get(i);
			calStart.setTimeInMillis(workday.getDate().getTime());
			workday.setName(application.dates.formatter(calStart, LibDates.FORMAT_SCHED));
			workday.setDow(calStart.get(Calendar.DAY_OF_WEEK));
			workday.setOn(workday.getType().equals("D"));
		}
	}

	private Object[] getPersons() {
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		ArrayList<PersonData> tempList = application.dbPowerJ.getPersons();
		PersonData person = new PersonData();
		persons.clear();
		list.add(new ItemData((short) 0, " "));
		persons.put((short) 0, "To be determined");
		for (int i = 0; i < tempList.size(); i++) {
			person = tempList.get(i);
			if (person.isActive() && person.getCode().equalsIgnoreCase("PT")) {
				list.add(new ItemData(person.getPrsID(), person.getInitials()));
				persons.put(person.getPrsID(), person.getFirstname() + " " + person.getLastname());
			}
		}
		tempList.clear();
		return list.toArray();
	}

	private void getScheduleService() {
		short rowID = -1;
		short colID = -1;
		short srvID = 0;
		int dateID = 0;
		ArrayList<ScheduleServiceData> cols = new ArrayList<ScheduleServiceData>();
		ScheduleServiceData schedule = new ScheduleServiceData();
		scheduleServices.clear();
		scheduleStaff.clear();
		for (int row = 0; row < services.size(); row++) {
			service = services.get(row);
			cols = new ArrayList<ScheduleServiceData>();
			for (int col = 0; col < workdays.size(); col++) {
				workday = workdays.get(col);
				schedule = new ScheduleServiceData();
				schedule.setNew(true);
				schedule.setWdID(workday.getDayID());
				schedule.setDate(workday.getName());
				schedule.setSrvID((short) service.getSrvID());
				schedule.setName(service.getName());
				// On call or (regular work day and on that specific day)
				schedule.setOn((service.getCode(0) || (workday.isOn() && service.getCode(workday.getDow()))));
				if (!schedule.isOn()) {
					schedule.setPerson((short) 0, " ");
				}
				cols.add(schedule);
			}
			scheduleServices.add(cols);
		}
		if (rowIndex > 0) {
			ArrayList<ScheduleServiceData> tempList = application.dbPowerJ.getScheduleServices(
					dates.get(rowIndex).getTime(), dates.get(rowIndex -1).getTime());
			ScheduleServiceData tempItem = new ScheduleServiceData();
			for (int i = 0; i < tempList.size(); i++) {
				tempItem = tempList.get(i);
				if (srvID != tempItem.getSrvID()) {
					srvID = tempItem.getSrvID();
					rowID = -1;
					for (short row = 0; row < services.size(); row++) {
						if (srvID == services.get(row).getSrvID()) {
							rowID = row;
							break;
						}
					}
				}
				if (dateID != tempItem.getWdID()) {
					dateID = tempItem.getWdID();
					colID = -1;
					for (short col = 0; col < workdays.size(); col++) {
						if (dateID == workdays.get(col).getDayID()) {
							colID = col;
							break;
						}
					}
				}
				if (rowID > -1 && colID > -1) {
					scheduleServices.get(rowID).get(colID).setNew(false);
					scheduleServices.get(rowID).get(colID).setPerson(tempItem.getPersonItem());
				}
			}
			tempList.clear();
		}
	}

	private void getScheduleStaff() {
		short rowID = -1;
		short colID = -1;
		short prsID = -1;
		int dateID = -1;
		scheduleServices.clear();
		scheduleStaff.clear();
		if (rowIndex > 0) {
			ArrayList<ScheduleStaffData> tempList = application.dbPowerJ.getScheduleStaff(
					dates.get(rowIndex).getTime(), dates.get(rowIndex -1).getTime());
			ScheduleStaffData tempItem = new ScheduleStaffData();
			ScheduleStaffData staff = new ScheduleStaffData();
			for (int i = 0; i < tempList.size(); i++) {
				tempItem = tempList.get(i);
				if (prsID != tempItem.getPrsID()) {
					prsID = tempItem.getPrsID();
					rowID = -1;
					dateID = -1;
					colID = -1;
					for (short row = 0; row < scheduleStaff.size(); row++) {
						if (prsID == scheduleStaff.get(row).getPrsID()) {
							staff = scheduleStaff.get(row);
							rowID = row;
							break;
						}
					}
					if (rowID == -1) {
						staff = new ScheduleStaffData();
						staff.setPrsID(prsID);
						staff.setName(tempItem.getName());
						for (int j = 0; j < 7; j++) {
							staff.setService(j, "");
						}
						scheduleStaff.add(staff);
						rowID = (short) (scheduleStaff.size() - 1);
					}
				}
				if (dateID != tempItem.getWdID()) {
					dateID = tempItem.getWdID();
					colID = -1;
					for (short col = 0; col < workdays.size(); col++) {
						if (dateID == workdays.get(col).getDayID()) {
							colID = col;
							break;
						}
					}
				}
				if (colID > -1) {
					if (staff.getService(colID).length() > 0) {
						staff.setService(colID, staff.getService(colID) + "/" + tempItem.getService(0));
					} else {
						staff.setService(colID, tempItem.getService(0));
					}
				}
			}
			tempList.clear();
		}
		Collections.sort(scheduleStaff, new Comparator<ScheduleStaffData>() {
			@Override
			public int compare(ScheduleStaffData o1, ScheduleStaffData o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
	}

	private void getServices() {
		services.clear();
		services = application.dbPowerJ.getServices();
		if (facID > 0) {
			for (int i = services.size() -1; i >= 0; i--) {
				if (facID != services.get(i).getFacID()) {
					services.remove(i);
				}
			}
		}
	}

	private void getWeeks() {
		int noOfFutureMondays = 0;
		Date date = new Date();
		Calendar isMonday = Calendar.getInstance();
		Calendar lastMonday = Calendar.getInstance();
		Calendar nextMonday = Calendar.getInstance();
		ArrayList<Long> tempList = application.dbPowerJ.getScheduleDates();
		lastMonday.setTimeInMillis(0);
		nextMonday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		for (int i = 0; i < tempList.size(); i++) {
			isMonday.setTimeInMillis(tempList.get(i));
			if (isMonday.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
				date = new Date();
				date.setTime(tempList.get(i));
				dates.add(date);
				if (lastMonday.getTimeInMillis() < tempList.get(i)) {
					lastMonday.setTimeInMillis(tempList.get(i));
				}
				if (nextMonday.getTimeInMillis() < tempList.get(i)) {
					nextMonday.setTimeInMillis(tempList.get(i));
					noOfFutureMondays++;
				}
			}
		}
		// Show up to date schedule, even if blank 
		while (lastMonday.getTimeInMillis() < nextMonday.getTimeInMillis()) {
			lastMonday.add(Calendar.DAY_OF_YEAR, 7);
			date = new Date();
			date.setTime(lastMonday.getTimeInMillis());
			dates.add(date);
		}
		// Always have 4 weeks in the future, to be able to add new schedule 
		for (int i = noOfFutureMondays; i < 4; i++) {
			lastMonday.add(Calendar.DAY_OF_YEAR, 7);
			date = new Date();
			date.setTime(lastMonday.getTimeInMillis());
			dates.add(date);
		}
		Collections.sort(dates, new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return (o1.getTime() > o2.getTime() ? -1 : (o1.getTime() < o2.getTime() ? 1 : 0));
			}
		});
		// Keep the top 52 weeks
		for (int i = dates.size() -1; dates.size() > 52; i--) {
			dates.remove(i);
		}
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("schedule.pdf").trim();
		if (fileName.length() == 0)
			return;
		float[] widths = new float[workdays.size() + 1];
		for (int col = 0; col < workdays.size() + 1; col++) {
			widths[col] = 1;
		}
		String str = "Schedule - " + application.dates.formatter(dates.get(rowIndex).getTime(), LibDates.FORMAT_DATELONG);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER, 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable pdfTable = new PdfPTable(widths.length);
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			PdfWriter.getInstance(document, fos);
			document.open();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.add(new Chunk(application.setup.getString(LibSetup.VAR_LAB_NAME)));
			document.add(paragraph);
			paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
			document.add(paragraph);
			document.add(Chunk.NEWLINE);
			pdfTable.setWidthPercentage(100);
			pdfTable.setWidths(widths);
			for (int col = 0; col < widths.length; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				if (col == 0) {
					str = (byService ? "Service" : "Staff");
				} else {
					str = workdays.get(col - 1).getName();
				}
				paragraph.add(new Chunk(str));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				pdfTable.addCell(cell);
			}
			pdfTable.setHeaderRows(1);
			// data rows
			int i = 0;
			for (int row = 0; row < tableSchedule.getRowCount(); row++) {
				i = tableSchedule.convertRowIndexToModel(row);
				for (int col = 0; col < widths.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					str = "";
					if (byService) {
						if (scheduleServices.size() > i) {
							if (col > 0) {
								if (scheduleServices.get(i).size() > col) {
									str = scheduleServices.get(i).get(col - 1).getPersonItem().getName();
								}
							} else {
								str = services.get(i).getName();
							}
						}
					} else {
						if (scheduleStaff.size() > i) {
							if (col > 0) {
								str = scheduleStaff.get(i).getService(col -1);
							} else {
								str = scheduleStaff.get(i).getName();
							}
						}
					}
					paragraph.add(new Chunk(str));
					paragraph.setAlignment(Element.ALIGN_LEFT);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell.addElement(paragraph);
					pdfTable.addCell(cell);
				}
			}
			document.add(pdfTable);
			document.close();
		} catch (DocumentException e) {
			application.log(LibConstants.ERROR_IO, getName(), e);
		} catch (FileNotFoundException e) {
			application.log(LibConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		}
	}

	void save(ScheduleServiceData schedule) {
		if (schedule.getWdID() > 0 && schedule.getSrvID() > 0) {
			if (schedule.getPersonId() > 0) {
				if (application.dbPowerJ.setSchedule(schedule) > 0) {
					altered = false;
					if (schedule.isNew()) {
						schedule.setNew(false);
					}
				}
			} else if (!schedule.isNew()) {
				if (application.dbPowerJ.deleteSchedule(schedule) > 0) {
					altered = false;
				}
			}
		}
	}

	@Override
	public void setFilter(short id, int value) {
		switch (id) {
		case IToolBar.TB_FAC:
			facID = (short) value;
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
			application.display(application.dates.formatter(dates.get(rowIndex).getTime(), LibDates.FORMAT_DATELONG));
		}
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("schedule.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Schedule");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					"Schedule - " + application.dates.formatter(dates.get(rowIndex).getTime(), LibDates.FORMAT_DATELONG));
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
					str = workdays.get(col - 1).getName();
				}
				xlsCell.setCellValue(str);
				xlsCell.setCellStyle(styles.get("header"));
				sheet.setColumnWidth(col, 15 * 256); // 15 characters
				sheet.setDefaultColumnStyle(col, styles.get("text"));
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < tableSchedule.getRowCount(); row++) {
				i = tableSchedule.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < workdays.size() + 1; col++) {
					xlsCell = xlsRow.createCell(col);
					str = "";
					if (byService) {
						if (scheduleServices.size() > i) {
							if (col > 0) {
								if (scheduleServices.get(i).size() > col) {
									str = scheduleServices.get(i).get(col -1).getPersonItem().getName();
								}
							} else {
								str = services.get(i).getName();
							}
						}
					} else {
						if (scheduleStaff.size() > i) {
							if (col > 0) {
								str = scheduleStaff.get(i).getService(col -1);
							} else {
								str = scheduleStaff.get(i).getName();
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
			application.log(LibConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		} catch (IOException e) {
			application.log(LibConstants.ERROR_IO, getName(), e);
		} catch (Exception e) {
			application.log(LibConstants.ERROR_UNEXPECTED, getName(), e);
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
			return ItemData.class;
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
				return workdays.get(col - 1).getName();
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
						value = scheduleServices.get(row).get(col - 1).getPersonItem();
					}
				} else if (services.size() > 0 && row < services.size()) {
					value = services.get(row).getName();
				}
			} else {
				if (col > 0 && scheduleStaff.size() > 0 && row < scheduleStaff.size()) {
					if (scheduleStaff.get(row).getServices().length >= col) {
						value = scheduleStaff.get(row).getService(col -1);
					}
				} else if (scheduleStaff.size() > 0 && row < scheduleStaff.size()) {
					value = scheduleStaff.get(row).getName();
				}
			}
			return value;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			// Only if view is byService is editable
			return (byService && col > 0 && application.userAccess[LibConstants.ACCESS_STP_SC]
					&& scheduleServices.get(row).get(col -1).isOn());
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (value instanceof ItemData) {
				altered = true;
				scheduleServices.get(row).get(col -1).setPerson((ItemData) value);
				save(scheduleServices.get(row).get(col -1));
			}
		}
	}
}