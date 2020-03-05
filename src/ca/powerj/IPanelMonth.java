package ca.powerj;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

class IPanelMonth extends JPanel {
	private volatile boolean programmaticChange;
	private SpinnerListModel monthModel;
	private SpinnerNumberModel yearModel;
	private JSpinner monthSpinner;
	private JSpinner yearSpinner;
	private Calendar selectedDate;
	private volatile Calendar startDate;
	private Calendar min;
	private Calendar max;
	private final JTable table;

	IPanelMonth(Calendar initialDate, Calendar minDate, Calendar maxDate) {
		super(new BorderLayout());
		min = minDate;
		max = maxDate;
		selectedDate = initialDate;
		// Start Date is the start of the combobox view (1st Sunday on Col 0 and Row 0);
		startDate = Calendar.getInstance();
		startDate.setTimeInMillis(selectedDate.getTimeInMillis());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.DAY_OF_YEAR, -(startDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
		MonthViewTableModel model = new MonthViewTableModel();
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(false);
		table.setBorder(BorderFactory.createLineBorder(table.getForeground()));
		table.setCellSelectionEnabled(true);
		table.setDefaultRenderer(Calendar.class, new MonthViewTableCellRenderer());
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			columnModel.getColumn(i).setPreferredWidth(30);
		}
		JTableHeader header = table.getTableHeader();
		header.setBorder(table.getBorder());
		header.setReorderingAllowed(false);
		header.setResizingAllowed(false);
		header.setDefaultRenderer(new MonthViewTableHeaderCellRenderer(header.getDefaultRenderer()));
		final Timer selectionTimer = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Calendar newValue = (Calendar) table.getValueAt(table.getSelectedRow(),
						table.getSelectedColumn());
				if (selectedDate.getTimeInMillis() == newValue.getTimeInMillis()) {
					return;
				}
				boolean needSetTableSelection = false;
				if (min != null && newValue.getTimeInMillis() < min.getTimeInMillis()) {
					newValue.setTimeInMillis(min.getTimeInMillis());
					needSetTableSelection = true;
				}
				if (max != null && newValue.getTimeInMillis() > max.getTimeInMillis()) {
					newValue.setTimeInMillis(max.getTimeInMillis());
					needSetTableSelection = true;
				}
				firePropertyChange("Value", selectedDate, newValue);
				selectedDate.setTimeInMillis(newValue.getTimeInMillis());
				if (needSetTableSelection) {
					setTableSelection();
				}
				setSpinnersSelection();
			}
		});
		selectionTimer.setRepeats(false);
		ListSelectionListener selectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// if (!programmaticChange && !e.getValueIsAdjusting()) {
				if (!e.getValueIsAdjusting()) {
					if (selectionTimer.isRunning()) {
						selectionTimer.restart();
					} else {
						selectionTimer.start();
					}
				}
			}
		};
		ListSelectionModel rowSelectionModel = table.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSelectionModel.addListSelectionListener(selectionListener);
		ListSelectionModel columnSelectionModel = columnModel.getSelectionModel();
		columnSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnSelectionModel.addListSelectionListener(selectionListener);
		table.addMouseWheelListener(new MouseAdapter() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() < 0) {
					selectedDate.add(Calendar.MONTH, 1);
				} else {
					selectedDate.add(Calendar.MONTH, -1);
				}
				setSpinnersSelection();
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				IPanelMonth.this.firePropertyChange("Confirm", null, selectedDate);
			}
		});
		setTableSelection();
		// Modify actions of cursor keys
		changeTableAction(KeyEvent.VK_LEFT);
		changeTableAction(KeyEvent.VK_UP);
		changeTableAction(KeyEvent.VK_RIGHT);
		changeTableAction(KeyEvent.VK_DOWN);
		changeTableAction(KeyEvent.VK_ENTER);
		setFocusable(true);
		JPanel north = new JPanel(new BorderLayout());
		north.add(YearMonthSpinner(), BorderLayout.CENTER);
		Box tableBox = Box.createVerticalBox();
		tableBox.add(header);
		tableBox.add(table);
		add(north, BorderLayout.NORTH);
		add(tableBox, BorderLayout.CENTER);
	}

	private JPanel YearMonthSpinner() {
		int lowerRange = 0, upperRange = 0;
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		final String[] MONTHS = new DateFormatSymbols().getMonths();
		monthModel = new SpinnerListModel(Arrays.asList(MONTHS).subList(0, 12));
		monthSpinner = new JSpinner(monthModel) {
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.RIGHT_TO_LEFT;
			}
		};
		monthModel.setValue(monthModel.getList().get(selectedDate.get(Calendar.MONTH)));
		JFormattedTextField ftf = getTextField(monthSpinner);
		if (ftf != null ) {
			ftf.setColumns(8); //specify more width than we need
			ftf.setHorizontalAlignment(JTextField.LEFT);
		}
		if (min == null) {
			lowerRange = selectedDate.get(Calendar.YEAR) - 100;
		} else {
			lowerRange = min.get(Calendar.YEAR);
		}
		if (max == null) {
			upperRange = selectedDate.get(Calendar.YEAR) + 100;
		} else {
			upperRange = max.get(Calendar.YEAR);
		}
		yearModel = new SpinnerNumberModel(
				selectedDate.get(Calendar.YEAR),
				lowerRange, upperRange, 1);
		yearSpinner = new JSpinner(yearModel) {
			public ComponentOrientation getComponentOrientation() {
				return ComponentOrientation.LEFT_TO_RIGHT;
			}
		};
		ftf = getTextField(yearSpinner);
		if (ftf != null ) {
			ftf.setColumns(5);
			ftf.setHorizontalAlignment(JTextField.RIGHT);
		}
		//Make the year be formatted without a thousands separator.
		yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#"));
        monthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int month = monthModel.getList().indexOf(monthSpinner.getValue());
					selectedDate.set(Calendar.MONTH, month);
					setValue();
				}
			}
		});
		yearSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!programmaticChange) {
					int year = ((Integer) yearSpinner.getValue()).intValue();
					selectedDate.set(Calendar.YEAR, year);
					setValue();
				}
			}
		});
		panel.add(monthSpinner);
		panel.add(yearSpinner);
		return panel;
	}

	public JFormattedTextField getTextField(JSpinner spinner) {
		JComponent editor = spinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			return ((JSpinner.DefaultEditor)editor).getTextField();
		} else {
			return null;
		}
	}

	private void changeTableAction(int key) {
		InputMap ancestorMap = table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		InputMap windowMap = table.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = table.getActionMap();
		KeyStroke keyStroke = KeyStroke.getKeyStroke(key, 0);
		Object actionKey = ancestorMap.get(keyStroke);
		windowMap.put(keyStroke, actionKey);
		final Action oldAction = actionMap.get(actionKey);
		Action replacement = new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {}
		};
		switch (key) {
		case KeyEvent.VK_LEFT:
			replacement = new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					boolean allow = true;
					if (min != null && selectedDate.getTimeInMillis() == min.getTimeInMillis()) {
						allow = false;
					}
					if (table.getSelectedColumn() == 0) {
						Calendar newValue = Calendar.getInstance();
						newValue.setTimeInMillis(selectedDate.getTimeInMillis());
						newValue.add(Calendar.DAY_OF_YEAR, -1);
						IPanelMonth.this.firePropertyChange("Value", selectedDate, newValue);
						setValue(newValue);
						allow = false;
					}
					if (allow) {
						oldAction.actionPerformed(ae);
					}
				}
			};
			break;
		case KeyEvent.VK_UP:
			replacement = new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					boolean allow = true;
					if (min != null && selectedDate.getTimeInMillis() - min.getTimeInMillis() < LDates.ONE_DAY * 7) {
						IPanelMonth.this.firePropertyChange("Value", selectedDate, min);
						setValue(min);
						allow = false;
					}
					if (table.getSelectedRow() == 0) {
						Calendar newValue = Calendar.getInstance();
						newValue.setTimeInMillis(selectedDate.getTimeInMillis());
						newValue.add(Calendar.DAY_OF_YEAR, -7);
						IPanelMonth.this.firePropertyChange("Value", selectedDate, newValue);
						setValue(newValue);
						allow = false;
					}
					if (allow) {
						oldAction.actionPerformed(ae);
					}
				}
			};
			break;
		case KeyEvent.VK_RIGHT:
			replacement = new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					boolean allow = true;
					if (max != null && selectedDate.getTimeInMillis() == max.getTimeInMillis()) {
						allow = false;
					}
					if (table.getSelectedColumn() == 6) {
						Calendar newValue = Calendar.getInstance();
						newValue.setTimeInMillis(selectedDate.getTimeInMillis());
						newValue.add(Calendar.DAY_OF_YEAR, 1);
						IPanelMonth.this.firePropertyChange("Value", selectedDate, newValue);
						setValue(newValue);
						allow = false;
					}
					if (allow) {
						oldAction.actionPerformed(ae);
					}
				}
			};
			break;
		case KeyEvent.VK_DOWN:
			replacement = new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					boolean allow = true;
					if (max != null && max.getTimeInMillis() - selectedDate.getTimeInMillis() < LDates.ONE_DAY * 7) {
						IPanelMonth.this.firePropertyChange("Value", selectedDate, max);
						setValue(max);
						allow = false;
					}
					if (table.getSelectedRow() == 5) {
						Calendar newValue = Calendar.getInstance();
						newValue.setTimeInMillis(selectedDate.getTimeInMillis());
						newValue.add(Calendar.DAY_OF_YEAR, 7);
						IPanelMonth.this.firePropertyChange("Value", selectedDate, newValue);
						setValue(newValue);
						allow = false;
					}
					if (allow) {
						oldAction.actionPerformed(ae);
					}
				}
			};
			break;
		case KeyEvent.VK_ENTER:
			replacement = new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					IPanelMonth.this.firePropertyChange("Confirm", null, selectedDate);
				}
			};
			break;
		}
		actionMap.put(actionKey, replacement);
	}

	public Calendar getValue() {
		return selectedDate;
	}

	void setMax(Calendar value) {
		max = value;
	}
	
	void setMin(Calendar value) {
		min = value;
	}
	
	private void setValue() {
		if (min != null && selectedDate.getTimeInMillis() < min.getTimeInMillis()) {
			selectedDate.setTimeInMillis(min.getTimeInMillis());
		}
		if (max != null && selectedDate.getTimeInMillis() > max.getTimeInMillis()) {
			selectedDate.setTimeInMillis(max.getTimeInMillis());
		}
		startDate.setTimeInMillis(selectedDate.getTimeInMillis());
		startDate.set(Calendar.DAY_OF_MONTH, 1);
		startDate.add(Calendar.DAY_OF_YEAR, -(startDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
		setSpinnersSelection();
		setTableSelection();
		repaint();
	}

	void setValue(Calendar newValue) {
		selectedDate.setTimeInMillis(newValue.getTimeInMillis());
		setValue();
	}
	
	private void setSpinnersSelection() {
		if (!programmaticChange) {
			boolean previousChange = programmaticChange;
			programmaticChange = true;
			if (yearModel.getValue() != null) {
				int year = ((Integer) yearModel.getValue()).intValue();
				if (year != selectedDate.get(Calendar.YEAR)) {
					yearModel.setValue(selectedDate.get(Calendar.YEAR));
				}
			}
			if (monthModel.getValue() != null) {
				int month = monthModel.getList().indexOf(monthModel.getValue());
				if (month != selectedDate.get(Calendar.MONTH)) {
					monthModel.setValue(monthModel.getList().get(selectedDate.get(Calendar.MONTH)));
				}
			}
			programmaticChange = previousChange;
		}
	}
	
	private void setTableSelection() {
		boolean previousChange = programmaticChange;
		programmaticChange = true;
		int days = (int)((selectedDate.getTimeInMillis() - startDate.getTimeInMillis()) / LDates.ONE_DAY);
		table.setRowSelectionInterval(days / 7, days / 7);
		table.setColumnSelectionInterval(days % 7, days % 7);
		programmaticChange = previousChange;
	}

	private class MonthViewTableModel<T extends Calendar> extends AbstractTableModel {
		private String[] columns = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};

		public Class<?> getColumnClass(int col) {
			return Calendar.class;
		}

		public String getColumnName(int col) {
	        return columns[col];
		}

		public int getRowCount() {
			return 6;
		}

		public int getColumnCount() {
			return 7;
		}

		public Object getValueAt(int row, int col) {
			Calendar thisDate = Calendar.getInstance();
			thisDate.setTimeInMillis(startDate.getTimeInMillis());
			thisDate.set(Calendar.DAY_OF_YEAR, startDate.get(Calendar.DAY_OF_YEAR) + (row * 7) + col);
			return thisDate;
		}
	}

	private class MonthViewTableHeaderCellRenderer implements TableCellRenderer {
		private final TableCellRenderer defaultRenderer;

		private MonthViewTableHeaderCellRenderer(TableCellRenderer defaultRenderer) {
			this.defaultRenderer = defaultRenderer;
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int col) {
			Component c = defaultRenderer.getTableCellRendererComponent(table, value,
					hasFocus, hasFocus, row, row);
			c.setForeground(col == 0 ? Colors.normalSunday
					: col == 6 ? Colors.normalSaturday
					: Colors.normal);
			return c;
		}
	}

	private class MonthViewTableCellRenderer extends DefaultTableCellRenderer {
		private final SimpleDateFormat formatter = new SimpleDateFormat("d");

		private MonthViewTableCellRenderer() {
			setHorizontalAlignment(RIGHT);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			setText(formatter.format(((Calendar)value).getTime()));
			Calendar dateValue = (Calendar) value;
			boolean isSunday = dateValue.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
			boolean isSaturday = dateValue.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
			if (dateValue.get(Calendar.MONTH) == dateValue.get(Calendar.MONTH)) {
				setForeground(isSunday ? Colors.normalSunday : isSaturday ? Colors.normalSaturday : Colors.normal);
			} else {
				setForeground(isSunday ? Colors.fadedSunday : isSaturday ? Colors.fadedSaturday : Colors.faded);
			}
			hasFocus = true;
			if ((min != null && dateValue.getTimeInMillis() < min.getTimeInMillis())
					|| (max != null && dateValue.getTimeInMillis() > max.getTimeInMillis())) {
				setForeground(Colors.blank);
				isSelected = false;
				hasFocus = false;
			}
			return this;
		}
	}

	private static final class Colors {
		static final Color normal;
		static final Color faded;
		static final Color normalSunday;
		static final Color fadedSunday;
		static final Color normalSaturday;
		static final Color fadedSaturday;
		static final Color blank;
		static final int INCREMENT = 60;

		static {
			JTextField dummy = new JTextField();
			normal = dummy.getForeground();
			faded = dummy.getDisabledTextColor();
			normalSunday = addRed(normal);
			fadedSunday = addRed(faded);
			normalSaturday = addGreen(normal);
			fadedSaturday = addGreen(faded);
			blank = faded.brighter();
		}

		private static Color addRed(Color color) {
			return new Color(increase(color.getRed()),
					decrease(color.getGreen()),
					decrease(color.getBlue()));
		}

		private static Color addGreen(Color color) {
			return new Color(decrease(color.getRed()),
					increase(color.getGreen()),
					decrease(color.getBlue()));
		}

		private static int increase(int i) {
			i += INCREMENT * 2;
			return i > 255 ? 255 : i;
		}

		private static int decrease(int i) {
			i -= INCREMENT;
			return i < 0 ? 0 : i;
		}
	}
}