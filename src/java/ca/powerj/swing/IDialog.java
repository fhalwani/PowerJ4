package ca.powerj.swing;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import ca.powerj.lib.LibConstants;

public class IDialog {
	private int choice = -1;

	public IDialog(Frame frame, int type, String name, String message,
		String[] choices, int[] mnemonics, int defaultChoice) {
		setDialog(frame, type, name, message, choices, mnemonics, defaultChoice);
	}

	public IDialog(Frame frame, int type, String name, String message) {
		String[] choices = {"Ok"};
		int[] mnemonics = {KeyEvent.VK_O};
		setDialog(frame, type, name, message, choices, mnemonics, 0);
	}

	public IDialog(Frame frame, String name) {
		String message = String.format("Save changes to %s before closing?", name);
		String[] choices = {"Save", "Ignore", "Cancel"};
		int[] mnemonics = {KeyEvent.VK_S, KeyEvent.VK_I, KeyEvent.VK_C};
		setDialog(frame, JOptionPane.QUESTION_MESSAGE, name, message,
				choices, mnemonics, 0);
	}

	private void setDialog(Frame frame, int type, String name, String message,
			String[] choices, int[] mnemonics, int defaultChoice) {
		if (name == null) name = LibConstants.APP_NAME;
		String prefix = "";
		Icon icon = null;
		switch (type) {
		case JOptionPane.ERROR_MESSAGE:
			prefix = "Error:";
			icon = IUtilities.getIcon(48, "error");
			break;
		case JOptionPane.INFORMATION_MESSAGE:
			prefix = "Info:";
			icon = IUtilities.getIcon(48, "information");
			break;
		case JOptionPane.WARNING_MESSAGE:
			prefix = "Warning:";
			icon = IUtilities.getIcon(48, "warning");
			break;
		case JOptionPane.QUESTION_MESSAGE:
			prefix = "Question:";
			icon = IUtilities.getIcon(48, "question");
			break;
		default:
			prefix = "About " + LibConstants.APP_NAME;
			icon = IUtilities.getIcon(48, "gplv3");
		}
		Border padding = BorderFactory.createEmptyBorder(10,10,10,10);
		JTextPane txtMessage = new JTextPane();
		txtMessage.setBorder(padding);
		txtMessage.setEditable(false);
		txtMessage.setFocusable(false);
		txtMessage.setOpaque(false);
		txtMessage.setContentType("text/html");
		txtMessage.setFont(LibConstants.APP_FONT);
		txtMessage.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent hle) {
				// Display Linked Form
				if (hle.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if(Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI(hle.getDescription()));
						} catch (IOException ignore) {
						} catch (URISyntaxException ignore) {
						}
					}
				}
			}
		});
		prefix = "<h1>" + prefix + "</h1>";
		String s = "";
		if (message == null) {
			s = setMessage();
		} else {
			String[] data = message.split("\n");
			s = "<div>" + data[0].trim() + "</div>";
			for (int i = 1; i < data.length; i++) {
				if (data[i].toLowerCase().contains("at ca.powerj")) {
					s = s + "<div>" + data[i] + "</div>";
				}
			}
		}
		message = prefix + s;
		HTMLDocument doc = (HTMLDocument) txtMessage.getDocument();
		HTMLEditorKit kit = (HTMLEditorKit) txtMessage.getEditorKit();
		StyleSheet styles = kit.getStyleSheet();
		styles.addRule("a {color:red}"); //change links to red
		styles.addRule("body {text-align:center}");
		try {
			kit.insertHTML(doc, doc.getLength(), message, 0, 0, null);
		} catch (BadLocationException ignore) {
		} catch (IOException ignore) {
		}
		txtMessage.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getViewport().setView(txtMessage);
		final JOptionPane pane = new JOptionPane(scrollPane);
		pane.setIcon(icon);
		JButton buttons[] = new JButton[choices.length];
		for (int i = 0; i < choices.length; i++) {
			buttons[i] = IUtilities.createJButton(48, mnemonics[i], choices[i]);
			buttons[i].setText(choices[i]);
			buttons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pane.setValue((JButton) e.getSource());
				}
			});
		}
		pane.setOptions(buttons);
		pane.setInitialValue(buttons[defaultChoice]);
		buttons[defaultChoice].requestFocusInWindow();
		buttons[defaultChoice].setSelected(true);
		JDialog dialog = pane.createDialog(frame, name);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// set the default button for <Enter> key
		dialog.getRootPane().setDefaultButton(buttons[defaultChoice]);
		dialog.setIconImage(IUtilities.getImage(48, LibConstants.APP_NAME));
		dialog.setResizable(true);
		// Center it on screen
		dialog.pack();
		if (dialog.getWidth() > 800) {
			Dimension dim = new Dimension(dialog.getWidth(), dialog.getHeight());
			dim.width = 800;
			dim.height += 200;
			dialog.setSize(dim);
		}
		dialog.setVisible(true);
		Object selectedValue = pane.getValue();
		dialog.dispose();
		if (selectedValue != null) {
			// If there is an array of option buttons:
			for (int counter = 0; counter < buttons.length; counter++) {
				if (buttons[counter].equals(selectedValue)) {
					choice = counter;
				}
			}
		}
	}
	
	private String setMessage() {
		return "<div>PowerJ is a data-mining program for PowerPath " +
			"Anatomical Pathology System</div><div>PowerJ is free " +
			"software: you can redistribute it and/or modify it under the terms of the " +
			"GNU General Public License as published by the Free Software Foundation.</div><div>" +
			"PowerJ is distributed in the hope that it will be useful, " +
			"but WITHOUT ANY WARRANTY; without even the implied warranty of " +
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " +
			"GNU General Public License for more details.</div>" +
			"<div>To review the licence terms, see <a HREF=\"http://www.gnu.org/licenses/\">" +
			"www.gnu.org/licenses/</a></div><div>Copyright: Dr. Fawaz Halwani " +
			"(<a HREF=\"mailto:fhalwani@gmail.com\">fhalwani@gmail.com</a>)</div>" +
			"<div>Free download from <a HREF=\"http://github.com/fhalwani/PowerJ\">GitHub.com</a></div>";
	}

	public int getChoice() {
		return choice;
	}
}