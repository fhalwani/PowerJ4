package ca.powerj.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import ca.powerj.data.AdditionalData;
import ca.powerj.data.CaseData;
import ca.powerj.data.FrozenData;
import ca.powerj.data.ItemData;
import ca.powerj.data.OrderData;
import ca.powerj.data.SpecimenData;
import ca.powerj.lib.LibConstants;
import ca.powerj.lib.LibDates;
import ca.powerj.lib.LibExcel;
import ca.powerj.lib.LibPdf;
import ca.powerj.lib.LibSetup;
import ca.powerj.swing.IFocusListener;
import ca.powerj.swing.ITable;
import ca.powerj.swing.ITableModel;
import ca.powerj.swing.IToolBar;
import ca.powerj.swing.IUtilities;

class FinalsPanel extends BasePanel {
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
	private int[] filters = { 0, 0, 0, 0 };
	private String[] columns = { "NO", "FAC", "SPY", "SUB", "PROC", "SPEC", "", "", "", "", "", "SPECS", "BLKS", "SLDS",
			"H&E", "SS", "IHC", "MOL", "SYNP", "FS", "ACCESS", "GROSS", "EMBED", "MICRO", "ROUTE", "FINAL", "GRNM",
			"EMNM", "MINM", "RONM", "FINM", "grta", "emta", "mita", "rota", "FITA" };
	private long caseID = 0;
	private long specID = 0;
	private ArrayList<CaseData> cases = new ArrayList<CaseData>();
	private ArrayList<SpecimenData> specimens = new ArrayList<SpecimenData>();
	private ArrayList<OrderData> orders = new ArrayList<OrderData>();
	private ArrayList<FrozenData> frozens = new ArrayList<FrozenData>();
	private ArrayList<AdditionalData> additionals = new ArrayList<AdditionalData>();
	private ModelAdditional modelAddl;
	private ModelCase modelCase;
	private ModelFrozen modelFrozen;
	private ModelOrder modelOrder;
	private ModelSpecimen modelSpec;
	private ITable tblCase, tblSpec;
	private JTextArea txtComment;

	FinalsPanel(AppFrame application) {
		super(application);
		setName("Finals");
		application.dbPowerJ.setStatements(LibConstants.ACTION_FINALS);
		columns[6] = application.getProperty("coder1");
		columns[7] = application.getProperty("coder2");
		columns[8] = application.getProperty("coder3");
		columns[9] = application.getProperty("coder4");
		columns[10] = application.getProperty("coder5");
		createPanel();
		WorkerData worker = new WorkerData();
		worker.execute();
		programmaticChange = false;
	}

	@Override
	public boolean close() {
		super.close();
		cases.clear();
		return true;
	}

