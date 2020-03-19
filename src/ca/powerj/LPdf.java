package ca.powerj;

import java.util.HashMap;

import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;

class LPdf {
	private HashMap<String, Font> fonts = new HashMap<String, Font>();

	LPdf() {
		Font font = new Font(FontFamily.HELVETICA, 12, Font.ITALIC);
		fonts.put("Font12", font);
		font = new Font(FontFamily.HELVETICA, 8, Font.ITALIC);
		fonts.put("Font8", font);
		font = new Font(FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
		fonts.put("Font10n", font);
		font = new Font(FontFamily.TIMES_ROMAN, 10, Font.BOLD);
		fonts.put("Font10b", font);
	}

	HashMap<String, Font> getFonts() {
		return fonts;
	}
}