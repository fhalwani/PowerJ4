package ca.powerj;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

class IStatusbar extends JPanel {
	private JLabel label;
	private JProgressBar bar;

	IStatusbar() {
		super();
		createPanel();
	}

	void clear() {
		label.setText("");
		bar.setVisible(false);
	}

	private void createPanel() {
		setLayout(new GridBagLayout());
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "");
		bar = new JProgressBar(0, 100);
		bar.setVisible(false);
		bar.setValue(0);
		IGUI.addComponent(label, 0, 0, 1, 1, 0.7, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, this);
		IGUI.addComponent(bar, 1, 0, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST, this);
	}

	void setMessage(String s) {
		final String m = s;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				label.setText(" " + m + " ");
			}
		});
	}

	void setProgress(int value) {
		if (value < 0) {
			bar.setVisible(true);
			bar.setIndeterminate(true);
		} else if (value < 100) {
			bar.setIndeterminate(false);
			bar.setValue(value);
		} else {
			bar.setValue(0);
			bar.setVisible(false);
		}
	}
}