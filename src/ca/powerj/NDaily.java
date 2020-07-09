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

class NDaily extends NBase {
	private final byte CASE_ROW = 0;
	private final byte CASE_NO = 1;
	private final byte CASE_SPY = 2;
	private final byte CASE_SUB = 3;
	private final byte CASE_PROC = 4;
	private final byte CASE_SPEC = 5;
	private final byte CASE_NOSP = 6;
	private final byte CASE_NOBL = 7;
	private final byte CASE_NOSL = 8;
	private final byte CASE_VAL5 = 9;
	private final byte CASE_ACED = 10;
	private final byte CASE_ROED = 11;
	private final byte CASE_ROBY = 12;
	private final byte CASE_FIBY = 13;
	private final byte CASE_CUTOFF = 14;
	private final byte CASE_PASSED = 15;
	private final byte CASE_DELAY = 16;
	private int routeTime = 0;
	private final String[] columns = { "NO", "CASE", "SPY", "SUB", "PROC", "SPEC", "SPECS", "BLKS", "SLDS",
			pj.setup.getString(LSetup.VAR_V5_NAME).toUpperCase(), "ACCESS", "ROUTE", "BY", "TO", "CUTOFF", "SPENT",
			"DELAY" };
	private HashMap<Byte, OTurnaround> turnarounds = new HashMap<Byte, OTurnaround>();
	private ArrayList<OCasePending> pendings = new ArrayList<OCasePending>();
	private ModelCases modelCases;
	private ITable tblCases;
	private IChartBar2Y chartBar;

