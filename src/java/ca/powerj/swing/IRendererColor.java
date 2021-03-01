package ca.powerj.swing;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibNumbers;

public class IRendererColor extends JLabel implements TableCellRenderer {
	private LibNumbers numbers = null;

	public IRendererColor(LibNumbers numbers) {
		super();
		this.numbers = numbers;
		setFont(LibConstants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		try {
			if (value instanceof Integer) {
				setText(numbers.formatNumber((Integer) value));
			} else if (value instanceof Short) {
				setText(numbers.formatNumber((Short) value));
			} else if (value instanceof Byte) {
				setText(numbers.formatNumber((Byte) value));
			} else if (value instanceof Long) {
				setText(numbers.formatNumber((Long) value));
			} else {
				setText(value.toString());
			}
		} catch (IllegalArgumentException ignore) {}
		if (value instanceof Short) {
			Color bg = ((Short)value > 100 ? Color.RED :
				(Short)value > 70 ? LibConstants.COLOR_AMBER : Color.GREEN);
			setBackground(bg);
		}
		return this;
	}
}