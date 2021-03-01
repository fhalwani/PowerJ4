package ca.powerj.gui;
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
import ca.powerj.data.RuleData;
import ca.powerj.data.CoderData;
import ca.powerj.data.ItemData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextDouble;
import ca.powerj.swing.ITextInteger;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class CoderPanel extends BasePanel {
	private byte coderID = 0;
	private short newID = 0;
	private int rowIndex = 0;
	private final String[] columns = { "CODE", "RULE", "QTY", "VAL1", "VAL2", "VAL3", "NAME", "DESCR" };
	private CoderData coder = new CoderData();
	private ArrayList<CoderData> list = new ArrayList<CoderData>();
	private HashMap<Short, RuleData> mapRules = new HashMap<Short, RuleData>();
	private ITable tbl;
	private ITextString txtName;
	private ITextInteger txtCount;
	private ITextDouble txtValueA, txtValueB, txtValueC;
	private IComboBox cboRules;
	private JTextArea txtDescr, txtRule;
	private ModelCoder model;

	CoderPanel(AppFrame application, byte coderID) {
		super(application);
		this.coderID = coderID;
		switch (coderID) {
		case LibConstants.ACTION_CODER1:
			setName(application.getProperty("coder1"));
			break;
		case LibConstants.ACTION_CODER2:
			setName(application.getProperty("coder2"));
			break;
		case LibConstants.ACTION_CODER3:
			setName(application.getProperty("coder3"));
			break;
		case LibConstants.ACTION_CODER4:
			setName(application.getProperty("coder4"));
			break;
		default:
		}
		application.dbPowerJ.setStatements(coderID);
		createPanel();
		getData(coderID);
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			list.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelCoder();
		tbl = new ITable(model, application.dates, application.numbers);
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		tbl.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				if (index > -1) {
					setRow(tbl.convertRowIndexToModel(index));
				}
			}
		});
		tbl.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IUtilities.createJScrollPane(tbl);
		scrollTable.setMinimumSize(new Dimension(200, 500));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 500));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtName = new ITextString(2, 16);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(txtName, 1, 0, 1, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtDescr = new JTextArea();
		txtDescr.setName("Descr");
		txtDescr.setMargin(new Insets(4, 4, 4, 4));
		txtDescr.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtDescr.setFont(LibConstants.APP_FONT);
		txtDescr.getDocument().addDocumentListener(this);
		txtDescr.addFocusListener(this);
		txtDescr.setLineWrap(true);
		txtDescr.setWrapStyleWord(true);
		JScrollPane scrollText = IUtilities.createJScrollPane(txtDescr);
		IUtilities.addComponent(scrollText, 0, 1, 2, 2, 0.9, 0.3, GridBagConstraints.BOTH, GridBagConstraints.EAST,
				pnlData);
		cboRules = new IComboBox();
		cboRules.setName("Rules");
		cboRules.setItems(getRules());
		cboRules.addFocusListener(this);
		cboRules.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						coder.setRuleID((short) cb.getIndex());
						altered = true;
						showRule();
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_R, "Rule:");
		label.setLabelFor(cboRules);
		IUtilities.addComponent(label, 0, 4, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(cboRules, 1, 4, 1, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtCount = new ITextInteger(application.numbers, 0, Byte.MAX_VALUE);
		txtCount.setName("Gross");
		txtCount.addFocusListener(this);
		txtCount.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_C, "Count:");
		label.setLabelFor(txtCount);
		IUtilities.addComponent(label, 0, 5, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(txtCount, 1, 5, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtValueA = new ITextDouble(application.numbers, 3, 0.0, 99.9);
		txtValueA.setName("ValueA");
		txtValueA.addFocusListener(this);
		txtValueA.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_1, "Value 1:");
		label.setLabelFor(txtValueA);
		IUtilities.addComponent(label, 0, 6, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(txtValueA, 1, 6, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtValueB = new ITextDouble(application.numbers, 3, 0.0, 99.9);
		txtValueB.setName("ValueB");
		txtValueB.addFocusListener(this);
		txtValueB.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_2, "Value 2:");
		label.setLabelFor(txtValueB);
		IUtilities.addComponent(label, 0, 7, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(txtValueB, 1, 7, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtValueC = new ITextDouble(application.numbers, 3, 0.0, 99.9);
		txtValueC.setName("ValueC");
		txtValueC.addFocusListener(this);
		txtValueC.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_3, "Value 3:");
		label.setLabelFor(txtValueC);
		IUtilities.addComponent(label, 0, 8, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(txtValueC, 1, 8, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtRule = new JTextArea();
		txtRule.setName("Rule");
		txtRule.setMargin(new Insets(4, 4, 4, 4));
		txtRule.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtRule.setFont(LibConstants.APP_FONT);
		txtRule.setEditable(false);
		txtRule.setFocusable(false);
		txtRule.setLineWrap(true);
		txtRule.setWrapStyleWord(true);
		JScrollPane scrollRule = IUtilities.createJScrollPane(txtRule);
		IUtilities.addComponent(scrollRule, 0, 9, 3, 3, 0.7, 0.3, GridBagConstraints.BOTH, GridBagConstraints.EAST,
				pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 500));
		add(pnlSplit);
	}

	private void getData(byte coderID) {
		list = application.dbPowerJ.getCoder(coderID);
		for (int i = 0; i < list.size(); i++) {
			if (newID < list.get(i).getCodeID()) {
				newID = list.get(i).getCodeID();
			}
		}
		model.fireTableDataChanged();
	}

	private ItemData[] getRules() {
		ArrayList<RuleData> list = application.dbPowerJ.getRules();
		ItemData[] items = new ItemData[list.size()];
		RuleData item = new RuleData();
		for (int i = 0; i < list.size(); i++) {
			item = list.get(i);
			mapRules.put(item.getID(), item);
			items[i] = new ItemData(item.getID(), item.getName());
		}
		return items;
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf(getName() + ".pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1, 1, 1, 1, 1, 2, 4 };
		String str = getName() + " - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER.rotate(), 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable table = new PdfPTable(columns.length);
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
						paragraph.add(new Chunk(application.numbers.formatNumber(list.get(i).getCodeID())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 1:
						paragraph.add(new Chunk(application.numbers.formatNumber(list.get(i).getRuleID())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 2:
						paragraph.add(new Chunk(application.numbers.formatNumber(list.get(i).getCount())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 3:
						paragraph.add(new Chunk(application.numbers.formatDouble(3, list.get(i).getValueA())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 4:
						paragraph.add(new Chunk(application.numbers.formatDouble(3, list.get(i).getValueB())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 5:
						paragraph.add(new Chunk(application.numbers.formatDouble(3, list.get(i).getValueC())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 6:
						paragraph.add(new Chunk(list.get(i).getName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(list.get(i).getDescr()));
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
			application.log(LibConstants.ERROR_IO, getName(), e);
		} catch (FileNotFoundException e) {
			application.log(LibConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		}
	}

	@Override
	void save() {
		if (coder.isNewRow()) {
			coder.setCodeID(newID);
		}
		coder.setCount((short) txtCount.getByte());
		coder.setValueA(txtValueA.getDouble());
		coder.setValueB(txtValueB.getDouble());
		coder.setValueC(txtValueC.getDouble());
		coder.setName(txtName.getText().trim());
		coder.setDescr(txtDescr.getText().trim());
		if (application.dbPowerJ.setCoder(coderID, coder) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (coder.isNewRow()) {
				coder.setNewRow(false);
				newID++;
				// Add a blank row
				CoderData newCoder = new CoderData();
				newCoder.setNewRow(true);
				list.add(newCoder);
				model.fireTableRowsInserted(list.size() - 1, list.size() - 1);

			}
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		coder = list.get(rowIndex);
		txtName.setText(coder.getName());
		txtDescr.setText(coder.getDescr());
		cboRules.setIndex(coder.getRuleID());
		txtCount.setInt(coder.getCount());
		txtValueA.setDouble(coder.getValueA());
		txtValueB.setDouble(coder.getValueB());
		txtValueC.setDouble(coder.getValueC());
		showRule();
		programmaticChange = false;
	}

	private void showRule() {
		String text = "";
		RuleData rule = mapRules.get(coder.getRuleID());
		if (rule != null) {
			text = rule.getDescription();
		}
		txtRule.setText(text);
	}

	@Override
	void xls() {
		String fileName = application.getFileXls(getName() + ".xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Backlog");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					getName() + " - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
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
				case 1:
				case 2:
					sheet.setColumnWidth(col, 5 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case 3:
				case 4:
				case 5:
					sheet.setColumnWidth(col, 6 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("data_float"));
					break;
				case 6:
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
						xlsCell.setCellValue(list.get(i).getCodeID());
						break;
					case 1:
						xlsCell.setCellValue(list.get(i).getRuleID());
						break;
					case 2:
						xlsCell.setCellValue(list.get(i).getCount());
						break;
					case 3:
						xlsCell.setCellValue(list.get(i).getValueA());
						break;
					case 4:
						xlsCell.setCellValue(list.get(i).getValueB());
						break;
					case 5:
						xlsCell.setCellValue(list.get(i).getValueC());
						break;
					case 6:
						xlsCell.setCellValue(list.get(i).getName());
						break;
					default:
						xlsCell.setCellValue(list.get(i).getDescr());
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

	private class ModelCoder extends ITableModel {

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
				value = list.get(row).getName();
			}
			return value;
		}
	}
}