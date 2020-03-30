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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class NSpecialty extends NBase {
	private byte newID    = 0;
	private int  rowIndex = 0;
	private OSpecialty specialty = new OSpecialty();
	private ArrayList<OSpecialty> list = new ArrayList<OSpecialty>();
	private ModelSpecialty model;
	private ITable tbl;
	private ITextString txtName;
	private JCheckBox ckbWorkflow, ckbWorkload, ckbSpecimen;

	NSpecialty(AClient parent) {
		super(parent);
		setName("Specialties");
		parent.dbPowerJ.prepareStpSpecialties();
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
		model = new ModelSpecialty();
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
		scrollTable.setMinimumSize(new Dimension(200, 300));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 300));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtName = new ITextString(2, 16);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		ckbWorkflow = new JCheckBox("Workflow");
		ckbWorkflow.setMnemonic(KeyEvent.VK_F);
		ckbWorkflow.setFont(LConstants.APP_FONT);
		ckbWorkflow.addFocusListener(this);
		ckbWorkflow.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					specialty.workflow = ckbWorkflow.isSelected();
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
					specialty.workload = ckbWorkload.isSelected();
					altered = true;
				}
			}
		});
		ckbSpecimen = new JCheckBox("Code Specimen");
		ckbSpecimen.setMnemonic(KeyEvent.VK_C);
		ckbSpecimen.setFont(LConstants.APP_FONT);
		ckbSpecimen.addFocusListener(this);
		ckbSpecimen.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					specialty.specimen = ckbSpecimen.isSelected();
					altered = true;
				}
			}
		});
		IGUI.addComponent(txtName, 0, 0, 4, 1, 1.0, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(ckbWorkflow, 0, 1, 1, 1, 0.25, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(ckbWorkload, 1, 1, 1, 1, 0.25, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(ckbSpecimen, 2, 1, 1, 1, 0.25, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		add(pnlSplit);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SPY_SELECT);
		try {
			while (rst.next()) {
				specialty = new OSpecialty();
				specialty.spyID = rst.getByte("SYID");
				specialty.name = rst.getString("SYNM").trim();
				specialty.workflow = (rst.getString("SYFL").equalsIgnoreCase("Y"));
				specialty.workload = (rst.getString("SYLD").equalsIgnoreCase("Y"));
				specialty.specimen =  (rst.getString("SYSP").equalsIgnoreCase("Y"));
				list.add(specialty);
				if (newID < specialty.spyID) {
					newID = specialty.spyID;
				}
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
			newID++;
			// Add a blank row
			specialty = new OSpecialty();
			specialty.newRow = true;
			list.add(specialty);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	void save() {
		byte index = DPowerJ.STM_SPY_UPDATE;
		if (specialty.newRow) {
			index = DPowerJ.STM_SPY_INSERT;
			specialty.spyID = newID;
		}
		specialty.name = specialty.name.trim();
		if (specialty.name.length() > 16) {
			specialty.name = specialty.name.substring(0, 16);
		}
		pj.dbPowerJ.setString(index, 1, (specialty.workflow ? "Y" : "N"));
		pj.dbPowerJ.setString(index, 2, (specialty.workload ? "Y" : "N"));
		pj.dbPowerJ.setString(index, 3, (specialty.specimen ? "Y" : "N"));
		pj.dbPowerJ.setString(index, 4, specialty.name);
		pj.dbPowerJ.setByte(index,   5, specialty.spyID);
		if (pj.dbPowerJ.execute(index) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (specialty.newRow) {
				specialty.newRow = false;
				newID++;
				// Add a blank row
				OSpecialty newSpec = new OSpecialty();
				newSpec.newRow = true;
				list.add(newSpec);
				model.fireTableRowsInserted(newID, newID);

			}
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		specialty = list.get(rowIndex);
		txtName.setText(specialty.name);
		ckbWorkflow.setSelected(specialty.workflow);
		ckbWorkload.setSelected(specialty.workload);
		ckbSpecimen.setSelected(specialty.specimen);
		programmaticChange = false;
	}

	private class ModelSpecialty extends ITableModel {

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