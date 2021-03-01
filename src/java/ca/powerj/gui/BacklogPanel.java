package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
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
import ca.powerj.data.CaseData;
import ca.powerj.data.TurnaroundData;
import ca.powerj.data.WorkdayData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IChartBar2Yaxis;
import ca.powerj.swing.IChartPie;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.IRendererColor;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class BacklogPanel extends BasePanel {
	private final byte CASE_ROW = 0;
	private final byte CASE_NO = 1;
	private final byte CASE_ACED = 2;
	private final byte CASE_FAC = 3;
	private final byte CASE_SPY = 4;
	private final byte CASE_SUB = 5;
	private final byte CASE_PROC = 6;
	private final byte CASE_STAT = 7;
	private final byte CASE_SPEC = 8;
	private final byte CASE_NOSP = 9;
	private final byte CASE_NOBL = 10;
	private final byte CASE_NOSL = 11;
	private final byte CASE_VAL5 = 12;
	private final byte CASE_CUTOFF = 13;
	private final byte CASE_PASSED = 14;
	private final byte CASE_DELAY = 15;
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_SUB = 2;
	private final byte FILTER_PRO = 3;
	private final byte FILTER_STA = 4;
	private int[] filters = { 0, 0, 0, 0, 8 };
	private final String[] columns = { "NO", "CASE", "ACCESS", "FAC", "SPY", "SUB", "PROC", "STATUS", "SPEC", "SPECS", "BLKS",
			"SLDS", application.getProperty("coder5"), "CUTOFF", "SPENT", "PRCNT" };
	private TurnaroundData turnaround = new TurnaroundData();
	private HashMap<Byte, TurnaroundData> turnarounds = null;
	private CaseData pending = new CaseData();
	private ArrayList<CaseData> pendings = new ArrayList<CaseData>();
	private ModelBacklog model = null;
	private ITable table;
	private IChartPie chartPie;
	private IChartBar2Yaxis chartBar;

	BacklogPanel(AppFrame application) {
		super(application);
		setName("Backlog");
		application.dbPowerJ.setStatements(LibConstants.ACTION_BACKLOG);
		if (application.getUserID() == -222 && application.isAutologin()) {
			filters[FILTER_STA] = LibConstants.STATUS_ACCES;
		} else if (application.getUserID() == -111 && application.isAutologin()) {
			filters[FILTER_STA] = LibConstants.STATUS_HISTO;
		}
		getTurnaround();
		createPanel();
		refresh();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		super.close();
		pendings.clear();
		turnarounds.clear();
		if (chartBar != null) {
			chartBar.close();
		}
		if (chartPie != null) {
			chartPie.close();
		}
		return true;
	}

	private void createPanel() {
		model = new ModelBacklog();
		table = new ITable(model, application.dates, application.numbers);
		table.addAncestorListener(new IFocusListener());
		table.addFocusListener(this);
		// Define color column renderer
		table.getColumnModel().getColumn(CASE_DELAY).setCellRenderer(new IRendererColor(application.numbers));
		// Set Row Counter Size
		table.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		table.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		JScrollPane scrollTable = IUtilities.createJScrollPane(table);
		scrollTable.setMinimumSize(new Dimension(1300, 400));
		Dimension dim = new Dimension(600, 400);
		chartPie = new IChartPie(dim);
		JScrollPane scrollPie = IUtilities.createJScrollPane(chartPie);
		scrollPie.setMinimumSize(dim);
		chartBar = new IChartBar2Yaxis(dim);
		JScrollPane scrollBar = IUtilities.createJScrollPane(chartBar);
		scrollBar.setMinimumSize(dim);
		JSplitPane splitChart = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitChart.setTopComponent(scrollPie);
		splitChart.setBottomComponent(scrollBar);
		splitChart.setOneTouchExpandable(true);
		splitChart.setDividerLocation(650);
		splitChart.setPreferredSize(new Dimension(1300, 400));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(splitChart);
		splitAll.setBottomComponent(scrollTable);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1300, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(application), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	private void getTurnaround() {
		turnarounds = new HashMap<Byte, TurnaroundData>();
		ArrayList<TurnaroundData> list = application.dbPowerJ.getTurnarounds();
		for (int i = 0; i < list.size(); i++) {
			turnarounds.put(list.get(i).getTurID(), list.get(i));
		}
		list.clear();
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("backlog.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 2, 2, 1, 1.5f, 1.5f, 1.5f, 1.5f, 2, 1, 1, 1, 1, 1, 1, 1 };
		String str = "Backlog - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER.rotate(), 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable pdfTable = new PdfPTable(columns.length);
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
			for (int col = 0; col < columns.length; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(columns[col]));
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
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case CASE_ROW:
						paragraph.add(new Chunk(application.numbers.formatNumber(row + 1)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NO:
						paragraph.add(new Chunk(pendings.get(i).getCaseNo()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_ACED:
						paragraph.add(new Chunk(application.dates.formatter(pendings.get(i).getAccessCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_FAC:
						paragraph.add(new Chunk(pendings.get(i).getFacName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPY:
						paragraph.add(new Chunk(pendings.get(i).getSpyName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SUB:
						paragraph.add(new Chunk(pendings.get(i).getSubName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_PROC:
						paragraph.add(new Chunk(pendings.get(i).getProcName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_STAT:
						paragraph.add(new Chunk(pendings.get(i).getStatusName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPEC:
						paragraph.add(new Chunk(pendings.get(i).getSpecName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_NOSP:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getNoSpecs())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOBL:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getNoBlocks())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSL:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getNoSlides())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_VAL5:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getValue5() / 60)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_CUTOFF:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getCutoff())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_PASSED:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getPassed())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_DELAY:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getDelay())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
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
	void refresh() {
		application.setBusy(true);
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	public void setFilter(short id, int value) {
		switch (id) {
		case IToolBar.TB_FAC:
			filters[FILTER_FAC] = value;
			break;
		case IToolBar.TB_PRO:
			filters[FILTER_PRO] = value;
			break;
		case IToolBar.TB_SPY:
			filters[FILTER_SPY] = value;
			break;
		case IToolBar.TB_STA:
			filters[FILTER_STA] = value;
			break;
		default:
			filters[FILTER_SUB] = value;
		}
		// Must initialize a new instance each time
		WorkerFilter worker = new WorkerFilter();
		worker.execute();
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("backlog.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Backlog");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Backlog - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length - 1));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col < columns.length - 1; col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellValue(columns[col + 1]);
				xlsCell.setCellStyle(styles.get("header"));
				switch (col + 1) {
				case CASE_ACED:
					sheet.setColumnWidth(col, 18 * 256); // 18 characters
					sheet.setDefaultColumnStyle(col, styles.get("datetime"));
					break;
				case CASE_NOSP:
				case CASE_NOBL:
				case CASE_NOSL:
				case CASE_CUTOFF:
				case CASE_PASSED:
				case CASE_DELAY:
				case CASE_VAL5:
					sheet.setColumnWidth(col, 10 * 256); // 10 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case CASE_FAC:
				case CASE_SUB:
					sheet.setColumnWidth(col, 5 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case CASE_SPY:
					sheet.setColumnWidth(col, 10 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case CASE_PROC:
					sheet.setColumnWidth(col, 8 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case CASE_SPEC:
					sheet.setColumnWidth(col, 12 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				default:
					sheet.setColumnWidth(col, 18 * 256); // 18 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				}
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length - 1; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col + 1) {
					case CASE_NO:
						xlsCell.setCellValue(pendings.get(i).getCaseNo());
						break;
					case CASE_ACED:
						xlsCell.setCellValue(pendings.get(i).getAccessCalendar());
						break;
					case CASE_FAC:
						xlsCell.setCellValue(pendings.get(i).getFacName());
						break;
					case CASE_SPY:
						xlsCell.setCellValue(pendings.get(i).getSpyName());
						break;
					case CASE_SUB:
						xlsCell.setCellValue(pendings.get(i).getSubName());
						break;
					case CASE_PROC:
						xlsCell.setCellValue(pendings.get(i).getProcName());
						break;
					case CASE_STAT:
						xlsCell.setCellValue(pendings.get(i).getStatusName());
						break;
					case CASE_SPEC:
						xlsCell.setCellValue(pendings.get(i).getSpecName());
						break;
					case CASE_NOSP:
						xlsCell.setCellValue(pendings.get(i).getNoSpecs());
						break;
					case CASE_NOBL:
						xlsCell.setCellValue(pendings.get(i).getNoBlocks());
						break;
					case CASE_NOSL:
						xlsCell.setCellValue(pendings.get(i).getNoSlides());
						break;
					case CASE_VAL5:
						xlsCell.setCellValue(pendings.get(i).getValue5() / 60);
						break;
					case CASE_CUTOFF:
						xlsCell.setCellValue(pendings.get(i).getCutoff());
						break;
					case CASE_PASSED:
						xlsCell.setCellValue(pendings.get(i).getPassed());
						break;
					case CASE_DELAY:
						xlsCell.setCellValue(pendings.get(i).getDelay());
						break;
					default:
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

	private class ModelBacklog extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case CASE_ACED:
				return Calendar.class;
			case CASE_ROW:
			case CASE_NOBL:
			case CASE_NOSL:
			case CASE_CUTOFF:
			case CASE_PASSED:
			case CASE_DELAY:
				return Short.class;
			case CASE_NOSP:
				return Byte.class;
			case CASE_VAL5:
				return Integer.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int col) {
			return columns[col];
		}

		@Override
		public int getRowCount() {
			return pendings.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (pendings.size() > 0 && row < pendings.size()) {
				switch (col) {
				case CASE_ROW:
					value = table.convertRowIndexToView(row) + 1;
					break;
				case CASE_NO:
					value = pendings.get(row).getCaseNo();
					break;
				case CASE_ACED:
					value = pendings.get(row).getAccessCalendar();
					break;
				case CASE_FAC:
					value = pendings.get(row).getFacName();
					break;
				case CASE_SPY:
					value = pendings.get(row).getSpyName();
					break;
				case CASE_SUB:
					value = pendings.get(row).getSubName();
					break;
				case CASE_PROC:
					value = pendings.get(row).getProcName();
					break;
				case CASE_STAT:
					value = pendings.get(row).getStatusName();
					break;
				case CASE_SPEC:
					value = pendings.get(row).getSpecName();
					break;
				case CASE_NOSP:
					value = pendings.get(row).getNoSpecs();
					break;
				case CASE_NOBL:
					value = pendings.get(row).getNoBlocks();
					break;
				case CASE_NOSL:
					value = pendings.get(row).getNoSlides();
					break;
				case CASE_VAL5:
					value = pendings.get(row).getValue5() / 60;
					break;
				case CASE_CUTOFF:
					value = pendings.get(row).getCutoff();
					break;
				case CASE_PASSED:
					value = pendings.get(row).getPassed();
					break;
				case CASE_DELAY:
					value = pendings.get(row).getDelay();
					break;
				default:
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			short cutoff = 0;
			int buffer = 0;
			Calendar calToday = Calendar.getInstance();
			pendings.clear();
			pendings = application.dbPowerJ.getPendings(LibConstants.STATUS_ALL);
			for (int i = 0; i < pendings.size(); i++) {
				pending = pendings.get(i);
				turnaround = turnarounds.get(pending.getTurnaroundID());
				cutoff = turnaround.getGross();
				if (pending.getStatusID() > LibConstants.STATUS_ACCES) {
					cutoff += turnaround.getEmbed();
				}
				if (pending.getStatusID() > LibConstants.STATUS_GROSS) {
					cutoff += turnaround.getMicrotomy();
				}
				if (pending.getStatusID() > LibConstants.STATUS_EMBED) {
					cutoff += turnaround.getRoute();
				}
				if (pending.getStatusID() > LibConstants.STATUS_MICRO) {
					cutoff += turnaround.getDiagnosis();
				}
				if (pending.getStatusID() == LibConstants.STATUS_FINAL) {
					buffer = pending.getFinalTAT();
				} else {
					buffer = application.dates.getBusinessHours(pending.getAccessCalendar(), calToday);
				}
				if (buffer > 9999) {
					buffer = 9999;
				}
				pending.setCutoff(cutoff);
				pending.setPassed((short) buffer);
				if (pending.getCutoff() > 0) {
					buffer = (100 * pending.getPassed()) / pending.getCutoff();
					if (buffer > 9999) {
						buffer = 9999;
					}
					pending.setDelay((short) buffer);
				}
			}
			Collections.sort(pendings, new Comparator<CaseData>() {
				@Override
				public int compare(CaseData o1, CaseData o2) {
					return (o1.getDelay() > o2.getDelay() ? -1 : (o1.getDelay() < o2.getDelay() ? 1 : 0));
				}
			});
			return null;
		}

		@Override
		public void done() {
			if (model != null) {
				// Display results
				model.fireTableDataChanged();
			}
			// Must initialize a new instance each time
			WorkerFilter worker = new WorkerFilter();
			worker.execute();
		}
	}

	private class WorkerFilter extends SwingWorker<Void, Void> {
		// Pie Chart Data Set
		String[] xPie = { "Red", "Amber", "Green" };
		double[] yPie;
		// Flow Chart Data Set
		String[] xDates;
		double[][] yDates;

		@Override
		protected Void doInBackground() throws Exception {
			double counter = 0;
			WorkdayData workday = new WorkdayData();
			ArrayList<WorkdayData> workdays = new ArrayList<WorkdayData>();
			ArrayList<ArrayList<Double>> workflow = new ArrayList<ArrayList<Double>>();
			Calendar calStart = application.dates.setMidnight(null);
			workday = new WorkdayData();
			workday.setDate(calStart.getTimeInMillis());
			workday.setName(application.dates.formatter(calStart, LibDates.FORMAT_DATESHORT));
			workdays.add(workday);
			while (workdays.size() < 7) {
				calStart.setTimeInMillis(application.dates.getPreviousBusinessDay(calStart));
				workday = new WorkdayData();
				workday.setDate(calStart.getTimeInMillis());
				workday.setName(application.dates.formatter(calStart, LibDates.FORMAT_DATESHORT));
				workdays.add(workday);
			}
			Collections.sort(workdays, new Comparator<WorkdayData>() {
				@Override
				public int compare(WorkdayData o1, WorkdayData o2) {
					return (o1.getTime() < o2.getTime() ? -1
							: (o1.getTime() > o2.getTime() ? 1 : 0));
				}
			});
			for (int x = 0; x < 3; x++) {
				workflow.add(new ArrayList<Double>());
				for (int y = 0; y < workdays.size(); y++) {
					workflow.get(x).add(0d);
				}
			}
			yPie = new double[3];
			for (CaseData pending : pendings) {
				if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == pending.getFacID()) {
					if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == pending.getSpyID()) {
						if (filters[FILTER_SUB] == 0 || filters[FILTER_SUB] == pending.getSubID()) {
							if (filters[FILTER_PRO] == 0 || filters[FILTER_PRO] == pending.getProcID()) {
								if (filters[FILTER_STA] == LibConstants.STATUS_ALL || filters[FILTER_STA] == pending.getStatusID()
										|| (filters[FILTER_STA] == LibConstants.STATUS_HISTO
												&& pending.getStatusID() > LibConstants.STATUS_ACCES
												&& pending.getStatusID() < LibConstants.STATUS_ROUTE)) {
									if (pending.getStatusID() < LibConstants.STATUS_FINAL) {
										if (pending.getDelay() > 100) {
											yPie[0]++;
										} else if (pending.getDelay() > 70) {
											yPie[1]++;
										} else {
											yPie[2]++;
										}
									}
								}
								switch (filters[FILTER_STA]) {
								case LibConstants.STATUS_ACCES:
									for (int x = workdays.size() - 1; x >= 0; x--) {
										if (pending.getAccessTime() > workdays.get(x).getTime()) {
											// Date In
											counter = 1.0 + workflow.get(0).get(x);
											workflow.get(0).set(x, counter);
											break;
										}
									}
									if (pending.getStatusID() == LibConstants.STATUS_ACCES) {
										// Still Pending
										counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
										workflow.get(2).set(workdays.size() - 1, counter);
									} else {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.getGrossTime() > workdays.get(x).getTime()) {
												// Date Out
												counter = 1.0 + workflow.get(1).get(x);
												workflow.get(1).set(x, counter);
												break;
											}
										}
									}
									break;
								case LibConstants.STATUS_GROSS:
									if (pending.getStatusID() > LibConstants.STATUS_ACCES) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.getGrossTime() > workdays.get(x).getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.getStatusID() == LibConstants.STATUS_GROSS) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.getEmbedTime() > workdays.get(x).getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case LibConstants.STATUS_EMBED:
									if (pending.getStatusID() > LibConstants.STATUS_GROSS) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.getEmbedTime() > workdays.get(x).getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.getStatusID() == LibConstants.STATUS_EMBED) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.getMicroTime() > workdays.get(x).getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case LibConstants.STATUS_MICRO:
									if (pending.getStatusID() > LibConstants.STATUS_EMBED) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.getMicroTime() > workdays.get(x).getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.getStatusID() == LibConstants.STATUS_MICRO) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.getRouteTime() > workdays.get(x).getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case LibConstants.STATUS_ROUTE:
									if (pending.getStatusID() > LibConstants.STATUS_MICRO) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.getRouteTime() > workdays.get(x).getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.getStatusID() == LibConstants.STATUS_ROUTE) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.getFinalTime() > workdays.get(x).getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case LibConstants.STATUS_DIAGN:
									if (pending.getStatusID() > LibConstants.STATUS_ROUTE) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.getFinalTime() > workdays.get(x).getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.getStatusID() == LibConstants.STATUS_DIAGN) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.getFinalTime() > workdays.get(x).getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case LibConstants.STATUS_HISTO:
									if (pending.getStatusID() > LibConstants.STATUS_ACCES) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.getGrossTime() > workdays.get(x).getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.getStatusID() < LibConstants.STATUS_ROUTE) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.getRouteTime() > workdays.get(x).getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								default:
									// All pending cases
									for (int x = workdays.size() - 1; x >= 0; x--) {
										if (pending.getAccessTime() > workdays.get(x).getTime()) {
											// Date In
											counter = 1.0 + workflow.get(0).get(x);
											workflow.get(0).set(x, counter);
											break;
										}
									}
									if (pending.getStatusID() < LibConstants.STATUS_FINAL) {
										// Still Pending
										counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
										workflow.get(2).set(workdays.size() - 1, counter);
									} else {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.getFinalTime() > workdays.get(x).getTime()) {
												// Date Out
												counter = 1.0 + workflow.get(1).get(x);
												workflow.get(1).set(x, counter);
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			for (int x = workdays.size() - 2; x >= 0; x--) {
				counter = workflow.get(2).get(x + 1) + workflow.get(1).get(x + 1) - workflow.get(0).get(x + 1);
				workflow.get(2).set(x, counter);
			}
			// Flow Chart Data Set
			xDates = new String[workdays.size() - 1];
			yDates = new double[3][workdays.size() - 1];
			for (int x = 0; x < xDates.length; x++) {
				xDates[x] = workdays.get(x + 1).getName();
				for (int y = 0; y < 3; y++) {
					yDates[y][x] = workflow.get(y).get(x + 1);
				}
			}
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		public void done() {
			if (chartPie != null) {
				// Display results
				chartPie.setChart(xPie, yPie, "Cases Status", IChartPie.COLOR_RAG);
				String[] legend = { "In", "Out", "Pending" };
				chartBar.setChart(xDates, legend, yDates, "Cases Workflow");
			}
			if (table != null) {
				RowFilter<AbstractTableModel, Integer> rowFilter = null;
				// When multiple Filters are required
				ArrayList<RowFilter<AbstractTableModel, Integer>> rowFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
				if (filters[FILTER_FAC] > 0) {
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).getFacID() == filters[FILTER_FAC]);
						}
					};
					rowFilters.add(rowFilter);
				}
				if (filters[FILTER_PRO] > 0) {
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).getProcID() == filters[FILTER_PRO]);
						}
					};
					rowFilters.add(rowFilter);
				}
				if (filters[FILTER_SPY] > 0) {
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).getSpyID() == filters[FILTER_SPY]);
						}
					};
					rowFilters.add(rowFilter);
				}
				if (filters[FILTER_SUB] > 0) {
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).getSubID() == filters[FILTER_SUB]);
						}
					};
					rowFilters.add(rowFilter);
				}
				// Status filter is always on
				switch (filters[FILTER_STA]) {
				case LibConstants.STATUS_ALL:
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).getStatusID() < LibConstants.STATUS_FINAL);
						}
					};
					break;
				case LibConstants.STATUS_HISTO:
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).getStatusID() > LibConstants.STATUS_ACCES
									&& pendings.get(entry.getIdentifier()).getStatusID() < LibConstants.STATUS_ROUTE);
						}
					};
					break;
				default:
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).getStatusID() == filters[FILTER_STA]);
						}
					};
				}
				rowFilters.add(rowFilter);
				if (rowFilters.size() > 0) {
					if (rowFilters.size() > 1) {
						// Add to the compound filter
						rowFilter = RowFilter.andFilter(rowFilters);
					}
					TableRowSorter<ModelBacklog> sorter = (TableRowSorter<ModelBacklog>) table.getRowSorter();
					sorter.setRowFilter(rowFilter);
					sorter.sort();
				}
			}
			application.display("Next update "
					+ application.dates.formatter(application.getNextUpdate(), LibDates.FORMAT_DATETIME));
			application.setBusy(false);
		}
	}
}