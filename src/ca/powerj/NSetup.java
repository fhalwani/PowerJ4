package ca.powerj;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

class NSetup extends NBase implements ItemListener {
	private final byte VAR_TYPE_BOOLEAN = 1;
	private final byte VAR_TYPE_BYTE = 2;
	private final byte VAR_TYPE_SHORT = 3;
	private final byte VAR_TYPE_INT = 4;
	private final byte VAR_TYPE_LONG = 5;
	private final byte VAR_TYPE_STRING = 6;
	private OSetup setup = new OSetup();
	private HashMap<Byte, OSetup> map = new HashMap<Byte, OSetup>();

	NSetup(AClient parent) {
		super(parent);
		setName("Setup");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_SETUP);
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			map.clear();
		}
		return !altered;
	}

	private void createPanel() {
		// Lay out 3 panels from top to bottom.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(createPanelServer());
		add(createPanelWorkflow());
		add(createPanelWorkload());
	}

	private JPanel createPanelServer() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Server");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Server");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		ITextString txtField = new ITextString(3, 50);
		txtField.addAncestorListener(new IFocusListener());
		txtField.setName("Server");
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		txtField.getDocument().putProperty("DV", LSetup.VAR_AP_SERVER);
		txtField.setText(map.get(LSetup.VAR_AP_SERVER).value);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_E, "Server Name:");
		label.setLabelFor(txtField);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(txtField, 1, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(3, 50);
		txtField.setName("Database");
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		txtField.getDocument().putProperty("DV", LSetup.VAR_AP_DATABASE);
		txtField.setText(map.get(LSetup.VAR_AP_DATABASE).value);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_A, "Database Name:");
		label.setLabelFor(txtField);
		IGUI.addComponent(label, 2, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(txtField, 3, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		ITextInteger intField = new ITextInteger(pj.numbers, 1000, 9999);
		intField.setName("Port");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LSetup.VAR_AP_PORT);
		intField.setText(map.get(LSetup.VAR_AP_PORT).value);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_O, "Port no:");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 4, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 5, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(3, 50);
		txtField.setName("Login");
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		txtField.getDocument().putProperty("DV", LSetup.VAR_AP_LOGIN);
		txtField.setText(map.get(LSetup.VAR_AP_LOGIN).value);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_L, "Login name:");
		label.setLabelFor(txtField);
		IGUI.addComponent(label, 0, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(txtField, 1, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JPasswordField textPassword = new JPasswordField(20);
		textPassword.setName("Password");
		textPassword.addFocusListener(this);
		textPassword.setToolTipText("Password at least 8 characters");
		textPassword.getDocument().putProperty("DV", LSetup.VAR_AP_PASSWORD);
		textPassword.setText(map.get(LSetup.VAR_AP_PASSWORD).value);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_P, "Password:");
		label.setLabelFor(textPassword);
		IGUI.addComponent(label, 2, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(textPassword, 3, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				panel);
		return panel;
	}

	private JPanel createPanelWorkflow() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Workflow");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Workflow");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		// Range 5-180 minutes (4 columns)
		ITextInteger intField = new ITextInteger(pj.numbers, 5, 180);
		intField.setName("Updater");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LSetup.VAR_UPDATER);
		intField.setText(map.get(LSetup.VAR_UPDATER).value);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_U, "Update every (min):");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 1, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		// Range 1-10 minutes (3 columns)
		intField = new ITextInteger(pj.numbers, 1, 10);
		intField.setName("Timer");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LSetup.VAR_TIMER);
		intField.setText(map.get(LSetup.VAR_TIMER).value);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_W, "Wakeup every (min):");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 2, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 3, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		// Range 1-12 o'clock (3 columns)
		intField = new ITextInteger(pj.numbers, 1, 12);
		intField.setName("Opening");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LSetup.VAR_OPENING);
		intField.setText(map.get(LSetup.VAR_OPENING).value);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_O, "Opening hour:");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 0, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 1, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		// Range 12-24 o'clock (3 columns)
		intField = new ITextInteger(pj.numbers, 12, 24);
		intField.setName("Closing");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LSetup.VAR_CLOSING);
		intField.setText(map.get(LSetup.VAR_CLOSING).value);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_C, "Closing hour:");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 2, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 3, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JCheckBox checkBox = new JCheckBox();
		checkBox.setName("Saturday");
		checkBox.setFont(LConstants.APP_FONT);
		checkBox.setText("Saturday Closed: ");
		checkBox.setMnemonic(KeyEvent.VK_T);
		checkBox.putClientProperty("DV", LSetup.VAR_SAT_OFF);
		checkBox.setSelected(map.get(LSetup.VAR_SAT_OFF).value.equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IGUI.addComponent(checkBox, 0, 2, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Sunday");
		checkBox.setFont(LConstants.APP_FONT);
		checkBox.setText("Sunday Closed: ");
		checkBox.setMnemonic(KeyEvent.VK_U);
		checkBox.putClientProperty("DV", LSetup.VAR_SUN_OFF);
		checkBox.setSelected(map.get(LSetup.VAR_SUN_OFF).value.equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IGUI.addComponent(checkBox, 2, 2, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IPanelTime timeRouting = new IPanelTime(pj.numbers.parseInt(map.get(LSetup.VAR_ROUTE_TIME).value));
		timeRouting.setName("Routing");
		timeRouting.putClientProperty("DV", LSetup.VAR_ROUTE_TIME);
		timeRouting.addFocusListener(this);
		timeRouting.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Value".equals(property)) {
					setup = map.get(LSetup.VAR_ROUTE_TIME);
					setup.value = String.valueOf(e.getNewValue());
					setup.altered = true;
					altered = true;
					save(LSetup.VAR_ROUTE_TIME);
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_R, "Routing Cutoff:");
		label.setLabelFor(timeRouting);
		IGUI.addComponent(label, 0, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(timeRouting, 1, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				panel);
		return panel;
	}

	private JPanel createPanelWorkload() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Workload");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Workload");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		ITextString txtField = new ITextString(2, 15);
		txtField.setName("Coder1Name");
		txtField.addFocusListener(this);
		txtField.getDocument().putProperty("DV", LSetup.VAR_CODER1_NAME);
		txtField.setText(map.get(LSetup.VAR_CODER1_NAME).value);
		txtField.getDocument().addDocumentListener(this);
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_1, "Coder1 Name:");
		label.setLabelFor(txtField);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(txtField, 1, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JCheckBox checkBox = new JCheckBox();
		checkBox.setName("Coder1Active");
		checkBox.setText("Active: ");
		checkBox.putClientProperty("DV", LSetup.VAR_CODER1_ACTIVE);
		checkBox.setSelected(map.get(LSetup.VAR_CODER1_ACTIVE).value.equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IGUI.addComponent(checkBox, 2, 0, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		ITextInteger intField = new ITextInteger(pj.numbers, 1000, Short.MAX_VALUE);
		intField.setName("FTE1");
		intField.getDocument().putProperty("DV", LSetup.VAR_CODER1_FTE);
		intField.setText(map.get(LSetup.VAR_CODER1_FTE).value);
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Coder1 FTE:");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 4, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 5, 0, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(2, 15);
		txtField.setName("Coder2Name");
		txtField.getDocument().putProperty("DV", LSetup.VAR_CODER2_NAME);
		txtField.setText(map.get(LSetup.VAR_CODER2_NAME).value);
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_2, "Coder2 Name:");
		label.setLabelFor(txtField);
		IGUI.addComponent(label, 0, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(txtField, 1, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Coder2Active");
		checkBox.setText("Active: ");
		checkBox.putClientProperty("DV", LSetup.VAR_CODER2_ACTIVE);
		checkBox.setSelected(map.get(LSetup.VAR_CODER2_ACTIVE).value.equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IGUI.addComponent(checkBox, 2, 1, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		intField = new ITextInteger(pj.numbers, 1000, Short.MAX_VALUE);
		intField.setName("FTE2");
		intField.getDocument().putProperty("DV", LSetup.VAR_CODER2_FTE);
		intField.setText(map.get(LSetup.VAR_CODER2_FTE).value);
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Coder2 FTE:");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 4, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 5, 1, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(2, 15);
		txtField.setName("Coder3Name");
		txtField.getDocument().putProperty("DV", LSetup.VAR_CODER3_NAME);
		txtField.setText(map.get(LSetup.VAR_CODER3_NAME).value);
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_3, "Coder3 Name:");
		label.setLabelFor(txtField);
		IGUI.addComponent(label, 0, 2, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(txtField, 1, 2, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Coder3Active");
		checkBox.setText("Active: ");
		checkBox.putClientProperty("DV", LSetup.VAR_CODER3_ACTIVE);
		checkBox.setSelected(map.get(LSetup.VAR_CODER3_ACTIVE).value.equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IGUI.addComponent(checkBox, 2, 2, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		intField = new ITextInteger(pj.numbers, 1000, Short.MAX_VALUE);
		intField.setName("FTE3");
		intField.getDocument().putProperty("DV", LSetup.VAR_CODER3_FTE);
		intField.setText(map.get(LSetup.VAR_CODER3_FTE).value);
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Coder3 FTE:");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 4, 2, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 5, 2, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(2, 15);
		txtField.setName("Coder4Name");
		txtField.getDocument().putProperty("DV", LSetup.VAR_CODER4_NAME);
		txtField.setText(map.get(LSetup.VAR_CODER4_NAME).value);
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_4, "Coder4 Name:");
		label.setLabelFor(txtField);
		IGUI.addComponent(label, 0, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(txtField, 1, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Coder4Active");
		checkBox.setText("Active: ");
		checkBox.putClientProperty("DV", LSetup.VAR_CODER4_ACTIVE);
		checkBox.setSelected(map.get(LSetup.VAR_CODER4_ACTIVE).value.equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IGUI.addComponent(checkBox, 2, 3, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		intField = new ITextInteger(pj.numbers, 1000, 9999);
		intField.setName("FTE4");
		intField.getDocument().putProperty("DV", LSetup.VAR_CODER4_FTE);
		intField.setText(map.get(LSetup.VAR_CODER4_FTE).value);
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Coder4 FTE:");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 4, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 5, 3, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = Calendar.getInstance();
		Calendar calStart = Calendar.getInstance();
		calMin.setTimeInMillis(0);
		calStart.setTimeInMillis(pj.numbers.parseLong(map.get(LSetup.VAR_MIN_WL_DATE).value));
		IComboDate cboStart = new IComboDate(calStart, calMin, calMax);
		cboStart.setName("cboStart");
		cboStart.putClientProperty("DV", LSetup.VAR_MIN_WL_DATE);
		cboStart.addFocusListener(this);
		cboStart.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboDate cbo = (IComboDate) e.getSource();
					Calendar cal = cbo.getValue();
					setup = map.get(LSetup.VAR_MIN_WL_DATE);
					setup.value = String.valueOf(cal.getTimeInMillis());
					setup.altered = true;
					altered = true;
				}
			}

		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_M, "Minimum Date:");
		label.setLabelFor(cboStart);
		IGUI.addComponent(label, 0, 4, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(cboStart, 1, 4, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		intField = new ITextInteger(pj.numbers, 100, 356);
		intField.setName("Business");
		intField.getDocument().putProperty("DV", LSetup.VAR_BUSINESS_DAYS);
		intField.setText(map.get(LSetup.VAR_BUSINESS_DAYS).value);
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_B, "Business Days:");
		label.setLabelFor(intField);
		IGUI.addComponent(label, 4, 4, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(intField, 5, 4, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		return panel;
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (altered) {
			Component c = e.getComponent();
			if (c != null) {
				if (c instanceof JCheckBox) {
					save((Byte) ((JCheckBox) c).getClientProperty("DV"));
				} else if (c instanceof IComboDate) {
					save((Byte) ((IComboDate) c).getClientProperty("DV"));
				} else if (c instanceof IPanelTime) {
					save(LSetup.VAR_ROUTE_TIME);
				} else if (c instanceof JPasswordField) {
					char[] input = ((JPasswordField) c).getPassword();
					setup = map.get(((JPasswordField) c).getDocument().getProperty("DV"));
					setup.value = String.copyValueOf(input).trim();
					setup.altered = true;
					save((Byte) ((JPasswordField) c).getDocument().getProperty("DV"));
				} else if (c instanceof JTextComponent) {
					save((Byte) ((JTextComponent) c).getDocument().getProperty("DV"));
				}
			}
		}
	}

	private void getData() {
		byte id = 0;
		ResultSet rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_STP_SELECT));
		try {
			while (rst.next()) {
				id = rst.getByte("STID");
				if (id > LSetup.VAR_V5_FTE)
					break; // The rest are read only
				setup = new OSetup();
				setup.value = rst.getString("STVA").trim();
				switch (id) {
				case LSetup.VAR_CODER1_ACTIVE:
				case LSetup.VAR_CODER2_ACTIVE:
				case LSetup.VAR_CODER3_ACTIVE:
				case LSetup.VAR_CODER4_ACTIVE:
				case LSetup.VAR_SAT_OFF:
				case LSetup.VAR_SUN_OFF:
					setup.type = VAR_TYPE_BOOLEAN;
					break;
				case LSetup.VAR_OPENING:
				case LSetup.VAR_CLOSING:
				case LSetup.VAR_V5_INTERVAL:
				case LSetup.VAR_V5_UPDATE:
					setup.type = VAR_TYPE_BYTE;
					break;
				case LSetup.VAR_AP_PORT:
				case LSetup.VAR_BUSINESS_DAYS:
					setup.type = VAR_TYPE_SHORT;
					break;
				case LSetup.VAR_TIMER:
				case LSetup.VAR_UPDATER:
				case LSetup.VAR_V5_FTE:
					setup.type = VAR_TYPE_INT;
					break;
				case LSetup.VAR_MIN_WL_DATE:
					setup.type = VAR_TYPE_LONG;
					break;
				default:
					setup.type = VAR_TYPE_STRING;
				}
				map.put(id, setup);
			}
			pj.statusBar.setMessage("Setup " + map.size());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, "Variables", e);
		} finally {
			pj.dbPowerJ.close(rst);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		trackCheckbox(e);
	}

	private void save(byte key) {
		setup = map.get(key);
		if (setup.altered) {
			switch (setup.type) {
			case VAR_TYPE_BYTE:
				if (pj.setup.setByte(key, pj.numbers.parseByte(setup.value))) {
					setup.altered = false;
					altered = false;
				}
				break;
			case VAR_TYPE_SHORT:
				if (pj.setup.setShort(key, pj.numbers.parseShort(setup.value))) {
					setup.altered = false;
					altered = false;
				}
				break;
			case VAR_TYPE_INT:
				if (pj.setup.setInt(key, pj.numbers.parseInt(setup.value))) {
					setup.altered = false;
					altered = false;
				}
				break;
			case VAR_TYPE_LONG:
				if (pj.setup.setLong(key, pj.numbers.parseLong(setup.value))) {
					setup.altered = false;
					altered = false;
				}
				break;
			default:
				if (pj.setup.setString(key, setup.value)) {
					setup.altered = false;
					altered = false;
				}
			}
		}
	}

	private void trackCheckbox(ItemEvent e) {
		if (!programmaticChange) {
			JCheckBox cb = (JCheckBox) e.getSource();
			setup = map.get(cb.getClientProperty("DV"));
			setup.value = cb.isSelected() ? "Y" : "N";
			setup.altered = true;
			altered = true;
		}
	}

	@Override
	void trackDocument(DocumentEvent e) {
		if (!programmaticChange) {
			try {
				Document doc = e.getDocument();
				setup = map.get(doc.getProperty("DV"));
				setup.value = doc.getText(0, doc.getLength());
				setup.altered = true;
				altered = true;
			} catch (BadLocationException ignore) {
			}
		}
	}
}