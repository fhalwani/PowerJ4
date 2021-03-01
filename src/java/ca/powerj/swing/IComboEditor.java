package ca.powerj.swing;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import ca.powerj.data.ItemData;

public class IComboEditor extends AbstractCellEditor implements TableCellEditor {
	private IComboBox comboBox;

	public IComboEditor(IComboBox combobox) {
		this.comboBox = combobox;
	}

	// Override to ensure that the value remains an ItemData.
	public Object getCellEditorValue() {
		return comboBox.getItem();
	}

	// Override to invoke setValue on the formatted text field.
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value instanceof ItemData) {
			comboBox.setIndex((ItemData) value);
		} else {
			comboBox.setSelectedText(value.toString());
		}
		return comboBox;
	}
}