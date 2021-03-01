package ca.powerj.swing;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class TransferHandlerHtml extends TransferHandler {
	Position p0 = null, p1 = null;
	private String[] data = new String[2];
	private DataFlavor[] flavors;

	TransferHandlerHtml() {
		flavors = setFlavors();
	}

	public boolean canImport(TransferHandler.TransferSupport support) {
		final DataFlavor[] flavors = support.getDataFlavors();
		for (final DataFlavor flavor : flavors) {
			if (flavor.getRepresentationClass() == String.class
					&& flavor.getMimeType().startsWith("text/html")) {
				return true;
			}
			if (flavor.getRepresentationClass() == String.class
					&& flavor.getMimeType().startsWith("text/plain")) {
				return true;
			}
		}
		return false;
	}

	protected Transferable createTransferable(JComponent c) {
		JTextPane source = (JTextPane)c;
		HTMLDocument htmlDoc = (HTMLDocument) source.getDocument();
		HTMLEditorKit htmlKit = (HTMLEditorKit) source.getEditorKit();
		StringWriter writer = new StringWriter();
		int start = source.getSelectionStart();
		int end = source.getSelectionEnd();
		if (start == end) {
			// Nothing selected; Copy/Move all text
			start = 0;
			end = htmlDoc.getLength();
			source.setSelectionStart(start);
			source.setSelectionEnd(end);
		}
		if (start < 0) {
			start = 0;
			source.setSelectionStart(start);
		}
		if (end < 0 || end > htmlDoc.getLength()) {
			end = htmlDoc.getLength();
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
			// Store location in case this is a cut/move
			p0 = htmlDoc.createPosition(start);
			p1 = htmlDoc.createPosition(end);
			// Get the html text
			htmlKit.write(writer, htmlDoc, start, end);
			data[0] = writer.toString();
			// Get the plain text
			data[1] = source.getSelectedText();
			writer.close();
		} catch (BadLocationException e) {
		} catch (IOException e) {
		}
		return new TransferableHtml(data, flavors);
	}

	public int getSourceActions(JComponent c) {
		return COPY;
	}

	private DataFlavor[] setFlavors() {
		try {
			String[] mimeTypes = {"text/html;", "text/plain;"};
			String[] representationClasses = {"class=java.lang.String",
					"class=java.io.Reader",
					"charset=unicode;class=java.io.InputStream"};
			ArrayList<DataFlavor> availableFlavors = new ArrayList<DataFlavor>();
			for (String m : mimeTypes) {
				for (String r : representationClasses) {
					availableFlavors.add(new DataFlavor(m + r));
				}
			}
			return availableFlavors.toArray(new DataFlavor[availableFlavors.size()]);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}