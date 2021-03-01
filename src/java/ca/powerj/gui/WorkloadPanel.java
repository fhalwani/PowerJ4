package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import ca.powerj.data.WorkloadData;
import ca.powerj.data.WorkloadList;
import ca.powerj.data.WorkloadNode;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IChartPie;
import ca.powerj.swing.IRowPanel;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.ITreeModel;
import ca.powerj.swing.ITreeTable;
import ca.powerj.swing.ITreeTableModel;
import ca.powerj.swing.IUtilities;

class WorkloadPanel extends BasePanel {
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

	WorkloadPanel(AppFrame parent) {
		super(parent);
		setName("Workload");
		parent.dbPowerJ.setStatements(LibConstants.ACTION_DISTRIBUTE);
		columns[5] = application.getProperty("coder1");
		columns[6] = application.getProperty("coder2");
		columns[7] = application.getProperty("coder3");
		columns[8] = application.getProperty("coder4");
		columns[9] = application.getProperty("coder5");
		if (application.userAccess[LibConstants.ACCESS_NAMES]) {
			rowsView[0] = IRowPanel.ROW_FACILITY;
			rowsView[1] = IRowPanel.ROW_SPECIALTY;
			rowsView[2] = IRowPanel.ROW_SUBSPECIAL;
			rowsView[3] = IRowPanel.ROW_PROCEDURE;
			rowsView[4] = IRowPanel.ROW_STAFF;
		} else {
			rowsView[0] = IRowPanel.ROW_STAFF;
			rowsView[1] = IRowPanel.ROW_FACILITY;
			rowsView[2] = IRowPanel.ROW_SPECIALTY;
			rowsView[3] = IRowPanel.ROW_SUBSPECIAL;
			rowsView[4] = IRowPanel.ROW_PROCEDURE;
		}
		createPanel();
		programmaticChange = false;
		altered = true;
	}

