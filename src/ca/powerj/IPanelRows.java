package ca.powerj;
import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class IPanelRows extends JPanel {
	static final byte ROW_FACILITY   = 0;
	static final byte ROW_SPECIALTY  = 1;
	static final byte ROW_SUBSPECIAL = 2;
	static final byte ROW_PROCEDURE  = 3;
	static final byte ROW_STAFF      = 4;
	private volatile boolean programmaticChange;
	private byte[] selected, original;
	private ArrayList<JSpinner> spinners = new ArrayList<JSpinner>();
	private Timer selectionTimer;

	IPanelRows(byte[] value) {
		super(new GridBagLayout());
		original = value;
		selected = new byte[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
			original[i] = value[i];
		}
		selectionTimer = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!selected.equals(original)) {
					firePropertyChange("Value", original, selected);
				}
			}
		});
		selectionTimer.setRepeats(false);
		int[] rank = new int[selected.length];
		for (byte i = 0; i < selected.length; i++) {
			switch (selected[i]) {
			case ROW_FACILITY:
				rank[0] = i+1;
				break;
			case ROW_SPECIALTY:
				rank[1] = i+1;
				break;
			case ROW_SUBSPECIAL:
				rank[2] = i+1;
				break;
			case ROW_PROCEDURE:
				rank[3] = i+1;
				break;
			case ROW_STAFF:
				rank[4] = i+1;
				break;
			default:
				// 0 = ignore
			}
		}
		SpinnerNumberModel mdlFacility = new SpinnerNumberModel(rank[0], 0, 5, 1);
		JSpinner spnFacility = new JSpinner(mdlFacility) {
			private static final long serialVersionUID = 2450610846855975702L;
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnFacility.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = ((Integer) spinners.get(0).getValue()).intValue();
					resetValues(ROW_FACILITY, newValue);
				}
			}
		});
		JFormattedTextField ftfFacility = getTextField(spnFacility);
		if (ftfFacility != null ) {
			ftfFacility.setColumns(5);
			ftfFacility.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnFacility.setEditor(new JSpinner.NumberEditor(spnFacility, "#"));
		spnFacility.addAncestorListener(new IFocusListener());
		JLabel lblFacility = new JLabel("Facility");
		lblFacility.setDisplayedMnemonic(KeyEvent.VK_F);
		lblFacility.setLabelFor(spnFacility);
		IGUI.addComponent(lblFacility, 0, 0, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		IGUI.addComponent(spnFacility, 1, 0, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnFacility);
		SpinnerNumberModel mdlSpecialty = new SpinnerNumberModel(rank[1], 0, 5, 1);
		JSpinner spnSpecialty = new JSpinner(mdlSpecialty) {
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnSpecialty.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = ((Integer) spinners.get(1).getValue()).intValue();
					resetValues(ROW_SPECIALTY, newValue);
				}
			}
		});
		JFormattedTextField ftfSpecialty = getTextField(spnSpecialty);
		if (ftfSpecialty != null ) {
			ftfSpecialty.setColumns(5);
			ftfSpecialty.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnSpecialty.setEditor(new JSpinner.NumberEditor(spnSpecialty, "#"));
		JLabel lblSpecialty = new JLabel("Specialty");
		lblSpecialty.setDisplayedMnemonic(KeyEvent.VK_S);
		lblSpecialty.setLabelFor(spnSpecialty);
		IGUI.addComponent(lblSpecialty, 0, 1, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		IGUI.addComponent(spnSpecialty, 1, 1, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnSpecialty);
		SpinnerNumberModel mdlSubspecialty = new SpinnerNumberModel(rank[2], 0, 5, 1);
		JSpinner spnSubspecialty = new JSpinner(mdlSubspecialty) {
			private static final long serialVersionUID = -5306144978669153048L;
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnSubspecialty.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = ((Integer) spinners.get(2).getValue()).intValue();
					resetValues(ROW_SUBSPECIAL, newValue);
				}
			}
		});
		JFormattedTextField ftfSubspecialty = getTextField(spnSubspecialty);
		if (ftfSubspecialty != null ) {
			ftfSubspecialty.setColumns(5);
			ftfSubspecialty.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnSubspecialty.setEditor(new JSpinner.NumberEditor(spnSubspecialty, "#"));
		JLabel lblSubspecialty = new JLabel("Subspecialty");
		lblSubspecialty.setDisplayedMnemonic(KeyEvent.VK_B);
		lblSubspecialty.setLabelFor(spnSubspecialty);
		IGUI.addComponent(lblSubspecialty, 0, 2, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		IGUI.addComponent(spnSubspecialty, 1, 2, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnSubspecialty);
		SpinnerNumberModel mdlProcedures = new SpinnerNumberModel(rank[3], 0, 5, 1);
		JSpinner spnProcedures= new JSpinner(mdlProcedures) {
			private static final long serialVersionUID = 402555220787352560L;
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnProcedures.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = ((Integer) spinners.get(3).getValue()).intValue();
					resetValues(ROW_PROCEDURE, newValue);
				}
			}
		});
		JFormattedTextField ftfProcedures = getTextField(spnProcedures);
		if (ftfProcedures != null ) {
			ftfProcedures.setColumns(5);
			ftfProcedures.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnProcedures.setEditor(new JSpinner.NumberEditor(spnProcedures, "#"));
		JLabel lblProcedures = new JLabel("Procedures");
		lblProcedures.setDisplayedMnemonic(KeyEvent.VK_R);
		lblProcedures.setLabelFor(spnProcedures);
		IGUI.addComponent(lblProcedures, 0, 3, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		IGUI.addComponent(spnProcedures, 1, 3, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnProcedures);
		SpinnerNumberModel mdlStaff = new SpinnerNumberModel(rank[4], 0, 5, 1);
		JSpinner spnStaff= new JSpinner(mdlStaff) {
			private static final long serialVersionUID = 6556424348594464923L;
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnStaff.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = ((Integer) spinners.get(4).getValue()).intValue();
					resetValues(ROW_STAFF, newValue);
				}
			}
		});
		JFormattedTextField ftfStaff = getTextField(spnStaff);
		if (ftfStaff != null ) {
			ftfStaff.setColumns(5);
			ftfStaff.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnStaff.setEditor(new JSpinner.NumberEditor(spnStaff, "#"));
		JLabel lblStaff = new JLabel("Pathologists");
		lblStaff.setDisplayedMnemonic(KeyEvent.VK_P);
		lblStaff.setLabelFor(spnStaff);
		IGUI.addComponent(lblStaff, 0, 4, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		IGUI.addComponent(spnStaff, 1, 4, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnStaff);
		JButton btnOkay = new JButton("OK");
		btnOkay.setMnemonic(KeyEvent.VK_O);
		btnOkay.setIcon(IGUI.getIcon(48, "ok"));
		btnOkay.setActionCommand("Okay");
		btnOkay.setFocusable(true);
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IPanelRows.this.firePropertyChange("Confirm", null, selected);
			}
		});
		if (rank.length == 5) {
			IGUI.addComponent(btnOkay, 0, 5, 2, 1, 1.0, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		} else {
			IGUI.addComponent(btnOkay, 0, 4, 2, 1, 1.0, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		}
	}

	private JFormattedTextField getTextField(JSpinner spinner) {
		JComponent editor = spinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			return ((JSpinner.DefaultEditor)editor).getTextField();
		} else {
			return null;
		}
	}

	public byte[] getValue() {
		return selected;
	}

	private void resetValues(byte element, int newPos) {
		boolean previousChange = programmaticChange;
		programmaticChange = true;
		if (newPos == 0) {
			// put it in last position so it will be ignored
			for (int i = 0; i < selected.length-1; i++) {
				selected[i] = selected[i+1];
			}
			selected[selected.length-1] = 0;
		} else {
			// Switch old element and new element positions, if not 0
			byte oldElement = selected[newPos-1];
			if (oldElement != 0) {
				for (int i = 0; i < selected.length; i++) {
					if (selected[i] == element) {
						SpinnerNumberModel model = (SpinnerNumberModel) spinners.get(0).getModel();
						switch (oldElement) {
						case ROW_FACILITY:
							model = (SpinnerNumberModel) spinners.get(0).getModel();
							break;
						case ROW_SPECIALTY:
							model = (SpinnerNumberModel) spinners.get(1).getModel();
							break;
						case ROW_SUBSPECIAL:
							model = (SpinnerNumberModel) spinners.get(2).getModel();
							break;
						case ROW_PROCEDURE:
							model = (SpinnerNumberModel) spinners.get(3).getModel();
							break;
						default:
							model = (SpinnerNumberModel) spinners.get(4).getModel();
						}
						model.setValue(i+1);
						selected[i] = oldElement;
						break;
					}
				}
			}
			selected[newPos-1] = element;
		}
		programmaticChange = previousChange;
		if (selectionTimer.isRunning()) {
			selectionTimer.restart();
		} else {
			selectionTimer.start();
		}
	}

	private void setValue() {
		if (!programmaticChange) {
			boolean previousChange = programmaticChange;
			programmaticChange = true;
			int[] rank = new int[selected.length];
			for (int i = 0; i < selected.length; i++) {
				original[i] = selected[i];
				switch (selected[i]) {
				case ROW_FACILITY:
					rank[0] = i+1;
					break;
				case ROW_SPECIALTY:
					rank[1] = i+1;
					break;
				case ROW_SUBSPECIAL:
					rank[2] = i+1;
					break;
				case ROW_PROCEDURE:
					rank[3] = i+1;
					break;
				case ROW_STAFF:
					rank[4] = i+1;
					break;
				default:
					// 0 = ignore
				}
			}
			SpinnerNumberModel model = (SpinnerNumberModel) spinners.get(0).getModel();
			int oldValue = ((Integer) model.getValue()).intValue();
			if (oldValue != rank[0]) {
				model.setValue(rank[0]);
			}
			model = (SpinnerNumberModel) spinners.get(1).getModel();
			oldValue = ((Integer) model.getValue()).intValue();
			if (oldValue != rank[1]) {
				model.setValue(rank[1]);
			}
			model = (SpinnerNumberModel) spinners.get(2).getModel();
			oldValue = ((Integer) model.getValue()).intValue();
			if (oldValue != rank[2]) {
				model.setValue(rank[2]);
			}
			model = (SpinnerNumberModel) spinners.get(3).getModel();
			oldValue = ((Integer) model.getValue()).intValue();
			if (oldValue != rank[3]) {
				model.setValue(rank[3]);
			}
			if (rank.length == 5) {
				model = (SpinnerNumberModel) spinners.get(4).getModel();
				oldValue = ((Integer) model.getValue()).intValue();
				if (oldValue != rank[4]) {
					model.setValue(rank[4]);
				}
			}
			programmaticChange = previousChange;
		}
	}
	
	void setValue(byte[] newValue) {
		for (int i = 0; i < newValue.length; i++) {
			selected[i] = newValue[i];
		}
		setValue();
	}
}