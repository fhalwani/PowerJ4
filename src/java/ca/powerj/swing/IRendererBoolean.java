package ca.powerj.swing;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ca.powerj.lib.LibConstants;

public class IRendererBoolean extends JCheckBox implements TableCellRenderer {

	public IRendererBoolean() {
		super();
		setFont(LibConstants.APP_FONT);
		setOpaque(true);
		setHorizontalAlignment(JLabel.CENTER);
	}

	public Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected,
		boolean hasFocus, int row, int column) {
		if (value instanceof Boolean) {
			setSelected((Boolean) value);
		}
		if (isSelected) {
			setBackground(LibConstants.COLOR_LIGHT_BLUE);
		} else {
			setBackground(LibConstants.COLOR_EVEN_ODD[row % 2]);
		}
		return this;
	}
}