package ca.powerj.gui;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ca.powerj.data.SpecialtyData;
import ca.powerj.data.SpecialtyList;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class SpecialtyPanel extends BasePanel {
	private byte newID = 0;
	private int rowIndex = 0;
	private SpecialtyData specialty = new SpecialtyData();
	private SpecialtyList list;
	private ModelSpecialty model;
	private ITable table;
	private ITextString txtName;
	private JCheckBox ckbWorkflow, ckbWorkload, ckbSpecimen;

	SpecialtyPanel(AppFrame application) {
		super(application);
		setName("Specialties");
		application.dbPowerJ.setStatements(LibConstants.ACTION_SPECIALTY);
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		super.close();
		return !altered;
	}

	private void createPanel() {
		model = new ModelSpecialty();
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
		ckbWorkflow.setFont(LibConstants.APP_FONT);
		ckbWorkflow.addFocusListener(this);
		ckbWorkflow.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					specialty.setWorkflow(ckbWorkflow.isSelected());
					altered = true;
				}
			}
		});
		ckbWorkload = new JCheckBox("Workload");
		ckbWorkload.setMnemonic(KeyEvent.VK_L);
		ckbWorkload.setFont(LibConstants.APP_FONT);
		ckbWorkload.addFocusListener(this);
		ckbWorkload.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					specialty.setWorkload(ckbWorkload.isSelected());
					altered = true;
				}
			}
		});
		ckbSpecimen = new JCheckBox("Code Specimen");
		ckbSpecimen.setMnemonic(KeyEvent.VK_C);
		ckbSpecimen.setFont(LibConstants.APP_FONT);
		ckbSpecimen.addFocusListener(this);
		ckbSpecimen.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					specialty.setCodeSpecimen(ckbSpecimen.isSelected());
					altered = true;
				}
			}
		});
		IUtilities.addComponent(txtName, 0, 0, 4, 1, 1.0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(ckbWorkflow, 0, 1, 1, 1, 0.25, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(ckbWorkload, 1, 1, 1, 1, 0.25, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(ckbSpecimen, 2, 1, 1, 1, 0.25, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		add(pnlSplit);
	}

	private void getData() {
		list = application.dbPowerJ.getSpecialties(false);
		int size = list.getSize();
		for (int i = 0; i < size; i++) {
			if (newID < list.get(i).getID()) {
				newID = list.get(i).getID();
			}
		}
		newID++;
		// Add a blank row
		specialty = new SpecialtyData();
		specialty.setNewRow(true);
		list.add(specialty);
		application.display("No Rows: " + size);
	}

	@Override
	void save() {
		if (specialty.isNewRow()) {
			specialty.setID(newID);
		}
		if (application.dbPowerJ.setSpecialty(specialty) > 0) {
			altered = false;
			specialty.setAltered(false);
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (specialty.isNewRow()) {
				newID++;
				// Add a blank row
				SpecialtyData newSpec = new SpecialtyData();
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
		txtName.setText(specialty.getName());
		ckbWorkflow.setSelected(specialty.isWorkflow());
		ckbWorkload.setSelected(specialty.isWorkload());
		ckbSpecimen.setSelected(specialty.codeSpecimen());
		programmaticChange = false;
	}

	private class ModelSpecialty extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return list.getSize();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (list.getSize() > 0 && row < list.getSize()) {
				value = list.get(row).getName();
			}
			return value;
		}
	}
}