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
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
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
import ca.powerj.data.ScheduleSumData;
import ca.powerj.data.ServiceSumData;
import ca.powerj.data.WorkdaySumData;
import ca.powerj.data.WorkdayTempData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IChartBar;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class WorkdaysPanel extends BasePanel {
	private boolean groupByService = true;
	private short facID = 0;
	private long timeFrom = 0;
	private long timeTo = 0;
	volatile ArrayList<ItemData> headers = new ArrayList<ItemData>();
	volatile ArrayList<WorkdaySumData> rows = new ArrayList<WorkdaySumData>();
	private ModelWorkdays model;
	private ITable table;
	private IChartBar chartBar;

	public WorkdaysPanel(AppFrame application) {
		super(application);
		setName("Workdays");
		application.dbPowerJ.setStatements(LibConstants.ACTION_WORKDAYS);
		createPanel();
		programmaticChange = false;
		altered = true;
	}

	@Override
	public boolean close() {
		altered = false;
		super.close();
		headers.clear();
		rows.clear();
		if (chartBar != null) {
			chartBar.close();
		}
		return true;
	}

	private void createPanel() {
		model = new ModelWorkdays();
		table = new ITable(model, application.dates, application.numbers) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					String s = "";
					if (m >= 0 && m < rows.size()) {
						s = rows.get(m).getPrsFull();
					}
					return s;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		JScrollPane scrollList = IUtilities.createJScrollPane(table);
		Dimension dim = new Dimension(1000, 300);
		chartBar = new IChartBar(dim);
		JScrollPane scrollChart = IUtilities.createJScrollPane(chartBar);
		scrollChart.setMinimumSize(dim);
		Calendar calStart = application.dates.setMidnight(null);
		Calendar calEnd = application.dates.setMidnight(null);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = Calendar.getInstance();
		calStart.add(Calendar.YEAR, -1);
		timeFrom = calStart.getTimeInMillis();
		timeTo = calEnd.getTimeInMillis();
		calMax.setTimeInMillis(timeTo);
		calMin.setTimeInMillis(application.setup.getLong(LibSetup.VAR_MIN_WL_DATE));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(scrollChart);
		splitAll.setBottomComponent(scrollList);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(application, calStart, calEnd, calMin, calMax, null), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		if (headers.size() == 0)
			return;
		String fileName = application.getFilePdf("workdays.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = new float[headers.size()];
		String str = "Workdays " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATELONG) + " - "
				+ application.dates.formatter(timeTo, LibDates.FORMAT_DATELONG);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable pdfTable = new PdfPTable(headers.size());
		for (int col = 0; col < headers.size(); col++) {
			widths[col] = 1;
		}
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
			for (int col = 0; col < headers.size(); col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(headers.get(col).getName()));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				pdfTable.addCell(cell);
			}
			pdfTable.setHeaderRows(1);
			// data rows
			int i = 0;
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				for (int col = 0; col < headers.size(); col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					if (col == 0) {
						paragraph.add(new Chunk(rows.get(i).getPrsName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					} else if (col > 1) {
						paragraph.add(new Chunk(application.numbers.formatNumber(rows.get(i).getNoServices(col -2))));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					} else {
						paragraph.add(new Chunk(application.numbers.formatNumber(rows.get(i).getNoDays())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					}
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

	@Override
	public void setFilter(short id, int value) {
		switch (id) {
		case IToolBar.TB_FAC:
			facID = (short) value;
			altered = true;
			break;
		case IToolBar.TB_SPIN:
			groupByService = !groupByService;
			if (timeTo > timeFrom) {
				WorkerData worker = new WorkerData();
				worker.execute();
			}
			break;
		default:
			if (altered && timeTo > timeFrom) {
				application.setBusy(true);
				WorkerData worker = new WorkerData();
				worker.execute();
				altered = false;
			}
		}
	}

	@Override
	public void setFilter(short id, Calendar value) {
		switch (id) {
		case IToolBar.TB_FROM:
			timeFrom = value.getTimeInMillis();
			break;
		default:
			timeTo = value.getTimeInMillis();
		}
		altered = true;
	}

	@Override
	void xls() {
		if (headers.size() == 0)
			return;
		String fileName = application.getFileXls("workdays.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Workdays");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Workdays " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATELONG) + " - "
					+ application.dates.formatter(timeTo, LibDates.FORMAT_DATELONG));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col < headers.size(); col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellStyle(styles.get("header"));
				xlsCell.setCellValue(headers.get(col).getName());
				if (col == 0) {
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				} else {
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
				}
				sheet.setColumnWidth(col, 6 * 256); // 6 characters
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < headers.size(); col++) {
					xlsCell = xlsRow.createCell(col);
					if (col == 0) {
						xlsCell.setCellValue(rows.get(i).getPrsName());
					} else if (col > 1) {
						xlsCell.setCellValue(rows.get(i).getNoServices(col -2));
					} else {
						xlsCell.setCellValue(rows.get(i).getNoDays());
					}
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

	private class ModelWorkdays extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			}
			return Short.class;
		}

		@Override
		public int getColumnCount() {
			return headers.size();
		}

		@Override
		public String getColumnName(int col) {
			return headers.get(col).getName();
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (rows.size() > 0 && row < rows.size()) {
				switch (col) {
				case 0:
					value = rows.get(row).getPrsName();
					break;
				case 1:
					value = rows.get(row).getNoDays();
					break;
				default:
					value = rows.get(row).getNoServices(col -2);
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			short serviceID = 0;
			short personID = 0;
			int dayID = 0;
			int noServices = 0;
			String serviceName = "";
			ServiceSumData prsSrvce = new ServiceSumData();
			WorkdayTempData person = new WorkdayTempData();
			WorkdaySumData row = new WorkdaySumData();
			ArrayList<ScheduleSumData> tempList = application.dbPowerJ.getScheduleSums(timeFrom, timeTo);
			HashMap<Short, WorkdayTempData> persons = new HashMap<Short, WorkdayTempData>();
			HashMap<Short, String> services = new HashMap<Short, String>();
			setName("WorkerData");
			headers.clear();
			rows.clear();
			for (int i = 0; i < tempList.size(); i++) {
				if (facID == 0 && facID == tempList.get(i).getFacID()) {
					if (personID != tempList.get(i).getPrsID()) {
						personID = tempList.get(i).getPrsID();
						dayID = 0;
						serviceID = 0;
						person = persons.get(personID);
						if (person == null) {
							person = new WorkdayTempData();
							person.setPrsID(personID);
							person.setPrsName(tempList.get(i).getPrsName());
							person.setPrsFull(tempList.get(i).getPrsFull());
							persons.put(personID, person);
						}
					}
					if (dayID != tempList.get(i).getDayID()) {
						dayID = tempList.get(i).getDayID();
						person.setNoDays(person.getNoDays() +1);
					}
					if (groupByService) {
						if (serviceID != tempList.get(i).getSrvID()) {
							serviceID = tempList.get(i).getSrvID();
							prsSrvce = person.getService(serviceID);
							if (prsSrvce == null) {
								prsSrvce = new ServiceSumData();
								prsSrvce.setSrvName(tempList.get(i).getSrvName());
								person.setService(serviceID, prsSrvce);
							}
							serviceName = services.get(serviceID);
							if (serviceName == null) {
								services.put(serviceID, tempList.get(i).getSrvName());
							}
						}
					} else {
						if (serviceID != tempList.get(i).getSubID()) {
							serviceID = tempList.get(i).getSubID();
							prsSrvce = person.getService(serviceID);
							if (prsSrvce == null) {
								prsSrvce = new ServiceSumData();
								prsSrvce.setSrvName(tempList.get(i).getSubName());
								person.setService(serviceID, prsSrvce);
							}
							serviceName = services.get(serviceID);
							if (serviceName == null) {
								services.put(serviceID, tempList.get(i).getSubName());
							}
						}
					}
					prsSrvce.setNoDays();
				}
			}
			ArrayList<ItemData> temp = new ArrayList<ItemData>();
			for (Entry<Short, String> entry : services.entrySet()) {
				temp.add(new ItemData(entry.getKey(), entry.getValue()));
			}
			noServices = temp.size();
			Collections.sort(temp, new Comparator<ItemData>() {
				@Override
				public int compare(ItemData o1, ItemData o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
			headers.add(new ItemData((short) 0, "NAME"));
			headers.add(new ItemData((short) 0, "DAYS"));
			for (int i = 0; i < noServices; i++) {
				headers.add(temp.get(i));
			}
			for (Entry<Short, WorkdayTempData> entry : persons.entrySet()) {
				person = entry.getValue();
				row = new WorkdaySumData();
				row.setPrsID(person.getPrsID());
				row.setPrsName(person.getPrsName());
				row.setPrsFull(person.getPrsFull());
				row.setNoDays(person.getNoDays());
				row.setNoServices(noServices);
				for (int i = 0; i < noServices; i++) {
					prsSrvce = person.getService(temp.get(i).getID());
					if (prsSrvce == null) {
						row.setNoServiceDays(i, 0);
					} else if (temp.get(i).getName().equals(prsSrvce.getSrvName())) {
						row.setNoServiceDays(i, prsSrvce.getNoDays());
					} else {
						row.setNoServiceDays(i, 0);
					}
				}
				rows.add(row);
			}
			Collections.sort(rows, new Comparator<WorkdaySumData>() {
				@Override
				public int compare(WorkdaySumData o1, WorkdaySumData o2) {
					return (o1.getNoDays() > o2.getNoDays() ? -1
							: (o1.getNoDays() < o2.getNoDays() ? 1
							: o1.getPrsName().compareToIgnoreCase(o2.getPrsName())));
				}
			});
			row = new WorkdaySumData();
			row.setPrsName("Ztotal");
			row.setPrsFull("Total");
			row.setNoServices(noServices);
			for (int i = 0; i < rows.size() - 1; i++) {
				row.setNoDays(row.getNoDays() + rows.get(i).getNoDays());
				for (int j = 0; j < noServices; j++) {
					row.setNoServiceDays(j, row.getNoServices(j) + rows.get(i).getNoServices(j));
				}
			}
			rows.add(row);
			persons.clear();
			return null;
		}

		@Override
		public void done() {
			model.fireTableStructureChanged();
			if (rows.size() > 1) {
				// Chart Data Set
				String[] x = new String[rows.size() - 1];
				double[] y = new double[rows.size() - 1];
				for (int i = 0; i < rows.size(); i++) {
					if (rows.get(i).getPrsID() > 0) {
						x[i] = rows.get(i).getPrsName();
						y[i] = rows.get(i).getNoDays();
					}
				}
				chartBar.setChart(x, y, "Workdays");
			}
			application.display("Workdays " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATELONG) + " - "
					+ application.dates.formatter(timeTo, LibDates.FORMAT_DATELONG));
			application.setBusy(false);
		}
	}
}