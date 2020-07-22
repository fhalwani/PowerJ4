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
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
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

class NForecast extends NBase {
	private final byte DATA_YEARS = 0;
	private final byte DATA_SPECS = 1;
	private final byte DATA_BLCKS = 2;
	private final byte DATA_SLIDE = 3;
	private final byte DATA_VALU1 = 4;
	private final byte DATA_VALU2 = 5;
	private final byte DATA_VALU3 = 6;
	private final byte DATA_VALU4 = 7;
	private final byte DATA_VALU5 = 8;
	private byte[] rowsView = new byte[5];
	private long timeFrom = 0;
	private long timeTo = 0;
	private String[] columns = {"YEAR", "SPECS", "BLKS", "SLDS", "", "", "", "", ""};
	private ArrayList<String> tableRows = new ArrayList<String>();
	private JTree tree;
	private ModelData modelData;
	private ITable table;
	private OAnnualNode dataNode = new OAnnualNode();
	private IChart2Lines chartSpecs, chartCoder1, chartCoder2, chartCoder3, chartCoder4, chartCoder5;

	public NForecast(AClient parent) {
		super(parent);
		setName("Forecast");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_FORECAST);
		columns[4] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		columns[5] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		columns[6] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		columns[7] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		columns[8] = pj.setup.getString(LSetup.VAR_V5_NAME);
		rowsView[0] = IPanelRows.ROW_FACILITY;
		rowsView[1] = IPanelRows.ROW_SPECIALTY;
		rowsView[2] = IPanelRows.ROW_SUBSPECIAL;
		rowsView[3] = IPanelRows.ROW_PROCEDURE;
		rowsView[4] = IPanelRows.ROW_SPECIMEN;
		createPanel();
		programmaticChange = false;
		altered = true;
	}

	@Override
	boolean close() {
		altered = false;
		super.close();
		if (chartSpecs != null) {
			chartSpecs.close();
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
		ModelGroup treeModel = new ModelGroup(dataNode);
		tree = new JTree(treeModel);
		tree.setFont(LConstants.APP_FONT);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setSelection(e.getNewLeadSelectionPath());
					}
				});
			}
		});
		JScrollPane scrollTree = IGUI.createJScrollPane(tree);
		scrollTree.setMinimumSize(new Dimension(200, 400));
		modelData = new ModelData();
		table = new ITable(pj, modelData);
		table.getColumnModel().getColumn(0).setMinWidth(120);
		JScrollPane scrollTable = IGUI.createJScrollPane(table);
		scrollTable.setMinimumSize(new Dimension(1000, 400));
		Dimension dim = new Dimension(400, 200);
		chartSpecs = new IChart2Lines(dim);
		chartCoder1 = new IChart2Lines(dim);
		chartCoder2 = new IChart2Lines(dim);
		chartCoder3 = new IChart2Lines(dim);
		chartCoder4 = new IChart2Lines(dim);
		chartCoder5 = new IChart2Lines(dim);
		JPanel pnlTop = new JPanel();
		pnlTop.setMinimumSize(new Dimension(1300, 200));
		pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.X_AXIS));
		pnlTop.setOpaque(true);
		pnlTop.add(chartSpecs);
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
		JSplitPane splitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitBottom.setTopComponent(scrollTree);
		splitBottom.setBottomComponent(scrollTable);
		splitBottom.setOneTouchExpandable(true);
		splitBottom.setDividerLocation(250);
		splitBottom.setMinimumSize(new Dimension(1300, 450));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(splitTop);
		splitAll.setBottomComponent(splitBottom);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(500);
		splitAll.setMinimumSize(new Dimension(1300, 900));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this, null, null, null, null, rowsView), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("forecast.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 4, 1, 1.5f, 1.5f, 1.5f, 1, 1, 1, 1, 1 };
		String str = "Forecast " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATE)
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
			for (int col = 0; col < widths.length; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				if (col == 0) {
					paragraph.add(new Chunk("Name"));
				} else {
					paragraph.add(new Chunk(columns[col -1]));
				}
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data rows
			ModelGroup model = (ModelGroup) tree.getModel();
			OAnnualNode root = (OAnnualNode) model.getRoot();
			pdfRow(root, table, paragraph, cell, fonts.get("Font10n"), "", widths.length);
			document.add(table);
			document.close();
		} catch (DocumentException e) {
			pj.log(LConstants.ERROR_IO, getName(), e);
		} catch (FileNotFoundException e) {
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		}
	}

	private void pdfRow(OAnnualNode parent, PdfPTable table, Paragraph paragraph, PdfPCell cell, Font font, String name, int width) {
		for (int row = 0; row < tableRows.size(); row++) {
			for (int col = 0; col < width; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(font);
				cell = new PdfPCell();
				switch (col) {
				case 0:
					if (row == 0) {
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
					} else {
						paragraph.add(new Chunk(""));
					}
					paragraph.setAlignment(Element.ALIGN_LEFT);
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					break;
				case 1:
					paragraph.add(new Chunk(tableRows.get(row)));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 2:
					paragraph.add(new Chunk(pj.numbers.formatNumber(parent.specs[row])));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 3:
					paragraph.add(new Chunk(pj.numbers.formatNumber(parent.blocks[row])));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 4:
					paragraph.add(new Chunk(pj.numbers.formatNumber(parent.slides[row])));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 5:
					paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte1[row])));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 6:
					paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte2[row])));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 7:
					paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte3[row])));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 8:
					paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte4[row])));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				default:
					paragraph.add(new Chunk(pj.numbers.formatDouble(2, parent.fte5[row])));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				}
				cell.addElement(paragraph);
				table.addCell(cell);
			}
		}
		if (parent.children != null) {
			for (int i = 0; i < parent.children.length; i++) {
				OAnnualNode child = (OAnnualNode) parent.children[i];
				pdfRow(child, table, paragraph, cell, font, name, width);
			}
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
		if (altered) {
			pj.setBusy(true);
			WorkerData worker = new WorkerData();
			worker.execute();
			altered = false;
		}
	}

	private void setSelection(TreePath treePath) {
		if (treePath != null) {
			dataNode = (OAnnualNode) (treePath.getLastPathComponent());
		} else {
			dataNode = null;
		}
		modelData.fireTableDataChanged();
		int yearID = 9999;
		int count1 = tableRows.size();
		int count2 = tableRows.size() + 3;
		double[][] ySpecs = new double[2][count2];
		double[][] yCoder1 = new double[2][count2];
		double[][] yCoder2 = new double[2][count2];
		double[][] yCoder3 = new double[2][count2];
		double[][] yCoder4 = new double[2][count2];
		double[][] yCoder5 = new double[2][count2];
		String[] xTitles = new String[count2];
		for (int i = 0; i < count2; i++) {
			if (i < count1) {
				ySpecs[0][i] = dataNode.specs[i];
				ySpecs[1][i] = dataNode.specsf[i];
				yCoder1[0][i] = dataNode.fte1[i];
				yCoder1[1][i] = dataNode.fte1f[i];
				yCoder2[0][i] = dataNode.fte2[i];
				yCoder2[1][i] = dataNode.fte2f[i];
				yCoder3[0][i] = dataNode.fte3[i];
				yCoder3[1][i] = dataNode.fte3f[i];
				yCoder4[0][i] = dataNode.fte4[i];
				yCoder4[1][i] = dataNode.fte4f[i];
				yCoder5[0][i] = dataNode.fte5[i];
				yCoder5[1][i] = dataNode.fte5f[i];
				xTitles[i] = tableRows.get(i);
				int n = pj.numbers.parseInt(xTitles[i]);
				if (yearID > n) {
					yearID = n;
				}
			} else {
				ySpecs[1][i] = dataNode.specsf[i];
				yCoder1[1][i] = dataNode.fte1f[i];
				yCoder2[1][i] = dataNode.fte2f[i];
				yCoder3[1][i] = dataNode.fte3f[i];
				yCoder4[1][i] = dataNode.fte4f[i];
				yCoder5[1][i] = dataNode.fte5f[i];
				xTitles[i] = Integer.toString(yearID + i);
			}
		}
		chartSpecs.setChart(xTitles, ySpecs, "Specimens");
		chartCoder1.setChart(xTitles, yCoder1, columns[4]);
		chartCoder2.setChart(xTitles, yCoder2, columns[5]);
		chartCoder3.setChart(xTitles, yCoder3, columns[6]);
		chartCoder4.setChart(xTitles, yCoder4, columns[7]);
		chartCoder5.setChart(xTitles, yCoder5, columns[8]);
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("forecast.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Forecast");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Forecast " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATE)
				+ " - " + pj.dates.formatter(timeTo, LDates.FORMAT_DATE));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col <= columns.length; col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellValue(columns[col]);
				xlsCell.setCellStyle(styles.get("header"));
				switch (col) {
				case 0:
					sheet.setColumnWidth(col, 30 * 256); // 30 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case 1:
				case 2:
				case 3:
				case 4:
					sheet.setColumnWidth(col, 10 * 256); // 10 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				default:
					sheet.setColumnWidth(col, 10 * 256); // 10 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_float"));
				}
			}
			// data rows
			ModelGroup model = (ModelGroup) tree.getModel();
			OAnnualNode root = (OAnnualNode) model.getRoot();
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

	private int xlsRow(OAnnualNode parent, Sheet sheet, Cell xlsCell, String name, int rownum) {
		for (int row = 0; row < tableRows.size(); row++) {
			Row xlsRow = sheet.createRow(rownum++);
			for (int col = 0; col < columns.length; col++) {
				xlsCell = xlsRow.createCell(col);
				switch (col) {
				case 0:
					if (row == 0) {
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
					}
					break;
				case 1:
					xlsCell.setCellValue(tableRows.get(row));
					break;
				case 2:
					xlsCell.setCellValue(parent.specs[row]);
					break;
				case 3:
					xlsCell.setCellValue(parent.blocks[row]);
					break;
				case 4:
					xlsCell.setCellValue(parent.slides[row]);
					break;
				case 5:
					xlsCell.setCellValue(parent.fte1[row]);
					break;
				case 6:
					xlsCell.setCellValue(parent.fte2[row]);
					break;
				case 7:
					xlsCell.setCellValue(parent.fte3[row]);
					break;
				case 8:
					xlsCell.setCellValue(parent.fte4[row]);
					break;
				default:
					xlsCell.setCellValue(parent.fte5[row]);
				}
			}
		}
		if (parent.children != null) {
			for (int i = 0; i < parent.children.length; i++) {
				OAnnualNode child = (OAnnualNode) parent.children[i];
				rownum = xlsRow(child, sheet, xlsCell, name, rownum);
			}
		}
		return rownum;
	}

	private class ModelData extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_YEARS:
				return String.class;
			case DATA_VALU1:
			case DATA_VALU2:
			case DATA_VALU3:
			case DATA_VALU4:
			case DATA_VALU5:
				return Double.class;
			default:
				return Integer.class;
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
			return tableRows.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (dataNode != null) {
				if (dataNode.specs != null) {
					if (dataNode.specs.length > row) {
						switch (col) {
						case DATA_YEARS:
							return tableRows.get(row);
						case DATA_SPECS:
							return dataNode.specs[row];
						case DATA_BLCKS:
							return dataNode.blocks[row];
						case DATA_SLIDE:
							return dataNode.slides[row];
						case DATA_VALU1:
							return dataNode.fte1[row];
						case DATA_VALU2:
							return dataNode.fte2[row];
						case DATA_VALU3:
							return dataNode.fte3[row];
						case DATA_VALU4:
							return dataNode.fte4[row];
						default:
							return dataNode.fte5[row];
						}
					}
				}
			}
			return Object.class;
		}
	}

	private class ModelGroup implements TreeModel {
		private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
		private OAnnualNode root;

		ModelGroup(OAnnualNode root) {
			this.root = root;
		}

		protected void fireTreeStructureChanged(OAnnualNode oldRoot) {
			TreeModelEvent event = new TreeModelEvent(this, new Object[] {oldRoot});
			for (TreeModelListener tml : treeModelListeners) {
				tml.treeStructureChanged(event);
			}
		}

		@Override
		public void addTreeModelListener(TreeModelListener tml) {
			treeModelListeners.add(tml);
		}

		@Override
		public Object getChild(Object parent, int index) {
			OAnnualNode data = (OAnnualNode) parent;
			return data.children[index];
		}

		@Override
		public int getChildCount(Object parent) {
			OAnnualNode data = (OAnnualNode) parent;
			if (data.children == null) {
				return 0;
			}
			return data.children.length;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			OAnnualNode data = (OAnnualNode) parent;
			for (int i = 0; i < data.children.length; i++) {
				if (data.children[i].equals(child)) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public Object getRoot() {
			return root;
		}

		@Override
		public boolean isLeaf(Object node) {
			OAnnualNode data = (OAnnualNode) node;
			return (data.children == null || data.children.length == 0);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener tml) {
			treeModelListeners.remove(tml);
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {
		private short yearMin = 9999;
		private short yearMax = 0;
		private ArrayList<OAnnual> rows = new ArrayList<OAnnual>();

		@Override
		protected Void doInBackground() throws Exception {
			setName("WorkerData");
			try {
				getData();
				structureData();
			} catch (Exception e) {
				pj.log(LConstants.ERROR_UNEXPECTED, getName(), e);
			}
			return null;
		}

		@Override
		public void done() {
			// Display tree results
			ModelGroup model = (ModelGroup) tree.getModel();
			dataNode = (OAnnualNode) model.getRoot();
			model.fireTreeStructureChanged(dataNode);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			pj.statusBar.setMessage("Forecast " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATELONG) + " - "
					+ pj.dates.formatter(timeTo, LDates.FORMAT_DATELONG));
			pj.setBusy(false);
		}

		private int[] extrapolate(int[] oYears, int[] nYears, int[] oCount) {
			PolynomialFunction func = setFunction(oYears, oCount);
			int[] nCount = new int[nYears.length];
			for (int i = 0; i < nYears.length; i++) {
				double xpol = func.value(nYears[i]);
				nCount[i] = (int) Math.round(xpol);
			}
			;
			return nCount;
		}

		private double[] extrapolate(int[] oYears, int[] nYears, double[] oCount) {
			PolynomialFunction func = setFunction(oYears, oCount);
			double[] nCount = new double[nYears.length];
			for (int i = 0; i < nYears.length; i++) {
				nCount[i] = func.value(nYears[i]);
			}
			return nCount;
		}

		private void extrapolate(int[] oYears, int[] nYears, OAnnualNode node) {
			node.specsf = extrapolate(oYears, nYears, node.specs);
			node.blocksf = extrapolate(oYears, nYears, node.blocks);
			node.slidesf = extrapolate(oYears, nYears, node.slides);
			node.fte1f = extrapolate(oYears, nYears, node.fte1);
			node.fte2f = extrapolate(oYears, nYears, node.fte2);
			node.fte3f = extrapolate(oYears, nYears, node.fte3);
			node.fte4f = extrapolate(oYears, nYears, node.fte4);
			node.fte5f = extrapolate(oYears, nYears, node.fte5);
			for (int i = 0; i < node.children.length; i++) {
				OAnnualNode child = (OAnnualNode) node.children[i];
				extrapolate(oYears, nYears, child);
			}
		}

		private void forecast(OAnnualNode root) {
			short noYears = (short) (yearMax - yearMin + 1);
			short yearID = yearMin;
			int[] oYears = new int[noYears];
			int[] nYears = new int[noYears + 3];
			for (int i = 0; i < noYears; i++) {
				oYears[i] = yearID;
				nYears[i] = yearID;
				yearID++;
			}
			for (int i = 0; i < 3; i++) {
				nYears[noYears + i] = yearID;
				yearID++;
			}
			extrapolate(oYears, nYears, root);
		}

		private void getData() {
			boolean exists = false;
			short yearID = 0;
			OAnnual row = new OAnnual();
			ResultSet rst = null;
			try {
				Calendar calStart = pj.dates.setMidnight(null);
				Calendar calEnd = pj.dates.setMidnight(null);
				calStart.setTimeInMillis(pj.setup.getLong(LSetup.VAR_MIN_WL_DATE));
				calEnd.set(Calendar.DAY_OF_YEAR, 1);
				timeFrom = calStart.getTimeInMillis();
				timeTo = calEnd.getTimeInMillis();
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SPG_SL_YER), 1, calStart.getTimeInMillis());
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SPG_SL_YER), 2, calEnd.getTimeInMillis());
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SPG_SL_YER));
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).facID == rst.getShort("faid") && rows.get(i).spyID == rst.getByte("syid")
								&& rows.get(i).subID == rst.getByte("sbid") && rows.get(i).proID == rst.getByte("poid")
								&& rows.get(i).spgID == rst.getShort("sgid")) {
							row = rows.get(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						row = new OAnnual();
						row.facID = rst.getShort("faid");
						row.spgID = rst.getShort("sgid");
						row.spyID = rst.getByte("syid");
						row.subID = rst.getByte("sbid");
						row.proID = rst.getByte("poid");
						row.facility = rst.getString("fanm");
						row.specialty = rst.getString("synm");
						row.subspecial = rst.getString("sbnm");
						row.procedure = rst.getString("ponm");
						row.specimen = rst.getString("sgdc");
						rows.add(row);
					}
					yearID = rst.getShort("yearid");
					row.specs.put(yearID, rst.getInt("qty"));
					row.blocks.put(yearID, rst.getInt("spbl"));
					row.slides.put(yearID, rst.getInt("spsl"));
					row.fte1.put(yearID, rst.getDouble("spv1"));
					row.fte2.put(yearID, rst.getDouble("spv2"));
					row.fte3.put(yearID, rst.getDouble("spv3"));
					row.fte4.put(yearID, rst.getDouble("spv4"));
					row.fte5.put(yearID, (double) rst.getInt("spv5"));
					if (yearMin > yearID) {
						yearMin = yearID;
					}
					if (yearMax < yearID) {
						yearMax = yearID;
					}
				}
				rst.close();
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_YER), 1, calStart.getTimeInMillis());
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_YER), 2, calEnd.getTimeInMillis());
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_FRZ_SL_YER));
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).facID == rst.getShort("faid") && rows.get(i).spyID == rst.getByte("syid")
								&& rows.get(i).subID == rst.getByte("sbid") && rows.get(i).proID == rst.getByte("poid")
								&& rows.get(i).spgID == rst.getShort("sgid")) {
							row = rows.get(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						row = new OAnnual();
						row.facID = rst.getShort("faid");
						row.spgID = rst.getShort("sgid");
						row.spyID = rst.getByte("syid");
						row.subID = rst.getByte("sbid");
						row.proID = rst.getByte("poid");
						row.facility = rst.getString("fanm");
						row.specialty = rst.getString("synm");
						row.subspecial = rst.getString("sbnm");
						row.procedure = rst.getString("ponm");
						row.specimen = rst.getString("sgdc");
						rows.add(row);
					}
					yearID = rst.getShort("yearid");
					if (row.specs.get(yearID) == null) {
						row.specs.put(yearID, rst.getInt("frsp"));
					} else {
						row.specs.replace(yearID, (rst.getInt("frsp") + row.specs.get(yearID)));
					}
					if (row.blocks.get(yearID) == null) {
						row.blocks.put(yearID, rst.getInt("frbl"));
					} else {
						row.blocks.replace(yearID, (rst.getInt("frbl") + row.blocks.get(yearID)));
					}
					if (row.slides.get(yearID) == null) {
						row.slides.put(yearID, rst.getInt("frsl"));
					} else {
						row.slides.replace(yearID, (rst.getInt("frsl") + row.slides.get(yearID)));
					}
					if (row.fte1.get(yearID) == null) {
						row.fte1.put(yearID, rst.getDouble("frv1"));
					} else {
						row.fte1.replace(yearID, (rst.getDouble("frv1") + row.fte1.get(yearID)));
					}
					if (row.fte2.get(yearID) == null) {
						row.fte2.put(yearID, rst.getDouble("frv2"));
					} else {
						row.fte2.replace(yearID, (rst.getDouble("frv2") + row.fte2.get(yearID)));
					}
					if (row.fte3.get(yearID) == null) {
						row.fte3.put(yearID, rst.getDouble("frv3"));
					} else {
						row.fte3.replace(yearID, (rst.getDouble("frv3") + row.fte3.get(yearID)));
					}
					if (row.fte4.get(yearID) == null) {
						row.fte4.put(yearID, rst.getDouble("frv4"));
					} else {
						row.fte4.replace(yearID, (rst.getDouble("frv4") + row.fte4.get(yearID)));
					}
					if (row.fte5.get(yearID) == null) {
						row.fte5.put(yearID, rst.getDouble("frv5"));
					} else {
						row.fte5.replace(yearID, (rst.getDouble("frv5") + row.fte5.get(yearID)));
					}
				}
				rst.close();
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_YER), 1, calStart.getTimeInMillis());
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_YER), 2, calEnd.getTimeInMillis());
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ADD_SL_YER));
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).facID == rst.getShort("faid") && rows.get(i).spyID == rst.getByte("syid")
								&& rows.get(i).subID == rst.getByte("sbid") && rows.get(i).proID == rst.getByte("poid")
								&& rows.get(i).spgID == rst.getShort("sgid")) {
							row = rows.get(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						row = new OAnnual();
						row.facID = rst.getShort("faid");
						row.spgID = rst.getShort("sgid");
						row.spyID = rst.getByte("syid");
						row.subID = rst.getByte("sbid");
						row.proID = rst.getByte("poid");
						row.facility = rst.getString("fanm");
						row.specialty = rst.getString("synm");
						row.subspecial = rst.getString("sbnm");
						row.procedure = rst.getString("ponm");
						row.specimen = rst.getString("sgdc");
						rows.add(row);
					}
					yearID = rst.getShort("yearid");
					if (row.specs.get(yearID) == null) {
						row.specs.put(yearID, 0);
					}
					if (row.blocks.get(yearID) == null) {
						row.blocks.put(yearID, 0);
					}
					if (row.slides.get(yearID) == null) {
						row.slides.put(yearID, 0);
					}
					if (row.fte1.get(yearID) == null) {
						row.fte1.put(yearID, rst.getDouble("adv1"));
					} else {
						row.fte1.replace(yearID, (rst.getDouble("adv1") + row.fte1.get(yearID)));
					}
					if (row.fte2.get(yearID) == null) {
						row.fte2.put(yearID, rst.getDouble("adv2"));
					} else {
						row.fte2.replace(yearID, (rst.getDouble("adv2") + row.fte2.get(yearID)));
					}
					if (row.fte3.get(yearID) == null) {
						row.fte3.put(yearID, rst.getDouble("adv3"));
					} else {
						row.fte3.replace(yearID, (rst.getDouble("adv3") + row.fte3.get(yearID)));
					}
					if (row.fte4.get(yearID) == null) {
						row.fte4.put(yearID, rst.getDouble("adv4"));
					} else {
						row.fte4.replace(yearID, (rst.getDouble("adv4") + row.fte4.get(yearID)));
					}
					if (row.fte5.get(yearID) == null) {
						row.fte5.put(yearID, rst.getDouble("adv5"));
					} else {
						row.fte5.replace(yearID, (rst.getDouble("adv5") + row.fte5.get(yearID)));
					}
				}
				tableRows.clear();
				for (short i = yearMin; i <= yearMax; i++) {
					tableRows.add(Integer.toString(i));
				}
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
		}

		private PolynomialFunction setFunction(int[] years, int[] count) {
			double[] dCount = new double[count.length];
			for (int i = 0; i < count.length; i++) {
				dCount[i] = count[i];
			}
			return setFunction(years, dCount);
		}

		private PolynomialFunction setFunction(int[] years, double[] count) {
			WeightedObservedPoints obs = new WeightedObservedPoints();
			for (int i = 0; i < years.length; i++) {
				obs.add(years[i], count[i]);
			}
			PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
			double[] coeff = fitter.fit(obs.toList());
			// make polynomial
			return new PolynomialFunction(coeff);
		}

		private void setModel(OAnnualList item, OAnnualNode node) {
			node.name = item.name;
			node.specs = item.specs;
			node.blocks = item.blocks;
			node.slides = item.slides;
			node.fte1 = item.fte1;
			node.fte2 = item.fte2;
			node.fte3 = item.fte3;
			node.fte4 = item.fte4;
			node.fte5 = item.fte5;
			node.children = new OAnnualNode[item.children.size()];
			for (int i = 0; i < item.children.size(); i++) {
				OAnnualList childItem = item.children.get(i);
				OAnnualNode childNode = new OAnnualNode();
				node.children[i] = childNode;
				setModel(childItem, childNode);
			}
		}

		private void setTotals(OAnnualList master, double fte1, double fte2, double fte3, double fte4, double fte5) {
			OAnnualList child;
			for (int i = master.children.size() - 1; i >= 0; i--) {
				child = master.children.get(i);
				if (child.id < 0) {
					// filtered out
					master.children.remove(i);
					continue;
				}
				for (int j = 0; j < child.fte1.length; j++) {
					child.fte1[j] = child.fte1[j] / fte1;
					child.fte2[j] = child.fte2[j] / fte2;
					child.fte3[j] = child.fte3[j] / fte3;
					child.fte4[j] = child.fte4[j] / fte4;
					child.fte5[j] = child.fte5[j] / fte5;
				}
				if (child.children.size() > 0) {
					setTotals(child, fte1, fte2, fte3, fte4, fte5);
				}
			}
		}

		private void structureData() {
			byte noYears = (byte) (yearMax - yearMin + 1);
			short id = 0;
			short ids[] = new short[rowsView.length];
			int rowNos[] = new int[rowsView.length];
			int size = rows.size();
			double fte1 = pj.setup.getShort(LSetup.VAR_CODER1_FTE);
			double fte2 = pj.setup.getShort(LSetup.VAR_CODER2_FTE);
			double fte3 = pj.setup.getShort(LSetup.VAR_CODER3_FTE);
			double fte4 = pj.setup.getShort(LSetup.VAR_CODER4_FTE);
			double fte5 = pj.setup.getInt(LSetup.VAR_V5_FTE);
			String name = "";
			OAnnualList data0 = new OAnnualList("Total", noYears, id);
			OAnnualList data1 = new OAnnualList(name, noYears, id);
			OAnnualList data2 = new OAnnualList(name, noYears, id);
			OAnnualList data3 = new OAnnualList(name, noYears, id);
			OAnnualList data4 = new OAnnualList(name, noYears, id);
			OAnnualList data5 = new OAnnualList(name, noYears, id);
			OAnnual row = new OAnnual();
			for (int i = 0; i < rowsView.length; i++) {
				ids[i] = -1;
			}
			if (fte1 == 0.0) {
				fte1 = 1.0;
			}
			if (fte2 == 0.0) {
				fte2 = 1.0;
			}
			if (fte3 == 0.0) {
				fte3 = 1.0;
			}
			if (fte4 == 0.0) {
				fte4 = 1.0;
			}
			if (fte5 == 0.0) {
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
				case IPanelRows.ROW_SPECIMEN:
					id = row.spgID;
					name = row.specimen;
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
						data1 = new OAnnualList(name, noYears, ids[0]);
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
				case IPanelRows.ROW_SPECIMEN:
					id = row.spgID;
					name = row.specimen;
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
						data2 = new OAnnualList(name, noYears, ids[1]);
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
				case IPanelRows.ROW_SPECIMEN:
					id = row.spgID;
					name = row.specimen;
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
						data3 = new OAnnualList(name, noYears, ids[2]);
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
				case IPanelRows.ROW_SPECIMEN:
					id = row.spgID;
					name = row.specimen;
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
						data4 = new OAnnualList(name, noYears, ids[3]);
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
				case IPanelRows.ROW_SPECIMEN:
					id = row.spgID;
					name = row.specimen;
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
						data5 = new OAnnualList(name, noYears, ids[4]);
						data4.children.add(data5);
					}
				}
				short yearID = yearMin;
				Integer qty = 0;
				Double val = 0.0;
				for (short y = 0; y < noYears; y++) {
					qty = row.specs.get(yearID);
					if (qty != null && qty > 0) {
						data0.specs[y] += qty;
						data1.specs[y] += qty;
						data2.specs[y] += qty;
						data3.specs[y] += qty;
						data4.specs[y] += qty;
						data5.specs[y] += qty;
					}
					qty = row.blocks.get(yearID);
					if (qty != null && qty > 0) {
						data0.blocks[y] += qty;
						data1.blocks[y] += qty;
						data2.blocks[y] += qty;
						data3.blocks[y] += qty;
						data4.blocks[y] += qty;
						data5.blocks[y] += qty;
					}
					qty = row.slides.get(yearID);
					if (qty != null && qty > 0) {
						data0.slides[y] += qty;
						data1.slides[y] += qty;
						data2.slides[y] += qty;
						data3.slides[y] += qty;
						data4.slides[y] += qty;
						data5.slides[y] += qty;
					}
					val = row.fte1.get(yearID);
					if (val != null && val > 0) {
						data0.fte1[y] += val;
						data1.fte1[y] += val;
						data2.fte1[y] += val;
						data3.fte1[y] += val;
						data4.fte1[y] += val;
						data5.fte1[y] += val;
					}
					val = row.fte2.get(yearID);
					if (val != null && val > 0) {
						data0.fte2[y] += val;
						data1.fte2[y] += val;
						data2.fte2[y] += val;
						data3.fte2[y] += val;
						data4.fte2[y] += val;
						data5.fte2[y] += val;
					}
					val = row.fte3.get(yearID);
					if (val != null && val > 0) {
						data0.fte3[y] += val;
						data1.fte3[y] += val;
						data2.fte3[y] += val;
						data3.fte3[y] += val;
						data4.fte3[y] += val;
						data5.fte3[y] += val;
					}
					val = row.fte4.get(yearID);
					if (val != null && val > 0) {
						data0.fte4[y] += val;
						data1.fte4[y] += val;
						data2.fte4[y] += val;
						data3.fte4[y] += val;
						data4.fte4[y] += val;
						data5.fte4[y] += val;
					}
					val = row.fte5.get(yearID);
					if (val != null && val > 0) {
						data0.fte5[y] += val;
						data1.fte5[y] += val;
						data2.fte5[y] += val;
						data3.fte5[y] += val;
						data4.fte5[y] += val;
						data5.fte5[y] += val;
					}
					yearID++;
				}
			}
			for (int i = 0; i < data0.fte1.length; i++) {
				data0.fte1[i] = data0.fte1[i] / fte1;
				data0.fte2[i] = data0.fte2[i] / fte2;
				data0.fte3[i] = data0.fte3[i] / fte3;
				data0.fte4[i] = data0.fte4[i] / fte4;
				data0.fte5[i] = data0.fte5[i] / fte5;
			}
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			setTotals(data0, fte1, fte2, fte3, fte4, fte5);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			
			ModelGroup model = (ModelGroup) tree.getModel();
			OAnnualNode node0 = (OAnnualNode) model.getRoot();
			setModel(data0, node0);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			forecast(node0);
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}
}