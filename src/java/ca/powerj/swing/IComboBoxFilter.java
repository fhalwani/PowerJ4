package ca.powerj.swing;

import java.awt.Dimension;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import ca.powerj.data.ItemData;
import ca.powerj.lib.LibConstants;

public class IComboBoxFilter extends JComboBox<ItemData> {
	private IComboBoxFilterDecorator<ItemData> decorate;

	public IComboBoxFilter() {
		super();
		init();
	}

	public IComboBoxFilter(ItemData[] items) {
		super(items);
		init();
	}

	private void init() {
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		setFont(LibConstants.APP_FONT);
		decorate = IComboBoxFilterDecorator.decorate(this, IComboBoxFilterRenderer::getDisplayText,
				IComboBoxFilter::isFilter);
		setRenderer(new IComboBoxFilterRenderer(decorate.getFilterTextSupplier()));
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
				return item.toString();
			}
		}
		return null;
	}

	public String getSelection() {
		if (getSelectedItem() != null) {
			ItemData item = (ItemData) getSelectedItem();
			return item.toString();
		}
		return null;
	}

	private static boolean isFilter(ItemData item, String textToFilter) {
		if (textToFilter.isEmpty()) {
			return true;
		}
		return IComboBoxFilterRenderer.getDisplayText(item).toLowerCase().contains(textToFilter.toLowerCase());
	}

	public void setData(ItemData[] items) {
		DefaultComboBoxModel<ItemData> model = (DefaultComboBoxModel<ItemData>) getModel();
		model.removeAllElements();
		for (ItemData item : items) {
			model.addElement(item);
		}
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

	public void setIndex(ItemData item) {
		setSelectedItem(item);
	}

	public void setSelectedText(String name) {
		setSelectedIndex(-1);
		ComboBoxModel model = getModel();
		for (int i = 0; i < getItemCount(); i++) {
			Object obj = model.getElementAt(i);
			if (obj != null) {
				ItemData item = (ItemData) obj;
				if (name.equals(item.toString())) {
					setSelectedItem(obj);
					break;
				}
			}
		}
	}
}