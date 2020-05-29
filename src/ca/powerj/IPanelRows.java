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
	static final byte ROW_IGNORE     = 0;
	static final byte ROW_FACILITY   = 1;
	static final byte ROW_SPECIALTY  = 2;
	static final byte ROW_SUBSPECIAL = 3;
	static final byte ROW_PROCEDURE  = 4;
	static final byte ROW_STAFF      = 5;
	private final byte SPN_FACILITY   = 0;
	private final byte SPN_SPECIALTY  = 1;
	private final byte SPN_SUBSPECIAL = 2;
	private final byte SPN_PROCEDURE  = 3;
	private final byte SPN_STAFF      = 4;
	private volatile boolean programmaticChange;
	private int[] selected, original, rank;
	private ArrayList<JSpinner> spinners = new ArrayList<JSpinner>();
	private Timer selectionTimer;

	IPanelRows(int[] value) {
		super(new GridBagLayout());
		original = value;
		selected = new int[value.length];
		rank = new int[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
			original[i] = value[i];
		}
		selectionTimer = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!original.equals(getValue())) {
					firePropertyChange("Value", original, getValue());
				}
			}
		});
		selectionTimer.setRepeats(false);
		setRanks();
		SpinnerNumberModel mdlFacility = new SpinnerNumberModel(rank[0], ROW_IGNORE, ROW_STAFF, 1);
		JSpinner spnFacility = new JSpinner(mdlFacility) {
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnFacility.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = (int) spinners.get(SPN_FACILITY).getValue();
					resetValues(SPN_FACILITY, newValue);
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
		SpinnerNumberModel mdlSpecialty = new SpinnerNumberModel(rank[1], ROW_IGNORE, ROW_STAFF, 1);
		JSpinner spnSpecialty = new JSpinner(mdlSpecialty) {
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnSpecialty.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = (int) spinners.get(SPN_SPECIALTY).getValue();
					resetValues(SPN_SPECIALTY, newValue);
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
		SpinnerNumberModel mdlSubspecialty = new SpinnerNumberModel(rank[2], ROW_IGNORE, ROW_STAFF, 1);
		JSpinner spnSubspecialty = new JSpinner(mdlSubspecialty) {
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnSubspecialty.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = (int) spinners.get(SPN_SUBSPECIAL).getValue();
					resetValues(SPN_SUBSPECIAL, newValue);
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
		SpinnerNumberModel mdlProcedures = new SpinnerNumberModel(rank[3], ROW_IGNORE, ROW_STAFF, 1);
		JSpinner spnProcedure = new JSpinner(mdlProcedures) {
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		spnProcedure.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int newValue = (int) spinners.get(SPN_PROCEDURE).getValue();
					resetValues(SPN_PROCEDURE, newValue);
				}
			}
		});
		JFormattedTextField ftfProcedures = getTextField(spnProcedure);
		if (ftfProcedures != null ) {
			ftfProcedures.setColumns(5);
			ftfProcedures.setHorizontalAlignment(JTextField.RIGHT);
		}
		// Make the format without a thousands separator.
		spnProcedure.setEditor(new JSpinner.NumberEditor(spnProcedure, "#"));
		JLabel lblProcedures = new JLabel("Procedure");
		lblProcedures.setDisplayedMnemonic(KeyEvent.VK_R);
		lblProcedures.setLabelFor(spnProcedure);
		IGUI.addComponent(lblProcedures, 0, 3, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		IGUI.addComponent(spnProcedure, 1, 3, 1, 1, 0.5, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
		spinners.add(spnProcedure);
		JButton btnOkay = new JButton("OK");
		btnOkay.setMnemonic(KeyEvent.VK_O);
		btnOkay.setIcon(IGUI.getIcon(48, "ok"));
		btnOkay.setActionCommand("Okay");
		btnOkay.setFocusable(true);
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IPanelRows.this.firePropertyChange("Confirm", null, getValue());
			}
		});
		if (rank.length == 5) {
			SpinnerNumberModel mdlStaff = new SpinnerNumberModel(rank[4], ROW_IGNORE, ROW_STAFF, 1);
			JSpinner spnStaff= new JSpinner(mdlStaff) {
				public ComponentOrientation getComponentOrientation() {
					return ComponentOrientation.RIGHT_TO_LEFT;
				}
			};
			spnStaff.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (!programmaticChange) {
						int newValue = (int) spinners.get(SPN_STAFF).getValue();
						resetValues(SPN_STAFF, newValue);
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
			JLabel lblStaff = new JLabel("Staff");
			lblStaff.setDisplayedMnemonic(KeyEvent.VK_P);
			lblStaff.setLabelFor(spnStaff);
			IGUI.addComponent(lblStaff, 0, 4, 1, 1, 0.5, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
			IGUI.addComponent(spnStaff, 1, 4, 1, 1, 0.5, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
			spinners.add(spnStaff);
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

	public int[] getValue() {
		return selected;
	}

	private void resetValues(byte element, int newPos) {
		boolean previousChange = programmaticChange;
		programmaticChange = true;
		if (newPos == ROW_IGNORE) {
			// Move all other elements down 1
			for (int i = 0; i < rank.length; i++) {
				if (i != element) {
					if (rank[i] > ROW_IGNORE) {
						rank[i]--;
					}
				}
			}
		} else {
			for (int i = 0; i < rank.length; i++) {
				// Switch positions with the previous element
				if (i != element) {
					if (rank[i] > ROW_IGNORE) {
						if (rank[i] == newPos) {
							rank[i] = rank[element];
							break;
						}
					}
				}
			}
		}
		rank[element] = newPos;
		for (int i = 0; i < selected.length; i++) {
			selected[i] = 0;
		}
		for (int i = 0; i < rank.length; i++) {
			// Move zeros to the end
			if (rank[i] > ROW_IGNORE) {
				selected[rank[i] -1] = i +1;
			}
		}
		setValue();
		programmaticChange = previousChange;
		if (selectionTimer.isRunning()) {
			selectionTimer.restart();
		} else {
			selectionTimer.start();
		}
	}

	private void setRanks() {
		for (int i = 0; i < selected.length; i++) {
			switch (selected[i]) {
			case ROW_FACILITY:
				rank[SPN_FACILITY] = ROW_FACILITY;
				break;
			case ROW_SPECIALTY:
				rank[SPN_SPECIALTY] = ROW_SPECIALTY;
				break;
			case ROW_SUBSPECIAL:
				rank[SPN_SUBSPECIAL] = ROW_SUBSPECIAL;
				break;
			case ROW_PROCEDURE:
				rank[SPN_PROCEDURE] = ROW_PROCEDURE;
				break;
			case ROW_STAFF:
				rank[SPN_STAFF] = ROW_STAFF;
				break;
			default:
				// 0 = ignore
			}
		}
	}

	private void setValue() {
		SpinnerNumberModel model = null;
		int oldValue = 0;
		for (int i = 0; i < spinners.size(); i++) {
			model = (SpinnerNumberModel) spinners.get(i).getModel();
			oldValue = (int) model.getValue();
			if (oldValue != rank[i]) {
				model.setValue(rank[i]);
			}
		}
	}
	
	void setValue(int[] newValue) {
		for (int i = 0; i < newValue.length; i++) {
			selected[i] = newValue[i];
		}
		setValue();
	}
}