	private void createPanel() {
		modelCase = new ModelCase();
		tblCase = new ITable(modelCase, application.dates, application.numbers) {
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
						tip = cases.get(modelRow).getGrossName();
						break;
					case CASE_EMBY:
						tip = cases.get(modelRow).getEmbedName();
						break;
					case CASE_MIBY:
						tip = cases.get(modelRow).getMicroName();
						break;
					case CASE_ROBY:
						tip = cases.get(modelRow).getRouteName();
						break;
					case CASE_FIBY:
						tip = cases.get(modelRow).getFinalName();
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
					if (caseID != cases.get(modelRow).getCaseID()) {
						caseID = cases.get(modelRow).getCaseID();
						setCase();
					}
				}
			}
		});
		JScrollPane scrollCase = IUtilities.createJScrollPane(tblCase);
		scrollCase.setMinimumSize(new Dimension(900, 100));
		modelSpec = new ModelSpecimen();
		tblSpec = new ITable(modelSpec, application.dates, application.numbers) {
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
							tip = specimens.get(modelRow).getDescr();
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
					if (specID != specimens.get(modelRow).getSpecID()) {
						specID = specimens.get(modelRow).getSpecID();
						setSpecimen();
					}
				}
			}
		});
		JScrollPane scrollSpec = IUtilities.createJScrollPane(tblSpec);
		scrollSpec.setMinimumSize(new Dimension(900, 100));
		modelOrder = new ModelOrder();
		ITable tblOrder = new ITable(modelOrder, application.dates, application.numbers);
		tblOrder.addFocusListener(this);
		JScrollPane scrollOrder = IUtilities.createJScrollPane(tblOrder);
		scrollOrder.setMinimumSize(new Dimension(500, 100));
		modelFrozen = new ModelFrozen();
		ITable tblFrozen = new ITable(modelFrozen, application.dates, application.numbers) {
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
							tip = frozens.get(modelRow).getName();
						}
					}
					return tip;
				} catch (IndexOutOfBoundsException ignore) {
					return null;
				}
			}
		};
		tblFrozen.addFocusListener(this);
		JScrollPane scrollFrozen = IUtilities.createJScrollPane(tblFrozen);
		scrollFrozen.setMinimumSize(new Dimension(500, 100));
		modelAddl = new ModelAdditional();
		ITable tblAddl = new ITable(modelAddl, application.dates, application.numbers) {
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
								tip = additionals.get(modelRow).getFinalFull();
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
		JScrollPane scrollAddl = IUtilities.createJScrollPane(tblAddl);
		scrollAddl.setMinimumSize(new Dimension(500, 100));
		txtComment = new JTextArea();
		txtComment.setEditable(false);
		txtComment.setMargin(new Insets(5, 5, 5, 5));
		txtComment.setFont(LibConstants.APP_FONT);
		txtComment.setLineWrap(true);
		txtComment.setWrapStyleWord(true);
		JScrollPane scrollComment = IUtilities.createJScrollPane(txtComment);
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
		JScrollPane scrollAll = IUtilities.createJScrollPane(splitAll);
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(application), BorderLayout.NORTH);
		add(scrollAll, BorderLayout.CENTER);
	}

	@Override
	void pdf() {
		String fileName = application.getFilePdf("finals.pdf").trim();
		if (fileName.length() == 0)
			return;
		final float[] widths = { 1.5f, 1, 1.5f, 1, 1.5f, 1.5f, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2,
				2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		String str = "Backlog - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME);
		LibPdf pdfLib = new LibPdf();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize._11X17.rotate(), 36, 18, 18, 18);
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
			for (int row = 0; row < tblCase.getRowCount(); row++) {
				i = tblCase.convertRowIndexToModel(row);
				for (int col = 0; col < columns.length; col++) {
					paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					cell = new PdfPCell();
					switch (col) {
					case CASE_NO:
						paragraph.add(new Chunk(cases.get(i).getCaseNo()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_FAC:
						paragraph.add(new Chunk(cases.get(i).getFacName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPY:
						paragraph.add(new Chunk(cases.get(i).getSpecName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SUB:
						paragraph.add(new Chunk(cases.get(i).getSubName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_PROC:
						paragraph.add(new Chunk(cases.get(i).getProcName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_SPEC:
						paragraph.add(new Chunk(cases.get(i).getSpecName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_VAL1:
						paragraph.add(new Chunk(application.numbers.formatDouble(3, cases.get(i).getValue1())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_VAL2:
						paragraph.add(new Chunk(application.numbers.formatDouble(3, cases.get(i).getValue2())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_VAL3:
						paragraph.add(new Chunk(application.numbers.formatDouble(3, cases.get(i).getValue3())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_VAL4:
						paragraph.add(new Chunk(application.numbers.formatDouble(3, cases.get(i).getValue4())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_VAL5:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getValue5() / 60)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSP:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoSpecs())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOBL:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoBlocks())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSL:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoSlides())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOHE:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoHE())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSS:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoSS())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOIH:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoIHC())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOMO:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoMol())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOSY:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoSynop())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_NOFS:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getNoFSSpec())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ACED:
						paragraph.add(new Chunk(application.dates.formatter(cases.get(i).getAccessCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_GRED:
						paragraph.add(new Chunk(application.dates.formatter(cases.get(i).getGrossCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_EMED:
						paragraph.add(new Chunk(application.dates.formatter(cases.get(i).getEmbedCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_MIED:
						paragraph.add(new Chunk(application.dates.formatter(cases.get(i).getMicroCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROED:
						paragraph.add(new Chunk(application.dates.formatter(cases.get(i).getRouteCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_FIED:
						paragraph.add(new Chunk(application.dates.formatter(cases.get(i).getFinalCalendar(), LibDates.FORMAT_DATETIME)));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_GRBY:
						paragraph.add(new Chunk(cases.get(i).getGrossName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_EMBY:
						paragraph.add(new Chunk(cases.get(i).getEmbedName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_MIBY:
						paragraph.add(new Chunk(cases.get(i).getMicroName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_ROBY:
						paragraph.add(new Chunk(cases.get(i).getRouteName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_FIBY:
						paragraph.add(new Chunk(cases.get(i).getFinalName()));
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						break;
					case CASE_GRTA:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getGrossTAT())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_EMTA:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getEmbedTAT())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_MITA:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getMicroTAT())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					case CASE_ROTA:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getRouteTAT())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
						paragraph.add(new Chunk(application.numbers.formatNumber(cases.get(i).getFinalTAT())));
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
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

	private void setCase() {
		if (application.isBusy())
			return;
		programmaticChange = true;
		specID = 0;
		specimens.clear();
		additionals.clear();
		orders.clear();
		frozens.clear();
		specimens = application.dbPowerJ.getSpecimens(caseID);
		additionals = application.dbPowerJ.getCaseAdditionals(caseID);
		txtComment.setText(application.dbPowerJ.getCaseComment(caseID));
		programmaticChange = false;
		modelAddl.fireTableDataChanged();
		modelFrozen.fireTableDataChanged();
		modelOrder.fireTableDataChanged();
		modelSpec.fireTableDataChanged();
	}

	@Override
	public void setFilter(short id, int value) {
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
			application.log(LibConstants.ERROR_UNEXPECTED, getName(), "Invalid filter setting");
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
		application.setBusy(true);
		// Must initialize a new instance each time
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	private void setSpecimen() {
		if (application.isBusy())
			return;
		programmaticChange = true;
		orders.clear();
		frozens.clear();
		orders = application.dbPowerJ.getSpecimenOrders(specID);
		frozens = application.dbPowerJ.getSpecimenFrozens(specID);
		programmaticChange = false;
		modelFrozen.fireTableDataChanged();
		modelOrder.fireTableDataChanged();
	}

	@Override
	void xls() {
		String fileName = application.getFileXls("finals.xls").trim();
		if (fileName.length() == 0)
			return;
		try {
			Workbook wb = new HSSFWorkbook();
			LibExcel xlsLib = new LibExcel(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			Sheet sheet = wb.createSheet("Finals");
			// title row
			Row xlsRow = sheet.createRow(0);
			xlsRow.setHeightInPoints(45);
			Cell xlsCell = xlsRow.createCell(0);
			xlsCell.setCellValue("Finals - " + application.dates.formatter(Calendar.getInstance(), LibDates.FORMAT_DATETIME));
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
						xlsCell.setCellValue(cases.get(i).getCaseNo());
						break;
					case CASE_FAC:
						xlsCell.setCellValue(cases.get(i).getFacName());
						break;
					case CASE_SPY:
						xlsCell.setCellValue(cases.get(i).getSpecName());
						break;
					case CASE_SUB:
						xlsCell.setCellValue(cases.get(i).getSubName());
						break;
					case CASE_PROC:
						xlsCell.setCellValue(cases.get(i).getProcName());
						break;
					case CASE_SPEC:
						xlsCell.setCellValue(cases.get(i).getSpecName());
						break;
					case CASE_VAL1:
						xlsCell.setCellValue(cases.get(i).getValue1());
						break;
					case CASE_VAL2:
						xlsCell.setCellValue(cases.get(i).getValue2());
						break;
					case CASE_VAL3:
						xlsCell.setCellValue(cases.get(i).getValue3());
						break;
					case CASE_VAL4:
						xlsCell.setCellValue(cases.get(i).getValue4());
						break;
					case CASE_VAL5:
						xlsCell.setCellValue(cases.get(i).getValue5() / 60);
						break;
					case CASE_NOSP:
						xlsCell.setCellValue(cases.get(i).getNoSpecs());
						break;
					case CASE_NOBL:
						xlsCell.setCellValue(cases.get(i).getNoBlocks());
						break;
					case CASE_NOSL:
						xlsCell.setCellValue(cases.get(i).getNoSlides());
						break;
					case CASE_NOHE:
						xlsCell.setCellValue(cases.get(i).getNoHE());
						break;
					case CASE_NOSS:
						xlsCell.setCellValue(cases.get(i).getNoSS());
						break;
					case CASE_NOIH:
						xlsCell.setCellValue(cases.get(i).getNoIHC());
						break;
					case CASE_NOMO:
						xlsCell.setCellValue(cases.get(i).getNoMol());
						break;
					case CASE_NOSY:
						xlsCell.setCellValue(cases.get(i).getNoSynop());
						break;
					case CASE_NOFS:
						xlsCell.setCellValue(cases.get(i).getNoFSSpec());
						break;
					case CASE_ACED:
						xlsCell.setCellValue(cases.get(i).getAccessCalendar());
						break;
					case CASE_GRED:
						xlsCell.setCellValue(cases.get(i).getGrossCalendar());
						break;
					case CASE_EMED:
						xlsCell.setCellValue(cases.get(i).getEmbedCalendar());
						break;
					case CASE_MIED:
						xlsCell.setCellValue(cases.get(i).getMicroCalendar());
						break;
					case CASE_ROED:
						xlsCell.setCellValue(cases.get(i).getRouteCalendar());
						break;
					case CASE_FIED:
						xlsCell.setCellValue(cases.get(i).getFinalCalendar());
						break;
					case CASE_GRBY:
						xlsCell.setCellValue(cases.get(i).getGrossName());
						break;
					case CASE_EMBY:
						xlsCell.setCellValue(cases.get(i).getEmbedName());
						break;
					case CASE_MIBY:
						xlsCell.setCellValue(cases.get(i).getMicroName());
						break;
					case CASE_ROBY:
						xlsCell.setCellValue(cases.get(i).getRouteName());
						break;
					case CASE_FIBY:
						xlsCell.setCellValue(cases.get(i).getFinalName());
						break;
					case CASE_GRTA:
						xlsCell.setCellValue(cases.get(i).getGrossTAT());
						break;
					case CASE_EMTA:
						xlsCell.setCellValue(cases.get(i).getEmbedTAT());
						break;
					case CASE_MITA:
						xlsCell.setCellValue(cases.get(i).getMicroTAT());
						break;
					case CASE_ROTA:
						xlsCell.setCellValue(cases.get(i).getRouteTAT());
						break;
					default:
						xlsCell.setCellValue(cases.get(i).getFinalTAT());
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
					value = additionals.get(row).getCode();
					break;
				case ADDL_STAF:
					value = additionals.get(row).getFinalName();
					break;
				case ADDL_DATE:
					value = additionals.get(row).getFinaled();
					break;
				case ADDL_VAL1:
					value = additionals.get(row).getValue1();
					break;
				case ADDL_VAL2:
					value = additionals.get(row).getValue2();
					break;
				case ADDL_VAL3:
					value = additionals.get(row).getValue3();
					break;
				case ADDL_VAL4:
					value = additionals.get(row).getValue4();
					break;
				default:
					value = additionals.get(row).getValue5() / 60;
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
					value = cases.get(row).getCaseNo();
					break;
				case CASE_FAC:
					value = cases.get(row).getFacName();
					break;
				case CASE_SPY:
					value = cases.get(row).getSpyName();
					break;
				case CASE_SUB:
					value = cases.get(row).getSubName();
					break;
				case CASE_PROC:
					value = cases.get(row).getProcName();
					break;
				case CASE_SPEC:
					value = cases.get(row).getSpecName();
					break;
				case CASE_VAL1:
					value = cases.get(row).getValue1();
					break;
				case CASE_VAL2:
					value = cases.get(row).getValue2();
					break;
				case CASE_VAL3:
					value = cases.get(row).getValue3();
					break;
				case CASE_VAL4:
					value = cases.get(row).getValue4();
					break;
				case CASE_VAL5:
					value = cases.get(row).getValue5() / 60;
					break;
				case CASE_NOSP:
					value = cases.get(row).getNoSpecs();
					break;
				case CASE_NOBL:
					value = cases.get(row).getNoBlocks();
					break;
				case CASE_NOSL:
					value = cases.get(row).getNoSlides();
					break;
				case CASE_NOHE:
					value = cases.get(row).getNoHE();
					break;
				case CASE_NOSS:
					value = cases.get(row).getNoSS();
					break;
				case CASE_NOIH:
					value = cases.get(row).getNoIHC();
					break;
				case CASE_NOMO:
					value = cases.get(row).getNoMol();
					break;
				case CASE_NOSY:
					value = cases.get(row).getNoSynop();
					break;
				case CASE_NOFS:
					value = cases.get(row).getNoFSSpec();
					break;
				case CASE_ACED:
					value = cases.get(row).getAccessCalendar();
					break;
				case CASE_GRED:
					value = cases.get(row).getGrossCalendar();
					break;
				case CASE_EMED:
					value = cases.get(row).getEmbedCalendar();
					break;
				case CASE_MIED:
					value = cases.get(row).getMicroCalendar();
					break;
				case CASE_ROED:
					value = cases.get(row).getRouteCalendar();
					break;
				case CASE_FIED:
					value = cases.get(row).getFinalCalendar();
					break;
				case CASE_GRBY:
					value = cases.get(row).getGrossName();
					break;
				case CASE_EMBY:
					value = cases.get(row).getEmbedName();
					break;
				case CASE_MIBY:
					value = cases.get(row).getMicroName();
					break;
				case CASE_ROBY:
					value = cases.get(row).getRouteName();
					break;
				case CASE_FIBY:
					value = cases.get(row).getFinalName();
					break;
				case CASE_GRTA:
					value = cases.get(row).getGrossTAT();
					break;
				case CASE_EMTA:
					value = cases.get(row).getEmbedTAT();
					break;
				case CASE_MITA:
					value = cases.get(row).getMicroTAT();
					break;
				case CASE_ROTA:
					value = cases.get(row).getRouteTAT();
					break;
				default:
					value = cases.get(row).getFinalTAT();
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
				return ItemData.class;
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
					value = frozens.get(row).getFinalBy();
					break;
				case FRZN_NOBL:
					value = frozens.get(row).getNoBlocks();
					break;
				case FRZN_NOSL:
					value = frozens.get(row).getNoSlides();
					break;
				case ADDL_VAL1:
					value = frozens.get(row).getValue1();
					break;
				case ADDL_VAL2:
					value = frozens.get(row).getValue2();
					break;
				case ADDL_VAL3:
					value = frozens.get(row).getValue3();
					break;
				case ADDL_VAL4:
					value = frozens.get(row).getValue4();
					break;
				default:
					value = frozens.get(row).getValue5() / 60;
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
					value = orders.get(row).getName();
					break;
				case ORDER_QTY:
					value = orders.get(row).getQty();
					break;
				case ORDER_VAL1:
					value = orders.get(row).getValue1();
					break;
				case ORDER_VAL2:
					value = orders.get(row).getValue2();
					break;
				case ORDER_VAL3:
					value = orders.get(row).getValue3();
					break;
				case ORDER_VAL4:
					value = orders.get(row).getValue4();
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
					value = specimens.get(row).getName();
					break;
				case SPEC_NOBL:
					value = specimens.get(row).getNoBlocks();
					break;
				case SPEC_NOSL:
					value = specimens.get(row).getNoSlides();
					break;
				case SPEC_NOHE:
					value = specimens.get(row).getNoHE();
					break;
				case SPEC_NOSS:
					value = specimens.get(row).getNoSS();
					break;
				case SPEC_NOIH:
					value = specimens.get(row).getNoIHC();
					break;
				case SPEC_NOMO:
					value = specimens.get(row).getNoMOL();
					break;
				case SPEC_NOFR:
					value = specimens.get(row).getNoFrags();
					break;
				case SPEC_VAL1:
					value = specimens.get(row).getValue1();
					break;
				case SPEC_VAL2:
					value = specimens.get(row).getValue2();
					break;
				case SPEC_VAL3:
					value = specimens.get(row).getValue3();
					break;
				case SPEC_VAL4:
					value = specimens.get(row).getValue4();
					break;
				default:
					value = specimens.get(row).getValue5() / 60;
				}
			}
			return value;
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			final String[] fields = { " AND faid = ", " AND syid = ", " AND sbid = ", " AND poid = " };
			String filter = "";
			setName("WorkerData");
			for (int i = 0; i < filters.length; i++) {
				if (filters[i] > 0) {
					filter += fields[i] + filters[i];
				}
			}
			if (filter.length() > 0) {
				filter = " WHERE " + filter.substring(5);
			}
			cases = application.dbPowerJ.getCases(filter);
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
			application.display("No Rows: " + application.numbers.formatNumber(tblCase.getRowCount()));
			application.setBusy(false);
			programmaticChange = false;
		}
	}
}