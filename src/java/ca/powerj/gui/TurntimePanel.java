package ca.powerj.gui;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
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
import ca.powerj.data.TurnaroundData;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextInteger;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class TurntimePanel extends BasePanel {
	private byte newID = 0;
	private int totalTime = 0;
	private int rowIndex = 0;
	private TurnaroundData turnaround = new TurnaroundData();
	private ArrayList<TurnaroundData> list = new ArrayList<TurnaroundData>();
	private ModelTurnaround model;
	private ITable table;
	private JLabel lblTotal;
	private ITextString txtName;
	private ITextInteger txtGross, txtEmbed, txtMicro, txtRoute, txtDiagn;

	TurntimePanel(AppFrame application) {
		super(application);
		setName("Turnaround");
		application.dbPowerJ.setStatements(LibConstants.ACTION_TURNMASTER);
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			list.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelTurnaround();
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
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtName, 1, 0, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		txtGross = new ITextInteger(application.numbers, 1, 200);
		txtGross.setName("Gross");
		txtGross.addFocusListener(this);
		txtGross.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_G, "Gross:");
		label.setLabelFor(txtGross);
		IUtilities.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtGross, 1, 1, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtEmbed = new ITextInteger(application.numbers, 1, 200);
		txtEmbed.setName("Embed");
		txtEmbed.addFocusListener(this);
		txtEmbed.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_E, "Embed:");
		label.setLabelFor(txtEmbed);
		IUtilities.addComponent(label, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtEmbed, 1, 2, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtMicro = new ITextInteger(application.numbers, 1, 200);
		txtMicro.setName("Embed");
		txtMicro.addFocusListener(this);
		txtMicro.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_M, "Microtomy:");
		label.setLabelFor(txtMicro);
		IUtilities.addComponent(label, 0, 3, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtMicro, 1, 3, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtRoute = new ITextInteger(application.numbers, 1, 200);
		txtRoute.setName("Route");
		txtRoute.addFocusListener(this);
		txtRoute.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_R, "Route:");
		label.setLabelFor(txtRoute);
		IUtilities.addComponent(label, 0, 4, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtRoute, 1, 4, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtDiagn = new ITextInteger(application.numbers, 1, 200);
		txtDiagn.setName("Diagnosis");
		txtDiagn.addFocusListener(this);
		txtDiagn.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Diagnosis:");
		label.setLabelFor(txtDiagn);
		IUtilities.addComponent(label, 0, 5, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtDiagn, 1, 5, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Total:");
		lblTotal = IUtilities.createJLabel(SwingConstants.RIGHT, 0, "");
		IUtilities.addComponent(label, 0, 6, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(lblTotal, 1, 6, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		list = application.dbPowerJ.getTurnarounds();
		for (int i = 0; i < list.size(); i++) {
			if (newID < list.get(i).getTurID()) {
				newID = list.get(i).getTurID();
			}
		}
		application.display("No Rows: " + list.size());
		newID++;
		// Add a blank row
		turnaround = new TurnaroundData();
		turnaround.setNewRow(true);
		list.add(turnaround);
	}

	@Override
	void save() {
		if (turnaround.isNewRow()) {
			turnaround.setTurID(newID);
		}
		turnaround.setGross(txtGross.getShort());
		turnaround.setEmbed(txtEmbed.getShort());
		turnaround.setMicrotomy(txtMicro.getShort());
		turnaround.setRoute(txtRoute.getShort());
		turnaround.setDiagnosis(txtDiagn.getShort());
		turnaround.setName(txtName.getText());
		if (application.dbPowerJ.setTurnaround(turnaround) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (turnaround.isNewRow()) {
				turnaround.setNewRow(false);
				newID++;
				// Add a blank row
				TurnaroundData turnaround = new TurnaroundData();
				turnaround.setNewRow(true);
				list.add(turnaround);
				model.fireTableRowsInserted(list.size() - 1, list.size() - 1);

			}
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		turnaround = list.get(rowIndex);
		txtName.setText(turnaround.getName());
		txtGross.setInt(turnaround.getGross());
		txtEmbed.setInt(turnaround.getEmbed());
		txtMicro.setInt(turnaround.getMicrotomy());
		txtRoute.setInt(turnaround.getRoute());
		txtDiagn.setInt(turnaround.getDiagnosis());
		totalTime = turnaround.getGross() + turnaround.getEmbed()
			+ turnaround.getMicrotomy() + turnaround.getRoute()
			+ turnaround.getDiagnosis();
		lblTotal.setText(application.numbers.formatNumber(totalTime));
		programmaticChange = false;
	}

	private class ModelTurnaround extends ITableModel {

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
				value = list.get(row).getName();
			}
			return value;
		}
	}
}