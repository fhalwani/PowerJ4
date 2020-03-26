package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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

class NPending extends NBase {
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
	private short[] filters = { 0, 0, 0, 0, 8 };
	private String[] columns = { "NO", "CASE", "FAC", "SPY", "SUB", "PROC", "SPEC", "STATUS", "", "SPECS", "BLKS",
			"SLDS", "CUTOFF", "SPENT", "%", "ACCESS", "GROSS", "EMBED", "MICRO", "ROUTE", "FINAL", "GRNM", "EMNM",
			"MINM", "RONM", "FINM", "GRTA", "EMTA", "MITA", "ROTA", "FITA" };
	private ModelPending model;
	private OTurnaround turnaround = new OTurnaround();
	private HashMap<Byte, OTurnaround> turnarounds = new HashMap<Byte, OTurnaround>();
	private OCasePending pending = new OCasePending();
	private ArrayList<OCasePending> pendings = new ArrayList<OCasePending>();
	private ITable tbl;

	NPending(AClient parent) {
		super(parent);
		setName("Pending");
		parent.dbPowerJ.preparePending();
		columns[8] = pj.setup.getString(LSetup.VAR_V5_NAME);
		getTats();
		createPanel();
		refresh();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		pendings.clear();
		turnarounds.clear();
		return true;
	}

