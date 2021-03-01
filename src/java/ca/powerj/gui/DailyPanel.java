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
import ca.powerj.data.CaseData;
import ca.powerj.data.TurnaroundData;
import ca.powerj.data.WorkflowData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IChartBar2Yaxis;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.IRendererColor;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IUtilities;

class DailyPanel extends BasePanel {
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
			application.getProperty("coder5"), "ACCESS", "ROUTE", "BY", "TO", "CUTOFF", "SPENT", "DELAY" };
	private HashMap<Byte, TurnaroundData> turnarounds = null;
	private ArrayList<CaseData> pendings = new ArrayList<CaseData>();
	private ModelCases model;
	private ITable table;
	private IChartBar2Yaxis chartBar;

	DailyPanel(AppFrame application) {
		super(application);
		setName("Daily");
		routeTime = application.setup.getInt(LibSetup.VAR_ROUTE_TIME);
		application.dbPowerJ.setStatements(LibConstants.ACTION_DAILY);
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
		return true;
	}

	private void createPanel() {
		model = new ModelCases();
		table = new ITable(model, application.dates, application.numbers) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					switch (columnAtPoint(p)) {
					case CASE_ROBY:
						return pendings.get(m).getRouteFull();
					case CASE_FIBY:
						return pendings.get(m).getFinalFull();
					default:
						return null;
					}
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		table.addAncestorListener(new IFocusListener());
		// Define color column renderer
		table.getColumnModel().getColumn(CASE_DELAY).setCellRenderer(new IRendererColor(application.numbers));
		// Set Row Counter Size
		table.getColumnModel().getColumn(CASE_ROW).setMinWidth(50);
		table.getColumnModel().getColumn(CASE_ROW).setMaxWidth(50);
		Dimension dim = new Dimension(1200, 400);
		chartBar = new IChartBar2Yaxis(dim);
		JScrollPane scrollTable = IUtilities.createJScrollPane(table);
		scrollTable.setMinimumSize(dim);
		JScrollPane scrollChart = IUtilities.createJScrollPane(chartBar);
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
		String fileName = application.getFilePdf("daily.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 2, 1.5f, 1.5f, 1.5f, 2, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1 };
		String str = "Daily - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
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
					case CASE_ACED:
						paragraph.add(new Chunk(application.dates.formatter(pendings.get(i).getAccessCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROED:
						paragraph.add(new Chunk(application.dates.formatter(pendings.get(i).getRouteCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("daily.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Daily");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Daily - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
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
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length - 1; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col + 1) {
					case CASE_NO:
						xlsCell.setCellValue(pendings.get(i).getCaseNo());
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
					case CASE_ACED:
						xlsCell.setCellValue(pendings.get(i).getAccessCalendar());
						break;
					case CASE_ROED:
						xlsCell.setCellValue(pendings.get(i).getRouteCalendar());
						break;
					case CASE_ROBY:
						xlsCell.setCellValue(pendings.get(i).getRouteName());
						break;
					case CASE_FIBY:
						xlsCell.setCellValue(pendings.get(i).getFinalName());
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
					value = table.convertRowIndexToView(row) + 1;
					break;
				case CASE_NO:
					value = pendings.get(row).getCaseNo();
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
				case CASE_ROED:
					value = pendings.get(row).getRouteCalendar();
					break;
				case CASE_ROBY:
					value = pendings.get(row).getRouteName();
					break;
				case CASE_FIBY:
					value = pendings.get(row).getFinalName();
					break;
				default:
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {
		private ArrayList<WorkflowData> persons = new ArrayList<WorkflowData>();
		private String message = "";

		@Override
		protected Void doInBackground() throws Exception {
			short finalID = 0;
			int buffer = 0;
			int hours = routeTime / 3600000;
			int minutes = (routeTime % 3600000) / 60000;
			WorkflowData person = new WorkflowData();
			CaseData pending = new CaseData();
			TurnaroundData turnaround = new TurnaroundData();
			Calendar startFinal = Calendar.getInstance();
			Calendar startRoute = Calendar.getInstance();
			Calendar endFinal = Calendar.getInstance();
			Calendar endRoute = Calendar.getInstance();
			HashMap<Short, WorkflowData> mapPersons = new HashMap<Short, WorkflowData>();
			setName("WorkerData");
			pendings.clear();
			ArrayList<CaseData> tempList = application.dbPowerJ.getPendings(LibConstants.STATUS_ALL);
			// Cases routed from yesterday till today at cutoff time
			startRoute.setTimeInMillis(application.dates.getPreviousBusinessDay(endRoute));
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
			for (int i = 0; i < tempList.size(); i++) {
				pending = tempList.get(i);
				finalID = pending.getFinalID();
				if (finalID > 0) {
					if (pending.getStatusID() > LibConstants.STATUS_MICRO
							&& pending.getStatusID() < LibConstants.STATUS_FINAL) {
						if (finalID == application.getUserID()) {
							turnaround = turnarounds.get(pending.getTurnaroundID());
							pending.setCutoff((short) (turnaround.getGross() + turnaround.getEmbed() + turnaround.getMicrotomy()
									+ turnaround.getRoute() + turnaround.getDiagnosis()));
							buffer = application.dates.getBusinessHours(pending.getAccessCalendar(), endFinal);
							if (buffer > 9999) {
								buffer = 9999;
							}
							pending.setPassed((short) buffer);
							if (pending.getCutoff() > 0) {
								buffer = (100 * pending.getPassed()) / pending.getCutoff();
								if (buffer > 9999) {
									buffer = 9999;
								}
								pending.setDelay((short) buffer);
							}
							pendings.add(pending);
						}
						person = mapPersons.get(finalID);
						if (person == null) {
							person = new WorkflowData();
							person.setPrsID(finalID);
							if (application.userAccess[LibConstants.ACCESS_NAMES] || finalID == application.getUserID()) {
								// Else, leave blank to hide names later
								person.setName(pending.getFinalName());
							}
							mapPersons.put(finalID, person);
						}
						person.setNoPending(person.getNoPending() + pending.getValue5());
					} else if (pending.getStatusID() == LibConstants.STATUS_FINAL) {
						if (startFinal.getTimeInMillis() < pending.getFinalTime()) {
							person = mapPersons.get(finalID);
							if (person == null) {
								person = new WorkflowData();
								person.setPrsID(finalID);
								if (application.userAccess[LibConstants.ACCESS_NAMES] || finalID == application.getUserID()) {
									// Else, leave blank to hide names later
									person.setName(pending.getFinalName());
								}
								mapPersons.put(finalID, person);
							}
							person.setNoOut(person.getNoOut() + pending.getValue5());
						}
					}
					if (startRoute.getTimeInMillis() < pending.getRouteTime()) {
						if (endRoute.getTimeInMillis() > pending.getRouteTime()) {
							person = mapPersons.get(finalID);
							if (person == null) {
								person = new WorkflowData();
								person.setPrsID(finalID);
								if (application.userAccess[LibConstants.ACCESS_NAMES] || finalID == application.getUserID()) {
									// Else, leave blank to hide names later
									person.setName(pending.getFinalName());
								}
								mapPersons.put(finalID, person);
							}
							person.setNoIn(person.getNoIn() + pending.getValue5());
						}
					}
				}
			}
			tempList.clear();
			for (Entry<Short, WorkflowData> entry : mapPersons.entrySet()) {
				person = entry.getValue();
				persons.add(person);
				if (person.getPrsID() == application.getUserID()) {
					message = "In " + (person.getNoIn() / 60)
							+ ", Out " + (person.getNoOut() / 60)
							+ ", Pending " + (person.getNoPending() / 60) + ", ";
				}
			}
			mapPersons.clear();
			Collections.sort(persons, new Comparator<WorkflowData>() {
				@Override
				public int compare(WorkflowData o1, WorkflowData o2) {
					return (o1.getNoPending() > o2.getNoPending() ? -1 : (o1.getNoPending() < o2.getNoPending() ? 1 : 0));
				}
			});
			while (persons.size() > 25) {
				persons.remove(persons.size() - 1);
			}
			for (byte i = 0; i < persons.size(); i++) {
				if (persons.get(i).getName().length() == 0) {
					persons.get(i).setName("P" + (i+1));
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
			if (persons.size() > 0) {
				String[] legend = { "In", "Out", "Pending" };
				String[] xData = new String[persons.size()];
				double[][] yData = new double[3][persons.size()];
				for (byte i = 0; i < persons.size(); i++) {
					xData[i] = persons.get(i).getName();
					yData[0][i] = persons.get(i).getNoIn() / 60;
					yData[1][i] = persons.get(i).getNoOut() / 60;
					yData[2][i] = persons.get(i).getNoPending() / 60;
					if (yData[2][i] > 1999) {
						yData[2][i] = 1999;
					}
				}
				chartBar.setChart(xData, legend, yData, "Today's Workflow");
				persons.clear();
			}
			if (model != null) {
				model.fireTableDataChanged();
			}
			application.display(message + "Next update "
					+ application.dates.formatter(application.getNextUpdate(), LibDates.FORMAT_DATETIME));
			application.setBusy(false);
		}
	}
}