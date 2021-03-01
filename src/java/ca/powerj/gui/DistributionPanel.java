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
import ca.powerj.data.WorkloadData;
import ca.powerj.data.DistributionPersonList;
import ca.powerj.data.DistributionSubspecialtyList;
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

class DistributionPanel extends BasePanel {
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_DATA = 2;
	private int[] filters = { 0, 0, 1 };
	private double annualFte = 1;
	private long timeFrom = 0;
	private long timeTo = 0;
	private String coderName = "Cases";
	private ArrayList<ItemData> itemsList = new ArrayList<ItemData>();
	private ArrayList<DistributionPersonList> subspecialtyList = new ArrayList<DistributionPersonList>();
	private ModelFTE model;
	private ITable table;
	private IChartBar chartBar;

	DistributionPanel(AppFrame application) {
		super(application);
		setName("Distribution");
		application.dbPowerJ.setStatements(LibConstants.ACTION_DISTRIBUTE);
		createPanel();
		programmaticChange = false;
		altered = true;
	}

	@Override
	public boolean close() {
		altered = false;
		super.close();
		itemsList.clear();
		subspecialtyList.clear();
		if (chartBar != null) {
			chartBar.close();
		}
		return true;
	}

	private void createPanel() {
		model = new ModelFTE();
		table = new ITable(model, application.dates, application.numbers) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int c = columnAtPoint(p);
					if (c == 0) {
						int v = rowAtPoint(p);
						int m = t.convertRowIndexToModel(v);
						return subspecialtyList.get(m).getPrsFull();
					}
				} catch (IndexOutOfBoundsException ignore) {
				}
				return null;
			}
		};
		JScrollPane scrollList = IUtilities.createJScrollPane(table);
		Dimension dim = new Dimension(1000, 300);
		chartBar = new IChartBar(dim);
		JScrollPane scrollChart = IUtilities.createJScrollPane(chartBar);
		scrollChart.setMinimumSize(dim);
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(scrollChart);
		splitAll.setBottomComponent(scrollList);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1100, 900));
		Calendar calStart = application.dates.setMidnight(null);
		Calendar calEnd = application.dates.setMidnight(null);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = Calendar.getInstance();
		calStart.add(Calendar.YEAR, -1);
		timeFrom = calStart.getTimeInMillis();
		timeTo = calEnd.getTimeInMillis();
		calMax.setTimeInMillis(timeTo);
		calMin.setTimeInMillis(application.setup.getLong(LibSetup.VAR_MIN_WL_DATE));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(application, calStart, calEnd, calMin, calMax, null), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		if (itemsList.size() == 0)
			return;
		String fileName = application.getFilePdf("distribution.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = new float[itemsList.size() + 1];
		String str = "Distribution - " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATE) + " - "
				+ application.dates.formatter(timeTo, LibDates.FORMAT_DATE);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable pdfTable = new PdfPTable(itemsList.size() + 1);
		for (int col = 0; col <= itemsList.size(); col++) {
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
			for (int col = 0; col <= itemsList.size(); col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				if (col == 0) {
					paragraph.add(new Chunk("Staff"));
				} else {
					paragraph.add(new Chunk(itemsList.get(col - 1).getName()));
				}
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				pdfTable.addCell(cell);
			}
			pdfTable.setHeaderRows(1);
			// data list
			int i = 0;
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				for (int col = 0; col <= itemsList.size(); col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case 0:
						paragraph.add(new Chunk(subspecialtyList.get(i).getPrsName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case 1:
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							paragraph.add(
									new Chunk(application.numbers.formatDouble(3, subspecialtyList.get(i).getFte())));
						} else {
							paragraph.add(
									new Chunk(application.numbers.formatNumber(subspecialtyList.get(i).getCount())));
						}
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							if (subspecialtyList.get(i).getDouble(itemsList.get(col - 1).getShortID()) != 0.0) {
								paragraph.add(new Chunk(application.numbers.formatDouble(3,
										subspecialtyList.get(i).getDouble(itemsList.get(col - 1).getShortID()))));
							} else {
								paragraph.add(new Chunk(""));
							}
						} else {
							if (subspecialtyList.get(i).getInteger(itemsList.get(col - 1).getShortID()) != 0.0) {
								paragraph.add(new Chunk(application.numbers.formatNumber(
										subspecialtyList.get(i).getInteger(itemsList.get(col - 1).getShortID()))));
							} else {
								paragraph.add(new Chunk(""));
							}
						}
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
	public void setFilter(short id, int value) {
		switch (id) {
		case IToolBar.TB_GO:
			// Go button
			if (altered && timeTo > timeFrom) {
				application.setBusy(true);
				WorkerData worker = new WorkerData();
				worker.execute();
				altered = false;
			}
			break;
		case IToolBar.TB_FAC:
			filters[FILTER_FAC] = value;
			altered = true;
			break;
		case IToolBar.TB_SPY:
			filters[FILTER_SPY] = value;
			altered = true;
			break;
		default:
			filters[FILTER_DATA] = value;
			altered = true;
			switch (value) {
			case IToolBar.TB_CASES:
				coderName = "Cases";
				annualFte = 1;
				break;
			case IToolBar.TB_SPECS:
				coderName = "Specimens";
				annualFte = 1;
				break;
			case IToolBar.TB_BLOCKS:
				coderName = "Blocks";
				annualFte = 1;
				break;
			case IToolBar.TB_SLIDES:
				coderName = "Slides";
				annualFte = 1;
				break;
			case IToolBar.TB_VALUE1:
				coderName = application.getProperty("coder1");
				annualFte = Double.parseDouble(application.setup.getString(LibSetup.VAR_CODER1_FTE));
				break;
			case IToolBar.TB_VALUE2:
				coderName = application.getProperty("coder2");
				annualFte = Double.parseDouble(application.setup.getString(LibSetup.VAR_CODER2_FTE));
				break;
			case IToolBar.TB_VALUE3:
				coderName = application.getProperty("coder3");
				annualFte = Double.parseDouble(application.setup.getString(LibSetup.VAR_CODER3_FTE));
				break;
			case IToolBar.TB_VALUE4:
				coderName = application.getProperty("coder4");
				annualFte = Double.parseDouble(application.setup.getString(LibSetup.VAR_CODER4_FTE));
				break;
			default:
				coderName = application.getProperty("coder5");
				annualFte = Double.parseDouble(application.setup.getString(LibSetup.VAR_V5_FTE));
			}
		}
	}

	@Override
	void xls() {
		if (itemsList.size() == 0)
			return;
		String fileName = application.getFileXls("distribution.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Distribution");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Distribution - " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATE) + " - "
					+ application.dates.formatter(timeTo, LibDates.FORMAT_DATE));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, itemsList.size()));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col <= itemsList.size(); col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellStyle(styles.get("header"));
				if (col == 0) {
					xlsCell.setCellValue("Staff");
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				} else {
					xlsCell.setCellValue(itemsList.get(col - 1).getName());
					if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
						sheet.setDefaultColumnStyle(col, styles.get("data_double"));
					} else {
						sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					}
				}
				sheet.setColumnWidth(col, 8 * 256); // 8 characters
			}
			// data list
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col <= itemsList.size(); col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case 0:
						xlsCell.setCellValue(subspecialtyList.get(i).getPrsName());
						break;
					case 1:
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							xlsCell.setCellValue(subspecialtyList.get(i).getFte());
						} else {
							xlsCell.setCellValue(subspecialtyList.get(i).getCount());
						}
						break;
					default:
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							xlsCell.setCellValue(
									subspecialtyList.get(i).getDouble(itemsList.get(col - 1).getShortID()));
						} else {
							xlsCell.setCellValue(
									subspecialtyList.get(i).getInteger(itemsList.get(col - 1).getShortID()));
						}
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

	private class ModelFTE extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			} else if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
				return Double.class;
			} else {
				return Integer.class;
			}
		}

		@Override
		public int getColumnCount() {
			return itemsList.size() + 1;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) {
				return "Staff";
			} else {
				return itemsList.get(col - 1).getName();
			}
		}

		@Override
		public int getRowCount() {
			return subspecialtyList.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (subspecialtyList.size() > 0 && row < subspecialtyList.size()) {
				switch (col) {
				case 0:
					return subspecialtyList.get(row).getPrsName();
				case 1:
					if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
						return subspecialtyList.get(row).getFte();
					} else {
						return subspecialtyList.get(row).getCount();
					}
				default:
					if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
						return subspecialtyList.get(row).getDouble(itemsList.get(col - 1).getShortID());
					} else {
						return subspecialtyList.get(row).getInteger(itemsList.get(col - 1).getShortID());
					}
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {
		private HashMap<Short, DistributionPersonList> persons = new HashMap<Short, DistributionPersonList>();
		private HashMap<Short, DistributionSubspecialtyList> subspecialties = new HashMap<Short, DistributionSubspecialtyList>();
		private ItemData itemData = new ItemData((short) 0, "");
		private DistributionSubspecialtyList subList = new DistributionSubspecialtyList();
		private DistributionPersonList prsList = new DistributionPersonList();

		@Override
		protected Void doInBackground() throws Exception {
			setName("WorkerData");
			if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
				getDataDouble();
				setListDouble();
			} else {
				getDataInteger();
				setListInteger();
			}
			return null;
		}

		@Override
		public void done() {
			model.fireTableStructureChanged();
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			if (subspecialtyList.size() > 1) {
				// Chart Data Set
				ArrayList<String> xData = new ArrayList<String>();
				ArrayList<Double> yData = new ArrayList<Double>();
				for (int i = 0; i < subspecialtyList.size(); i++) {
					if (subspecialtyList.get(i).getPrsID() > 0) {
						xData.add(subspecialtyList.get(i).getPrsName());
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							yData.add(subspecialtyList.get(i).getFte());
						} else {
							yData.add((double) subspecialtyList.get(i).getCount());
						}
						if (xData.size() == 40) {
							break;
						}
					}
				}
				String[] x = new String[xData.size()];
				double[] y = new double[yData.size()];
				for (int i = 0; i < xData.size(); i++) {
					x[i] = xData.get(i);
					y[i] = yData.get(i);
				}
				chartBar.setChart(x, y, "Distribution");
			}
			application.display("Distribution " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATELONG)
					+ " - " + application.dates.formatter(timeTo, LibDates.FORMAT_DATELONG));
			application.setBusy(false);
		}

		private void getDataDouble() {
			boolean found = false;
			short subID = 0;
			short prsID = 0;
			Double newValue = 0.0;
			ArrayList<WorkloadData> list = application.dbPowerJ.getCaseSums(true, application.getUserID(), timeFrom, timeTo);
			WorkloadData item = new WorkloadData();
			itemsList.clear();
			subspecialtyList.clear();
			for (int x = 0; x < list.size(); x++) {
				item = list.get(x);
				if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == item.getFacID()) {
					if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == item.getSpyID()) {
						subID = item.getSubID();
						prsID = item.getPrsID();
						found = false;
						for (int i = 0; i < itemsList.size(); i++) {
							itemData = itemsList.get(i);
							if (itemData.getShortID() == subID) {
								found = true;
								break;
							}
						}
						if (!found) {
							itemData = new ItemData(subID, item.getSubName());
							itemsList.add(itemData);
						}
						subList = subspecialties.get(subID);
						if (subList == null) {
							subList = new DistributionSubspecialtyList();
							subspecialties.put(subID, subList);
						}
						prsList = persons.get(prsID);
						if (prsList == null) {
							prsList = new DistributionPersonList();
							prsList.setPrsName(item.getPrsName());
							prsList.setPrsFull(item.getPrsFull());
							persons.put(prsID, prsList);
						}
						switch (filters[FILTER_DATA]) {
						case IToolBar.TB_VALUE1:
							newValue = item.getFte1();
							break;
						case IToolBar.TB_VALUE2:
							newValue = item.getFte2();
							break;
						case IToolBar.TB_VALUE3:
							newValue = item.getFte3();
							break;
						case IToolBar.TB_VALUE4:
							newValue = item.getFte4();
							break;
						default:
							newValue = item.getFte5();
						}
						prsList.setFte(prsList.getFte() + newValue);
						subList.setFte(subList.getFte() + newValue);
						prsList.setDouble(subID, newValue);
						subList.setDouble(prsID, newValue);
					}
				}
			}
			list.clear();
			if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == 3) {
				list = application.dbPowerJ.getFrozenSums(true, application.getUserID(), timeFrom, timeTo);
				for (int x = 0; x < list.size(); x++) {
					item = list.get(x);
					subID = item.getSubID();
					prsID = item.getPrsID();
					found = false;
					for (int i = 0; i < itemsList.size(); i++) {
						itemData = itemsList.get(i);
						if (itemData.getShortID() == subID) {
							found = true;
							break;
						}
					}
					if (!found) {
						itemData = new ItemData(subID, item.getSubName());
						itemsList.add(itemData);
					}
					subList = subspecialties.get(subID);
					if (subList == null) {
						subList = new DistributionSubspecialtyList();
						subspecialties.put(subID, subList);
					}
					prsList = persons.get(prsID);
					if (prsList == null) {
						prsList = new DistributionPersonList();
						prsList.setPrsName(item.getPrsName());
						prsList.setPrsFull(item.getPrsFull());
						persons.put(prsID, prsList);
					}
					switch (filters[FILTER_DATA]) {
					case IToolBar.TB_VALUE1:
						newValue = item.getFte1();
						break;
					case IToolBar.TB_VALUE2:
						newValue = item.getFte2();
						break;
					case IToolBar.TB_VALUE3:
						newValue = item.getFte3();
						break;
					case IToolBar.TB_VALUE4:
						newValue = item.getFte4();
						break;
					default:
						newValue = item.getFte5();
					}
					prsList.setFte(prsList.getFte() + newValue);
					subList.setFte(subList.getFte() + newValue);
					prsList.setDouble(subID, newValue);
					subList.setDouble(prsID, newValue);
				}
			}
			list.clear();
			list = application.dbPowerJ.getAdditionalSums(timeFrom, timeTo);
			for (int x = 0; x < list.size(); x++) {
				item = list.get(x);
				subID = item.getSubID();
				prsID = item.getPrsID();
				prsList = persons.get(prsID);
				if (prsList == null) {
					// Not a pathologist
					continue;
				}
				found = false;
				for (int i = 0; i < itemsList.size(); i++) {
					itemData = itemsList.get(i);
					if (itemData.getShortID() == subID) {
						found = true;
						break;
					}
				}
				if (!found) {
					itemData = new ItemData(subID, item.getSubName());
					itemsList.add(itemData);
				}
				subList = subspecialties.get(subID);
				if (subList == null) {
					subList = new DistributionSubspecialtyList();
					subspecialties.put(subID, subList);
				}
				switch (filters[FILTER_DATA]) {
				case IToolBar.TB_VALUE1:
					newValue = item.getFte1();
					break;
				case IToolBar.TB_VALUE2:
					newValue = item.getFte2();
					break;
				case IToolBar.TB_VALUE3:
					newValue = item.getFte3();
					break;
				case IToolBar.TB_VALUE4:
					newValue = item.getFte4();
					break;
				default:
					newValue = item.getFte5();
				}
				prsList.setFte(prsList.getFte() + newValue);
				subList.setFte(subList.getFte() + newValue);
				prsList.setDouble(subID, newValue);
				subList.setDouble(prsID, newValue);
			}
			list.clear();
		}

		private void getDataInteger() {
			boolean found = false;
			short subID = 0;
			short prsID = 0;
			Integer newValue = 0;
			Integer subValue = 0;
			Integer prsValue = 0;
			ArrayList<WorkloadData> list = application.dbPowerJ.getCaseSums(true, application.getUserID(), timeFrom, timeTo);
			WorkloadData item = new WorkloadData();
			itemsList.clear();
			subspecialtyList.clear();
			for (int x = 0; x < list.size(); x++) {
				item = list.get(x);
				if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == item.getFacID()) {
					if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == item.getSpyID()) {
						subID = item.getSubID();
						prsID = item.getPrsID();
						found = false;
						for (int i = 0; i < itemsList.size(); i++) {
							itemData = itemsList.get(i);
							if (itemData.getShortID() == subID) {
								found = true;
								break;
							}
						}
						if (!found) {
							itemData = new ItemData(subID, item.getSubName());
							itemsList.add(itemData);
						}
						subList = subspecialties.get(subID);
						if (subList == null) {
							subList = new DistributionSubspecialtyList();
							subspecialties.put(subID, subList);
						}
						prsList = persons.get(prsID);
						if (prsList == null) {
							prsList = new DistributionPersonList();
							prsList.setPrsName(item.getPrsName());
							prsList.setPrsFull(item.getPrsFull());
							persons.put(prsID, prsList);
						}
						prsValue = prsList.getInteger(subID);
						if (prsValue == null) {
							prsValue = 0;
							prsList.setInteger(subID, prsValue);
						}
						subValue = subList.getInteger(prsID);
						if (subValue == null) {
							subValue = 0;
							subList.setInteger(prsID, subValue);
						}
						switch (filters[FILTER_DATA]) {
						case IToolBar.TB_CASES:
							newValue = item.getNoCases();
							break;
						case IToolBar.TB_SPECS:
							newValue = item.getNoSpecs();
							break;
						case IToolBar.TB_BLOCKS:
							newValue = item.getNoBlocks();
							break;
						default:
							newValue = item.getNoSlides();
						}
						prsList.setCount(prsList.getCount() + newValue);
						subList.setCount(subList.getCount() + newValue);
						prsList.setInteger(subID, newValue);
						subList.setInteger(prsID, newValue);
					}
				}
			}
			list.clear();
		}

		private void setListDouble() {
			double totalFTE = 0;
			Double prsValue = 0.0;
			DistributionPersonList staff = new DistributionPersonList();
			itemData = new ItemData((short) 0, coderName);
			itemsList.add(itemData);
			Collections.sort(itemsList, new Comparator<ItemData>() {
				@Override
				public int compare(ItemData h1, ItemData h2) {
					return (h1.getName().compareToIgnoreCase(h2.getName()));
				}
			});
			if (annualFte < 1.00) {
				annualFte = 1.00;
			}
			if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
				int noDays = application.dates.getNoDays(timeFrom, timeTo);
				annualFte = annualFte * noDays / 365.0;
			}
			for (Entry<Short, DistributionPersonList> entry : persons.entrySet()) {
				prsList = entry.getValue();
				if (prsList.getFte() > 0.0) {
					staff = new DistributionPersonList();
					staff.setPrsID((short) entry.getKey());
					staff.setPrsName(prsList.getPrsName());
					staff.setPrsFull(prsList.getPrsFull());
					staff.setFte(prsList.getFte() / annualFte);
					for (int i = 0; i < itemsList.size(); i++) {
						itemData = itemsList.get(i);
						prsValue = prsList.getDouble(itemData.getShortID());
						if (prsValue == null) {
							prsValue = 0.0;
						}
						totalFTE += prsValue;
						prsValue = prsValue / annualFte;
						staff.setDouble(itemData.getShortID(), prsValue);
					}
					subspecialtyList.add(staff);
				}
			}
			Collections.sort(subspecialtyList, new Comparator<DistributionPersonList>() {
				@Override
				public int compare(DistributionPersonList p1, DistributionPersonList p2) {
					return (p1.getFte() > p2.getFte() ? -1 : (p1.getFte() < p2.getFte() ? 1 : 0));
				}
			});
			DistributionPersonList total = new DistributionPersonList();
			total.setFte(totalFTE / annualFte);
			total.setPrsName("ZZZZ");
			total.setPrsFull("Total");
			for (int i = 0; i < itemsList.size(); i++) {
				itemData = itemsList.get(i);
				if (itemData.getShortID() > 0) {
					subList = subspecialties.get(itemData.getShortID());
					prsValue = subList.getFte();
					if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
						prsValue = prsValue / annualFte;
					}
					total.setDouble(itemData.getShortID(), prsValue);
				}
			}
			subspecialtyList.add(total);
			persons.clear();
			subspecialties.clear();
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}

		private void setListInteger() {
			int totalCount = 0;
			Integer prsValue = 0;
			DistributionPersonList staff = new DistributionPersonList();
			itemData = new ItemData((short) 0, coderName);
			itemsList.add(itemData);
			Collections.sort(itemsList, new Comparator<ItemData>() {
				@Override
				public int compare(ItemData h1, ItemData h2) {
					return (h1.getName().compareToIgnoreCase(h2.getName()));
				}
			});
			for (Entry<Short, DistributionPersonList> entry : persons.entrySet()) {
				prsList = entry.getValue();
				if (prsList.getCount() > 0) {
					staff = new DistributionPersonList();
					staff.setPrsID((short) entry.getKey());
					staff.setPrsName(prsList.getPrsName());
					staff.setPrsFull(prsList.getPrsFull());
					staff.setCount(prsList.getCount());
					for (int i = 0; i < itemsList.size(); i++) {
						itemData = itemsList.get(i);
						prsValue = prsList.getInteger(itemData.getShortID());
						if (prsValue == null) {
							prsValue = 0;
						}
						totalCount += prsValue;
						staff.setInteger(itemData.getShortID(), prsValue);
					}
					subspecialtyList.add(staff);
				}
			}
			Collections.sort(subspecialtyList, new Comparator<DistributionPersonList>() {
				@Override
				public int compare(DistributionPersonList p1, DistributionPersonList p2) {
					return (p1.getCount() > p2.getCount() ? -1 : (p1.getCount() < p2.getCount() ? 1 : 0));
				}
			});
			DistributionPersonList total = new DistributionPersonList();
			total.setCount(totalCount);
			total.setPrsName("ZZZZ");
			total.setPrsFull("Total");
			for (int i = 0; i < itemsList.size(); i++) {
				itemData = itemsList.get(i);
				if (itemData.getShortID() > 0) {
					subList = subspecialties.get(itemData.getShortID());
					prsValue = subList.getCount();
					total.setInteger(itemData.getShortID(), prsValue);
				}
			}
			subspecialtyList.add(total);
			persons.clear();
			subspecialties.clear();
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}
}