package ca.powerj.swing;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

public class TransferableHtml implements Transferable {
	private String data[] = null;
	private DataFlavor flavors[] = null;

	public TransferableHtml(String data[], DataFlavor flavors[]){
		this.data = data;
		this.flavors = flavors;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (String.class.equals(flavor.getRepresentationClass())) {
			if (flavor.getMimeType().contains("text/html")){
				return data[0];
			} else if (flavor.getMimeType().contains("text/plain")) {
				return data[1];
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		} else if (Reader.class.equals(flavor.getRepresentationClass())) {
			if (flavor.getMimeType().contains("text/html")){
				return new StringReader(data[0]);
			} else if (flavor.getMimeType().contains("text/plain")) {
				return new StringReader(data[1]);
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		} else if (InputStream.class.equals(flavor.getRepresentationClass())) {
			if (flavor.getMimeType().contains("text/html")){
				return new ByteArrayInputStream(data[0].getBytes());
			} else if (flavor.getMimeType().contains("text/plain")) {
				return new ByteArrayInputStream(data[1].getBytes());
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}
		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}
}