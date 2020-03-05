package ca.powerj;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

class IRenderInterval extends JLabel implements TableCellRenderer {
	private long millis = 0, seconds = 0, minutes = 0, hours = 0;
	private String interval = "";
	
	IRenderInterval() {
		super();
		setFont(LConstants.APP_FONT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		interval = "";
		try {
			millis = (Long)value;
			// A work shift is 8-12 hours; if 2 events have more than 8 hours difference, they are assumed to belong to different days
			if (millis < 28800000) {
				seconds = (millis / 1000) % 60;
				minutes = (millis / 60000) % 60;
				hours = millis / 3600000;
				if (hours > 0) {
					if (hours > 99) {
						hours = 99;
					}
					interval += hours + ":";
					if (minutes > 9) {
						interval += minutes + ":";
					} else {
						interval += "0" + minutes + ":";
					}
				} else if (minutes > 0) {
					interval += minutes + ":";
				}
				if (seconds > 9) {
					interval += seconds;
				} else if (hours > 0 || minutes > 0) {
					interval += "0" + seconds;
				} else {
					interval += seconds;
				}
			} else {
				interval = "--";
			}
			setText(interval);
		} catch (IllegalArgumentException ignore) {}
		if (isSelected) {
			setBackground(LConstants.COLOR_LIGHT_BLUE);
        } else {
			setBackground(LConstants.COLOR_EVEN_ODD[row % 2]);
        }
		return this;
	}
}