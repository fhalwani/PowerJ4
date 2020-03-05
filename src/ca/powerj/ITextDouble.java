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

class ITextDouble extends JTextField {
	private boolean altered = false;
	private int decimals = 0;
	private double minValue = 0;
    private double maxValue = 0;
	private int minLength = 0;
    private int maxLength = 0;
    private double value = 0;
	private LNumbers numbers;
    private DblVerifier verifier = new DblVerifier();

	ITextDouble(LNumbers numbers, int decimals, double min, double max) {
    	super();
		this.numbers = numbers;
		this.decimals = decimals;
    	this.minValue = min;
    	this.maxValue = max;
    	minLength = String.valueOf(minValue).length();
    	maxLength = String.valueOf(maxValue).length();
    	setToolTipText("(" + numbers.formatDouble(decimals, min) +
    			"-" + numbers.formatDouble(decimals, max) + ")");
    	setColumns(10);
    	setFont(LConstants.APP_FONT);
    	setInputVerifier(verifier);
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

    double getDouble() {
    	return value;
    }

	void setDouble(double d) {
    	value = d;
    	super.setText(numbers.formatDouble(decimals, value));
    	altered = false;
    }

    public void setText(String str) {
    	setDouble(numbers.parseDouble(str));
    }

    class DblVerifier extends InputVerifier implements ActionListener {

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
		// If it is valid, it returns true; otherwise, returns false.
		// If the change argument is true, this method reigns in the
		// value if necessary and (even if not) sets it to the
		// parsed number so that it looks good -- no letters, for example.
		protected boolean checkField(JComponent input) {
			boolean isValid = true;
			Document doc = ((JTextField)input).getDocument();
			int length = doc.getLength();
			double newValue = 0;
			String txtValue;
			try {
				txtValue = doc.getText(0, length).trim();
				length = txtValue.length();
				if (length < minLength) {
					isValid = false;
				} else if (length > maxLength) {
					txtValue = txtValue.substring(0, maxLength);
				}
				if (isValid) {
					newValue = numbers.parseDouble(txtValue);
					if (newValue < minValue) {
						value = minValue;
					} else if (newValue > maxValue) {
						value = maxValue;
					} else {
						value = newValue;
					}
				}
			} catch (BadLocationException e) {
				isValid = false;
			}
			return isValid;
		}
    }
}