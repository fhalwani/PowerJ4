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
	private static final String[] strings = {"", ":FAC", ":SPE", ":SUB", ":PRO", ":STA"};
	private final DefaultComboBoxModel model = new DefaultComboBoxModel();
	private IPanelRows pnlRows;
	private final JPopupMenu popupMenu = new JPopupMenu();

	IComboRows(byte[] value) {
		super();
		byte[] selected = new byte[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
		}
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		pnlRows = new IPanelRows(selected);
		model.addElement(selected);
		setModel(model);
		setRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean hasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
				byte[] results = (byte[]) model.getElementAt(0);
				String display = "";
				for (byte i = 0; i < results.length; i++) {
					display += strings[results[i]];
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
					popupMenu.setVisible(false);
				}
			}
		});
		pnlRows.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Cancel".equals(property)) {
					popupMenu.setVisible(false);
					setValue((byte[]) e.getNewValue());
				}
			}
		});
		pnlRows.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Value".equals(property)) {
					setValue((byte[]) e.getNewValue());
					firePropertyChange("Value", e.getOldValue(), e.getNewValue());
				}
			}
		});
	}
	
	public byte[] getValue() {
		return (byte[]) model.getElementAt(0);
	}

	public void setValue(byte[] value) {
		byte[] selected = new byte[value.length];
		for (int i = 0; i < value.length; i++) {
			selected[i] = value[i];
		}
		setSelectedItem(selected);
		model.removeAllElements();
		model.addElement(selected);
		if (!pnlRows.getValue().equals(selected)) {
			pnlRows.setValue(selected);
		}
	}
}