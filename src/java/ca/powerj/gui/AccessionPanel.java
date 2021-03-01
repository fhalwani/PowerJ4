package ca.powerj.gui;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
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
import ca.powerj.data.AccessionData;
import ca.powerj.data.SpecialtyList;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IUtilities;

class AccessionPanel extends BasePanel {
	private int rowIndex = 0;
	private AccessionData accession = new AccessionData();
	private ArrayList<AccessionData> accessions = new ArrayList<AccessionData>();
	private ITable table;
	private JLabel lblName;
	private JCheckBox ckbWorkflow, ckbWorkload;
	private IComboBox cboSpecialties;
	private ModelAccession model;

	AccessionPanel(AppFrame application) {
		super(application);
		setName("Accessions");
		application.dbPowerJ.setStatements(LibConstants.ACTION_ACCESSION);
		accessions = application.dbPowerJ.getAccessionsList();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			accessions.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelAccession();
		table = new ITable(model, application.dates, application.numbers);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		table.addAncestorListener(new IFocusListener());
		table.addFocusListener(this);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
					setRow(table.convertRowIndexToModel(index));
				}
			}
		});
		table.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IUtilities.createJScrollPane(table);
		JPanel pnlData = new JPanel();
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		// Read only, cannot be edited
		lblName = IUtilities.createJLabel(SwingConstants.LEFT, 0, "");
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(lblName, 1, 0, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		ckbWorkflow = new JCheckBox("Workflow");
		ckbWorkflow.setMnemonic(KeyEvent.VK_F);
		ckbWorkflow.setFont(LibConstants.APP_FONT);
		ckbWorkflow.addFocusListener(this);
		ckbWorkflow.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					accession.setWorkflow(ckbWorkflow.isSelected());
					altered = true;
				}
			}
		});
		IUtilities.addComponent(ckbWorkflow, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		ckbWorkload = new JCheckBox("Workload");
		ckbWorkload.setMnemonic(KeyEvent.VK_L);
		ckbWorkload.setFont(LibConstants.APP_FONT);
		ckbWorkload.addFocusListener(this);
		ckbWorkload.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					accession.setWorkload(ckbWorkload.isSelected());
					altered = true;
				}
			}
		});
		IUtilities.addComponent(ckbWorkload, 1, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		SpecialtyList lstSpecialties = application.dbPowerJ.getSpecialties(false);
		cboSpecialties = new IComboBox();
		cboSpecialties.setName("Specialties");
		cboSpecialties.setItems(lstSpecialties.getAll());
		cboSpecialties.addFocusListener(this);
		cboSpecialties.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						accession.setSpyID((byte) cb.getIndex());
						altered = true;
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_S, "Specialty:");
		label.setLabelFor(cboSpecialties);
		IUtilities.addComponent(label, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboSpecialties, 1, 2, 2, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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

	@Override
	void save() {
		if (application.dbPowerJ.setAccession(false, accession) > 0) {
			altered = false;
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		accession = accessions.get(rowIndex);
		lblName.setText(accession.getName());
		ckbWorkflow.setSelected(accession.isWorkflow());
		ckbWorkload.setSelected(accession.isWorkload());
		cboSpecialties.setIndex(accession.getSpyID());
		programmaticChange = false;
	}

	private class ModelAccession extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return accessions.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (accessions.size() > 0 && row < accessions.size()) {
				value = accessions.get(row).getName();
			}
			return value;
		}
	}
}