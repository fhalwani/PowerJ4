package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
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
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
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
import ca.powerj.data.CoderData;
import ca.powerj.data.ItemData;
import ca.powerj.data.OrderGroupData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class OrderGroupPanel extends BasePanel {
	private final byte ORG_NO = 0;
	private final byte ORG_NAME = 1;
	private final byte ORG_DESCR = 2;
	private final byte ORG_TYPE = 3;
	private final byte ORG_VAL1 = 4;
	private final byte ORG_VAL2 = 5;
	private final byte ORG_VAL3 = 6;
	private final byte ORG_VAL4 = 7;
	private final byte ORG_VAL5 = 8;
	private short newID = 0;
	private int rowIndex = 0;
	private String[] columns = { "NO", "NAME", "DESCR", "TYPE", "", "", "", "", "" };
	private OrderGroupData ordergroup = new OrderGroupData();
	private ArrayList<OrderGroupData> list = new ArrayList<OrderGroupData>();
	private HashMap<Short, String> mapCoder1 = new HashMap<Short, String>();
	private HashMap<Short, String> mapCoder2 = new HashMap<Short, String>();
	private HashMap<Short, String> mapCoder3 = new HashMap<Short, String>();
	private HashMap<Short, String> mapCoder4 = new HashMap<Short, String>();
	private ModelOrderGroup model;
	private JLabel lblValue5;
	private ITable table;
	private ITextString txtName, txtDescr;
	private IComboBox cboTypes, cboCoder1, cboCoder2, cboCoder3, cboCoder4;
	private JTextArea txtToolTip;

	OrderGroupPanel(AppFrame application) {
		super(application);
		setName("Orders");
		columns[4] = application.getProperty("coder1");
		columns[5] = application.getProperty("coder2");
		columns[6] = application.getProperty("coder3");
		columns[7] = application.getProperty("coder4");
		columns[8] = application.getProperty("coder5");
		application.dbPowerJ.setStatements(LibConstants.ACTION_ORDERGROUP);
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		trackDocument(e);
	}

	@Override
	public boolean close() {
		if (super.close()) {
			list.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelOrderGroup();
		table = new ITable(model, application.dates, application.numbers);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		table.addAncestorListener(new IFocusListener());
		table.addFocusListener(this);
		table.setName("tblList");
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
		scrollTable.setMinimumSize(new Dimension(200, 300));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 300));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtName = new ITextString(2, 8);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtName, 1, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		txtDescr = new ITextString(2, 64);
		txtDescr.setName("Descr");
		txtDescr.addFocusListener(this);
		txtDescr.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Description:");
		label.setLabelFor(txtDescr);
		IUtilities.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtDescr, 1, 1, 3, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboTypes = new IComboBox();
		cboTypes.setName("Types");
		cboTypes.setItems(application.dbPowerJ.getOrderTypes().toArray());
		cboTypes.addFocusListener(this);
		cboTypes.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.setTypeID((short) cb.getIndex());
						altered = true;
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_S, "Type:");
		label.setLabelFor(cboTypes);
		IUtilities.addComponent(label, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboTypes, 1, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboCoder1 = new IComboBox();
		cboCoder1.setName("Coder1");
		cboCoder1.setItems(getCoder(LibConstants.ACTION_CODER1));
		cboCoder1.putClientProperty("Tip", LibConstants.ACTION_CODER1);
		cboCoder1.addFocusListener(this);
		cboCoder1.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.setValue1((short) cb.getIndex());
						altered = true;
						setToolTip(LibConstants.ACTION_CODER1);
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, columns[4]);
		label.setLabelFor(cboCoder1);
		IUtilities.addComponent(label, 0, 3, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboCoder1, 1, 3, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboCoder2 = new IComboBox();
		cboCoder2.setName("Coder2");
		cboCoder2.setItems(getCoder(LibConstants.ACTION_CODER2));
		cboCoder2.putClientProperty("Tip", LibConstants.ACTION_CODER2);
		cboCoder2.addFocusListener(this);
		cboCoder2.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.setValue2((short) cb.getIndex());
						altered = true;
						setToolTip(LibConstants.ACTION_CODER2);
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, columns[5]);
		label.setLabelFor(cboCoder2);
		IUtilities.addComponent(label, 0, 4, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboCoder2, 1, 4, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboCoder3 = new IComboBox();
		cboCoder3.setName("Coder3");
		cboCoder3.setItems(getCoder(LibConstants.ACTION_CODER3));
		cboCoder3.putClientProperty("Tip", LibConstants.ACTION_CODER3);
		cboCoder3.addFocusListener(this);
		cboCoder3.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.setValue3((short) cb.getIndex());
						altered = true;
						setToolTip(LibConstants.ACTION_CODER3);
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, columns[6]);
		label.setLabelFor(cboCoder3);
		IUtilities.addComponent(label, 0, 5, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboCoder3, 1, 5, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboCoder4 = new IComboBox();
		cboCoder4.setName("Coder4");
		cboCoder4.setItems(getCoder(LibConstants.ACTION_CODER4));
		cboCoder4.putClientProperty("Tip", LibConstants.ACTION_CODER4);
		cboCoder4.addFocusListener(this);
		cboCoder4.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.setValue4((short) cb.getIndex());
						altered = true;
						setToolTip(LibConstants.ACTION_CODER4);
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, columns[7]);
		label.setLabelFor(cboCoder4);
		IUtilities.addComponent(label, 0, 6, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboCoder4, 1, 6, 2, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, columns[8]);
		IUtilities.addComponent(label, 0, 7, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblValue5 = IUtilities.createJLabel(SwingConstants.LEFT, 0, "   0");
		IUtilities.addComponent(lblValue5, 1, 7, 2, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtToolTip = new JTextArea();
		txtToolTip.setName("ToolTip");
		txtToolTip.setMargin(new Insets(4, 4, 4, 4));
		txtToolTip.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtToolTip.setFont(LibConstants.APP_FONT);
		txtToolTip.setEditable(false);
		txtToolTip.setFocusable(false);
		txtToolTip.setLineWrap(true);
		txtToolTip.setWrapStyleWord(true);
		JScrollPane scrollToolTip = IUtilities.createJScrollPane(txtToolTip);
		IUtilities.addComponent(scrollToolTip, 0, 8, 3, 3, 1.0, 0.25, GridBagConstraints.BOTH, GridBagConstraints.EAST,
				pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		setLayout(new BorderLayout());
		add(new IToolBar(application), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	@Override
	public void focusGained(FocusEvent e) {
		Component c = (Component) e.getSource();
		if (c instanceof IComboBox) {
			IComboBox cc = (IComboBox) c;
			String s = (String) cc.getClientProperty("Tip");
			byte i = Byte.valueOf(s);
			setToolTip(i);
			return;
		}
		setToolTip((byte) 0);
	}

	private void getData() {
		list = application.dbPowerJ.getOrderGroups();
		for (int i = 0; i < list.size(); i++) {
			if (newID < list.get(i).getGrpID()) {
				newID = list.get(i).getGrpID();
			}
		}
		application.display("No Rows: " + list.size());
		newID++;
		// Add a blank row
		ordergroup = new OrderGroupData();
		ordergroup.setNewRow(true);
		list.add(ordergroup);
	}

	private Object[] getCoder(byte coderID) {
		ArrayList<CoderData> temp = application.dbPowerJ.getCoder(coderID);
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		switch (coderID) {
		case 1:
			for (int i = 0; i < temp.size(); i++) {
				list.add(new ItemData(temp.get(i).getCodeID(), temp.get(i).getName()));
				mapCoder1.put(temp.get(i).getCodeID(), temp.get(i).getDescr());
			}
			break;
		case 2:
			for (int i = 0; i < temp.size(); i++) {
				list.add(new ItemData(temp.get(i).getCodeID(), temp.get(i).getName()));
				mapCoder2.put(temp.get(i).getCodeID(), temp.get(i).getDescr());
			}
			break;
		case 3:
			for (int i = 0; i < temp.size(); i++) {
				list.add(new ItemData(temp.get(i).getCodeID(), temp.get(i).getName()));
				mapCoder3.put(temp.get(i).getCodeID(), temp.get(i).getDescr());
			}
			break;
		default:
			for (int i = 0; i < temp.size(); i++) {
				list.add(new ItemData(temp.get(i).getCodeID(), temp.get(i).getName()));
				mapCoder4.put(temp.get(i).getCodeID(), temp.get(i).getDescr());
			}
		}
		temp.clear();
		return list.toArray();
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("ordergroups.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1.5f, 4, 1, 1, 1, 1, 1, 1 };
		String str = "Orders Groups - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
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
					case ORG_NO:
						paragraph.add(new Chunk(application.numbers.formatNumber(list.get(i).getGrpID())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case ORG_NAME:
						paragraph.add(new Chunk(list.get(i).getName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_DESCR:
						paragraph.add(new Chunk(list.get(i).getDescr()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_TYPE:
						paragraph.add(new Chunk(cboTypes.getItemName(list.get(i).getTypeID())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_VAL1:
						paragraph.add(new Chunk(cboCoder1.getItemName(list.get(i).getValue1())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_VAL2:
						paragraph.add(new Chunk(cboCoder2.getItemName(list.get(i).getValue2())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_VAL3:
						paragraph.add(new Chunk(cboCoder3.getItemName(list.get(i).getValue3())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_VAL4:
						paragraph.add(new Chunk(cboCoder4.getItemName(list.get(i).getValue4())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(application.numbers.formatNumber(list.get(i).getValue5())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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
		if (ordergroup.isNewRow()) {
			ordergroup.setGrpID(newID);
		}
		ordergroup.setName(txtName.getText().trim());
		if (ordergroup.getName().length() > 8) {
			ordergroup.setName(ordergroup.getName().substring(0, 8));
		}
		ordergroup.setDescr(txtDescr.getText().trim());
		if (ordergroup.getDescr().length() > 64) {
			ordergroup.setDescr(ordergroup.getDescr().substring(0, 64));
		}
		if (application.dbPowerJ.setOrderGroup(ordergroup) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (ordergroup.isNewRow()) {
				ordergroup.setNewRow(false);
				newID++;
				// Add a blank row
				OrderGroupData nextOrdergroup = new OrderGroupData();
				nextOrdergroup.setNewRow(true);
				list.add(nextOrdergroup);
				model.fireTableRowsInserted(list.size() - 1, list.size() - 1);

			}
		}
	}

	@Override
	public void setFilter(short id, int value) {
		final int v = value;
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		if (value > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).getTypeID() == v);
				}
			};
		}
		TableRowSorter<ModelOrderGroup> sorter = (TableRowSorter<ModelOrderGroup>) table.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		ordergroup = list.get(rowIndex);
		txtName.setText(ordergroup.getName());
		txtDescr.setText(ordergroup.getDescr());
		lblValue5.setText(application.numbers.formatNumber(ordergroup.getValue5() / 60));
		cboTypes.setIndex(ordergroup.getTypeID());
		cboCoder1.setIndex(ordergroup.getValue1());
		cboCoder2.setIndex(ordergroup.getValue2());
		cboCoder3.setIndex(ordergroup.getValue3());
		cboCoder4.setIndex(ordergroup.getValue4());
		programmaticChange = false;
		setToolTip((byte) 0);
	}

	private void setToolTip(byte coderID) {
		switch (coderID) {
		case LibConstants.ACTION_CODER1:
			txtToolTip.setText(mapCoder1.get(ordergroup.getValue1()));
			break;
		case LibConstants.ACTION_CODER2:
			txtToolTip.setText(mapCoder2.get(ordergroup.getValue2()));
			break;
		case LibConstants.ACTION_CODER3:
			txtToolTip.setText(mapCoder3.get(ordergroup.getValue3()));
			break;
		case LibConstants.ACTION_CODER4:
			txtToolTip.setText(mapCoder4.get(ordergroup.getValue4()));
			break;
		default:
			txtToolTip.setText(null);
		}
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("ordergroups.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Orders Groups");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					"Orders Groups - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
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
				case ORG_NO:
				case ORG_VAL5:
					sheet.setColumnWidth(col, 5 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case ORG_NAME:
					sheet.setColumnWidth(col, 15 * 256); // 15 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case ORG_DESCR:
					sheet.setColumnWidth(col, 30 * 256); // 30 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				default:
					sheet.setColumnWidth(col, 8 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				}
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case ORG_NO:
						xlsCell.setCellValue(list.get(i).getGrpID());
						break;
					case ORG_NAME:
						xlsCell.setCellValue(list.get(i).getName());
						break;
					case ORG_DESCR:
						xlsCell.setCellValue(list.get(i).getDescr());
						break;
					case ORG_TYPE:
						xlsCell.setCellValue(cboTypes.getItemName(list.get(i).getTypeID()));
						break;
					case ORG_VAL1:
						xlsCell.setCellValue(cboCoder1.getItemName(list.get(i).getValue1()));
						break;
					case ORG_VAL2:
						xlsCell.setCellValue(cboCoder2.getItemName(list.get(i).getValue2()));
						break;
					case ORG_VAL3:
						xlsCell.setCellValue(cboCoder3.getItemName(list.get(i).getValue3()));
						break;
					case ORG_VAL4:
						xlsCell.setCellValue(cboCoder4.getItemName(list.get(i).getValue4()));
						break;
					default:
						xlsCell.setCellValue(list.get(i).getValue5());
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

	private class ModelOrderGroup extends ITableModel {

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