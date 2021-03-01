package ca.powerj.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibNumbers;

public class ITreeTable extends JTable implements KeyListener {
	protected boolean programaticChange = false;
	protected TreeTableCellRenderer tree;
	private ITreeModel treeTableModel;
	private TreePath treePath;

	public ITreeTable(ITreeModel tm, LibNumbers numbers) {
		super();
		// Create the tree. It will be used as a renderer and editor.
		// First we create a dummy model for the tree and set later the
		// real model with setModel(). This way JTree's TreeModelListener
		// will be called first and we can update our table.
		tree = new TreeTableCellRenderer(new DefaultTreeModel(new DefaultMutableTreeNode()));
		// Install a tableModel representing the visible rows in the tree. 
		setTreeTableModel(tm);
		// Force the JTable and JTree to share their row selection models. 
		ListToTreeSelectionModelWrapper selectionWrapper = new ListToTreeSelectionModelWrapper();
		tree.setSelectionModel(selectionWrapper);
		setSelectionModel(selectionWrapper.getListSelectionModel());
		// Install the tree editor renderer and editor
		setDefaultEditor(ITreeModel.class, new TreeTableCellEditor());
		setDefaultRenderer(ITreeModel.class, tree);
		setDefaultRenderer(Long.class, new IRendererInteger(numbers));
		setDefaultRenderer(Integer.class, new IRendererInteger(numbers));
		setDefaultRenderer(Short.class, new IRendererInteger(numbers));
		setDefaultRenderer(Byte.class, new IRendererInteger(numbers));
		setDefaultRenderer(Double.class, new IRendererDouble(numbers, 2));
		// No grid
		setShowGrid(false);
		// No intercell spacing
		setIntercellSpacing(new Dimension(0, 0));
		// And update the height of the trees row to match that of the table
		setRowHeight(24);
		// Single row selection only
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Autoresize
		setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName() == "rowMargin") {
					tree.intercellSpacing = getIntercellSpacing();
				}
			}
		});
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				if (!programaticChange) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setPath(e.getNewLeadSelectionPath());
						}
					});
				}
			}
		});
		// createDefaultColumnsFromModel();
		addKeyListener(this);
		setFocusable(true);
		InputMap imp2 = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		// copied from TreeView which tried to fix #18292 by doing this
		imp2.put(KeyStroke.getKeyStroke("control C"), "none"); // NOI18N
		imp2.put(KeyStroke.getKeyStroke("control V"), "none"); // NOI18N
		imp2.put(KeyStroke.getKeyStroke("control X"), "none"); // NOI18N
		imp2.put(KeyStroke.getKeyStroke("COPY"), "none"); // NOI18N
		imp2.put(KeyStroke.getKeyStroke("PASTE"), "none"); // NOI18N
		imp2.put(KeyStroke.getKeyStroke("CUT"), "none"); // NOI18N
	}

	/**
	 * Collapses all nodes
	 */    
	public void collapseAll() {
		TreePath tp = new TreePath(tree.getModel().getRoot());
		collapseAllUnder(tp);
		tree.setSelectionPath(tp);
	}

	/**
	 * Collapses all nodes under the specified path
	 */
	public void collapseAllUnder(TreePath tp) {
		Object last = tp.getLastPathComponent();
		for (int i = 0; i < tree.getModel().getChildCount(last); i++) {
			Object child = tree.getModel().getChild(last, i);
			collapseAllUnder(tp.pathByAddingChild(child));
		}
		tree.collapsePath(tp);
		tree.setSelectionPath(tp);
	}

	/**
	 * Collapses selected node under the specified path
	 */
	public void collapsePath(TreePath tp) {
		tree.collapsePath(tp);
		tree.setSelectionPath(tp);
	}

	/**
	 * Expands all nodes
	 */
	public void expandAll() {
		TreePath tp = new TreePath(tree.getModel().getRoot());
		expandAllUnder(tp);
		tree.setSelectionPath(tp);
	}

	/**
	 * Expands all nodes under the specified path
	 */
	public void expandAllUnder(TreePath tp) {
		tree.expandPath(tp);
		Object last = tp.getLastPathComponent();
		for (int i = 0; i < tree.getModel().getChildCount(last); i++) {
			Object child = tree.getModel().getChild(last, i);
			expandAllUnder(tp.pathByAddingChild(child));
		}
		tree.setSelectionPath(tp);
	}

	/**
	 * Expands selected node under the specified path
	 */
	public void expandPath(TreePath tp) {
		tree.expandPath(tp);
		tree.setSelectionPath(tp);
	}

	/* Workaround for BasicTableUI anomaly. Make sure the UI never tries to 
	 * paint the editor. The UI currently uses different techniques to 
	 * paint the renderers and editors and overriding setBounds() below 
	 * is not the right thing to do for an editor. Returning -1 for the 
	 * editing row in this case, ensures the editor is never painted. 
	 */
	public int getEditingRow() {
		return (getColumnClass(editingColumn) == ITreeModel.class) ? -1 : editingRow;  
	}

	/**
	 * Returns the object for the specified row
	 */
	public Object getNodeForRow(int row) {
		TreePath tp = tree.getPathForRow(row);
		return tp.getLastPathComponent();
	}

	/**
	 * Returns the row corresponding to the specified path
	 */
	public int getRowForPath(TreePath path) {
		return tree.getRowForPath(path);
	}

	/**
	 * Returns the tree that is being shared between the model.
	 */
	public JTree getTree() {
		return tree;
	}

	/**
	 * Returns the current model
	 */
	public ITreeModel getTreeTableModel() {
		return treeTableModel;
	}

	public boolean isBusy() {
		return programaticChange;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (treePath == null) return;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ADD: // 107
		case KeyEvent.VK_PLUS: // 521
		case KeyEvent.VK_RIGHT:
			programaticChange = true;
			if (e.isControlDown()) {
				expandAll();
			} else if (e.isAltDown()) {
				expandAllUnder(treePath);
			} else {
				expandPath(treePath);
			}
			programaticChange = false;
			break;
		case KeyEvent.VK_SUBTRACT: // 109
		case KeyEvent.VK_MINUS: // 45
		case KeyEvent.VK_LEFT:
			programaticChange = true;
			if (e.isControlDown()) {
				collapseAll();
			} else if (e.isAltDown()) {
				collapseAllUnder(treePath);
			} else {
				collapsePath(treePath);
			}
			programaticChange = false;
			break;
		default:
			// Ignore rest
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	private void setPath(TreePath newPath) {
		treePath = newPath;
	}

	/**
	 * Overridden to pass the new rowHeight to the tree.
	 */
	public void setRowHeight(int rowHeight) { 
		super.setRowHeight(rowHeight); 
		if (tree != null && tree.getRowHeight() != rowHeight) {
			tree.setRowHeight(getRowHeight()); 
		}
	}

	/**
	 * Sets new TreeTableModel
	 */
	public void setTreeTableModel(ITreeModel treeTableModel) {
		this.treeTableModel = treeTableModel;
		setModel(new ITreeAdapter(treeTableModel, tree));
		tree.setModel(treeTableModel);
	}

	/**
	 * Overridden to message super and forward the method to the tree.
	 * Since the tree is not actually in the component hierarchy it will
	 * never receive this unless we forward it in this manner.
	 */
	public void updateUI() {
		super.updateUI();
		if (tree != null) {
			tree.updateUI();
		}
		// Use the tree's default foreground and background colors in the table 
		LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
	}

	/**
	 * A TreeCellRenderer that displays a JTree.
	 */
	public class TreeTableCellRenderer extends JTree implements TableCellRenderer {
		private static final long serialVersionUID = 1;
		/** Last table/tree row asked to renderer. */
		protected int visibleRow;
		private Border border;
		private Dimension intercellSpacing = new Dimension(1, 1);
		private final Border borderEmpty = BorderFactory.createEmptyBorder(2, 5, 2, 5);

		public TreeTableCellRenderer(TreeModel model) {
			super(model);
			setFont(LibConstants.APP_FONT);
			setOpaque(true);
		}

		/**
		 * updateUI is overridden to set the colors of the Tree's renderer
		 * to match that of the table.
		 */
		public void updateUI() {
			super.updateUI();
			// Make the tree's cell renderer use the table's cell selection colors 
			TreeCellRenderer tcr = getCellRenderer();
			if (tcr instanceof DefaultTreeCellRenderer) {
				DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr);
				//dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
				dtcr.setBackgroundSelectionColor(LibConstants.COLOR_LIGHT_BLUE);
			}
		}

		/**
		 * Sets the row height of the tree, and forwards the row height to
		 * the table.
		 */
		public void setRowHeight(int rowHeight) {
			if (rowHeight > 0) {
				super.setRowHeight(rowHeight);
				if (ITreeTable.this != null &&ITreeTable.this.getRowHeight() != rowHeight) {
					ITreeTable.this.setRowHeight(getRowHeight());
				}
			}
		}

		/**
		 * This is overridden to set the height to match that of the JTable.
		 */
		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, 0, w, ITreeTable.this.getHeight());
		}

		/**
		 * Subclassed to translate the graphics such that the last visible
		 * row will be drawn at 0,0.
		 */
		public void paint(Graphics g) {
			Rectangle oldClip = g.getClipBounds();
			Rectangle clip;
			clip = oldClip.intersection(
					new Rectangle(0, 0, 
							getWidth() - intercellSpacing.width, 
							getRowHeight() - intercellSpacing.height));
			g.setClip(clip);
			g.translate(0, -visibleRow * getRowHeight());
			super.paint(g);
			g.translate(0, visibleRow * getRowHeight());
			g.setClip(oldClip);
			if (border != null)
				border.paintBorder(this, g, 0, 0, getWidth(), getRowHeight());
		}

		/**
		 * TreeCellRenderer method. Overridden to update the visible row.
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				border = borderEmpty;
	            setBackground(LibConstants.COLOR_LIGHT_BLUE);
	        } else {
	        	border = null;
	        	setBackground(LibConstants.COLOR_EVEN_ODD[row % 2]);
	        }
			visibleRow = row;
			return this;
		}
	}


	/**
	 * TreeTableCellEditor implementation. Component returned is the
	 * JTree.
	 */
	public class TreeTableCellEditor extends ICellEditor implements TableCellEditor {
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int r, int c) {
			return tree;
		}

		/**
		 * Overridden to return false, and if the event is a mouse event
		 * it is forwarded to the tree.
		 * The behavior for this is debatable, and should really be offered
		 * as a property. By returning false, all keyboard actions are
		 * implemented in terms of the table. By returning true, the
		 * tree would get a chance to do something with the keyboard
		 * events. For the most part this is ok. But for certain keys,
		 * such as left/right, the tree will expand/collapse where as
		 * the table focus should really move to a different column. Page
		 * up/down should also be implemented in terms of the table.
		 * By returning false this also has the added benefit that clicking
		 * outside of the bounds of the tree node, but still in the tree
		 * column will select the row, whereas if this returned true
		 * that wouldn't be the case.
		 * By returning false we are also enforcing the policy that
		 * the tree will never be editable (at least by a key sequence).
		 */
		public boolean isCellEditable(EventObject e) {
			if (e instanceof MouseEvent) {
				for (int counter = getColumnCount() - 1; counter >= 0;
						counter--) {
					if (getColumnClass(counter) == ITreeModel.class) {
						MouseEvent me = (MouseEvent)e;
						MouseEvent newME = new MouseEvent(tree, me.getID(),
								me.getWhen(), me.getModifiersEx(),
								me.getX() - getCellRect(0, counter, true).x,
								me.getY(), me.getClickCount(),
								me.isPopupTrigger());
						tree.dispatchEvent(newME);
						break;
					}
				}
			}
			return false;
		}
	}


	/**
	 * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
	 * to listen for changes in the ListSelectionModel it maintains. Once
	 * a change in the ListSelectionModel happens, the paths are updated
	 * in the DefaultTreeSelectionModel.
	 */
	class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {
		private static final long serialVersionUID = 1;
		/** Set to true when we are updating the ListSelectionModel. */
		protected boolean updatingListSelectionModel;

		public ListToTreeSelectionModelWrapper() {
			super();
			getListSelectionModel().addListSelectionListener
			(createListSelectionListener());
		}

		/**
		 * Returns the list selection model. ListToTreeSelectionModelWrapper
		 * listens for changes to this model and updates the selected paths
		 * accordingly.
		 */
		ListSelectionModel getListSelectionModel() {
			return listSelectionModel;
		}

		/**
		 * This is overridden to set updatingListSelectionModel
		 * and message super. This is the only place DefaultTreeSelectionModel
		 * alters the ListSelectionModel.
		 */
		public void resetRowSelection() {
			if(!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				try {
					super.resetRowSelection();
				}
				finally {
					updatingListSelectionModel = false;
				}
			}
			// Notice how we don't message super if
			// updatingListSelectionModel is true. If
			// updatingListSelectionModel is true, it implies the
			// ListSelectionModel has already been updated and the
			// paths are the only thing that needs to be updated.
		}

		/**
		 * Creates and returns an instance of ListSelectionHandler.
		 */
		protected ListSelectionListener createListSelectionListener() {
			return new ListSelectionHandler();
		}

		/**
		 * If updatingListSelectionModel is false, this will
		 * reset the selected paths from the selected rows in the list
		 * selection model.
		 */
		protected void updateSelectedPathsFromSelectedRows() {
			if(!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				try {
					// This is way expensive, ListSelectionModel needs an
					// enumerator for iterating.
					int min = listSelectionModel.getMinSelectionIndex();
					int max = listSelectionModel.getMaxSelectionIndex();
					clearSelection();
					if(min != -1 && max != -1) {
						for(int counter = min; counter <= max; counter++) {
							if(listSelectionModel.isSelectedIndex(counter)) {
								TreePath selPath = tree.getPathForRow(counter);
								if(selPath != null) {
									addSelectionPath(selPath);
								}
							}
						}
					}
				} finally {
					updatingListSelectionModel = false;
				}
			}
		}

		/**
		 * Class responsible for calling updateSelectedPathsFromSelectedRows
		 * when the selection of the list changes.
		 */
		class ListSelectionHandler implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent e) {
				updateSelectedPathsFromSelectedRows();
			}
		}
	}
}