	private void createPanel() {
		model = new ModelPending();
		tbl = new ITable(pj, model) {
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
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		// Set Row Counter Size
		tbl.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		tbl.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		JScrollPane scrollTable = IGUI.createJScrollPane(tbl);
		setLayout(new BorderLayout());
		add(new IToolBar(this), BorderLayout.NORTH);
		add(scrollTable, BorderLayout.CENTER);
	}

	private void getTats() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_TUR_SELECT);
		try {
			while (rst.next()) {
				turnaround = new OTurnaround();
				turnaround.turID = rst.getByte("TAID");
				turnaround.gross = rst.getShort("GRSS");
				turnaround.embed = rst.getShort("EMBD");
				turnaround.micro = rst.getShort("MICR");
				turnaround.route = rst.getShort("ROUT");
				turnaround.diagn = rst.getShort("FINL");
				turnarounds.put(turnaround.turID, turnaround);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	private String getTooltip(int row, int col) {
		switch (col) {
		case CASE_GRBY:
			return pendings.get(row).grossFull;
		case CASE_EMBY:
			return pendings.get(row).embedFull;
		case CASE_MIBY:
			return pendings.get(row).microFull;
		case CASE_ROBY:
			return pendings.get(row).routeFull;
		case CASE_FIBY:
			return pendings.get(row).finalFull;
		default:
			return null;
		}
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("pending.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1.5f, 1, 1.5f, 1, 1.5f, 1.5f, 1, 1, 1, 1, 1, 1, 1, 1, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f,
				1.5f, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		String str = "Pending - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable table = new PdfPTable(columns.length);
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
			for (int col = 0; col < columns.length; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(columns[col]));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data rows
			int i = 0;
			for (int row = 0; row < tbl.getRowCount(); row++) {
				i = tbl.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case CASE_ROW:
						paragraph.add(new Chunk(pj.numbers.formatNumber(row + 1)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NO:
						paragraph.add(new Chunk(pendings.get(i).caseNo));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_FAC:
						paragraph.add(new Chunk(pendings.get(i).facility));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPY:
						paragraph.add(new Chunk(pendings.get(i).specialty));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SUB:
						paragraph.add(new Chunk(pendings.get(i).subspecial));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_PROC:
						paragraph.add(new Chunk(pendings.get(i).procedure));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPEC:
						paragraph.add(new Chunk(pendings.get(i).specimen));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_STAT:
						paragraph.add(new Chunk(pendings.get(i).status));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_VAL5:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).value5 / 60)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSP:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).noSpec)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOBL:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).noBlocks)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSL:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).noSlides)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_CUTOFF:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).cutoff)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_PASSED:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).passed)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_DELAY:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).delay)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ACED:
						paragraph.add(new Chunk(pj.dates.formatter(pendings.get(i).accessed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_GRED:
						paragraph.add(new Chunk(pj.dates.formatter(pendings.get(i).grossed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_EMED:
						paragraph.add(new Chunk(pj.dates.formatter(pendings.get(i).embeded, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_MIED:
						paragraph.add(new Chunk(pj.dates.formatter(pendings.get(i).microed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROED:
						paragraph.add(new Chunk(pj.dates.formatter(pendings.get(i).routed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_FIED:
						paragraph.add(new Chunk(pj.dates.formatter(pendings.get(i).finaled, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_GRBY:
						paragraph.add(new Chunk(pendings.get(i).grossName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_EMBY:
						paragraph.add(new Chunk(pendings.get(i).embedName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_MIBY:
						paragraph.add(new Chunk(pendings.get(i).microName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_ROBY:
						paragraph.add(new Chunk(pendings.get(i).routeName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_FIBY:
						paragraph.add(new Chunk(pendings.get(i).finalName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_GRTA:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).grossTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_EMTA:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).embedTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_MITA:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).microTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROTA:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).routeTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).finalTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					}
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

	@Override
	void refresh() {
		pj.setBusy(true);
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	void setFilter(short id, short value) {
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
					return (pendings.get(entry.getIdentifier()).facID == filters[FILTER_FAC]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_PRO] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (pendings.get(entry.getIdentifier()).procID == filters[FILTER_PRO]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SPY] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (pendings.get(entry.getIdentifier()).spyID == filters[FILTER_SPY]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SUB] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (pendings.get(entry.getIdentifier()).subID == filters[FILTER_SUB]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_STA] != OCaseStatus.ID_ALL) {
			if (filters[FILTER_STA] == OCaseStatus.ID_HISTO) {
				rowFilter = new RowFilter<AbstractTableModel, Integer>() {
					@Override
					public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
						return (pendings.get(entry.getIdentifier()).statusID > OCaseStatus.ID_ACCES
								&& pendings.get(entry.getIdentifier()).statusID < OCaseStatus.ID_ROUTE);
					}
				};
			} else {
				rowFilter = new RowFilter<AbstractTableModel, Integer>() {
					@Override
					public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
						return (pendings.get(entry.getIdentifier()).statusID == filters[FILTER_STA]);
					}
				};
			}
			rowFilters.add(rowFilter);
		}
		// Add to the compound filter
		rowFilter = RowFilter.andFilter(rowFilters);
		TableRowSorter<ModelPending> sorter = (TableRowSorter<ModelPending>) tbl.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
		pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(tbl.getRowCount()));
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("pending.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Pending");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Pending - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
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
			for (int row = 0; row < tbl.getRowCount(); row++) {
				i = tbl.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case CASE_ROW:
						xlsCell.setCellValue(row + 1);
						break;
					case CASE_NO:
						xlsCell.setCellValue(pendings.get(i).caseNo);
						break;
					case CASE_FAC:
						xlsCell.setCellValue(pendings.get(i).facility);
						break;
					case CASE_SPY:
						xlsCell.setCellValue(pendings.get(i).specialty);
						break;
					case CASE_SUB:
						xlsCell.setCellValue(pendings.get(i).subspecial);
						break;
					case CASE_PROC:
						xlsCell.setCellValue(pendings.get(i).procedure);
						break;
					case CASE_SPEC:
						xlsCell.setCellValue(pendings.get(i).specimen);
						break;
					case CASE_STAT:
						xlsCell.setCellValue(pendings.get(i).status);
						break;
					case CASE_VAL5:
						xlsCell.setCellValue(pendings.get(i).value5 / 60);
						break;
					case CASE_NOSP:
						xlsCell.setCellValue(pendings.get(i).noSpec);
						break;
					case CASE_NOBL:
						xlsCell.setCellValue(pendings.get(i).noBlocks);
						break;
					case CASE_NOSL:
						xlsCell.setCellValue(pendings.get(i).noSlides);
						break;
					case CASE_CUTOFF:
						xlsCell.setCellValue(pendings.get(i).cutoff);
						break;
					case CASE_PASSED:
						xlsCell.setCellValue(pendings.get(i).passed);
						break;
					case CASE_DELAY:
						xlsCell.setCellValue(pendings.get(i).delay);
						break;
					case CASE_ACED:
						xlsCell.setCellValue(pendings.get(i).accessed);
						break;
					case CASE_GRED:
						xlsCell.setCellValue(pendings.get(i).grossed);
						break;
					case CASE_EMED:
						xlsCell.setCellValue(pendings.get(i).embeded);
						break;
					case CASE_MIED:
						xlsCell.setCellValue(pendings.get(i).microed);
						break;
					case CASE_ROED:
						xlsCell.setCellValue(pendings.get(i).routed);
						break;
					case CASE_FIED:
						xlsCell.setCellValue(pendings.get(i).finaled);
						break;
					case CASE_GRBY:
						xlsCell.setCellValue(pendings.get(i).grossName);
						break;
					case CASE_EMBY:
						xlsCell.setCellValue(pendings.get(i).embedName);
						break;
					case CASE_MIBY:
						xlsCell.setCellValue(pendings.get(i).microName);
						break;
					case CASE_ROBY:
						xlsCell.setCellValue(pendings.get(i).routeName);
						break;
					case CASE_FIBY:
						xlsCell.setCellValue(pendings.get(i).finalName);
						break;
					case CASE_GRTA:
						xlsCell.setCellValue(pendings.get(i).grossTAT);
						break;
					case CASE_EMTA:
						xlsCell.setCellValue(pendings.get(i).embedTAT);
						break;
					case CASE_MITA:
						xlsCell.setCellValue(pendings.get(i).microTAT);
						break;
					case CASE_ROTA:
						xlsCell.setCellValue(pendings.get(i).routeTAT);
						break;
					default:
						xlsCell.setCellValue(pendings.get(i).finalTAT);
					}
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
			switch (col) {
			case CASE_ROW:
				value = tbl.convertRowIndexToView(row) + 1;
				break;
			case CASE_NO:
				value = pendings.get(row).caseNo;
				break;
			case CASE_FAC:
				value = pendings.get(row).facility;
				break;
			case CASE_SPY:
				value = pendings.get(row).specialty;
				break;
			case CASE_SUB:
				value = pendings.get(row).subspecial;
				break;
			case CASE_PROC:
				value = pendings.get(row).procedure;
				break;
			case CASE_STAT:
				value = pendings.get(row).status;
				break;
			case CASE_SPEC:
				value = pendings.get(row).specimen;
				break;
			case CASE_NOSP:
				value = pendings.get(row).noSpec;
				break;
			case CASE_NOBL:
				value = pendings.get(row).noBlocks;
				break;
			case CASE_NOSL:
				value = pendings.get(row).noSlides;
				break;
			case CASE_VAL5:
				value = pendings.get(row).value5 / 60;
				break;
			case CASE_CUTOFF:
				value = pendings.get(row).cutoff;
				break;
			case CASE_PASSED:
				value = pendings.get(row).passed;
				break;
			case CASE_DELAY:
				value = pendings.get(row).delay;
				break;
			case CASE_ACED:
				value = pendings.get(row).accessed;
				break;
			case CASE_GRED:
				value = pendings.get(row).grossed;
				break;
			case CASE_GRBY:
				value = pendings.get(row).grossName;
				break;
			case CASE_GRTA:
				value = pendings.get(row).grossTAT;
				break;
			case CASE_EMED:
				value = pendings.get(row).embeded;
				break;
			case CASE_EMBY:
				value = pendings.get(row).embedName;
				break;
			case CASE_EMTA:
				value = pendings.get(row).embedTAT;
				break;
			case CASE_MIED:
				value = pendings.get(row).microed;
				break;
			case CASE_MIBY:
				value = pendings.get(row).microName;
				break;
			case CASE_MITA:
				value = pendings.get(row).microTAT;
				break;
			case CASE_ROED:
				value = pendings.get(row).routed;
				break;
			case CASE_ROBY:
				value = pendings.get(row).routeName;
				break;
			case CASE_ROTA:
				value = pendings.get(row).routeTAT;
				break;
			case CASE_FIED:
				value = pendings.get(row).finaled;
				break;
			case CASE_FIBY:
				value = pendings.get(row).finalName;
				break;
			case CASE_FITA:
				value = pendings.get(row).finalTAT;
				break;
			default:
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			final String[] statuses = OCaseStatus.NAMES_ALL;
			int buffer = 0;
			int noRows = 0;
			ResultSet rst = null;
			try {
				pj.setBusy(true);
				programmaticChange = true;
				Calendar calToday = Calendar.getInstance();
				rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_PND_SELECT);
				pendings.clear();
				while (rst.next()) {
					pending = new OCasePending();
					pending.spyID = rst.getByte("SYID");
					pending.subID = rst.getByte("SBID");
					pending.procID = rst.getByte("POID");
					pending.turID = rst.getByte("TAID");
					pending.statusID = rst.getByte("PNST");
					pending.noSpec = rst.getByte("PNSP");
					pending.facID = rst.getShort("FAID");
					pending.noBlocks = rst.getShort("PNBL");
					pending.noSlides = rst.getShort("PNSL");
					pending.value5 = rst.getInt("PNV5");
					pending.caseNo = rst.getString("PNNO");
					pending.facility = rst.getString("FANM");
					pending.specialty = rst.getString("SYNM");
					pending.subspecial = rst.getString("SBNM");
					pending.procedure = rst.getString("PONM");
					pending.specimen = rst.getString("SMDC");
					pending.status = statuses[rst.getByte("PNST")];
					pending.accessed.setTimeInMillis(rst.getTimestamp("ACED").getTime());
					pending.grossed.setTimeInMillis(0);
					pending.embeded.setTimeInMillis(0);
					pending.microed.setTimeInMillis(0);
					pending.routed.setTimeInMillis(0);
					pending.finaled.setTimeInMillis(0);
					pending.cutoff = turnaround.gross;
					pending.passed = pj.dates.getBusinessHours(pending.accessed, calToday);
					turnaround = turnarounds.get(pending.turID);
					if (pending.statusID > OCaseStatus.ID_ACCES) {
						pending.grossed.setTimeInMillis(rst.getTimestamp("GRED").getTime());
						pending.grossTAT = rst.getShort("GRTA");
						pending.grossName = rst.getString("GRNM");
						pending.grossFull = rst.getString("GRFR").trim() + " " + rst.getString("GRLS").trim();
						pending.cutoff += turnaround.embed;
					}
					if (pending.statusID > OCaseStatus.ID_GROSS) {
						pending.embeded.setTimeInMillis(rst.getTimestamp("EMED").getTime());
						pending.embedTAT = rst.getShort("EMTA");
						pending.embedName = rst.getString("EMNM");
						pending.embedFull = rst.getString("EMFR").trim() + " " + rst.getString("EMLS").trim();
						pending.cutoff += turnaround.micro;
					}
					if (pending.statusID > OCaseStatus.ID_EMBED) {
						pending.microed.setTimeInMillis(rst.getTimestamp("MIED").getTime());
						pending.microTAT = rst.getShort("MITA");
						pending.microName = rst.getString("MINM");
						pending.microFull = rst.getString("MIFR").trim() + " " + rst.getString("MILS").trim();
						pending.cutoff += turnaround.route;
					}
					if (pending.statusID > OCaseStatus.ID_MICRO) {
						pending.routed.setTimeInMillis(rst.getTimestamp("ROED").getTime());
						pending.routeTAT = rst.getShort("ROTA");
						pending.routeName = rst.getString("RONM");
						pending.routeFull = rst.getString("ROFR").trim() + " " + rst.getString("ROLS").trim();
						pending.cutoff += turnaround.diagn;
					}
					if (pending.statusID > OCaseStatus.ID_ROUTE) {
						pending.finaled.setTimeInMillis(rst.getTimestamp("FNED").getTime());
						pending.finalName = rst.getString("FNNM");
						pending.finalFull = rst.getString("FNFR").trim() + " " + rst.getString("FNLS").trim();
					}
					if (pending.statusID == OCaseStatus.ID_FINAL) {
						pending.finalTAT = rst.getShort("FNTA");
						pending.passed = pending.finalTAT;
					}
					if (pending.cutoff > 0) {
						buffer = (100 * pending.passed) / pending.cutoff;
						if (buffer > Short.MAX_VALUE) {
							buffer = Short.MAX_VALUE;
						}
						pending.delay = (short) buffer;
					}
					pendings.add(pending);
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(LConstants.SLEEP_TIME);
						} catch (InterruptedException ignore) {
						}
					}
				}
//				Collections.sort(pendings, new Comparator<OCasePending>() {
//					@Override
//					public int compare(OCasePending o1, OCasePending o2) {
//						return (o1.delay > o2.delay ? -1 : (o1.delay == o2.delay ? 0 : 1));
//					}
//				});
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.closeRst(rst);
				pj.setBusy(false);
				programmaticChange = false;
			}
			return null;
		}

		@Override
		public void done() {
			if (model != null) {
				// Display results
				model.fireTableDataChanged();
			}
		}
	}
}