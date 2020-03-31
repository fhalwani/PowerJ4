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

class NWorkdays extends NBase {
	private boolean byService = true;
	private short facID = 0;
	private long timeFrom = 0;
	private long timeTo = 0;
	volatile ArrayList<DataHeader> headers = new ArrayList<DataHeader>();
	volatile ArrayList<DataRow> list = new ArrayList<DataRow>();
	private ModelWorkdays model;
	private ITable tblList;
	private IChartBar chartBar;

	public NWorkdays(AClient parent) {
		super(parent);
		setName("Workdays");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_WORKDAYS);
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		headers.clear();
		list.clear();
		if (chartBar != null) {
			chartBar.close();
		}
		return true;
	}

	private void createPanel() {
		model = new ModelWorkdays();
		tblList = new ITable(pj, model) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					JTable t = (JTable) e.getSource();
					Point p = e.getPoint();
					int v = rowAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					String s = "";
					if (m >= 0 && m < list.size()) {
						s = list.get(m).full;
					}
					return s;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		JScrollPane scrollList = IGUI.createJScrollPane(tblList);
		Dimension dim = new Dimension(1000, 300);
		chartBar = new IChartBar(dim);
		JScrollPane scrollChart = IGUI.createJScrollPane(chartBar);
		scrollChart.setMinimumSize(dim);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = pj.dates.setMidnight(null);
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calMax.set(Calendar.DAY_OF_MONTH, 1);
		timeFrom = pj.setup.getLong(LSetup.VAR_MIN_WL_DATE);
		timeTo = calMax.getTimeInMillis();
		calMin.setTimeInMillis(timeFrom);
		calStart.setTimeInMillis(calMin.getTimeInMillis());
		calEnd.setTimeInMillis(calMax.getTimeInMillis());
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(scrollChart);
		splitAll.setBottomComponent(scrollList);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this, calStart, calEnd, calMin, calMax, false), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		if (headers.size() == 0)
			return;
		String fileName = pj.getFilePdf("workdays.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = new float[headers.size()];
		String str = "Workdays - " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATELONG) + " - "
				+ pj.dates.formatter(timeTo, LDates.FORMAT_DATELONG);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable table = new PdfPTable(headers.size());
		for (int col = 0; col < headers.size(); col++) {
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
			for (int col = 0; col < headers.size(); col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(headers.get(col).srvName));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data rows
			int i = 0;
			for (int row = 0; row < tblList.getRowCount(); row++) {
				i = tblList.convertRowIndexToModel(row);
				for (int col = 0; col < headers.size(); col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					if (col == 0) {
						paragraph.add(new Chunk(list.get(i).name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					} else if (col > 1) {
						paragraph.add(new Chunk(pj.numbers.formatNumber(list.get(i).services[col - 2])));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					} else {
						paragraph.add(new Chunk(pj.numbers.formatNumber(list.get(i).noDays)));
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
		case IToolBar.TB_FAC:
			facID = value;
			break;
		case IToolBar.TB_SPIN:
			byService = !byService;
			if (timeTo > timeFrom) {
				WorkerData worker = new WorkerData();
				worker.execute();
			}
			break;
		default:
			if (timeTo > timeFrom) {
				WorkerData worker = new WorkerData();
				worker.execute();
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
	}

	@Override
	void xls() {
		if (headers.size() == 0)
			return;
		String fileName = pj.getFileXls("workdays.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Workdays");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Workdays - " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATELONG) + " - "
					+ pj.dates.formatter(timeTo, LDates.FORMAT_DATELONG));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col < headers.size(); col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellStyle(styles.get("header"));
				xlsCell.setCellValue(headers.get(col).srvName);
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
			for (int row = 0; row < tblList.getRowCount(); row++) {
				i = tblList.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < headers.size(); col++) {
					xlsCell = xlsRow.createCell(col);
					if (col == 0) {
						xlsCell.setCellValue(list.get(i).name);
					} else if (col > 1) {
						xlsCell.setCellValue(list.get(i).services[col - 2]);
					} else {
						xlsCell.setCellValue(list.get(i).noDays);
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
			return headers.get(col).srvName;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (list.size() > 0 && row < list.size()) {
				if (col == 0) {
					value = list.get(row).name;
				} else if (col > 1) {
					value = list.get(row).services[col - 2];
				} else {
					value = list.get(row).noDays;
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			short tmpSrvID = 0;
			short tmpPrsID = 0;
			int tmpWdID = 0;
			String service = "";
			DataSrvce prsSrvce = new DataSrvce();
			DataPerson person = new DataPerson();
			DataHeader header = new DataHeader();
			DataRow row = new DataRow();
			ResultSet rst = null;
			HashMap<Short, DataPerson> persons = new HashMap<Short, DataPerson>();
			HashMap<Short, String> services = new HashMap<Short, String>();
			try {
				headers.clear();
				list.clear();
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SCH_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SCH_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SCH_SL_SUM));
				while (rst.next()) {
					if (facID > 0 && facID != rst.getShort("FAID")) {
						continue;
					}
					if (tmpPrsID != rst.getShort("PRID")) {
						tmpWdID = 0;
						tmpSrvID = 0;
						tmpPrsID = rst.getShort("PRID");
						person = persons.get(tmpPrsID);
						if (person == null) {
							person = new DataPerson();
							person.prsID = tmpPrsID;
							person.name = rst.getString("PRNM");
							person.full = rst.getString("PRFR").trim() + " " + rst.getString("PRLS").trim();
							persons.put(tmpPrsID, person);
						}
					}
					if (tmpWdID != rst.getInt("WDID")) {
						tmpWdID = rst.getInt("WDID");
						person.noDays++;
					}
					if (byService) {
						if (tmpSrvID != rst.getShort("SRID")) {
							tmpSrvID = rst.getShort("SRID");
							prsSrvce = person.services.get(tmpSrvID);
							if (prsSrvce == null) {
								prsSrvce = new DataSrvce();
								prsSrvce.name = rst.getString("SRNM");
								person.services.put(tmpSrvID, prsSrvce);
							}
							service = services.get(tmpSrvID);
							if (service == null) {
								services.put(tmpSrvID, prsSrvce.name);
							}
						}
					} else {
						if (tmpSrvID != rst.getShort("SBID")) {
							tmpSrvID = rst.getShort("SBID");
							prsSrvce = person.services.get(tmpSrvID);
							if (prsSrvce == null) {
								prsSrvce = new DataSrvce();
								prsSrvce.name = rst.getString("SBNM");
								person.services.put(tmpSrvID, prsSrvce);
							}
							service = services.get(tmpSrvID);
							if (service == null) {
								services.put(tmpSrvID, prsSrvce.name);
							}
						}
					}
					prsSrvce.noDays += 1;
				}
				ArrayList<DataHeader> temp = new ArrayList<DataHeader>();
				for (Entry<Short, String> entry : services.entrySet()) {
					header = new DataHeader();
					header.srvID = entry.getKey();
					header.srvName = entry.getValue();
					temp.add(header);
				}
				int noServices = temp.size();
				Collections.sort(temp, new Comparator<DataHeader>() {
					@Override
					public int compare(DataHeader o1, DataHeader o2) {
						return o1.srvName.compareToIgnoreCase(o2.srvName);
					}
				});
				header = new DataHeader();
				header.srvName = "NAME";
				headers.add(header);
				header = new DataHeader();
				header.srvName = "DAYS";
				headers.add(header);
				for (int i = 0; i < noServices; i++) {
					header = new DataHeader();
					header.srvID = temp.get(i).srvID;
					header.srvName = temp.get(i).srvName;
					headers.add(header);
				}
				for (Entry<Short, DataPerson> entry : persons.entrySet()) {
					person = entry.getValue();
					row = new DataRow();
					row.prsID = person.prsID;
					row.name = person.name;
					row.full = person.full;
					row.noDays = person.noDays;
					row.services = new short[noServices];
					for (int i = 0; i < noServices; i++) {
						header = temp.get(i);
						DataSrvce ds = person.services.get(header.srvID);
						if (ds == null) {
							row.services[i] = 0;
						} else if (header.srvName.equals(ds.name)) {
							row.services[i] = ds.noDays;
						} else {
							row.services[i] = 0;
						}
					}
					list.add(row);
				}
				Collections.sort(list, new Comparator<DataRow>() {
					@Override
					public int compare(DataRow o1, DataRow o2) {
						return (o1.noDays > o2.noDays ? -1 : (o1.noDays < o2.noDays ? 1 : o1.name.compareTo(o2.name)));
					}
				});
				DataRow total = new DataRow();
				total.name = "Ztotal";
				total.full = "Ztotal";
				total.services = new short[noServices];
				for (int i = 0; i < list.size() - 1; i++) {
					total.noDays += list.get(i).noDays;
					for (int j = 0; j < noServices; j++) {
						total.services[j] += list.get(i).services[j];
					}
				}
				list.add(total);
				persons.clear();
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
			return null;
		}

		@Override
		public void done() {
			model.fireTableStructureChanged();
			if (list.size() > 1) {
				// Chart Data Set
				String[] x = new String[list.size() - 1];
				double[] y = new double[list.size() - 1];
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).prsID > 0) {
						x[i] = list.get(i).name;
						y[i] = list.get(i).noDays;
					}
				}
				chartBar.setChart(x, y, "Workdays Distribution");
			}
			pj.setBusy(false);
		}
	}

	private class DataHeader {
		short srvID = 0;
		String srvName = "";
	}

	private class DataPerson {
		short prsID = 0;
		short noDays = 0;
		String name = "";
		String full = "";
		HashMap<Short, DataSrvce> services = new HashMap<Short, DataSrvce>();
	}

	private class DataRow {
		short prsID = 0;
		short noDays = 0;
		String name = "";
		String full = "";
		short[] services;
	}

	private class DataSrvce {
		short noDays = 0;
		String name = "";
	}
}
