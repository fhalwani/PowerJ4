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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

class NOrderGroup extends NBase {
	private short newID = 0;
	private int rowIndex = 0;
	private String[] coders = new String[5];
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
		coders[0] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		coders[1] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		coders[2] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		coders[3] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		coders[4] = pj.setup.getString(LSetup.VAR_V5_NAME);
		pj.dbPowerJ.prepareStpOrdGroup();
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
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, coders[0]);
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
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, coders[1]);
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
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, coders[2]);
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
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, coders[3]);
		label.setLabelFor(cboCoder4);
		IGUI.addComponent(label, 0, 6, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboCoder4, 1, 6, 2, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, coders[4]);
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
			int i = (int) cc.getClientProperty("Tip");
			setToolTip(i);
			return;
		}
		setToolTip(0);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ORG_SELECT);
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
			pj.dbPowerJ.closeRst(rst);
		}
		return list.toArray();
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
		pj.dbPowerJ.setShort(index, 1, ordergroup.typID);
		pj.dbPowerJ.setShort(index, 2, ordergroup.value1);
		pj.dbPowerJ.setShort(index, 3, ordergroup.value2);
		pj.dbPowerJ.setShort(index, 4, ordergroup.value3);
		pj.dbPowerJ.setShort(index, 5, ordergroup.value4);
		pj.dbPowerJ.setString(index, 6, ordergroup.name);
		pj.dbPowerJ.setString(index, 7, ordergroup.descr);
		pj.dbPowerJ.setShort(index, 8, ordergroup.grpID);
		if (pj.dbPowerJ.execute(index) > 0) {
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
			return list.get(row).name;
		}
	}
}