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

class NAccession extends NBase {
	private int rowIndex = 0;
	private OAccession accession = new OAccession();
	private ArrayList<OAccession> list = new ArrayList<OAccession>();
	ModelAccession model;
	private ITable tbl;
	private JLabel lblName;
	private JCheckBox ckbWorkflow, ckbWorkload;
	private IComboBox cboSpecialties;

	NAccession(AClient parent) {
		super(parent);
		setName("Accessions");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_ACCESSION);
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
		model = new ModelAccession();
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
		JPanel pnlData = new JPanel();
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		// Read only, cannot be edited
		lblName = IGUI.createJLabel(SwingConstants.LEFT, 0, "");
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblName, 1, 0, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		ckbWorkflow = new JCheckBox("Workflow");
		ckbWorkflow.setMnemonic(KeyEvent.VK_F);
		ckbWorkflow.setFont(LConstants.APP_FONT);
		ckbWorkflow.addFocusListener(this);
		ckbWorkflow.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					accession.workflow = ckbWorkflow.isSelected();
					altered = true;
				}
			}
		});
		IGUI.addComponent(ckbWorkflow, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		ckbWorkload = new JCheckBox("Workload");
		ckbWorkload.setMnemonic(KeyEvent.VK_L);
		ckbWorkload.setFont(LConstants.APP_FONT);
		ckbWorkload.addFocusListener(this);
		ckbWorkload.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					accession.workload = ckbWorkload.isSelected();
					altered = true;
				}
			}
		});
		IGUI.addComponent(ckbWorkload, 1, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboSpecialties = new IComboBox();
		cboSpecialties.setName("Specialties");
		cboSpecialties.setModel(pj.dbPowerJ.getSpecialties(false));
		cboSpecialties.addFocusListener(this);
		cboSpecialties.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						accession.spyID = (byte) cb.getIndex();
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_S, "Specialty:");
		label.setLabelFor(cboSpecialties);
		IGUI.addComponent(label, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboSpecialties, 1, 2, 2, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		pnlData.setMinimumSize(new Dimension(300, 300));
		pnlSplit.setPreferredSize(new Dimension(600, 300));
		add(pnlSplit);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ACC_SELECT));
		try {
			while (rst.next()) {
				accession = new OAccession();
				accession.accID = rst.getShort("acid");
				accession.spyID = rst.getByte("syid");
				accession.name = rst.getString("acnm").trim();
				accession.workflow = (rst.getString("acfl").equalsIgnoreCase("Y"));
				accession.workload = (rst.getString("acld").equalsIgnoreCase("Y"));
				list.add(accession);
			}
			pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(list.size()));
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	void save() {
		pj.dbPowerJ.setByte(pjStms.get(DPowerJ.STM_ACC_UPDATE), 1, accession.spyID);
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_ACC_UPDATE), 2, (accession.workflow ? "Y" : "N"));
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_ACC_UPDATE), 3, (accession.workload ? "Y" : "N"));
		pj.dbPowerJ.setString(pjStms.get(DPowerJ.STM_ACC_UPDATE), 4, accession.name.trim());
		pj.dbPowerJ.setShort(pjStms.get(DPowerJ.STM_ACC_UPDATE), 5, accession.accID);
		if (pj.dbPowerJ.execute(pjStms.get(DPowerJ.STM_ACC_UPDATE)) > 0) {
			altered = false;
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		accession = list.get(rowIndex);
		lblName.setText(accession.name);
		ckbWorkflow.setSelected(accession.workflow);
		ckbWorkload.setSelected(accession.workload);
		cboSpecialties.setIndex(accession.spyID);
		programmaticChange = false;
	}

	private class ModelAccession extends ITableModel {

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