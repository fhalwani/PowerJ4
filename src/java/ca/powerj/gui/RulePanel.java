package ca.powerj.gui;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
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
import ca.powerj.data.RuleData;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class RulePanel extends BasePanel {
	private int rowIndex = 0;
	private RuleData rule = new RuleData();
	private ArrayList<RuleData> rules = null;
	private ITextString txtName;
	private JTextArea txtDescr;
	private ModelRule model;
	private ITable table;

	RulePanel(AppFrame application) {
		super(application);
		setName("Rules");
		application.dbPowerJ.setStatements(LibConstants.ACTION_RULES);
		rules = application.dbPowerJ.getRules();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			rules.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelRule();
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
		scrollTable.setMinimumSize(new Dimension(200, 400));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 400));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtName = new ITextString(2, 16);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
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
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtName, 1, 0, 3, 1, 0.75, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(scrollText, 0, 1, 4, 4, 1.0, 1.0, GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 400));
		add(pnlSplit);
	}

	@Override
	void save() {
		rule.setName(txtName.getText().trim());
		if (rule.getName().length() > 16) {
			rule.setName(rule.getName().substring(0, 16));
		}
		rule.setDescription(txtDescr.getText().trim());
		if (rule.getDescription().length() > 256) {
			rule.setDescription(rule.getDescription().substring(0, 256));
		}
		if (application.dbPowerJ.setRule(rule) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		rule = rules.get(rowIndex);
		txtName.setText(rule.getName());
		txtDescr.setText(rule.getDescription());
		programmaticChange = false;
	}

	private class ModelRule extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return rules.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (rules.size() > 0 && row < rules.size()) {
				value = rules.get(row).getName();
			}
			return value;
		}
	}
}