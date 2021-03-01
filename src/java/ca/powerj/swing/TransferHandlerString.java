package ca.powerj.swing;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

public class TransferHandlerString extends TransferHandler {
	Position p0 = null, p1 = null;

	public boolean importData(TransferHandler.TransferSupport support) {
		// If we can't handle the import, bail now.
		if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}
		try {
			String data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
			JTextArea source = (JTextArea) support.getComponent();
			Document doc = source.getDocument();
			int offset = source.getCaretPosition();
			doc.insertString(offset, data, null);
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	* Bundle up the data for export.
	*/
	protected Transferable createTransferable(JComponent c) {
		JTextArea source = (JTextArea)c;
		int start = source.getSelectionStart();
		int end = source.getSelectionEnd();
		Document doc = source.getDocument();
		if (start == end) {
			// Nothing selected; Copy/Move all text
			start = 0;
			end = doc.getLength();
			source.setSelectionStart(start);
			source.setSelectionEnd(end);
		}
		if (start < 0) {
			start = 0;
			source.setSelectionStart(start);
		}
		if (end < 0 || end > doc.getLength()) {
			end = doc.getLength();
			source.setSelectionEnd(end);
		}
		if (start > end) {
			// Flip
			int temp = start;
			start = end;
			end = temp;
			source.setSelectionStart(start);
			source.setSelectionEnd(end);
		}
		try {
			p0 = doc.createPosition(start);
			p1 = doc.createPosition(end);
		} catch (BadLocationException e) {
		}
		String data = source.getSelectedText();
		return new StringSelection(data);
	}

	/**
	* These text fields handle both copy and move actions.
	*/
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	/**
	* When the export is complete, remove the old text if the action
	* was a move.
	*/
	protected void exportDone(JComponent c, Transferable data, int action) {
		if (action != MOVE) {
			return;
		}
		if ((p0 != null) && (p1 != null) &&
				(p0.getOffset() != p1.getOffset())) {
			try {
				JTextArea source = (JTextArea)c;
				Document doc = source.getDocument();
				doc.remove(p0.getOffset(), p1.getOffset() - p0.getOffset());
			} catch (BadLocationException e) {
			}
		}
	}
}