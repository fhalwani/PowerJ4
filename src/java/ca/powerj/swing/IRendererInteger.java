package ca.powerj.swing;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibNumbers;

public class IRendererInteger extends JLabel implements TableCellRenderer {
	private LibNumbers numbers = null;

	public IRendererInteger(LibNumbers numbers) {
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
				if ((Integer) value != 0) {
					setText(numbers.formatNumber((Integer) value));
				} else {
					setText("");
				}
			} else if (value instanceof Short) {
				if ((Short) value != 0) {
					setText(numbers.formatNumber((Short) value));
				} else {
					setText("");
				}
			} else if (value instanceof Byte) {
				if ((Byte) value != 0) {
					setText(numbers.formatNumber((Byte) value));
				} else {
					setText("");
				}
			} else if (value instanceof Long) {
				if ((Long) value != 0) {
					setText(numbers.formatNumber((Long) value));
				} else {
					setText("");
				}
			} else {
				setText("");
			}
		} catch (IllegalArgumentException ignore) {}
		if (isSelected) {
			setBackground(LibConstants.COLOR_LIGHT_BLUE);
		} else {
			setBackground(LibConstants.COLOR_EVEN_ODD[row % 2]);
		}
		return this;
	}
}