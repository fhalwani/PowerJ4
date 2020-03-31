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

class NProcedure extends NBase {
	private short newID = 0;
	private int rowIndex = 0;
	private OItem procedure = new OItem();
	private ArrayList<OItem> list = new ArrayList<OItem>();
	private ModelProcedure model;
	private ITable tbl;
	private ITextString txtName;
	private JTextArea txtDescr;

	NProcedure(AClient parent) {
		super(parent);
		setName("Procedures");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_PROCEDURES);
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
		model = new ModelProcedure();
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
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtName, 1, 0, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
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
		IGUI.addComponent(scrollText, 0, 1, 3, 3, 0.7, 1.0, GridBagConstraints.BOTH, GridBagConstraints.EAST, pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		add(pnlSplit);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_PRO_SELECT));
		try {
			while (rst.next()) {
				procedure = new OItem();
				procedure.id = rst.getShort("POID");
				procedure.name = rst.getString("PONM").trim();
				procedure.descr = rst.getString("PODC").trim();
				list.add(procedure);
				if (newID < procedure.id) {
					newID = procedure.id;
				}
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
			newID++;
			// Add a blank row
			procedure = new OItem();
			procedure.newRow = true;
			list.add(procedure);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	void save() {
		byte index = DPowerJ.STM_PRO_UPDATE;
		if (procedure.newRow) {
			index = DPowerJ.STM_PRO_INSERT;
			procedure.id = newID;
		}
		procedure.name = txtName.getText().trim();
		if (procedure.name.length() > 16) {
			procedure.name = procedure.name.substring(0, 16);
		}
		procedure.descr = txtDescr.getText().trim();
		if (procedure.descr.length() > 256) {
			procedure.descr = procedure.descr.substring(0, 256);
		}
		pj.dbPowerJ.setString(pjStms.get(index), 1, procedure.name);
		pj.dbPowerJ.setString(pjStms.get(index), 2, procedure.descr);
		pj.dbPowerJ.setShort(pjStms.get(index), 3, procedure.id);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (procedure.newRow) {
				procedure.newRow = false;
				newID++;
				// Add a blank row
				OItem row = new OItem();
				row.newRow = true;
				list.add(row);
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
		procedure = list.get(rowIndex);
		txtName.setText(procedure.name);
		txtDescr.setText(procedure.descr);
		programmaticChange = false;
	}

	private class ModelProcedure extends ITableModel {

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