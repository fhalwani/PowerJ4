package ca.powerj.swing;
import java.awt.Color;
import java.awt.Component;
import java.util.function.Supplier;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import ca.powerj.data.ItemData;

public class IComboBoxFilterRenderer extends DefaultListCellRenderer {
	public static final Color background = new Color(225, 240, 255);
	private static final Color defaultBackground = (Color) UIManager.get("List.background");
	private static final Color defaultForeground = (Color) UIManager.get("List.foreground");
	private Supplier<String> highlightTextSupplier;

	public static String getDisplayText(ItemData item) {
		if (item == null) {
			return "";
		}
		return item.getName();
	}

	public IComboBoxFilterRenderer(Supplier<String> highlightTextSupplier) {
		this.highlightTextSupplier = highlightTextSupplier;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		ItemData item = (ItemData) value;
		if (item == null) {
			return this;
		}
		String text = getDisplayText(item);
		text = IComboBoxFilterHighlighter.highlightText(text, highlightTextSupplier.get());
		this.setText(text);
		if (!isSelected) {
			this.setBackground(index % 2 == 0 ? background : defaultBackground);
		}
		this.setForeground(defaultForeground);
		return this;
	}
}