	NDaily(AClient parent) {
		super(parent);
		setName("Daily");
		routeTime = parent.setup.getInt(LSetup.VAR_ROUTE_TIME);
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_DAILY);
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
		return true;
	}

	private void createPanel() {
		modelCases = new ModelCases();
		tblCases = new ITable(pj, modelCases) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					switch (columnAtPoint(p)) {
					case CASE_ROBY:
						return pendings.get(m).routeFull;
					case CASE_FIBY:
						return pendings.get(m).finalFull;
					default:
						return null;
					}
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblCases.addAncestorListener(new IFocusListener());
		// Define color column renderer
		tblCases.getColumnModel().getColumn(CASE_DELAY).setCellRenderer(new IRenderColor(pj));
		// Set Row Counter Size
		tblCases.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		tblCases.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		Dimension dim = new Dimension(1200, 400);
		chartBar = new IChartBar2Y(dim);
		JScrollPane scrollTable = IGUI.createJScrollPane(tblCases);
		scrollTable.setMinimumSize(dim);
		JScrollPane scrollChart = IGUI.createJScrollPane(chartBar);
		scrollChart.setMinimumSize(dim);
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(scrollChart);
		splitAll.setBottomComponent(scrollTable);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(splitAll, BorderLayout.CENTER);
	}

	private void getTats() {
		OTurnaround turnaround = new OTurnaround();
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_TUR_SELECT));
		try {
			while (rst.next()) {
				turnaround = new OTurnaround();
				turnaround.turID = rst.getByte("taid");
				turnaround.gross = rst.getShort("grss");
				turnaround.embed = rst.getShort("embd");
				turnaround.micro = rst.getShort("micr");
				turnaround.route = rst.getShort("rout");
				turnaround.diagn = rst.getShort("finl");
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
		String fileName = pj.getFilePdf("daily.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 2, 1.5f, 1.5f, 1.5f, 2, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1 };
		String str = "Daily - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
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
					case CASE_ACED:
						paragraph.add(new Chunk(pj.dates.formatter(pendings.get(i).accessed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROED:
						paragraph.add(new Chunk(pj.dates.formatter(pendings.get(i).routed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("daily.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Daily");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Daily - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
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
				case CASE_ROED:
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
					case CASE_ACED:
						xlsCell.setCellValue(pendings.get(i).accessed);
						break;
					case CASE_ROED:
						xlsCell.setCellValue(pendings.get(i).routed);
						break;
					case CASE_ROBY:
						xlsCell.setCellValue(pendings.get(i).routeName);
						break;
					case CASE_FIBY:
						xlsCell.setCellValue(pendings.get(i).finalName);
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
				case CASE_SPY:
					value = pendings.get(row).specialty;
					break;
				case CASE_SUB:
					value = pendings.get(row).subspecial;
					break;
				case CASE_PROC:
					value = pendings.get(row).procedure;
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
				case CASE_ROED:
					value = pendings.get(row).routed;
					break;
				case CASE_ROBY:
					value = pendings.get(row).routeName;
					break;
				case CASE_FIBY:
					value = pendings.get(row).finalName;
					break;
				default:
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {
		private ArrayList<OWorkflow> lstPersons = new ArrayList<OWorkflow>();

		@Override
		protected Void doInBackground() throws Exception {
			byte statusID = 0;
			short finalID = 0;
			int buffer = 0;
			int hours = routeTime / 3600000;
			int minutes = (routeTime % 3600000) / 60000;
			OWorkflow person = new OWorkflow();
			OCasePending pending = new OCasePending();
			OTurnaround turnaround = new OTurnaround();
			Calendar startFinal = Calendar.getInstance();
			Calendar startRoute = Calendar.getInstance();
			Calendar endFinal = Calendar.getInstance();
			Calendar endRoute = Calendar.getInstance();
			HashMap<Short, OWorkflow> mapPersons = new HashMap<Short, OWorkflow>();
			ResultSet rst = null;
			try {
				setName("WorkerData");
				pendings.clear();
				// Cases routed from yesterday till today at cutoff time
				startRoute.setTimeInMillis(pj.dates.getPreviousBusinessDay(endRoute));
				endRoute.set(Calendar.HOUR_OF_DAY, hours);
				endRoute.set(Calendar.MINUTE, minutes);
				endRoute.set(Calendar.SECOND, 0);
				startRoute.set(Calendar.HOUR_OF_DAY, hours);
				startRoute.set(Calendar.MINUTE, minutes);
				startRoute.set(Calendar.SECOND, 0);
				// Cases finalized today all day
				startFinal.set(Calendar.HOUR_OF_DAY, 0);
				startFinal.set(Calendar.MINUTE, 0);
				startFinal.set(Calendar.SECOND, 0);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_PND_SELECT));
				while (rst.next()) {
					statusID = rst.getByte("pnst");
					finalID = rst.getShort("fnid");
					if (finalID < 1)
						continue;
					if (statusID > OCaseStatus.ID_MICRO && statusID < OCaseStatus.ID_FINAL) {
						if (finalID == pj.userID) {
							pending = new OCasePending();
							pending.turID = rst.getByte("taid");
							pending.noSpec = rst.getByte("pnsp");
							pending.noBlocks = rst.getShort("pnbl");
							pending.noSlides = rst.getShort("pnsl");
							pending.value5 = rst.getInt("pnv5");
							pending.caseNo = rst.getString("pnno");
							pending.specialty = rst.getString("synm");
							pending.subspecial = rst.getString("sbnm");
							pending.procedure = rst.getString("ponm");
							pending.specimen = rst.getString("smnm");
							pending.routeName = rst.getString("RONM");
							pending.finalName = rst.getString("fnnm");
							pending.routeFull = rst.getString("ROFR").trim() + " " + rst.getString("ROLS").trim();
							pending.finalFull = rst.getString("fnfr").trim() + " " + rst.getString("fnls").trim();
							pending.accessed.setTimeInMillis(rst.getTimestamp("aced").getTime());
							pending.routed.setTimeInMillis(rst.getTimestamp("roed").getTime());
							pending.passed = pj.dates.getBusinessHours(pending.accessed, endFinal);
							turnaround = turnarounds.get(pending.turID);
							pending.cutoff = (short) (turnaround.gross + turnaround.embed + turnaround.micro
									+ turnaround.route + turnaround.diagn);
							if (pending.cutoff > 0) {
								buffer = (100 * pending.passed) / pending.cutoff;
								if (buffer < 10000) {
									// Else, assume the case was abandoned & avoid overflow exception
									pending.delay = (short) buffer;
									pendings.add(pending);
								}
							}
						}
						if (person.prsID != finalID) {
							person = mapPersons.get(finalID);
							if (person == null) {
								person = new OWorkflow();
								person.prsID = finalID;
								if (pj.userAccess[LConstants.ACCESS_NAMES] || finalID == pj.userID) {
									// Else, leave blank to hide names later
									person.name = rst.getString("fnnm");
								}
								mapPersons.put(finalID, person);
							}
						}
						person.noPending += rst.getInt("pnv5");
					} else if (statusID == OCaseStatus.ID_FINAL) {
						if (startFinal.getTimeInMillis() < rst.getTimestamp("fned").getTime()) {
							if (person.prsID != finalID) {
								person = mapPersons.get(finalID);
								if (person == null) {
									person = new OWorkflow();
									person.prsID = finalID;
									if (pj.userAccess[LConstants.ACCESS_NAMES] || finalID == pj.userID) {
										person.name = rst.getString("fnnm");
									}
									mapPersons.put(finalID, person);
								}
							}
							person.noOut += rst.getInt("pnv5");
						}
					}
					if (startRoute.getTimeInMillis() < rst.getTimestamp("roed").getTime()) {
						if (endRoute.getTimeInMillis() > rst.getTimestamp("roed").getTime()) {
							if (person.prsID != finalID) {
								person = mapPersons.get(finalID);
								if (person == null) {
									person = new OWorkflow();
									person.prsID = finalID;
									if (pj.userAccess[LConstants.ACCESS_NAMES] || finalID == pj.userID) {
										person.name = rst.getString("fnnm");
									}
									mapPersons.put(finalID, person);
								}
							}
							person.noIn += rst.getInt("pnv5");
						}
					}
				}
				int i = 1;
				for (Entry<Short, OWorkflow> entry : mapPersons.entrySet()) {
					person = entry.getValue();
					if (person.name.length() == 0) {
						person.name = "P" + i;
						i++;
					}
					lstPersons.add(person);
				}
				Collections.sort(lstPersons, new Comparator<OWorkflow>() {
					@Override
					public int compare(OWorkflow o1, OWorkflow o2) {
						return (o1.noPending > o2.noPending ? -1 : (o1.noPending == o2.noPending ? 0 : 1));
					}
				});
				Collections.sort(pendings, new Comparator<OCasePending>() {
					@Override
					public int compare(OCasePending o1, OCasePending o2) {
						return (o1.delay > o2.delay ? -1 : (o1.delay == o2.delay ? 0 : 1));
					}
				});
				while (lstPersons.size() > 25) {
					lstPersons.remove(lstPersons.size() - 1);
				}
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
				mapPersons.clear();
			}
			return null;
		}

		@Override
		public void done() {
			if (modelCases != null) {
				modelCases.fireTableDataChanged();
			}
			if (lstPersons.size() > 0) {
				String[] legend = { "In", "Out", "Pending" };
				String[] xData = new String[lstPersons.size()];
				double[][] yData = new double[3][lstPersons.size()];
				for (int i = 0; i < lstPersons.size(); i++) {
					xData[i] = lstPersons.get(i).name;
					yData[0][i] = lstPersons.get(i).noIn / 60;
					yData[1][i] = lstPersons.get(i).noOut / 60;
					yData[2][i] = lstPersons.get(i).noPending / 60;
				}
				chartBar.setChart(xData, legend, yData, "Today's Workflow");
				lstPersons.clear();
			}
		}
	}
}