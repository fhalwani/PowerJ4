package ca.powerj.gui;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ca.powerj.data.ProcedureData;
import ca.powerj.data.ProcedureList;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class ProcedurePanel extends BasePanel {
	private byte newID = 0;
	private int rowIndex = 0;
	private ProcedureData procedure = new ProcedureData();
	private ProcedureList list;
	private ModelProcedure model;
	private ITable tbl;
	private ITextString txtName;
	private JTextArea txtDescr;

	ProcedurePanel(AppFrame application) {
		super(application);
		setName("Procedures");
		application.dbPowerJ.setStatements(LibConstants.ACTION_PROCEDURES);
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
		model = new ModelProcedure();
		tbl = new ITable(model, application.dates, application.numbers);
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
		JScrollPane scrollTable = IUtilities.createJScrollPane(tbl);
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
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtName, 1, 0, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		txtDescr = new JTextArea();
		txtDescr.setName("Descr");
		txtDescr.setMargin(new Insets(4, 4, 4, 4));
		txtDescr.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtDescr.setFont(LibConstants.APP_FONT);
		txtDescr.getDocument().addDocumentListener(this);
		txtDescr.addFocusListener(this);
		txtDescr.setLineWrap(true);
		txtDescr.setWrapStyleWord(true);
		JScrollPane scrollText = IUtilities.createJScrollPane(txtDescr);
		IUtilities.addComponent(scrollText, 0, 1, 3, 3, 0.7, 1.0, GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		add(pnlSplit);
	}

	private void getData() {
		list = application.dbPowerJ.getProcedures(false);
		int size = list.getSize();
		for (int i = 0; i < size; i++) {
			if (newID < list.get(i).getID()) {
				newID = list.get(i).getID();
			}
		}
		newID++;
		// Add a blank row
		procedure = new ProcedureData();
		list.add(procedure);
		application.display("No Rows: " + size);
	}

	@Override
	void save() {
		if (procedure.isNewRow()) {
			procedure.setID(newID);
		}
		procedure.setName(txtName.getText().trim());
		if (procedure.getName().length() > 16) {
			procedure.setName(procedure.getName().substring(0, 16));
		}
		procedure.setDescription(txtDescr.getText().trim());
		if (procedure.getDescription().length() > 256) {
			procedure.setDescription(procedure.getDescription().substring(0, 256));
		}
		if (application.dbPowerJ.setProcedure(procedure) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (procedure.isNewRow()) {
				procedure.setNewRow(false);
				newID++;
				// Add a blank row
				ProcedureData row = new ProcedureData();
				list.add(row);
				model.fireTableRowsInserted(list.getSize() - 1, list.getSize() - 1);
			}
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		procedure = list.get(rowIndex);
		txtName.setText(procedure.getName());
		txtDescr.setText(procedure.getDescription());
		programmaticChange = false;
	}

	private class ModelProcedure extends ITableModel {

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