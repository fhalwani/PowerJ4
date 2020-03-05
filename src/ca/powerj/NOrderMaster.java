package ca.powerj;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

class NOrderMaster extends NBase {
	private int rowIndex = 0;
	private OOrderMaster ordermaster = new OOrderMaster();
	private ArrayList<OOrderMaster> list = new ArrayList<OOrderMaster>();
	private HashMap<Short, String> mapGroup = new HashMap<Short, String>();
	private ModelOrdMstr model;
	private ITable tbl;
	private JLabel lblName, lblDescr;
	private IComboBox cboGroup;
	private JTextArea txtGroup;

	NOrderMaster(AClient parent) {
		super(parent);
		setName("Orders");
		parent.dbPowerJ.prepareStpOrdMstr();
		getData();
		createPanel();
		programmaticChange = false;
	}

	boolean close() {
		if (super.close()) {
			list.clear();
			mapGroup.clear();
		}
		return !altered;
	}

	private void createPanel() {
		mapGroup = pj.dbPowerJ.getOrderGroupMap();
		model = new ModelOrdMstr();
		tbl = new ITable(pj, model);
		// This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		tbl.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (e.getValueIsAdjusting()) return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) return;
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
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblName, 1, 0, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		lblDescr = IGUI.createJLabel(SwingConstants.LEFT, 0, "");
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Descr:");
		IGUI.addComponent(label, 0, 1, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblDescr, 1, 1, 2, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		cboGroup = new IComboBox();
		cboGroup.setName("cboGroup");
		cboGroup.setModel(pj.dbPowerJ.getOrderGroupArray(false));
		cboGroup.addFocusListener(this);
		cboGroup.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox)e.getSource();
						ordermaster.grpID = cb.getIndex();
						altered = true;
						txtGroup.setText(mapGroup.get(ordermaster.grpID));
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_G, "Group:");
		label.setLabelFor(cboGroup);
		IGUI.addComponent(label, 0, 2, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboGroup, 1, 2, 2, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		txtGroup = new JTextArea();
		txtGroup.setName("txtGroup");
		txtGroup.setMargin(new Insets(4, 4, 4, 4));
		txtGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtGroup.setFont(LConstants.APP_FONT);
		txtGroup.setEditable(false);
		txtGroup.setFocusable(false);
		txtGroup.setLineWrap(true);
		txtGroup.setWrapStyleWord(true);
		JScrollPane scrollOrdGrp = IGUI.createJScrollPane(txtGroup);
		IGUI.addComponent(scrollOrdGrp, 0, 3, 3, 3, 1.0, 0.5,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
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
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ORM_SELECT);
		try {
			while (rst.next()) {
				ordermaster = new OOrderMaster();
				ordermaster.ordID = rst.getShort("OMID");
				ordermaster.grpID = rst.getShort("OGID");
				ordermaster.name  = rst.getString("OMNM").trim();
				ordermaster.descr = rst.getString("OMDC").trim();
				list.add(ordermaster);
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	void save() {
		pj.dbPowerJ.setShort(DPowerJ.STM_ORM_UPDATE,  1, ordermaster.grpID);
		pj.dbPowerJ.setString(DPowerJ.STM_ORM_UPDATE, 2, ordermaster.name.trim());
		pj.dbPowerJ.setString(DPowerJ.STM_ORM_UPDATE, 3, ordermaster.descr.trim());
		pj.dbPowerJ.setShort(DPowerJ.STM_ORM_UPDATE,  4, ordermaster.ordID);
		if (pj.dbPowerJ.execute(DPowerJ.STM_ORM_UPDATE) > 0) {
			altered = false;
		}
	}

	@Override
	void setFilter(short id, short value) {
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		if (id == IToolBar.TB_ORG && value >= 0) {
			final short v = value;
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).grpID == v);
				}
			};
		}
		TableRowSorter<ModelOrdMstr> sorter = (TableRowSorter<ModelOrdMstr>) tbl.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		ordermaster = list.get(rowIndex);
		lblName.setText(ordermaster.name);
		lblDescr.setText(ordermaster.descr);
		cboGroup.setIndex(ordermaster.grpID);
		txtGroup.setText(mapGroup.get(ordermaster.grpID));
		programmaticChange = false;
	}

	private class ModelOrdMstr extends ITableModel {

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