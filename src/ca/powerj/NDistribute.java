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

class NDistribute extends NBase {
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_DATA = 2;
	private short[] filters = { 0, 0, 1 };
	private double annualFte = 1;
	private long timeFrom = 0;
	private long timeTo = 0;
	private String coderName = "Cases";
	private ArrayList<DataHeader> headers = new ArrayList<DataHeader>();
	private ArrayList<DataPerson> list = new ArrayList<DataPerson>();
	private ModelFTE model;
	private ITable tblList;
	private IChartBar chartBar;

	NDistribute(AClient parent) {
		super(parent);
		setName("Distribution");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_DISTRIBUTE);
		createPanel();
		programmaticChange = false;
		altered = true;
	}

	@Override
	boolean close() {
		altered = false;
		super.close();
		headers.clear();
		list.clear();
		if (chartBar != null) {
			chartBar.close();
		}
		return true;
	}

	private void createPanel() {
		model = new ModelFTE();
		tblList = new ITable(pj, model) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int c = columnAtPoint(p);
					if (c == 0) {
						int v = rowAtPoint(p);
						int m = t.convertRowIndexToModel(v);
						return list.get(m).prsFull;
					}
				} catch (IndexOutOfBoundsException ignore) {
				}
				return null;
			}
		};
		JScrollPane scrollList = IGUI.createJScrollPane(tblList);
		Dimension dim = new Dimension(1000, 300);
		chartBar = new IChartBar(dim);
		JScrollPane scrollChart = IGUI.createJScrollPane(chartBar);
		scrollChart.setMinimumSize(dim);
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(scrollChart);
		splitAll.setBottomComponent(scrollList);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1100, 900));
		Calendar calStart = pj.dates.setMidnight(null);
		Calendar calEnd = pj.dates.setMidnight(null);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = Calendar.getInstance();
		calStart.add(Calendar.YEAR, -1);
		timeFrom = calStart.getTimeInMillis();
		timeTo = calEnd.getTimeInMillis();
		calMax.setTimeInMillis(timeTo);
		calMin.setTimeInMillis(pj.setup.getLong(LSetup.VAR_MIN_WL_DATE));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this, calStart, calEnd, calMin, calMax, null),
				BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		if (headers.size() == 0)
			return;
		String fileName = pj.getFilePdf("distribution.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = new float[headers.size() + 1];
		String str = "Distribution - " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATE)
			+ " - " + pj.dates.formatter(timeTo, LDates.FORMAT_DATE);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable table = new PdfPTable(headers.size() + 1);
		for (int col = 0; col <= headers.size(); col++) {
			widths[col] = 1;
		}
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
			for (int col = 0; col <= headers.size(); col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				if (col == 0) {
					paragraph.add(new Chunk("Staff"));
				} else {
					paragraph.add(new Chunk(headers.get(col - 1).subName));
				}
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data list
			int i = 0;
			for (int row = 0; row < tblList.getRowCount(); row++) {
				i = tblList.convertRowIndexToModel(row);
				for (int col = 0; col <= headers.size(); col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case 0:
						paragraph.add(new Chunk(list.get(i).prsName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case 1:
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							paragraph.add(new Chunk(pj.numbers.formatDouble(3, list.get(i).fte)));
						} else {
							paragraph.add(new Chunk(pj.numbers.formatNumber(list.get(i).count)));
						}
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							if (list.get(i).dSubs.get(headers.get(col - 1).subID) != 0.0) {
								paragraph.add(new Chunk(pj.numbers.formatDouble(3, list.get(i).dSubs.get(headers.get(col - 1).subID))));
							} else {
								paragraph.add(new Chunk(""));
							}
						} else {
							if (list.get(i).iSubs.get(headers.get(col - 1).subID) != 0.0) {
								paragraph.add(new Chunk(pj.numbers.formatNumber(list.get(i).iSubs.get(headers.get(col - 1).subID))));
							} else {
								paragraph.add(new Chunk(""));
							}
						}
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
	void setFilter(short id, short value) {
		switch (id) {
		case IToolBar.TB_GO:
			// Go button
			if (altered && timeTo > timeFrom) {
				pj.setBusy(true);
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
				coderName = pj.setup.getString(LSetup.VAR_CODER1_NAME);
				annualFte = Double.parseDouble(pj.setup.getString(LSetup.VAR_CODER1_FTE));
				break;
			case IToolBar.TB_VALUE2:
				coderName = pj.setup.getString(LSetup.VAR_CODER2_NAME);
				annualFte = Double.parseDouble(pj.setup.getString(LSetup.VAR_CODER2_FTE));
				break;
			case IToolBar.TB_VALUE3:
				coderName = pj.setup.getString(LSetup.VAR_CODER3_NAME);
				annualFte = Double.parseDouble(pj.setup.getString(LSetup.VAR_CODER3_FTE));
				break;
			case IToolBar.TB_VALUE4:
				coderName = pj.setup.getString(LSetup.VAR_CODER4_NAME);
				annualFte = Double.parseDouble(pj.setup.getString(LSetup.VAR_CODER4_FTE));
				break;
			default:
				coderName = pj.setup.getString(LSetup.VAR_V5_NAME);
				annualFte = Double.parseDouble(pj.setup.getString(LSetup.VAR_V5_FTE));
			}
		}
	}

	@Override
	void setFilter(short id, Calendar value) {
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
		String fileName = pj.getFileXls("distribution.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Distribution");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Distribution - " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATE)
					+ " - " + pj.dates.formatter(timeTo, LDates.FORMAT_DATE));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size()));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col <= headers.size(); col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellStyle(styles.get("header"));
				if (col == 0) {
					xlsCell.setCellValue("Staff");
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				} else {
					xlsCell.setCellValue(headers.get(col -1).subName);
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
			for (int row = 0; row < tblList.getRowCount(); row++) {
				i = tblList.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col <= headers.size(); col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case 0:
						xlsCell.setCellValue(list.get(i).prsName);
						break;
					case 1:
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							xlsCell.setCellValue(list.get(i).fte);
						} else {
							xlsCell.setCellValue(list.get(i).count);
						}
						break;
					default:
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							xlsCell.setCellValue(list.get(i).dSubs.get(headers.get(col - 1).subID));
						} else {
							xlsCell.setCellValue(list.get(i).iSubs.get(headers.get(col - 1).subID));
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
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		} catch (IOException e) {
			pj.log(LConstants.ERROR_IO, getName(), e);
		} catch (Exception e) {
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), e);
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
			return headers.size() + 1;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) {
				return "Staff";
			} else {
				return headers.get(col - 1).subName;
			}
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (list.size() > 0 && row < list.size()) {
				switch (col) {
				case 0:
					return list.get(row).prsName;
				case 1:
					if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
						return list.get(row).fte;
					} else {
						return list.get(row).count;
					}
				default:
					if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
						return list.get(row).dSubs.get(headers.get(col - 1).subID);
					} else {
						return list.get(row).iSubs.get(headers.get(col - 1).subID);
					}
				}
			}
			return value;
		}
	}

	private class DataHeader {
		byte subID = 0;
		String subName = "";
	}

	private class DataPerson {
		short prsID = 0;
		int count = 0;
		double fte = 0;
		String prsName = "";
		String prsFull = "";
		HashMap<Byte, Double> dSubs = new HashMap<Byte, Double>();
		HashMap<Byte, Integer> iSubs = new HashMap<Byte, Integer>();
	}

	private class DataSubspec {
		int count = 0;
		double fte = 0;
		HashMap<Short, Double> dPersons = new HashMap<Short, Double>();
		HashMap<Short, Integer> iPersons = new HashMap<Short, Integer>();
	}

	private class WorkerData extends SwingWorker<Void, Void> {
		private HashMap<Short, DataPerson> persons = new HashMap<Short, DataPerson>();
		private HashMap<Byte, DataSubspec> subspecs = new HashMap<Byte, DataSubspec>();
		private DataHeader header = new DataHeader();
		private DataSubspec subspec = new DataSubspec();
		private DataPerson person = new DataPerson();

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
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			if (list.size() > 1) {
				// Chart Data Set
				ArrayList<String> xData = new ArrayList<String>();
				ArrayList<Double> yData = new ArrayList<Double>();
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).prsID > 0) {
						xData.add(list.get(i).prsName);
						if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
							yData.add(list.get(i).fte);
						} else {
							yData.add((double) list.get(i).count);
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
			pj.statusBar.setMessage("Distribution " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATELONG) + " - "
					+ pj.dates.formatter(timeTo, LDates.FORMAT_DATELONG));
			pj.setBusy(false);
		}

		private void getDataDouble() {
			boolean found = false;
			byte subID = 0;
			short prsID = 0;
			Double newValue = 0.0;
			Double subValue = 0.0;
			Double prsValue = 0.0;
			ResultSet rst = null;
			headers.clear();
			list.clear();
			try {
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_CSE_SL_SUM));
				while (rst.next()) {
					if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == rst.getShort("faid")) {
						if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == rst.getByte("syid")) {
							subID = rst.getByte("sbid");
							prsID = rst.getShort("fnid");
							found = false;
							for (int i = 0; i < headers.size(); i++) {
								header = headers.get(i);
								if (header.subID == subID) {
									found = true;
									break;
								}
							}
							if (!found) {
								header = new DataHeader();
								header.subID = subID;
								header.subName = rst.getString("sbnm");
								headers.add(header);
							}
							subspec = subspecs.get(subID);
							if (subspec == null) {
								subspec = new DataSubspec();
								subspecs.put(subID, subspec);
							}
							person = persons.get(prsID);
							if (person == null) {
								person = new DataPerson();
								person.prsName = rst.getString("fnnm");
								person.prsFull = rst.getString("fnfr") + " " + rst.getString("fnls");
								persons.put(prsID, person);
							}
							prsValue = person.dSubs.get(subID);
							if (prsValue == null) {
								prsValue = 0.0;
								person.dSubs.put(subID, prsValue);
							}
							subValue = subspec.dPersons.get(prsID);
							if (subValue == null) {
								subValue = 0.0;
								subspec.dPersons.put(prsID, subValue);
							}
							switch (filters[FILTER_DATA]) {
							case IToolBar.TB_VALUE1:
								newValue = rst.getDouble("cav1");
								break;
							case IToolBar.TB_VALUE2:
								newValue = rst.getDouble("cav2");
								break;
							case IToolBar.TB_VALUE3:
								newValue = rst.getDouble("cav3");
								break;
							case IToolBar.TB_VALUE4:
								newValue = rst.getDouble("cav4");
								break;
							default:
								newValue = rst.getDouble("cav5");
							}
							person.fte += newValue;
							subspec.fte += newValue;
							person.dSubs.replace(subID, (newValue + prsValue));
							subspec.dPersons.replace(prsID, (newValue + subValue));
						}
					}
				}
				rst.close();
				// Frozen Sections
				if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == 3) {
					pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_SUM), 1, timeFrom);
					pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_SUM), 2, timeTo);
					rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_FRZ_SL_SUM));
					while (rst.next()) {
						subID = rst.getByte("sbid");
						prsID = rst.getShort("prid");
						found = false;
						for (int i = 0; i < headers.size(); i++) {
							header = headers.get(i);
							if (header.subID == subID) {
								found = true;
								break;
							}
						}
						if (!found) {
							header = new DataHeader();
							header.subID = subID;
							header.subName = rst.getString("sbnm");
							headers.add(header);
						}
						subspec = subspecs.get(subID);
						if (subspec == null) {
							subspec = new DataSubspec();
							subspecs.put(subID, subspec);
						}
						person = persons.get(prsID);
						if (person == null) {
							person = new DataPerson();
							person.prsName = rst.getString("prnm");
							person.prsFull = rst.getString("prfr") + " " + rst.getString("prls");
							persons.put(prsID, person);
						}
						prsValue = person.dSubs.get(subID);
						if (prsValue == null) {
							prsValue = 0.0;
							person.dSubs.put(subID, prsValue);
						}
						subValue = subspec.dPersons.get(prsID);
						if (subValue == null) {
							subValue = 0.0;
							subspec.dPersons.put(prsID, subValue);
						}
						switch (filters[FILTER_DATA]) {
						case IToolBar.TB_VALUE1:
							newValue = rst.getDouble("frv1");
							break;
						case IToolBar.TB_VALUE2:
							newValue = rst.getDouble("frv2");
							break;
						case IToolBar.TB_VALUE3:
							newValue = rst.getDouble("frv3");
							break;
						case IToolBar.TB_VALUE4:
							newValue = rst.getDouble("frv4");
							break;
						default:
							newValue = rst.getDouble("frv5");
						}
						person.fte += newValue;
						subspec.fte += newValue;
						person.dSubs.replace(subID, (newValue + prsValue));
						subspec.dPersons.replace(prsID, (newValue + subValue));
					}
					rst.close();
				}
				// Additional
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ADD_SL_SUM));
				while (rst.next()) {
					subID = rst.getByte("sbid");
					prsID = rst.getShort("prid");
					found = false;
					for (int i = 0; i < headers.size(); i++) {
						header = headers.get(i);
						if (header.subID == subID) {
							found = true;
							break;
						}
					}
					if (!found) {
						header = new DataHeader();
						header.subID = subID;
						header.subName = rst.getString("sbnm");
						headers.add(header);
					}
					subspec = subspecs.get(subID);
					if (subspec == null) {
						subspec = new DataSubspec();
						subspecs.put(subID, subspec);
					}
					person = persons.get(prsID);
					if (person == null) {
						person = new DataPerson();
						person.prsName = rst.getString("prnm");
						person.prsFull = rst.getString("prfr") + " " + rst.getString("prls");
						persons.put(prsID, person);
					}
					prsValue = person.dSubs.get(subID);
					if (prsValue == null) {
						prsValue = 0.0;
						person.dSubs.put(subID, prsValue);
					}
					subValue = subspec.dPersons.get(prsID);
					if (subValue == null) {
						subValue = 0.0;
						subspec.dPersons.put(prsID, subValue);
					}
					switch (filters[FILTER_DATA]) {
					case IToolBar.TB_VALUE1:
						newValue = rst.getDouble("adv1");
						break;
					case IToolBar.TB_VALUE2:
						newValue = rst.getDouble("adv2");
						break;
					case IToolBar.TB_VALUE3:
						newValue = rst.getDouble("adv3");
						break;
					case IToolBar.TB_VALUE4:
						newValue = rst.getDouble("adv4");
						break;
					default:
						newValue = rst.getDouble("adv5");
					}
					person.fte += newValue;
					subspec.fte += newValue;
					person.dSubs.replace(subID, (newValue + prsValue));
					subspec.dPersons.replace(prsID, (newValue + subValue));
				}
				rst.close();
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
		}

		private void getDataInteger() {
			boolean found = false;
			byte subID = 0;
			short prsID = 0;
			Integer newValue = 0;
			Integer subValue = 0;
			Integer prsValue = 0;
			ResultSet rst = null;
			headers.clear();
			list.clear();
			try {
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_CSE_SL_SUM));
				while (rst.next()) {
					if (filters[FILTER_FAC] == 0 || filters[FILTER_FAC] == rst.getShort("faid")) {
						if (filters[FILTER_SPY] == 0 || filters[FILTER_SPY] == rst.getByte("syid")) {
							subID = rst.getByte("sbid");
							prsID = rst.getShort("fnid");
							found = false;
							for (int i = 0; i < headers.size(); i++) {
								header = headers.get(i);
								if (header.subID == subID) {
									found = true;
									break;
								}
							}
							if (!found) {
								header = new DataHeader();
								header.subID = subID;
								header.subName = rst.getString("sbnm");
								headers.add(header);
							}
							subspec = subspecs.get(subID);
							if (subspec == null) {
								subspec = new DataSubspec();
								subspecs.put(subID, subspec);
							}
							person = persons.get(prsID);
							if (person == null) {
								person = new DataPerson();
								person.prsName = rst.getString("fnnm");
								person.prsFull = rst.getString("fnfr") + " " + rst.getString("fnls");
								persons.put(prsID, person);
							}
							prsValue = person.iSubs.get(subID);
							if (prsValue == null) {
								prsValue = 0;
								person.iSubs.put(subID, prsValue);
							}
							subValue = subspec.iPersons.get(prsID);
							if (subValue == null) {
								subValue = 0;
								subspec.iPersons.put(prsID, subValue);
							}
							switch (filters[FILTER_DATA]) {
							case IToolBar.TB_CASES:
								newValue = rst.getInt("caca");
								break;
							case IToolBar.TB_SPECS:
								newValue = rst.getInt("caSP");
								break;
							case IToolBar.TB_BLOCKS:
								newValue = rst.getInt("cabl");
								break;
							default:
								newValue = rst.getInt("casl");
							}
							person.count += newValue;
							subspec.count += newValue;
							person.iSubs.replace(subID, (newValue + prsValue));
							subspec.iPersons.replace(prsID, (newValue + subValue));
						}
					}
				}
				rst.close();
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
		}

		private void setListDouble() {
			double totalFTE = 0;
			Double prsValue = 0.0;
			DataPerson staff = new DataPerson();
			header = new DataHeader();
			header.subName = "aaaa";
			headers.add(header);
			Collections.sort(headers, new Comparator<DataHeader>() {
				@Override
				public int compare(DataHeader h1, DataHeader h2) {
					return (h1.subName.compareToIgnoreCase(h2.subName));
				}
			});
			header.subName = coderName;
			if (annualFte < 1.00) {
				annualFte = 1.00;
			}
			if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
				int noDays = pj.dates.getNoDays(timeFrom, timeTo);
				annualFte = annualFte * noDays / 365.0;
			}
			for (Entry<Short, DataPerson> entry : persons.entrySet()) {
				person = entry.getValue();
				if (person.fte > 0.0) {
					staff = new DataPerson();
					staff.prsID = entry.getKey();
					staff.prsName = person.prsName;
					staff.prsFull = person.prsFull;
					staff.fte = person.fte / annualFte;
					for (int i = 0; i < headers.size(); i++) {
						header = headers.get(i);
						prsValue = person.dSubs.get(header.subID);
						if (prsValue == null) {
							prsValue = 0.0;
						}
						totalFTE += prsValue;
						prsValue = prsValue / annualFte;
						staff.dSubs.put(header.subID, prsValue);
					}
					list.add(staff);
				}
			}
			Collections.sort(list, new Comparator<DataPerson>() {
				@Override
				public int compare(DataPerson p1, DataPerson p2) {
					return (p1.fte > p2.fte ? -1 : (p1.fte < p2.fte ? 1 : 0));
				}
			});
			DataPerson total = new DataPerson();
			total.fte = totalFTE / annualFte;
			total.prsName = "ZZZZ";
			total.prsFull = "Total";
			for (int i = 0; i < headers.size(); i++) {
				header = headers.get(i);
				if (header.subID > 0) {
					subspec = subspecs.get(header.subID);
					prsValue = subspec.fte;
					if (filters[FILTER_DATA] > IToolBar.TB_SLIDES) {
						prsValue = prsValue / annualFte;
					}
					total.dSubs.put(header.subID, prsValue);
				}
			}
			list.add(total);
			persons.clear();
			subspecs.clear();
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}

		private void setListInteger() {
			int totalCount = 0;
			Integer prsValue = 0;
			DataPerson staff = new DataPerson();
			header = new DataHeader();
			header.subName = "aaaa";
			headers.add(header);
			Collections.sort(headers, new Comparator<DataHeader>() {
				@Override
				public int compare(DataHeader h1, DataHeader h2) {
					return (h1.subName.compareToIgnoreCase(h2.subName));
				}
			});
			header.subName = coderName;
			for (Entry<Short, DataPerson> entry : persons.entrySet()) {
				person = entry.getValue();
				if (person.count > 0) {
					staff = new DataPerson();
					staff.prsID = entry.getKey();
					staff.prsName = person.prsName;
					staff.prsFull = person.prsFull;
					staff.count = person.count;
					for (int i = 0; i < headers.size(); i++) {
						header = headers.get(i);
						prsValue = person.iSubs.get(header.subID);
						if (prsValue == null) {
							prsValue = 0;
						}
						totalCount += prsValue;
						staff.iSubs.put(header.subID, prsValue);
					}
					list.add(staff);
				}
			}
			Collections.sort(list, new Comparator<DataPerson>() {
				@Override
				public int compare(DataPerson p1, DataPerson p2) {
					return (p1.count > p2.count ? -1 : (p1.count < p2.count ? 1 : 0));
				}
			});
			DataPerson total = new DataPerson();
			total.count = totalCount;
			total.prsName = "ZZZZ";
			total.prsFull = "Total";
			for (int i = 0; i < headers.size(); i++) {
				header = headers.get(i);
				if (header.subID > 0) {
					subspec = subspecs.get(header.subID);
					prsValue = subspec.count;
					total.iSubs.put(header.subID, prsValue);
				}
			}
			list.add(total);
			persons.clear();
			subspecs.clear();
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}
}