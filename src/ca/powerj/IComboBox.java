package ca.powerj;

import java.awt.Dimension;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

class IComboBox extends JComboBox<OItem> {

	IComboBox() {
		super();
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		setFont(LConstants.APP_FONT);
		// has to be editable
		// setEditable(true);
		// change the editor's document
		// new IComboMatch(this);
	}

	short getIndex() {
		if (getSelectedItem() != null) {
			return ((OItem) getSelectedItem()).id;
		}
		return 0;
	}

	OItem getItem() {
		if (getSelectedItem() != null) {
			return (OItem) getSelectedItem();
		}
		return null;
	}

	String getItemName(short index) {
		for (int i = 0; i < getItemCount(); i++) {
			OItem item = getItemAt(i);
			if (item.id == index) {
				return item.name;
			}
		}
		return null;
	}

	String getSelection() {
		if (getSelectedItem() != null) {
			return ((OItem) getSelectedItem()).name;
		}
		return null;
	}

	void setIndex(OItem item) {
		setSelectedItem(item);
	}

	void setIndex(int index) {
		setSelectedIndex(-1);
		ComboBoxModel model = getModel();
		for (int i = 0; i < getItemCount(); i++) {
			Object o = model.getElementAt(i);
			if (o != null) {
				OItem item = (OItem) o;
				if (item.id == index) {
					setSelectedItem(o);
					break;
				}
			}
		}
	}

	void setModel(Object[] items) {
		setModel(new DefaultComboBoxModel(items));
	}
}