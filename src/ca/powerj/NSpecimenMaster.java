package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

class NSpecimenMaster extends NBase {
	private final byte FILTER_SPY = 0;
	private final byte FILTER_SUB = 1;
	private final byte FILTER_PRO = 2;
	private boolean newCodes = false;
	private short[] filters = { 0, 0, 0 };
	private int rowIndex = 0;
	private OSpecMaster specimenmaster = new OSpecMaster();
	private ArrayList<OSpecMaster> list = new ArrayList<OSpecMaster>();
	private ModelSpecMstr model;
	private ITable tbl;
	private JLabel lblName, lblDescr;
	private IComboBox cboGroup, cboTAT;

	NSpecimenMaster(AClient parent) {
		super(parent);
		setName("Specimens");
		parent.dbPowerJ.prepareStpSpeMstr();
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
		model = new ModelSpecMstr();
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
		cboTAT = new IComboBox();
		cboTAT.setName("cboTurn");
		cboTAT.setModel(getTurnaround());
		cboTAT.addFocusListener(this);
		cboTAT.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimenmaster.turID = (byte) cb.getIndex();
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_T, "Turnaround:");
		label.setLabelFor(cboTAT);
		IGUI.addComponent(label, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboTAT, 1, 2, 2, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		cboGroup = new IComboBox();
		cboGroup.setName("cboGroup");
		cboGroup.setModel(getGroups());
		cboGroup.addFocusListener(this);
		cboGroup.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimenmaster.grpID = cb.getIndex();
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_G, "Group:");
		label.setLabelFor(cboGroup);
		IGUI.addComponent(label, 0, 3, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboGroup, 1, 3, 2, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SPM_SELECT);
		try {
			while (rst.next()) {
				specimenmaster = new OSpecMaster();
				specimenmaster.spcID = rst.getShort("SMID");
				specimenmaster.grpID = rst.getShort("SGID");
				specimenmaster.spyID = rst.getByte("SYID");
				specimenmaster.subID = rst.getByte("SBID");
				specimenmaster.proID = rst.getByte("POID");
				specimenmaster.turID = rst.getByte("TAID");
				specimenmaster.name = rst.getString("SMNM").trim();
				specimenmaster.descr = rst.getString("SMDC").trim();
				list.add(specimenmaster);
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	private Object[] getGroups() {
		ArrayList<OItem> list = new ArrayList<OItem>();
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SPG_SELECT);
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("SGID"), rst.getString("SGDC")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
		return list.toArray();
	}

	private Object[] getTurnaround() {
		ArrayList<OItem> list = new ArrayList<OItem>();
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_TUR_SELECT);
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("TAID"), rst.getString("TANM")));
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
		pj.dbPowerJ.setShort(DPowerJ.STM_SPM_UPDATE, 1, specimenmaster.grpID);
		pj.dbPowerJ.setShort(DPowerJ.STM_SPM_UPDATE, 2, specimenmaster.turID);
		pj.dbPowerJ.setString(DPowerJ.STM_SPM_UPDATE, 3, specimenmaster.name.trim());
		pj.dbPowerJ.setString(DPowerJ.STM_SPM_UPDATE, 4, specimenmaster.descr.trim());
		pj.dbPowerJ.setShort(DPowerJ.STM_SPM_UPDATE, 5, specimenmaster.spcID);
		if (pj.dbPowerJ.execute(DPowerJ.STM_SPM_UPDATE) > 0) {
			altered = false;
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
			newCodes = !newCodes;
		}
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> rowFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[FILTER_SPY] != 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (list.get(entry.getIdentifier()).spyID == filters[FILTER_SPY]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SUB] != 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (list.get(entry.getIdentifier()).subID == filters[FILTER_SUB]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_PRO] != 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (list.get(entry.getIdentifier()).proID == filters[FILTER_PRO]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (newCodes) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (list.get(entry.getIdentifier()).grpID == 0);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (rowFilters.size() > 0) {
			// Add to the compound filter
			rowFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(rowFilters);
		}
		TableRowSorter<ModelSpecMstr> sorter = (TableRowSorter<ModelSpecMstr>) tbl.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		specimenmaster = list.get(rowIndex);
		lblName.setText(specimenmaster.name);
		lblDescr.setText(specimenmaster.descr);
		cboGroup.setIndex(specimenmaster.grpID);
		cboTAT.setIndex(specimenmaster.turID);
		programmaticChange = false;
	}

	private class ModelSpecMstr extends ITableModel {

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
