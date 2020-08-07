package ca.powerj;

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

class NSpecimenGroup extends NBase {
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
	private final byte SPG_VAL4R = 18;
	private final byte FILTER_SPY = 0;
	private final byte FILTER_SUB = 1;
	private final byte FILTER_PRO = 2;
	private short newID = 0;
	private int rowIndex = 0;
	private short[] filters = { 0, 0, 0 };
	private String[] columns = { "NO", "DESCR", "SPY", "SUB", "PROC", "LN", "", "", "", "", "", "", "", "", "", "", "",
			"", "" };
	private OSpecGroup specimen = new OSpecGroup();
	private ArrayList<OSpecGroup> specimens = new ArrayList<OSpecGroup>();
	private HashMap<Byte, String> specialties = new HashMap<Byte, String>();
	private HashMap<Byte, String> procedures = new HashMap<Byte, String>();
	private HashMap<Short, String> coder1 = new HashMap<Short, String>();
	private HashMap<Short, String> coder2 = new HashMap<Short, String>();
	private HashMap<Short, String> coder3 = new HashMap<Short, String>();
	private HashMap<Short, String> coder4 = new HashMap<Short, String>();
	private ModelSpecimen modelList;
	private ModelCode modelCodes;
	private JLabel lblSpecialty, lblValue5;
	private ITextString txtDescr;
	private IComboBox cboProcedure, cboSubspecial;
	private JTextArea txtToolTip;
	private JCheckBox ckbLN;
	private ITable tblList, tblCodes;

