package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;
import ca.powerj.database.DBPath;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.IUtilities;

class LoginPanel extends JDialog implements ActionListener, FocusListener, KeyListener {
	boolean cancel = false;
	private String apUser = "";
	private String apPassword = "";
	private JPasswordField txtPassword = new JPasswordField(15);
	private AppFrame application;

	LoginPanel(AppFrame application, Frame frame) {
		super(frame, true);
		this.application = application;
		createDialog();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("cancel")) {
			cancel = true;
			setVisible(false);
		} else if (e.getActionCommand().equals("ok")) {
			if (apUser.length() > 2
					&& apPassword.length() > 2) {
				cancel = false;
				setVisible(false);
			}
		}
	}

	/** Create and set up the GUI window **/
	private void createDialog() {
		setName("Login");
		// Make it modal
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		// The calling frame must dispose of window
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		// Remove all window decoration & buttons
		setUndecorated(true);
		setFocusable(true);
		// default BorderLayout used
		setLayout(new BorderLayout());
		// Create a panel within JDialog to manage layout better
		add(createPanel(), BorderLayout.CENTER);
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocationRelativeTo(null);	// for double monitors
		setLocation((screenSize.width / 2) - (this.getWidth() / 2),
				(screenSize.height / 2) - (this.getHeight() / 2));
		setVisible(true);
	}

	/** Create and set up the GUI window **/
	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		// Display image
		JLabel lblImage = new JLabel(IUtilities.getIcon(48, "institute"));
		IUtilities.addComponent(lblImage, 0, 0, 1, 3, 0.5, 0.33,
			GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, panel);
		JLabel lblLicence = IUtilities.createJLabel(SwingConstants.CENTER, 0,
			"<html><H1><B><I>" + application.setup.getString(LibSetup.VAR_LAB_NAME) + "</I></B></H1></html>");
		IUtilities.addComponent(lblLicence, 1, 0, 1, 1, 0.5, 0.1,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		JLabel lblAppName = IUtilities.createJLabel(SwingConstants.CENTER, 0,
			"<html><H2><B><I>" + LibConstants.APP_NAME + "</I></B></H2></html>");
		IUtilities.addComponent(lblAppName, 1, 1, 1, 1, 0.5, 0.1,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		JLabel lblVersion = IUtilities.createJLabel(SwingConstants.CENTER, 0,
			"<html><H4><B><I>" + LibConstants.APP_VERSION + "</I></B></H4></html>");
		IUtilities.addComponent(lblVersion, 1, 2, 1, 1, 0.5, 0.1,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		// Add a horizontal line
		IUtilities.addComponent(new JSeparator(SwingConstants.HORIZONTAL), 0, 3, 2, 1, 1, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		apUser = System.getProperty("user.name");
		JTextField txtName = new JTextField(15);
		txtName.setName("Name");
		txtName.setText(apUser);
		txtName.addFocusListener(this);
		// Escape closes form
		txtName.addKeyListener(this);
		JLabel lblName = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_N, "Name: ");
		lblName.setLabelFor(txtName);
		IUtilities.addComponent(lblName, 0, 4, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IUtilities.addComponent(txtName, 1, 4, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		txtPassword.setName("Password");
		txtPassword.addFocusListener(this);
		// Escape closes form
		txtPassword.addKeyListener(this);
		// No copy/paste
		txtPassword.setDragEnabled(false);
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		txtPassword.addAncestorListener(new IFocusListener());
		JLabel lblPassword = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_P,
				"Password: ");
		lblPassword.setDisplayedMnemonic(KeyEvent.VK_P);
		IUtilities.addComponent(lblPassword, 0, 5, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		lblPassword.setLabelFor(txtPassword);
		IUtilities.addComponent(txtPassword, 1, 5, 1, 1, 0.5, 0.1, 
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		JButton btnOkay = IUtilities.createJButton(48, KeyEvent.VK_O, "Ok");
		btnOkay.addActionListener(this);
		IUtilities.addComponent(btnOkay, 0, 6, 1, 1, 0.5, 0.1, 
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JButton btnCancel = IUtilities.createJButton(48, KeyEvent.VK_C, "Cancel");
		btnCancel.addActionListener(this);
		IUtilities.addComponent(btnCancel, 1, 6, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		return panel;
	}

	public void focusGained(FocusEvent e) {
		// Highlight text
		Component c = e.getComponent();
		if (c != null) {
			if (c instanceof JTextComponent) {
				((JTextComponent) c).setSelectionStart(0);
				((JTextComponent) c).setSelectionEnd(
						((JTextComponent) c).getText().length());
			}
		}
	}

	public void focusLost(FocusEvent e) {
		// De-highlight text
		Component c = e.getComponent();
		if (c != null) {
			if (c instanceof JTextComponent) {
				((JTextComponent) c).setSelectionStart(0);
				((JTextComponent) c).setSelectionEnd(0);
				if (c instanceof JPasswordField) {
					char[] input = ((JPasswordField) c).getPassword();
					apPassword = String.copyValueOf(input);
				} else {
					apUser = ((JTextComponent) c).getText();
				}
			}
		}
	}

	/** Check login access to APIS & set userID value **/
	short getLoginID() {
		short userID = 0;
		application.setProperty("apUser", apUser);
		application.setProperty("apPass", apPassword);
		DBPath dbPathology = application.getDBPath();
		if (application.errorID == LibConstants.ERROR_NONE) {
			userID = dbPathology.getLoginID();
			dbPathology.close();
		}
		return userID;
	}

	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_ENTER) {
			Component c = e.getComponent();
			if (c instanceof JPasswordField) {
				char[] input = ((JPasswordField) c).getPassword();
				apPassword = String.copyValueOf(input);
			} else {
				apUser = ((JTextComponent) c).getText();
			}
			if (apUser.length() > 2
					&& apPassword.length() > 2) {
				cancel = false;
				setVisible(false);
			}
		} else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
			// Escape closes form
			apUser = "";
			apPassword = "";
			cancel = true;
			setVisible(false);
		}
	}

	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
}