package ca.powerj.gui;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import ca.powerj.data.FacilityData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class FacililtyPanel extends BasePanel {
	private int rowIndex = 0;
	private String[] columns = { "ID", "FLOW", "LOAD", "NAME", "DESCR" };
	private FacilityData facility = new FacilityData();
	private ArrayList<FacilityData> facilities = null;
	private ModelFacility model;
	private ITable table;
	private ITextString txtName;
	private JLabel lblDescr;
	private JCheckBox ckbWorkflow, ckbWorkload;

	FacililtyPanel(AppFrame application) {
		super(application);
		setName("Facilities");
		application.dbPowerJ.setStatements(LibConstants.ACTION_FACILITY);
		facilities = application.dbPowerJ.getFacilities(true);
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			facilities.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelFacility();
		table = new ITable(model, application.dates, application.numbers);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		table.addAncestorListener(new IFocusListener());
		table.addFocusListener(this);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
					setRow(table.convertRowIndexToModel(index));
				}
			}
		});
		table.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IUtilities.createJScrollPane(table);
		JPanel pnlData = new JPanel();
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtName = new ITextString(2, 4);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name: ");
		label.setLabelFor(txtName);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtName, 1, 0, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		// Read only, cannot be edited
		lblDescr = IUtilities.createJLabel(SwingConstants.LEFT, 0, "");
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Description:");
		IUtilities.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(lblDescr, 1, 1, 3, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		ckbWorkflow = new JCheckBox("Workflow");
		ckbWorkflow.setMnemonic(KeyEvent.VK_F);
		ckbWorkflow.setFont(LibConstants.APP_FONT);
		ckbWorkflow.addFocusListener(this);
		ckbWorkflow.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					facility.setWorkflow(ckbWorkflow.isSelected());
					altered = true;
				}
			}
		});
		ckbWorkload = new JCheckBox("Workload");
		ckbWorkload.setMnemonic(KeyEvent.VK_L);
		ckbWorkload.setFont(LibConstants.APP_FONT);
		ckbWorkload.addFocusListener(this);
		ckbWorkload.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					facility.setWorkload(ckbWorkload.isSelected());
					altered = true;
				}
			}
		});
		IUtilities.addComponent(ckbWorkflow, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(ckbWorkload, 1, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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

	@Override
	void pdf() {
		String fileName = application.getFilePdf("facilities.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1, 1, 2, 4 };
		String str = "Facilities - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
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
			int i = 0;
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case 0:
						paragraph.add(new Chunk(application.numbers.formatNumber(facilities.get(i).getFacID())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 1:
						paragraph.add(new Chunk((facilities.get(i).isWorkflow() ? "Y" : "N")));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case 2:
						paragraph.add(new Chunk((facilities.get(i).isWorkload() ? "Y" : "N")));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case 3:
						paragraph.add(new Chunk(facilities.get(i).getName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(facilities.get(i).getDescr()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
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
	void save() {
		if (application.dbPowerJ.setFacility(false, facility) > 0) {
			altered = false;
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		facility = facilities.get(rowIndex);
		txtName.setText(facility.getName());
		lblDescr.setText(facility.getDescr());
		ckbWorkflow.setSelected(facility.isWorkflow());
		ckbWorkload.setSelected(facility.isWorkload());
		programmaticChange = false;
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("facilities.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Facilities");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Facilities - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
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
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length - 1; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case 0:
						xlsCell.setCellValue(facilities.get(i).getFacID());
						break;
					case 1:
						xlsCell.setCellValue((facilities.get(i).isWorkflow() ? "Y" : "N"));
						break;
					case 2:
						xlsCell.setCellValue((facilities.get(i).isWorkload() ? "Y" : "N"));
						break;
					case 3:
						xlsCell.setCellValue(facilities.get(i).getName());
						break;
					default:
						xlsCell.setCellValue(facilities.get(i).getDescr());
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

	private class ModelFacility extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return facilities.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (facilities.size() > 0 && row < facilities.size()) {
				value = facilities.get(row).getName();
			}
			return value;
		}
	}
}