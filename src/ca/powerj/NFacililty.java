package ca.powerj;
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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class NFacililty extends NBase {
	private int rowIndex = 0;
	private OFacility facility = new OFacility();
	private ArrayList<OFacility> list = new ArrayList<OFacility>();
	private ModelFacility model;
	private ITable tbl;
	private ITextString txtName;
	private JLabel lblDescr;
	private JCheckBox ckbWorkflow, ckbWorkload;

	NFacililty(AClient parent) {
		super(parent);
		setName("Facilities");
		parent.dbPowerJ.prepareStpFacilities();
		getData();
		createPanel();
		programmaticChange = false;
	}

	boolean close() {
		if (super.close()) {
			list.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelFacility();
		tbl = new ITable(pj, model);
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		tbl.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				//Ignore extra messages
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
		JPanel pnlData = new JPanel();
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtName = new ITextString(2, 4);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name: ");
		label.setLabelFor(txtName);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtName, 1, 0, 1, 1, 0.5, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		// Read only, cannot be edited
		lblDescr = IGUI.createJLabel(SwingConstants.LEFT, 0, "");
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Description:");
		IGUI.addComponent(label, 0, 1, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblDescr, 1, 1, 3, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		ckbWorkflow = new JCheckBox("Workflow");
		ckbWorkflow.setMnemonic(KeyEvent.VK_F);
		ckbWorkflow.setFont(LConstants.APP_FONT);
		ckbWorkflow.addFocusListener(this);
		ckbWorkflow.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					facility.workflow = ckbWorkflow.isSelected();
					altered = true;
				}
			}
		});
		ckbWorkload = new JCheckBox("Workload");
		ckbWorkload.setMnemonic(KeyEvent.VK_L);
		ckbWorkload.setFont(LConstants.APP_FONT);
		ckbWorkload.addFocusListener(this);
		ckbWorkload.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					facility.workload = ckbWorkload.isSelected();
					altered = true;
				}
			}
		});
		IGUI.addComponent(ckbWorkflow, 0, 2, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(ckbWorkload, 1, 2, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
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

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_FAC_SELECT);
		try {
			while (rst.next()) {
				facility = new OFacility();
				facility.facID    = rst.getShort("FAID");
				facility.name     = rst.getString("FANM").trim();
				facility.descr    = rst.getString("FADC").trim();
				facility.workflow = (rst.getString("FAFL").equalsIgnoreCase("Y"));
				facility.workload = (rst.getString("FALD").equalsIgnoreCase("Y"));
				list.add(facility);
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	void save() {
		pj.dbPowerJ.setString(DPowerJ.STM_FAC_UPDATE, 1, (facility.workflow ? "Y" : "N"));
		pj.dbPowerJ.setString(DPowerJ.STM_FAC_UPDATE, 2, (facility.workload ? "Y" : "N"));
		pj.dbPowerJ.setString(DPowerJ.STM_FAC_UPDATE, 3, facility.name.trim());
		pj.dbPowerJ.setString(DPowerJ.STM_FAC_UPDATE, 4, facility.descr.trim());
		pj.dbPowerJ.setShort(DPowerJ.STM_FAC_UPDATE,  5, facility.facID);
		if (pj.dbPowerJ.execute(DPowerJ.STM_FAC_UPDATE) > 0) {
			altered = false;
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		facility = list.get(rowIndex);
		txtName.setText(facility.name);
		lblDescr.setText(facility.descr);
		ckbWorkflow.setSelected(facility.workflow);
		ckbWorkload.setSelected(facility.workload);
		programmaticChange = false;
	}

	private class ModelFacility extends ITableModel {

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