package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class PendingPanel extends BasePanel {
	private final byte CASE_ROW = 0;
	private final byte CASE_NO = 1;
	private final byte CASE_FAC = 2;
	private final byte CASE_SPY = 3;
	private final byte CASE_SUB = 4;
	private final byte CASE_PROC = 5;
	private final byte CASE_SPEC = 6;
	private final byte CASE_STAT = 7;
	private final byte CASE_VAL5 = 8;
	private final byte CASE_NOSP = 9;
	private final byte CASE_NOBL = 10;
	private final byte CASE_NOSL = 11;
	private final byte CASE_CUTOFF = 12;
	private final byte CASE_PASSED = 13;
	private final byte CASE_DELAY = 14;
	private final byte CASE_ACED = 15;
	private final byte CASE_GRED = 16;
	private final byte CASE_EMED = 17;
	private final byte CASE_MIED = 18;
	private final byte CASE_ROED = 19;
	private final byte CASE_FIED = 20;
	private final byte CASE_GRBY = 21;
	private final byte CASE_EMBY = 22;
	private final byte CASE_MIBY = 23;
	private final byte CASE_ROBY = 24;
	private final byte CASE_FIBY = 25;
	private final byte CASE_GRTA = 26;
	private final byte CASE_EMTA = 27;
	private final byte CASE_MITA = 28;
	private final byte CASE_ROTA = 29;
	private final byte CASE_FITA = 30;
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_SUB = 2;
	private final byte FILTER_PRO = 3;
	private final byte FILTER_STA = 4;
	private int[] filters = { 0, 0, 0, 0, 8 };
	private final String[] columns = { "NO", "CASE", "FAC", "SPY", "SUB", "PROC", "SPEC", "STATUS", "", "SPECS", "BLKS",
			"SLDS", "CUTOFF", "SPENT", "%", "ACCESS", "GROSS", "EMBED", "MICRO", "ROUTE", "FINAL", "GRNM", "EMNM",
			"MINM", "RONM", "FINM", "grta", "emta", "mita", "rota", "FITA" };
	private TurnaroundData turnaround = new TurnaroundData();
	private CaseData pending = new CaseData();
	private HashMap<Byte, TurnaroundData> turnarounds = new HashMap<Byte, TurnaroundData>();
	private ArrayList<CaseData> pendings = new ArrayList<CaseData>();
	private ModelPending model;
	private ITable table;

	PendingPanel(AppFrame application) {
		super(application);
		setName("Pending");
		application.dbPowerJ.setStatements(LibConstants.ACTION_PENDING);
		columns[8] = application.getProperty("coder5");
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
		return true;
	}

	private void createPanel() {
		model = new ModelPending();
		table = new ITable(model, application.dates, application.numbers) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					String tip = null;
					try {
						Point p = e.getPoint();
						JTable tbl = (JTable) e.getSource();
						int col = columnAtPoint(p);
						int viewRow = rowAtPoint(p);
						int modelRow = tbl.convertRowIndexToModel(viewRow);
						tip = getTooltip(modelRow, col);
					} catch (ArrayIndexOutOfBoundsException ignore) {
					} catch (RuntimeException ignore) {
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		table.addAncestorListener(new IFocusListener());
		table.addFocusListener(this);
		// Set Row Counter Size
		table.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		table.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		JScrollPane scrollTable = IUtilities.createJScrollPane(table);
		setLayout(new BorderLayout());
		add(new IToolBar(application), BorderLayout.NORTH);
		add(scrollTable, BorderLayout.CENTER);
	}

	private void getTurnaround() {
		ArrayList<TurnaroundData> tempTurn = new ArrayList<TurnaroundData>();
		for (int i = 0; i < tempTurn.size(); i++) {
			turnarounds.put(tempTurn.get(i).getTurID(), tempTurn.get(i));
		}
		tempTurn.clear();
	}

	private String getTooltip(int row, int col) {
		switch (col) {
		case CASE_GRBY:
			return pendings.get(row).getGrossFull();
		case CASE_EMBY:
			return pendings.get(row).getEmbedFull();
		case CASE_MIBY:
			return pendings.get(row).getMicroFull();
		case CASE_ROBY:
			return pendings.get(row).getRouteFull();
		case CASE_FIBY:
			return pendings.get(row).getFinalFull();
		default:
			return null;
		}
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("pending.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1.5f, 1, 1.5f, 1, 1.5f, 1.5f, 1, 1, 1, 1, 1, 1, 1, 1, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f,
				1.5f, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		String str = "Pending - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
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
					case CASE_SPEC:
						paragraph.add(new Chunk(pendings.get(i).getSpecName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_STAT:
						paragraph.add(new Chunk(pendings.get(i).getStatusName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_VAL5:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getValue5() / 60)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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
					case CASE_ACED:
						paragraph.add(new Chunk(application.dates.formatter(pendings.get(i).getAccessCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_GRED:
						paragraph.add(new Chunk(application.dates.formatter(pendings.get(i).getGrossCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_EMED:
						paragraph.add(new Chunk(application.dates.formatter(pendings.get(i).getEmbedCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_MIED:
						paragraph.add(new Chunk(application.dates.formatter(pendings.get(i).getMicroCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROED:
						paragraph.add(new Chunk(application.dates.formatter(pendings.get(i).getRouteCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_FIED:
						paragraph.add(new Chunk(application.dates.formatter(pendings.get(i).getFinalCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_GRBY:
						paragraph.add(new Chunk(pendings.get(i).getGrossName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_EMBY:
						paragraph.add(new Chunk(pendings.get(i).getEmbedName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_MIBY:
						paragraph.add(new Chunk(pendings.get(i).getMicroName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_ROBY:
						paragraph.add(new Chunk(pendings.get(i).getRouteName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_FIBY:
						paragraph.add(new Chunk(pendings.get(i).getFinalName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_GRTA:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getGrossTAT())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_EMTA:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getEmbedTAT())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_MITA:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getMicroTAT())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROTA:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getRouteTAT())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
						paragraph.add(new Chunk(application.numbers.formatNumber(pendings.get(i).getFinalTAT())));
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
	void refresh() {
		application.setBusy(true);
		programmaticChange = true;
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	public void setFilter(short id, int value) {
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
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
		if (filters[FILTER_STA] != LibConstants.STATUS_ALL) {
			if (filters[FILTER_STA] == LibConstants.STATUS_HISTO) {
				rowFilter = new RowFilter<AbstractTableModel, Integer>() {
					@Override
					public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
						return (pendings.get(entry.getIdentifier()).getStatusID() > LibConstants.STATUS_ACCES
								&& pendings.get(entry.getIdentifier()).getStatusID() < LibConstants.STATUS_ROUTE);
					}
				};
			} else {
				rowFilter = new RowFilter<AbstractTableModel, Integer>() {
					@Override
					public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
						return (pendings.get(entry.getIdentifier()).getStatusID() == filters[FILTER_STA]);
					}
				};
			}
			rowFilters.add(rowFilter);
		}
		// Add to the compound filter
		rowFilter = RowFilter.andFilter(rowFilters);
		TableRowSorter<ModelPending> sorter = (TableRowSorter<ModelPending>) table.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
		application.display("No Rows: " + application.numbers.formatNumber(table.getRowCount()));
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("pending.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Pending");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Pending - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length - 1));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col < columns.length; col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellValue(columns[col]);
				xlsCell.setCellStyle(styles.get("header"));
				switch (col) {
				case CASE_ACED:
				case CASE_GRED:
				case CASE_EMED:
				case CASE_MIED:
				case CASE_ROED:
				case CASE_FIED:
					sheet.setColumnWidth(col, 18 * 256); // 18 characters
					sheet.setDefaultColumnStyle(col, styles.get("datetime"));
					break;
				case CASE_ROW:
				case CASE_NOSP:
				case CASE_NOBL:
				case CASE_NOSL:
				case CASE_CUTOFF:
				case CASE_PASSED:
				case CASE_DELAY:
				case CASE_VAL5:
				case CASE_GRTA:
				case CASE_EMTA:
				case CASE_MITA:
				case CASE_ROTA:
				case CASE_FITA:
					sheet.setColumnWidth(col, 5 * 256); // 10 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case CASE_FAC:
				case CASE_SUB:
				case CASE_STAT:
				case CASE_GRBY:
				case CASE_EMBY:
				case CASE_MIBY:
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
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case CASE_ROW:
						xlsCell.setCellValue(row + 1);
						break;
					case CASE_NO:
						xlsCell.setCellValue(pendings.get(i).getCaseNo());
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
					case CASE_SPEC:
						xlsCell.setCellValue(pendings.get(i).getSpecName());
						break;
					case CASE_STAT:
						xlsCell.setCellValue(pendings.get(i).getStatusName());
						break;
					case CASE_VAL5:
						xlsCell.setCellValue(pendings.get(i).getValue5() / 60);
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
					case CASE_CUTOFF:
						xlsCell.setCellValue(pendings.get(i).getCutoff());
						break;
					case CASE_PASSED:
						xlsCell.setCellValue(pendings.get(i).getPassed());
						break;
					case CASE_DELAY:
						xlsCell.setCellValue(pendings.get(i).getDelay());
						break;
					case CASE_ACED:
						xlsCell.setCellValue(pendings.get(i).getAccessCalendar());
						break;
					case CASE_GRED:
						xlsCell.setCellValue(pendings.get(i).getGrossCalendar());
						break;
					case CASE_EMED:
						xlsCell.setCellValue(pendings.get(i).getEmbedCalendar());
						break;
					case CASE_MIED:
						xlsCell.setCellValue(pendings.get(i).getMicroCalendar());
						break;
					case CASE_ROED:
						xlsCell.setCellValue(pendings.get(i).getRouteCalendar());
						break;
					case CASE_FIED:
						xlsCell.setCellValue(pendings.get(i).getFinalCalendar());
						break;
					case CASE_GRBY:
						xlsCell.setCellValue(pendings.get(i).getGrossName());
						break;
					case CASE_EMBY:
						xlsCell.setCellValue(pendings.get(i).getEmbedName());
						break;
					case CASE_MIBY:
						xlsCell.setCellValue(pendings.get(i).getMicroName());
						break;
					case CASE_ROBY:
						xlsCell.setCellValue(pendings.get(i).getRouteName());
						break;
					case CASE_FIBY:
						xlsCell.setCellValue(pendings.get(i).getFinalName());
						break;
					case CASE_GRTA:
						xlsCell.setCellValue(pendings.get(i).getGrossTAT());
						break;
					case CASE_EMTA:
						xlsCell.setCellValue(pendings.get(i).getEmbedTAT());
						break;
					case CASE_MITA:
						xlsCell.setCellValue(pendings.get(i).getMicroTAT());
						break;
					case CASE_ROTA:
						xlsCell.setCellValue(pendings.get(i).getRouteTAT());
						break;
					default:
						xlsCell.setCellValue(pendings.get(i).getFinalTAT());
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

	private class ModelPending extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case CASE_ACED:
			case CASE_GRED:
			case CASE_EMED:
			case CASE_MIED:
			case CASE_ROED:
			case CASE_FIED:
				return Calendar.class;
			case CASE_ROW:
			case CASE_NOBL:
			case CASE_NOSL:
			case CASE_CUTOFF:
			case CASE_PASSED:
			case CASE_DELAY:
			case CASE_GRTA:
			case CASE_EMTA:
			case CASE_MITA:
			case CASE_ROTA:
			case CASE_FITA:
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
				case CASE_ACED:
					value = pendings.get(row).getAccessCalendar();
					break;
				case CASE_GRED:
					value = pendings.get(row).getGrossCalendar();
					break;
				case CASE_GRBY:
					value = pendings.get(row).getGrossName();
					break;
				case CASE_GRTA:
					value = pendings.get(row).getGrossTAT();
					break;
				case CASE_EMED:
					value = pendings.get(row).getEmbedCalendar();
					break;
				case CASE_EMBY:
					value = pendings.get(row).getEmbedName();
					break;
				case CASE_EMTA:
					value = pendings.get(row).getEmbedTAT();
					break;
				case CASE_MIED:
					value = pendings.get(row).getMicroCalendar();
					break;
				case CASE_MIBY:
					value = pendings.get(row).getMicroName();
					break;
				case CASE_MITA:
					value = pendings.get(row).getMicroTAT();
					break;
				case CASE_ROED:
					value = pendings.get(row).getRouteCalendar();
					break;
				case CASE_ROBY:
					value = pendings.get(row).getRouteName();
					break;
				case CASE_ROTA:
					value = pendings.get(row).getRouteTAT();
					break;
				case CASE_FIED:
					value = pendings.get(row).getFinalCalendar();
					break;
				case CASE_FIBY:
					value = pendings.get(row).getFinalName();
					break;
				default:
					value = pendings.get(row).getFinalTAT();
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
			return null;
		}

		@Override
		public void done() {
			if (model != null) {
				// Display results
				model.fireTableDataChanged();
			}
			application.setBusy(false);
			programmaticChange = false;
		}
	}
}