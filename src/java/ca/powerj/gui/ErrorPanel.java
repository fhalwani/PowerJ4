package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
import ca.powerj.data.ErrorData;
import ca.powerj.data.ItemData;
import ca.powerj.data.SpecimenMasterData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IComboEditor;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.ITableModelEvent;
import ca.powerj.swing.ITableModelSpecimen;
import ca.powerj.swing.IUtilities;

class ErrorPanel extends BasePanel {
	private long caseID = 0;
	private final String[] columns = { "NO", "ERROR", "CASE" };
	private ArrayList<ErrorData> list = new ArrayList<ErrorData>();
	private ITableModelEvent modelEvent;
	private ITableModelSpecimen modelSpec;
	private ModelError modelError;
	private ITable tableEvents;
	private ITable tableSpecs;
	private ITable tableErrors;
	private JTextArea txtComment;

	ErrorPanel(AppFrame application) {
		super(application);
		setName("Error");
		application.dbPowerJ.setStatements(LibConstants.ACTION_ERROR);
		if (!application.isOffline()) {
			application.dbPath.setStatements(LibConstants.ACTION_EDITOR);
		}
		list = application.dbPowerJ.getErrorPending();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			modelEvent.close();
			modelSpec.close();
			if (application.dbPath != null) {
				application.dbPath.closeStms();
			}
		}
		return !altered;
	}

	private void createPanel() {
		modelError = new ModelError();
		tableErrors = new ITable(modelError, application.dates, application.numbers);
		tableErrors.addAncestorListener(new IFocusListener());
		tableErrors.addFocusListener(this);
		tableErrors.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
					setRow(tableErrors.convertRowIndexToModel(index));
				}
			}
		});
		tableErrors.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IUtilities.createJScrollPane(tableErrors);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		modelSpec = new ITableModelSpecimen();
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
		tableSpecs = new ITable(modelSpec, application.dates, application.numbers);
		tableSpecs.addFocusListener(this);
		IComboBox cboMaster = new IComboBox();
		cboMaster.setName("SpecMaster");
		cboMaster.setItems(getSpecimens());
		TableColumn column = tableSpecs.getColumnModel().getColumn(ITableModelSpecimen.SPEC_CODE);
		column.setCellEditor(new IComboEditor(cboMaster));
		JScrollPane scrollSpec = IUtilities.createJScrollPane(tableSpecs);
		scrollSpec.setMinimumSize(new Dimension(600, 200));
		modelEvent = new ITableModelEvent();
		tableEvents = new ITable(modelEvent, application.dates, application.numbers);
		tableEvents.addFocusListener(this);
		JScrollPane scrollEvents = IUtilities.createJScrollPane(tableEvents);
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
		txtComment.setFont(LibConstants.APP_FONT);
		txtComment.setLineWrap(true);
		txtComment.setWrapStyleWord(true);
		JScrollPane scrollComment = IUtilities.createJScrollPane(txtComment);
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

	private Object[] getSpecimens() {
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		ArrayList<SpecimenMasterData> temp = application.dbPowerJ.getSpecimenMasters();
		for (int i = 0; i < temp.size(); i++) {
			list.add(new ItemData(temp.get(i).getSpmID(), temp.get(i).getName()));
		}
		return list.toArray();
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("errors.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1, 2 };
		String str = "Errors - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER, 36, 18, 18, 18);
		Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		PdfPTable pdfTable = new PdfPTable(columns.length);
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			PdfWriter.getInstance(document, fos);
			document.open();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.add(new Chunk(application.setup.getString(LibSetup.VAR_LAB_NAME)));
			document.add(paragraph);
			paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
			document.add(paragraph);
			document.add(Chunk.NEWLINE);
			pdfTable.setWidthPercentage(100);
			pdfTable.setWidths(widths);
			for (int col = 0; col < columns.length; col++) {
				paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(columns[col]));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				pdfTable.addCell(cell);
			}
			pdfTable.setHeaderRows(1);
			// data rows
			int i = 0;
			for (int row = 0; row < tableErrors.getRowCount(); row++) {
				i = tableErrors.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case 0:
						paragraph.add(new Chunk(application.numbers.formatNumber(row + 1)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case 1:
						paragraph.add(new Chunk(application.numbers.formatNumber(list.get(i).getErrID())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					default:
						paragraph.add(new Chunk(list.get(i).getCaseNo()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					}
					cell.addElement(paragraph);
					pdfTable.addCell(cell);
				}
			}
			document.add(pdfTable);
			document.close();
		} catch (DocumentException e) {
			application.log(LibConstants.ERROR_IO, getName(), e);
		} catch (FileNotFoundException e) {
			application.log(LibConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		}
	}

	private void save(short masterID, long specID) {
		if (application.dbPath.setSpecimenID(masterID, specID) > 0) {
			if (application.dbPowerJ.setErrorFixed(caseID) > 0) {
				altered = false;
			}
		}
	}

	private void setRow(int row) {
		programmaticChange = true;
		application.setBusy(true);
		caseID = list.get(row).getCaseID();
		if (!application.isOffline()) {
			modelEvent.setData(application.dbPath.getCaseEvents(caseID));
			modelSpec.setData(application.dbPath.getCaseSpecimens(caseID));
		}
		txtComment.setText(application.dbPowerJ.getErrorComment(caseID));
		txtComment.setCaretPosition(0);
		programmaticChange = false;
		application.setBusy(false);
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("errors.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Errors");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Errors - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
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
			for (int row = 0; row < tableErrors.getRowCount(); row++) {
				i = tableErrors.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length - 1; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case 0:
						xlsCell.setCellValue(row + 1);
						break;
					case 1:
						xlsCell.setCellValue(list.get(i).getErrID());
						break;
					default:
						xlsCell.setCellValue(list.get(i).getCaseNo());
					}
				}
			}
			sheet.createFreezePane(1, 2);
			// Write the output to a file
			FileOutputStream out = new FileOutputStream(fileName);
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			application.log(LibConstants.ERROR_FILE_NOT_FOUND, getName(), e);
		} catch (IOException e) {
			application.log(LibConstants.ERROR_IO, getName(), e);
		} catch (Exception e) {
			application.log(LibConstants.ERROR_UNEXPECTED, getName(), e);
		}
	}

	private class ModelError extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (list.size() > 0 && row < list.size()) {
				value = list.get(row).getCaseNo();
			}
			return value;
		}
	}
}