	NSpecimenGroup(AClient parent) {
		super(parent);
		setName("Specimens");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_SPECGROUP);
		columns[SPG_VAL5] = pj.setup.getString(LSetup.VAR_V5_NAME);
		columns[SPG_VAL1B] = pj.setup.getString(LSetup.VAR_CODER1_NAME) + "-B";
		columns[SPG_VAL2B] = pj.setup.getString(LSetup.VAR_CODER2_NAME) + "-B";
		columns[SPG_VAL3B] = pj.setup.getString(LSetup.VAR_CODER3_NAME) + "-B";
		columns[SPG_VAL4B] = pj.setup.getString(LSetup.VAR_CODER4_NAME) + "-B";
		columns[SPG_VAL1M] = pj.setup.getString(LSetup.VAR_CODER1_NAME) + "-M";
		columns[SPG_VAL2M] = pj.setup.getString(LSetup.VAR_CODER2_NAME) + "-M";
		columns[SPG_VAL3M] = pj.setup.getString(LSetup.VAR_CODER3_NAME) + "-M";
		columns[SPG_VAL4M] = pj.setup.getString(LSetup.VAR_CODER4_NAME) + "-M";
		columns[SPG_VAL1R] = pj.setup.getString(LSetup.VAR_CODER1_NAME) + "-R";
		columns[SPG_VAL2R] = pj.setup.getString(LSetup.VAR_CODER2_NAME) + "-R";
		columns[SPG_VAL3R] = pj.setup.getString(LSetup.VAR_CODER3_NAME) + "-R";
		columns[SPG_VAL4R] = pj.setup.getString(LSetup.VAR_CODER4_NAME) + "-R";
		getSubspecialties();
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			specimens.clear();
		}
		return !altered;
	}

	private void createPanel() {
		modelList = new ModelSpecimen();
		tblList = new ITable(pj, modelList);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tblList.addAncestorListener(new IFocusListener());
		tblList.addFocusListener(this);
		tblList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
					setRow(tblList.convertRowIndexToModel(index));
				}
			}
		});
		tblList.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IGUI.createJScrollPane(tblList);
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
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Descr:");
		label.setLabelFor(txtDescr);
		IGUI.addComponent(label, 0, 0, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtDescr, 1, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Specialty:");
		IGUI.addComponent(label, 0, 1, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblSpecialty = IGUI.createJLabel(SwingConstants.LEFT, 0, "      ");
		IGUI.addComponent(lblSpecialty, 1, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboSubspecial = new IComboBox();
		cboSubspecial.setName("Subspecial");
		cboSubspecial.setModel(pj.dbPowerJ.getSubspecialties(false));
		cboSubspecial.addFocusListener(this);
		cboSubspecial.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimen.subID = (byte) cb.getIndex();
						lblSpecialty.setText(specialties.get(specimen.subID));
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_U, "Subspecialty:");
		label.setLabelFor(cboSubspecial);
		IGUI.addComponent(label, 0, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboSubspecial, 1, 2, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboProcedure = new IComboBox();
		cboProcedure.setName("Procedure");
		cboProcedure.setModel(getProcedure());
		cboProcedure.addFocusListener(this);
		cboProcedure.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimen.proID = (byte) cb.getIndex();
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_R, "Procedure:");
		label.setLabelFor(cboProcedure);
		IGUI.addComponent(label, 0, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboProcedure, 1, 3, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		ckbLN = new JCheckBox("Has LN:");
		ckbLN.setMnemonic(KeyEvent.VK_L);
		ckbLN.setName("LN");
		ckbLN.setFont(LConstants.APP_FONT);
		ckbLN.addFocusListener(this);
		ckbLN.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					specimen.hasLN = ckbLN.isSelected();
					altered = true;
				}
			}
		});
		IGUI.addComponent(ckbLN, 1, 4, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, pj.setup.getString(LSetup.VAR_V5_NAME) + ":");
		IGUI.addComponent(label, 0, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblValue5 = IGUI.createJLabel(SwingConstants.LEFT, 0, "   0");
		IGUI.addComponent(lblValue5, 1, 5, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		modelCodes = new ModelCode();
		tblCodes = new ITable(pj, modelCodes) {
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
		tblCodes.addFocusListener(this);
		IComboBox cboCoder1 = new IComboBox();
		cboCoder1.setModel(getCoder(1));
		IComboBox cboCoder2 = new IComboBox();
		cboCoder2.setModel(getCoder(2));
		IComboBox cboCoder3 = new IComboBox();
		cboCoder3.setModel(getCoder(3));
		IComboBox cboCoder4 = new IComboBox();
		cboCoder4.setModel(getCoder(4));
		TableColumn column = null;
		for (int i = 1; i < 5; i++) {
			column = tblCodes.getColumnModel().getColumn(i);
			column.setMinWidth(100);
			switch (i) {
			case 1:
				column.setCellEditor(new IComboTableEditor(cboCoder1));
				break;
			case 2:
				column.setCellEditor(new IComboTableEditor(cboCoder2));
				break;
			case 3:
				column.setCellEditor(new IComboTableEditor(cboCoder3));
				break;
			default:
				column.setCellEditor(new IComboTableEditor(cboCoder4));
			}
		}
		JScrollPane scrollCodes = IGUI.createJScrollPane(tblCodes);
		IGUI.addComponent(scrollCodes, 0, 6, 2, 2, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtToolTip = new JTextArea();
		txtToolTip.setName("ToolTip");
		txtToolTip.setMargin(new Insets(4, 4, 4, 4));
		txtToolTip.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtToolTip.setFont(LConstants.APP_FONT);
		txtToolTip.setEditable(false);
		txtToolTip.setFocusable(false);
		txtToolTip.setLineWrap(true);
		txtToolTip.setWrapStyleWord(true);
		JScrollPane scrollToolTip = IGUI.createJScrollPane(txtToolTip);
		IGUI.addComponent(scrollToolTip, 0, 7, 2, 2, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
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

		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SPG_SELECT));
		try {
			while (rst.next()) {
				specimen = new OSpecGroup();
				specimen.spyID = rst.getByte("syid");
				specimen.subID = rst.getByte("sbid");
				specimen.proID = rst.getByte("poid");
				specimen.grpID = rst.getShort("sgid");
				specimen.value5 = rst.getInt("sgv5");
				specimen.hasLN = (rst.getString("sgln").toUpperCase().equals("Y"));
				specimen.codes[0][0] = new OItem(rst.getShort("sg1b"), rst.getString("C1NB"));
				specimen.codes[0][1] = new OItem(rst.getShort("sg2b"), rst.getString("C2NB"));
				specimen.codes[0][2] = new OItem(rst.getShort("sg3b"), rst.getString("C3NB"));
				specimen.codes[0][3] = new OItem(rst.getShort("sg4b"), rst.getString("C4NB"));
				specimen.codes[1][0] = new OItem(rst.getShort("sg1m"), rst.getString("C1NM"));
				specimen.codes[1][1] = new OItem(rst.getShort("sg2m"), rst.getString("C2NM"));
				specimen.codes[1][2] = new OItem(rst.getShort("sg3m"), rst.getString("C3NM"));
				specimen.codes[1][3] = new OItem(rst.getShort("sg4m"), rst.getString("C4NM"));
				specimen.codes[2][0] = new OItem(rst.getShort("sg1r"), rst.getString("C1NR"));
				specimen.codes[2][1] = new OItem(rst.getShort("sg2r"), rst.getString("C2NR"));
				specimen.codes[2][2] = new OItem(rst.getShort("sg3r"), rst.getString("C3NR"));
				specimen.codes[2][3] = new OItem(rst.getShort("sg4r"), rst.getString("C4NR"));
				specimen.descr = rst.getString("sgdc").trim();
				specimens.add(specimen);
				if (newID < specimen.grpID) {
					newID = specimen.grpID;
				}
			}
			pj.statusBar.setMessage("No Rows: " + specimens.size());
			newID++;
			// Add a blank row
			specimen = new OSpecGroup();
			specimen.newRow = true;
			specimens.add(specimen);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Variables", e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	private Object[] getCoder(int coderID) {
		byte index = 0;
		ArrayList<OItem> list = new ArrayList<OItem>();
		switch (coderID) {
		case 1:
			index = DPowerJ.STM_CD1_SELECT;
			break;
		case 2:
			index = DPowerJ.STM_CD2_SELECT;
			break;
		case 3:
			index = DPowerJ.STM_CD3_SELECT;
			break;
		default:
			index = DPowerJ.STM_CD4_SELECT;
		}
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(index));
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("coid"), rst.getString("conm")));
				switch (coderID) {
				case 1:
					coder1.put(rst.getShort("coid"), rst.getString("codc"));
					break;
				case 2:
					coder2.put(rst.getShort("coid"), rst.getString("codc"));
					break;
				case 3:
					coder3.put(rst.getShort("coid"), rst.getString("codc"));
					break;
				default:
					coder4.put(rst.getShort("coid"), rst.getString("codc"));
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
		return list.toArray();
	}

	private Object[] getProcedure() {
		ArrayList<OItem> list = new ArrayList<OItem>();
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_PRO_SELECT));
		try {
			while (rst.next()) {
				procedures.put(rst.getByte("poid"), rst.getString("podc"));
				list.add(new OItem(rst.getByte("poid"), rst.getString("ponm")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
		return list.toArray();
	}

	private void getSubspecialties() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SUB_SELECT));
		try {
			while (rst.next()) {
				specialties.put(rst.getByte("sbid"), rst.getString("synm"));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("specimengroups.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 4, 1.5f, 1, 1.5f, 1, 1, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f,
				1.5f, 1.5f };
		String str = "Specimens Groups - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
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
			for (int row = 0; row < tblList.getRowCount(); row++) {
				i = tblList.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case SPG_ID:
						paragraph.add(new Chunk(pj.numbers.formatNumber(specimens.get(i).grpID)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SPG_DESCR:
						paragraph.add(new Chunk(specimens.get(i).descr));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_SPY:
						paragraph.add(new Chunk(specialties.get(specimens.get(i).subID)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_SUB:
						paragraph.add(new Chunk(cboSubspecial.getItemName(specimens.get(i).subID)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_PROC:
						paragraph.add(new Chunk(cboProcedure.getItemName(specimens.get(i).proID)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_LN:
						paragraph.add(new Chunk((specimens.get(i).hasLN ? "Y" : "N")));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL5:
						paragraph.add(new Chunk(pj.numbers.formatNumber(specimens.get(i).value5 / 60)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SPG_VAL1B:
						paragraph.add(new Chunk(specimens.get(i).codes[0][0].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL2B:
						paragraph.add(new Chunk(specimens.get(i).codes[0][1].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL3B:
						paragraph.add(new Chunk(specimens.get(i).codes[0][2].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL4B:
						paragraph.add(new Chunk(specimens.get(i).codes[0][3].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL1M:
						paragraph.add(new Chunk(specimens.get(i).codes[1][0].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL2M:
						paragraph.add(new Chunk(specimens.get(i).codes[1][1].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL3M:
						paragraph.add(new Chunk(specimens.get(i).codes[1][2].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL4M:
						paragraph.add(new Chunk(specimens.get(i).codes[1][3].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL1R:
						paragraph.add(new Chunk(specimens.get(i).codes[2][0].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL2R:
						paragraph.add(new Chunk(specimens.get(i).codes[2][1].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPG_VAL3R:
						paragraph.add(new Chunk(specimens.get(i).codes[2][2].name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(specimens.get(i).codes[2][3].name));
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
		byte index = DPowerJ.STM_SPG_UPDATE;
		if (specimen.newRow) {
			index = DPowerJ.STM_SPG_INSERT;
			specimen.grpID = newID;
		}
		specimen.descr = txtDescr.getText().trim();
		if (specimen.descr.length() > 64) {
			specimen.descr = specimen.descr.substring(0, 64);
		}
		pj.dbPowerJ.setShort(pjStms.get(index), 1, specimen.subID);
		pj.dbPowerJ.setShort(pjStms.get(index), 2, specimen.proID);
		pj.dbPowerJ.setShort(pjStms.get(index), 3, specimen.codes[0][0].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 4, specimen.codes[1][0].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 5, specimen.codes[2][0].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 6, specimen.codes[0][1].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 7, specimen.codes[1][1].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 8, specimen.codes[2][1].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 9, specimen.codes[0][2].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 10, specimen.codes[1][2].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 11, specimen.codes[2][2].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 12, specimen.codes[0][3].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 13, specimen.codes[1][3].id);
		pj.dbPowerJ.setShort(pjStms.get(index), 14, specimen.codes[2][3].id);
		pj.dbPowerJ.setInt(pjStms.get(index), 15, specimen.value5);
		pj.dbPowerJ.setString(pjStms.get(index), 16, (specimen.hasLN ? "Y" : "N"));
		pj.dbPowerJ.setString(pjStms.get(index), 17, specimen.descr);
		pj.dbPowerJ.setShort(pjStms.get(index), 18, specimen.grpID);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			altered = false;
			modelList.fireTableRowsUpdated(rowIndex, rowIndex);
			if (specimen.newRow) {
				specimen.newRow = false;
				newID++;
				// Add a blank row
				OSpecGroup nextGroup = new OSpecGroup();
				nextGroup.newRow = true;
				specimens.add(nextGroup);
				modelList.fireTableRowsInserted(specimens.size() - 1, specimens.size() - 1);
			}
		}
	}

	@Override
	void setFilter(short id, short value) {
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
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
			return;
		}
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> rowFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[FILTER_SPY] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).spyID == filters[FILTER_SPY]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SUB] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).subID == filters[FILTER_SUB]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_PRO] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).proID == filters[FILTER_PRO]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (rowFilters.size() > 0) {
			// Add to the compound filter
			rowFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(rowFilters);
		}
		TableRowSorter<ModelSpecimen> sorter = (TableRowSorter<ModelSpecimen>) tblList.getRowSorter();
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
		txtDescr.setText(specimen.descr);
		lblSpecialty.setText(specialties.get(specimen.subID));
		lblValue5.setText(pj.numbers.formatNumber(specimen.value5 / 60));
		ckbLN.setSelected(specimen.hasLN);
		cboSubspecial.setIndex(specimen.subID);
		cboProcedure.setIndex(specimen.proID);
		modelCodes.fireTableDataChanged();
		programmaticChange = false;
	}

	private void setTooltip(int row, int col) {
		txtToolTip.setText(null);
		if (row > -1) {
			switch (col) {
			case 1:
				txtToolTip.setText(coder1.get(specimen.codes[row][0].id));
			case 2:
				txtToolTip.setText(coder2.get(specimen.codes[row][1].id));
			case 3:
				txtToolTip.setText(coder3.get(specimen.codes[row][2].id));
			case 4:
				txtToolTip.setText(coder4.get(specimen.codes[row][3].id));
			case 5:
				txtToolTip.setText(procedures.get(specimen.proID));
			}
		}
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("specimengroups.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Specimens Groups");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					"Specimens Groups - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
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
			for (int row = 0; row < tblList.getRowCount(); row++) {
				i = tblList.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case SPG_ID:
						xlsCell.setCellValue(specimens.get(i).grpID);
						break;
					case SPG_DESCR:
						xlsCell.setCellValue(specimens.get(i).descr);
						break;
					case SPG_SPY:
						xlsCell.setCellValue(specialties.get(specimens.get(i).subID));
						break;
					case SPG_SUB:
						xlsCell.setCellValue(cboSubspecial.getItemName(specimens.get(i).subID));
						break;
					case SPG_PROC:
						xlsCell.setCellValue(cboProcedure.getItemName(specimens.get(i).proID));
						break;
					case SPG_LN:
						xlsCell.setCellValue((specimens.get(i).hasLN ? "Y" : "N"));
						break;
					case SPG_VAL5:
						xlsCell.setCellValue(specimens.get(i).value5 / 60);
						break;
					case SPG_VAL1B:
						xlsCell.setCellValue(specimens.get(i).codes[0][0].name);
						break;
					case SPG_VAL2B:
						xlsCell.setCellValue(specimens.get(i).codes[0][1].name);
						break;
					case SPG_VAL3B:
						xlsCell.setCellValue(specimens.get(i).codes[0][2].name);
						break;
					case SPG_VAL4B:
						xlsCell.setCellValue(specimens.get(i).codes[0][3].name);
						break;
					case SPG_VAL1M:
						xlsCell.setCellValue(specimens.get(i).codes[1][0].name);
						break;
					case SPG_VAL2M:
						xlsCell.setCellValue(specimens.get(i).codes[1][1].name);
						break;
					case SPG_VAL3M:
						xlsCell.setCellValue(specimens.get(i).codes[1][2].name);
						break;
					case SPG_VAL4M:
						xlsCell.setCellValue(specimens.get(i).codes[1][3].name);
						break;
					case SPG_VAL1R:
						xlsCell.setCellValue(specimens.get(i).codes[2][0].name);
						break;
					case SPG_VAL2R:
						xlsCell.setCellValue(specimens.get(i).codes[2][1].name);
						break;
					case SPG_VAL3R:
						xlsCell.setCellValue(specimens.get(i).codes[2][2].name);
						break;
					default:
						xlsCell.setCellValue(specimens.get(i).codes[2][3].name);
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
				value = specimens.get(row).descr;
			}
			return value;
		}
	}

	private class ModelCode extends AbstractTableModel {
		private final String[] rows = { "Benign", "Malignant", "Radical" };
		private final String[] columns = { "         ", pj.setup.getString(LSetup.VAR_CODER1_NAME),
				pj.setup.getString(LSetup.VAR_CODER2_NAME), pj.setup.getString(LSetup.VAR_CODER3_NAME),
				pj.setup.getString(LSetup.VAR_CODER4_NAME) };

		@Override
		public int getColumnCount() {
			return columns.length;
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
			if (col == 0 && rows.length > 0 && row < rows.length) {
				value = rows[row];
			} else if (col > 0 && specimen.codes.length > 0 && row < specimen.codes.length) {
				value = specimen.codes[row][col - 1];
			}
			return value;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			}
			return OItem.class;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return (col > 0);
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (value instanceof OItem) {
				specimen.codes[row][col - 1] = (OItem) value;
				altered = true;
			}
		}
	}
}