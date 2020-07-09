package ca.powerj;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
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

class NTurnMaster extends NBase {
	private byte newID = 0;
	private int totalTime = 0;
	private int rowIndex = 0;
	private OTurnaround turnaround = new OTurnaround();
	private ArrayList<OTurnaround> list = new ArrayList<OTurnaround>();
	private ModelTurn model;
	private JLabel lblTotal;
	private ITable tbl;
	private ITextString txtName;
	private ITextInteger txtGross, txtEmbed, txtMicro, txtRoute, txtDiagn;

	NTurnMaster(AClient parent) {
		super(parent);
		setName("Turnaround");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_TURNMASTER);
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
		model = new ModelTurn();
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
		txtGross = new ITextInteger(pj.numbers, 1, 200);
		txtGross.setName("Gross");
		txtGross.addFocusListener(this);
		txtGross.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_G, "Gross:");
		label.setLabelFor(txtGross);
		IGUI.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtGross, 1, 1, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtEmbed = new ITextInteger(pj.numbers, 1, 200);
		txtEmbed.setName("Embed");
		txtEmbed.addFocusListener(this);
		txtEmbed.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_E, "Embed:");
		label.setLabelFor(txtEmbed);
		IGUI.addComponent(label, 0, 2, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtEmbed, 1, 2, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtMicro = new ITextInteger(pj.numbers, 1, 200);
		txtMicro.setName("Embed");
		txtMicro.addFocusListener(this);
		txtMicro.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_M, "Microtomy:");
		label.setLabelFor(txtMicro);
		IGUI.addComponent(label, 0, 3, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtMicro, 1, 3, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtRoute = new ITextInteger(pj.numbers, 1, 200);
		txtRoute.setName("Route");
		txtRoute.addFocusListener(this);
		txtRoute.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_R, "Route:");
		label.setLabelFor(txtRoute);
		IGUI.addComponent(label, 0, 4, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtRoute, 1, 4, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		txtDiagn = new ITextInteger(pj.numbers, 1, 200);
		txtDiagn.setName("Diagnosis");
		txtDiagn.addFocusListener(this);
		txtDiagn.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Diagnosis:");
		label.setLabelFor(txtDiagn);
		IGUI.addComponent(label, 0, 5, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtDiagn, 1, 5, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Total:");
		lblTotal = IGUI.createJLabel(SwingConstants.RIGHT, 0, "");
		IGUI.addComponent(label, 0, 6, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblTotal, 1, 6, 2, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_TUR_SELECT));
		try {
			while (rst.next()) {
				turnaround = new OTurnaround();
				turnaround.turID = rst.getByte("taid");
				turnaround.gross = rst.getShort("grss");
				turnaround.embed = rst.getShort("embd");
				turnaround.micro = rst.getShort("micr");
				turnaround.route = rst.getShort("rout");
				turnaround.diagn = rst.getShort("finl");
				turnaround.name = rst.getString("tanm").trim();
				list.add(turnaround);
				if (newID < turnaround.turID) {
					newID = turnaround.turID;
				}
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
			newID++;
			// Add a blank row
			turnaround = new OTurnaround();
			turnaround.newRow = true;
			list.add(turnaround);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	void save() {
		byte index = DPowerJ.STM_TUR_UPDATE;
		if (turnaround.newRow) {
			index = DPowerJ.STM_TUR_INSERT;
			turnaround.turID = newID;
		}
		turnaround.gross = txtGross.getShort();
		turnaround.embed = txtGross.getShort();
		turnaround.micro = txtGross.getShort();
		turnaround.route = txtGross.getShort();
		turnaround.diagn = txtGross.getShort();
		turnaround.name = txtName.getText().trim();
		if (turnaround.name.length() > 16) {
			turnaround.name = turnaround.name.substring(0, 16);
		}
		pj.dbPowerJ.setShort(pjStms.get(index), 1, turnaround.gross);
		pj.dbPowerJ.setShort(pjStms.get(index), 2, turnaround.embed);
		pj.dbPowerJ.setShort(pjStms.get(index), 3, turnaround.micro);
		pj.dbPowerJ.setShort(pjStms.get(index), 4, turnaround.route);
		pj.dbPowerJ.setShort(pjStms.get(index), 5, turnaround.diagn);
		pj.dbPowerJ.setString(pjStms.get(index), 6, turnaround.name);
		pj.dbPowerJ.setByte(pjStms.get(index), 7, turnaround.turID);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (turnaround.newRow) {
				turnaround.newRow = false;
				newID++;
				// Add a blank row
				OTurnaround newTurn = new OTurnaround();
				newTurn.newRow = true;
				list.add(newTurn);
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
		txtName.setText(turnaround.name);
		txtGross.setInt(turnaround.gross);
		txtEmbed.setInt(turnaround.embed);
		txtMicro.setInt(turnaround.micro);
		txtRoute.setInt(turnaround.route);
		txtDiagn.setInt(turnaround.diagn);
		totalTime = turnaround.gross + turnaround.embed + turnaround.micro + turnaround.route + turnaround.diagn;
		lblTotal.setText(pj.numbers.formatNumber(totalTime));
		programmaticChange = false;
	}

	private class ModelTurn extends ITableModel {

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