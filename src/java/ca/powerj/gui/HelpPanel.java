package ca.powerj.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import ca.powerj.lib.LibConstants;
import ca.powerj.swing.IUtilities;

class HelpPanel extends JDialog implements WindowListener {
	private Deque<String> stack = new ArrayDeque<String>();
	private JTextPane txtPane;
	AppFrame frame;

	HelpPanel(AppFrame frame) {
		super();
		this.frame = frame;
		setName("Help");
		createAndShowGUI();
	}

	/** Create and set up the main window **/
	private void createAndShowGUI() {
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setLayout(new BorderLayout());
		setResizable(true);
		Image image = IUtilities.getImage(32, "help");
		setIconImage(image);
		Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		txtPane = new JTextPane() {
			// Disable cut/paste
			@Override
			public void cut() {}

			@Override
			public void paste() {}
		};
		txtPane.setContentType("text/html");
		txtPane.setMargin(new Insets(4, 4, 4, 4));
		txtPane.setBorder(border);
		txtPane.setEditable(false);
		txtPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				// Display Linked Form
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					setText(e);
				}
			}
		});
		setStyle();
		JScrollPane scrollPane = new JScrollPane(txtPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(border);
		setText(getFile(frame.panelID));
		JToolBar toolBar = new JToolBar();
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBar.add(IUtilities.createJButton(new ActionBack()));
		add(toolBar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		Point location = new Point(0, 0);
		location.x = frame.defaults.getInt("helpx", location.x);
		location.y = frame.defaults.getInt("helpy", location.y);
		if (location.x > -1 && location.y > -1) {
			setLocation(location);
		}
		Dimension defaultSize = getPreferredSize();
		defaultSize.width = frame.defaults.getInt("helpw", defaultSize.width);
		defaultSize.height = frame.defaults.getInt("helph", defaultSize.height);
		if (defaultSize.width > 50 && defaultSize.height > 50) {
			setPreferredSize(defaultSize);
		}
		pack();
	}

	private String getFile(byte id) {
		String fileName = "";
		switch (id) {
		case LibConstants.ACTION_ACCESSION:
			fileName = "accession.html";
			break;
		case LibConstants.ACTION_BACKLOG:
			fileName = "backlog.html";
			break;
		case LibConstants.ACTION_CODER1:
		case LibConstants.ACTION_CODER2:
		case LibConstants.ACTION_CODER3:
		case LibConstants.ACTION_CODER4:
			fileName = "coder.html";
			break;
		case LibConstants.ACTION_DAILY:
			fileName = "daily.html";
			break;
		case LibConstants.ACTION_DISTRIBUTE:
			fileName = "distro.html";
			break;
		case LibConstants.ACTION_EDITOR:
			fileName = "editor.html";
			break;
		case LibConstants.ACTION_ERROR:
			fileName = "error.html";
			break;
		case LibConstants.ACTION_FACILITY:
			fileName = "facility.html";
			break;
		case LibConstants.ACTION_FINALS:
			fileName = "finals.html";
			break;
		case LibConstants.ACTION_FORECAST:
			fileName = "forecast.html";
			break;
		case LibConstants.ACTION_HISTOLOGY:
			fileName = "histology.html";
			break;
		case LibConstants.ACTION_ORDERGROUP:
			fileName = "ordergroup.html";
			break;
		case LibConstants.ACTION_ORDERMASTER:
			fileName = "ordermaster.html";
			break;
		case LibConstants.ACTION_PENDING:
			fileName = "pending.html";
			break;
		case LibConstants.ACTION_PERSONNEL:
			fileName = "persons.html";
			break;
		case LibConstants.ACTION_PROCEDURES:
			fileName = "procedures.html";
			break;
		case LibConstants.ACTION_ROUTING:
			fileName = "routing.html";
			break;
		case LibConstants.ACTION_RULES:
			fileName = "rules.html";
			break;
		case LibConstants.ACTION_SCHEDULE:
			fileName = "schedule.html";
			break;
		case LibConstants.ACTION_SERVICES:
			fileName = "services.html";
			break;
		case LibConstants.ACTION_SETUP:
			fileName = "setuppj.html";
			break;
		case LibConstants.ACTION_SPECGROUP:
			fileName = "specimengroup.html";
			break;
		case LibConstants.ACTION_SPECIALTY:
			fileName = "specialties.html";
			break;
		case LibConstants.ACTION_SPECIMEN:
			fileName = "specimen.html";
			break;
		case LibConstants.ACTION_SPECMASTER:
			fileName = "specimenmaster.html";
			break;
		case LibConstants.ACTION_SUBSPECIAL:
			fileName = "subspecialty.html";
			break;
		case LibConstants.ACTION_TURNAROUND:
			fileName = "turnaround.html";
			break;
		case LibConstants.ACTION_TURNMASTER:
			fileName = "turnmaster.html";
			break;
		case LibConstants.ACTION_WORKDAYS:
			fileName = "workdays.html";
			break;
		case LibConstants.ACTION_WORKLOAD:
			fileName = "workload.html";
			break;
		default:
			fileName = "about.html";
		}
		return fileName;
	}

	private void goBack() {
		if (stack.size() > 1) {
			stack.removeLast();
			setText(stack.removeLast());
		}
	}

	private void setStyle() {
		try {
			String fileName = "help/" + "screen.css";
			URL url = ClassLoader.getSystemClassLoader().getResource(fileName);
			StyleSheet stylesheet = new StyleSheet();
			if (url != null) {
				stylesheet.importStyleSheet(url);
				HTMLEditorKit kit = (HTMLEditorKit) txtPane.getEditorKitForContentType("text/html");
				kit.setStyleSheet(stylesheet);
				txtPane.setEditorKit(kit);
			}
		} catch (NullPointerException e) {
			frame.log(LibConstants.ERROR_NULL, getName(), e);
		}
	}

	private void setText(HyperlinkEvent hle) {
		try {
			String fileName = "help/" + hle.getDescription();
			URL url = ClassLoader.getSystemClassLoader().getResource(fileName);
			if (url == null) {
				// Outside link
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI(hle.getDescription()));
				}
			} else {
				setText(hle.getDescription());
			}
		} catch (IOException e) {
			frame.log(LibConstants.ERROR_IO, getName(), e);
		} catch (URISyntaxException e) {
			frame.log(LibConstants.ERROR_UNEXPECTED, getName(), e);
		}
	}

	private void setText(String fileName) {
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("help/" + fileName);
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				ir.close();
				is.close();
				txtPane.setText(sb.toString());
				txtPane.setCaretPosition(0);
				setTitle(formatTitle(fileName));
				stack.addLast(fileName);
			}
		} catch (FileNotFoundException e) {
			frame.log(LibConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		} catch (NullPointerException e) {
			frame.log(LibConstants.ERROR_NULL, getName(), e);
		} catch (IOException e) {
			frame.log(LibConstants.ERROR_IO, getName(), e);
		}
	}

	private String formatTitle(String fileName) {
		String title = "";
		if (fileName.equals("accession.html")) {
			title = "Accession";
		} else if (fileName.equals("backlog.html")) {
			title = "Backlog";
		} else if (fileName.equals("coder.html")) {
			title = "Coders";
		} else if (fileName.equals("daily.html")) {
			title = "Daily";
		} else if (fileName.equals("distro.html")) {
			title = "Distribution";
		} else if (fileName.equals("editor.html")) {
			title = "Specimen Editor";
		} else if (fileName.equals("error.html")) {
			title = "Errors";
		} else if (fileName.equals("facility.html")) {
			title = "Facilities";
		} else if (fileName.equals("finals.html")) {
			title = "Final Cases";
		} else if (fileName.equals("forecast.html")) {
			title = "Forecast";
		} else if (fileName.equals("histology.html")) {
			title = "Histology";
		} else if (fileName.equals("ordergroup.html")) {
			title = "Orders Groups";
		} else if (fileName.equals("ordermaster.html")) {
			title = "Orders Master";
		} else if (fileName.equals("pending.html")) {
			title = "Pending Cases";
		} else if (fileName.equals("persons.html")) {
			title = "Persons";
		} else if (fileName.equals("procedures.html")) {
			title = "Procedures";
		} else if (fileName.equals("routing.html")) {
			title = "Routing";
		} else if (fileName.equals("rules.html")) {
			title = "PowerJ Rules";
		} else if (fileName.equals("schedule.html")) {
			title = "Schedules";
		} else if (fileName.equals("services.html")) {
			title = "Services";
		} else if (fileName.equals("setup.html")) {
			title = "PowerJ Setup";
		} else if (fileName.equals("specimengroup.html")) {
			title = "Specimens Groups";
		} else if (fileName.equals("specialties.html")) {
			title = "Specialties";
		} else if (fileName.equals("specimen.html")) {
			title = "Specimens";
		} else if (fileName.equals("specimenmaster.html")) {
			title = "Specimens Master";
		} else if (fileName.equals("subspecialty.html")) {
			title = "Subspecialties";
		} else if (fileName.equals("turnaround.html")) {
			title = "Turnaround Time";
		} else if (fileName.equals("turnmaster.html")) {
			title = "Turnaround Master";
		} else if (fileName.equals(fileName = "workdays.html")) {
			title = "Workdays";
		} else if (fileName.equals("workload.html")) {
			title = "Workloads";
		} else {
			title = "PowerJ";
		}
		return ("Help - " + title);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (isVisible()) {
			Point location = getLocationOnScreen();
			if (location.x >= 0 && location.y >= 0) {
				frame.defaults.setInt("helpx", location.x);
				frame.defaults.setInt("helpy", location.y);
			}
			Dimension size = getSize();
			if (size.width > 50 && size.height > 50) {
				frame.defaults.setInt("helpw", size.width);
				frame.defaults.setInt("helph", size.height);
			}
		}
		stack.clear();
		dispose();
	}

	class ActionBack extends AbstractAction {

		ActionBack() {
			super("Back");
			putValue(SHORT_DESCRIPTION, "Go Back");
			putValue(MNEMONIC_KEY, KeyEvent.VK_B);
			putValue(SMALL_ICON, IUtilities.getIcon(16, "back"));
			putValue(LARGE_ICON_KEY, IUtilities.getIcon(32, "back"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			goBack();
		}
	}

	@Override
	public void windowOpened(WindowEvent ignore) {
	}

	@Override
	public void windowClosed(WindowEvent ignore) {
	}

	@Override
	public void windowIconified(WindowEvent ignore) {
	}

	@Override
	public void windowDeiconified(WindowEvent ignore) {
	}

	@Override
	public void windowActivated(WindowEvent ignore) {
	}

	@Override
	public void windowDeactivated(WindowEvent ignore) {
	}
}
