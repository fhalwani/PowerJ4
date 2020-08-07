package ca.powerj;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

class IComboTableEditor extends AbstractCellEditor implements TableCellEditor {
	IComboBox combo;

	IComboTableEditor(IComboBox combo) {
		this.combo = combo;
	}

	// Override to ensure that the value remains an OItem.
    public Object getCellEditorValue() {
    	return combo.getItem();
    }

    // Override to invoke setValue on the formatted text field.
    public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected,
			int row, int column) {
		if (value instanceof OItem) {
			combo.setIndex((OItem) value);
		} else {
			combo.setSelectedText(value.toString());
		}
		return combo;
	}
}