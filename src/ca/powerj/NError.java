package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class NError extends NBase {
	private long caseID = 0;
	private final String[] columns = { "NO", "ERROR", "CASE" };
	private ArrayList<OError> lstErrors = new ArrayList<OError>();
	private ITableModelEvent modelEvent;
	private ITableModelSpecimen modelSpec;
	private ModelError modelError;
	private ITable tblEvents;
	private ITable tblSpec;
	private ITable tblError;
	private JTextArea txtComment;

	NError(AClient parent) {
		super(parent);
		setName("Error");
		parent.dbPowerJ.prepareError();
		if (!parent.offLine) {
			pj.dbAP.prepareEditor();
		}
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			modelEvent.close();
			modelSpec.close();
			if (pj.dbAP != null) {
				pj.dbAP.close();
			}
		}
		return !altered;
	}

	private void createPanel() {
		modelError = new ModelError();
		tblError = new ITable(pj, modelError);
		tblError.addAncestorListener(new IFocusListener());
		tblError.addFocusListener(this);
		tblError.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int index = lsm.getMinSelectionIndex();
				if (index > -1) {
					// else, Selection got filtered away.
					setRow(tblError.convertRowIndexToModel(index));
				}
			}
		});
		tblError.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IGUI.createJScrollPane(tblError);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		modelSpec = new ITableModelSpecimen(pj);
		modelSpec.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (!programmaticChange) {
					if (e.getType() == TableModelEvent.UPDATE) {
						save(modelSpec.getMasterID(e.getFirstRow()), modelSpec.getSpecID(e.getFirstRow()));
					}
				}
			}
		});
		tblSpec = new ITable(pj, modelSpec);
		tblSpec.addFocusListener(this);
		IComboBox cboMaster = new IComboBox();
		cboMaster.setName("SpecMaster");
		cboMaster.setModel(pj.dbPowerJ.getSpecimenMaster(false));
		TableColumn column = tblSpec.getColumnModel().getColumn(ITableModelSpecimen.SPEC_CODE);
		column.setCellEditor(new DefaultCellEditor(cboMaster));
		JScrollPane scrollSpec = IGUI.createJScrollPane(tblSpec);
		scrollSpec.setMinimumSize(new Dimension(600, 200));
		modelEvent = new ITableModelEvent(pj);
		tblEvents = new ITable(pj, modelEvent);
		tblEvents.addFocusListener(this);
		JScrollPane scrollEvents = IGUI.createJScrollPane(tblEvents);
		scrollEvents.setMinimumSize(new Dimension(600, 200));
		JSplitPane spltBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		spltBottom.setTopComponent(scrollSpec);
		spltBottom.setBottomComponent(scrollEvents);
		spltBottom.setOneTouchExpandable(true);
		spltBottom.setPreferredSize(new Dimension(700, 500));
		spltBottom.setDividerLocation(250);
		txtComment = new JTextArea();
		txtComment.setEditable(false);
		txtComment.setMargin(new Insets(5, 5, 5, 5));
		txtComment.setFont(LConstants.APP_FONT);
		txtComment.setLineWrap(true);
		txtComment.setWrapStyleWord(true);
		JScrollPane scrollComment = IGUI.createJScrollPane(txtComment);
		scrollComment.setMinimumSize(new Dimension(600, 200));
		JSplitPane spltTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		spltTop.setTopComponent(scrollComment);
		spltTop.setBottomComponent(spltBottom);
		spltTop.setOneTouchExpandable(true);
		spltTop.setPreferredSize(new Dimension(700, 800));
		spltTop.setDividerLocation(250);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(spltTop);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setPreferredSize(new Dimension(1000, 1100));
		pnlSplit.setDividerLocation(200);
		setLayout(new BorderLayout());
		add(pnlSplit, BorderLayout.CENTER);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ERR_SELECT);
		try {
			while (rst.next()) {
				OError error = new OError();
				error.caseID = rst.getLong("CAID");
				error.errID = rst.getByte("ERID");
				error.caseNo = rst.getString("CANO");
				lstErrors.add(error);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
			pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(lstErrors.size()));
		}
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("errors.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1, 2 };
		String str = "Errors - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER, 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable table = new PdfPTable(columns.length);
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			PdfWriter.getInstance(document, fos);
			document.open();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.add(new Chunk(pj.setup.getString(LSetup.VAR_LAB_NAME)));
			document.add(paragraph);
			paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
			document.add(paragraph);
			document.add(Chunk.NEWLINE);
			table.setWidthPercentage(100);
			table.setWidths(widths);
			for (int col = 0; col < columns.length; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(columns[col]));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data rows
			int i = 0;
			for (int row = 0; row < tblError.getRowCount(); row++) {
				i = tblError.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case 0:
						paragraph.add(new Chunk(pj.numbers.formatNumber(row + 1)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 1:
						paragraph.add(new Chunk(pj.numbers.formatNumber(lstErrors.get(i).errID)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					default:
						paragraph.add(new Chunk(lstErrors.get(i).caseNo));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					}
					cell.addElement(paragraph);
					table.addCell(cell);
				}
			}
			document.add(table);
			document.close();
		} catch (DocumentException e) {
			pj.log(LConstants.ERROR_IO, getName(), e);
		} catch (FileNotFoundException e) {
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		}
	}

	private void save(short masterID, long specID) {
		pj.dbAP.setShort(DPowerpath.STM_SPEC_UPDATE, 1, masterID);
		pj.dbAP.setLong(DPowerpath.STM_SPEC_UPDATE, 2, specID);
		if (pj.dbAP.execute(DPowerpath.STM_SPEC_UPDATE) > 0) {
			pj.dbPowerJ.setLong(DPowerJ.STM_ERR_UPDATE, 1, caseID);
			if (pj.dbPowerJ.execute(DPowerJ.STM_ERR_UPDATE) > 0) {
				altered = false;
			}
		}
	}

	private void setRow(int row) {
		programmaticChange = true;
		pj.setBusy(true);
		caseID = lstErrors.get(row).caseID;
		if (!pj.offLine) {
			modelSpec.getData(caseID);
			modelSpec.getData(caseID);
		}
		pj.dbPowerJ.setLong(DPowerJ.STM_ERR_SL_CMT, 1, caseID);
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_ERR_SL_CMT);
		try {
			while (rst.next()) {
				if (rst.getString("ERDC") != null) {
					txtComment.setText(rst.getString("ERDC"));
					txtComment.setCaretPosition(0);
				}
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
		programmaticChange = false;
		pj.setBusy(false);
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("errors.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Errors");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Errors - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length - 1));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col < columns.length - 1; col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellValue(columns[col + 1]);
				xlsCell.setCellStyle(styles.get("header"));
				switch (col) {
				case 0:
				case 1:
					sheet.setColumnWidth(col, 5 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				default:
					sheet.setColumnWidth(col, 13 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				}
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < tblError.getRowCount(); row++) {
				i = tblError.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length - 1; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case 0:
						xlsCell.setCellValue(row + 1);
						break;
					case 1:
						xlsCell.setCellValue(lstErrors.get(i).errID);
						break;
					default:
						xlsCell.setCellValue(lstErrors.get(i).caseNo);
					}
				}
			}
			sheet.createFreezePane(1, 2);
			// Write the output to a file
			FileOutputStream out = new FileOutputStream(fileName);
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			pj.log(LConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		} catch (IOException e) {
			pj.log(LConstants.ERROR_IO, getName(), e);
		} catch (Exception e) {
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), e);
		}
	}

	private class ModelError extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return lstErrors.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			return lstErrors.get(row).caseNo;
		}
	}
}