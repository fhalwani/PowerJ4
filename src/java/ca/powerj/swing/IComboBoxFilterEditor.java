package ca.powerj.swing;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import ca.powerj.lib.LibConstants;

public class IComboBoxFilterEditor<ItemData> extends BasicComboBoxEditor {
	private JLabel filterLabel = new JLabel();
	private String text = "";
	boolean editing;
	private Function<ItemData, String> displayTextFunction;
	private Consumer<Boolean> editingChangeListener;
	private Object selected;

	public IComboBoxFilterEditor(Function<ItemData, String> displayTextFunction,
			Consumer<Boolean> editingChangeListener) {
		this.displayTextFunction = displayTextFunction;
		this.editingChangeListener = editingChangeListener;
	}

	@Override
	public void addActionListener(ActionListener l) {
	}

	public void addChar(char c) {
		text += c;
		if (!editing) {
			enableEditingMode();
		}
	}

	private void enableEditingMode() {
		editing = true;
		filterLabel.setFont(LibConstants.APP_FONT);
		editingChangeListener.accept(true);
	}

	@Override
	public Component getEditorComponent() {
		return filterLabel;
	}

	public JLabel getFilterLabel() {
		return filterLabel;
	}

	@Override
	public Object getItem() {
		return selected;
	}

	public String getText() {
		return text;
	}

	public boolean isEditing() {
		return editing;
	}

	@Override
	public void removeActionListener(ActionListener l) {
	}

	public void removeCharAtEnd() {
		if (text.length() > 0) {
			text = text.substring(0, text.length() - 1);
			if (!editing) {
				enableEditingMode();
			}
		}
	}

	public void reset() {
		if (editing) {
			filterLabel.setFont(UIManager.getFont("ComboBox.font"));
			filterLabel.setForeground(UIManager.getColor("Label.foreground"));
			text = "";
			editing = false;
			editingChangeListener.accept(false);
		}
	}

	@Override
	public void selectAll() {
	}

	@Override
	public void setItem(Object anObject) {
		if (editing) {
			filterLabel.setText(text);
		} else {
			ItemData item = (ItemData) anObject;
			filterLabel.setText(displayTextFunction.apply(item));
		}
		this.selected = anObject;
	}
}