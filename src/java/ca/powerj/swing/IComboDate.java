package ca.powerj.swing;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Calendar;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class IComboDate extends JComboBox {
	private final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
	private DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
	private Calendar min;
	private Calendar max;
	private final IMonthPanel pnlMonth;
	private final JPopupMenu popupMenu = new JPopupMenu();

	public IComboDate(Calendar value, Calendar minDate, Calendar maxDate) {
		min = minDate;
		max = maxDate;
		if (value == null) {
			value = Calendar.getInstance();
		}
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		pnlMonth = new IMonthPanel(value, min, max);
		comboModel.addElement(value);
		setModel(comboModel);
		setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean hasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
				setText(formatter.format(((Calendar) value).getTime()));
				return this;
			}
		});
		popupMenu.add(pnlMonth);
		addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
				final boolean popupShown = popupMenu.isShowing();
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						hidePopup();
						if (popupShown) {
							popupMenu.setVisible(false);
						} else {
							pnlMonth.setValue(getValue());
							popupMenu.show(IComboDate.this, 0, getHeight());
						}
					}
				});
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent pme) {
			}
		});
		pnlMonth.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				if ("Confirm".equals(property)) {
					setValue((Calendar) e.getNewValue());
					firePropertyChange("Value", e.getOldValue(), e.getNewValue());
					popupMenu.setVisible(false);
				} else if ("Value".equals(property)) {
					setValue((Calendar) e.getNewValue());
					firePropertyChange("Value", e.getOldValue(), e.getNewValue());
				}
			}
		});
	}

	public Calendar getValue() {
		return (Calendar) comboModel.getElementAt(0);
	}

	public void setMax(Calendar value) {
		max = value;
		pnlMonth.setMax(value);
	}

	public void setMin(Calendar value) {
		min = value;
		pnlMonth.setMin(value);
	}

	public void setValue(Calendar value) {
		if (getSelectedItem() != null && getSelectedItem().equals(value)) {
			return;
		}
		if (min != null && value.getTimeInMillis() < min.getTimeInMillis()) {
			value = min;
		}
		if (max != null && value.getTimeInMillis() > max.getTimeInMillis()) {
			value = max;
		}
		setSelectedItem(value);
		comboModel.removeAllElements();
		comboModel.addElement(value);
		if (!pnlMonth.getValue().equals(value)) {
			pnlMonth.setValue(value);
		}
	}
}