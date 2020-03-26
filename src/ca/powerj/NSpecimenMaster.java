package ca.powerj;

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
import java.sql.ResultSet;
import java.sql.SQLException;
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

class NSpecimenMaster extends NBase {
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
	private short[] filters = { 0, 0, 0 };
	private int rowIndex = 0;
	private final String[] columns = { "NO", "NAME", "DESCR", "TURN", "SPY", "SUB", "GROUP", "PROC" };
	private OSpecMaster specimenmaster = new OSpecMaster();
	private ArrayList<OSpecMaster> list = new ArrayList<OSpecMaster>();
	private HashMap<Short, OSpecDescr> specgroups = new HashMap<Short, OSpecDescr>();
	private ModelSpecMstr model;
	private ITable tbl;
	private JLabel lblName, lblDescr, lblProcedure, lblSpecialty, lblSubspecial;
	private IComboBox cboGroup, cboTAT;

	NSpecimenMaster(AClient parent) {
		super(parent);
		setName("Specimens");
		parent.dbPowerJ.prepareStpSpeMstr();
		getData();
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		if (super.close()) {
			list.clear();
		}
		return !altered;
	}

	private void createPanel() {
		model = new ModelSpecMstr();
		tbl = new ITable(pj, model);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tbl.addAncestorListener(new IFocusListener());
		tbl.addFocusListener(this);
		tbl.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
					setRow(tbl.convertRowIndexToModel(index));
				}
			}
		});
		tbl.getColumnModel().getColumn(0).setMinWidth(190);
		JScrollPane scrollTable = IGUI.createJScrollPane(tbl);
		scrollTable.setMinimumSize(new Dimension(200, 300));
		JPanel pnlData = new JPanel();
		pnlData.setMinimumSize(new Dimension(500, 300));
		pnlData.setLayout(new GridBagLayout());
		pnlData.setOpaque(true);
		pnlData.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		// Read only, cannot be edited
		lblName = IGUI.createJLabel(SwingConstants.LEFT, 0, "");
		JLabel label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Name:");
		IGUI.addComponent(label, 0, 0, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblName, 1, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblDescr = IGUI.createJLabel(SwingConstants.LEFT, 0, "");
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_N, "Descr:");
		IGUI.addComponent(label, 0, 1, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(lblDescr, 1, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		cboTAT = new IComboBox();
		cboTAT.setName("cboTurn");
		cboTAT.setModel(getTurnaround());
		cboTAT.addFocusListener(this);
		cboTAT.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimenmaster.turID = (byte) cb.getIndex();
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_T, "Turnaround:");
		label.setLabelFor(cboTAT);
		IGUI.addComponent(label, 0, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboTAT, 1, 2, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		cboGroup = new IComboBox();
		cboGroup.setName("cboGroup");
		cboGroup.setModel(getGroups());
		cboGroup.addFocusListener(this);
		cboGroup.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						IComboBox cb = (IComboBox) e.getSource();
						specimenmaster.grpID = cb.getIndex();
						lblSpecialty.setText(specgroups.get(specimenmaster.grpID).specialty);
						lblSubspecial.setText(specgroups.get(specimenmaster.grpID).subspecial);
						lblProcedure.setText(specgroups.get(specimenmaster.grpID).procedure);
						altered = true;
					}
				}
			}
		});
		label = IGUI.createJLabel(SwingConstants.LEFT, KeyEvent.VK_G, "Group:");
		label.setLabelFor(cboGroup);
		IGUI.addComponent(label, 0, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		IGUI.addComponent(cboGroup, 1, 3, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Specialty:");
		IGUI.addComponent(label, 0, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblSpecialty = IGUI.createJLabel(SwingConstants.LEFT, 0, "      ");
		IGUI.addComponent(lblSpecialty, 1, 4, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Subspecialty:");
		IGUI.addComponent(label, 0, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblSubspecial = IGUI.createJLabel(SwingConstants.LEFT, 0, "      ");
		IGUI.addComponent(lblSubspecial, 1, 5, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		label = IGUI.createJLabel(SwingConstants.LEFT, 0, "Procedure:");
		IGUI.addComponent(label, 0, 6, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, pnlData);
		lblProcedure = IGUI.createJLabel(SwingConstants.LEFT, 0, "      ");
		IGUI.addComponent(lblProcedure, 1, 6, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,
				pnlData);
		JSplitPane pnlSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pnlSplit.setTopComponent(scrollTable);
		pnlSplit.setBottomComponent(pnlData);
		pnlSplit.setOneTouchExpandable(true);
		pnlSplit.setDividerLocation(250);
		pnlSplit.setPreferredSize(new Dimension(800, 300));
		setLayout(new BorderLayout());
		add(new IToolBar(this), BorderLayout.NORTH);
		add(pnlSplit, BorderLayout.CENTER);
	}

	private void getData() {
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SPM_SELECT);
		try {
			while (rst.next()) {
				specimenmaster = new OSpecMaster();
				specimenmaster.spcID = rst.getShort("SMID");
				specimenmaster.grpID = rst.getShort("SGID");
				specimenmaster.spyID = rst.getByte("SYID");
				specimenmaster.subID = rst.getByte("SBID");
				specimenmaster.proID = rst.getByte("POID");
				specimenmaster.turID = rst.getByte("TAID");
				specimenmaster.name = rst.getString("SMNM").trim();
				specimenmaster.descr = rst.getString("SMDC").trim();
				list.add(specimenmaster);
			}
			pj.statusBar.setMessage("No Rows: " + list.size());
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
	}

	private Object[] getGroups() {
		OSpecDescr group = new OSpecDescr();
		ArrayList<OItem> list = new ArrayList<OItem>();
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_SPG_SELECT);
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("SGID"), rst.getString("SGDC")));
				group = new OSpecDescr();
				group.procedure = rst.getString("PONM");
				group.specialty = rst.getString("SYNM");
				group.subspecial = rst.getString("SBNM");
				specgroups.put(rst.getShort("SGID"), group);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
		return list.toArray();
	}

	private Object[] getTurnaround() {
		ArrayList<OItem> list = new ArrayList<OItem>();
		ResultSet rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_TUR_SELECT);
		try {
			while (rst.next()) {
				list.add(new OItem(rst.getShort("TAID"), rst.getString("TANM")));
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			pj.dbPowerJ.closeRst(rst);
		}
		return list.toArray();
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("specimenmaster.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1, 1.5f, 5, 1.5f, 1.5f, 1, 4, 1.5f };
		String str = "Specimens Master - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER.rotate(), 36, 18, 18, 18);
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
			for (int row = 0; row < tbl.getRowCount(); row++) {
				i = tbl.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case SPM_ID:
						paragraph.add(new Chunk(pj.numbers.formatNumber(list.get(i).spcID)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case SPM_NAME:
						paragraph.add(new Chunk(list.get(i).name));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_DESCR:
						paragraph.add(new Chunk(list.get(i).descr));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_TAT:
						paragraph.add(new Chunk(cboTAT.getItemName(list.get(i).turID)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_SPY:
						paragraph.add(new Chunk(specgroups.get(list.get(i).grpID).specialty));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_SUB:
						paragraph.add(new Chunk(specgroups.get(list.get(i).grpID).subspecial));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case SPM_GROUP:
						paragraph.add(new Chunk(cboGroup.getItemName(list.get(i).grpID)));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					default:
						paragraph.add(new Chunk(specgroups.get(list.get(i).grpID).procedure));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
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

	@Override
	void save() {
		pj.dbPowerJ.setShort(DPowerJ.STM_SPM_UPDATE, 1, specimenmaster.grpID);
		pj.dbPowerJ.setShort(DPowerJ.STM_SPM_UPDATE, 2, specimenmaster.turID);
		pj.dbPowerJ.setString(DPowerJ.STM_SPM_UPDATE, 3, specimenmaster.name.trim());
		pj.dbPowerJ.setString(DPowerJ.STM_SPM_UPDATE, 4, specimenmaster.descr.trim());
		pj.dbPowerJ.setShort(DPowerJ.STM_SPM_UPDATE, 5, specimenmaster.spcID);
		if (pj.dbPowerJ.execute(DPowerJ.STM_SPM_UPDATE) > 0) {
			altered = false;
		}
	}

	@Override
	void setFilter(short id, short value) {
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
					return (list.get(entry.getIdentifier()).spyID == filters[FILTER_SPY]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_SUB] != 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (list.get(entry.getIdentifier()).subID == filters[FILTER_SUB]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (filters[FILTER_PRO] != 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (list.get(entry.getIdentifier()).proID == filters[FILTER_PRO]);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (newCodes) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					return (list.get(entry.getIdentifier()).grpID == 0);
				}
			};
			rowFilters.add(rowFilter);
		}
		if (rowFilters.size() > 0) {
			// Add to the compound filter
			rowFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(rowFilters);
		}
		TableRowSorter<ModelSpecMstr> sorter = (TableRowSorter<ModelSpecMstr>) tbl.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
	}

	private void setRow(int index) {
		if (altered) {
			save();
		}
		programmaticChange = true;
		rowIndex = index;
		specimenmaster = list.get(rowIndex);
		lblName.setText(specimenmaster.name);
		lblDescr.setText(specimenmaster.descr);
		cboGroup.setIndex(specimenmaster.grpID);
		cboTAT.setIndex(specimenmaster.turID);
		lblSpecialty.setText(specgroups.get(specimenmaster.grpID).specialty);
		lblSubspecial.setText(specgroups.get(specimenmaster.grpID).subspecial);
		lblProcedure.setText(specgroups.get(specimenmaster.grpID).procedure);
		programmaticChange = false;
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("specimenmaster.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Specimens Master");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue(
					"Specimens Master - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
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
			for (int row = 0; row < tbl.getRowCount(); row++) {
				i = tbl.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case SPM_ID:
						xlsCell.setCellValue(list.get(i).spcID);
						break;
					case SPM_NAME:
						xlsCell.setCellValue(list.get(i).name);
						break;
					case SPM_DESCR:
						xlsCell.setCellValue(list.get(i).descr);
						break;
					case SPM_TAT:
						xlsCell.setCellValue(cboTAT.getItemName(list.get(i).turID));
						break;
					case SPM_SPY:
						xlsCell.setCellValue(specgroups.get(specimenmaster.grpID).specialty);
						break;
					case SPM_SUB:
						xlsCell.setCellValue(specgroups.get(specimenmaster.grpID).subspecial);
						break;
					case SPM_GROUP:
						xlsCell.setCellValue(cboGroup.getItemName(list.get(i).grpID));
						break;
					default:
						xlsCell.setCellValue(specgroups.get(specimenmaster.grpID).procedure);
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

	private class ModelSpecMstr extends ITableModel {

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
			return list.get(row).name;
		}
	}
}
