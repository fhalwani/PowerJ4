package ca.powerj.swing;
import java.awt.Dimension;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import ca.powerj.data.ItemData;
import ca.powerj.lib.LibConstants;

public class IComboBox extends JComboBox<ItemData> {

	public IComboBox() {
		super();
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		setFont(LibConstants.APP_FONT);
		// has to be editable
		setEditable(true);
	}

	public int getIndex() {
		if (getSelectedItem() != null) {
			return ((ItemData) getSelectedItem()).getID();
		}
		return 0;
	}

	public ItemData getItem() {
		if (getSelectedItem() != null) {
			return (ItemData) getSelectedItem();
		}
		return null;
	}

	public String getItemName(short index) {
		for (int i = 0; i < getItemCount(); i++) {
			ItemData item = getItemAt(i);
			if (item.getID() == index) {
				return item.getName();
			}
		}
		return null;
	}

	public String getSelection() {
		if (getSelectedItem() != null) {
			return ((ItemData) getSelectedItem()).getName();
		}
		return null;
	}

	public void setIndex(ItemData item) {
		setSelectedItem(item);
	}

	public void setIndex(int index) {
		setSelectedIndex(-1);
		ComboBoxModel<ItemData> model = getModel();
		for (int i = 0; i < getItemCount(); i++) {
			Object o = model.getElementAt(i);
			if (o != null) {
				ItemData item = (ItemData) o;
				if (item.getID() == index) {
					setSelectedItem(o);
					break;
				}
			}
		}
	}

	public void setItems(Object[] items) {
		DefaultComboBoxModel model = (DefaultComboBoxModel) getModel();
		model.removeAllElements();
		for (int i = 0; i < items.length; i++) {
			model.addElement(items[i]);
		}
		// change the editor's document
		new IComboMatcher(this);
	}

	public void setSelectedText(String name) {
		setSelectedIndex(-1);
		ComboBoxModel model = getModel();
		for (int i = 0; i < getItemCount(); i++) {
			Object o = model.getElementAt(i);
			if (o != null) {
				ItemData item = (ItemData) o;
				if (item.getName() == name) {
					setSelectedItem(o);
					break;
				}
			}
		}
	}
}