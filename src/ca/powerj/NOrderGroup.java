package ca.powerj;

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

class NOrderGroup extends NBase {
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
	private OOrderGroup ordergroup = new OOrderGroup();
	private ArrayList<OOrderGroup> list = new ArrayList<OOrderGroup>();
	private HashMap<Short, String> mapCoder1 = new HashMap<Short, String>();
	private HashMap<Short, String> mapCoder2 = new HashMap<Short, String>();
	private HashMap<Short, String> mapCoder3 = new HashMap<Short, String>();
	private HashMap<Short, String> mapCoder4 = new HashMap<Short, String>();
	private ModelOrderGroup model;
	private JLabel lblValue5;
	private ITable tbl;
	private ITextString txtName, txtDescr;
	private IComboBox cboTypes, cboCoder1, cboCoder2, cboCoder3, cboCoder4;
	private JTextArea txtToolTip;

	NOrderGroup(AClient pj) {
		super(pj);
		setName("Orders");
		columns[4] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		columns[5] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		columns[6] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		columns[7] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		columns[8] = pj.setup.getString(LSetup.VAR_V5_NAME);
		pjStms = pj.dbPowerJ.prepareStatements(LConstants.ACTION_ORDERGROUP);
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		trackDocument(e);
	}

	@Override
	boolean close() {
		if (super.close()) {
			list.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelOrderGroup();
		tbl = new ITable(pj, model);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		tbl.setName("tblList");
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
		txtName = new ITextString(2, 8);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtName, 1, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		txtDescr = new ITextString(2, 64);
		txtDescr.setName("Descr");
		txtDescr.addFocusListener(this);
		txtDescr.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Description:");
		label.setLabelFor(txtDescr);
		IGUI.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtDescr, 1, 1, 3, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboTypes = new IComboBox();
		cboTypes.setName("Types");
		cboTypes.setModel(pj.dbPowerJ.getOrderTypes(false));
		cboTypes.addFocusListener(this);
		cboTypes.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.typID = cb.getIndex();
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_S, "Type:");
		label.setLabelFor(cboTypes);
		IGUI.addComponent(label, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboTypes, 1, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboCoder1 = new IComboBox();
		cboCoder1.setName("Coder1");
		cboCoder1.setModel(getCoder(1));
		cboCoder1.putClientProperty("Tip", 1);
		cboCoder1.addFocusListener(this);
		cboCoder1.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.value1 = cb.getIndex();
						altered = true;
						setToolTip(1);
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, columns[4]);
		label.setLabelFor(cboCoder1);
		IGUI.addComponent(label, 0, 3, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboCoder1, 1, 3, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboCoder2 = new IComboBox();
		cboCoder2.setName("Coder2");
		cboCoder2.setModel(getCoder(2));
		cboCoder2.putClientProperty("Tip", 2);
		cboCoder2.addFocusListener(this);
		cboCoder2.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.value2 = cb.getIndex();
						altered = true;
						setToolTip(2);
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, columns[5]);
		label.setLabelFor(cboCoder2);
		IGUI.addComponent(label, 0, 4, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboCoder2, 1, 4, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboCoder3 = new IComboBox();
		cboCoder3.setName("Coder3");
		cboCoder3.setModel(getCoder(3));
		cboCoder3.putClientProperty("Tip", 3);
		cboCoder3.addFocusListener(this);
		cboCoder3.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.value3 = cb.getIndex();
						altered = true;
						setToolTip(3);
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, columns[6]);
		label.setLabelFor(cboCoder3);
		IGUI.addComponent(label, 0, 5, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboCoder3, 1, 5, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboCoder4 = new IComboBox();
		cboCoder4.setName("Coder4");
		cboCoder4.setModel(getCoder(4));
		cboCoder4.putClientProperty("Tip", 4);
		cboCoder4.addFocusListener(this);
		cboCoder4.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						ordergroup.value4 = cb.getIndex();
						altered = true;
						setToolTip(4);
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, columns[7]);
		label.setLabelFor(cboCoder4);
		IGUI.addComponent(label, 0, 6, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboCoder4, 1, 6, 2, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, columns[8]);
		IGUI.addComponent(label, 0, 7, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblValue5 = IGUI.createJLabel(SwingConstants.LEFT, 0, "   0");
		IGUI.addComponent(lblValue5, 1, 7, 2, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		IGUI.addComponent(scrollToolTip, 0, 8, 3, 3, 1.0, 0.25, GridBagConstraints.BOTH, GridBagConstraints.EAST,
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

	@Override
	public void focusGained(FocusEvent e) {
		Component c = (Component) e.getSource();
		if (c instanceof IComboBox) {
			IComboBox cc = (IComboBox) c;
			String s = (String) cc.getClientProperty("Tip");
			int i = Integer.valueOf(s);
			setToolTip(i);
			return;
		}
		setToolTip(0);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ORG_SELECT));
		try {
			while (rst.next()) {
				ordergroup = new OOrderGroup();
				ordergroup.grpID = rst.getShort("OGID");
				ordergroup.typID = rst.getShort("OTID");
				ordergroup.value1 = rst.getShort("OGC1");
				ordergroup.value2 = rst.getShort("OGC2");
				ordergroup.value3 = rst.getShort("OGC3");
				ordergroup.value4 = rst.getShort("OGC4");
				ordergroup.value5 = rst.getShort("OGC5");
				ordergroup.name = rst.getString("OGNM").trim();
				ordergroup.descr = rst.getString("OGDC").trim();
				list.add(ordergroup);
				if (newID < ordergroup.grpID) {
					newID = ordergroup.grpID;
				}
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
			newID++;
			// Add a blank row
			ordergroup = new OOrderGroup();
			ordergroup.newRow = true;
			list.add(ordergroup);
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
				list.add(new OItem(rst.getShort("COID"), rst.getString("CONM")));
				switch (coderID) {
				case 1:
					mapCoder1.put(rst.getShort("COID"), rst.getString("CODC"));
					break;
				case 2:
					mapCoder2.put(rst.getShort("COID"), rst.getString("CODC"));
					break;
				case 3:
					mapCoder3.put(rst.getShort("COID"), rst.getString("CODC"));
					break;
				default:
					mapCoder4.put(rst.getShort("COID"), rst.getString("CODC"));
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
		return list.toArray();
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("ordergroups.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1.5f, 4, 1, 1, 1, 1, 1, 1 };
		String str = "Orders Groups - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
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
					case ORG_NO:
						paragraph.add(new Chunk(pj.numbers.formatNumber(list.get(i).grpID)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case ORG_NAME:
						paragraph.add(new Chunk(list.get(i).name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_DESCR:
						paragraph.add(new Chunk(list.get(i).descr));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_TYPE:
						paragraph.add(new Chunk(cboTypes.getItemName(list.get(i).typID)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_VAL1:
						paragraph.add(new Chunk(cboCoder1.getItemName(list.get(i).value1)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_VAL2:
						paragraph.add(new Chunk(cboCoder2.getItemName(list.get(i).value2)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_VAL3:
						paragraph.add(new Chunk(cboCoder3.getItemName(list.get(i).value3)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case ORG_VAL4:
						paragraph.add(new Chunk(cboCoder4.getItemName(list.get(i).value4)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(pj.numbers.formatNumber(list.get(i).value5)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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
		byte index = DPowerJ.STM_ORG_UPDATE;
		if (ordergroup.newRow) {
			index = DPowerJ.STM_ORG_INSERT;
			ordergroup.grpID = newID;
		}
		ordergroup.name = txtName.getText().trim();
		if (ordergroup.name.length() > 8) {
			ordergroup.name = ordergroup.name.substring(0, 8);
		}
		ordergroup.descr = txtDescr.getText().trim();
		if (ordergroup.descr.length() > 64) {
			ordergroup.descr = ordergroup.descr.substring(0, 64);
		}
		pj.dbPowerJ.setShort(pjStms.get(index), 1, ordergroup.typID);
		pj.dbPowerJ.setShort(pjStms.get(index), 2, ordergroup.value1);
		pj.dbPowerJ.setShort(pjStms.get(index), 3, ordergroup.value2);
		pj.dbPowerJ.setShort(pjStms.get(index), 4, ordergroup.value3);
		pj.dbPowerJ.setShort(pjStms.get(index), 5, ordergroup.value4);
		pj.dbPowerJ.setString(pjStms.get(index), 6, ordergroup.name);
		pj.dbPowerJ.setString(pjStms.get(index), 7, ordergroup.descr);
		pj.dbPowerJ.setShort(pjStms.get(index), 8, ordergroup.grpID);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (ordergroup.newRow) {
				ordergroup.newRow = false;
				newID++;
				// Add a blank row
				OOrderGroup nextOrdergroup = new OOrderGroup();
				nextOrdergroup.newRow = true;
				list.add(nextOrdergroup);
				model.fireTableRowsInserted(list.size() - 1, list.size() - 1);

			}
		}
	}

	@Override
	void setFilter(short id, short value) {
		final short v = value;
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		if (value > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).typID == v);
				}
			};
		}
		TableRowSorter<ModelOrderGroup> sorter = (TableRowSorter<ModelOrderGroup>) tbl.getRowSorter();
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
		txtName.setText(ordergroup.name);
		txtDescr.setText(ordergroup.descr);
		lblValue5.setText(pj.numbers.formatNumber(ordergroup.value5 / 60));
		cboTypes.setIndex(ordergroup.typID);
		cboCoder1.setIndex(ordergroup.value1);
		cboCoder2.setIndex(ordergroup.value2);
		cboCoder3.setIndex(ordergroup.value3);
		cboCoder4.setIndex(ordergroup.value4);
		programmaticChange = false;
		setToolTip(0);
	}

	private void setToolTip(int coderID) {
		switch (coderID) {
		case 1:
			txtToolTip.setText(mapCoder1.get(ordergroup.value1));
			break;
		case 2:
			txtToolTip.setText(mapCoder2.get(ordergroup.value2));
			break;
		case 3:
			txtToolTip.setText(mapCoder3.get(ordergroup.value3));
			break;
		case 4:
			txtToolTip.setText(mapCoder4.get(ordergroup.value4));
			break;
		default:
			txtToolTip.setText(null);
		}
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("ordergroups.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Orders Groups");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					"Orders Groups - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
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
			for (int row = 0; row < tbl.getRowCount(); row++) {
				i = tbl.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case ORG_NO:
						xlsCell.setCellValue(list.get(i).grpID);
						break;
					case ORG_NAME:
						xlsCell.setCellValue(list.get(i).name);
						break;
					case ORG_DESCR:
						xlsCell.setCellValue(list.get(i).descr);
						break;
					case ORG_TYPE:
						xlsCell.setCellValue(cboTypes.getItemName(list.get(i).typID));
						break;
					case ORG_VAL1:
						xlsCell.setCellValue(cboCoder1.getItemName(list.get(i).value1));
						break;
					case ORG_VAL2:
						xlsCell.setCellValue(cboCoder2.getItemName(list.get(i).value2));
						break;
					case ORG_VAL3:
						xlsCell.setCellValue(cboCoder3.getItemName(list.get(i).value3));
						break;
					case ORG_VAL4:
						xlsCell.setCellValue(cboCoder4.getItemName(list.get(i).value4));
						break;
					default:
						xlsCell.setCellValue(list.get(i).value5);
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
				value = list.get(row).name;
			}
			return value;
		}
	}
}