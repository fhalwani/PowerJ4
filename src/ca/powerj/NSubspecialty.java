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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class NSubspecialty extends NBase {
	private byte newID = 0;
	private int rowIndex = 0;
	private OSubspecialty subspecialty = new OSubspecialty();
	private ArrayList<OSubspecialty> list = new ArrayList<OSubspecialty>();
	private ITable tbl;
	private ModelSubspecial model;
	private ITextString txtName, txtDescr;
	private IComboBox cboSpecialties;

	NSubspecialty(AClient parent) {
		super(parent);
		setName("Subspecialties");
		parent.dbPowerJ.prepareStpSubspecialty();
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
		model = new ModelSubspecial();
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
		txtName = new ITextString(2, 8);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtName, 1, 0, 2, 1, 0.5, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		txtDescr = new ITextString(2, 32);
		txtDescr.setName("Descr");
		txtDescr.addFocusListener(this);
		txtDescr.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Description:");
		label.setLabelFor(txtDescr);
		IGUI.addComponent(label, 0, 1, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtDescr, 1, 1, 3, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		cboSpecialties = new IComboBox();
		cboSpecialties.setName("Specialties");
		cboSpecialties.setModel(pj.dbPowerJ.getSpecialties(false));
		cboSpecialties.addFocusListener(this);
		cboSpecialties.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox)e.getSource();
						subspecialty.spyID = (byte) cb.getIndex();
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_S, "Specialty:");
		label.setLabelFor(cboSpecialties);
		IGUI.addComponent(label, 0, 2, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboSpecialties, 1, 2, 2, 1, 0.5, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		add(pnlSplit);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SUB_SELECT);
		try {
			while (rst.next()) {
				subspecialty = new OSubspecialty();
				subspecialty.subID = rst.getByte("SBID");
				subspecialty.spyID = rst.getByte("SYID");
				subspecialty.name  = rst.getString("SBNM").trim();
				subspecialty.descr = rst.getString("SBDC").trim();
				list.add(subspecialty);
				if (newID < subspecialty.subID) {
					newID = subspecialty.subID;
				}
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
			newID++;
			// Add a blank row
			subspecialty = new OSubspecialty();
			subspecialty.newRow = true;
			list.add(subspecialty);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Variables", e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	void save() {
		byte index = DPowerJ.STM_SUB_UPDATE;
		if (subspecialty.newRow) {
			index = DPowerJ.STM_SUB_INSERT;
			subspecialty.subID = newID;
		}
		subspecialty.name = txtName.getText().trim();
		if (subspecialty.name.length() > 8) {
			subspecialty.name = subspecialty.name.substring(0, 8);
		}
		subspecialty.descr = txtDescr.getText().trim();
		if (subspecialty.descr.length() > 32) {
			subspecialty.descr = subspecialty.descr.substring(0, 32);
		}
		pj.dbPowerJ.setByte(index,   1, subspecialty.spyID);
		pj.dbPowerJ.setString(index, 2, subspecialty.name);
		pj.dbPowerJ.setString(index, 3, subspecialty.descr);
		pj.dbPowerJ.setByte(index,   4, subspecialty.subID);
		if (pj.dbPowerJ.execute(index) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (subspecialty.newRow) {
				subspecialty.newRow = false;
				newID++;
				// Add a blank row
				OSubspecialty nextSub = new OSubspecialty();
				nextSub.newRow = true;
				list.add(nextSub);
				model.fireTableRowsInserted(list.size()-1, list.size()-1);
			}
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		subspecialty = list.get(rowIndex);
		txtName.setText(subspecialty.name);
		txtDescr.setText(subspecialty.descr);
		cboSpecialties.setIndex(subspecialty.spyID);
		programmaticChange = false;
	}

	private class ModelSubspecial extends ITableModel {

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