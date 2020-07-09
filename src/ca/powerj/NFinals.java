package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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

class NFinals extends NBase {
	private final byte ADDL_CODE = 0;
	private final byte ADDL_VAL1 = 1;
	private final byte ADDL_VAL2 = 2;
	private final byte ADDL_VAL3 = 3;
	private final byte ADDL_VAL4 = 4;
	private final byte ADDL_VAL5 = 5;
	private final byte ADDL_STAF = 6;
	private final byte ADDL_DATE = 7;
	private final byte CASE_NO = 0;
	private final byte CASE_FAC = 1;
	private final byte CASE_SPY = 2;
	private final byte CASE_SUB = 3;
	private final byte CASE_PROC = 4;
	private final byte CASE_SPEC = 5;
	private final byte CASE_VAL1 = 6;
	private final byte CASE_VAL2 = 7;
	private final byte CASE_VAL3 = 8;
	private final byte CASE_VAL4 = 9;
	private final byte CASE_VAL5 = 10;
	private final byte CASE_NOSP = 11;
	private final byte CASE_NOBL = 12;
	private final byte CASE_NOSL = 13;
	private final byte CASE_NOHE = 14;
	private final byte CASE_NOSS = 15;
	private final byte CASE_NOIH = 16;
	private final byte CASE_NOMO = 17;
	private final byte CASE_NOSY = 18;
	private final byte CASE_NOFS = 19;
	private final byte CASE_ACED = 20;
	private final byte CASE_GRED = 21;
	private final byte CASE_EMED = 22;
	private final byte CASE_MIED = 23;
	private final byte CASE_ROED = 24;
	private final byte CASE_FIED = 25;
	private final byte CASE_GRBY = 26;
	private final byte CASE_EMBY = 27;
	private final byte CASE_MIBY = 28;
	private final byte CASE_ROBY = 29;
	private final byte CASE_FIBY = 30;
	private final byte CASE_GRTA = 31;
	private final byte CASE_EMTA = 32;
	private final byte CASE_MITA = 33;
	private final byte CASE_ROTA = 34;
	private final byte CASE_FITA = 35;
	private final byte FRZN_STAF = 0;
	private final byte FRZN_VAL1 = 1;
	private final byte FRZN_VAL2 = 2;
	private final byte FRZN_VAL3 = 3;
	private final byte FRZN_VAL4 = 4;
	private final byte FRZN_VAL5 = 5;
	private final byte FRZN_NOBL = 6;
	private final byte FRZN_NOSL = 7;
	private final byte ORDER_NAME = 0;
	private final byte ORDER_QTY = 1;
	private final byte ORDER_VAL1 = 2;
	private final byte ORDER_VAL2 = 3;
	private final byte ORDER_VAL3 = 4;
	private final byte ORDER_VAL4 = 5;
	private final byte SPEC_NAME = 0;
	private final byte SPEC_VAL1 = 1;
	private final byte SPEC_VAL2 = 2;
	private final byte SPEC_VAL3 = 3;
	private final byte SPEC_VAL4 = 4;
	private final byte SPEC_VAL5 = 5;
	private final byte SPEC_NOBL = 6;
	private final byte SPEC_NOSL = 7;
	private final byte SPEC_NOFR = 8;
	private final byte SPEC_NOHE = 9;
	private final byte SPEC_NOSS = 10;
	private final byte SPEC_NOIH = 11;
	private final byte SPEC_NOMO = 12;
	private final byte FILTER_FAC = 0;
	private final byte FILTER_SPY = 1;
	private final byte FILTER_SUB = 2;
	private final byte FILTER_PRO = 3;
	private short[] filters = { 0, 0, 0, 0 };
	private String[] columns = { "NO", "FAC", "SPY", "SUB", "PROC", "SPEC", "", "", "", "", "", "SPECS", "BLKS", "SLDS",
			"H&E", "SS", "IHC", "MOL", "SYNP", "FS", "ACCESS", "GROSS", "EMBED", "MICRO", "ROUTE", "FINAL", "GRNM",
			"EMNM", "MINM", "RONM", "FINM", "grta", "emta", "mita", "rota", "FITA" };
	private long caseID = 0;
	private long specID = 0;
	private ArrayList<OCaseFinal> cases = new ArrayList<OCaseFinal>();
	private ArrayList<OSpecFinal> specimens = new ArrayList<OSpecFinal>();
	private ArrayList<OOrderFinal> orders = new ArrayList<OOrderFinal>();
	private ArrayList<OFrozen> frozens = new ArrayList<OFrozen>();
	private ArrayList<OAdditional> additionals = new ArrayList<OAdditional>();
	private ModelAdditional modelAddl;
	private ModelCase modelCase;
	private ModelFrozen modelFrozen;
	private ModelOrder modelOrder;
	private ModelSpecimen modelSpec;
	private ITable tblCase, tblSpec;
	private JTextArea txtComment;

