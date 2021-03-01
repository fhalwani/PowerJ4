package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
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
import ca.powerj.data.SpecimenGroupData;
import ca.powerj.data.SubspecialtyList;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IComboEditor;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class SpecimenGroupPanel extends BasePanel {
	private final byte SPG_ID = 0;
	private final byte SPG_DESCR = 1;
	private final byte SPG_SPY = 2;
	private final byte SPG_SUB = 3;
	private final byte SPG_PROC = 4;
	private final byte SPG_LN = 5;
	private final byte SPG_VAL5 = 6;
	private final byte SPG_VAL1B = 7;
	private final byte SPG_VAL2B = 8;
	private final byte SPG_VAL3B = 9;
	private final byte SPG_VAL4B = 10;
	private final byte SPG_VAL1M = 11;
	private final byte SPG_VAL2M = 12;
	private final byte SPG_VAL3M = 13;
	private final byte SPG_VAL4M = 14;
	private final byte SPG_VAL1R = 15;
	private final byte SPG_VAL2R = 16;
	private final byte SPG_VAL3R = 17;
//	private final byte SPG_VAL4R = 18;
	private final byte FILTER_SPY = 0;
	private final byte FILTER_SUB = 1;
	private final byte FILTER_PRO = 2;
	private short newID = 0;
	private int rowIndex = 0;
	private int[] filters = { 0, 0, 0 };
	private SpecimenGroupData specimen = new SpecimenGroupData();
	private ArrayList<SpecimenGroupData> specimens = new ArrayList<SpecimenGroupData>();
	private HashMap<Byte, String> specialties = new HashMap<Byte, String>();
	private HashMap<Byte, String> procedures = new HashMap<Byte, String>();
	private HashMap<Short, String> coder1 = new HashMap<Short, String>();
	private HashMap<Short, String> coder2 = new HashMap<Short, String>();
	private HashMap<Short, String> coder3 = new HashMap<Short, String>();
	private HashMap<Short, String> coder4 = new HashMap<Short, String>();
	private ModelSpecimen modelSpecimen;
	private ModelCoder modelCoder;
	private JLabel lblSpecialty, lblValue5;
	private ITextString txtDescr;
	private IComboBox cboProcedure, cboSubspecial;
	private JTextArea txtToolTip;
	private JCheckBox ckbLN;
	private ITable tableSpecimen, tableCoder;

	SpecimenGroupPanel(AppFrame application) {
		super(application);
		setName("Specimens");
		application.dbPowerJ.setStatements(LibConstants.ACTION_SPECGROUP);
		getSubspecialties();
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			specimens.clear();
			specialties.clear();
			procedures.clear();
			coder1.clear();
			coder2.clear();
			coder3.clear();
			coder4.clear();
		}
		return !altered;
	}

	private void createPanel() {
		// Table collapses & disappears below this size!!!
		Dimension dim = new Dimension(650, 1005);
		setMinimumSize(dim);
		setPreferredSize(dim);
		modelSpecimen = new ModelSpecimen();
		tableSpecimen = new ITable(modelSpecimen, application.dates, application.numbers);
		tableSpecimen.addAncestorListener(new IFocusListener());
		tableSpecimen.addFocusListener(this);
		tableSpecimen.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
					setRow(tableSpecimen.convertRowIndexToModel(index));
				}
			}
		});
		tableSpecimen.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IUtilities.createJScrollPane(tableSpecimen);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 300));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtDescr = new ITextString(2, 64);
		txtDescr.setName("Descr");
		txtDescr.addFocusListener(this);
		txtDescr.getDocument().addDocumentListener(this);
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Descr:");
		label.setLabelFor(txtDescr);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtDescr, 1, 0, 4, 1, 0.8, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Specialty:");
		IUtilities.addComponent(label, 0, 1, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblSpecialty = IUtilities.createJLabel(SwingConstants.LEFT, 0, "      ");
		IUtilities.addComponent(lblSpecialty, 1, 1, 2, 1, 0.4, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboSubspecial = new IComboBox();
		cboSubspecial.setName("Subspecial");
		cboSubspecial.setItems(getSubspecialties());
		cboSubspecial.addFocusListener(this);
		cboSubspecial.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimen.setSubID((byte) cb.getIndex());
						lblSpecialty.setText(specialties.get(specimen.getSubID()));
						altered = true;
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_U, "Subspecialty:");
		label.setLabelFor(cboSubspecial);
		IUtilities.addComponent(label, 0, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboSubspecial, 1, 2, 2, 1, 0.4, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboProcedure = new IComboBox();
		cboProcedure.setName("Procedure");
		cboProcedure.setItems(getProcedure());
		cboProcedure.addFocusListener(this);
		cboProcedure.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimen.setProID((byte) cb.getIndex());
						altered = true;
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_R, "Procedure:");
		label.setLabelFor(cboProcedure);
		IUtilities.addComponent(label, 0, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboProcedure, 1, 3, 2, 1, 0.4, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		ckbLN = new JCheckBox("Has LN:");
		ckbLN.setMnemonic(KeyEvent.VK_H);
		ckbLN.setHorizontalTextPosition(SwingConstants.LEFT);
		ckbLN.setName("LN");
		ckbLN.setFont(LibConstants.APP_FONT);
		ckbLN.addFocusListener(this);
		ckbLN.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					specimen.setHasLN(ckbLN.isSelected());
					altered = true;
				}
			}
		});
		IUtilities.addComponent(ckbLN, 0, 4, 2, 1, 0.4, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, application.getProperty("coder5") + ":");
		IUtilities.addComponent(label, 2, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblValue5 = IUtilities.createJLabel(SwingConstants.LEFT, 0, "   0");
		IUtilities.addComponent(lblValue5, 3, 4, 1, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		modelCoder = new ModelCoder();
		tableCoder = new ITable(modelCoder, application.dates, application.numbers) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					String tip = null;
					Point p = e.getPoint();
					int row = rowAtPoint(p);
					int colIndex = columnAtPoint(p);
					int col = convertColumnIndexToModel(colIndex);
					setTooltip(row, col);
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tableCoder.addFocusListener(this);
		TableColumn column = null;
		for (int i = 1; i < 5; i++) {
			column = tableCoder.getColumnModel().getColumn(i);
			column.setMinWidth(150);
			switch (i) {
			case 1:
				IComboBox cboCoder1 = new IComboBox();
				cboCoder1.setItems(getCoder(LibConstants.ACTION_CODER1));
				column.setCellEditor(new IComboEditor(cboCoder1));
				break;
			case 2:
				IComboBox cboCoder2 = new IComboBox();
				cboCoder2.setItems(getCoder(LibConstants.ACTION_CODER2));
				column.setCellEditor(new IComboEditor(cboCoder2));
				break;
			case 3:
				IComboBox cboCoder3 = new IComboBox();
				cboCoder3.setItems(getCoder(LibConstants.ACTION_CODER3));
				column.setCellEditor(new IComboEditor(cboCoder3));
				break;
			default:
				IComboBox cboCoder4 = new IComboBox();
				cboCoder4.setItems(getCoder(LibConstants.ACTION_CODER4));
				column.setCellEditor(new IComboEditor(cboCoder4));
			}
		}
		JScrollPane scrollCodes = IUtilities.createJScrollPane(tableCoder);
		IUtilities.addComponent(scrollCodes, 0, 5, 5, 4, 1, 0.5, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		IUtilities.addComponent(scrollToolTip, 0, 7, 2, 2, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
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
		String s = c.getName();
		if (s != null) {
			if (s.equals("Procedure")) {
				setTooltip(0, 5);
			}
		}
	}

	private void getData() {
		specimens = application.dbPowerJ.getSpecimenGroups();
		for (int i = 0; i < specimens.size(); i++) {
			if (newID < specimen.getGrpID()) {
				newID = specimen.getGrpID();
			}
		}
		newID++;
		application.display("No Rows: " + specimens.size());
		// Add a blank row
		specimen = new SpecimenGroupData();
		specimen.setNewRow(true);
		specimens.add(specimen);
	}

	private Object[] getCoder(byte coderID) {
		ArrayList<CoderData> temp = application.dbPowerJ.getCoder(coderID);
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		switch (coderID) {
		case LibConstants.ACTION_CODER1:
			for (int i = 0; i < temp.size(); i++) {
				list.add(new ItemData(temp.get(i).getCodeID(), temp.get(i).getName()));
				coder1.put(temp.get(i).getCodeID(), temp.get(i).getDescr());
			}
			break;
		case LibConstants.ACTION_CODER2:
			for (int i = 0; i < temp.size(); i++) {
				list.add(new ItemData(temp.get(i).getCodeID(), temp.get(i).getName()));
				coder2.put(temp.get(i).getCodeID(), temp.get(i).getDescr());
			}
			break;
		case LibConstants.ACTION_CODER3:
			for (int i = 0; i < temp.size(); i++) {
				list.add(new ItemData(temp.get(i).getCodeID(), temp.get(i).getName()));
				coder3.put(temp.get(i).getCodeID(), temp.get(i).getDescr());
			}
			break;
		case LibConstants.ACTION_CODER4:
			for (int i = 0; i < temp.size(); i++) {
				list.add(new ItemData(temp.get(i).getCodeID(), temp.get(i).getName()));
				coder4.put(temp.get(i).getCodeID(), temp.get(i).getDescr());
			}
			break;
		default:
		}
		temp.clear();
		return list.toArray();
	}

	private String[] getExportColumns() {
		String[] columns = {"NO", "DESCR", "SPY", "SUB", "PROC", "LN",
				application.getProperty("coder5"),
				application.getProperty("coder1") + "-B",
				application.getProperty("coder2") + "-B",
				application.getProperty("coder3") + "-B",
				application.getProperty("coder4") + "-B",
				application.getProperty("coder1") + "-M",
				application.getProperty("coder2") + "-M",
				application.getProperty("coder3") + "-M",
				application.getProperty("coder4") + "-M",
				application.getProperty("coder1") + "-R",
				application.getProperty("coder2") + "-R",
				application.getProperty("coder3") + "-R",
				application.getProperty("coder4") + "-R"};
		return columns;
	}

	private Object[] getProcedure() {
		return application.dbPowerJ.getProcedures(false).getAll();
	}

	private Object[] getSubspecialties() {
		SubspecialtyList tempSub = application.dbPowerJ.getSubspecialties(false);
		ItemData[] list = tempSub.getAll();
		for (int i = 0; i < list.length; i++) {
			specialties.put(tempSub.get(i).getID(), tempSub.get(i).getSpecialty());
		}
		return list;
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("specimengroups.pdf").trim();
		if (fileName.length() == 0) {
			return;
		}
		final float[] widths = { 1, 4, 1.5f, 1, 1.5f, 1, 1, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f,
				1.5f, 1.5f };
		final String[] columns = getExportColumns();
		String str = "Specimens Groups - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
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
			for (int row = 0; row < tableSpecimen.getRowCount(); row++) {
				i = tableSpecimen.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case SPG_ID:
						paragraph.add(new Chunk(application.numbers.formatNumber(specimens.get(i).getGrpID())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SPG_DESCR:
						paragraph.add(new Chunk(specimens.get(i).getDescr()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_SPY:
						paragraph.add(new Chunk(specialties.get(specimens.get(i).getSpyID())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_SUB:
						paragraph.add(new Chunk(cboSubspecial.getItemName(specimens.get(i).getSubID())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_PROC:
						paragraph.add(new Chunk(cboProcedure.getItemName(specimens.get(i).getProID())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_LN:
						paragraph.add(new Chunk((specimens.get(i).isHasLN() ? "Y" : "N")));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL5:
						paragraph.add(new Chunk(application.numbers.formatNumber(specimens.get(i).getValue5() / 60)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SPG_VAL1B:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(0, 0)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL2B:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(0, 1)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL3B:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(0, 2)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL4B:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(0, 3)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL1M:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(1, 0)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL2M:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(1, 1)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL3M:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(1, 2)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL4M:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(1, 3)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL1R:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(2, 0)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL2R:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(2, 1)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL3R:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(2, 2)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(specimens.get(i).getCodeName(2, 3)));
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
		if (specimen.isNewRow()) {
			specimen.setGrpID(newID);
		}
		specimen.setDescr(txtDescr.getText());
		if (application.dbPowerJ.setSpecimenGroup(specimen) > 0) {
			altered = false;
			modelSpecimen.fireTableRowsUpdated(rowIndex, rowIndex);
			if (specimen.isNewRow()) {
				specimen.setNewRow(false);
				newID++;
				// Add a blank row
				SpecimenGroupData nextGroup = new SpecimenGroupData();
				nextGroup.setNewRow(true);
				specimens.add(nextGroup);
				modelSpecimen.fireTableRowsInserted(specimens.size() - 1, specimens.size() - 1);
			}
		}
	}

	@Override
	public void setFilter(short id, int value) {
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		switch (id) {
		case IToolBar.TB_PRO:
			filters[FILTER_PRO] = value;
			break;
		case IToolBar.TB_SPY:
			filters[FILTER_SPY] = value;
			break;
		case IToolBar.TB_SUB:
			filters[FILTER_SUB] = value;
			break;
		default:
			application.log(LibConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
			return;
		}
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> rowFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[FILTER_SPY] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).getSpyID() == filters[FILTER_SPY]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SUB] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).getSubID() == filters[FILTER_SUB]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_PRO] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).getProID() == filters[FILTER_PRO]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (rowFilters.size() > 0) {
			// Add to the compound filter
			rowFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(rowFilters);
		}
		TableRowSorter<ModelSpecimen> sorter = (TableRowSorter<ModelSpecimen>) tableSpecimen.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		specimen = specimens.get(rowIndex);
		txtDescr.setText(specimen.getDescr());
		lblSpecialty.setText(specialties.get(specimen.getSpyID()));
		lblValue5.setText(String.valueOf((specimen.getValue5() / 60)));
		ckbLN.setSelected(specimen.isHasLN());
		cboSubspecial.setIndex(specimen.getSubID());
		cboProcedure.setIndex(specimen.getProID());
		programmaticChange = false;
		modelCoder.fireTableDataChanged();
	}

	private void setTooltip(int row, int col) {
		txtToolTip.setText(null);
		if (row > -1) {
			switch (col) {
			case 1:
				txtToolTip.setText(coder1.get(specimen.getCodeId(row, 0)));
			case 2:
				txtToolTip.setText(coder2.get(specimen.getCodeId(row, 1)));
			case 3:
				txtToolTip.setText(coder3.get(specimen.getCodeId(row, 2)));
			case 4:
				txtToolTip.setText(coder4.get(specimen.getCodeId(row, 3)));
			case 5:
				txtToolTip.setText(procedures.get(specimen.getProID()));
			}
		}
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("specimengroups.xls").trim();
		if (fileName.length() == 0) {
			return;
		}
		final String[] columns = getExportColumns();
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Specimens Groups");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					"Specimens Groups - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
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
				case SPG_ID:
				case SPG_VAL5:
					sheet.setColumnWidth(col, 5 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case SPG_DESCR:
					sheet.setColumnWidth(col, 30 * 256); // 30 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case SPG_LN:
					sheet.setColumnWidth(col, 5 * 256); // 5 characters
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
			for (int row = 0; row < tableSpecimen.getRowCount(); row++) {
				i = tableSpecimen.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case SPG_ID:
						xlsCell.setCellValue(specimens.get(i).getGrpID());
						break;
					case SPG_DESCR:
						xlsCell.setCellValue(specimens.get(i).getDescr());
						break;
					case SPG_SPY:
						xlsCell.setCellValue(specialties.get(specimens.get(i).getSpyID()));
						break;
					case SPG_SUB:
						xlsCell.setCellValue(cboSubspecial.getItemName(specimens.get(i).getSubID()));
						break;
					case SPG_PROC:
						xlsCell.setCellValue(cboProcedure.getItemName(specimens.get(i).getProID()));
						break;
					case SPG_LN:
						xlsCell.setCellValue((specimens.get(i).isHasLN() ? "Y" : "N"));
						break;
					case SPG_VAL5:
						xlsCell.setCellValue(specimens.get(i).getValue5() / 60);
						break;
					case SPG_VAL1B:
						xlsCell.setCellValue(specimens.get(i).getCodeName(0, 0));
						break;
					case SPG_VAL2B:
						xlsCell.setCellValue(specimens.get(i).getCodeName(0, 1));
						break;
					case SPG_VAL3B:
						xlsCell.setCellValue(specimens.get(i).getCodeName(0, 2));
						break;
					case SPG_VAL4B:
						xlsCell.setCellValue(specimens.get(i).getCodeName(0, 3));
						break;
					case SPG_VAL1M:
						xlsCell.setCellValue(specimens.get(i).getCodeName(1, 0));
						break;
					case SPG_VAL2M:
						xlsCell.setCellValue(specimens.get(i).getCodeName(1, 1));
						break;
					case SPG_VAL3M:
						xlsCell.setCellValue(specimens.get(i).getCodeName(1, 2));
						break;
					case SPG_VAL4M:
						xlsCell.setCellValue(specimens.get(i).getCodeName(1, 3));
						break;
					case SPG_VAL1R:
						xlsCell.setCellValue(specimens.get(i).getCodeName(2, 0));
						break;
					case SPG_VAL2R:
						xlsCell.setCellValue(specimens.get(i).getCodeName(2, 1));
						break;
					case SPG_VAL3R:
						xlsCell.setCellValue(specimens.get(i).getCodeName(2, 2));
						break;
					default:
						xlsCell.setCellValue(specimens.get(i).getCodeName(2, 3));
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

	private class ModelSpecimen extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return specimens.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (specimens.size() > 0 && row < specimens.size()) {
				value = specimens.get(row).getDescr();
			}
			return value;
		}
	}

	private class ModelCoder extends ITableModel {
		private final String[] rowNames = {"Benign", "Malignant", "Radical"};
		private final String[] columns = {"Category", application.getProperty("coder1"),
				application.getProperty("coder2"), application.getProperty("coder3"),
				application.getProperty("coder4")};

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return 3;
		}

		@Override
		public String getColumnName(int col) {
			return columns[col];
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (col == 0) {
				value = rowNames[row];
			} else {
				value = specimen.getCode(row, col -1);
			}
			return value;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			}
			return ItemData.class;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return (col > 0);
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (value instanceof ItemData) {
				specimen.setCode(row, col -1, (ItemData) value);
				altered = true;
			}
		}
	}
}