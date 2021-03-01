package ca.powerj.swing;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import ca.powerj.lib.LibDates;

public class ITimePanel extends JPanel implements FocusListener {
	protected boolean programmaticChange = true;
	private int minutes = 0, hours = 0, value = 0;
	private SpinnerNumberModel modelHour;
	private SpinnerNumberModel modelMin;
	private JSpinner spinHour;
	private JSpinner spinMin;

	public ITimePanel(int value) {
		super(new FlowLayout(FlowLayout.CENTER, 0, 0));
		if (value >= 0 && value < LibDates.ONE_DAY) {
			setTime(value);
		}
		createPanel();
		programmaticChange = false;
	}

	private void createPanel() {
		setFocusable(true);
		modelHour = new SpinnerNumberModel(hours, 0, 23, 1);
		spinHour = new JSpinner(modelHour) {
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		JFormattedTextField ftfHour = getTextField(spinHour);
		if (ftfHour != null ) {
			ftfHour.setColumns(3);
			ftfHour.setHorizontalAlignment(JTextField.LEFT);
			ftfHour.addFocusListener(this);
		}
		spinHour.addFocusListener(this);
		spinHour.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					hours = ((Integer) spinHour.getValue()).intValue();
					setValue();
				}
			}
		});
		add(spinHour);
		modelMin = new SpinnerNumberModel(minutes, 0, 59, 1);
		spinMin = new JSpinner(modelMin) {
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.LEFT_TO_RIGHT;
			}
		};
		JFormattedTextField ftfMin = getTextField(spinMin);
		if (ftfMin != null ) {
			ftfMin.setColumns(3);
			ftfMin.setHorizontalAlignment(JTextField.RIGHT);
			ftfMin.addFocusListener(this);
		}
		spinMin.addFocusListener(this);
		spinMin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					minutes = ((Integer) spinMin.getValue()).intValue();
					setValue();
				}
			}
		});
		add(spinMin);
	}

	private JFormattedTextField getTextField(JSpinner spinner) {
		JComponent editor = spinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			return ((JSpinner.DefaultEditor)editor).getTextField();
		} else {
			return null;
		}
	}

	public int getValue() {
		return value;
	}

	private void setTime(int value) {
		this.value = value;
		hours   = value / 3600000;
		minutes = (value % 3600000) / 60000;
	}

	private void setValue() {
		value = ((hours * 3600000) + (minutes * 60000));
		firePropertyChange("Value", null, value);
	}

	public void setValue(int value) {
		if (value >= 0 && value < 86400000) {
			programmaticChange = true;
			setTime(value);
			modelHour.setValue(hours);
			modelMin.setValue(minutes);
			programmaticChange = false;
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		firePropertyChange("Confirm", null, value);
	}

	@Override
	public void focusGained(FocusEvent e) {}
}