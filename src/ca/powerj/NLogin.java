package ca.powerj;
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
import java.sql.PreparedStatement;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

class NLogin extends JDialog implements ActionListener, FocusListener, KeyListener {
	boolean cancel = false;
	private String apLogin     = "";
	private String apPassword  = "";
	private JPasswordField txtPassword = new JPasswordField(15);
	LBase pj;

	NLogin(LBase parent, Frame owner) {
		super(owner, true);
		this.pj = parent;
		createDialog();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("cancel")) {
			cancel = true;
			setVisible(false);
		} else if (e.getActionCommand().equals("ok")) {
			if (apLogin.length() > 2
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
		JLabel lblImage = new JLabel(IGUI.getIcon(48, "institute"));
		IGUI.addComponent(lblImage, 0, 0, 1, 3, 0.5, 0.33,
				GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, panel);
		JLabel lblLicence = IGUI.createJLabel(SwingConstants.CENTER, 0,
				"<html><H1><B><I>" +
				pj.setup.getString(LSetup.VAR_LAB_NAME) +
				"</I></B></H1></html>");
		IGUI.addComponent(lblLicence, 1, 0, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		JLabel lblAppName = IGUI.createJLabel(SwingConstants.CENTER, 0,
				"<html><H2><B><I>" + LConstants.APP_NAME + "</I></B></H2></html>");
		IGUI.addComponent(lblAppName, 1, 1, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		JLabel lblVersion = IGUI.createJLabel(SwingConstants.CENTER, 0,
				"<html><H4><B><I>" + LConstants.APP_VERSION + "</I></B></H4></html>");
		IGUI.addComponent(lblVersion, 1, 2, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		// Add a horizontal line
		IGUI.addComponent(new JSeparator(SwingConstants.HORIZONTAL), 0, 3, 2, 1, 1, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		apLogin = System.getProperty("user.name");
		JTextField txtName = new JTextField(15);
		txtName.setName("Name");
		txtName.setText(apLogin);
		txtName.addFocusListener(this);
		// Escape closes form
		txtName.addKeyListener(this);
		JLabel lblName = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_N, "Name: ");
		lblName.setLabelFor(txtName);
		IGUI.addComponent(lblName, 0, 4, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		IGUI.addComponent(txtName, 1, 4, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		txtPassword.setName("Password");
		txtPassword.addFocusListener(this);
		// Escape closes form
		txtPassword.addKeyListener(this);
		// No copy/paste
		txtPassword.setDragEnabled(false);
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		txtPassword.addAncestorListener(new IFocusListener());
		JLabel lblPassword = IGUI.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_P,
				"Password: ");
		lblPassword.setDisplayedMnemonic(KeyEvent.VK_P);
		IGUI.addComponent(lblPassword, 0, 5, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		lblPassword.setLabelFor(txtPassword);
		IGUI.addComponent(txtPassword, 1, 5, 1, 1, 0.5, 0.1, 
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		JButton btnOkay = IGUI.createJButton(48, KeyEvent.VK_O, "Ok");
		btnOkay.addActionListener(this);
		IGUI.addComponent(btnOkay, 0, 6, 1, 1, 0.5, 0.1, 
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JButton btnCancel = IGUI.createJButton(48, KeyEvent.VK_C, "Cancel");
		btnCancel.addActionListener(this);
		IGUI.addComponent(btnCancel, 1, 6, 1, 1, 0.5, 0.1,
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
					apLogin = ((JTextComponent) c).getText();
				}
			}
		}
	}

	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_ENTER) {
			Component c = e.getComponent();
			if (c instanceof JPasswordField) {
				char[] input = ((JPasswordField) c).getPassword();
				apPassword = String.copyValueOf(input);
			} else {
				apLogin = ((JTextComponent) c).getText();
			}
			if (apLogin.length() > 2
					&& apPassword.length() > 2) {
				cancel = false;
				setVisible(false);
			}
		} else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
			// Escape closes form
			apLogin = "";
			apPassword = "";
			cancel = true;
			setVisible(false);
		}
	}

	/** Check login access to APIS & set userID value **/
	boolean validateLogin() {
		pj.apUser = apLogin;
		pj.apPass = apPassword;
		if (pj.errorID == LConstants.ERROR_NONE) {
			pj.initDBAP();
		}
		if (pj.errorID == LConstants.ERROR_NONE) {
			Hashtable<Byte, PreparedStatement> apStms = pj.dbAP.prepareStatements(LConstants.ACTION_LLOGIN);
			pj.dbAP.setString(apStms.get(DPowerpath.STM_PERS_LOGIN), 1, apLogin);
			pj.userID = pj.dbAP.getShort(apStms.get(DPowerpath.STM_PERS_LOGIN));
			pj.dbAP.close(apStms);
		}
		return (pj.userID > 0);
	}

	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
}