package ca.powerj;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
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

class NRule extends NBase {
	private int rowIndex = 0;
	private OItem rule = new OItem();
	private ArrayList<OItem> list = new ArrayList<OItem>();
	private ModelRule model;
	private JLabel lblID;
	private ITextString txtName;
	private JTextArea txtDescr;
	private ITable tbl;

	NRule(AClient parent) {
		super(parent);
		setName("Rules");
		parent.dbPowerJ.prepareStpRules();
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
		model = new ModelRule();
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
		scrollTable.setMinimumSize(new Dimension(200, 400));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 400));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		lblID = IGUI.createJLabel(SwingConstants.LEFT, 0, "0  ");
		txtName = new ITextString(2, 16);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		txtDescr = new JTextArea();
		txtDescr.setName("Descr");
		txtDescr.setMargin(new Insets(4, 4, 4, 4));
		txtDescr.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtDescr.setFont(LConstants.APP_FONT);
		txtDescr.getDocument().addDocumentListener(this);
		txtDescr.addFocusListener(this);
		txtDescr.setLineWrap(true);
		txtDescr.setWrapStyleWord(true);
		JScrollPane scrollText = IGUI.createJScrollPane(txtDescr);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtName, 1, 0, 2, 1, 0.6, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblID, 3, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(scrollText, 0, 1, 4, 4, 1.0, 1.0,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 400));
		add(pnlSplit);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_RUL_SELECT);
		try {
			while (rst.next()) {
				rule = new OItem();
				rule.id = rst.getShort("RUID");
				rule.name  = rst.getString("RUNM").trim();
				rule.descr = rst.getString("RUDC").trim();
				list.add(rule);
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Variables", e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	void save() {
		rule.name = txtName.getText().trim();
		if (rule.name.length() > 16) {
			rule.name = rule.name.substring(0, 16);
		}
		rule.descr = txtDescr.getText().trim();
		if (rule.descr.length() > 256) {
			rule.descr = rule.descr.substring(0, 256);
		}
		pj.dbPowerJ.setString(DPowerJ.STM_RUL_UPDATE, 1, rule.name);
		pj.dbPowerJ.setString(DPowerJ.STM_RUL_UPDATE, 2, rule.descr);
		pj.dbPowerJ.setShort(DPowerJ.STM_RUL_UPDATE,  3, rule.id);
		if (pj.dbPowerJ.execute(DPowerJ.STM_RUL_UPDATE) > 0) {
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
		rule = list.get(rowIndex);
		lblID.setText(pj.numbers.formatNumber(rule.id));
		txtName.setText(rule.name);
		txtDescr.setText(rule.descr);
		programmaticChange = false;
	}

	private class ModelRule extends ITableModel {

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