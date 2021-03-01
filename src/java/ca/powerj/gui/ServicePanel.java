package ca.powerj.gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
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
import ca.powerj.data.FacilityData;
import ca.powerj.data.ItemData;
import ca.powerj.data.ServiceData;
import ca.powerj.data.SpecialtyList;
import ca.powerj.data.SubspecialtyList;
import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class ServicePanel extends BasePanel {
	private final String[] strCheck = { "Call", "Sun", "Mon", "Tue", "Wed", "Thu",
			"Fri", "Sat", "Active", "Sub", "", "", "", "", "" };
	private byte newID = 0;
	private int rowIndex = 0;
	private ServiceData service = new ServiceData();
	private ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
	private ArrayList<ServiceData> list = new ArrayList<ServiceData>();
	private HashMap<Byte, String> mapSpecialties = new HashMap<Byte, String>();
	private ITextString txtName, txtDescr;
	private ModelService model;
	private ITable table;
	private IComboBox cboSubspecial, cboFacilities;
	private JLabel lblSpecialties;

	ServicePanel(AppFrame application) {
		super(application);
		setName("Services");
		application.dbPowerJ.setStatements(LibConstants.ACTION_SERVICES);
		getData();
		getSpecialties();
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
		for (int i = 0; i < strCheck.length; i++) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setName(strCheck[i]);
			checkBox.setText(strCheck[i]);
			checkBox.setFont(LibConstants.APP_FONT);
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
			checkBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!programmaticChange) {
						altered = true;
					}
				}
				
			});
			checkboxes.add(checkBox);
		}
		model = new ModelService();
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
		txtName = new ITextString(2, 8);
		txtName.setName("Name");
		txtName.addFocusListener(this);
		txtName.getDocument().addDocumentListener(this);
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		label.setLabelFor(txtName);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtName, 1, 0, 1, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		txtDescr = new ITextString(2, 64);
		txtDescr.setName("Description");
		txtDescr.addFocusListener(this);
		txtDescr.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_D, "Descr:");
		label.setLabelFor(txtDescr);
		IUtilities.addComponent(label, 0, 1, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(txtDescr, 1, 1, 1, 1, 0.7, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(createPanelDays(), 0, 2, 2, 6, 1.0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(checkboxes.get(8), 0, 8, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		IUtilities.addComponent(checkboxes.get(9), 1, 8, 1, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		cboFacilities = new IComboBox();
		cboFacilities.setName("Facilities");
		cboFacilities.setItems(getFacilities());
		cboFacilities.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						service.setFacID((short) cb.getIndex());
						altered = true;
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_F, "Facility:");
		label.setLabelFor(cboFacilities);
		IUtilities.addComponent(label, 0, 9, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboFacilities, 1, 9, 2, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		SubspecialtyList lstSubspecialty = application.dbPowerJ.getSubspecialties(false);
		cboSubspecial = new IComboBox();
		cboSubspecial.setName("Subspecial");
		cboSubspecial.setItems(lstSubspecialty.getAll());
		cboSubspecial.addFocusListener(this);
		cboSubspecial.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						service.setSubID((byte) cb.getIndex());
						altered = true;
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_B, "Subspecial:");
		label.setLabelFor(cboSubspecial);
		IUtilities.addComponent(label, 0, 10, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboSubspecial, 1, 10, 2, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Specialty:");
		IUtilities.addComponent(label, 0, 11, 1, 1, 0.3, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblSpecialties = IUtilities.createJLabel(SwingConstants.LEFT, 0, "            ");
		IUtilities.addComponent(lblSpecialties, 1, 11, 2, 1, 0.5, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
			IUtilities.addComponent(checkboxes.get(i), (i % 2 == 0 ? 0 : 1), (i / 2), 1, 1, 0.5, 0,
					GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		}
		return panel;
	}

	private void getData() {
		list = application.dbPowerJ.getServices();
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setCodes(application.numbers.shortToBoolean(list.get(i).getCodeID()));
			if (newID < list.get(i).getSrvID()) {
				newID = list.get(i).getSrvID();
			}
		}
		application.display("No Rows: " + list.size());
		newID++;
		// Add a blank row
		service = new ServiceData();
		service.setNewRow(true);
		list.add(service);
	}

	private ItemData[] getFacilities() {
		ArrayList<FacilityData> temp = application.dbPowerJ.getFacilities(false);
		ItemData[] items = new ItemData[temp.size()];
		for (int i = 0; i < temp.size(); i++) {
			items[i] = new ItemData(temp.get(i).getFacID(), temp.get(i).getName());
		}
		return items;
	}

	private void getSpecialties() {
		SpecialtyList temp = application.dbPowerJ.getSpecialties(false);
		for (int i = 0; i < temp.getSize(); i++) {
			mapSpecialties.put(temp.get(i).getID(), temp.get(i).getName());
		}
	}

	@Override
	void save() {
		if (service.isNewRow()) {
			service.setSrvID(newID);
		}
		service.setName(txtName.getText());
		service.setDescr(txtDescr.getText());
		if (service.getFacID() == 0 || service.getSubID() == 0 || service.getName().length() == 0) {
			return;
		}
		for (int i = 0; i < strCheck.length; i++) {
			service.setCode(i, checkboxes.get(i).isSelected());
		}
		service.setCodeID(application.numbers.booleanToShort(service.getCodes()));
		if (application.dbPowerJ.setService(service) > 0) {
			altered = false;
			model.fireTableRowsUpdated(rowIndex, rowIndex);
			if (service.isNewRow()) {
				service.setNewRow(false);
				newID++;
				// Add a blank row
				ServiceData blank = new ServiceData();
				blank.setNewRow(true);
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
		txtName.setText(service.getName());
		txtDescr.setText(service.getDescr());
		for (int i = 0; i < 10; i++) {
			checkboxes.get(i).setSelected(service.getCode(i));
		}
		cboFacilities.setIndex(service.getFacID());
		cboSubspecial.setIndex(service.getSubID());
		lblSpecialties.setText(mapSpecialties.get(service.getSubID()));
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
				value = list.get(row).getName();
			}
			return value;
		}
	}
}