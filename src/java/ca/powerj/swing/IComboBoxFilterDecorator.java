package ca.powerj.swing;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import ca.powerj.data.ItemData;

public class IComboBoxFilterDecorator<T> {
	private JComboBox<T> comboBox;
	private BiPredicate<T, String> userFilter;
	private Function<T, String> comboDisplayTextMapper;
	ArrayList<ItemData> originalItems;
	ArrayList<T> activeItems;
	private Object selectedItem;
	private IComboBoxFilterEditor filterEditor;

	public IComboBoxFilterDecorator(JComboBox<T> comboBox,
			BiPredicate<T, String> userFilter,
			Function<T, String> comboDisplayTextMapper) {
		this.comboBox = comboBox;
		this.userFilter = userFilter;
		this.comboDisplayTextMapper = comboDisplayTextMapper;
		setOriginalItems();
	}

	public static <T> IComboBoxFilterDecorator<T> decorate(JComboBox<T> comboBox,
			Function<T, String> comboDisplayTextMapper,
			BiPredicate<T, String> userFilter) {
		IComboBoxFilterDecorator decorator = new IComboBoxFilterDecorator(comboBox, userFilter, comboDisplayTextMapper);
		decorator.init();
		return decorator;
	}

	private void applyFilter() {
		DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
		model.removeAllElements();
		ArrayList<T> filteredItems = new ArrayList<>();
		// add matched items at top
		for (T t : activeItems) {
			if (userFilter.test(t, filterEditor.getFilterLabel().getText())) {
				model.addElement(t);
			} else {
				filteredItems.add(t);
			}
		}
		// red color when no match
		filterEditor.getFilterLabel().setForeground(model.getSize() == 0 ?
				Color.red : UIManager.getColor("Label.foreground"));
		//add unmatched items
		filteredItems.forEach(model::addElement);
	}

	public Supplier<String> getFilterTextSupplier() {
		return () -> {
			if (filterEditor.isEditing()) {
				return filterEditor.getFilterLabel().getText();
			}
			return "";
		};
	}

	private void init() {
		prepareComboFiltering();
		initComboPopupListener();
		initComboKeyListener();
	}

	private void initComboKeyListener() {
		filterEditor.getFilterLabel().addKeyListener(
			new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					char keyChar = e.getKeyChar();
					if (!Character.isDefined(keyChar)) {
						return;
					}
					int keyCode = e.getKeyCode();
					switch (keyCode) {
					case KeyEvent.VK_DELETE:
						return;
					case KeyEvent.VK_ENTER:
						selectedItem = comboBox.getSelectedItem();
						resetFilterComponent();
						return;
					case KeyEvent.VK_ESCAPE:
						resetFilterComponent();
						return;
					case KeyEvent.VK_BACK_SPACE:
						filterEditor.removeCharAtEnd();
						break;
					default:
						filterEditor.addChar(keyChar);
					}
					if (!comboBox.isPopupVisible()) {
						comboBox.showPopup();
					}
					if (filterEditor.isEditing() && filterEditor.getText().length() > 0) {
						applyFilter();
					} else {
						comboBox.hidePopup();
						resetFilterComponent();
					}
				}
			}
		);
	}

	private void initComboPopupListener() {
		comboBox.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				resetFilterComponent();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				resetFilterComponent();
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
		});
	}

	private void prepareComboFiltering() {
		DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
		this.activeItems = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			this.activeItems.add(model.getElementAt(i));
		}
		filterEditor = new IComboBoxFilterEditor(comboDisplayTextMapper, new Consumer<Boolean>() {
			//editing mode (commit/cancel) change listener
			@Override
			public void accept(Boolean aBoolean) {
				if (aBoolean) {//commit
					selectedItem = comboBox.getSelectedItem();
				} else {//rollback to the last one
					comboBox.setSelectedItem(selectedItem);
					filterEditor.setItem(selectedItem);
				}
			}
		});
		JLabel filterLabel = filterEditor.getFilterLabel();
		filterLabel.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				filterLabel.setBorder(BorderFactory.createLoweredBevelBorder());
			}

			@Override
			public void focusLost(FocusEvent e) {
				filterLabel.setBorder(UIManager.getBorder("TextField.border"));
				resetFilterComponent();
			}
		});
		comboBox.setEditor(filterEditor);
		comboBox.setEditable(true);
	}

	private void resetFilterComponent() {
		if (!filterEditor.isEditing()) {
			return;
		}
		// restore original order
		DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
		model.removeAllElements();
		for (T t : activeItems) {
			model.addElement(t);
		}
		filterEditor.reset();
	}

	private void setOriginalItems() {
		DefaultComboBoxModel<ItemData> model = (DefaultComboBoxModel<ItemData>) comboBox.getModel();
		originalItems = new ArrayList<ItemData>(model.getSize());
		for (int i = 0; i < model.getSize(); i++) {
			originalItems.add(model.getElementAt(i));
		}
	}
}