package ca.powerj.gui;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ca.powerj.data.SpecialtyList;
import ca.powerj.data.SubspecialtyData;
import ca.powerj.data.SubspecialtyList;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class SubspecialtyPanel extends BasePanel {
	private byte newID = 0;
	private int rowIndex = 0;
	private SubspecialtyData subspecialty = new SubspecialtyData();
	private SubspecialtyList subspecialties;
	private ITable table;
	private ModelSubspecialty model;
	private ITextString txtName, txtDescr;
	private IComboBox cboSpecialties;

	SubspecialtyPanel(AppFrame application) {
		super(application);
		setName("Subspecialties");
		application.dbPowerJ.setStatements(LibConstants.ACTION_SUBSPECIAL);
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
		model = new ModelSubspecialty();
		table = new ITable(model, application.dates, application.numbers);
		table.addAncestorListener(new IFocusListener());
		table.addFocusListener(this);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				if (index > -1) {
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
		txtName = new ITextString(2, 8);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtName, 1, 0, 2, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		txtDescr = new ITextString(2, 32);
		txtDescr.setName("Descr");
		txtDescr.addFocusListener(this);
		txtDescr.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Description:");
		label.setLabelFor(txtDescr);
		IUtilities.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtDescr, 1, 1, 3, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		SpecialtyList lstSpecialty = application.dbPowerJ.getSpecialties(false);
		cboSpecialties = new IComboBox();
		cboSpecialties.setName("Specialties");
		cboSpecialties.setItems(lstSpecialty.getAll());
		cboSpecialties.addFocusListener(this);
		cboSpecialties.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						subspecialty.setSpyID((byte) cb.getIndex());
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
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		add(pnlSplit);
	}

	private void getData() {
		subspecialties = application.dbPowerJ.getSubspecialties(false);
		int size = subspecialties.getSize();
		for (int i = 0; i < size; i++) {
			if (newID < subspecialties.get(i).getID()) {
				newID = subspecialties.get(i).getID();
			}
		}
		newID++;
		// Add a blank row
		subspecialty = new SubspecialtyData();
		subspecialties.add(subspecialty);
		application.display("No Rows: " + size);
	}

	@Override
	void save() {
		if (subspecialty.isNewRow()) {
			subspecialty.setID(newID);
		}
		subspecialty.setName(txtName.getText());
		subspecialty.setDescr(txtDescr.getText());
		if (application.dbPowerJ.setSubspecialty(subspecialty) > 0) {
			altered = false;
			subspecialty.setAltered(false);
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (subspecialty.isNewRow()) {
				subspecialty.setNewRow(false);
				newID++;
				// Add a blank row
				SubspecialtyData newSub = new SubspecialtyData();
				subspecialties.add(newSub);
				model.fireTableRowsInserted(subspecialties.getSize() -1, subspecialties.getSize() -1);
			}
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		subspecialty = subspecialties.get(rowIndex);
		txtName.setText(subspecialty.getName());
		txtDescr.setText(subspecialty.getDescr());
		cboSpecialties.setIndex(subspecialty.getSpyID());
		programmaticChange = false;
	}

	private class ModelSubspecialty extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return subspecialties.getSize();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (subspecialties.getSize() > 0 && row < subspecialties.getSize()) {
				value = subspecialties.get(row).getName();
			}
			return value;
		}
	}
}