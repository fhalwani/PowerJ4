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
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

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
	private short facID = 0;
	private double annualFte = 0;
	private long timeFrom = 0;
	private long timeTo = 0;
	private HashMap<Short, DataPerson> persons = new HashMap<Short, DataPerson>();
	private HashMap<Byte, DataSubspec> subs = new HashMap<Byte, DataSubspec>();
	private HashMap<Short, DataFacility> facilities = new HashMap<Short, DataFacility>();
	private ArrayList<DataHeader> headers = new ArrayList<DataHeader>();
	private ArrayList<DataPerson> rows = new ArrayList<DataPerson>();
	private ModelFTE model;
	private ITable tblList;
	private IChartBar chartBar;

	NDistribute(AClient parent) {
		super(parent);
		setName("Distribution");
		parent.dbPowerJ.prepareCasesSummary();
		annualFte = Double.parseDouble(parent.setup.getString(LSetup.VAR_V5_FTE));
		if (annualFte < 1.00) {
			annualFte = 1.00;
		}
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		for (Entry<Short, DataFacility> entry1 : facilities.entrySet()) {
			DataFacility facility = entry1.getValue();
			for (Entry<Byte, DataSubspec> entry2 : facility.subspecs.entrySet()) {
				DataSubspec subspec = entry2.getValue();
				subspec.persons.clear();
			}
			facility.subspecs.clear();
		}
		facilities.clear();
		persons.clear();
		subs.clear();
		headers.clear();
		rows.clear();
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
					int v = rowAtPoint(p);
					int c = columnAtPoint(p);
					int m = t.convertRowIndexToModel(v);
					String s = "";
					if (c <= 0) {
						s = rows.get(m).prsFull;
					} else if (c < headers.size()) {
						s = rows.get(m).prsFull + ": " + headers.get(c - 1).subDescr + ": "
								+ rows.get(m).subspecs.get(headers.get(c - 1).subID);
					} else {
						s = rows.get(m).prsFull + ": " + rows.get(m).fte;
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
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(scrollChart);
		splitAll.setBottomComponent(scrollList);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(450);
		splitAll.setPreferredSize(new Dimension(1100, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	private void getData() {
		ResultSet rst = null;
		try {
			for (Entry<Short, DataFacility> entry1 : facilities.entrySet()) {
				DataFacility facility = entry1.getValue();
				for (Entry<Byte, DataSubspec> entry2 : facility.subspecs.entrySet()) {
					DataSubspec subspec = entry2.getValue();
					subspec.persons.clear();
				}
				facility.subspecs.clear();
			}
			facilities.clear();
			persons.clear();
			subs.clear();
			pj.dbPowerJ.setDate(DPowerJ.STM_CSE_SL_SUM, 1, timeFrom);
			pj.dbPowerJ.setDate(DPowerJ.STM_CSE_SL_SUM, 2, timeTo);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_CSE_SL_SUM);
			while (rst.next()) {
				DataFacility facility = facilities.get(rst.getShort("FAID"));
				if (facility == null) {
					facility = new DataFacility();
					facilities.put(rst.getShort("FAID"), facility);
				}
				DataSubspec subspec = facility.subspecs.get(rst.getByte("SBID"));
				if (subspec == null) {
					subspec = new DataSubspec();
					subspec.subID = rst.getByte("SBID");
					facility.subspecs.put(rst.getByte("SBID"), subspec);
				}
				DataPerson person = subspec.persons.get(rst.getShort("FNID"));
				if (person == null) {
					person = new DataPerson();
					person.prsID = rst.getShort("FNID");
					subspec.persons.put(rst.getShort("FNID"), person);
				}
				person.fte += rst.getDouble("CAV5");
				subspec.fte += rst.getDouble("CAV5");
				DataSubspec sub = subs.get(rst.getByte("SBID"));
				if (sub == null) {
					sub = new DataSubspec();
					sub.subName = rst.getString("SBNM");
					sub.subDescr = rst.getString("SBDC");
				}
				DataPerson staff = persons.get(rst.getShort("FNID"));
				if (staff == null) {
					staff = new DataPerson();
					staff.prsName = rst.getString("FNNM");
					staff.prsFull = rst.getString("FNLS");
				}
			}
			rst.close();
			// Frozen Sections
			pj.dbPowerJ.setDate(DPowerJ.STM_FRZ_SL_SUM, 1, timeFrom);
			pj.dbPowerJ.setDate(DPowerJ.STM_FRZ_SL_SUM, 2, timeTo);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_FRZ_SL_SUM);
			while (rst.next()) {
				DataFacility facility = facilities.get(rst.getShort("FAID"));
				if (facility == null) {
					facility = new DataFacility();
					facilities.put(rst.getShort("FAID"), facility);
				}
				DataSubspec subspec = facility.subspecs.get(rst.getByte("SBID"));
				if (subspec == null) {
					subspec = new DataSubspec();
					subspec.subID = rst.getByte("SBID");
					facility.subspecs.put(rst.getByte("SBID"), subspec);
				}
				DataPerson person = subspec.persons.get(rst.getShort("PRID"));
				if (person == null) {
					person = new DataPerson();
					person.prsID = rst.getShort("PRID");
					subspec.persons.put(rst.getShort("PRID"), person);
				}
				person.fte += rst.getDouble("CAV5");
				DataSubspec sub = subs.get(rst.getByte("SBID"));
				if (sub == null) {
					sub = new DataSubspec();
					sub.subName = rst.getString("SBNM");
					sub.subDescr = rst.getString("SBDC");
				}
				DataPerson staff = persons.get(rst.getShort("PRID"));
				if (staff == null) {
					staff = new DataPerson();
					staff.prsName = rst.getString("PRNM");
					staff.prsFull = rst.getString("PRLS");
				}
			}
			// Additional
			pj.dbPowerJ.setDate(DPowerJ.STM_ADD_SL_SUM, 1, timeFrom);
			pj.dbPowerJ.setDate(DPowerJ.STM_ADD_SL_SUM, 2, timeTo);
			rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ADD_SL_SUM);
			while (rst.next()) {
				DataFacility facility = facilities.get(rst.getShort("FAID"));
				if (facility == null) {
					facility = new DataFacility();
					facilities.put(rst.getShort("FAID"), facility);
				}
				DataSubspec subspec = facility.subspecs.get(rst.getByte("SBID"));
				if (subspec == null) {
					subspec = new DataSubspec();
					subspec.subID = rst.getByte("SBID");
					facility.subspecs.put(rst.getByte("SBID"), subspec);
				}
				DataPerson person = subspec.persons.get(rst.getShort("PRID"));
				if (person == null) {
					person = new DataPerson();
					person.prsID = rst.getShort("PRID");
					subspec.persons.put(rst.getShort("PRID"), person);
				}
				person.fte += rst.getDouble("CAV5");
				DataSubspec sub = subs.get(rst.getByte("SBID"));
				if (sub == null) {
					sub = new DataSubspec();
					sub.subName = rst.getString("SBNM");
					sub.subDescr = rst.getString("SBDC");
				}
				DataPerson staff = persons.get(rst.getShort("PRID"));
				if (staff == null) {
					staff = new DataPerson();
					staff.prsName = rst.getString("PRNM");
					staff.prsFull = rst.getString("PRLS");
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("distribution.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = new float[headers.size() + 1];
		String str = "Distribution - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER.rotate(), 36, 18, 18, 18);
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
			// data rows
			int i = 0;
			for (int row = 0; row <= tblList.getRowCount(); row++) {
				i = tblList.convertRowIndexToModel(row);
				for (int col = 0; col <= headers.size(); col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					if (col == 0) {
						paragraph.add(new Chunk(rows.get(i).prsName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					} else if (col < headers.size()) {
						paragraph.add(new Chunk(
								pj.numbers.formatDouble(2, rows.get(i).subspecs.get(headers.get(col - 1).subID))));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					} else {
						paragraph.add(new Chunk(pj.numbers.formatDouble(2, rows.get(i).fte)));
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

	void setFilter() {
		DataPerson rowTotal = new DataPerson();
		headers.clear();
		rows.clear();
		for (Entry<Short, DataFacility> entry1 : facilities.entrySet()) {
			if (facID == 0 || facID == entry1.getKey()) {
				DataFacility facility = entry1.getValue();
				for (Entry<Byte, DataSubspec> entry2 : facility.subspecs.entrySet()) {
					DataSubspec subspec = entry2.getValue();
					if (subspec.fte > 0) {
						boolean found = false;
						DataHeader header = new DataHeader();
						for (int i = 0; i < headers.size(); i++) {
							header = headers.get(i);
							if (header.subID == subspec.subID) {
								found = true;
								break;
							}
						}
						if (!found) {
							header = new DataHeader();
							header.subID = subspec.subID;
							header.subName = subs.get(subspec.subID).subName;
							header.subDescr = subs.get(subspec.subID).subDescr;
							headers.add(header);
						}
						for (Entry<Short, DataPerson> entry3 : subspec.persons.entrySet()) {
							DataPerson person = entry3.getValue();
							if (person.fte > 0) {
								found = false;
								DataPerson row = new DataPerson();
								for (int i = 0; i < rows.size(); i++) {
									row = rows.get(i);
									if (row.prsID == person.prsID) {
										found = true;
										break;
									}
								}
								if (!found) {
									row = new DataPerson();
									row.prsID = person.prsID;
									row.prsName = persons.get(person.prsID).prsName;
									row.prsFull = persons.get(person.prsID).prsFull;
									rows.add(row);
								}
								row.fte += person.fte;
								Double fte5 = row.subspecs.get(header.subID);
								if (fte5 == null) {
									row.subspecs.put(header.subID, person.fte);
								} else {
									fte5 += person.fte;
								}
								rowTotal.fte += person.fte;
								Double fteTotal = row.subspecs.get(header.subID);
								if (fteTotal == null) {
									rowTotal.subspecs.put(header.subID, person.fte);
								} else {
									fteTotal += person.fte;
								}
							}
						}
					}
				}
			}
		}
		rowTotal.prsName = "SUM";
		rowTotal.prsFull = "Total";
		rows.add(rowTotal);
		DataHeader colTotal = new DataHeader();
		colTotal.subName = "SUM";
		colTotal.subDescr = "Total";
		headers.add(colTotal);
		for (DataPerson row : rows) {
			for (Entry<Byte, Double> entry2 : row.subspecs.entrySet()) {
				double d = entry2.getValue().doubleValue();
				entry2.setValue(1.00 * d / annualFte);
			}
		}
		if (rows.size() > 1) {
			// Chart Data Set
			String[] x = new String[rows.size() - 1];
			double[] y = new double[rows.size() - 1];
			for (int i = 0; i < rows.size(); i++) {
				if (rows.get(i).prsID > 0) {
					x[i] = rows.get(i).prsName;
					y[i] = rows.get(i).fte;
				}
			}
			chartBar.setChart(x, y, "FTE Distribution");
		}
	}

	@Override
	void xls() {
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
			xlsCell.setCellValue(
					"Distribution - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$O$1"));
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
					xlsCell.setCellValue(headers.get(col).subName);
					sheet.setDefaultColumnStyle(col, styles.get("data_double"));
				}
				sheet.setColumnWidth(col, 5 * 256); // 5 characters
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < tblList.getRowCount(); row++) {
				i = tblList.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col <= headers.size(); col++) {
					xlsCell = xlsRow.createCell(col);
					if (col == 0) {
						xlsCell.setCellValue(rows.get(i).prsName);
					} else if (col < headers.size()) {
						xlsCell.setCellValue(rows.get(i).subspecs.get(headers.get(col - 1).subID));
					} else {
						xlsCell.setCellValue(rows.get(i).fte);
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
			}
			return Double.class;
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
			return rows.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return rows.get(row).prsName;
			} else if (col < headers.size()) {
				return rows.get(row).subspecs.get(headers.get(col - 1).subID);
			} else {
				return rows.get(row).fte;
			}
		}
	}

	private class DataFacility {
		HashMap<Byte, DataSubspec> subspecs = new HashMap<Byte, DataSubspec>();
	}

	private class DataHeader {
		byte subID = 0;
		String subName = "";
		String subDescr = "";
	}

	private class DataPerson {
		short prsID = 0;
		double fte = 0;
		String prsName = "";
		String prsFull = "";
		private HashMap<Byte, Double> subspecs = new HashMap<Byte, Double>();
	}

	private class DataSubspec {
		byte subID = 0;
		double fte = 0;
		String subName = "";
		String subDescr = "";
		private HashMap<Short, DataPerson> persons = new HashMap<Short, DataPerson>();
	}
}