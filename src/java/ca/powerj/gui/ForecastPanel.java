package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import ca.powerj.data.ForecastData;
import ca.powerj.data.ForecastList;
import ca.powerj.data.ForecastNode;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IChart2Lines;
import ca.powerj.swing.IRowPanel;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class ForecastPanel extends BasePanel {
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
	private ForecastNode dataNode = new ForecastNode();
	private IChart2Lines chartSpecs, chartCoder1, chartCoder2, chartCoder3, chartCoder4, chartCoder5;

	public ForecastPanel(AppFrame application) {
		super(application);
		setName("Forecast");
		application.dbPowerJ.setStatements(LibConstants.ACTION_FORECAST);
		columns[4] = application.getProperty("coder1");
		columns[5] = application.getProperty("coder2");
		columns[6] = application.getProperty("coder3");
		columns[7] = application.getProperty("coder4");
		columns[8] = application.getProperty("coder5");
		rowsView[0] = IRowPanel.ROW_FACILITY;
		rowsView[1] = IRowPanel.ROW_SPECIALTY;
		rowsView[2] = IRowPanel.ROW_SUBSPECIAL;
		rowsView[3] = IRowPanel.ROW_PROCEDURE;
		rowsView[4] = IRowPanel.ROW_SPECIMEN;
		createPanel();
		programmaticChange = false;
		altered = true;
	}

	@Override
	public boolean close() {
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
		tree.setFont(LibConstants.APP_FONT);
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
		JScrollPane scrollTree = IUtilities.createJScrollPane(tree);
		scrollTree.setMinimumSize(new Dimension(200, 400));
		modelData = new ModelData();
		table = new ITable(modelData, application.dates, application.numbers);
		table.getColumnModel().getColumn(0).setMinWidth(120);
		JScrollPane scrollTable = IUtilities.createJScrollPane(table);
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
		add(new IToolBar(application, null, null, null, null, rowsView), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("forecast.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 4, 1, 1.5f, 1.5f, 1.5f, 1, 1, 1, 1, 1 };
		String str = "Forecast " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATE)
			+ " - " + application.dates.formatter(timeTo, LibDates.FORMAT_DATE);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER, 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable pdfTable = new PdfPTable(widths.length);
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
				pdfTable.addCell(cell);
			}
			pdfTable.setHeaderRows(1);
			// data rows
			ModelGroup model = (ModelGroup) tree.getModel();
			ForecastNode root = (ForecastNode) model.getRoot();
			pdfRow(root, pdfTable, paragraph, cell, fonts.get("Font10n"), "", widths.length);
			document.add(pdfTable);
			document.close();
		} catch (DocumentException e) {
			application.log(LibConstants.ERROR_IO, getName(), e);
		} catch (FileNotFoundException e) {
			application.log(LibConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		}
	}

	private void pdfRow(ForecastNode dataNode, PdfPTable table, Paragraph paragraph, PdfPCell cell, Font font, String name, int width) {
		for (int row = 0; row < tableRows.size(); row++) {
			for (int col = 0; col < width; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(font);
				cell = new PdfPCell();
				switch (col) {
				case 0:
					if (row == 0) {
						if (name.length() == 0) {
							paragraph.add(new Chunk(dataNode.getName()));
							name = "/";
						} else if (name.equals("/")) {
							paragraph.add(new Chunk(dataNode.getName()));
							name = dataNode.getName();
						} else {
							name += "/" + dataNode.getName();
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
					paragraph.add(new Chunk(application.numbers.formatNumber(dataNode.getSpecimens(row))));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 3:
					paragraph.add(new Chunk(application.numbers.formatNumber(dataNode.getBlocks(row))));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 4:
					paragraph.add(new Chunk(application.numbers.formatNumber(dataNode.getSlides(row))));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 5:
					paragraph.add(new Chunk(application.numbers.formatDouble(2, dataNode.getFte1(row))));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 6:
					paragraph.add(new Chunk(application.numbers.formatDouble(2, dataNode.getFte2(row))));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 7:
					paragraph.add(new Chunk(application.numbers.formatDouble(2, dataNode.getFte3(row))));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				case 8:
					paragraph.add(new Chunk(application.numbers.formatDouble(2, dataNode.getFte4(row))));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					break;
				default:
					paragraph.add(new Chunk(application.numbers.formatDouble(2, dataNode.getFte5(row))));
					paragraph.setAlignment(Element.ALIGN_RIGHT);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				}
				cell.addElement(paragraph);
				table.addCell(cell);
			}
		}
		int noChildren = dataNode.getNoChildren();
		for (int i = 0; i < noChildren; i++) {
			ForecastNode child = (ForecastNode) dataNode.getChild(i);
			pdfRow(child, table, paragraph, cell, font, name, width);
		}
	}

	@Override
	public void setFilter(int[] values) {
		for (int i = 0; i < rowsView.length; i++) {
			rowsView[i] = (byte) values[i];
		}
		altered = true;
	}

	@Override
	public void setFilter(short id, int value) {
		// Go button
		if (altered) {
			application.setBusy(true);
			WorkerData worker = new WorkerData();
			worker.execute();
			altered = false;
		}
	}

	private void setSelection(TreePath treePath) {
		if (treePath != null) {
			dataNode = (ForecastNode) (treePath.getLastPathComponent());
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
				ySpecs[0][i] = dataNode.getSpecimens(i);
				ySpecs[1][i] = dataNode.getSpecimensf(i);
				yCoder1[0][i] = dataNode.getFte1(i);
				yCoder1[1][i] = dataNode.getFte1f(i);
				yCoder2[0][i] = dataNode.getFte2(i);
				yCoder2[1][i] = dataNode.getFte2f(i);
				yCoder3[0][i] = dataNode.getFte3(i);
				yCoder3[1][i] = dataNode.getFte3f(i);
				yCoder4[0][i] = dataNode.getFte4(i);
				yCoder4[1][i] = dataNode.getFte4f(i);
				yCoder5[0][i] = dataNode.getFte5(i);
				yCoder5[1][i] = dataNode.getFte5f(i);
				xTitles[i] = tableRows.get(i);
				int n = application.numbers.parseInt(xTitles[i]);
				if (yearID > n) {
					yearID = n;
				}
			} else {
				ySpecs[1][i] = dataNode.getSpecimensf(i);
				yCoder1[1][i] = dataNode.getFte1f(i);
				yCoder2[1][i] = dataNode.getFte2f(i);
				yCoder3[1][i] = dataNode.getFte3f(i);
				yCoder4[1][i] = dataNode.getFte4f(i);
				yCoder5[1][i] = dataNode.getFte5f(i);
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
		String fileName = application.getFileXls("forecast.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Forecast");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Forecast " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATE)
				+ " - " + application.dates.formatter(timeTo, LibDates.FORMAT_DATE));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col <= columns.length; col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellStyle(styles.get("header"));
				if (col == 0) {
					xlsCell.setCellValue("Name");
				} else {
					xlsCell.setCellValue(columns[col-1]);
				}
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
			ForecastNode root = (ForecastNode) model.getRoot();
			int rownum = 2;
			xlsRow(root, sheet, xlsCell, "", rownum);
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

	private int xlsRow(ForecastNode annualNode, Sheet sheet, Cell xlsCell, String name, int rownum) {
		for (int row = 0; row < tableRows.size(); row++) {
			Row xlsRow = sheet.createRow(rownum++);
			for (int col = 0; col < 10; col++) {
				xlsCell = xlsRow.createCell(col);
				switch (col) {
				case 0:
					if (row == 0) {
						if (name.length() == 0) {
							xlsCell.setCellValue(annualNode.getName());
							name = "/";
						} else if (name.equals("/")) {
							xlsCell.setCellValue(annualNode.getName());
							name = annualNode.getName();
						} else {
							name += "/" + annualNode.getName();
							xlsCell.setCellValue(name);
						}
					}
					break;
				case 1:
					xlsCell.setCellValue(tableRows.get(row));
					break;
				case 2:
					xlsCell.setCellValue(annualNode.getSpecimens(row));
					break;
				case 3:
					xlsCell.setCellValue(annualNode.getBlocks(row));
					break;
				case 4:
					xlsCell.setCellValue(annualNode.getSlides(row));
					break;
				case 5:
					xlsCell.setCellValue(annualNode.getFte1(row));
					break;
				case 6:
					xlsCell.setCellValue(annualNode.getFte2(row));
					break;
				case 7:
					xlsCell.setCellValue(annualNode.getFte3(row));
					break;
				case 8:
					xlsCell.setCellValue(annualNode.getFte4(row));
					break;
				default:
					xlsCell.setCellValue(annualNode.getFte5(row));
				}
			}
		}
		int noChildren = annualNode.getNoChildren();
		for (int i = 0; i < noChildren; i++) {
			ForecastNode child = (ForecastNode) annualNode.getChild(i);
			rownum = xlsRow(child, sheet, xlsCell, name, rownum);
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
				switch (col) {
				case DATA_YEARS:
					return tableRows.get(row);
				case DATA_SPECS:
					return dataNode.getSpecimens(row);
				case DATA_BLCKS:
					return dataNode.getBlocks(row);
				case DATA_SLIDE:
					return dataNode.getSlides(row);
				case DATA_VALU1:
					return dataNode.getFte1(row);
				case DATA_VALU2:
					return dataNode.getFte2(row);
				case DATA_VALU3:
					return dataNode.getFte3(row);
				case DATA_VALU4:
					return dataNode.getFte4(row);
				default:
					return dataNode.getFte5(row);
				}
			}
			return Object.class;
		}
	}

	private class ModelGroup implements TreeModel {
		private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
		private ForecastNode root;

		ModelGroup(ForecastNode root) {
			this.root = root;
		}

		protected void fireTreeStructureChanged(ForecastNode oldRoot) {
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
		public Object getChild(Object node, int index) {
			return ((ForecastNode) node).getChild(index);
		}

		@Override
		public int getChildCount(Object node) {
			return ((ForecastNode) node).getNoChildren();
		}

		@Override
		public int getIndexOfChild(Object node, Object child) {
			ForecastNode data = (ForecastNode) node;
			int noChildren = data.getNoChildren();
			for (int i = 0; i < noChildren; i++) {
				if (data.getChild(i).equals(child)) {
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
			return (((ForecastNode) node).getNoChildren() == 0);
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
		private ArrayList<ForecastData> rows = new ArrayList<ForecastData>();

		@Override
		protected Void doInBackground() throws Exception {
			setName("WorkerData");
			try {
				getData();
				structureData();
			} catch (Exception e) {
				application.log(LibConstants.ERROR_UNEXPECTED, getName(), e);
			}
			return null;
		}

		@Override
		public void done() {
			// Display tree results
			ModelGroup model = (ModelGroup) tree.getModel();
			dataNode = (ForecastNode) model.getRoot();
			model.fireTreeStructureChanged(dataNode);
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			application.display("Forecast " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATELONG) + " - "
					+ application.dates.formatter(timeTo, LibDates.FORMAT_DATELONG));
			application.setBusy(false);
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
			PolynomialFunction polynomial = setFunction(oYears, oCount);
			double[] nCount = new double[nYears.length];
			for (int i = 0; i < nYears.length; i++) {
				nCount[i] = polynomial.value(nYears[i]);
			}
			return nCount;
		}

		private void extrapolate(int[] oYears, int[] nYears, ForecastNode node) {
			node.setSpecsf(extrapolate(oYears, nYears, node.getSpecimens()));
			node.setBlocksf(extrapolate(oYears, nYears, node.getBlocks()));
			node.setSlidesf(extrapolate(oYears, nYears, node.getSlides()));
			node.setFte1f(extrapolate(oYears, nYears, node.getFte1()));
			node.setFte2f(extrapolate(oYears, nYears, node.getFte2()));
			node.setFte3f(extrapolate(oYears, nYears, node.getFte3()));
			node.setFte4f(extrapolate(oYears, nYears, node.getFte4()));
			node.setFte5f(extrapolate(oYears, nYears, node.getFte5()));
			int noChildren = node.getNoChildren();
			for (int i = 0; i < noChildren; i++) {
				extrapolate(oYears, nYears, (ForecastNode) node.getChild(i));
			}
		}

		private void forecast(ForecastNode root) {
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
			Calendar calStart = application.dates.setMidnight(null);
			Calendar calEnd = application.dates.setMidnight(null);
			ForecastData row = new ForecastData();
			ForecastData tempItem = new ForecastData();
			ArrayList<Short> years = new ArrayList<Short>();
			calStart.setTimeInMillis(application.setup.getLong(LibSetup.VAR_MIN_WL_DATE));
			calEnd.set(Calendar.DAY_OF_YEAR, 1);
			timeFrom = calStart.getTimeInMillis();
			timeTo = calEnd.getTimeInMillis();
			rows = application.dbPowerJ.getSpecimenGroupForecast(timeFrom, timeTo);
			if (rows.size() > 0) {
				years = rows.get(0).getYears();
			}
			for (int i = 0; i < years.size(); i++) {
				if (yearMin > years.get(i)) {
					yearMin = years.get(i);
				}
				if (yearMax < years.get(i)) {
					yearMax = years.get(i);
				}
			}
			ArrayList<ForecastData> tempList = application.dbPowerJ.getFrozenForecast(timeFrom, timeTo);
			for (int j = 0; j < tempList.size(); j++) {
				tempItem = tempList.get(j);
				exists = false;
				for (int i = 0; i < rows.size(); i++) {
					if (rows.get(i).getFacID() == tempItem.getFacID() && rows.get(i).getSpyID() == tempItem.getSpyID()
							&& rows.get(i).getSubID() == tempItem.getSubID() && rows.get(i).getProID() == tempItem.getProID()
							&& rows.get(i).getSpgID() == tempItem.getSpgID()) {
						row = rows.get(i);
						exists = true;
						break;
					}
				}
				if (!exists) {
					row = new ForecastData();
					row.setFacID(tempItem.getFacID());
					row.setProID(tempItem.getProID());
					row.setSpgID(tempItem.getSpgID());
					row.setSpyID(tempItem.getSpyID());
					row.setSubID(tempItem.getSubID());
					row.setFacName(tempItem.getFacName());
					row.setProName(tempItem.getProName());
					row.setSpgName(tempItem.getSpgName());
					row.setSpyName(tempItem.getSpyName());
					row.setSubName(tempItem.getSubName());
					rows.add(row);
				}
				for (int k = 0; k < years.size(); k++) {
					yearID = years.get(k);
					row.setNoSpecs(yearID, tempItem.getNoSpecs(yearID));
					row.setNoBlocks(yearID, tempItem.getNoBlocks(yearID));
					row.setNoSlides(yearID, tempItem.getNoSlides(yearID));
					row.setFte1(yearID, tempItem.getFte1(yearID));
					row.setFte2(yearID, tempItem.getFte2(yearID));
					row.setFte3(yearID, tempItem.getFte3(yearID));
					row.setFte4(yearID, tempItem.getFte4(yearID));
					row.setFte5(yearID, tempItem.getFte5(yearID));
				}
			}
			tempList.clear();
			tempList = application.dbPowerJ.getAdditionalForecast(timeFrom, timeTo);
			for (int j = 0; j < tempList.size(); j++) {
				tempItem = tempList.get(j);
				exists = false;
				for (int i = 0; i < rows.size(); i++) {
					if (rows.get(i).getFacID() == tempItem.getFacID() && rows.get(i).getSpyID() == tempItem.getSpyID()
							&& rows.get(i).getSubID() == tempItem.getSubID() && rows.get(i).getProID() == tempItem.getProID()
							&& rows.get(i).getSpgID() == tempItem.getSpgID()) {
						row = rows.get(i);
						exists = true;
						break;
					}
				}
				if (!exists) {
					row = new ForecastData();
					row.setFacID(tempItem.getFacID());
					row.setProID(tempItem.getProID());
					row.setSpgID(tempItem.getSpgID());
					row.setSpyID(tempItem.getSpyID());
					row.setSubID(tempItem.getSubID());
					row.setFacName(tempItem.getFacName());
					row.setProName(tempItem.getProName());
					row.setSpgName(tempItem.getSpgName());
					row.setSpyName(tempItem.getSpyName());
					row.setSubName(tempItem.getSubName());
					rows.add(row);
				}
				for (int k = 0; k < years.size(); k++) {
					yearID = years.get(k);
					row.setNoSpecs(yearID, tempItem.getNoSpecs(yearID));
					row.setNoBlocks(yearID, tempItem.getNoBlocks(yearID));
					row.setNoSlides(yearID, tempItem.getNoSlides(yearID));
					row.setFte1(yearID, tempItem.getFte1(yearID));
					row.setFte2(yearID, tempItem.getFte2(yearID));
					row.setFte3(yearID, tempItem.getFte3(yearID));
					row.setFte4(yearID, tempItem.getFte4(yearID));
					row.setFte5(yearID, tempItem.getFte5(yearID));
				}
			}
			tempList.clear();
			tableRows.clear();
			for (short i = yearMin; i <= yearMax; i++) {
				tableRows.add(Integer.toString(i));
			}
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
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

		private void setModel(ForecastList item, ForecastNode node) {
			int noChildren = item.getNoChildren();
			node.setName(item.getName());
			node.setSpecimens(item.getSpecimens());
			node.setBlocks(item.getBlocks());
			node.setSlides(item.getSlides());
			node.setFte1(item.getFte1());
			node.setFte2(item.getFte2());
			node.setFte3(item.getFte3());
			node.setFte4(item.getFte4());
			node.setFte5(item.getFte5());
			node.setChildren(noChildren);
			for (int i = 0; i < noChildren; i++) {
				ForecastNode child = new ForecastNode();
				node.setChild(i, child);
				setModel(item.getChild(i), child);
			}
		}

		private void setTotals(ForecastList item, double fte1, double fte2, double fte3, double fte4, double fte5) {
			int noChildren = item.getNoChildren();
			int noYears = 0;
			ForecastList child;
			for (int i = noChildren -1; i >= 0; i--) {
				child = item.getChild(i);
				if (child.getId() < 0) {
					// filtered out
					item.removeChild(i);
					continue;
				}
				noYears = child.getNoYears();
				for (int j = 0; j < noYears; j++) {
					child.setFte1(j, child.getFte1(j) / fte1);
					child.setFte2(j, child.getFte2(j) / fte2);
					child.setFte3(j, child.getFte3(j) / fte3);
					child.setFte4(j, child.getFte4(j) / fte4);
					child.setFte5(j, child.getFte5(j) / fte5);
				}
				if (child.getNoChildren() > 0) {
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
			double fte1 = application.setup.getShort(LibSetup.VAR_CODER1_FTE);
			double fte2 = application.setup.getShort(LibSetup.VAR_CODER2_FTE);
			double fte3 = application.setup.getShort(LibSetup.VAR_CODER3_FTE);
			double fte4 = application.setup.getShort(LibSetup.VAR_CODER4_FTE);
			double fte5 = application.setup.getInt(LibSetup.VAR_V5_FTE);
			String name = "";
			ForecastList data0 = new ForecastList("Total", noYears, id);
			ForecastList data1 = new ForecastList(name, noYears, id);
			ForecastList data2 = new ForecastList(name, noYears, id);
			ForecastList data3 = new ForecastList(name, noYears, id);
			ForecastList data4 = new ForecastList(name, noYears, id);
			ForecastList data5 = new ForecastList(name, noYears, id);
			ForecastData row = new ForecastData();
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
				case IRowPanel.ROW_FACILITY:
					id = row.getFacID();
					name = row.getFacName();
					break;
				case IRowPanel.ROW_SPECIALTY:
					id = row.getSpyID();
					name = row.getSpyName();
					break;
				case IRowPanel.ROW_SUBSPECIAL:
					id = row.getSubID();
					name = row.getSubName();
					break;
				case IRowPanel.ROW_PROCEDURE:
					id = row.getProID();
					name = row.getProName();
					break;
				case IRowPanel.ROW_SPECIMEN:
					id = row.getSpgID();
					name = row.getSpgName();
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
					for (int j = 0; j < data0.getNoChildren(); j++) {
						data1 = data0.getChild(j);
						if (data1.getId() == ids[0]) {
							rowNos[0] = j;
							break;
						}
					}
					if (rowNos[0] < 0) {
						rowNos[0] = data0.getNoChildren();
						data1 = new ForecastList(name, noYears, ids[0]);
						data0.addChild(data1);
					}
				}
				// Match 2nd node
				switch (rowsView[1]) {
				case IRowPanel.ROW_FACILITY:
					id = row.getFacID();
					name = row.getFacName();
					break;
				case IRowPanel.ROW_SPECIALTY:
					id = row.getSpyID();
					name = row.getSpyName();
					break;
				case IRowPanel.ROW_SUBSPECIAL:
					id = row.getSubID();
					name = row.getSubName();
					break;
				case IRowPanel.ROW_PROCEDURE:
					id = row.getProID();
					name = row.getProName();
					break;
				case IRowPanel.ROW_SPECIMEN:
					id = row.getSpgID();
					name = row.getSpgName();
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
					for (int j = 0; j < data1.getNoChildren(); j++) {
						data2 = data1.getChild(j);
						if (data2.getId() == ids[1]) {
							rowNos[1] = j;
							break;
						}
					}
					if (rowNos[1] < 0) {
						rowNos[1] = data1.getNoChildren();
						data2 = new ForecastList(name, noYears, ids[1]);
						data1.addChild(data2);
					}
				}
				// Match 3rd node
				switch (rowsView[2]) {
				case IRowPanel.ROW_FACILITY:
					id = row.getFacID();
					name = row.getFacName();
					break;
				case IRowPanel.ROW_SPECIALTY:
					id = row.getSpyID();
					name = row.getSpyName();
					break;
				case IRowPanel.ROW_SUBSPECIAL:
					id = row.getSubID();
					name = row.getSubName();
					break;
				case IRowPanel.ROW_PROCEDURE:
					id = row.getProID();
					name = row.getProName();
					break;
				case IRowPanel.ROW_SPECIMEN:
					id = row.getSpgID();
					name = row.getSpgName();
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
					for (int j = 0; j < data2.getNoChildren(); j++) {
						data3 = data2.getChild(j);
						if (data3.getId() == ids[2]) {
							rowNos[2] = j;
							break;
						}
					}
					if (rowNos[2] < 0) {
						rowNos[2] = data2.getNoChildren();
						data3 = new ForecastList(name, noYears, ids[2]);
						data2.addChild(data3);
					}
				}
				// Match 4th node
				switch (rowsView[3]) {
				case IRowPanel.ROW_FACILITY:
					id = row.getFacID();
					name = row.getFacName();
					break;
				case IRowPanel.ROW_SPECIALTY:
					id = row.getSpyID();
					name = row.getSpyName();
					break;
				case IRowPanel.ROW_SUBSPECIAL:
					id = row.getSubID();
					name = row.getSubName();
					break;
				case IRowPanel.ROW_PROCEDURE:
					id = row.getProID();
					name = row.getProName();
					break;
				case IRowPanel.ROW_SPECIMEN:
					id = row.getSpgID();
					name = row.getSpgName();
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
					for (int j = 0; j < data3.getNoChildren(); j++) {
						data4 = data3.getChild(j);
						if (data4.getId() == ids[3]) {
							rowNos[3] = j;
							break;
						}
					}
					if (rowNos[3] < 0) {
						rowNos[3] = data3.getNoChildren();
						data4 = new ForecastList(name, noYears, ids[3]);
						data3.addChild(data4);
					}
				}
				// Match 5th node
				switch (rowsView[4]) {
				case IRowPanel.ROW_FACILITY:
					id = row.getFacID();
					name = row.getFacName();
					break;
				case IRowPanel.ROW_SPECIALTY:
					id = row.getSpyID();
					name = row.getSpyName();
					break;
				case IRowPanel.ROW_SUBSPECIAL:
					id = row.getSubID();
					name = row.getSubName();
					break;
				case IRowPanel.ROW_PROCEDURE:
					id = row.getProID();
					name = row.getProName();
					break;
				case IRowPanel.ROW_SPECIMEN:
					id = row.getSpgID();
					name = row.getSpgName();
					break;
				default:
					id = -2;
				}
				if (ids[4] != id) {
					ids[4] = id;
					rowNos[4] = -1;
					for (int j = 0; j < data4.getNoChildren(); j++) {
						data5 = data4.getChild(j);
						if (data5.getId() == ids[4]) {
							rowNos[4] = j;
							break;
						}
					}
					if (rowNos[4] < 0) {
						rowNos[4] = data4.getNoChildren();
						data5 = new ForecastList(name, noYears, ids[4]);
						data4.addChild(data5);
					}
				}
				short yearID = yearMin;
				Integer qty = 0;
				Double val = 0.0;
				for (short y = 0; y < noYears; y++) {
					qty = row.getNoSpecs(yearID);
					if (qty != null && qty > 0) {
						data0.setSpecimens(y, data0.getSpecimens(y) + qty);
						data1.setSpecimens(y, data1.getSpecimens(y) + qty);
						data2.setSpecimens(y, data2.getSpecimens(y) + qty);
						data3.setSpecimens(y, data3.getSpecimens(y) + qty);
						data4.setSpecimens(y, data4.getSpecimens(y) + qty);
						data5.setSpecimens(y, data5.getSpecimens(y) + qty);
					}
					qty = row.getNoBlocks(yearID);
					if (qty != null && qty > 0) {
						data0.setBlocks(y, data0.getBlocks(y) + qty);
						data1.setBlocks(y, data1.getBlocks(y) + qty);
						data2.setBlocks(y, data2.getBlocks(y) + qty);
						data3.setBlocks(y, data3.getBlocks(y) + qty);
						data4.setBlocks(y, data4.getBlocks(y) + qty);
						data5.setBlocks(y, data5.getBlocks(y) + qty);
					}
					qty = row.getNoSlides(yearID);
					if (qty != null && qty > 0) {
						data0.setSlides(y, data0.getSlides(y) + qty);
						data1.setSlides(y, data1.getSlides(y) + qty);
						data2.setSlides(y, data2.getSlides(y) + qty);
						data3.setSlides(y, data3.getSlides(y) + qty);
						data4.setSlides(y, data4.getSlides(y) + qty);
						data5.setSlides(y, data5.getSlides(y) + qty);
					}
					val = row.getFte1(yearID);
					if (val != null && val > 0) {
						data0.setFte1(y, data0.getFte1(y) + val);
						data1.setFte1(y, data1.getFte1(y) + val);
						data2.setFte1(y, data2.getFte1(y) + val);
						data3.setFte1(y, data3.getFte1(y) + val);
						data4.setFte1(y, data4.getFte1(y) + val);
						data5.setFte1(y, data5.getFte1(y) + val);
					}
					val = row.getFte2(yearID);
					if (val != null && val > 0) {
						data0.setFte2(y, data0.getFte2(y) + val);
						data1.setFte2(y, data1.getFte2(y) + val);
						data2.setFte2(y, data2.getFte2(y) + val);
						data3.setFte2(y, data3.getFte2(y) + val);
						data4.setFte2(y, data4.getFte2(y) + val);
						data5.setFte2(y, data5.getFte2(y) + val);
					}
					val = row.getFte3(yearID);
					if (val != null && val > 0) {
						data0.setFte3(y, data0.getFte3(y) + val);
						data1.setFte3(y, data1.getFte3(y) + val);
						data2.setFte3(y, data2.getFte3(y) + val);
						data3.setFte3(y, data3.getFte3(y) + val);
						data4.setFte3(y, data4.getFte3(y) + val);
						data5.setFte3(y, data5.getFte3(y) + val);
					}
					val = row.getFte4(yearID);
					if (val != null && val > 0) {
						data0.setFte4(y, data0.getFte4(y) + val);
						data1.setFte4(y, data1.getFte4(y) + val);
						data2.setFte4(y, data2.getFte4(y) + val);
						data3.setFte4(y, data3.getFte4(y) + val);
						data4.setFte4(y, data4.getFte4(y) + val);
						data5.setFte4(y, data5.getFte4(y) + val);
					}
					val = row.getFte5(yearID);
					if (val != null && val > 0) {
						data0.setFte5(y, data0.getFte5(y) + val);
						data1.setFte5(y, data1.getFte5(y) + val);
						data2.setFte5(y, data2.getFte5(y) + val);
						data3.setFte5(y, data3.getFte5(y) + val);
						data4.setFte5(y, data4.getFte5(y) + val);
						data5.setFte5(y, data5.getFte5(y) + val);
					}
					yearID++;
				}
			}
			for (int i = 0; i < data0.getNoYears(); i++) {
				data0.setFte1(i, data0.getFte1(i) / fte1);
				data0.setFte2(i, data0.getFte2(i) / fte2);
				data0.setFte3(i, data0.getFte3(i) / fte3);
				data0.setFte4(i, data0.getFte4(i) / fte4);
				data0.setFte5(i, data0.getFte5(i) / fte5);
			}
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			setTotals(data0, fte1, fte2, fte3, fte4, fte5);
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			
			ModelGroup model = (ModelGroup) tree.getModel();
			ForecastNode node0 = (ForecastNode) model.getRoot();
			setModel(data0, node0);
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
			forecast(node0);
			try {
				Thread.sleep(LibConstants.SLEEP_TIME);
			} catch (InterruptedException ignore) {
			}
		}
	}
}