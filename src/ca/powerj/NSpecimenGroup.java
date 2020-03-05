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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
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

class NSpecimenGroup extends NBase {
	private final byte FILTER_SPY = 0;
	private final byte FILTER_SUB = 1;
	private final byte FILTER_PRO = 2;
	private short newID = 0;
	private int rowIndex = 0;
	private short[] filters = { 0, 0, 0 };
	private OSpecGroup specimen = new OSpecGroup();
	private ArrayList<OSpecGroup> specimens = new ArrayList<OSpecGroup>();
	private HashMap<Byte, String> procedures = new HashMap<Byte, String>();
	private HashMap<Short, String> coder1 = new HashMap<Short, String>();
	private HashMap<Short, String> coder2 = new HashMap<Short, String>();
	private HashMap<Short, String> coder3 = new HashMap<Short, String>();
	private HashMap<Short, String> coder4 = new HashMap<Short, String>();
	private ModelSpecimen modelList;
	private ModelCode modelCodes;
	private JLabel lblValue5;
	private ITextString txtDescr;
	private IComboBox cboProcedure, cboSubspecial;
	private JTextArea txtToolTip;
	private JCheckBox ckbLN;
	private ITable tblList, tblCodes;

	NSpecimenGroup(AClient parent) {
		super(parent);
		setName("Specimens");
		parent.dbPowerJ.prepareStpSpeGroup();
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
		IGUI.addComponent(txtDescr, 1, 0, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		IGUI.addComponent(label, 0, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboProcedure, 1, 2, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_U, "Subspecialty:");
		label.setLabelFor(cboSubspecial);
		IGUI.addComponent(label, 0, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboSubspecial, 1, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		IGUI.addComponent(ckbLN, 0, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, pj.setup.getString(LSetup.VAR_V5_NAME) + ":");
		IGUI.addComponent(label, 0, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblValue5 = IGUI.createJLabel(SwingConstants.LEFT, 0, "   0");
		IGUI.addComponent(lblValue5, 1, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
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
				column.setCellEditor(new DefaultCellEditor(cboCoder1));
				break;
			case 2:
				column.setCellEditor(new DefaultCellEditor(cboCoder2));
				break;
			case 3:
				column.setCellEditor(new DefaultCellEditor(cboCoder3));
				break;
			default:
				column.setCellEditor(new DefaultCellEditor(cboCoder4));
			}
		}
		JScrollPane scrollCodes = IGUI.createJScrollPane(tblCodes);
		IGUI.addComponent(scrollCodes, 0, 6, 2, 2, 1.0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		IGUI.addComponent(scrollToolTip, 0, 7, 2, 2, 1.0, 0, GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
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

		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SPG_SELECT);
		try {
			while (rst.next()) {
				specimen = new OSpecGroup();
				specimen.spyID = rst.getByte("SYID");
				specimen.subID = rst.getByte("SBID");
				specimen.proID = rst.getByte("POID");
				specimen.grpID = rst.getShort("SGID");
				specimen.value5 = rst.getInt("SGV5");
				specimen.hasLN = (rst.getString("SGLN").toUpperCase().equals("Y"));
				specimen.codes[0][0] = new OItem(rst.getShort("SG1B"), rst.getString("C1NB"));
				specimen.codes[0][1] = new OItem(rst.getShort("SG2B"), rst.getString("C2NB"));
				specimen.codes[0][2] = new OItem(rst.getShort("SG3B"), rst.getString("C2NB"));
				specimen.codes[0][3] = new OItem(rst.getShort("SG4B"), rst.getString("C4NB"));
				specimen.codes[1][0] = new OItem(rst.getShort("SG1M"), rst.getString("C1NM"));
				specimen.codes[1][1] = new OItem(rst.getShort("SG2M"), rst.getString("C2NM"));
				specimen.codes[1][2] = new OItem(rst.getShort("SG3M"), rst.getString("C3NM"));
				specimen.codes[1][3] = new OItem(rst.getShort("SG4M"), rst.getString("C4NM"));
				specimen.codes[2][0] = new OItem(rst.getShort("SG1R"), rst.getString("C1NR"));
				specimen.codes[2][1] = new OItem(rst.getShort("SG2R"), rst.getString("C2NR"));
				specimen.codes[2][2] = new OItem(rst.getShort("SG3R"), rst.getString("C3NR"));
				specimen.codes[2][3] = new OItem(rst.getShort("SG4R"), rst.getString("C4NR"));
				specimen.descr = rst.getString("SGDC").trim();
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
			pj.dbPowerJ.closeRst(rst);
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
		ResultSet rst = pj.dbPowerJ.getResultSet(index);
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("COID"), rst.getString("CONM")));
				switch (coderID) {
				case 1:
					coder1.put(rst.getShort("COID"), rst.getString("CODC"));
					break;
				case 2:
					coder2.put(rst.getShort("COID"), rst.getString("CODC"));
					break;
				case 3:
					coder3.put(rst.getShort("COID"), rst.getString("CODC"));
					break;
				default:
					coder4.put(rst.getShort("COID"), rst.getString("CODC"));
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
		return list.toArray();
	}

	private Object[] getProcedure() {
		ArrayList<OItem> list = new ArrayList<OItem>();
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_PRO_SELECT);
		try {
			while (rst.next()) {
				procedures.put(rst.getByte("POID"), rst.getString("PODC"));
				list.add(new OItem(rst.getByte("POID"), rst.getString("PONM")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
		return list.toArray();
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
		pj.dbPowerJ.setShort(index, 1, specimen.subID);
		pj.dbPowerJ.setShort(index, 2, specimen.proID);
		pj.dbPowerJ.setShort(index, 3, specimen.codes[0][0].id);
		pj.dbPowerJ.setShort(index, 4, specimen.codes[1][0].id);
		pj.dbPowerJ.setShort(index, 5, specimen.codes[2][0].id);
		pj.dbPowerJ.setShort(index, 6, specimen.codes[0][1].id);
		pj.dbPowerJ.setShort(index, 7, specimen.codes[1][1].id);
		pj.dbPowerJ.setShort(index, 8, specimen.codes[2][1].id);
		pj.dbPowerJ.setShort(index, 9, specimen.codes[0][2].id);
		pj.dbPowerJ.setShort(index, 10, specimen.codes[1][2].id);
		pj.dbPowerJ.setShort(index, 11, specimen.codes[2][2].id);
		pj.dbPowerJ.setShort(index, 12, specimen.codes[0][3].id);
		pj.dbPowerJ.setShort(index, 13, specimen.codes[1][3].id);
		pj.dbPowerJ.setShort(index, 14, specimen.codes[2][3].id);
		pj.dbPowerJ.setString(index, 15, (specimen.hasLN ? "Y" : "N"));
		pj.dbPowerJ.setString(index, 16, specimen.descr);
		pj.dbPowerJ.setShort(index, 17, specimen.grpID);
		if (pj.dbPowerJ.execute(index) > 0) {
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
			return specimens.get(row).descr;
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
			if (col == 0) {
				return rows[row];
			}
			return specimen.codes[row][col - 1];
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
			specimen.codes[row][col - 1] = (OItem) value;
			setTooltip(row, col);
		}
	}
}