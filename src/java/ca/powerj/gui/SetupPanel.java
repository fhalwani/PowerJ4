package ca.powerj.gui;
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
import java.util.ArrayList;
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
import ca.powerj.data.ItemData;
import ca.powerj.data.SetupData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IComboDate;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITimePanel;
import ca.powerj.swing.ITextInteger;
import ca.powerj.swing.ITextString;
import ca.powerj.swing.IUtilities;

class SetupPanel extends BasePanel implements ItemListener {
	private final byte VAR_TYPE_BOOLEAN = 1;
	private final byte VAR_TYPE_BYTE = 2;
	private final byte VAR_TYPE_SHORT = 3;
	private final byte VAR_TYPE_INT = 4;
	private final byte VAR_TYPE_LONG = 5;
	private final byte VAR_TYPE_STRING = 6;
	private SetupData setupData = new SetupData();
	private HashMap<Byte, SetupData> setups = new HashMap<Byte, SetupData>();

	SetupPanel(AppFrame application) {
		super(application);
		setName("Setup");
		application.dbPowerJ.setStatements(LibConstants.ACTION_SETUP);
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			setups.clear();
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
		txtField.getDocument().putProperty("DV", LibSetup.VAR_AP_SERVER);
		txtField.setText(setups.get(LibSetup.VAR_AP_SERVER).getValue());
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_E, "Server Name:");
		label.setLabelFor(txtField);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(txtField, 1, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(3, 50);
		txtField.setName("Database");
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		txtField.getDocument().putProperty("DV", LibSetup.VAR_AP_DATABASE);
		txtField.setText(setups.get(LibSetup.VAR_AP_DATABASE).getValue());
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_A, "Database Name:");
		label.setLabelFor(txtField);
		IUtilities.addComponent(label, 2, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(txtField, 3, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		ITextInteger intField = new ITextInteger(application.numbers, 1000, 9999);
		intField.setName("Port");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LibSetup.VAR_AP_PORT);
		intField.setText(setups.get(LibSetup.VAR_AP_PORT).getValue());
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_O, "Port no:");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 4, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 5, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(3, 50);
		txtField.setName("Login");
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		txtField.getDocument().putProperty("DV", LibSetup.VAR_AP_LOGIN);
		txtField.setText(setups.get(LibSetup.VAR_AP_LOGIN).getValue());
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_L, "Login name:");
		label.setLabelFor(txtField);
		IUtilities.addComponent(label, 0, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(txtField, 1, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JPasswordField textPassword = new JPasswordField(20);
		textPassword.setName("Password");
		textPassword.addFocusListener(this);
		textPassword.setToolTipText("Password at least 8 characters");
		textPassword.getDocument().putProperty("DV", LibSetup.VAR_AP_PASSWORD);
		textPassword.setText(setups.get(LibSetup.VAR_AP_PASSWORD).getValue());
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_P, "Password:");
		label.setLabelFor(textPassword);
		IUtilities.addComponent(label, 2, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(textPassword, 3, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		ITextInteger intField = new ITextInteger(application.numbers, 5, 180);
		intField.setName("Updater");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LibSetup.VAR_UPDATER);
		intField.setText(setups.get(LibSetup.VAR_UPDATER).getValue());
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_U, "Update every (min):");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 1, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		// Range 1-10 minutes (3 columns)
		intField = new ITextInteger(application.numbers, 1, 10);
		intField.setName("Timer");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LibSetup.VAR_TIMER);
		intField.setText(setups.get(LibSetup.VAR_TIMER).getValue());
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_W, "Wakeup every (min):");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 2, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 3, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		// Range 1-12 o'clock (3 columns)
		intField = new ITextInteger(application.numbers, 1, 12);
		intField.setName("Opening");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LibSetup.VAR_OPENING);
		intField.setText(setups.get(LibSetup.VAR_OPENING).getValue());
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_O, "Opening hour:");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 0, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 1, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		// Range 12-24 o'clock (3 columns)
		intField = new ITextInteger(application.numbers, 12, 24);
		intField.setName("Closing");
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("DV", LibSetup.VAR_CLOSING);
		intField.setText(setups.get(LibSetup.VAR_CLOSING).getValue());
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_C, "Closing hour:");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 2, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 3, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JCheckBox checkBox = new JCheckBox();
		checkBox.setName("Saturday");
		checkBox.setFont(LibConstants.APP_FONT);
		checkBox.setText("Saturday Closed: ");
		checkBox.setMnemonic(KeyEvent.VK_T);
		checkBox.putClientProperty("DV", LibSetup.VAR_SAT_OFF);
		checkBox.setSelected(setups.get(LibSetup.VAR_SAT_OFF).getValue().equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IUtilities.addComponent(checkBox, 0, 2, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Sunday");
		checkBox.setFont(LibConstants.APP_FONT);
		checkBox.setText("Sunday Closed: ");
		checkBox.setMnemonic(KeyEvent.VK_U);
		checkBox.putClientProperty("DV", LibSetup.VAR_SUN_OFF);
		checkBox.setSelected(setups.get(LibSetup.VAR_SUN_OFF).getValue().equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IUtilities.addComponent(checkBox, 2, 2, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		ITimePanel timeRouting = new ITimePanel(application.numbers.parseInt(setups.get(LibSetup.VAR_ROUTE_TIME).getValue()));
		timeRouting.setName("Routing");
		timeRouting.putClientProperty("DV", LibSetup.VAR_ROUTE_TIME);
		timeRouting.addFocusListener(this);
		timeRouting.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Value".equals(property)) {
					setupData = setups.get(LibSetup.VAR_ROUTE_TIME);
					setupData.setValue(String.valueOf(e.getNewValue()));
					setupData.setAltered(true);
					altered = true;
					save(LibSetup.VAR_ROUTE_TIME);
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_R, "Routing Cutoff:");
		label.setLabelFor(timeRouting);
		IUtilities.addComponent(label, 0, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(timeRouting, 1, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
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
		txtField.getDocument().putProperty("DV", LibSetup.VAR_CODER1_NAME);
		txtField.setText(setups.get(LibSetup.VAR_CODER1_NAME).getValue());
		txtField.getDocument().addDocumentListener(this);
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_1, "Coder1 Name:");
		label.setLabelFor(txtField);
		IUtilities.addComponent(label, 0, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(txtField, 1, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JCheckBox checkBox = new JCheckBox();
		checkBox.setName("Coder1Active");
		checkBox.setText("Active: ");
		checkBox.putClientProperty("DV", LibSetup.VAR_CODER1_ACTIVE);
		checkBox.setSelected(setups.get(LibSetup.VAR_CODER1_ACTIVE).getValue().equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IUtilities.addComponent(checkBox, 2, 0, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		ITextInteger intField = new ITextInteger(application.numbers, 1000, Short.MAX_VALUE);
		intField.setName("FTE1");
		intField.getDocument().putProperty("DV", LibSetup.VAR_CODER1_FTE);
		intField.setText(setups.get(LibSetup.VAR_CODER1_FTE).getValue());
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Coder1 FTE:");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 4, 0, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 5, 0, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(2, 15);
		txtField.setName("Coder2Name");
		txtField.getDocument().putProperty("DV", LibSetup.VAR_CODER2_NAME);
		txtField.setText(setups.get(LibSetup.VAR_CODER2_NAME).getValue());
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_2, "Coder2 Name:");
		label.setLabelFor(txtField);
		IUtilities.addComponent(label, 0, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(txtField, 1, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Coder2Active");
		checkBox.setText("Active: ");
		checkBox.putClientProperty("DV", LibSetup.VAR_CODER2_ACTIVE);
		checkBox.setSelected(setups.get(LibSetup.VAR_CODER2_ACTIVE).getValue().equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IUtilities.addComponent(checkBox, 2, 1, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		intField = new ITextInteger(application.numbers, 1000, Short.MAX_VALUE);
		intField.setName("FTE2");
		intField.getDocument().putProperty("DV", LibSetup.VAR_CODER2_FTE);
		intField.setText(setups.get(LibSetup.VAR_CODER2_FTE).getValue());
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Coder2 FTE:");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 4, 1, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 5, 1, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(2, 15);
		txtField.setName("Coder3Name");
		txtField.getDocument().putProperty("DV", LibSetup.VAR_CODER3_NAME);
		txtField.setText(setups.get(LibSetup.VAR_CODER3_NAME).getValue());
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_3, "Coder3 Name:");
		label.setLabelFor(txtField);
		IUtilities.addComponent(label, 0, 2, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(txtField, 1, 2, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Coder3Active");
		checkBox.setText("Active: ");
		checkBox.putClientProperty("DV", LibSetup.VAR_CODER3_ACTIVE);
		checkBox.setSelected(setups.get(LibSetup.VAR_CODER3_ACTIVE).getValue().equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IUtilities.addComponent(checkBox, 2, 2, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		intField = new ITextInteger(application.numbers, 1000, Short.MAX_VALUE);
		intField.setName("FTE3");
		intField.getDocument().putProperty("DV", LibSetup.VAR_CODER3_FTE);
		intField.setText(setups.get(LibSetup.VAR_CODER3_FTE).getValue());
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Coder3 FTE:");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 4, 2, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 5, 2, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtField = new ITextString(2, 15);
		txtField.setName("Coder4Name");
		txtField.getDocument().putProperty("DV", LibSetup.VAR_CODER4_NAME);
		txtField.setText(setups.get(LibSetup.VAR_CODER4_NAME).getValue());
		txtField.addFocusListener(this);
		txtField.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_4, "Coder4 Name:");
		label.setLabelFor(txtField);
		IUtilities.addComponent(label, 0, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(txtField, 1, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Coder4Active");
		checkBox.setText("Active: ");
		checkBox.putClientProperty("DV", LibSetup.VAR_CODER4_ACTIVE);
		checkBox.setSelected(setups.get(LibSetup.VAR_CODER4_ACTIVE).getValue().equals("Y"));
		checkBox.addFocusListener(this);
		checkBox.addItemListener(this);
		IUtilities.addComponent(checkBox, 2, 3, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		intField = new ITextInteger(application.numbers, 1000, 9999);
		intField.setName("FTE4");
		intField.getDocument().putProperty("DV", LibSetup.VAR_CODER4_FTE);
		intField.setText(setups.get(LibSetup.VAR_CODER4_FTE).getValue());
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Coder4 FTE:");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 4, 3, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 5, 3, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = Calendar.getInstance();
		Calendar calStart = Calendar.getInstance();
		calMin.setTimeInMillis(0);
		calStart.setTimeInMillis(application.numbers.parseLong(setups.get(LibSetup.VAR_MIN_WL_DATE).getValue()));
		IComboDate cboStart = new IComboDate(calStart, calMin, calMax);
		cboStart.setName("cboStart");
		cboStart.putClientProperty("DV", LibSetup.VAR_MIN_WL_DATE);
		cboStart.addFocusListener(this);
		cboStart.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					IComboDate cbo = (IComboDate) e.getSource();
					Calendar cal = cbo.getValue();
					setupData = setups.get(LibSetup.VAR_MIN_WL_DATE);
					setupData.setValue(String.valueOf(cal.getTimeInMillis()));
					setupData.setAltered(true);
					altered = true;
				}
			}

		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_M, "Minimum Date:");
		label.setLabelFor(cboStart);
		IUtilities.addComponent(label, 0, 4, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(cboStart, 1, 4, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		intField = new ITextInteger(application.numbers, 100, 356);
		intField.setName("Business");
		intField.getDocument().putProperty("DV", LibSetup.VAR_BUSINESS_DAYS);
		intField.setText(setups.get(LibSetup.VAR_BUSINESS_DAYS).getValue());
		intField.addFocusListener(this);
		intField.getDocument().addDocumentListener(this);
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_B, "Business Days:");
		label.setLabelFor(intField);
		IUtilities.addComponent(label, 4, 4, 1, 1, 0.1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(intField, 5, 4, 2, 1, 0.2, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
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
				} else if (c instanceof ITimePanel) {
					save(LibSetup.VAR_ROUTE_TIME);
				} else if (c instanceof JPasswordField) {
					char[] input = ((JPasswordField) c).getPassword();
					setupData = setups.get(((JPasswordField) c).getDocument().getProperty("DV"));
					setupData.setValue(String.copyValueOf(input).trim());
					setupData.setAltered(true);
					save((Byte) ((JPasswordField) c).getDocument().getProperty("DV"));
				} else if (c instanceof JTextComponent) {
					save((Byte) ((JTextComponent) c).getDocument().getProperty("DV"));
				}
			}
		}
	}

	private void getData() {
		byte id = 0;
		ArrayList<ItemData> list = application.dbPowerJ.getSetups();
		for (int i = 0; i < list.size(); i++) {
			id = (byte) list.get(i).getID();
			if (id > LibSetup.VAR_V5_FTE) {
				break; // The rest are read only
			}
			setupData = new SetupData();
			setupData.setValue(list.get(i).getName());
			switch (id) {
			case LibSetup.VAR_CODER1_ACTIVE:
			case LibSetup.VAR_CODER2_ACTIVE:
			case LibSetup.VAR_CODER3_ACTIVE:
			case LibSetup.VAR_CODER4_ACTIVE:
			case LibSetup.VAR_SAT_OFF:
			case LibSetup.VAR_SUN_OFF:
				setupData.setType(VAR_TYPE_BOOLEAN);
				break;
			case LibSetup.VAR_OPENING:
			case LibSetup.VAR_CLOSING:
			case LibSetup.VAR_V5_INTERVAL:
			case LibSetup.VAR_V5_UPDATE:
				setupData.setType(VAR_TYPE_BYTE);
				break;
			case LibSetup.VAR_AP_PORT:
			case LibSetup.VAR_BUSINESS_DAYS:
				setupData.setType(VAR_TYPE_SHORT);
				break;
			case LibSetup.VAR_TIMER:
			case LibSetup.VAR_UPDATER:
			case LibSetup.VAR_V5_FTE:
				setupData.setType(VAR_TYPE_INT);
				break;
			case LibSetup.VAR_MIN_WL_DATE:
				setupData.setType(VAR_TYPE_LONG);
				break;
			default:
				setupData.setType(VAR_TYPE_STRING);
			}
			setups.put(id, setupData);
		}
		application.display("Setup " + setups.size());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		trackCheckbox(e);
	}

	private void save(byte key) {
		setupData = setups.get(key);
		if (setupData.isAltered()) {
			switch (setupData.getType()) {
			case VAR_TYPE_BYTE:
				if (application.setup.setByte(key, application.numbers.parseByte(setupData.getValue()))) {
					setupData.setAltered(false);
					altered = false;
				}
				break;
			case VAR_TYPE_SHORT:
				if (application.setup.setShort(key, application.numbers.parseShort(setupData.getValue()))) {
					setupData.setAltered(false);
					altered = false;
				}
				break;
			case VAR_TYPE_INT:
				if (application.setup.setInt(key, application.numbers.parseInt(setupData.getValue()))) {
					setupData.setAltered(false);
					altered = false;
				}
				break;
			case VAR_TYPE_LONG:
				if (application.setup.setLong(key, application.numbers.parseLong(setupData.getValue()))) {
					setupData.setAltered(false);
					altered = false;
				}
				break;
			default:
				if (application.setup.setString(key, setupData.getValue())) {
					setupData.setAltered(false);
					altered = false;
				}
			}
		}
	}

	private void trackCheckbox(ItemEvent e) {
		if (!programmaticChange) {
			JCheckBox cb = (JCheckBox) e.getSource();
			setupData = setups.get(cb.getClientProperty("DV"));
			setupData.setValue(cb.isSelected() ? "Y" : "N");
			setupData.setAltered(true);
			altered = true;
		}
	}

	@Override
	void trackDocument(DocumentEvent e) {
		if (!programmaticChange) {
			try {
				Document doc = e.getDocument();
				setupData = setups.get(doc.getProperty("DV"));
				setupData.setValue(doc.getText(0, doc.getLength()));
				setupData.setAltered(true);
				altered = true;
			} catch (BadLocationException ignore) {
			}
		}
	}
}