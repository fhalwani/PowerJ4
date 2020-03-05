package ca.powerj;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

class ITree extends JTree implements KeyListener {

	public ITree(TreeModel treeModel) {
		super(treeModel);
		setFont(LConstants.APP_FONT);
		setRowHeight(24);
		setEditable(false);
		setShowsRootHandles(true);
		// Render background color in alternating rows
		setCellRenderer(new ITreeRender());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	/**
	* Collapses all nodes
	*/    
	public void collapseAll() {
		TreePath tp = new TreePath(getModel().getRoot());
		collapseAllUnder(tp);
		setSelectionPath(tp);
	}

	/**
	* Collapses all nodes under the specified path
	*/
	public void collapseAllUnder(TreePath tp) {
		Object last = tp.getLastPathComponent();
		for (int i = 0; i < getModel().getChildCount(last); i++) {
			Object child = getModel().getChild(last, i);
			collapseAllUnder(tp.pathByAddingChild(child));
		}
		collapsePath(tp);
		setSelectionPath(tp);
	}

	/**
	* Expands all nodes
	*/    
	public void expandAll() {
		TreePath tp = new TreePath(getModel().getRoot());
		expandAllUnder(tp);
		setSelectionPath(tp);
	}

	/**
	* Expands all nodes under the specified path
	*/
	public void expandAllUnder(TreePath tp) {
		expandPath(tp);
		Object last = tp.getLastPathComponent();
		for (int i = 0; i < getModel().getChildCount(last); i++) {
			Object child = getModel().getChild(last, i);
			expandAllUnder(tp.pathByAddingChild(child));
		}
		setSelectionPath(tp);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		TreePath treePath = getSelectionPath();
		if (treePath == null) return;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ADD: // 107
		case KeyEvent.VK_PLUS: // 521
			if (e.isControlDown()) {
				expandAll();
			} else if (e.isAltDown()) {
				expandAllUnder(treePath);
			} else {
				expandPath(treePath);
			}
			break;
		case KeyEvent.VK_SUBTRACT: // 109
		case KeyEvent.VK_MINUS: // 45
			if (e.isControlDown()) {
				collapseAll();
			} else if (e.isAltDown()) {
				collapseAllUnder(treePath);
			} else {
				collapsePath(treePath);
			}
			break;
		default:
			// Ignore rest
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}
	@Override
	public void keyTyped(KeyEvent arg0) {}
}