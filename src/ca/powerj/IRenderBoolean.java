package ca.powerj;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class IRenderBoolean extends JCheckBox implements TableCellRenderer {

	IRenderBoolean() {
		super();
		setFont(LConstants.APP_FONT);
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
    		setBackground(LConstants.COLOR_LIGHT_BLUE);
    	} else {
			setBackground(LConstants.COLOR_EVEN_ODD[row % 2]);
    	}
    	return this;
    }
}
