package ca.powerj;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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

class NFacililty extends NBase {
	private int rowIndex = 0;
	private String[] columns = { "ID", "FLOW", "LOAD", "NAME", "DESCR" };
	private OFacility facility = new OFacility();
	private ArrayList<OFacility> list = new ArrayList<OFacility>();
	private ModelFacility model;
	private ITable tbl;
	private ITextString txtName;
	private JLabel lblDescr;
	private JCheckBox ckbWorkflow, ckbWorkload;

	NFacililty(AClient parent) {
		super(parent);
		setName("Facilities");
		parent.dbPowerJ.prepareStpFacilities();
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			list.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelFacility();
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
		JPanel pnlData = new JPanel();
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtName = new ITextString(2, 4);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name: ");
		label.setLabelFor(txtName);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtName, 1, 0, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		// Read only, cannot be edited
		lblDescr = IGUI.createJLabel(SwingConstants.LEFT, 0, "");
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Description:");
		IGUI.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblDescr, 1, 1, 3, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		ckbWorkflow = new JCheckBox("Workflow");
		ckbWorkflow.setMnemonic(KeyEvent.VK_F);
		ckbWorkflow.setFont(LConstants.APP_FONT);
		ckbWorkflow.addFocusListener(this);
		ckbWorkflow.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					facility.workflow = ckbWorkflow.isSelected();
					altered = true;
				}
			}
		});
		ckbWorkload = new JCheckBox("Workload");
		ckbWorkload.setMnemonic(KeyEvent.VK_L);
		ckbWorkload.setFont(LConstants.APP_FONT);
		ckbWorkload.addFocusListener(this);
		ckbWorkload.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					facility.workload = ckbWorkload.isSelected();
					altered = true;
				}
			}
		});
		IGUI.addComponent(ckbWorkflow, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IGUI.addComponent(ckbWorkload, 1, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		pnlData.setMinimumSize(new Dimension(500, 300));
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		add(pnlSplit);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_FAC_SELECT);
		try {
			while (rst.next()) {
				facility = new OFacility();
				facility.facID = rst.getShort("FAID");
				facility.name = rst.getString("FANM").trim();
				facility.descr = rst.getString("FADC").trim();
				facility.workflow = (rst.getString("FAFL").equalsIgnoreCase("Y"));
				facility.workload = (rst.getString("FALD").equalsIgnoreCase("Y"));
				list.add(facility);
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("facilities.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1, 1, 2, 4 };
		String str = "Facilities - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
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
					case 0:
						paragraph.add(new Chunk(pj.numbers.formatNumber(list.get(i).facID)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 1:
						paragraph.add(new Chunk((list.get(i).workflow ? "Y" : "N")));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case 2:
						paragraph.add(new Chunk((list.get(i).workload ? "Y" : "N")));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case 3:
						paragraph.add(new Chunk(list.get(i).name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(list.get(i).descr));
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
		pj.dbPowerJ.setString(DPowerJ.STM_FAC_UPDATE, 1, (facility.workflow ? "Y" : "N"));
		pj.dbPowerJ.setString(DPowerJ.STM_FAC_UPDATE, 2, (facility.workload ? "Y" : "N"));
		pj.dbPowerJ.setString(DPowerJ.STM_FAC_UPDATE, 3, facility.name.trim());
		pj.dbPowerJ.setString(DPowerJ.STM_FAC_UPDATE, 4, facility.descr.trim());
		pj.dbPowerJ.setShort(DPowerJ.STM_FAC_UPDATE, 5, facility.facID);
		if (pj.dbPowerJ.execute(DPowerJ.STM_FAC_UPDATE) > 0) {
			altered = false;
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		facility = list.get(rowIndex);
		txtName.setText(facility.name);
		lblDescr.setText(facility.descr);
		ckbWorkflow.setSelected(facility.workflow);
		ckbWorkload.setSelected(facility.workload);
		programmaticChange = false;
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("facilities.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Facilities");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Facilities - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length - 1));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col < columns.length - 1; col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellValue(columns[col + 1]);
				xlsCell.setCellStyle(styles.get("header"));
				switch (col) {
				case 0:
					sheet.setColumnWidth(col, 5 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case 1:
				case 2:
					sheet.setColumnWidth(col, 5 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case 3:
					sheet.setColumnWidth(col, 16 * 256);
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
				for (int col = 0; col < columns.length - 1; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case 0:
						xlsCell.setCellValue(list.get(i).facID);
						break;
					case 1:
						xlsCell.setCellValue((list.get(i).workflow ? "Y" : "N"));
						break;
					case 2:
						xlsCell.setCellValue((list.get(i).workload ? "Y" : "N"));
						break;
					case 3:
						xlsCell.setCellValue(list.get(i).name);
						break;
					default:
						xlsCell.setCellValue(list.get(i).descr);
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

	private class ModelFacility extends ITableModel {

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