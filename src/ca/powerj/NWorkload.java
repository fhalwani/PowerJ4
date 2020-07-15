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

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

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

class NWorkload extends NBase {
	private final byte DATA_GROUPS = 0;
	private final byte DATA_CASES  = 1;
	private final byte DATA_SPECS  = 2;
	private final byte DATA_BLOCKS = 3;
	private final byte DATA_SLIDES = 4;
	private final byte DATA_VALUE1 = 5;
	private final byte DATA_VALUE2 = 6;
	private final byte DATA_VALUE3 = 7;
	private final byte DATA_VALUE4 = 8;
	private final byte DATA_VALUE5 = 9;
	private byte[] rowsView = new byte[5];
	private long timeFrom = 0;
	private long timeTo = 0;
	private String[] columns = {"GROUP", "CASES", "SPECS", "BLKS", "SLDS", "", "", "", "", ""};
	private TreePath treePath;
	private ITreeTable tree;
	private IChartPie chartGroup, chartCoder1, chartCoder2, chartCoder3, chartCoder4, chartCoder5;

	NWorkload(AClient parent) {
		super(parent);
		setName("Workload");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_DISTRIBUTE);
		columns[5] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		columns[6] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		columns[7] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		columns[8] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		columns[9] = pj.setup.getString(LSetup.VAR_V5_NAME);
		if (pj.userAccess[LConstants.ACCESS_NAMES]) {
			rowsView[0] = IPanelRows.ROW_FACILITY;
			rowsView[1] = IPanelRows.ROW_SPECIALTY;
			rowsView[2] = IPanelRows.ROW_SUBSPECIAL;
			rowsView[3] = IPanelRows.ROW_PROCEDURE;
			rowsView[4] = IPanelRows.ROW_STAFF;
		} else {
			rowsView[0] = IPanelRows.ROW_STAFF;
			rowsView[1] = IPanelRows.ROW_FACILITY;
			rowsView[2] = IPanelRows.ROW_SPECIALTY;
			rowsView[3] = IPanelRows.ROW_SUBSPECIAL;
			rowsView[4] = IPanelRows.ROW_PROCEDURE;
		}
		createPanel();
		programmaticChange = false;
		altered = true;
	}

	@Override
	boolean close() {
		altered = false;
		super.close();
		if (chartGroup != null) {
			chartGroup.close();
		}
		if (chartCoder1 != null) {
			chartCoder1.close();
		}
		if (chartCoder2 != null) {
			chartCoder2.close();
		}
		if (chartCoder3 != null) {
			chartCoder3.close();
		}
		if (chartCoder4 != null) {
			chartCoder4.close();
		}
		if (chartCoder5 != null) {
			chartCoder5.close();
		}
		return true;
	}

	private void createPanel() {
		OWorknode root = new OWorknode("Total");
		ModelWorkload model = new ModelWorkload(root);
		tree = new ITreeTable(pj, model);
		tree.getTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setCharts(e.getNewLeadSelectionPath());
					}
				});
			}
		});
		JScrollPane scrollTree = IGUI.createJScrollPane(tree);
		scrollTree.setMinimumSize(new Dimension(1200, 400));
		Dimension dim = new Dimension(400, 200);
		chartGroup = new IChartPie(dim);
		chartCoder1 = new IChartPie(dim);
		chartCoder2 = new IChartPie(dim);
		chartCoder3 = new IChartPie(dim);
		chartCoder4 = new IChartPie(dim);
		chartCoder5 = new IChartPie(dim);
		JPanel pnlTop = new JPanel();
		pnlTop.setMinimumSize(new Dimension(1300, 200));
		pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.X_AXIS));
		pnlTop.setOpaque(true);
		pnlTop.add(chartGroup);
		pnlTop.add(chartCoder1);
		pnlTop.add(chartCoder2);
		JPanel pnlMiddle = new JPanel();
		pnlMiddle.setMinimumSize(new Dimension(1300, 200));
		pnlMiddle.setLayout(new BoxLayout(pnlMiddle, BoxLayout.X_AXIS));
		pnlMiddle.setOpaque(true);
		pnlMiddle.add(chartCoder3);
		pnlMiddle.add(chartCoder4);
		pnlMiddle.add(chartCoder5);
		JSplitPane splitTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitTop.setTopComponent(pnlTop);
		splitTop.setBottomComponent(pnlMiddle);
		splitTop.setOneTouchExpandable(true);
		splitTop.setDividerLocation(250);
		splitTop.setMinimumSize(new Dimension(1300, 450));
		JSplitPane splitBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitBottom.setTopComponent(splitTop);
		splitBottom.setBottomComponent(scrollTree);
		splitBottom.setOneTouchExpandable(true);
		splitBottom.setDividerLocation(500);
		splitBottom.setMinimumSize(new Dimension(1300, 900));
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
		add(new IToolBar(this, calStart, calEnd, calMin, calMax, rowsView), BorderLayout.NORTH);
		add(splitBottom, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("workload.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 5, 1.5f, 1.5f, 1.5f, 1.5f, 1, 1, 1, 1, 1 };
		String str = "Workload " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATE)
			+ " - " + pj.dates.formatter(timeTo, LDates.FORMAT_DATE);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER, 36, 18, 18, 18);
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
			OWorknode root = (OWorknode) tree.getTreeTableModel().getRoot();
			pdfRow(root, table, paragraph, cell, fonts.get("Font10n"), "");
			document.add(table);
			document.close();
		} catch (DocumentException e) {
			pj.log(LConstants.ERROR_IO, getName(), e);
		} catch (FileNotFoundException e) {
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		}
	}

	private void pdfRow(OWorknode parent, PdfPTable table, Paragraph paragraph, PdfPCell cell, Font font, String name) {
		for (int col = 0; col < columns.length; col++) {
			paragraph = new Paragraph();
			paragraph.setFont(font);
			cell = new PdfPCell();
			switch (col) {
			case DATA_GROUPS:
				if (name.length() == 0) {
					paragraph.add(new Chunk(parent.name));
					name = "/";
				} else if (name.equals("/")) {
					paragraph.add(new Chunk(parent.name));
					name = parent.name;
				} else {
					name += "/" + parent.name;
					paragraph.add(new Chunk(name));
				}
				paragraph.setAlignment(Element.ALIGN_LEFT);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				break;
			case DATA_CASES:
				paragraph.add(new Chunk(pj.numbers.formatNumber(parent.noCases)));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_SPECS:
				paragraph.add(new Chunk(pj.numbers.formatNumber(parent.noSpecs)));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_BLOCKS:
				paragraph.add(new Chunk(pj.numbers.formatNumber(parent.noBlocks)));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_SLIDES:
				paragraph.add(new Chunk(pj.numbers.formatNumber(parent.noSlides)));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_VALUE1:
				paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte1)));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_VALUE2:
				paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte2)));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_VALUE3:
				paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte3)));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_VALUE4:
				paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte4)));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			default:
				paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte5)));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			}
			cell.addElement(paragraph);
			table.addCell(cell);

		}
		if (parent.children != null) {
			for (int i = 0; i < parent.children.length; i++) {
				OWorknode child = (OWorknode) parent.children[i];
				pdfRow(child, table, paragraph, cell, font, name);
			}
		}
	}

	private void setCharts(TreePath newPath) {
		if (newPath == null)
			return;
		if (treePath == null || !treePath.equals(newPath)) {
			int count = 1;
			double[] yCases = new double[count];
			double[] yCoder1 = new double[count];
			double[] yCoder2 = new double[count];
			double[] yCoder3 = new double[count];
			double[] yCoder4 = new double[count];
			double[] yCoder5 = new double[count];
			String[] xTitles = new String[count];
			treePath = newPath;
			OWorknode node = (OWorknode) treePath.getPathComponent(treePath.getPathCount() - 1);
			if (node != null && node.children != null && node.children.length > 0) {
				count = node.children.length;
				xTitles = new String[count];
				yCases = new double[count];
				yCoder1 = new double[count];
				yCoder2 = new double[count];
				yCoder3 = new double[count];
				yCoder4 = new double[count];
				yCoder5 = new double[count];
				for (int i = 0; i < count; i++) {
					OWorknode leaf = (OWorknode) node.children[i];
					if (leaf.name.length() > 4) {
						xTitles[i] = leaf.name.substring(0, 4);
					} else {
						xTitles[i] = leaf.name;
					}
					yCases[i] = leaf.noCases;
					yCoder1[i] = leaf.fte1;
					yCoder2[i] = leaf.fte2;
					yCoder3[i] = leaf.fte3;
					yCoder4[i] = leaf.fte4;
					yCoder5[i] = leaf.fte5;
				}
			}
			chartGroup.setChart(xTitles, yCases, "Cases", IChartPie.COLOR_DEF);
			chartCoder1.setChart(xTitles, yCoder1, columns[5], IChartPie.COLOR_DEF);
			chartCoder2.setChart(xTitles, yCoder2, columns[6], IChartPie.COLOR_DEF);
			chartCoder3.setChart(xTitles, yCoder3, columns[7], IChartPie.COLOR_DEF);
			chartCoder4.setChart(xTitles, yCoder4, columns[8], IChartPie.COLOR_DEF);
			chartCoder5.setChart(xTitles, yCoder5, columns[9], IChartPie.COLOR_DEF);
		}
	}

	@Override
	void setFilter(int[] values) {
		for (int i = 0; i < rowsView.length; i++) {
			rowsView[i] = (byte) values[i];
		}
		altered = true;
	}

	@Override
	void setFilter(short id, short value) {
		// Go button
		if (altered && timeTo > timeFrom) {
			pj.setBusy(true);
			WorkerData worker = new WorkerData();
			worker.execute();
			altered = false;
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
		String fileName = pj.getFileXls("workload.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Workload");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Workload " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATE)
				+ " - " + pj.dates.formatter(timeTo, LDates.FORMAT_DATE));
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
				case DATA_GROUPS:
					sheet.setColumnWidth(col, 30 * 256); // 30 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case DATA_CASES:
				case DATA_SPECS:
				case DATA_BLOCKS:
				case DATA_SLIDES:
					sheet.setColumnWidth(col, 10 * 256); // 10 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				default:
					sheet.setColumnWidth(col, 10 * 256); // 10 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_float"));
				}
			}
			// data rows
			OWorknode root = (OWorknode) tree.getTreeTableModel().getRoot();
			int rownum = 2;
			xlsRow(root, sheet, xlsCell, "", rownum);
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

	private int xlsRow(OWorknode parent, Sheet sheet, Cell xlsCell, String name, int rownum) {
		Row xlsRow = sheet.createRow(rownum++);
		for (int col = 0; col < columns.length; col++) {
			xlsCell = xlsRow.createCell(col);
			switch (col) {
			case DATA_GROUPS:
				if (name.length() == 0) {
					xlsCell.setCellValue(parent.name);
					name = "/";
				} else if (name.equals("/")) {
					xlsCell.setCellValue(parent.name);
					name = parent.name;
				} else {
					name += "/" + parent.name;
					xlsCell.setCellValue(name);
				}
				break;
			case DATA_CASES:
				xlsCell.setCellValue(parent.noCases);
				break;
			case DATA_SPECS:
				xlsCell.setCellValue(parent.noSpecs);
				break;
			case DATA_BLOCKS:
				xlsCell.setCellValue(parent.noBlocks);
				break;
			case DATA_SLIDES:
				xlsCell.setCellValue(parent.noSlides);
				break;
			case DATA_VALUE1:
				xlsCell.setCellValue(parent.fte1);
				break;
			case DATA_VALUE2:
				xlsCell.setCellValue(parent.fte2);
				break;
			case DATA_VALUE3:
				xlsCell.setCellValue(parent.fte3);
				break;
			case DATA_VALUE4:
				xlsCell.setCellValue(parent.fte4);
				break;
			default:
				xlsCell.setCellValue(parent.fte5);
			}
		}
		if (parent.children != null) {
			for (int i = 0; i < parent.children.length; i++) {
				OWorknode child = (OWorknode) parent.children[i];
				rownum = xlsRow(child, sheet, xlsCell, name, rownum);
			}
		}
		return rownum;
	}

	class ModelWorkload extends ITreeTableModel implements ITreeModel {

		public ModelWorkload(Object nodeRoot) {
			super(nodeRoot);
		}

		@Override
		public Object getChild(Object node, int elem) {
			return ((OWorknode) node).children[elem];
		}

		@Override
		public int getChildCount(Object node) {
			Object[] children = getChildren(node);
			return (children == null) ? 0 : children.length;
		}

		protected Object[] getChildren(Object node) {
			return ((OWorknode) node).children;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_GROUPS:
				return ITreeModel.class;
			case DATA_CASES:
			case DATA_SPECS:
			case DATA_BLOCKS:
			case DATA_SLIDES:
				return Integer.class;
			default:
				return Double.class;
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
		public Object getValueAt(Object node, int col) {
			OWorknode data = (OWorknode) node;
			switch (col) {
			case DATA_GROUPS:
				return data.name;
			case DATA_CASES:
				return data.noCases;
			case DATA_SPECS:
				return data.noSpecs;
			case DATA_BLOCKS:
				return data.noBlocks;
			case DATA_SLIDES:
				return data.noSlides;
			case DATA_VALUE1:
				return data.fte1;
			case DATA_VALUE2:
				return data.fte2;
			case DATA_VALUE3:
				return data.fte3;
			case DATA_VALUE4:
				return data.fte4;
			case DATA_VALUE5:
				return data.fte5;
			default:
				return null;
			}
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {
		private int noRows = 0;
		private ArrayList<OWorkload> rows = new ArrayList<OWorkload>();

		@Override
		protected Void doInBackground() throws Exception {
			setName("WorkerData");
			getData();
			structureData();
			return null;
		}

		@Override
		public void done() {
			// Display results
			OWorknode root = (OWorknode) tree.getTreeTableModel().getRoot();
			ModelWorkload model = new ModelWorkload(root);
			tree.setTreeTableModel(model);
			setCharts(tree.getTree().getPathForRow(0));
			pj.statusBar.setMessage("Workload " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATELONG) + " - "
					+ pj.dates.formatter(timeTo, LDates.FORMAT_DATELONG));
			pj.setBusy(false);
		}

		private void getData() {
			boolean exists = false;
			OWorkload row = new OWorkload();
			ResultSet rst = null;
			try {
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_CSE_SL_SUM));
				while (rst.next()) {
					row = new OWorkload();
					row.spyID = rst.getByte("syid");
					row.subID = rst.getByte("sbid");
					row.proID = rst.getByte("poid");
					row.facID = rst.getShort("faid");
					row.prsID = rst.getShort("fnid");
					row.noCases = rst.getInt("caca");
					row.noSpecs = rst.getInt("casp");
					row.noBlocks = rst.getInt("cabl");
					row.noSlides = rst.getInt("casl");
					row.fte1 = rst.getDouble("cav1");
					row.fte2 = rst.getDouble("cav2");
					row.fte3 = rst.getDouble("cav3");
					row.fte4 = rst.getDouble("cav4");
					row.fte5 = rst.getDouble("cav5");
					row.facility = rst.getString("fanm");
					row.specialty = rst.getString("synm");
					row.subspecial = rst.getString("sbnm");
					row.procedure = rst.getString("ponm");
					if (pj.userAccess[LConstants.ACCESS_NAMES] || row.prsID == pj.userID) {
						// Hide names except the current user
						row.staff = rst.getString("fnnm");
					}
					rows.add(row);
				}
				rst.close();
				// Frozen Section
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_FRZ_SL_SUM));
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).facID == rst.getShort("faid") && rows.get(i).spyID == rst.getByte("syid")
								&& rows.get(i).subID == rst.getByte("sbid") && rows.get(i).proID == rst.getByte("poid")
								&& rows.get(i).prsID == rst.getShort("prid")) {
							row = rows.get(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						row = new OWorkload();
						row.spyID = rst.getByte("syid");
						row.subID = rst.getByte("sbid");
						row.proID = rst.getByte("poid");
						row.facID = rst.getShort("faid");
						row.prsID = rst.getShort("prid");
						row.facility = rst.getString("fanm");
						row.specialty = rst.getString("synm");
						row.subspecial = rst.getString("sbnm");
						row.procedure = rst.getString("ponm");
						if (pj.userAccess[LConstants.ACCESS_NAMES] || row.prsID == pj.userID) {
							row.staff = rst.getString("prnm");
						}
						rows.add(row);
					}
					row.fte1 += rst.getDouble("frv1");
					row.fte2 += rst.getDouble("frv2");
					row.fte3 += rst.getDouble("frv3");
					row.fte4 += rst.getDouble("frv4");
					row.fte5 += rst.getDouble("frv5");
				}
				rst.close();
				// Additional
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ADD_SL_SUM));
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).facID == rst.getShort("faid") && rows.get(i).spyID == rst.getByte("syid")
								&& rows.get(i).subID == rst.getByte("sbid") && rows.get(i).proID == rst.getByte("poid")
								&& rows.get(i).prsID == rst.getShort("prid")) {
							row = rows.get(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						row = new OWorkload();
						row.spyID = rst.getByte("syid");
						row.subID = rst.getByte("sbid");
						row.proID = rst.getByte("poid");
						row.facID = rst.getShort("faid");
						row.prsID = rst.getShort("prid");
						row.facility = rst.getString("fanm");
						row.specialty = rst.getString("synm");
						row.subspecial = rst.getString("sbnm");
						row.procedure = rst.getString("ponm");
						if (pj.userAccess[LConstants.ACCESS_NAMES] || row.prsID == pj.userID) {
							row.staff = rst.getString("prnm");
						}
						rows.add(row);
					}
					row.fte1 += rst.getDouble("adv1");
					row.fte2 += rst.getDouble("adv2");
					row.fte3 += rst.getDouble("adv3");
					row.fte4 += rst.getDouble("adv4");
					row.fte5 += rst.getDouble("adv5");
				}
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
		}

		private void setModel(OWorklist root) {
			OWorknode node0 = (OWorknode) tree.getTreeTableModel().getRoot();
			OWorknode node1 = new OWorknode("node1");
			OWorknode node2 = new OWorknode("node2");
			OWorknode node3 = new OWorknode("node3");
			OWorknode node4 = new OWorknode("node4");
			OWorknode node5 = new OWorknode("node5");
			OWorklist data1 = new OWorklist();
			OWorklist data2 = new OWorklist();
			OWorklist data3 = new OWorklist();
			OWorklist data4 = new OWorklist();
			OWorklist data5 = new OWorklist();
			node0.noCases = root.noCases;
			node0.noSpecs = root.noSpecs;
			node0.noBlocks = root.noBlocks;
			node0.noSlides = root.noSlides;
			node0.fte1 = root.fte1;
			node0.fte2 = root.fte2;
			node0.fte3 = root.fte3;
			node0.fte4 = root.fte4;
			node0.fte5 = root.fte5;
			node0.children = new OWorknode[root.children.size()];
			for (int i = 0; i < root.children.size(); i++) {
				data1 = root.children.get(i);
				node1 = new OWorknode(data1.name);
				node1.noCases = data1.noCases;
				node1.noSpecs = data1.noSpecs;
				node1.noBlocks = data1.noBlocks;
				node1.noSlides = data1.noSlides;
				node1.fte1 = data1.fte1;
				node1.fte2 = data1.fte2;
				node1.fte3 = data1.fte3;
				node1.fte4 = data1.fte4;
				node1.fte5 = data1.fte5;
				node1.children = new OWorknode[data1.children.size()];
				for (int j = 0; j < data1.children.size(); j++) {
					data2 = data1.children.get(j);
					node2 = new OWorknode(data2.name);
					node2.noCases = data2.noCases;
					node2.noSpecs = data2.noSpecs;
					node2.noBlocks = data2.noBlocks;
					node2.noSlides = data2.noSlides;
					node2.fte1 = data2.fte1;
					node2.fte2 = data2.fte2;
					node2.fte3 = data2.fte3;
					node2.fte4 = data2.fte4;
					node2.fte5 = data2.fte5;
					node2.children = new OWorknode[data2.children.size()];
					for (int k = 0; k < data2.children.size(); k++) {
						data3 = data2.children.get(k);
						node3 = new OWorknode(data3.name);
						node3.noCases = data3.noCases;
						node3.noSpecs = data3.noSpecs;
						node3.noBlocks = data3.noBlocks;
						node3.noSlides = data3.noSlides;
						node3.fte1 = data3.fte1;
						node3.fte2 = data3.fte2;
						node3.fte3 = data3.fte3;
						node3.fte4 = data3.fte4;
						node3.fte5 = data3.fte5;
						node3.children = new OWorknode[data3.children.size()];
						for (int l = 0; l < data3.children.size(); l++) {
							data4 = data3.children.get(l);
							node4 = new OWorknode(data4.name);
							node4.noCases = data4.noCases;
							node4.noSpecs = data4.noSpecs;
							node4.noBlocks = data4.noBlocks;
							node4.noSlides = data4.noSlides;
							node4.fte1 = data4.fte1;
							node4.fte2 = data4.fte2;
							node4.fte3 = data4.fte3;
							node4.fte4 = data4.fte4;
							node4.fte5 = data4.fte5;
							node4.children = new OWorknode[data4.children.size()];
							for (int m = 0; m < data4.children.size(); m++) {
								data5 = data4.children.get(m);
								node5 = new OWorknode(data5.name);
								node5.noCases = data5.noCases;
								node5.noSpecs = data5.noSpecs;
								node5.noBlocks = data5.noBlocks;
								node5.noSlides = data5.noSlides;
								node5.fte1 = data5.fte1;
								node5.fte2 = data5.fte2;
								node5.fte3 = data5.fte3;
								node5.fte4 = data5.fte4;
								node5.fte5 = data5.fte5;
								node4.children[m] = node5;
							}
							node3.children[l] = node4;
						}
						node2.children[k] = node3;
					}
					node1.children[j] = node2;
				}
				node0.children[i] = node1;
			}
		}

		private void setTotals(OWorklist master, double fte1, double fte2, double fte3, double fte4, double fte5) {
			OWorklist child = new OWorklist();
			for (int i = master.children.size() - 1; i >= 0; i--) {
				child = master.children.get(i);
				if (child.id < 0) {
					// filtered out
					master.children.remove(i);
					continue;
				}
				child.fte1 = child.fte1 / fte1;
				child.fte2 = child.fte2 / fte2;
				child.fte3 = child.fte3 / fte3;
				child.fte4 = child.fte4 / fte4;
				child.fte5 = child.fte5 / fte5;
				if (child.children.size() > 0) {
					setTotals(child, fte1, fte2, fte3, fte4, fte5);
				}
			}
		}

		private void sortChildren(OWorklist master) {
			Collections.sort(master.children, new Comparator<OWorklist>() {
				@Override
				public int compare(OWorklist o1, OWorklist o2) {
					return (o1.noCases > o2.noCases ? -1
							: (o1.noCases < o2.noCases ? 1 
							: (o1.noSpecs > o2.noSpecs ? -1
							: (o1.noSpecs < o2.noSpecs ? 1
									: 0))));
				}
			});
			int nameID = 0;
			OWorklist child = new OWorklist();
			for (int i = 0; i < master.children.size(); i++) {
				child = master.children.get(i);
				if (!pj.userAccess[LConstants.ACCESS_NAMES]) {
					if (child.name.trim().length() == 0) {
						nameID = 1;
						noRows++;
						child.name = "P" + noRows;
					} else if (nameID == 1) {
						noRows++;
					}
				}
				if (child.children.size() > 1) {
					sortChildren(child);
				}
			}
		}

		private void structureData() {
			try {
				short id = 0;
				short ids[] = new short[rowsView.length];
				int rowNos[] = new int[rowsView.length];
				int size = rows.size();
				int noDays = pj.dates.getNoDays(timeFrom, timeTo);
				double fte1 = pj.setup.getShort(LSetup.VAR_CODER1_FTE) * noDays / 365.0;
				double fte2 = pj.setup.getShort(LSetup.VAR_CODER2_FTE) * noDays / 365.0;
				double fte3 = pj.setup.getShort(LSetup.VAR_CODER3_FTE) * noDays / 365.0;
				double fte4 = pj.setup.getShort(LSetup.VAR_CODER4_FTE) * noDays / 365.0;
				double fte5 = pj.setup.getInt(LSetup.VAR_V5_FTE) * noDays / 365.0;
				String name = "";
				OWorklist data0 = new OWorklist();
				OWorklist data1 = new OWorklist();
				OWorklist data2 = new OWorklist();
				OWorklist data3 = new OWorklist();
				OWorklist data4 = new OWorklist();
				OWorklist data5 = new OWorklist();
				OWorkload row = new OWorkload();
				for (int i = 0; i < rowsView.length; i++) {
					ids[i] = -1;
				}
				if (fte1 == 0) {
					fte1 = 1.0;
				}
				if (fte2 == 0) {
					fte2 = 1.0;
				}
				if (fte3 == 0) {
					fte3 = 1.0;
				}
				if (fte4 == 0) {
					fte4 = 1.0;
				}
				if (fte5 == 0) {
					fte5 = 1.0;
				}
				for (int x = 0; x < size; x++) {
					row = rows.get(x);
					// Match 1st node
					switch (rowsView[0]) {
					case IPanelRows.ROW_FACILITY:
						id = row.facID;
						name = row.facility;
						break;
					case IPanelRows.ROW_SPECIALTY:
						id = row.spyID;
						name = row.specialty;
						break;
					case IPanelRows.ROW_SUBSPECIAL:
						id = row.subID;
						name = row.subspecial;
						break;
					case IPanelRows.ROW_PROCEDURE:
						id = row.proID;
						name = row.procedure;
						break;
					case IPanelRows.ROW_STAFF:
						id = row.prsID;
						name = row.staff;
						break;
					default:
						id = -2;
					}
					if (ids[0] != id) {
						ids[0] = id;
						rowNos[0] = -1;
						for (int i = 1; i < rowsView.length; i++) {
							ids[i] = -1;
							rowNos[i] = -1;
						}
						for (int j = 0; j < data0.children.size(); j++) {
							data1 = data0.children.get(j);
							if (data1.id == ids[0]) {
								rowNos[0] = j;
								break;
							}
						}
						if (rowNos[0] < 0) {
							rowNos[0] = data0.children.size();
							data1 = new OWorklist();
							data1.id = ids[0];
							data1.name = name;
							data0.children.add(data1);
						}
					}
					// Match 2nd node
					switch (rowsView[1]) {
					case IPanelRows.ROW_FACILITY:
						id = row.facID;
						name = row.facility;
						break;
					case IPanelRows.ROW_SPECIALTY:
						id = row.spyID;
						name = row.specialty;
						break;
					case IPanelRows.ROW_SUBSPECIAL:
						id = row.subID;
						name = row.subspecial;
						break;
					case IPanelRows.ROW_PROCEDURE:
						id = row.proID;
						name = row.procedure;
						break;
					case IPanelRows.ROW_STAFF:
						id = row.prsID;
						name = row.staff;
						break;
					default:
						id = -2;
					}
					if (ids[1] != id) {
						ids[1] = id;
						rowNos[1] = -1;
						for (int i = 2; i < rowsView.length; i++) {
							ids[i] = -1;
							rowNos[i] = -1;
						}
						for (int j = 0; j < data1.children.size(); j++) {
							data2 = data1.children.get(j);
							if (data2.id == ids[1]) {
								rowNos[1] = j;
								break;
							}
						}
						if (rowNos[1] < 0) {
							rowNos[1] = data1.children.size();
							data2 = new OWorklist();
							data2.id = ids[1];
							data2.name = name;
							data1.children.add(data2);
						}
					}
					// Match 3rd node
					switch (rowsView[2]) {
					case IPanelRows.ROW_FACILITY:
						id = row.facID;
						name = row.facility;
						break;
					case IPanelRows.ROW_SPECIALTY:
						id = row.spyID;
						name = row.specialty;
						break;
					case IPanelRows.ROW_SUBSPECIAL:
						id = row.subID;
						name = row.subspecial;
						break;
					case IPanelRows.ROW_PROCEDURE:
						id = row.proID;
						name = row.procedure;
						break;
					case IPanelRows.ROW_STAFF:
						id = row.prsID;
						name = row.staff;
						break;
					default:
						id = -2;
					}
					if (ids[2] != id) {
						ids[2] = id;
						rowNos[2] = -1;
						for (int i = 3; i < rowsView.length; i++) {
							ids[i] = -1;
							rowNos[i] = -1;
						}
						for (int j = 0; j < data2.children.size(); j++) {
							data3 = data2.children.get(j);
							if (data3.id == ids[2]) {
								rowNos[2] = j;
								break;
							}
						}
						if (rowNos[2] < 0) {
							rowNos[2] = data2.children.size();
							data3 = new OWorklist();
							data3.id = ids[2];
							data3.name = name;
							data2.children.add(data3);
						}
					}
					// Match 4th node
					switch (rowsView[3]) {
					case IPanelRows.ROW_FACILITY:
						id = row.facID;
						name = row.facility;
						break;
					case IPanelRows.ROW_SPECIALTY:
						id = row.spyID;
						name = row.specialty;
						break;
					case IPanelRows.ROW_SUBSPECIAL:
						id = row.subID;
						name = row.subspecial;
						break;
					case IPanelRows.ROW_PROCEDURE:
						id = row.proID;
						name = row.procedure;
						break;
					case IPanelRows.ROW_STAFF:
						id = row.prsID;
						name = row.staff;
						break;
					default:
						id = -2;
					}
					if (ids[3] != id) {
						ids[3] = id;
						rowNos[3] = -1;
						for (int i = 4; i < rowsView.length; i++) {
							ids[i] = -1;
							rowNos[i] = -1;
						}
						for (int j = 0; j < data3.children.size(); j++) {
							data4 = data3.children.get(j);
							if (data4.id == ids[3]) {
								rowNos[3] = j;
								break;
							}
						}
						if (rowNos[3] < 0) {
							rowNos[3] = data3.children.size();
							data4 = new OWorklist();
							data4.id = ids[3];
							data4.name = name;
							data3.children.add(data4);
						}
					}
					// Match 5th node
					switch (rowsView[4]) {
					case IPanelRows.ROW_FACILITY:
						id = row.facID;
						name = row.facility;
						break;
					case IPanelRows.ROW_SPECIALTY:
						id = row.spyID;
						name = row.specialty;
						break;
					case IPanelRows.ROW_SUBSPECIAL:
						id = row.subID;
						name = row.subspecial;
						break;
					case IPanelRows.ROW_PROCEDURE:
						id = row.proID;
						name = row.procedure;
						break;
					case IPanelRows.ROW_STAFF:
						id = row.prsID;
						name = row.staff;
						break;
					default:
						id = -2;
					}
					if (ids[4] != id) {
						ids[4] = id;
						rowNos[4] = -1;
						for (int j = 0; j < data4.children.size(); j++) {
							data5 = data4.children.get(j);
							if (data5.id == ids[4]) {
								rowNos[4] = j;
								break;
							}
						}
						if (rowNos[4] < 0) {
							rowNos[4] = data4.children.size();
							data5 = new OWorklist();
							data5.id = ids[4];
							data5.name = name;
							data4.children.add(data5);
						}
					}
					data5.noCases += row.noCases;
					data4.noCases += row.noCases;
					data3.noCases += row.noCases;
					data2.noCases += row.noCases;
					data1.noCases += row.noCases;
					data0.noCases += row.noCases;
					data5.noSpecs += row.noSpecs;
					data4.noSpecs += row.noSpecs;
					data3.noSpecs += row.noSpecs;
					data2.noSpecs += row.noSpecs;
					data1.noSpecs += row.noSpecs;
					data0.noSpecs += row.noSpecs;
					data5.noBlocks += row.noBlocks;
					data4.noBlocks += row.noBlocks;
					data3.noBlocks += row.noBlocks;
					data2.noBlocks += row.noBlocks;
					data1.noBlocks += row.noBlocks;
					data0.noBlocks += row.noBlocks;
					data5.noSlides += row.noSlides;
					data4.noSlides += row.noSlides;
					data3.noSlides += row.noSlides;
					data2.noSlides += row.noSlides;
					data1.noSlides += row.noSlides;
					data0.noSlides += row.noSlides;
					data5.fte1 += row.fte1;
					data4.fte1 += row.fte1;
					data3.fte1 += row.fte1;
					data2.fte1 += row.fte1;
					data1.fte1 += row.fte1;
					data0.fte1 += row.fte1;
					data5.fte2 += row.fte2;
					data4.fte2 += row.fte2;
					data3.fte2 += row.fte2;
					data2.fte2 += row.fte2;
					data1.fte2 += row.fte2;
					data0.fte2 += row.fte2;
					data5.fte3 += row.fte3;
					data4.fte3 += row.fte3;
					data3.fte3 += row.fte3;
					data2.fte3 += row.fte3;
					data1.fte3 += row.fte3;
					data0.fte3 += row.fte3;
					data5.fte4 += row.fte4;
					data4.fte4 += row.fte4;
					data3.fte4 += row.fte4;
					data2.fte4 += row.fte4;
					data1.fte4 += row.fte4;
					data0.fte4 += row.fte4;
					data5.fte5 += row.fte5;
					data4.fte5 += row.fte5;
					data3.fte5 += row.fte5;
					data2.fte5 += row.fte5;
					data1.fte5 += row.fte5;
					data0.fte5 += row.fte5;
				}
				try {
					Thread.sleep(LConstants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				data0.fte1 = data0.fte1 / fte1;
				data0.fte2 = data0.fte2 / fte2;
				data0.fte3 = data0.fte3 / fte3;
				data0.fte4 = data0.fte4 / fte4;
				data0.fte5 = data0.fte5 / fte5;
				try {
					Thread.sleep(LConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
				setTotals(data0, fte1, fte2, fte3, fte4, fte5);
				try {
					Thread.sleep(LConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
				if (data0.children.size() > 1) {
					sortChildren(data0);
				}
				try {
					Thread.sleep(LConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
				setModel(data0);
				try {
					Thread.sleep(LConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			} catch (Exception e) {
				pj.log(LConstants.ERROR_UNEXPECTED, getName(), e);
			}
		}
	}
}