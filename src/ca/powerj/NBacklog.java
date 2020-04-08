package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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

class NBacklog extends NBase {
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
	private short[] filters = { 0, 0, 0, 0, 8 };
	private String[] columns = { "NO", "CASE", "ACCESS", "FAC", "SPY", "SUB", "PROC", "STATUS", "SPEC", "SPECS", "BLKS",
			"SLDS", "", "CUTOFF", "SPENT", "PRCNT" };
	private ModelBacklog model = null;
	private OTurnaround turnaround = new OTurnaround();
	private HashMap<Byte, OTurnaround> turnarounds = new HashMap<Byte, OTurnaround>();
	private OCasePending pending = new OCasePending();
	private ArrayList<OCasePending> pendings = new ArrayList<OCasePending>();
	private ITable tblCases;
	private IChartPie chartPie;
	private IChartBar2Y chartBar;

	NBacklog(AClient parent) {
		super(parent);
		setName("Backlog");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_BACKLOG);
		columns[12] = pj.setup.getString(LSetup.VAR_V5_NAME);
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
		tblCases = new ITable(pj, model);
		tblCases.addAncestorListener(new IFocusListener());
		tblCases.addFocusListener(this);
		// Define color column renderer
		tblCases.getColumnModel().getColumn(CASE_DELAY).setCellRenderer(new IRenderColor(pj));
		// Set Row Counter Size
		tblCases.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		tblCases.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		JScrollPane scrollTable = IGUI.createJScrollPane(tblCases);
		scrollTable.setMinimumSize(new Dimension(1300, 400));
		Dimension dim = new Dimension(600, 400);
		chartPie = new IChartPie(dim);
		JScrollPane scrollPie = IGUI.createJScrollPane(chartPie);
		scrollPie.setMinimumSize(dim);
		chartBar = new IChartBar2Y(dim);
		JScrollPane scrollBar = IGUI.createJScrollPane(chartBar);
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
		add(new IToolBar(this), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	private void getTats() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_TUR_SELECT));
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
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("backlog.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 2, 2, 1, 1.5f, 1.5f, 1.5f, 1.5f, 2, 1, 1, 1, 1, 1, 1, 1 };
		String str = "Backlog - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER.rotate(), 36, 18, 18, 18);
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
			for (int row = 0; row < tblCases.getRowCount(); row++) {
				i = tblCases.convertRowIndexToModel(row);
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
					case CASE_ACED:
						paragraph.add(new Chunk(pj.dates.formatter(pendings.get(i).accessed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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
					case CASE_STAT:
						paragraph.add(new Chunk(pendings.get(i).status));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPEC:
						paragraph.add(new Chunk(pendings.get(i).specimen));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
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
					case CASE_VAL5:
						paragraph.add(new Chunk(pj.numbers.formatNumber(pendings.get(i).value5 / 60)));
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
					default:
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
		String fileName = pj.getFileXls("backlog.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Backlog");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Backlog - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
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
			for (int row = 0; row < tblCases.getRowCount(); row++) {
				i = tblCases.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length - 1; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col + 1) {
					case CASE_NO:
						xlsCell.setCellValue(pendings.get(i).caseNo);
						break;
					case CASE_ACED:
						xlsCell.setCellValue(pendings.get(i).accessed);
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
					case CASE_STAT:
						xlsCell.setCellValue(pendings.get(i).status);
						break;
					case CASE_SPEC:
						xlsCell.setCellValue(pendings.get(i).specimen);
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
					case CASE_VAL5:
						xlsCell.setCellValue(pendings.get(i).value5 / 60);
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
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		} catch (IOException e) {
			pj.log(LConstants.ERROR_IO, getName(), e);
		} catch (Exception e) {
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), e);
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
					value = tblCases.convertRowIndexToView(row) + 1;
					break;
				case CASE_NO:
					value = pendings.get(row).caseNo;
					break;
				case CASE_ACED:
					value = pendings.get(row).accessed;
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
				default:
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			final String[] statuses = OCaseStatus.NAMES_ALL;
			int buffer = 0;
			ResultSet rst = null;
			try {
				Calendar calToday = Calendar.getInstance();
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_PND_SELECT));
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
					pending.specimen = rst.getString("SMNM");
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
						pending.cutoff += turnaround.embed;
					}
					if (pending.statusID > OCaseStatus.ID_GROSS) {
						pending.embeded.setTimeInMillis(rst.getTimestamp("EMED").getTime());
						pending.cutoff += turnaround.micro;
					}
					if (pending.statusID > OCaseStatus.ID_EMBED) {
						pending.microed.setTimeInMillis(rst.getTimestamp("MIED").getTime());
						pending.cutoff += turnaround.route;
					}
					if (pending.statusID > OCaseStatus.ID_MICRO) {
						pending.routed.setTimeInMillis(rst.getTimestamp("ROED").getTime());
						pending.cutoff += turnaround.diagn;
					}
					if (pending.statusID > OCaseStatus.ID_ROUTE) {
						pending.finaled.setTimeInMillis(rst.getTimestamp("FNED").getTime());
						pending.finalName = rst.getString("FNNM");
						pending.finalFull = rst.getString("FNFR").trim() + " " + rst.getString("FNLS").trim();
					}
					if (pending.statusID == OCaseStatus.ID_FINAL) {
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
				}
				Collections.sort(pendings, new Comparator<OCasePending>() {
					@Override
					public int compare(OCasePending o1, OCasePending o2) {
						return (o1.delay > o2.delay ? -1 : (o1.delay == o2.delay ? 0 : 1));
					}
				});
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException e) {
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
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
			OWorkday workday = new OWorkday();
			ArrayList<OWorkday> workdays = new ArrayList<OWorkday>();
			ArrayList<ArrayList<Double>> workflow = new ArrayList<ArrayList<Double>>();
			Calendar calStart = pj.dates.setMidnight(null);
			workday = new OWorkday();
			workday.date.setTime(calStart.getTimeInMillis());
			workday.name = pj.dates.formatter(workday.date, LDates.FORMAT_DATESHORT);
			workdays.add(workday);
			while (workdays.size() < 7) {
				calStart.setTimeInMillis(pj.dates.getPreviousBusinessDay(calStart));
				workday = new OWorkday();
				workday.date.setTime(calStart.getTimeInMillis());
				workday.name = pj.dates.formatter(workday.date, LDates.FORMAT_DATESHORT);
				workdays.add(workday);
			}
			Collections.sort(workdays, new Comparator<OWorkday>() {
				@Override
				public int compare(OWorkday o1, OWorkday o2) {
					return (o1.date.getTime() < o2.date.getTime() ? -1
							: (o1.date.getTime() > o2.date.getTime() ? 1 : 0));
				}
			});
			for (int x = 0; x < 3; x++) {
				workflow.add(new ArrayList<Double>());
				for (int y = 0; y < workdays.size(); y++) {
					workflow.get(x).add(0d);
				}
			}
			yPie = new double[3];
			for (OCasePending pending : pendings) {
				if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == pending.facID) {
					if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == pending.spyID) {
						if (filters[FILTER_SUB] == 0 || filters[FILTER_SUB] == pending.subID) {
							if (filters[FILTER_PRO] == 0 || filters[FILTER_PRO] == pending.procID) {
								if (filters[FILTER_STA] == OCaseStatus.ID_ALL || filters[FILTER_STA] == pending.statusID
										|| (filters[FILTER_STA] == OCaseStatus.ID_HISTO
												&& pending.statusID > OCaseStatus.ID_ACCES
												&& pending.statusID < OCaseStatus.ID_ROUTE)) {
									if (pending.statusID < OCaseStatus.ID_FINAL) {
										if (pending.delay > 100) {
											yPie[0]++;
										} else if (pending.delay > 70) {
											yPie[1]++;
										} else {
											yPie[2]++;
										}
									}
								}
								switch (filters[FILTER_STA]) {
								case OCaseStatus.ID_ACCES:
									for (int x = workdays.size() - 1; x >= 0; x--) {
										if (pending.accessed.getTimeInMillis() > workdays.get(x).date.getTime()) {
											// Date In
											counter = 1.0 + workflow.get(0).get(x);
											workflow.get(0).set(x, counter);
											break;
										}
									}
									if (pending.statusID == OCaseStatus.ID_ACCES) {
										// Still Pending
										counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
										workflow.get(2).set(workdays.size() - 1, counter);
									} else {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.grossed.getTimeInMillis() > workdays.get(x).date.getTime()) {
												// Date Out
												counter = 1.0 + workflow.get(1).get(x);
												workflow.get(1).set(x, counter);
												break;
											}
										}
									}
									break;
								case OCaseStatus.ID_GROSS:
									if (pending.statusID > OCaseStatus.ID_ACCES) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.grossed.getTimeInMillis() > workdays.get(x).date.getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.statusID == OCaseStatus.ID_GROSS) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.embeded.getTimeInMillis() > workdays.get(x).date
														.getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case OCaseStatus.ID_EMBED:
									if (pending.statusID > OCaseStatus.ID_GROSS) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.embeded.getTimeInMillis() > workdays.get(x).date.getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.statusID == OCaseStatus.ID_EMBED) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.microed.getTimeInMillis() > workdays.get(x).date
														.getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case OCaseStatus.ID_MICRO:
									if (pending.statusID > OCaseStatus.ID_EMBED) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.microed.getTimeInMillis() > workdays.get(x).date.getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.statusID == OCaseStatus.ID_MICRO) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.routed.getTimeInMillis() > workdays.get(x).date.getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case OCaseStatus.ID_ROUTE:
									if (pending.statusID > OCaseStatus.ID_MICRO) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.routed.getTimeInMillis() > workdays.get(x).date.getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.statusID == OCaseStatus.ID_ROUTE) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.finaled.getTimeInMillis() > workdays.get(x).date
														.getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case OCaseStatus.ID_DIAGN:
									if (pending.statusID > OCaseStatus.ID_ROUTE) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.finaled.getTimeInMillis() > workdays.get(x).date.getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.statusID == OCaseStatus.ID_DIAGN) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.finaled.getTimeInMillis() > workdays.get(x).date
														.getTime()) {
													// Date Out
													counter = 1.0 + workflow.get(1).get(x);
													workflow.get(1).set(x, counter);
													break;
												}
											}
										}
									}
									break;
								case OCaseStatus.ID_HISTO:
									if (pending.statusID > OCaseStatus.ID_ACCES) {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.grossed.getTimeInMillis() > workdays.get(x).date.getTime()) {
												// Date In
												counter = 1.0 + workflow.get(0).get(x);
												workflow.get(0).set(x, counter);
												break;
											}
										}
										if (pending.statusID < OCaseStatus.ID_ROUTE) {
											// Still Pending
											counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
											workflow.get(2).set(workdays.size() - 1, counter);
										} else {
											for (int x = workdays.size() - 1; x >= 0; x--) {
												if (pending.routed.getTimeInMillis() > workdays.get(x).date.getTime()) {
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
										if (pending.accessed.getTimeInMillis() > workdays.get(x).date.getTime()) {
											// Date In
											counter = 1.0 + workflow.get(0).get(x);
											workflow.get(0).set(x, counter);
											break;
										}
									}
									if (pending.statusID < OCaseStatus.ID_FINAL) {
										// Still Pending
										counter = 1.0 + workflow.get(2).get(workdays.size() - 1);
										workflow.get(2).set(workdays.size() - 1, counter);
									} else {
										for (int x = workdays.size() - 1; x >= 0; x--) {
											if (pending.finaled.getTimeInMillis() > workdays.get(x).date.getTime()) {
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
				xDates[x] = workdays.get(x + 1).name;
				for (int y = 0; y < 3; y++) {
					yDates[y][x] = workflow.get(y).get(x + 1);
				}
			}
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
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
			if (tblCases != null) {
				RowFilter<AbstractTableModel, Integer> rowFilter = null;
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
				// Status filter is always on
				switch (filters[FILTER_STA]) {
				case OCaseStatus.ID_ALL:
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).statusID < OCaseStatus.ID_FINAL);
						}
					};
					break;
				case OCaseStatus.ID_HISTO:
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).statusID > OCaseStatus.ID_ACCES
									&& pendings.get(entry.getIdentifier()).statusID < OCaseStatus.ID_ROUTE);
						}
					};
					break;
				default:
					rowFilter = new RowFilter<AbstractTableModel, Integer>() {
						@Override
						public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
							return (pendings.get(entry.getIdentifier()).statusID == filters[FILTER_STA]);
						}
					};
				}
				rowFilters.add(rowFilter);
				if (rowFilters.size() > 0) {
					if (rowFilters.size() > 1) {
						// Add to the compound filter
						rowFilter = RowFilter.andFilter(rowFilters);
					}
					TableRowSorter<ModelBacklog> sorter = (TableRowSorter<ModelBacklog>) tblCases.getRowSorter();
					sorter.setRowFilter(rowFilter);
					sorter.sort();
				}
			}
			pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(tblCases.getRowCount()));
			pj.setBusy(false);
		}
	}
}