	@Override
	public boolean close() {
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
		WorkloadNode root = new WorkloadNode("Total");
		ModelWorkload model = new ModelWorkload(root);
		tree = new ITreeTable(model, application.numbers);
		tree.getTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				if (!tree.isBusy()) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setCharts(e.getNewLeadSelectionPath());
						}
					});
				}
			}
		});
		JScrollPane scrollTree = IUtilities.createJScrollPane(tree);
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
		add(new IToolBar(application, calStart, calEnd, calMin, calMax, rowsView), BorderLayout.NORTH);
		add(splitBottom, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("workload.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 5, 1.5f, 1.5f, 1.5f, 1.5f, 1, 1, 1, 1, 1 };
		String str = "Workload " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATE)
			+ " - " + application.dates.formatter(timeTo, LibDates.FORMAT_DATE);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER, 36, 18, 18, 18);
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
			WorkloadNode root = (WorkloadNode) tree.getTreeTableModel().getRoot();
			pdfRow(root, pdfTable, paragraph, cell, fonts.get("Font10n"), "");
			document.add(pdfTable);
			document.close();
		} catch (DocumentException e) {
			application.log(LibConstants.ERROR_IO, getName(), e);
		} catch (FileNotFoundException e) {
			application.log(LibConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		}
	}

	private void pdfRow(WorkloadNode parent, PdfPTable pdfTable, Paragraph paragraph, PdfPCell cell, Font font, String name) {
		for (int col = 0; col < columns.length; col++) {
			paragraph = new Paragraph();
			paragraph.setFont(font);
			cell = new PdfPCell();
			switch (col) {
			case DATA_GROUPS:
				if (name.length() == 0) {
					paragraph.add(new Chunk(parent.getName()));
					name = "/";
				} else if (name.equals("/")) {
					paragraph.add(new Chunk(parent.getName()));
					name = parent.getName();
				} else {
					name += "/" + parent.getName();
					paragraph.add(new Chunk(name));
				}
				paragraph.setAlignment(Element.ALIGN_LEFT);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				break;
			case DATA_CASES:
				paragraph.add(new Chunk(application.numbers.formatNumber(parent.getNoCases())));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_SPECS:
				paragraph.add(new Chunk(application.numbers.formatNumber(parent.getNoSpecs())));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_BLOCKS:
				paragraph.add(new Chunk(application.numbers.formatNumber(parent.getNoBlocks())));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_SLIDES:
				paragraph.add(new Chunk(application.numbers.formatNumber(parent.getNoSlides())));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_VALUE1:
				paragraph.add(new Chunk(application.numbers.formatDouble(2, parent.getFte1())));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_VALUE2:
				paragraph.add(new Chunk(application.numbers.formatDouble(2, parent.getFte2())));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_VALUE3:
				paragraph.add(new Chunk(application.numbers.formatDouble(2, parent.getFte3())));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			case DATA_VALUE4:
				paragraph.add(new Chunk(application.numbers.formatDouble(2, parent.getFte4())));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				break;
			default:
				paragraph.add(new Chunk(application.numbers.formatDouble(2, parent.getFte5())));
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			}
			cell.addElement(paragraph);
			pdfTable.addCell(cell);

		}
		for (int i = 0; i < parent.getNoChildren(); i++) {
			WorkloadNode child = (WorkloadNode) parent.getChild(i);
			pdfRow(child, pdfTable, paragraph, cell, font, name);
		}
	}

	private void setCharts(TreePath newPath) {
		if (newPath != null && (treePath == null || !treePath.equals(newPath))) {
			int count = 1;
			double[] yCases = new double[count];
			double[] yCoder1 = new double[count];
			double[] yCoder2 = new double[count];
			double[] yCoder3 = new double[count];
			double[] yCoder4 = new double[count];
			double[] yCoder5 = new double[count];
			String[] xTitles = new String[count];
			treePath = newPath;
			WorkloadNode node = (WorkloadNode) treePath.getPathComponent(treePath.getPathCount() - 1);
			if (node != null && node.getNoChildren() > 0) {
				count = node.getNoChildren();
				xTitles = new String[count];
				yCases = new double[count];
				yCoder1 = new double[count];
				yCoder2 = new double[count];
				yCoder3 = new double[count];
				yCoder4 = new double[count];
				yCoder5 = new double[count];
				for (int i = 0; i < count; i++) {
					WorkloadNode leaf = (WorkloadNode) node.getChild(i);
					if (leaf.getName().length() > 4) {
						xTitles[i] = leaf.getName().substring(0, 4);
					} else {
						xTitles[i] = leaf.getName();
					}
					yCases[i] = leaf.getNoCases();
					yCoder1[i] = leaf.getFte1();
					yCoder2[i] = leaf.getFte2();
					yCoder3[i] = leaf.getFte3();
					yCoder4[i] = leaf.getFte4();
					yCoder5[i] = leaf.getFte5();
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
	public void setFilter(int[] values) {
		for (int i = 0; i < rowsView.length; i++) {
			rowsView[i] = (byte) values[i];
		}
		altered = true;
	}

	@Override
	public void setFilter(short id, int value) {
		// Go button
		if (altered && timeTo > timeFrom) {
			application.setBusy(true);
			WorkerData worker = new WorkerData();
			worker.execute();
			altered = false;
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
	void xls() {
		String fileName = application.getFileXls("workload.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Workload");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Workload " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATE)
				+ " - " + application.dates.formatter(timeTo, LibDates.FORMAT_DATE));
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
			WorkloadNode root = (WorkloadNode) tree.getTreeTableModel().getRoot();
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

	private int xlsRow(WorkloadNode parent, Sheet sheet, Cell xlsCell, String name, int rownum) {
		Row xlsRow = sheet.createRow(rownum++);
		for (int col = 0; col < columns.length; col++) {
			xlsCell = xlsRow.createCell(col);
			switch (col) {
			case DATA_GROUPS:
				if (name.length() == 0) {
					xlsCell.setCellValue(parent.getName());
					name = "/";
				} else if (name.equals("/")) {
					xlsCell.setCellValue(parent.getName());
					name = parent.getName();
				} else {
					name += "/" + parent.getName();
					xlsCell.setCellValue(name);
				}
				break;
			case DATA_CASES:
				xlsCell.setCellValue(parent.getNoCases());
				break;
			case DATA_SPECS:
				xlsCell.setCellValue(parent.getNoSpecs());
				break;
			case DATA_BLOCKS:
				xlsCell.setCellValue(parent.getNoBlocks());
				break;
			case DATA_SLIDES:
				xlsCell.setCellValue(parent.getNoSlides());
				break;
			case DATA_VALUE1:
				xlsCell.setCellValue(parent.getFte1());
				break;
			case DATA_VALUE2:
				xlsCell.setCellValue(parent.getFte2());
				break;
			case DATA_VALUE3:
				xlsCell.setCellValue(parent.getFte3());
				break;
			case DATA_VALUE4:
				xlsCell.setCellValue(parent.getFte4());
				break;
			default:
				xlsCell.setCellValue(parent.getFte5());
			}
		}
		for (int i = 0; i < parent.getNoChildren(); i++) {
			WorkloadNode child = (WorkloadNode) parent.getChild(i);
			rownum = xlsRow(child, sheet, xlsCell, name, rownum);
		}
		return rownum;
	}

	private class ModelWorkload extends ITreeTableModel implements ITreeModel {

		public ModelWorkload(Object nodeRoot) {
			super(nodeRoot);
		}

		@Override
		public Object getChild(Object node, int elem) {
			return ((WorkloadNode) node).getChild(elem);
		}

		@Override
		public int getChildCount(Object node) {
			Object[] children = getChildren(node);
			return (children == null) ? 0 : children.length;
		}

		protected Object[] getChildren(Object node) {
			return ((WorkloadNode) node).getChildren();
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
			WorkloadNode data = (WorkloadNode) node;
			switch (col) {
			case DATA_GROUPS:
				return data.getName();
			case DATA_CASES:
				return data.getNoCases();
			case DATA_SPECS:
				return data.getNoSpecs();
			case DATA_BLOCKS:
				return data.getNoBlocks();
			case DATA_SLIDES:
				return data.getNoSlides();
			case DATA_VALUE1:
				return data.getFte1();
			case DATA_VALUE2:
				return data.getFte2();
			case DATA_VALUE3:
				return data.getFte3();
			case DATA_VALUE4:
				return data.getFte4();
			case DATA_VALUE5:
				return data.getFte5();
			default:
				return null;
			}
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {
		private int noRows = 0;
		private ArrayList<WorkloadData> rows = new ArrayList<WorkloadData>();

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
			WorkloadNode root = (WorkloadNode) tree.getTreeTableModel().getRoot();
			ModelWorkload model = new ModelWorkload(root);
			tree.setTreeTableModel(model);
			setCharts(tree.getTree().getPathForRow(0));
			application.display("Workload " + application.dates.formatter(timeFrom, LibDates.FORMAT_DATELONG) + " - "
					+ application.dates.formatter(timeTo, LibDates.FORMAT_DATELONG));
			application.setBusy(false);
		}

		private void getData() {
			boolean exists = false;
			WorkloadData row = new WorkloadData();
			rows = application.dbPowerJ.getCaseSums(application.userAccess[LibConstants.ACCESS_NAMES],
					application.getUserID(), timeFrom, timeTo);
			// Frozen Section
			ArrayList<WorkloadData> tempList = application.dbPowerJ.getFrozenSums(application.userAccess[LibConstants.ACCESS_NAMES],
					application.getUserID(), timeFrom, timeTo);
			for (int i = 0; i < tempList.size(); i++) {
				exists = false;
				for (int j = 0; j < rows.size(); j++) {
					if (rows.get(j).getFacID() == tempList.get(i).getFacID()
							&& rows.get(j).getSpyID() == tempList.get(i).getSpyID()
							&& rows.get(j).getSubID() == tempList.get(i).getSubID()
							&& rows.get(j).getProID() == tempList.get(i).getProID()
							&& rows.get(j).getPrsID() == tempList.get(i).getPrsID()) {
						row = rows.get(j);
						exists = true;
						break;
					}
				}
				if (!exists) {
					row = new WorkloadData();
					row.setSpyID(tempList.get(i).getSpyID());
					row.setSubID(tempList.get(i).getSubID());
					row.setProID(tempList.get(i).getProID());
					row.setFacID(tempList.get(i).getFacID());
					row.setPrsID(tempList.get(i).getPrsID());
					row.setFacName(tempList.get(i).getFacName());
					row.setSpyName(tempList.get(i).getSpyName());
					row.setSubName(tempList.get(i).getSubName());
					row.setProName(tempList.get(i).getProName());
					row.setPrsName(tempList.get(i).getPrsName());
					rows.add(row);
				}
				row.setFte1(row.getFte1() + tempList.get(i).getFte1());
				row.setFte2(row.getFte2() + tempList.get(i).getFte2());
				row.setFte3(row.getFte3() + tempList.get(i).getFte3());
				row.setFte4(row.getFte4() + tempList.get(i).getFte4());
				row.setFte5(row.getFte5() + tempList.get(i).getFte5());
			}
			tempList.clear();
			// Additional
			tempList = application.dbPowerJ.getAdditionalSums(timeFrom, timeTo);
			for (int i = 0; i < tempList.size(); i++) {
				exists = false;
				for (int j = 0; j < rows.size(); j++) {
					if (rows.get(j).getFacID() == tempList.get(i).getFacID()
							&& rows.get(j).getSpyID() == tempList.get(i).getSpyID()
							&& rows.get(j).getSubID() == tempList.get(i).getSubID()
							&& rows.get(j).getProID() == tempList.get(i).getProID()
							&& rows.get(j).getPrsID() == tempList.get(i).getPrsID()) {
						row = rows.get(j);
						exists = true;
						break;
					}
				}
				if (exists) {
					// else, not a pathologist
					row.setFte1(row.getFte1() + tempList.get(i).getFte1());
					row.setFte2(row.getFte2() + tempList.get(i).getFte2());
					row.setFte3(row.getFte3() + tempList.get(i).getFte3());
					row.setFte4(row.getFte4() + tempList.get(i).getFte4());
					row.setFte5(row.getFte5() + tempList.get(i).getFte5());
				}
			}
			tempList.clear();
		}

		private void setModel(WorkloadList item, WorkloadNode node) {
			node.setNoCases(item.getNoCases());
			node.setNoSpecs(item.getNoSpecs());
			node.setNoBlocks(item.getNoBlocks());
			node.setNoSlides(item.getNoSlides());
			node.setFte1(item.getFte1());
			node.setFte2(item.getFte2());
			node.setFte3(item.getFte3());
			node.setFte4(item.getFte4());
			node.setFte5(item.getFte5());
			node.setChildren(item.getNoChildren());
			for (int i = 0; i < item.getNoChildren(); i++) {
				WorkloadList childItem = item.getChild(i);
				WorkloadNode childNode = new WorkloadNode(childItem.getName());
				node.setChild(i, childNode);
				setModel(childItem, childNode);
			}
		}

		private void setTotals(WorkloadList item, double fte1, double fte2, double fte3, double fte4, double fte5) {
			WorkloadList child = new WorkloadList();
			for (int i = item.getNoChildren() -1; i >= 0; i--) {
				child = item.getChild(i);
				if (child.getId() < 0) {
					// filtered out
					item.getChildren().remove(i);
					continue;
				}
				child.setFte1(child.getFte1() / fte1);
				child.setFte2(child.getFte2() / fte2);
				child.setFte3(child.getFte3() / fte3);
				child.setFte4(child.getFte4() / fte4);
				child.setFte5(child.getFte5() / fte5);
				if (child.getNoChildren() > 0) {
					setTotals(child, fte1, fte2, fte3, fte4, fte5);
				}
			}
		}

		private void sortChildren(WorkloadList master) {
			Collections.sort(master.getChildren(), new Comparator<WorkloadList>() {
				@Override
				public int compare(WorkloadList o1, WorkloadList o2) {
					return (o1.getFte5() > o2.getFte5() ? -1
							: (o1.getFte5() < o2.getFte5() ? 1 
							: (o1.getNoSpecs() > o2.getNoSpecs() ? -1
							: (o1.getNoSpecs() < o2.getNoSpecs() ? 1 : 0))));
				}
			});
			int nameID = 0;
			WorkloadList child = new WorkloadList();
			for (int i = 0; i < master.getNoChildren(); i++) {
				child = master.getChild(i);
				if (!application.userAccess[LibConstants.ACCESS_NAMES]) {
					if (child.getName().trim().length() == 0) {
						nameID = 1;
						noRows++;
						child.setName("P" + noRows);
					} else if (nameID == 1) {
						noRows++;
					}
				}
				if (child.getNoChildren() > 1) {
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
				int noDays = application.dates.getNoDays(timeFrom, timeTo);
				double fte1 = application.setup.getShort(LibSetup.VAR_CODER1_FTE) * noDays / 365.0;
				double fte2 = application.setup.getShort(LibSetup.VAR_CODER2_FTE) * noDays / 365.0;
				double fte3 = application.setup.getShort(LibSetup.VAR_CODER3_FTE) * noDays / 365.0;
				double fte4 = application.setup.getShort(LibSetup.VAR_CODER4_FTE) * noDays / 365.0;
				double fte5 = application.setup.getInt(LibSetup.VAR_V5_FTE) * noDays / 365.0;
				String name = "";
				WorkloadList data0 = new WorkloadList();
				WorkloadList data1 = new WorkloadList();
				WorkloadList data2 = new WorkloadList();
				WorkloadList data3 = new WorkloadList();
				WorkloadList data4 = new WorkloadList();
				WorkloadList data5 = new WorkloadList();
				WorkloadData row = new WorkloadData();
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
					case IRowPanel.ROW_STAFF:
						id = row.getPrsID();
						name = row.getPrsName();
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
							data1 = new WorkloadList();
							data1.setId(ids[0]);
							data1.setName(name);
							data0.setChild(data1);
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
					case IRowPanel.ROW_STAFF:
						id = row.getPrsID();
						name = row.getPrsName();
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
							data2 = new WorkloadList();
							data2.setId(ids[1]);
							data2.setName(name);
							data1.setChild(data2);
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
					case IRowPanel.ROW_STAFF:
						id = row.getPrsID();
						name = row.getPrsName();
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
							data3 = new WorkloadList();
							data3.setId(ids[2]);
							data3.setName(name);
							data2.setChild(data3);
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
					case IRowPanel.ROW_STAFF:
						id = row.getPrsID();
						name = row.getPrsName();
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
							data4 = new WorkloadList();
							data4.setId(ids[3]);
							data4.setName(name);
							data3.setChild(data4);
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
					case IRowPanel.ROW_STAFF:
						id = row.getPrsID();
						name = row.getPrsName();
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
							data5 = new WorkloadList();
							data5.setId(ids[4]);
							data5.setName(name);
							data4.setChild(data5);
						}
					}
					data5.setNoCases(data5.getNoCases() + row.getNoCases());
					data4.setNoCases(data4.getNoCases() + row.getNoCases());
					data3.setNoCases(data3.getNoCases() + row.getNoCases());
					data2.setNoCases(data2.getNoCases() + row.getNoCases());
					data1.setNoCases(data1.getNoCases() + row.getNoCases());
					data0.setNoCases(data0.getNoCases() + row.getNoCases());
					data5.setNoSpecs(data5.getNoSpecs() + row.getNoSpecs());
					data4.setNoSpecs(data4.getNoSpecs() + row.getNoSpecs());
					data3.setNoSpecs(data3.getNoSpecs() + row.getNoSpecs());
					data2.setNoSpecs(data2.getNoSpecs() + row.getNoSpecs());
					data1.setNoSpecs(data1.getNoSpecs() + row.getNoSpecs());
					data0.setNoSpecs(data0.getNoSpecs() + row.getNoSpecs());
					data5.setNoBlocks(data5.getNoBlocks() + row.getNoBlocks());
					data4.setNoBlocks(data4.getNoBlocks() + row.getNoBlocks());
					data3.setNoBlocks(data3.getNoBlocks() + row.getNoBlocks());
					data2.setNoBlocks(data2.getNoBlocks() + row.getNoBlocks());
					data1.setNoBlocks(data1.getNoBlocks() + row.getNoBlocks());
					data0.setNoBlocks(data0.getNoBlocks() + row.getNoBlocks());
					data5.setNoSlides(data5.getNoSlides() + row.getNoSlides());
					data4.setNoSlides(data4.getNoSlides() + row.getNoSlides());
					data3.setNoSlides(data3.getNoSlides() + row.getNoSlides());
					data2.setNoSlides(data2.getNoSlides() + row.getNoSlides());
					data1.setNoSlides(data1.getNoSlides() + row.getNoSlides());
					data0.setNoSlides(data0.getNoSlides() + row.getNoSlides());
					data5.setFte1(data5.getFte1() + row.getFte1());
					data4.setFte1(data4.getFte1() + row.getFte1());
					data3.setFte1(data3.getFte1() + row.getFte1());
					data2.setFte1(data2.getFte1() + row.getFte1());
					data1.setFte1(data1.getFte1() + row.getFte1());
					data0.setFte1(data0.getFte1() + row.getFte1());
					data5.setFte2(data5.getFte2() + row.getFte2());
					data4.setFte2(data4.getFte2() + row.getFte2());
					data3.setFte2(data3.getFte2() + row.getFte2());
					data2.setFte2(data2.getFte2() + row.getFte2());
					data1.setFte2(data1.getFte2() + row.getFte2());
					data0.setFte2(data0.getFte2() + row.getFte2());
					data5.setFte3(data5.getFte3() + row.getFte3());
					data4.setFte3(data4.getFte3() + row.getFte3());
					data3.setFte3(data3.getFte3() + row.getFte3());
					data2.setFte3(data2.getFte3() + row.getFte3());
					data1.setFte3(data1.getFte3() + row.getFte3());
					data0.setFte3(data0.getFte3() + row.getFte3());
					data5.setFte4(data5.getFte4() + row.getFte4());
					data4.setFte4(data4.getFte4() + row.getFte4());
					data3.setFte4(data3.getFte4() + row.getFte4());
					data2.setFte4(data2.getFte4() + row.getFte4());
					data1.setFte4(data1.getFte4() + row.getFte4());
					data0.setFte4(data0.getFte4() + row.getFte4());
					data5.setFte5(data5.getFte5() + row.getFte5());
					data4.setFte5(data4.getFte5() + row.getFte5());
					data3.setFte5(data3.getFte5() + row.getFte5());
					data2.setFte5(data2.getFte5() + row.getFte5());
					data1.setFte5(data1.getFte5() + row.getFte5());
					data0.setFte5(data0.getFte5() + row.getFte5());
				}
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				data0.setFte1(data0.getFte1() / fte1);
				data0.setFte2(data0.getFte2() / fte2);
				data0.setFte3(data0.getFte3() / fte3);
				data0.setFte4(data0.getFte4() / fte4);
				data0.setFte5(data0.getFte5() / fte5);
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
				setTotals(data0, fte1, fte2, fte3, fte4, fte5);
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
				if (data0.getNoChildren() > 1) {
					sortChildren(data0);
				}
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
				WorkloadNode node0 = (WorkloadNode) tree.getTreeTableModel().getRoot();
				setModel(data0, node0);
				try {
					Thread.sleep(LibConstants.SLEEP_TIME);
				} catch (InterruptedException ignore) {
				}
			} catch (Exception e) {
				application.log(LibConstants.ERROR_UNEXPECTED, getName(), e);
			}
		}
	}
}