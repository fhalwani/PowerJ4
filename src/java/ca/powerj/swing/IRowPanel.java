package ca.powerj.swing;
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

public class IRowPanel extends JPanel {
	public static final byte ROW_IGNORE     = 0;
	public static final byte ROW_FACILITY   = 1;
	public static final byte ROW_SPECIALTY  = 2;
	public static final byte ROW_SUBSPECIAL = 3;
	public static final byte ROW_PROCEDURE  = 4;
	public static final byte ROW_SPECIMEN   = 5;
	public static final byte ROW_STAFF      = 5;
	public static final byte SPN_FACILITY   = 0;
	public static final byte SPN_SPECIALTY  = 1;
	public static final byte SPN_SUBSPECIAL = 2;
	public static final byte SPN_PROCEDURE  = 3;
	public static final byte SPN_SPECIMEN   = 4;
	public static final byte SPN_STAFF      = 5;
	private volatile boolean programmaticChange;
	private int[] original, selected, rank;
	private ArrayList<JSpinner> spinners = new ArrayList<JSpinner>();
	private Timer selectionTimer;

	public IRowPanel(byte[] rows, byte[] values) {
		super(new GridBagLayout());
		rank = new int[values.length];
		selected = new int[values.length];
		original = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			selected[i] = values[i];
			original[i] = values[i];
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
		JButton btnOkay = new JButton("OK");
		btnOkay.setMnemonic(KeyEvent.VK_O);
		btnOkay.setIcon(IUtilities.getIcon(48, "ok"));
		btnOkay.setActionCommand("Okay");
		btnOkay.setFocusable(true);
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IRowPanel.this.firePropertyChange("Confirm", null, getValue());
			}
		});
		for (byte i = 0; i < rows.length; i++) {
			final byte position = i;
			SpinnerNumberModel model = new SpinnerNumberModel(rank[i], 0, rank.length, 1);
			JSpinner spinner = new JSpinner(model) {
				public ComponentOrientation getComponentOrientation() {
					return ComponentOrientation.RIGHT_TO_LEFT;
				}
			};
			spinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (!programmaticChange) {
						int newValue = (int) spinners.get(position).getValue();
						resetValues(position, newValue);
					}
				}
			});
			JFormattedTextField textField = getTextField(spinner);
			if (textField != null ) {
				textField.setColumns(1);
				textField.setHorizontalAlignment(JTextField.RIGHT);
			}
			JLabel label = new JLabel();
			switch (rows[i]) {
			case SPN_FACILITY:
				label.setText("Facility: ");
				label.setDisplayedMnemonic(KeyEvent.VK_F);
				break;
			case SPN_SPECIALTY:
				label.setText("Specialty: ");
				label.setDisplayedMnemonic(KeyEvent.VK_S);
				break;
			case SPN_SUBSPECIAL:
				label.setText("Subspecialty: ");
				label.setDisplayedMnemonic(KeyEvent.VK_U);
				break;
			case SPN_PROCEDURE:
				label.setText("Procedure: ");
				label.setDisplayedMnemonic(KeyEvent.VK_P);
				break;
			case SPN_SPECIMEN:
				label.setText("Specimen: ");
				label.setDisplayedMnemonic(KeyEvent.VK_E);
				break;
			default:
				label.setText("Staff: ");
				label.setDisplayedMnemonic(KeyEvent.VK_T);
			}
			if (i == 0) {
				spinner.addAncestorListener(new IFocusListener());
			}
			label.setLabelFor(spinner);
			IUtilities.addComponent(label, 0, i, 1, 1, 0.5, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
			IUtilities.addComponent(spinner, 1, i, 1, 1, 0.5, 0.2,
					GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
			spinners.add(spinner);
		}
		IUtilities.addComponent(btnOkay, 0, rows.length, 2, 1, 1.0, 0.2,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, this);
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
		for (byte i = 0; i < selected.length; i++) {
			selected[i] = 0;
		}
		for (byte i = 0; i < rank.length; i++) {
			// Move zeros to the end
			if (rank[i] > ROW_IGNORE) {
				selected[rank[i] -1] = (byte) (i +1);
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
				rank[4] = ROW_STAFF;
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
		for (byte i = 0; i < newValue.length; i++) {
			selected[i] = newValue[i];
		}
		setValue();
	}
}