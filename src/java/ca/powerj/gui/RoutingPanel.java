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
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import ca.powerj.data.WorkflowData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IChartBar;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class RoutingPanel extends BasePanel {
	private final byte CASE_ROW = 0;
	private final byte CASE_NO = 1;
	private final byte CASE_FAC = 2;
	private final byte CASE_SPY = 3;
	private final byte CASE_SUB = 4;
	private final byte CASE_PROC = 5;
	private final byte CASE_SPEC = 6;
	private final byte CASE_NOSP = 7;
	private final byte CASE_NOBL = 8;
	private final byte CASE_NOSL = 9;
	private final byte CASE_V5 = 10;
	private final byte CASE_ACED = 11;
	private final byte CASE_ROED = 12;
	private final byte CASE_ROBY = 13;
	private final byte CASE_FIBY = 14;
	private final byte SUM_ROW = 0;
	private final byte SUM_INIT = 1;
	private final byte SUM_NAME = 2;
	private final byte SUM_CASES = 3;
	private final byte SUM_SPECS = 4;
	private final byte SUM_SLIDES = 5;
	private final byte SUM_V5 = 6;
	private final byte SUM_FTE = 7;
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_SUB = 2;
	private final byte FILTER_INT = 3;
	private int[] filters = { 0, 0, 0, 1 };
	private int routeTime = 0;
	private int rowIndex = 0;
	private int noCases = 0, noSlides = 0;
	private double noFTE = 0.0;
	private double v5FTE = 0.0;
	private String[] colCases = { "NO", "CASE", "FAC", "SPY", "SUB", "PROC", "SPEC", "SPECS", "BLKS", "SLDS", "",
			"ACCESS", "ROUTE", "BY", "TO" };
	private String[] colSum = { "NO", "INIT", "NAME", "CASES", "SPECS", "SLIDES", "", "FTE" };
	private ArrayList<Calendar> dates = new ArrayList<Calendar>();
	private ArrayList<CaseData> cases = new ArrayList<CaseData>();
	private ArrayList<WorkflowData> summary = new ArrayList<WorkflowData>();
	private ModelDate modelDate;
	private ModelCases modelCases;
	private ModelSummary modelSummary;
	private ITable tblList, tblCases, tblSummary;
	private IChartBar chartBar;

	RoutingPanel(AppFrame application) {
		super(application);
		setName("Routing");
		application.dbPowerJ.setStatements(LibConstants.ACTION_ROUTING);
		routeTime = application.setup.getInt(LibSetup.VAR_ROUTE_TIME);
		colSum[6] = application.getProperty("coder5");
		colCases[10] = colSum[6];
		v5FTE = Double.parseDouble(application.setup.getString(LibSetup.VAR_V5_FTE)) / 215;
		if (v5FTE < 1.00) {
			v5FTE = 1.00;
		}
		refresh();
		createPanel();
		setFilter(IToolBar.TB_FAC, filters[0]);
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		super.close();
		cases.clear();
		summary.clear();
		if (chartBar != null) {
			chartBar.close();
		}
		return true;
	}

	private void createPanel() {
		modelDate = new ModelDate();
		tblList = new ITable(modelDate, application.dates, application.numbers);
		tblList.addAncestorListener(new IFocusListener());
		tblList.addFocusListener(this);
		tblList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				if (index > -1) {
					setRow(tblList.convertRowIndexToModel(index));
				}
			}
		});
		tblList.getColumnModel().getColumn(0).setMinWidth(120);
		JScrollPane scrollTable = IUtilities.createJScrollPane(tblList);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		modelCases = new ModelCases();
		tblCases = new ITable(modelCases, application.dates, application.numbers) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					switch (columnAtPoint(p)) {
					case CASE_ROBY:
						return cases.get(m).getRouteFull();
					case CASE_FIBY:
						return cases.get(m).getFinalFull();
					default:
						return null;
					}
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblCases.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		tblCases.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		modelSummary = new ModelSummary();
		tblSummary = new ITable(modelSummary, application.dates, application.numbers);
		tblSummary.getColumnModel().getColumn(SUM_ROW).setMinWidth(50);
		tblSummary.getColumnModel().getColumn(SUM_ROW).setMaxWidth(50);
		JScrollPane scrollCases = IUtilities.createJScrollPane(tblCases);
		scrollCases.setMinimumSize(new Dimension(800, 200));
		JScrollPane scrollSummary = IUtilities.createJScrollPane(tblSummary);
		scrollSummary.setMinimumSize(new Dimension(800, 200));
		Dimension dim = new Dimension(1000, 300);
		chartBar = new IChartBar(dim);
		JScrollPane scrollChart = IUtilities.createJScrollPane(chartBar);
		scrollChart.setMinimumSize(dim);
		JSplitPane splitBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitBottom.setTopComponent(scrollSummary);
		splitBottom.setBottomComponent(scrollCases);
		splitBottom.setOneTouchExpandable(true);
		splitBottom.setDividerLocation(250);
		splitBottom.setPreferredSize(new Dimension(800, 500));
		JSplitPane splitTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitTop.setTopComponent(scrollChart);
		splitTop.setBottomComponent(splitBottom);
		splitTop.setOneTouchExpandable(true);
		splitTop.setDividerLocation(350);
		splitTop.setPreferredSize(new Dimension(800, 900));
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(splitTop);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(application), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("routing.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widthSum = { 1, 1, 3, 1, 1, 1, 1, 1 };
		String str = "Routing Summary - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER, 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable pdfTable = new PdfPTable(colSum.length);
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
			pdfTable.setWidths(widthSum);
			for (int col = 0; col < colSum.length; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(colSum[col]));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				pdfTable.addCell(cell);
			}
			pdfTable.setHeaderRows(1);
			// data rows
			int i = 0;
			for (int row = 0; row < tblSummary.getRowCount(); row++) {
				i = tblSummary.convertRowIndexToModel(row);
				for (int col = 0; col < colSum.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case SUM_ROW:
						paragraph.add(new Chunk(application.numbers.formatNumber(row + 1)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SUM_INIT:
						paragraph.add(new Chunk(summary.get(i).getName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SUM_NAME:
						paragraph.add(new Chunk(summary.get(i).getFull()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SUM_CASES:
						paragraph.add(new Chunk(application.numbers.formatNumber(summary.get(i).getNoCases())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SUM_SPECS:
						paragraph.add(new Chunk(application.numbers.formatNumber(summary.get(i).getNoSpecs())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SUM_SLIDES:
						paragraph.add(new Chunk(application.numbers.formatNumber(summary.get(i).getNoSlides())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SUM_V5:
						paragraph.add(new Chunk(application.numbers.formatNumber(summary.get(i).getValue5() / 60)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
						paragraph.add(new Chunk(application.numbers.formatDouble(2, summary.get(i).getFtIn())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					}
					cell.addElement(paragraph);
					pdfTable.addCell(cell);
				}
			}
			document.add(pdfTable);
			// Second page
			document.setPageSize(PageSize.LETTER.rotate());
			document.setMargins(36, 18, 18, 18);
			document.newPage();
			final float[] widthCases = { 1, 1.5f, 1, 1.5f, 1, 1.5f, 1.5f, 1, 1, 1, 1, 1.5f, 1.5f, 1, 1 };
			str = "Routing Cases - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
			paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.add(new Chunk(application.setup.getString(LibSetup.VAR_LAB_NAME)));
			document.add(paragraph);
			paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
			document.add(paragraph);
			document.add(Chunk.NEWLINE);
			pdfTable = new PdfPTable(colCases.length);
			pdfTable.setWidthPercentage(100);
			pdfTable.setWidths(widthCases);
			for (int col = 0; col < colCases.length; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(colCases[col]));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				pdfTable.addCell(cell);
			}
			pdfTable.setHeaderRows(1);
			// data rows
			for (int row = 0; row < tblCases.getRowCount(); row++) {
				i = tblCases.convertRowIndexToModel(row);
				for (int col = 0; col < colCases.length; col++) {
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
						paragraph.add(new Chunk(cases.get(i).getCaseNo()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_FAC:
						paragraph.add(new Chunk(cases.get(i).getFacName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPY:
						paragraph.add(new Chunk(cases.get(i).getSpyName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SUB:
						paragraph.add(new Chunk(cases.get(i).getSubName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_PROC:
						paragraph.add(new Chunk(cases.get(i).getProcName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPEC:
						paragraph.add(new Chunk(cases.get(i).getSpecName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_NOSP:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoSpecs())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOBL:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoBlocks())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSL:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoSlides())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_V5:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getValue5() / 60)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ACED:
						paragraph.add(new Chunk(application.dates.formatter(cases.get(i).getAccessCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROED:
						paragraph.add(new Chunk(application.dates.formatter(cases.get(i).getRouteCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROBY:
						paragraph.add(new Chunk(cases.get(i).getRouteName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(cases.get(i).getFinalName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
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
		if (application.isBusy())
			return;
		application.setBusy(true);
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	public void setFilter(short id, int value) {
		TableRowSorter<ModelCases> sorter = (TableRowSorter<ModelCases>) tblCases.getRowSorter();
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		switch (id) {
		case IToolBar.TB_FAC:
			filters[FILTER_FAC] = value;
			break;
		case IToolBar.TB_SPY:
			filters[FILTER_SPY] = value;
			break;
		case IToolBar.TB_SUB:
			filters[FILTER_SUB] = value;
			break;
		case IToolBar.TB_INTERVAL:
			filters[FILTER_INT] = value;
			WorkerData worker = new WorkerData();
			worker.execute();
			return;
		default:
			application.log(LibConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
			return;
		}
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> rowFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[FILTER_FAC] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (cases.get(entry.getIdentifier()).getFacID() == filters[FILTER_FAC]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SPY] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (cases.get(entry.getIdentifier()).getSpyID() == filters[FILTER_SPY]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SUB] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (cases.get(entry.getIdentifier()).getSubID() == filters[FILTER_SUB]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (rowFilters.size() > 0) {
			// Add to the compound filter
			rowFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(rowFilters);
		}
		sorter.setRowFilter(rowFilter);
		sorter.sort();
		application.display("No Rows: " + application.numbers.formatNumber(tblCases.getRowCount()));
		// Must initialize a new instance each time
		WorkerSummary worker = new WorkerSummary();
		worker.execute();
	}

	private void setRow(int index) {
		if (index >= 0 && index < dates.size() - 1) {
			application.setBusy(true);
			rowIndex = index;
			// Must initialize a new instance each time
			WorkerData worker = new WorkerData();
			worker.execute();
		}
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("routing.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Summary");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Routing - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colSum.length - 1));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col < colSum.length; col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellValue(colSum[col]);
				xlsCell.setCellStyle(styles.get("header"));
				switch (col) {
				case SUM_ROW:
				case SUM_CASES:
				case SUM_SPECS:
				case SUM_SLIDES:
				case SUM_V5:
					sheet.setColumnWidth(col, 5 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case SUM_FTE:
					sheet.setColumnWidth(col, 6 * 256); // 6 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_float"));
					break;
				case SUM_NAME:
					sheet.setColumnWidth(col, 20 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				default:
					sheet.setColumnWidth(col, 5 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				}
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < tblSummary.getRowCount(); row++) {
				i = tblSummary.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < colSum.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case SUM_ROW:
						xlsCell.setCellValue(row + 1);
						break;
					case SUM_INIT:
						xlsCell.setCellValue(summary.get(i).getName());
						break;
					case SUM_NAME:
						xlsCell.setCellValue(summary.get(i).getFull());
						break;
					case SUM_CASES:
						xlsCell.setCellValue(summary.get(i).getNoCases());
						break;
					case SUM_SPECS:
						xlsCell.setCellValue(summary.get(i).getNoSpecs());
						break;
					case SUM_SLIDES:
						xlsCell.setCellValue(summary.get(i).getNoSlides());
						break;
					case SUM_V5:
						xlsCell.setCellValue(summary.get(i).getValue5() / 60);
						break;
					default:
						xlsCell.setCellValue(summary.get(i).getFtIn());
					}
				}
			}
			sheet.createFreezePane(1, 2);
			// Second sheet
			sheet = wb.createSheet("Cases");
			// title row
			xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Routing - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colCases.length - 1));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col < colCases.length; col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellValue(colCases[col]);
				xlsCell.setCellStyle(styles.get("header"));
				switch (col) {
				case CASE_ACED:
				case CASE_ROED:
					sheet.setColumnWidth(col, 18 * 256); // 18 characters
					sheet.setDefaultColumnStyle(col, styles.get("datetime"));
					break;
				case CASE_ROW:
				case CASE_NOSP:
				case CASE_NOBL:
				case CASE_NOSL:
				case CASE_V5:
					sheet.setColumnWidth(col, 5 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case CASE_FAC:
				case CASE_SUB:
				case CASE_ROBY:
				case CASE_FIBY:
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
			rownum = 2;
			for (int row = 0; row < tblCases.getRowCount(); row++) {
				i = tblCases.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < colCases.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case CASE_ROW:
						xlsCell.setCellValue(row + 1);
						break;
					case CASE_NO:
						xlsCell.setCellValue(cases.get(i).getCaseNo());
						break;
					case CASE_FAC:
						xlsCell.setCellValue(cases.get(i).getFacName());
						break;
					case CASE_SPY:
						xlsCell.setCellValue(cases.get(i).getSpyName());
						break;
					case CASE_SUB:
						xlsCell.setCellValue(cases.get(i).getSubName());
						break;
					case CASE_PROC:
						xlsCell.setCellValue(cases.get(i).getProcName());
						break;
					case CASE_SPEC:
						xlsCell.setCellValue(cases.get(i).getSpecName());
						break;
					case CASE_NOSP:
						xlsCell.setCellValue(cases.get(i).getNoSpecs());
						break;
					case CASE_NOBL:
						xlsCell.setCellValue(cases.get(i).getNoBlocks());
						break;
					case CASE_NOSL:
						xlsCell.setCellValue(cases.get(i).getNoSlides());
						break;
					case CASE_V5:
						xlsCell.setCellValue(cases.get(i).getValue5() / 60);
						break;
					case CASE_ACED:
						xlsCell.setCellValue(cases.get(i).getAccessCalendar());
						break;
					case CASE_ROED:
						xlsCell.setCellValue(cases.get(i).getRouteCalendar());
						break;
					case CASE_ROBY:
						xlsCell.setCellValue(cases.get(i).getRouteName());
						break;
					default:
						xlsCell.setCellValue(cases.get(i).getFinalName());
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

	private class ModelDate extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			return Calendar.class;
		}

		@Override
		public String getColumnName(int col) {
			return "Date";
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

	private class ModelCases extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case CASE_ACED:
			case CASE_ROED:
				return Calendar.class;
			case CASE_ROW:
			case CASE_NOBL:
			case CASE_NOSL:
				return Short.class;
			case CASE_NOSP:
				return Byte.class;
			case CASE_V5:
				return Integer.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return colCases.length;
		}

		@Override
		public String getColumnName(int col) {
			return colCases[col];
		}

		@Override
		public int getRowCount() {
			return cases.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (cases.size() > 0 && row < cases.size()) {
				switch (col) {
				case CASE_ROW:
					value = tblCases.convertRowIndexToView(row) + 1;
					break;
				case CASE_NO:
					value = cases.get(row).getCaseNo();
					break;
				case CASE_FAC:
					value = cases.get(row).getFacName();
					break;
				case CASE_SPY:
					value = cases.get(row).getSpyName();
					break;
				case CASE_SUB:
					value = cases.get(row).getSubName();
					break;
				case CASE_PROC:
					value = cases.get(row).getProcName();
					break;
				case CASE_SPEC:
					value = cases.get(row).getSpecName();
					break;
				case CASE_NOSP:
					value = cases.get(row).getNoSpecs();
					break;
				case CASE_NOBL:
					value = cases.get(row).getNoBlocks();
					break;
				case CASE_NOSL:
					value = cases.get(row).getNoSlides();
					break;
				case CASE_V5:
					value = cases.get(row).getValue5() / 60;
					break;
				case CASE_ACED:
					value = cases.get(row).getAccessCalendar();
					break;
				case CASE_ROED:
					value = cases.get(row).getRouteCalendar();
					break;
				case CASE_ROBY:
					value = cases.get(row).getRouteName();
					break;
				case CASE_FIBY:
					value = cases.get(row).getFinalName();
					break;
				default:
				}
			}
			return value;
		}
	}

	private class ModelSummary extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case SUM_ROW:
			case SUM_CASES:
			case SUM_SPECS:
			case SUM_SLIDES:
				return Short.class;
			case SUM_V5:
				return Integer.class;
			case SUM_FTE:
				return Double.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return colSum.length;
		}

		@Override
		public String getColumnName(int col) {
			return colSum[col];
		}

		@Override
		public int getRowCount() {
			return summary.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (summary.size() > 0 && row < summary.size()) {
				switch (col) {
				case SUM_ROW:
					value = tblSummary.convertRowIndexToView(row) + 1;
					break;
				case SUM_INIT:
					value = summary.get(row).getName();
					break;
				case SUM_NAME:
					value = summary.get(row).getFull();
					break;
				case SUM_CASES:
					value = summary.get(row).getNoCases();
					break;
				case SUM_SPECS:
					value = summary.get(row).getNoSpecs();
					break;
				case SUM_SLIDES:
					value = summary.get(row).getNoSlides();
					break;
				case SUM_V5:
					// Display in minutes
					value = summary.get(row).getValue5() / 60;
					break;
				case SUM_FTE:
					value = summary.get(row).getFtIn();
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
			setName("WorkerData");
			getDates();
			cases.clear();
			cases = application.dbPowerJ.getRouting(
					dates.get(rowIndex + 1).getTimeInMillis(),
					dates.get(rowIndex).getTimeInMillis());
			return null;
		}

		private void getDates() {
			int hours = routeTime / 3600000;
			int minutes = (routeTime % 3600000) / 60000;
			boolean startTomorrow = false;
			Calendar tomorrow = Calendar.getInstance();
			Calendar yesterday = Calendar.getInstance();
			Calendar endDate = Calendar.getInstance();
			// Tomorrow may be a weekend or off-day
			tomorrow.setTimeInMillis(application.dates.getNextBusinessDay(tomorrow));
			// Today may be a weekend or off-day
			yesterday.setTimeInMillis(application.dates.getPreviousBusinessDay(tomorrow));
			if (endDate.get(Calendar.HOUR_OF_DAY) > hours) {
				startTomorrow = true;
			} else if (endDate.get(Calendar.HOUR_OF_DAY) == hours && endDate.get(Calendar.MINUTE) >= minutes) {
				startTomorrow = true;
			}
			if (startTomorrow) {
				endDate.setTimeInMillis(tomorrow.getTimeInMillis());
			} else {
				endDate.setTimeInMillis(yesterday.getTimeInMillis());
			}
			endDate.set(Calendar.HOUR_OF_DAY, hours);
			endDate.set(Calendar.MINUTE, minutes);
			dates.clear();
			if (filters[FILTER_INT] == IToolBar.TB_DAILY) {
				do {
					Calendar thisDate = Calendar.getInstance();
					thisDate.setTimeInMillis(endDate.getTimeInMillis());
					dates.add(thisDate);
					endDate.setTimeInMillis(application.dates.getPreviousBusinessDay(thisDate));
					endDate.set(Calendar.HOUR_OF_DAY, hours);
					endDate.set(Calendar.MINUTE, minutes);
				} while (dates.size() < 6);
			} else {
				do {
					Calendar thisDate = Calendar.getInstance();
					thisDate.setTimeInMillis(endDate.getTimeInMillis());
					dates.add(thisDate);
					endDate.add(Calendar.DAY_OF_YEAR, -7);
				} while (dates.size() < 4);
			}
		}

		@Override
		public void done() {
			// Display results
			if (modelDate != null) {
				modelDate.fireTableDataChanged();
			}
			if (modelCases != null) {
				modelCases.fireTableDataChanged();
			}
			WorkerSummary worker = new WorkerSummary();
			worker.execute();
		}
	}

	private class WorkerSummary extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			HashMap<Short, WorkflowData> hashMap = new HashMap<Short, WorkflowData>();
			WorkflowData staff = new WorkflowData();
			summary.clear();
			noCases = 0;
			noSlides = 0;
			noFTE = 0;
			for (CaseData pending : cases) {
				if (filters[0] == 0 || filters[0] == pending.getFacID()) {
					if (filters[1] == 0 || filters[1] == pending.getSpyID()) {
						if (filters[2] == 0 || filters[2] == pending.getSubID()) {
							staff = hashMap.get(pending.getFinalID());
							if (staff == null) {
								staff = new WorkflowData();
								staff.setPrsID(pending.getFinalID());
								staff.setName(pending.getFinalName());
								staff.setFull(pending.getFinalFull());
								hashMap.put(pending.getFinalID(), staff);
							}
							staff.setNoCases((short) (staff.getNoCases() + 1));
							staff.setNoSpecs((short) (staff.getNoSpecs() + pending.getNoSpecs()));
							staff.setNoSlides((short) (staff.getNoSlides() + pending.getNoSlides()));
							staff.setValue5(staff.getValue5() + pending.getValue5());
							noCases++;
							noSlides += pending.getNoSlides();
						}
					}
				}
			}
			for (Entry<Short, WorkflowData> entry : hashMap.entrySet()) {
				staff = entry.getValue();
				staff.setFtIn((1.00 * staff.getValue5() / v5FTE));
				summary.add(staff);
				noFTE += staff.getFtIn();
			}
			hashMap.clear();
			Collections.sort(summary, new Comparator<WorkflowData>() {
				@Override
				public int compare(WorkflowData ds1, WorkflowData ds2) {
					return (ds1.getValue5() < ds2.getValue5() ? -1 : (ds1.getValue5() > ds2.getValue5() ? 1 : 0));
				}
			});
			return null;
		}

		@Override
		public void done() {
			// Display results
			if (modelSummary != null) {
				modelSummary.fireTableDataChanged();
			}
			if (summary.size() > 0) {
				// Chart Data Set
				String[] x = new String[summary.size()];
				double[] y = new double[summary.size()];
				for (int i = 0; i < summary.size(); i++) {
					x[i] = summary.get(i).getName();
					y[i] = summary.get(i).getFtIn();
				}
				chartBar.setChart(x, y, application.dates.formatter(dates.get(rowIndex), LibDates.FORMAT_DATE));
			}
			application.display(String.format("%s cases, %s slides, %s FTE", application.numbers.formatNumber(noCases),
					application.numbers.formatNumber(noSlides), application.numbers.formatDouble(2, noFTE)));
			application.setBusy(false);
		}
	}
}