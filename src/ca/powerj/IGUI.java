package ca.powerj;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.net.URL;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

class IGUI {

	static void addComponent(Component comp,
			int gridx, int gridy, int gridwidth, int gridheight,
			double weightx, double weighty, int fill, int anchor,
			JPanel pnl) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = fill;
		gbc.anchor = anchor;
		pnl.add(comp, gbc);
	}

	static JLabel createJLabel(int horizontalAlignment, int mnemonic, String text) {
		JLabel lbl = new JLabel(text);
		lbl.setFont(LConstants.APP_FONT);
		lbl.setHorizontalAlignment(horizontalAlignment);
		if (mnemonic != 0) {
			lbl.setDisplayedMnemonic(mnemonic);
		}
		return lbl;
	}

	static JButton createJButton(Action action) {
		JButton btn = new JButton(action);
		btn.setText(null);
		btn.setFocusable(true);
		return btn;
	}

	static JButton createJButton(int size, int mnemonic, String text) {
		JButton btn = new JButton(text);
		btn.setIcon(getIcon(size, text.toLowerCase()));
		btn.setActionCommand(text.toLowerCase());
		btn.setFocusable(true);
		if (mnemonic == 0) {
			btn.setText(null);
		} else {
			btn.setMnemonic(mnemonic);
		}
		return btn;
	}

	static JScrollPane createJScrollPane(Component c) {
		JScrollPane scroll = new JScrollPane(c,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		scroll.setColumnHeader(new JViewport() {
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		return scroll;
	}

	/** Returns an ImageIcon, or null if the path is invalid. */
	static ImageIcon getIcon(int size, String name) {
		ImageIcon icon = new ImageIcon();
		String location = "icons/" + size + "/";
		String path = location + name.toLowerCase() + ".png";
		URL url = ClassLoader.getSystemClassLoader().getResource(path);
		if (url != null) {
			icon = new ImageIcon(url);
		} else {
			path = location + "default.png";
			url = ClassLoader.getSystemClassLoader().getResource(path);
			if (url != null) {
				icon = new ImageIcon(url);
			}
		}
		return icon;
	}

	/** Returns an Image from an Icon. */
	static Image getImage(int size, String name) {
		return getIcon(size, name).getImage();
	}
}