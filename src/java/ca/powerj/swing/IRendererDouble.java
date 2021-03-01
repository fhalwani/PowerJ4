package ca.powerj.swing;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibNumbers;

public class IRendererDouble extends JLabel implements TableCellRenderer {
	private int noFractions = 0;
	private LibNumbers numbers = null;

	public IRendererDouble(LibNumbers numbers, int noFractions) {
		super();
		this.numbers = numbers;
		this.noFractions = noFractions;
		setFont(LibConstants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		try {
			if (value instanceof Double) {
				if ((Double) value != 0.0) {
					setText(numbers.formatDouble(noFractions, (Double) value));
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