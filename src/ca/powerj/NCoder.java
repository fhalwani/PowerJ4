package ca.powerj;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

class NCoder extends NBase {
	static final byte CODER1 = 1;
	static final byte CODER2 = 2;
	static final byte CODER3 = 3;
	static final byte CODER4 = 4;
	private short newID = 0;
	private int rowIndex = 0;
	private OWorkcode coder = new OWorkcode();
	private ArrayList<OWorkcode> list = new ArrayList<OWorkcode>();
	private HashMap<Short, OItem> mapRules = new HashMap<Short, OItem>();
	private ITable tbl;
	private ITextString txtName;
	private ITextInteger txtCount;
	private ITextDouble txtValueA, txtValueB, txtValueC;
	private IComboBox cboRules;
	private JTextArea txtDescr, txtRule;
	private ModelCoder model;

	NCoder(AClient parent, byte coderID) {
		super(parent);
		parent.dbPowerJ.prepareStpCoder(coderID);
		byte index = 0;
		switch (coderID) {
		case CODER1:
			index = LSetup.VAR_CODER1_NAME;
			break;
		case CODER2:
			index = LSetup.VAR_CODER2_NAME;
			break;
		case CODER3:
			index = LSetup.VAR_CODER3_NAME;
			break;
		default:
			index = LSetup.VAR_CODER4_NAME;
		}
		setName(pj.setup.getString(index));
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
		model = new ModelCoder();
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
		scrollTable.setMinimumSize(new Dimension(200, 500));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 500));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtName = new ITextString(2, 16);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtName, 1, 0, 1, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
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
		IGUI.addComponent(scrollText, 0, 1, 2, 2, 0.9, 0.3,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
		cboRules = new IComboBox();
		cboRules.setName("Rules");
		cboRules.setModel(getRules());
		cboRules.addFocusListener(this);
		cboRules.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox)e.getSource();
						coder.ruleID = cb.getIndex();
						altered = true;
						showRule();
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_R, "Rule:");
		label.setLabelFor(cboRules);
		IGUI.addComponent(label, 0, 4, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboRules, 1, 4, 1, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		txtCount = new ITextInteger(pj.numbers, 0, Byte.MAX_VALUE);
		txtCount.setName("Gross");
		txtCount.addFocusListener(this);
		txtCount.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_C, "Count:");
		label.setLabelFor(txtCount);
		IGUI.addComponent(label, 0, 5, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtCount, 1, 5, 2, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		txtValueA = new ITextDouble(pj.numbers, 3, 0.0, 99.9);
		txtValueA.setName("ValueA");
		txtValueA.addFocusListener(this);
		txtValueA.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_1, "Value 1:");
		label.setLabelFor(txtValueA);
		IGUI.addComponent(label, 0, 6, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtValueA, 1, 6, 2, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		txtValueB = new ITextDouble(pj.numbers, 3, 0.0, 99.9);
		txtValueB.setName("ValueB");
		txtValueB.addFocusListener(this);
		txtValueB.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_2, "Value 2:");
		label.setLabelFor(txtValueB);
		IGUI.addComponent(label, 0, 7, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtValueB, 1, 7, 2, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		txtValueC = new ITextDouble(pj.numbers, 3, 0.0, 99.9);
		txtValueC.setName("ValueC");
		txtValueC.addFocusListener(this);
		txtValueC.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_3, "Value 3:");
		label.setLabelFor(txtValueC);
		IGUI.addComponent(label, 0, 8, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtValueC, 1, 8, 2, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, pnlData);
		txtRule = new JTextArea();
		txtRule.setName("Rule");
		txtRule.setMargin(new Insets(4, 4, 4, 4));
		txtRule.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtRule.setFont(LConstants.APP_FONT);
		txtRule.setEditable(false);
		txtRule.setFocusable(false);
		txtRule.setLineWrap(true);
		txtRule.setWrapStyleWord(true);
		JScrollPane scrollRule = IGUI.createJScrollPane(txtRule);
		IGUI.addComponent(scrollRule, 0, 9, 3, 3, 0.7, 0.3,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 500));
		add(pnlSplit);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_CD1_SELECT);
		try {
			while (rst.next()) {
				coder = new OWorkcode();
				coder.codeID = rst.getShort("COID");
				coder.ruleID = rst.getShort("RUID");
				coder.count  = rst.getShort("COQY");
				coder.valueA = rst.getDouble("COV1");
				coder.valueB = rst.getDouble("COV2");
				coder.valueC = rst.getDouble("COV3");
				coder.name   = rst.getString("CONM").trim();
				coder.descr  = rst.getString("CODC").trim();
				list.add(coder);
				if (newID < coder.codeID) {
					newID = coder.codeID;
				}
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
			newID++;
			// Add a blank row
			coder = new OWorkcode();
			coder.newRow = true;
			list.add(coder);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	private Object[] getRules() {
		OItem dr = new OItem();
		ArrayList<OItem> list = new ArrayList<OItem>();
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_RUL_SELECT);
		try {
			while (rst.next()) {
				dr = new OItem();
				dr.name = rst.getString("RUNM");
				dr.descr = rst.getString("RUDC");
				mapRules.put(rst.getShort("RUID"), dr);
				list.add(new OItem(rst.getShort("RUID"),
						rst.getString("RUNM")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Rules", e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
		return list.toArray();
	}

	void save() {
		byte index = DPowerJ.STM_CD1_UPDATE;
		if (coder.newRow) {
			index = DPowerJ.STM_CD1_INSERT;
			coder.codeID = newID;
		}
		coder.count = txtCount.getByte();
		coder.valueA = txtValueA.getDouble();
		coder.valueB = txtValueB.getDouble();
		coder.valueC = txtValueC.getDouble();
		coder.name = txtName.getText().trim();
		coder.descr = txtDescr.getText().trim();
		if (coder.name.length() > 16) {
			coder.name = coder.name.substring(0, 16);
		}
		if (coder.descr.length() > 256) {
			coder.descr = coder.descr.substring(0, 256);
		}
		if (coder.ruleID > Byte.MAX_VALUE) {
			coder.ruleID = Byte.MAX_VALUE;
		}
		pj.dbPowerJ.setInt(index,    1, coder.ruleID);
		pj.dbPowerJ.setShort(index,  2, coder.count);
		pj.dbPowerJ.setDouble(index, 3, coder.valueA);
		pj.dbPowerJ.setDouble(index, 4, coder.valueB);
		pj.dbPowerJ.setDouble(index, 5, coder.valueC);
		pj.dbPowerJ.setString(index, 6, coder.name);
		pj.dbPowerJ.setString(index, 7, coder.descr);
		pj.dbPowerJ.setShort(index,  8, coder.codeID);
		if (pj.dbPowerJ.execute(index) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (coder.newRow) {
				coder.newRow = false;
				newID++;
				// Add a blank row
				OWorkcode newCoder = new OWorkcode();
				newCoder.newRow = true;
				list.add(newCoder);
				model.fireTableRowsInserted(list.size() -1, list.size() -1);

			}
		}
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		coder = list.get(rowIndex);
		txtName.setText(coder.name);
		txtDescr.setText(coder.descr);
		cboRules.setIndex(coder.ruleID);
		txtCount.setInt(coder.count);
		txtValueA.setDouble(coder.valueA);
		txtValueB.setDouble(coder.valueB);
		txtValueC.setDouble(coder.valueC);
		showRule();
		programmaticChange = false;
	}

	private void showRule() {
		String text = "";
		OItem rule = mapRules.get(coder.ruleID);
		if (rule != null) {
			text = rule.descr;
		}
		txtRule.setText(text);
	}

	private class ModelCoder extends ITableModel {

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
			return list.get(row).name;
		}
	}
}