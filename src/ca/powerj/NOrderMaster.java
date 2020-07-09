package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

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

class NOrderMaster extends NBase {
	private final byte ORM_NO = 0;
	private final byte ORM_NAME = 1;
	private final byte ORM_DESCR = 2;
	// private final byte ORM_GROUP = 3;
	private final String[] columns = { "NO", "NAME", "DESCR", "GROUP" };
	private int rowIndex = 0;
	private OOrderMaster ordermaster = new OOrderMaster();
	private ArrayList<OOrderMaster> list = new ArrayList<OOrderMaster>();
	private HashMap<Short, String> mapGroup = new HashMap<Short, String>();
	private ModelOrdMstr model;
	private ITable tbl;
	private JLabel lblName, lblDescr;
	private IComboBox cboGroup;
	private JTextArea txtGroup;

	NOrderMaster(AClient parent) {
		super(parent);
		setName("Orders");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_ORDERMASTER);
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			list.clear();
			mapGroup.clear();
		}
		return !altered;
	}

	private void createPanel() {
		mapGroup = pj.dbPowerJ.getOrderGroupMap(pjStms.get(DPowerJ.STM_ORG_SELECT));
		model = new ModelOrdMstr();
		tbl = new ITable(pj, model);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		tbl.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				if (index > -1) {
					// else, Selection got filtered away.
					setRow(tbl.convertRowIndexToModel(index));
				}
			}
		});
		tbl.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IGUI.createJScrollPane(tbl);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 300));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		// Read only, cannot be edited
		lblName = IGUI.createJLabel(SwingConstants.LEFT, 0, "");
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblName, 1, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblDescr = IGUI.createJLabel(SwingConstants.LEFT, 0, "");
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Descr:");
		IGUI.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblDescr, 1, 1, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboGroup = new IComboBox();
		cboGroup.setName("cboGroup");
		cboGroup.setModel(pj.dbPowerJ.getOrderGroupArray(false));
		cboGroup.addFocusListener(this);
		cboGroup.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordermaster.grpID = cb.getIndex();
						altered = true;
						txtGroup.setText(mapGroup.get(ordermaster.grpID));
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_G, "Group:");
		label.setLabelFor(cboGroup);
		IGUI.addComponent(label, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboGroup, 1, 2, 2, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtGroup = new JTextArea();
		txtGroup.setName("txtGroup");
		txtGroup.setMargin(new Insets(4, 4, 4, 4));
		txtGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtGroup.setFont(LConstants.APP_FONT);
		txtGroup.setEditable(false);
		txtGroup.setFocusable(false);
		txtGroup.setLineWrap(true);
		txtGroup.setWrapStyleWord(true);
		JScrollPane scrollOrdGrp = IGUI.createJScrollPane(txtGroup);
		IGUI.addComponent(scrollOrdGrp, 0, 3, 3, 3, 1.0, 0.5, GridBagConstraints.BOTH, GridBagConstraints.EAST,
				pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		setLayout(new BorderLayout());
		add(new IToolBar(this), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ORM_SELECT));
		try {
			while (rst.next()) {
				ordermaster = new OOrderMaster();
				ordermaster.ordID = rst.getShort("omid");
				ordermaster.grpID = rst.getShort("ogid");
				ordermaster.name = rst.getString("omnm").trim();
				ordermaster.descr = rst.getString("omdc").trim();
				list.add(ordermaster);
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("ordermaster.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1.5f, 4, 4, 4 };
		String str = "Orders Master - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
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
			int i = 0;
			for (int row = 0; row < tbl.getRowCount(); row++) {
				i = tbl.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case ORM_NO:
						paragraph.add(new Chunk(pj.numbers.formatNumber(list.get(i).ordID)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case ORM_NAME:
						paragraph.add(new Chunk(list.get(i).name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORM_DESCR:
						paragraph.add(new Chunk(list.get(i).descr));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(cboGroup.getItemName(list.get(i).grpID)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
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
	void save() {
		pj.dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ORM_UPDATE), 1, ordermaster.grpID);
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_ORM_UPDATE), 2, ordermaster.name.trim());
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_ORM_UPDATE), 3, ordermaster.descr.trim());
		pj.dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ORM_UPDATE), 4, ordermaster.ordID);
		if (pj.dbPowerJ.execute(pjStms.get(DPowerJ.STM_ORM_UPDATE)) > 0) {
			altered = false;
		}
	}

	@Override
	void setFilter(short id, short value) {
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		if (id == IToolBar.TB_ORG && value >= 0) {
			final short v = value;
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).grpID == v);
				}
			};
		}
		TableRowSorter<ModelOrdMstr> sorter = (TableRowSorter<ModelOrdMstr>) tbl.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		ordermaster = list.get(rowIndex);
		lblName.setText(ordermaster.name);
		lblDescr.setText(ordermaster.descr);
		cboGroup.setIndex(ordermaster.grpID);
		txtGroup.setText(mapGroup.get(ordermaster.grpID));
		programmaticChange = false;
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("ordermaster.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Orders Master");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					"Orders Master - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
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
				case ORM_NO:
					sheet.setColumnWidth(col, 5 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case ORM_NAME:
					sheet.setColumnWidth(col, 15 * 256); // 15 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				default:
					sheet.setColumnWidth(col, 30 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				}
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < tbl.getRowCount(); row++) {
				i = tbl.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case ORM_NO:
						xlsCell.setCellValue(list.get(i).ordID);
						break;
					case ORM_NAME:
						xlsCell.setCellValue(list.get(i).name);
						break;
					case ORM_DESCR:
						xlsCell.setCellValue(list.get(i).descr);
						break;
					default:
						xlsCell.setCellValue(cboGroup.getItemName(list.get(i).grpID));
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

	private class ModelOrdMstr extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (list.size() > 0 && row < list.size()) {
				value = list.get(row).name;
			}
			return value;
		}
	}
}