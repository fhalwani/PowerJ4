package ca.powerj;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

class IComboRows extends JComboBox {
	static String[] choices = new String[6];
	private final DefaultComboBoxModel model = new DefaultComboBoxModel();
	private IPanelRows pnlRows;
	private final JPopupMenu popupMenu = new JPopupMenu();

	IComboRows(byte[] rows, byte[] values) {
		super();
		int[] selected = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			selected[i] = values[i];
		}
		choices[0] = "";
		for (byte i = 0; i < rows.length; i++) {
			switch (rows[i]) {
			case IPanelRows.SPN_FACILITY:
				choices[i+1] = ":Fac";
				break;
			case IPanelRows.SPN_SPECIALTY:
				choices[i+1] = ":Spy";
				break;
			case IPanelRows.SPN_SUBSPECIAL:
				choices[i+1] = ":Sub";
				break;
			case IPanelRows.SPN_PROCEDURE:
				choices[i+1] = ":Pro";
				break;
			case IPanelRows.SPN_SPECIMEN:
				choices[i+1] = ":Spe";
				break;
			case IPanelRows.SPN_STAFF:
				choices[i+1] = ":Sta";
				break;
			default:
				choices[i+1] = "";
			}
		}
		Dimension dim = new Dimension(170, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		pnlRows = new IPanelRows(rows, values);
		model.addElement(selected);
		setModel(model);
		setRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean hasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
				int[] results = (int[]) model.getElementAt(0);
				String display = "";
				for (int i = 0; i < results.length; i++) {
					display += choices[results[i]];
				}
				if (display.length() > 2) {
					display = display.substring(1);
				}
				setText(display);
				return this;
			}
		});
		popupMenu.add(pnlRows);
		addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
				final boolean popupShown = popupMenu.isShowing();
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						hidePopup();
						if (popupShown) {
							popupMenu.setVisible(false);
						} else {
							pnlRows.setValue(getValue());
							popupMenu.show(IComboRows.this, 0, getHeight());
						}
					}
				});
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			public void popupMenuCanceled(PopupMenuEvent e) {}
		});
		pnlRows.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Confirm".equals(property)) {
					setValue((int[]) e.getNewValue());
					popupMenu.setVisible(false);
				} else if ("Cancel".equals(property)) {
					setValue((int[]) e.getOldValue());
					popupMenu.setVisible(false);
				} else if ("Value".equals(property)) {
					setValue((int[]) e.getNewValue());
					firePropertyChange("Value", e.getOldValue(), e.getNewValue());
				}
			}
		});
	}
	
	public int[] getValue() {
		return (int[]) model.getElementAt(0);
	}

	public void setValue(int[] value) {
		int[] selected = new int[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
		}
		model.removeAllElements();
		model.addElement(selected);
		setSelectedItem(selected);
	}
}