package ca.powerj;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

class ITextString extends JTextField {
	private boolean altered = false;
	private int minimum = 0;
    private int maximum = 100;
    private String value = "";
    private TextVerifier verifier = new TextVerifier();

	ITextString(int min, int max) {
    	super();
    	minimum = min;
    	maximum = max;
    	if (maximum > 10) {
        	setColumns(10);
    	} else {
        	setColumns(maximum);
    	}
    	setFont(LConstants.APP_FONT);
    	setInputVerifier(verifier);
    	// Register an action listener to handle Return.
    	addActionListener(verifier);
    	addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				JTextField source = (JTextField)e.getSource();
				source.selectAll();
			}

			public void focusLost(FocusEvent e) {
				JTextField source = (JTextField)e.getSource();
				source.setCaretPosition(0);
				source.setSelectionStart(0);
				source.setSelectionEnd(0);
			}
    	});
    	getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				altered = true;
			}
			public void removeUpdate(DocumentEvent e) {
				altered = true;
			}
			public void changedUpdate(DocumentEvent e) {
				altered = true;
			}
    	});
	}

	boolean altered() {
		return altered;
	}

	public String getText() {
		return value;
	}

    public void setText(String s) {
    	this.value = s;
    	super.setText(s);
    	altered = false;
    }

    class TextVerifier extends InputVerifier implements ActionListener {
    	
		public void actionPerformed(ActionEvent e) {
			JTextField source = (JTextField)e.getSource();
			shouldYieldFocus(source); //ignore return value
		}

        public boolean shouldYieldFocus(JComponent input) {
        	if (!altered) return true;
            boolean inputOK = verify(input);
            if (inputOK) {
                return true;
            } else {
                return false;
            }
        }

        /* Checks whether the JComponent's input is valid. 
         * This method should have no side effects. 
         * It returns a boolean indicating the status of the argument's input. 
         */
		public boolean verify(JComponent input) {
			return checkField(input);
		}
    	
		// Checks that the field is valid.
		// If the change argument is true, this method reigns in the
		// value if necessary and (even if not) sets it to the
		// parsed number so that it looks good -- no letters, for example.
		protected boolean checkField(JComponent input) {
			boolean isValid = true;
			Document doc = ((JTextField)input).getDocument();
			int length = doc.getLength();
			String newValue;
			try {
				newValue = doc.getText(0, length).trim();
				length = newValue.length();
				if (length < minimum) {
					isValid = false;
				} else if (length > maximum) {
					value = newValue.substring(0, maximum);
				} else {
					value = newValue;
				}
			} catch (BadLocationException e) {
				isValid = false;
			}
			return isValid;
		}
    }
}
