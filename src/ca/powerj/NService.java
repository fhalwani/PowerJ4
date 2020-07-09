package ca.powerj;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class NService extends NBase implements ItemListener {
	private final String[] strCheck = { "Call", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Active", "Sub", "",
			"", "", "", "" };
	private byte newID = 0;
	private int rowIndex = 0;
	private OService service = new OService();
	private ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
	private ArrayList<OService> list = new ArrayList<OService>();
	private HashMap<Byte, String> mapSpecialties = new HashMap<Byte, String>();
	private ITextString txtName, txtDescr;
	private ModelService model;
	private ITable tblList;
	private IComboBox cboSubspecial, cboFacilities;
	private JLabel lblSpecialties;

	NService(AClient parent) {
		super(parent);
		setName("Services");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_SERVICES);
		getData();
		getSpecialties();
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
		for (int i = 0; i < strCheck.length; i++) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setName(strCheck[i]);
			checkBox.setText(strCheck[i]);
			checkBox.setFont(LConstants.APP_FONT);
			switch (i) {
			case 0:
				checkBox.setMnemonic(KeyEvent.VK_C);
				break;
			case 1:
				checkBox.setMnemonic(KeyEvent.VK_S);
				break;
			case 2:
				checkBox.setMnemonic(KeyEvent.VK_M);
				break;
			case 3:
				checkBox.setMnemonic(KeyEvent.VK_T);
				break;
			case 4:
				checkBox.setMnemonic(KeyEvent.VK_W);
				break;
			case 5:
				checkBox.setMnemonic(KeyEvent.VK_H);
				break;
			case 6:
				checkBox.setMnemonic(KeyEvent.VK_R);
				break;
			case 7:
				checkBox.setMnemonic(KeyEvent.VK_A);
				break;
			case 8:
				checkBox.setMnemonic(KeyEvent.VK_I);
				break;
			case 9:
				checkBox.setMnemonic(KeyEvent.VK_U);
				break;
			default:
			}
			checkBox.addFocusListener(this);
			checkBox.addItemListener(this);
			checkboxes.add(checkBox);
		}
		model = new ModelService();
		tblList = new ITable(pj, model);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tblList.addAncestorListener(new IFocusListener());
		tblList.addFocusListener(this);
		tblList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
					setRow(tblList.convertRowIndexToModel(index));
				}
			}
		});
		tblList.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IGUI.createJScrollPane(tblList);
		scrollTable.setMinimumSize(new Dimension(200, 400));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 400));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		txtName = new ITextString(2, 8);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtName, 1, 0, 1, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		txtDescr = new ITextString(2, 64);
		txtDescr.setName("Description");
		txtDescr.addFocusListener(this);
		txtDescr.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Descr:");
		label.setLabelFor(txtDescr);
		IGUI.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(txtDescr, 1, 1, 1, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IGUI.addComponent(createPanelDays(), 0, 2, 2, 6, 1.0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IGUI.addComponent(checkboxes.get(8), 0, 8, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IGUI.addComponent(checkboxes.get(9), 1, 8, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboFacilities = new IComboBox();
		cboFacilities.setName("Facilities");
		cboFacilities.setModel(pj.dbPowerJ.getFacilities(false));
		cboFacilities.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						service.facID = cb.getIndex();
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_F, "Facility:");
		label.setLabelFor(cboFacilities);
		IGUI.addComponent(label, 0, 9, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboFacilities, 1, 9, 2, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboSubspecial = new IComboBox();
		cboSubspecial.setName("Subspecial");
		cboSubspecial.setModel(pj.dbPowerJ.getSubspecialties(false));
		cboSubspecial.addFocusListener(this);
		cboSubspecial.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						service.subID = (byte) cb.getIndex();
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_B, "Subspecial:");
		label.setLabelFor(cboSubspecial);
		IGUI.addComponent(label, 0, 10, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboSubspecial, 1, 10, 2, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Specialty:");
		IGUI.addComponent(label, 0, 11, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblSpecialties = IGUI.createJLabel(SwingConstants.LEFT, 0, "            ");
		IGUI.addComponent(lblSpecialties, 1, 11, 2, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 400));
		add(pnlSplit);
	}

	private JPanel createPanelDays() {
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Days");
		title.setTitleJustification(TitledBorder.CENTER);
		JPanel panel = new JPanel();
		panel.setBorder(title);
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		for (int i = 0; i < 8; i++) {
			IGUI.addComponent(checkboxes.get(i), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
					GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		}
		return panel;
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SRV_SELECT));
		try {
			while (rst.next()) {
				service = new OService();
				service.srvID = rst.getByte("srid");
				service.subID = rst.getByte("sbid");
				service.facID = rst.getShort("faid");
				service.name = rst.getString("srnm");
				service.descr = rst.getString("srdc");
				service.codes = pj.numbers.shortToBoolean(rst.getShort("srcd"));
				list.add(service);
				if (newID < service.srvID) {
					newID = service.srvID;
				}
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
			newID++;
			// Add a blank row
			service = new OService();
			service.newRow = true;
			list.add(service);
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Variables", e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	private void getSpecialties() {
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SUB_SELECT));
		try {
			while (rst.next()) {
				mapSpecialties.put(rst.getByte("sbid"), rst.getString("synm"));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!programmaticChange) {
			altered = true;
		}
	}

	@Override
	void save() {
		byte index = DPowerJ.STM_SRV_UPDATE;
		if (service.newRow) {
			index = DPowerJ.STM_SRV_INSERT;
			service.srvID = newID;
		}
		service.name = txtName.getText().trim().toUpperCase();
		if (service.name.length() > 8) {
			service.name = service.name.substring(0, 8);
		}
		service.descr = txtDescr.getText().trim();
		if (service.name.length() > 64) {
			service.name = service.name.substring(0, 64);
		}
		if (service.facID == 0 || service.subID == 0 || service.name.length() == 0) {
			return;
		}
		for (int i = 0; i < strCheck.length; i++) {
			service.codes[i] = checkboxes.get(i).isSelected();
		}
		pj.dbPowerJ.setShort(pjStms.get(index), 1, service.facID);
		pj.dbPowerJ.setShort(pjStms.get(index), 2, service.subID);
		pj.dbPowerJ.setShort(pjStms.get(index), 3, pj.numbers.booleanToShort(service.codes));
		pj.dbPowerJ.setString(pjStms.get(index), 4, service.name);
		pj.dbPowerJ.setString(pjStms.get(index), 5, service.descr);
		pj.dbPowerJ.setShort(pjStms.get(index), 6, service.srvID);
		if (pj.dbPowerJ.execute(pjStms.get(index)) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (service.newRow) {
				service.newRow = false;
				newID++;
				// Add a blank row
				OService blank = new OService();
				blank.newRow = true;
				list.add(blank);
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
		service = list.get(rowIndex);
		txtName.setText(service.name);
		txtDescr.setText(service.descr);
		for (int i = 0; i < 10; i++) {
			checkboxes.get(i).setSelected(service.codes[i]);
		}
		cboFacilities.setIndex(service.facID);
		cboSubspecial.setIndex(service.subID);
		lblSpecialties.setText(mapSpecialties.get(service.subID));
		programmaticChange = false;
	}

	private class ModelService extends ITableModel {

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