	NFinals(AClient parent) {
		super(parent);
		setName("Finals");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_FINALS);
		columns[6] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		columns[7] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		columns[8] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		columns[9] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		columns[10] = pj.setup.getString(LSetup.VAR_V5_NAME);
		createPanel();
		WorkerData worker = new WorkerData();
		worker.execute();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		cases.clear();
		return true;
	}

	private void createPanel() {
		modelCase = new ModelCase();
		tblCase = new ITable(pj, modelCase) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					Point p = e.getPoint();
					int col = columnAtPoint(p);
					JTable tbl = (JTable) e.getSource();
					int viewRow = rowAtPoint(p);
					int modelRow = tbl.convertRowIndexToModel(viewRow);
					String tip = null;
					switch (col) {
					case CASE_GRBY:
						tip = cases.get(modelRow).grossName;
						break;
					case CASE_EMBY:
						tip = cases.get(modelRow).embedName;
						break;
					case CASE_MIBY:
						tip = cases.get(modelRow).microName;
						break;
					case CASE_ROBY:
						tip = cases.get(modelRow).routeName;
						break;
					case CASE_FIBY:
						tip = cases.get(modelRow).finalName;
						break;
					default:
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblCase.setName("Cases");
		tblCase.addFocusListener(this);
		// This class handles the ancestorAdded event and invokes the
		// requestFocusInWindow() method
		tblCase.addAncestorListener(new IFocusListener());
		tblCase.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (programmaticChange)
					return;
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int viewRow = lsm.getMinSelectionIndex();
				if (viewRow > -1) {
					// else, Selection got filtered away.
					int modelRow = tblCase.convertRowIndexToModel(viewRow);
					if (caseID != cases.get(modelRow).caseID) {
						caseID = cases.get(modelRow).caseID;
						setCase();
					}
				}
			}
		});
		JScrollPane scrollCase = IGUI.createJScrollPane(tblCase);
		scrollCase.setMinimumSize(new Dimension(900, 100));
		modelSpec = new ModelSpecimen();
		tblSpec = new ITable(pj, modelSpec) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					String tip = null;
					if (!programmaticChange) {
						Point p = e.getPoint();
						int col = columnAtPoint(p);
						if (col == SPEC_NAME) {
							int viewRow = rowAtPoint(p);
							JTable tbl = (JTable) e.getSource();
							int modelRow = tbl.convertRowIndexToModel(viewRow);
							tip = specimens.get(modelRow).descr;
						}
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblSpec.addFocusListener(this);
		tblSpec.setName("Specimens");
		tblSpec.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages
				if (programmaticChange)
					return;
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
					return;
				int viewRow = lsm.getMinSelectionIndex();
				if (viewRow > -1) {
					// else, Selection got filtered away.
					int modelRow = tblSpec.convertRowIndexToModel(viewRow);
					if (specID != specimens.get(modelRow).specID) {
						specID = specimens.get(modelRow).specID;
						setSpecimen();
					}
				}
			}
		});
		JScrollPane scrollSpec = IGUI.createJScrollPane(tblSpec);
		scrollSpec.setMinimumSize(new Dimension(900, 100));
		modelOrder = new ModelOrder();
		ITable tblOrder = new ITable(pj, modelOrder);
		tblOrder.addFocusListener(this);
		JScrollPane scrollOrder = IGUI.createJScrollPane(tblOrder);
		scrollOrder.setMinimumSize(new Dimension(500, 100));
		modelFrozen = new ModelFrozen();
		ITable tblFrozen = new ITable(pj, modelFrozen) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					String tip = null;
					if (!programmaticChange) {
						Point p = e.getPoint();
						int col = columnAtPoint(p);
						if (col == FRZN_STAF) {
							int viewRow = rowAtPoint(p);
							JTable tbl = (JTable) e.getSource();
							int modelRow = tbl.convertRowIndexToModel(viewRow);
							tip = frozens.get(modelRow).name;
						}
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblFrozen.addFocusListener(this);
		JScrollPane scrollFrozen = IGUI.createJScrollPane(tblFrozen);
		scrollFrozen.setMinimumSize(new Dimension(500, 100));
		modelAddl = new ModelAdditional();
		ITable tblAddl = new ITable(pj, modelAddl) {
			@Override
			public String getToolTipText(MouseEvent e) {
				try {
					String tip = null;
					if (!programmaticChange) {
						Point p = e.getPoint();
						if (columnAtPoint(p) == ADDL_STAF) {
							JTable tbl = (JTable) e.getSource();
							int viewRow = rowAtPoint(p);
							int modelRow = tbl.convertRowIndexToModel(viewRow);
							try {
								tip = additionals.get(modelRow).finalFull;
							} catch (RuntimeException ignore) {
							}
						}
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblAddl.addFocusListener(this);
		JScrollPane scrollAddl = IGUI.createJScrollPane(tblAddl);
		scrollAddl.setMinimumSize(new Dimension(500, 100));
		txtComment = new JTextArea();
		txtComment.setEditable(false);
		txtComment.setMargin(new Insets(5, 5, 5, 5));
		txtComment.setFont(LConstants.APP_FONT);
		txtComment.setLineWrap(true);
		txtComment.setWrapStyleWord(true);
		JScrollPane scrollComment = IGUI.createJScrollPane(txtComment);
		scrollComment.setMinimumSize(new Dimension(300, 400));
		JSplitPane splitTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitTop.setTopComponent(scrollCase);
		splitTop.setBottomComponent(scrollSpec);
		splitTop.setOneTouchExpandable(true);
		splitTop.setDividerLocation(150);
		splitTop.setPreferredSize(new Dimension(900, 300));
		JSplitPane splitMiddle = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitMiddle.setTopComponent(scrollOrder);
		splitMiddle.setBottomComponent(scrollFrozen);
		splitMiddle.setOneTouchExpandable(true);
		splitMiddle.setDividerLocation(150);
		splitMiddle.setPreferredSize(new Dimension(500, 300));
		JSplitPane splitLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitLeft.setTopComponent(splitMiddle);
		splitLeft.setBottomComponent(scrollAddl);
		splitLeft.setOneTouchExpandable(true);
		splitLeft.setDividerLocation(350);
		splitLeft.setPreferredSize(new Dimension(500, 500));
		JSplitPane splitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitBottom.setTopComponent(splitLeft);
		splitBottom.setBottomComponent(scrollComment);
		splitBottom.setOneTouchExpandable(true);
		splitBottom.setDividerLocation(550);
		splitBottom.setPreferredSize(new Dimension(900, 500));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(splitTop);
		splitAll.setBottomComponent(splitBottom);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(350);
		splitAll.setPreferredSize(new Dimension(900, 900));
		JScrollPane scrollAll = IGUI.createJScrollPane(splitAll);
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this), BorderLayout.NORTH);
		add(scrollAll, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		String fileName = pj.getFilePdf("finals.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1.5f, 1, 1.5f, 1, 1.5f, 1.5f, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2,
				2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		String str = "Backlog - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME);
		LPdf pdfLib = new LPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
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
			for (int row = 0; row < tblCase.getRowCount(); row++) {
				i = tblCase.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case CASE_NO:
						paragraph.add(new Chunk(cases.get(i).caseNo));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_FAC:
						paragraph.add(new Chunk(cases.get(i).facName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPY:
						paragraph.add(new Chunk(cases.get(i).specName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SUB:
						paragraph.add(new Chunk(cases.get(i).subName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_PROC:
						paragraph.add(new Chunk(cases.get(i).procName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPEC:
						paragraph.add(new Chunk(cases.get(i).specName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_VAL1:
						paragraph.add(new Chunk(pj.numbers.formatDouble(3, cases.get(i).value1)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_VAL2:
						paragraph.add(new Chunk(pj.numbers.formatDouble(3, cases.get(i).value2)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_VAL3:
						paragraph.add(new Chunk(pj.numbers.formatDouble(3, cases.get(i).value3)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_VAL4:
						paragraph.add(new Chunk(pj.numbers.formatDouble(3, cases.get(i).value4)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_VAL5:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).value5 / 60)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSP:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).noSpec)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOBL:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).noBlocks)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSL:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).noSlides)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOHE:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).noHE)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSS:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).noSS)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOIH:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).noIHC)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOMO:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).noMol)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSY:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).noSynop)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOFS:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).noFSSpec)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ACED:
						paragraph.add(new Chunk(pj.dates.formatter(cases.get(i).accessed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_GRED:
						paragraph.add(new Chunk(pj.dates.formatter(cases.get(i).grossed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_EMED:
						paragraph.add(new Chunk(pj.dates.formatter(cases.get(i).embeded, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_MIED:
						paragraph.add(new Chunk(pj.dates.formatter(cases.get(i).microed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROED:
						paragraph.add(new Chunk(pj.dates.formatter(cases.get(i).routed, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_FIED:
						paragraph.add(new Chunk(pj.dates.formatter(cases.get(i).finaled, LDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_GRBY:
						paragraph.add(new Chunk(cases.get(i).grossName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_EMBY:
						paragraph.add(new Chunk(cases.get(i).embedName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_MIBY:
						paragraph.add(new Chunk(cases.get(i).microName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_ROBY:
						paragraph.add(new Chunk(cases.get(i).routeName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_FIBY:
						paragraph.add(new Chunk(cases.get(i).finalName));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_GRTA:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).grossTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_EMTA:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).embedTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_MITA:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).microTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROTA:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).routeTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
						paragraph.add(new Chunk(pj.numbers.formatNumber(cases.get(i).finalTAT)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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

	private void setCase() {
		if (LBase.busy.get())
			return;
		String comment = "";
		ResultSet rst = null;
		try {
			programmaticChange = true;
			specID = 0;
			specimens.clear();
			additionals.clear();
			orders.clear();
			frozens.clear();
			pj.dbPowerJ.setLong(pjStms.get(DPowerJ.STM_CMT_SELECT), 1, caseID);
			rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_CMT_SELECT));
			while (rst.next()) {
				// deleted after a year
				if (rst.getString("com1") != null && rst.getString("com1").length() > 2) {
					comment = rst.getString("com1");
				}
				if (rst.getString("com2") != null && rst.getString("com2").length() > 2) {
					comment += rst.getString("com2");
				}
				if (rst.getString("com3") != null && rst.getString("com3").length() > 2) {
					comment += rst.getString("com3");
				}
				if (rst.getString("com4") != null && rst.getString("com4").length() > 2) {
					comment += rst.getString("com4");
				}
			}
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.setLong(pjStms.get(DPowerJ.STM_SPE_SELECT), 1, caseID);
			rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SPE_SELECT));
			OSpecFinal specimen = new OSpecFinal();
			while (rst.next()) {
				specimen = new OSpecFinal();
				specimen.noBlocks = rst.getShort("spbl");
				specimen.noSlides = rst.getShort("spsl");
				specimen.noHE = rst.getShort("sphe");
				specimen.noSS = rst.getShort("spss");
				specimen.noIHC = rst.getShort("spih");
				specimen.noMOL = rst.getShort("spmo");
				specimen.noFrags = rst.getShort("spfr");
				specimen.value5 = rst.getInt("spv5");
				specimen.specID = rst.getLong("spid");
				specimen.value1 = rst.getDouble("spv1");
				specimen.value2 = rst.getDouble("spv2");
				specimen.value3 = rst.getDouble("spv3");
				specimen.value4 = rst.getDouble("spv4");
				specimen.name = rst.getString("smnm");
				specimen.descr = rst.getString("spdc");
				specimens.add(specimen);
			}
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.setLong(pjStms.get(DPowerJ.STM_ADD_SL_CID), 1, caseID);
			rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ADD_SL_CID));
			byte codeID = 0;
			final String[] codes = { "NIL", "AMND", "ADDN", "CORR", "REVW" };
			OAdditional additional = new OAdditional();
			while (rst.next()) {
				if (rst.getShort("adcd") > 4) {
					// Reviews
					codeID = 4;
				} else {
					codeID = rst.getByte("adcd");
				}
				additional = new OAdditional();
				additional.code = codes[codeID];
				additional.value5 = rst.getInt("adv5");
				additional.value1 = rst.getDouble("adv1");
				additional.value2 = rst.getDouble("adv2");
				additional.value3 = rst.getDouble("adv3");
				additional.value4 = rst.getDouble("adv4");
				additional.finalName = rst.getString("prnm");
				additional.finalFull = rst.getString("prls") + ", " + rst.getString("prfr").substring(0, 1);
				additional.finaled.setTimeInMillis(rst.getTimestamp("addt").getTime());
				additionals.add(additional);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			programmaticChange = false;
			pj.dbPowerJ.close(rst);
			modelAddl.fireTableDataChanged();
			modelFrozen.fireTableDataChanged();
			modelOrder.fireTableDataChanged();
			modelSpec.fireTableDataChanged();
			txtComment.setText(comment);
		}
	}

	@Override
	void setFilter(short id, short value) {
		switch (id) {
		case IToolBar.TB_FAC:
			filters[FILTER_FAC] = value;
			break;
		case IToolBar.TB_SPY:
			filters[FILTER_SPY] = value;
			break;
		case IToolBar.TB_SUB:
			filters[FILTER_SUB] = value;
			break;
		case IToolBar.TB_PRO:
			filters[FILTER_PRO] = value;
			break;
		default:
			pj.log(LConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
			return;
		}
		caseID = 0;
		specID = 0;
		cases.clear();
		specimens.clear();
		additionals.clear();
		orders.clear();
		frozens.clear();
		txtComment.setText("");
		pj.setBusy(true);
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	private void setSpecimen() {
		if (LBase.busy.get())
			return;
		ResultSet rst = null;
		try {
			programmaticChange = true;
			orders.clear();
			frozens.clear();
			pj.dbPowerJ.setLong(pjStms.get(DPowerJ.STM_ORD_SELECT), 1, specID);
			rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ORD_SELECT));
			OOrderFinal order = new OOrderFinal();
			while (rst.next()) {
				order = new OOrderFinal();
				order.qty = rst.getShort("orqy");
				order.value1 = rst.getDouble("orv1");
				order.value2 = rst.getDouble("orv2");
				order.value3 = rst.getDouble("orv3");
				order.value4 = rst.getDouble("orv4");
				order.name = rst.getString("ognm");
				orders.add(order);
			}
			pj.dbPowerJ.close(rst);
			pj.dbPowerJ.setLong(pjStms.get(DPowerJ.STM_FRZ_SL_SID), 1, specID);
			rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_FRZ_SL_SID));
			OFrozen frozen = new OFrozen();
			while (rst.next()) {
				frozen = new OFrozen();
				frozen.noBlocks = rst.getShort("frbl");
				frozen.noSlides = rst.getShort("frsl");
				frozen.value5 = rst.getInt("frv5");
				frozen.value1 = rst.getDouble("frv1");
				frozen.value2 = rst.getDouble("frv2");
				frozen.value3 = rst.getDouble("frv3");
				frozen.value4 = rst.getDouble("frv4");
				frozen.finalBy = rst.getString("prnm");
				frozen.name = rst.getString("prls") + ", " + rst.getString("prfr").substring(0, 1);
				frozens.add(frozen);
			}
		} catch (SQLException e) {
			pj.log(LConstants.ERROR_SQL, getName(), e);
		} finally {
			programmaticChange = false;
			pj.dbPowerJ.close(rst);
			modelFrozen.fireTableDataChanged();
			modelOrder.fireTableDataChanged();
		}
	}

	@Override
	void xls() {
		String fileName = pj.getFileXls("finals.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LExcel xlsLib = new LExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Finals");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Finals - " + pj.dates.formatter(Calendar.getInstance(), LDates.FORMAT_DATETIME));
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
				case CASE_ACED:
				case CASE_GRED:
				case CASE_EMED:
				case CASE_MIED:
				case CASE_ROED:
				case CASE_FIED:
					sheet.setColumnWidth(col, 18 * 256); // 18 characters
					sheet.setDefaultColumnStyle(col, styles.get("datetime"));
					break;
				case CASE_VAL1:
				case CASE_VAL2:
				case CASE_VAL3:
				case CASE_VAL4:
					sheet.setColumnWidth(col, 6 * 256); // 6 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_float"));
					break;
				case CASE_NOSP:
				case CASE_NOBL:
				case CASE_NOSL:
				case CASE_NOHE:
				case CASE_NOSS:
				case CASE_NOIH:
				case CASE_NOMO:
				case CASE_NOSY:
				case CASE_NOFS:
				case CASE_VAL5:
				case CASE_GRTA:
				case CASE_EMTA:
				case CASE_MITA:
				case CASE_ROTA:
				case CASE_FITA:
					sheet.setColumnWidth(col, 5 * 256); // 10 characters
					sheet.setDefaultColumnStyle(col, styles.get("data_int"));
					break;
				case CASE_FAC:
				case CASE_SUB:
				case CASE_GRBY:
				case CASE_EMBY:
				case CASE_MIBY:
				case CASE_ROBY:
				case CASE_FIBY:
					sheet.setColumnWidth(col, 5 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case CASE_SPY:
					sheet.setColumnWidth(col, 10 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case CASE_PROC:
					sheet.setColumnWidth(col, 8 * 256); // 5 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				case CASE_SPEC:
					sheet.setColumnWidth(col, 12 * 256);
					sheet.setDefaultColumnStyle(col, styles.get("text"));
					break;
				default:
					sheet.setColumnWidth(col, 18 * 256); // 18 characters
					sheet.setDefaultColumnStyle(col, styles.get("text"));
				}
			}
			// data rows
			int rownum = 2;
			int i = 0;
			for (int row = 0; row < tblCase.getRowCount(); row++) {
				i = tblCase.convertRowIndexToModel(row);
				xlsRow = sheet.createRow(rownum++);
				for (int col = 0; col < columns.length; col++) {
					xlsCell = xlsRow.createCell(col);
					switch (col) {
					case CASE_NO:
						xlsCell.setCellValue(cases.get(i).caseNo);
						break;
					case CASE_FAC:
						xlsCell.setCellValue(cases.get(i).facName);
						break;
					case CASE_SPY:
						xlsCell.setCellValue(cases.get(i).specName);
						break;
					case CASE_SUB:
						xlsCell.setCellValue(cases.get(i).subName);
						break;
					case CASE_PROC:
						xlsCell.setCellValue(cases.get(i).procName);
						break;
					case CASE_SPEC:
						xlsCell.setCellValue(cases.get(i).specName);
						break;
					case CASE_VAL1:
						xlsCell.setCellValue(cases.get(i).value1);
						break;
					case CASE_VAL2:
						xlsCell.setCellValue(cases.get(i).value2);
						break;
					case CASE_VAL3:
						xlsCell.setCellValue(cases.get(i).value3);
						break;
					case CASE_VAL4:
						xlsCell.setCellValue(cases.get(i).value4);
						break;
					case CASE_VAL5:
						xlsCell.setCellValue(cases.get(i).value5 / 60);
						break;
					case CASE_NOSP:
						xlsCell.setCellValue(cases.get(i).noSpec);
						break;
					case CASE_NOBL:
						xlsCell.setCellValue(cases.get(i).noBlocks);
						break;
					case CASE_NOSL:
						xlsCell.setCellValue(cases.get(i).noSlides);
						break;
					case CASE_NOHE:
						xlsCell.setCellValue(cases.get(i).noHE);
						break;
					case CASE_NOSS:
						xlsCell.setCellValue(cases.get(i).noSS);
						break;
					case CASE_NOIH:
						xlsCell.setCellValue(cases.get(i).noIHC);
						break;
					case CASE_NOMO:
						xlsCell.setCellValue(cases.get(i).noMol);
						break;
					case CASE_NOSY:
						xlsCell.setCellValue(cases.get(i).noSynop);
						break;
					case CASE_NOFS:
						xlsCell.setCellValue(cases.get(i).noFSSpec);
						break;
					case CASE_ACED:
						xlsCell.setCellValue(cases.get(i).accessed);
						break;
					case CASE_GRED:
						xlsCell.setCellValue(cases.get(i).grossed);
						break;
					case CASE_EMED:
						xlsCell.setCellValue(cases.get(i).embeded);
						break;
					case CASE_MIED:
						xlsCell.setCellValue(cases.get(i).microed);
						break;
					case CASE_ROED:
						xlsCell.setCellValue(cases.get(i).routed);
						break;
					case CASE_FIED:
						xlsCell.setCellValue(cases.get(i).finaled);
						break;
					case CASE_GRBY:
						xlsCell.setCellValue(cases.get(i).grossName);
						break;
					case CASE_EMBY:
						xlsCell.setCellValue(cases.get(i).embedName);
						break;
					case CASE_MIBY:
						xlsCell.setCellValue(cases.get(i).microName);
						break;
					case CASE_ROBY:
						xlsCell.setCellValue(cases.get(i).routeName);
						break;
					case CASE_FIBY:
						xlsCell.setCellValue(cases.get(i).finalName);
						break;
					case CASE_GRTA:
						xlsCell.setCellValue(cases.get(i).grossTAT);
						break;
					case CASE_EMTA:
						xlsCell.setCellValue(cases.get(i).embedTAT);
						break;
					case CASE_MITA:
						xlsCell.setCellValue(cases.get(i).microTAT);
						break;
					case CASE_ROTA:
						xlsCell.setCellValue(cases.get(i).routeTAT);
						break;
					default:
						xlsCell.setCellValue(cases.get(i).finalTAT);
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

	private class ModelAdditional extends ITableModel {
		private String[] headers = { "ADDL", columns[6], columns[7], columns[8], columns[9], columns[10], "STAFF",
				"DATE" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case ADDL_VAL1:
			case ADDL_VAL2:
			case ADDL_VAL3:
			case ADDL_VAL4:
				return Double.class;
			case ADDL_VAL5:
				return Integer.class;
			case ADDL_DATE:
				return Calendar.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return headers.length;
		}

		@Override
		public String getColumnName(int col) {
			return headers[col];
		}

		@Override
		public int getRowCount() {
			return additionals.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (additionals.size() > 0 && row < additionals.size()) {
				switch (col) {
				case ADDL_CODE:
					value = additionals.get(row).code;
					break;
				case ADDL_STAF:
					value = additionals.get(row).finalName;
					break;
				case ADDL_DATE:
					value = additionals.get(row).finaled;
					break;
				case ADDL_VAL1:
					value = additionals.get(row).value1;
					break;
				case ADDL_VAL2:
					value = additionals.get(row).value2;
					break;
				case ADDL_VAL3:
					value = additionals.get(row).value3;
					break;
				case ADDL_VAL4:
					value = additionals.get(row).value4;
					break;
				default:
					value = additionals.get(row).value5 / 60;
				}
			}
			return value;
		}
	}

	private class ModelCase extends ITableModel {

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case CASE_VAL1:
			case CASE_VAL2:
			case CASE_VAL3:
			case CASE_VAL4:
				return Double.class;
			case CASE_ACED:
			case CASE_GRED:
			case CASE_EMED:
			case CASE_MIED:
			case CASE_ROED:
			case CASE_FIED:
				return Calendar.class;
			case CASE_NOBL:
			case CASE_NOSL:
			case CASE_NOSP:
			case CASE_NOSY:
			case CASE_NOFS:
			case CASE_NOHE:
			case CASE_NOSS:
			case CASE_NOIH:
			case CASE_NOMO:
				return Short.class;
			case CASE_VAL5:
				return Integer.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int col) {
			return columns[col];
		}

		@Override
		public int getRowCount() {
			return cases.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (cases.size() > 0 && row < cases.size()) {
				switch (col) {
				case CASE_NO:
					value = cases.get(row).caseNo;
					break;
				case CASE_FAC:
					value = cases.get(row).facName;
					break;
				case CASE_SPY:
					value = cases.get(row).spyName;
					break;
				case CASE_SUB:
					value = cases.get(row).subName;
					break;
				case CASE_PROC:
					value = cases.get(row).procName;
					break;
				case CASE_SPEC:
					value = cases.get(row).specName;
					break;
				case CASE_VAL1:
					value = cases.get(row).value1;
					break;
				case CASE_VAL2:
					value = cases.get(row).value2;
					break;
				case CASE_VAL3:
					value = cases.get(row).value3;
					break;
				case CASE_VAL4:
					value = cases.get(row).value4;
					break;
				case CASE_VAL5:
					value = cases.get(row).value5 / 60;
					break;
				case CASE_NOSP:
					value = cases.get(row).noSpec;
					break;
				case CASE_NOBL:
					value = cases.get(row).noBlocks;
					break;
				case CASE_NOSL:
					value = cases.get(row).noSlides;
					break;
				case CASE_NOHE:
					value = cases.get(row).noHE;
					break;
				case CASE_NOSS:
					value = cases.get(row).noSS;
					break;
				case CASE_NOIH:
					value = cases.get(row).noIHC;
					break;
				case CASE_NOMO:
					value = cases.get(row).noMol;
					break;
				case CASE_NOSY:
					value = cases.get(row).noSynop;
					break;
				case CASE_NOFS:
					value = cases.get(row).noFSSpec;
					break;
				case CASE_ACED:
					value = cases.get(row).accessed;
					break;
				case CASE_GRED:
					value = cases.get(row).grossed;
					break;
				case CASE_EMED:
					value = cases.get(row).embeded;
					break;
				case CASE_MIED:
					value = cases.get(row).microed;
					break;
				case CASE_ROED:
					value = cases.get(row).routed;
					break;
				case CASE_FIED:
					value = cases.get(row).finaled;
					break;
				case CASE_GRBY:
					value = cases.get(row).grossName;
					break;
				case CASE_EMBY:
					value = cases.get(row).embedName;
					break;
				case CASE_MIBY:
					value = cases.get(row).microName;
					break;
				case CASE_ROBY:
					value = cases.get(row).routeName;
					break;
				case CASE_FIBY:
					value = cases.get(row).finalName;
					break;
				case CASE_GRTA:
					value = cases.get(row).grossTAT;
					break;
				case CASE_EMTA:
					value = cases.get(row).embedTAT;
					break;
				case CASE_MITA:
					value = cases.get(row).microTAT;
					break;
				case CASE_ROTA:
					value = cases.get(row).routeTAT;
					break;
				default:
					value = cases.get(row).finalTAT;
				}
			}
			return value;
		}
	}

	private class ModelFrozen extends ITableModel {
		private final String[] headers = { "FSEC", columns[6], columns[7], columns[8], columns[9], columns[10], "BLKS",
				"SLDS" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case FRZN_VAL1:
			case FRZN_VAL2:
			case FRZN_VAL3:
			case FRZN_VAL4:
				return Double.class;
			case FRZN_NOBL:
			case FRZN_NOSL:
				return Short.class;
			case FRZN_VAL5:
				return Integer.class;
			default:
				return OItem.class;
			}
		}

		@Override
		public int getColumnCount() {
			return headers.length;
		}

		@Override
		public String getColumnName(int col) {
			return headers[col];
		}

		@Override
		public int getRowCount() {
			return frozens.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (frozens.size() > 0 && row < frozens.size()) {
				switch (col) {
				case FRZN_STAF:
					value = frozens.get(row).finalBy;
					break;
				case FRZN_NOBL:
					value = frozens.get(row).noBlocks;
					break;
				case FRZN_NOSL:
					value = frozens.get(row).noSlides;
					break;
				case ADDL_VAL1:
					value = frozens.get(row).value1;
					break;
				case ADDL_VAL2:
					value = frozens.get(row).value2;
					break;
				case ADDL_VAL3:
					value = frozens.get(row).value3;
					break;
				case ADDL_VAL4:
					value = frozens.get(row).value4;
					break;
				default:
					value = frozens.get(row).value5 / 60;
				}
			}
			return value;
		}
	}

	private class ModelOrder extends ITableModel {
		private final String[] headers = { "ORDER", "QTY", columns[6], columns[7], columns[8], columns[9] };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case ORDER_VAL1:
			case ORDER_VAL2:
			case ORDER_VAL3:
			case ORDER_VAL4:
				return Double.class;
			case ORDER_QTY:
				return Short.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return headers.length;
		}

		@Override
		public String getColumnName(int col) {
			return headers[col];
		}

		@Override
		public int getRowCount() {
			return orders.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (orders.size() > 0 && row < orders.size()) {
				switch (col) {
				case ORDER_NAME:
					value = orders.get(row).name;
					break;
				case ORDER_QTY:
					value = orders.get(row).qty;
					break;
				case ORDER_VAL1:
					value = orders.get(row).value1;
					break;
				case ORDER_VAL2:
					value = orders.get(row).value2;
					break;
				case ORDER_VAL3:
					value = orders.get(row).value3;
					break;
				case ORDER_VAL4:
					value = orders.get(row).value4;
					break;
				default:
				}
			}
			return value;
		}
	}

	private class ModelSpecimen extends ITableModel {
		private final String[] headers = { "SPEC", columns[6], columns[7], columns[8], columns[9], columns[10], "BLKS",
				"SLDS", "FRAG", "H&E", "SS", "IHC", "MOL" };

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case SPEC_VAL1:
			case SPEC_VAL2:
			case SPEC_VAL3:
			case SPEC_VAL4:
				return Double.class;
			case SPEC_NOBL:
			case SPEC_NOSL:
			case SPEC_NOHE:
			case SPEC_NOSS:
			case SPEC_NOIH:
			case SPEC_NOMO:
			case SPEC_NOFR:
				return Short.class;
			case SPEC_VAL5:
				return Integer.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return headers.length;
		}

		@Override
		public String getColumnName(int col) {
			return headers[col];
		}

		@Override
		public int getRowCount() {
			return specimens.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			if (specimens.size() > 0 && row < specimens.size()) {
				switch (col) {
				case SPEC_NAME:
					value = specimens.get(row).name;
					break;
				case SPEC_NOBL:
					value = specimens.get(row).noBlocks;
					break;
				case SPEC_NOSL:
					value = specimens.get(row).noSlides;
					break;
				case SPEC_NOHE:
					value = specimens.get(row).noHE;
					break;
				case SPEC_NOSS:
					value = specimens.get(row).noSS;
					break;
				case SPEC_NOIH:
					value = specimens.get(row).noIHC;
					break;
				case SPEC_NOMO:
					value = specimens.get(row).noMOL;
					break;
				case SPEC_NOFR:
					value = specimens.get(row).noFrags;
					break;
				case SPEC_VAL1:
					value = specimens.get(row).value1;
					break;
				case SPEC_VAL2:
					value = specimens.get(row).value2;
					break;
				case SPEC_VAL3:
					value = specimens.get(row).value3;
					break;
				case SPEC_VAL4:
					value = specimens.get(row).value4;
					break;
				default:
					value = specimens.get(row).value5 / 60;
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			final String[] fields = { " AND faid = ", " AND syid = ", " AND sbid = ", " AND poid = " };
			int noRows = 0;
			String filter = "";
			ResultSet rst = null;
			try {
				setName("WorkerData");
				for (int i = 0; i < filters.length; i++) {
					if (filters[i] > 0) {
						filter += fields[i] + filters[i];
					}
				}
				if (filter.length() > 0) {
					filter = " WHERE " + filter.substring(5);
				}
				rst = pj.dbPowerJ.getResultSet(DPowerJ.STM_CSE_SELECT, filter);
				while (rst.next()) {
					OCaseFinal thisRow = new OCaseFinal();
					thisRow.noSynop = rst.getByte("casy");
					thisRow.noSpec = rst.getByte("casp");
					thisRow.noFSSpec = rst.getByte("cafs");
					thisRow.noBlocks = rst.getShort("cabl");
					thisRow.noSlides = rst.getShort("casl");
					thisRow.noHE = rst.getShort("cahe");
					thisRow.noSS = rst.getShort("cass");
					thisRow.noIHC = rst.getShort("caih");
					thisRow.noMol = rst.getShort("camo");
					thisRow.grossTAT = rst.getShort("grta");
					thisRow.embedTAT = rst.getShort("emta");
					thisRow.microTAT = rst.getShort("mita");
					thisRow.routeTAT = rst.getShort("rota");
					thisRow.finalTAT = rst.getShort("fnta");
					thisRow.value5 = rst.getInt("cav5");
					thisRow.caseID = rst.getLong("caid");
					thisRow.value1 = rst.getDouble("cav1");
					thisRow.value2 = rst.getDouble("cav2");
					thisRow.value3 = rst.getDouble("cav3");
					thisRow.value4 = rst.getDouble("cav4");
					thisRow.caseNo = rst.getString("cano");
					thisRow.facName = rst.getString("fanm");
					thisRow.spyName = rst.getString("synm");
					thisRow.subName = rst.getString("sbnm");
					thisRow.procName = rst.getString("ponm");
					thisRow.specName = rst.getString("smnm");
					thisRow.grossName = rst.getString("GRNM");
					thisRow.embedName = rst.getString("EMNM");
					thisRow.microName = rst.getString("MINM");
					thisRow.routeName = rst.getString("RONM");
					thisRow.finalName = rst.getString("fnnm");
					thisRow.grossFull = rst.getString("GRFR").trim() + " " + rst.getString("GRLS").trim();
					thisRow.embedFull = rst.getString("EMFR").trim() + " " + rst.getString("EMLS").trim();
					thisRow.microFull = rst.getString("MIFR").trim() + " " + rst.getString("MILS").trim();
					thisRow.routeFull = rst.getString("ROFR").trim() + " " + rst.getString("ROLS").trim();
					thisRow.finalFull = rst.getString("fnfr").trim() + " " + rst.getString("fnls").trim();
					thisRow.accessed.setTimeInMillis(rst.getTimestamp("aced").getTime());
					thisRow.grossed.setTimeInMillis(rst.getTimestamp("gred").getTime());
					thisRow.embeded.setTimeInMillis(rst.getTimestamp("emed").getTime());
					thisRow.microed.setTimeInMillis(rst.getTimestamp("mied").getTime());
					thisRow.routed.setTimeInMillis(rst.getTimestamp("roed").getTime());
					thisRow.finaled.setTimeInMillis(rst.getTimestamp("fned").getTime());
					cases.add(thisRow);
					noRows++;
					if (noRows % 1000 == 0) {
						try {
							Thread.sleep(LConstants.SLEEP_TIME);
						} catch (InterruptedException ignore) {
						}
					}
				}
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
			return null;
		}

		@Override
		public void done() {
			if (modelCase != null) {
				modelCase.fireTableDataChanged();
				modelAddl.fireTableDataChanged();
				modelFrozen.fireTableDataChanged();
				modelOrder.fireTableDataChanged();
				modelSpec.fireTableDataChanged();
			}
			pj.statusBar.setMessage("No Rows: " + pj.numbers.formatNumber(tblCase.getRowCount()));
			pj.setBusy(false);
			programmaticChange = false;
		}
	}
}