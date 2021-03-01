package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
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
import ca.powerj.data.ItemData;
import ca.powerj.data.SpecimenGroupData;
import ca.powerj.data.SpecimenMasterData;
import ca.powerj.data.SpecimenNameData;
import ca.powerj.data.TurnaroundData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IComboBox;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class SpecimenMasterPanel extends BasePanel {
	private final byte SPM_ID = 0;
	private final byte SPM_NAME = 1;
	private final byte SPM_DESCR = 2;
	private final byte SPM_TAT = 3;
	private final byte SPM_SPY = 4;
	private final byte SPM_SUB = 5;
	private final byte SPM_GROUP = 6;
	private final byte SPM_PROC = 7;
	private final byte FILTER_SPY = 0;
	private final byte FILTER_SUB = 1;
	private final byte FILTER_PRO = 2;
	private boolean newCodes = false;
	private int[] filters = { 0, 0, 0 };
	private int rowIndex = 0;
	private final String[] columns = { "NO", "NAME", "DESCR", "TURN", "SPY", "SUB", "GROUP", "PROC" };
	private SpecimenMasterData specimen = new SpecimenMasterData();
	private ArrayList<SpecimenMasterData> specimens = new ArrayList<SpecimenMasterData>();
	private HashMap<Short, SpecimenNameData> names = new HashMap<Short, SpecimenNameData>();
	private ModelSpecimen model;
	private ITable table;
	private JLabel lblName, lblDescr, lblProcedure, lblSpecialty, lblSubspecial;
	private IComboBox cboGroup, cboTAT;

	SpecimenMasterPanel(AppFrame application) {
		super(application);
		setName("Specimens");
		application.dbPowerJ.setStatements(LibConstants.ACTION_SPECMASTER);
		specimens = application.dbPowerJ.getSpecimenMasters();
		createPanel();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		if (super.close()) {
			specimens.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelSpecimen();
		table = new ITable(model, application.dates, application.numbers);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		table.addAncestorListener(new IFocusListener());
		table.addFocusListener(this);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
					setRow(table.convertRowIndexToModel(index));
				}
			}
		});
		table.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IUtilities.createJScrollPane(table);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 300));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		// Read only, cannot be edited
		lblName = IUtilities.createJLabel(SwingConstants.LEFT, 0, "");
		JLabel label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		IUtilities.addComponent(label, 0, 0, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(lblName, 1, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblDescr = IUtilities.createJLabel(SwingConstants.LEFT, 0, "");
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Descr:");
		IUtilities.addComponent(label, 0, 1, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(lblDescr, 1, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		cboTAT = new IComboBox();
		cboTAT.setName("cboTurn");
		cboTAT.setItems(getTurnaround());
		cboTAT.addFocusListener(this);
		cboTAT.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimen.setTurID((byte) cb.getIndex());
						altered = true;
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_T, "Turnaround:");
		label.setLabelFor(cboTAT);
		IUtilities.addComponent(label, 0, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboTAT, 1, 2, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		cboGroup = new IComboBox();
		cboGroup.setName("cboGroup");
		cboGroup.setItems(getGroups());
		cboGroup.addFocusListener(this);
		cboGroup.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimen.setSpgID((short) cb.getIndex());
						lblSpecialty.setText(names.get(specimen.getSpgID()).getSpecialty());
						lblSubspecial.setText(names.get(specimen.getSpgID()).getSubspecial());
						lblProcedure.setText(names.get(specimen.getSpgID()).getProcedure());
						altered = true;
					}
				}
			}
		});
		label = IUtilities.createJLabel(SwingConstants.LEFT, KeyEvent.VK_G, "Group:");
		label.setLabelFor(cboGroup);
		IUtilities.addComponent(label, 0, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IUtilities.addComponent(cboGroup, 1, 3, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Specialty:");
		IUtilities.addComponent(label, 0, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblSpecialty = IUtilities.createJLabel(SwingConstants.LEFT, 0, "      ");
		IUtilities.addComponent(lblSpecialty, 1, 4, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Subspecialty:");
		IUtilities.addComponent(label, 0, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblSubspecial = IUtilities.createJLabel(SwingConstants.LEFT, 0, "      ");
		IUtilities.addComponent(lblSubspecial, 1, 5, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IUtilities.createJLabel(SwingConstants.LEFT, 0, "Procedure:");
		IUtilities.addComponent(label, 0, 6, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblProcedure = IUtilities.createJLabel(SwingConstants.LEFT, 0, "      ");
		IUtilities.addComponent(lblProcedure, 1, 6, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		setLayout(new BorderLayout());
		add(new IToolBar(application), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	private Object[] getGroups() {
		SpecimenNameData name = new SpecimenNameData();
		SpecimenGroupData group = new SpecimenGroupData();
		ArrayList<SpecimenGroupData> tempGroups = application.dbPowerJ.getSpecimenGroups();
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		for (int i = 0; i < tempGroups.size(); i++) {
			group = tempGroups.get(i);
			name = new SpecimenNameData();
			name.setProcedure(group.getProcedure());
			name.setSpecialty(group.getSpecialty());
			name.setSubspecial(group.getSubspecial());
			names.put(group.getGrpID(), name);
			list.add(new ItemData(group.getGrpID(), group.getDescr()));
		}
		return list.toArray();
	}

	private Object[] getTurnaround() {
		ArrayList<ItemData> list = new ArrayList<ItemData>();
		ArrayList<TurnaroundData> tempTurn = new ArrayList<TurnaroundData>();
		for (int i = 0; i < tempTurn.size(); i++) {
			list.add(new ItemData(tempTurn.get(i).getTurID(), tempTurn.get(i).getName()));
		}
		tempTurn.clear();
		return list.toArray();
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("specimenmaster.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1.5f, 5, 1.5f, 1.5f, 1, 4, 1.5f };
		String str = "Specimens Master - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER.rotate(), 36, 18, 18, 18);
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
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case SPM_ID:
						paragraph.add(new Chunk(application.numbers.formatNumber(specimens.get(i).getSpmID())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SPM_NAME:
						paragraph.add(new Chunk(specimens.get(i).getName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_DESCR:
						paragraph.add(new Chunk(specimens.get(i).getDescr()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_TAT:
						paragraph.add(new Chunk(cboTAT.getItemName(specimens.get(i).getTurID())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_SPY:
						paragraph.add(new Chunk(names.get(specimens.get(i).getSpgID()).getSpecialty()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_SUB:
						paragraph.add(new Chunk(names.get(specimens.get(i).getSpgID()).getSubspecial()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_GROUP:
						paragraph.add(new Chunk(cboGroup.getItemName(specimens.get(i).getSpgID())));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(names.get(specimens.get(i).getSpgID()).getProcedure()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
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

	@Override
	void save() {
		if (application.dbPowerJ.setSpecimenMaster(false, specimen) > 0) {
			altered = false;
		}
	}

	@Override
	public void setFilter(short id, int value) {
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		switch (id) {
		case IToolBar.TB_PRO:
			filters[FILTER_PRO] = value;
			break;
		case IToolBar.TB_SPY:
			filters[FILTER_SPY] = value;
			break;
		case IToolBar.TB_SUB:
			filters[FILTER_SUB] = value;
			break;
		default:
			newCodes = !newCodes;
		}
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> rowFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[FILTER_SPY] != 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).getSpyID() == filters[FILTER_SPY]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SUB] != 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).getSubID() == filters[FILTER_SUB]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_PRO] != 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).getProID() == filters[FILTER_PRO]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (newCodes) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (specimens.get(entry.getIdentifier()).getSpgID() == 0);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (rowFilters.size() > 0) {
			// Add to the compound filter
			rowFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(rowFilters);
		}
		TableRowSorter<ModelSpecimen> sorter = (TableRowSorter<ModelSpecimen>) table.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		specimen = specimens.get(rowIndex);
		lblName.setText(specimen.getName());
		lblDescr.setText(specimen.getDescr());
		cboGroup.setIndex(specimen.getSpgID());
		cboTAT.setIndex(specimen.getTurID());
		lblSpecialty.setText(names.get(specimen.getSpgID()).getSpecialty());
		lblSubspecial.setText(names.get(specimen.getSpgID()).getSubspecial());
		lblProcedure.setText(names.get(specimen.getSpgID()).getProcedure());
		programmaticChange = false;
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("specimenmaster.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Specimens Master");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					"Specimens Master - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
			xlsCell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length - 1));
			// header row
			xlsRow = sheet.createRow(1);
			xlsRow.setHeightInPoints(30);
			for (int col = 0; col < columns.length; col++) {
				xlsCell = xlsRow.createCell(col);
				xlsCell.setCellValue(columns[col]);
				xlsCell.setCellStyle(styles.get("header"));
				switch (col) {
				case SPM_ID:
					sheet.setColumnWidth(col, 6 * 256); // 6 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case SPM_DESCR:
				case SPM_GROUP:
					sheet.setColumnWidth(col, 30 * 256); // 30 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case SPM_NAME:
				case SPM_SPY:
				case SPM_PROC:
					sheet.setColumnWidth(col, 10 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case SPM_TAT:
					sheet.setColumnWidth(col, 12 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				default:
					sheet.setColumnWidth(col, 8 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				}
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < table.getRowCount(); row++) {
				i = table.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case SPM_ID:
						xlsCell.setCellValue(specimens.get(i).getSpmID());
						break;
					case SPM_NAME:
						xlsCell.setCellValue(specimens.get(i).getName());
						break;
					case SPM_DESCR:
						xlsCell.setCellValue(specimens.get(i).getDescr());
						break;
					case SPM_TAT:
						xlsCell.setCellValue(cboTAT.getItemName(specimens.get(i).getTurID()));
						break;
					case SPM_SPY:
						xlsCell.setCellValue(names.get(specimen.getSpgID()).getSpecialty());
						break;
					case SPM_SUB:
						xlsCell.setCellValue(names.get(specimen.getSpgID()).getSubspecial());
						break;
					case SPM_GROUP:
						xlsCell.setCellValue(cboGroup.getItemName(specimens.get(i).getSpgID()));
						break;
					default:
						xlsCell.setCellValue(names.get(specimen.getSpgID()).getProcedure());
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

	private class ModelSpecimen extends ITableModel {

		@Override
		public String getColumnName(int col) {
			return getName();
		}

		@Override
		public int getRowCount() {
			return specimens.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (specimens.size() > 0 && row < specimens.size()) {
				value = specimens.get(row).getName();
			}
			return value;
		}
	}
}