package ca.powerj;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

class LExcel {
	private HashMap<String, CellStyle> styles = new HashMap<String, CellStyle>();

	LExcel(Workbook wb) {
		CreationHelper creationHelper = wb.getCreationHelper();
		CellStyle style;
		Font font = wb.createFont();
		font.setFontHeightInPoints((short) 18);
		font.setBold(true);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(font);
		styles.put("title", style);
		font = wb.createFont();
		font.setFontHeightInPoints((short) 11);
		font.setColor(IndexedColors.BLACK.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFont(font);
		style.setWrapText(false);
		setBorders(style);
		styles.put("header", style);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setWrapText(false);
		styles.put("text", style);
		style = wb.createCellStyle();
		style.setWrapText(false);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFillForegroundColor(IndexedColors.RED.getIndex());
		styles.put("textHighlight", style);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setWrapText(false);
		styles.put("left", style);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setWrapText(false);
		style.setDataFormat(creationHelper.createDataFormat().getFormat("#,###"));
		styles.put("data_int", style);
		style = wb.createCellStyle();
		style.setWrapText(false);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0.00"));
		styles.put("data_double", style);
		style = wb.createCellStyle();
		style.setWrapText(false);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0.000"));
		styles.put("data_float", style);
		style = wb.createCellStyle();
		style.setWrapText(false);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		short dateFormat = wb.createDataFormat().getFormat("dd/mm/yyyy;@");
		style.setDataFormat(dateFormat);
		styles.put("date", style);
		style = wb.createCellStyle();
		style.setWrapText(false);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setDataFormat(dateFormat);
		style.setFillForegroundColor(IndexedColors.RED.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put("dateHighlight", style);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setWrapText(false);
		dateFormat = wb.createDataFormat().getFormat("dd/mm/yyyy hh:mm;@");
		style.setDataFormat(dateFormat);
		styles.put("datetime", style);
		style = wb.createCellStyle();
		style.setWrapText(false);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setDataFormat(dateFormat);
		style.setFillForegroundColor(IndexedColors.RED.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put("datetimeHighlight", style);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0"));
		styles.put("formula_int", style);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0.00"));
		styles.put("formula_double", style);
	}

	HashMap<String, CellStyle> getStyles() {
		return styles;
	}

	private void setBorders(CellStyle style) {
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	}
}