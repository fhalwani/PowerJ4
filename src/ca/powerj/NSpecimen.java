package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

class NSpecimen extends NBase {
	private final byte DATA_NAME = 0;
	private final byte DATA_COUNT = 1;
	private final byte DATA_BLOCKS = 2;
	private final byte DATA_SLIDES = 3;
	private final byte DATA_HE = 4;
	private final byte DATA_SS = 5;
	private final byte DATA_IHC = 6;
	private final byte DATA_VALUE1 = 7;
	private final byte DATA_VALUE2 = 8;
	private final byte DATA_VALUE3 = 9;
	private final byte DATA_VALUE4 = 10;
	private final byte DATA_VALUE5 = 11;
	private short facID = 0;
	private long timeFrom = 0;
	private long timeTo = 0;
	private String[] columns = { "Name", "Count", "Blocks", "Slides", "H&E", "SS", "IHC", "", "", "", "", "" };
	private TreePath treePath;
	private ITreeTable tree;
	private ModelSpecimen model;
	private IChartPie chartCoder1, chartCoder2, chartCoder3, chartCoder4, chartCoder5;

	NSpecimen(AClient parent) {
		super(parent);
		setName("Specimens");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_SPECIMEN);
		columns[7] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		columns[8] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		columns[9] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		columns[10] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		columns[11] = pj.setup.getString(LSetup.VAR_V5_NAME);
		createPanel();
		programmaticChange = false;
	}

	@Override
	boolean close() {
		super.close();
		if (chartCoder1 != null) {
			chartCoder1.close();
		}
		if (chartCoder2 != null) {
			chartCoder2.close();
		}
		if (chartCoder3 != null) {
			chartCoder3.close();
		}
		if (chartCoder4 != null) {
			chartCoder4.close();
		}
		if (chartCoder5 != null) {
			chartCoder5.close();
		}
		return true;
	}

	private void createPanel() {
		OSpecnode root = new OSpecnode("Total");
		model = new ModelSpecimen(root);
		tree = new ITreeTable(pj, model);
		tree.setFocusable(true);
		tree.tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setCharts(e.getNewLeadSelectionPath());
					}
				});
			}
		});
		JScrollPane scrollTree = IGUI.createJScrollPane(tree);
		scrollTree.setMinimumSize(new Dimension(1000, 600));
		chartCoder1 = new IChartPie(new Dimension(200, 200));
		chartCoder2 = new IChartPie(new Dimension(200, 200));
		chartCoder3 = new IChartPie(new Dimension(200, 200));
		chartCoder4 = new IChartPie(new Dimension(200, 200));
		chartCoder5 = new IChartPie(new Dimension(200, 200));
		JPanel pnlCharts = new JPanel();
		pnlCharts.setMinimumSize(new Dimension(1000, 200));
		pnlCharts.setLayout(new BoxLayout(pnlCharts, BoxLayout.X_AXIS));
		pnlCharts.setOpaque(true);
		pnlCharts.add(chartCoder1);
		pnlCharts.add(chartCoder2);
		pnlCharts.add(chartCoder3);
		pnlCharts.add(chartCoder4);
		pnlCharts.add(chartCoder5);
		JScrollPane scrollCharts = IGUI.createJScrollPane(pnlCharts);
		scrollCharts.setMinimumSize(new Dimension(1000, 200));
		JSplitPane splitAll = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitAll.setTopComponent(scrollCharts);
		splitAll.setBottomComponent(scrollTree);
		splitAll.setOneTouchExpandable(true);
		splitAll.setDividerLocation(250);
		splitAll.setMinimumSize(new Dimension(1000, 900));
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = pj.dates.setMidnight(null);
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calMax.set(Calendar.DAY_OF_MONTH, 1);
		timeFrom = pj.setup.getLong(LSetup.VAR_MIN_WL_DATE);
		timeTo = calMax.getTimeInMillis();
		calMin.setTimeInMillis(timeFrom);
		calStart.setTimeInMillis(calMin.getTimeInMillis());
		calEnd.setTimeInMillis(calMax.getTimeInMillis());
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this, calStart, calEnd, calMin, calMax, true), BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	private void setCharts(TreePath newPath) {
		if (newPath == null)
			return;
		if (treePath == null || !treePath.equals(newPath)) {
			treePath = newPath;
			OSpecnode node = (OSpecnode) treePath.getPathComponent(treePath.getPathCount() - 1);
			if (node != null && node.children.length > 0) {
				int count = node.children.length;
				String[] xTitles = new String[count];
				double[] yCoder1 = new double[count];
				double[] yCoder2 = new double[count];
				double[] yCoder3 = new double[count];
				double[] yCoder4 = new double[count];
				double[] yCoder5 = new double[count];
				for (int i = 0; i < count; i++) {
					OWorknode leaf = (OWorknode) node.children[i];
					xTitles[i] = leaf.name;
					yCoder1[i] = leaf.fte1;
					yCoder2[i] = leaf.fte2;
					yCoder3[i] = leaf.fte3;
					yCoder4[i] = leaf.fte4;
					yCoder5[i] = leaf.fte5;
				}
				chartCoder1.setChart(xTitles, yCoder1, columns[7], IChartPie.COLOR_DEF);
				chartCoder2.setChart(xTitles, yCoder2, columns[8], IChartPie.COLOR_DEF);
				chartCoder3.setChart(xTitles, yCoder3, columns[9], IChartPie.COLOR_DEF);
				chartCoder4.setChart(xTitles, yCoder4, columns[10], IChartPie.COLOR_DEF);
				chartCoder5.setChart(xTitles, yCoder5, columns[11], IChartPie.COLOR_DEF);
			}
		}
		altered = false;
	}

	@Override
	void setFilter(short id, short value) {
		switch (id) {
		case IToolBar.TB_FAC:
			facID = value;
			altered = true;
			break;
		default:
			if (altered && timeTo > timeFrom) {
				WorkerData worker = new WorkerData();
				worker.execute();
			}
		}
	}

	@Override
	void setFilter(short id, Calendar value) {
		switch (id) {
		case IToolBar.TB_FROM:
			timeFrom = value.getTimeInMillis();
			break;
		default:
			timeTo = value.getTimeInMillis();
		}
		altered = true;
	}

	private class ModelSpecimen extends ITreeTableModel implements ITreeModel {

		private final Class<?>[] types = { ITreeModel.class, Integer.class, Integer.class, Integer.class, Integer.class,
				Integer.class, Integer.class, Double.class, Double.class, Double.class, Double.class, Double.class };

		public ModelSpecimen(Object nodeRoot) {
			super(nodeRoot);
		}

		@Override
		public Object getChild(Object node, int element) {
			return ((OSpecnode) node).children[element];
		}

		@Override
		public int getChildCount(Object node) {
			Object[] children = getChildren(node);
			return (children == null) ? 0 : children.length;
		}

		@Override
		protected Object[] getChildren(Object node) {
			return ((OSpecnode) node).children;
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return types[column];
		}

		@Override
		public Object getValueAt(Object node, int column) {
			OSpecnode data = (OSpecnode) node;
			switch (column) {
			case DATA_NAME:
				return data.name;
			case DATA_COUNT:
				return data.noSpecs;
			case DATA_BLOCKS:
				return data.noBlocks;
			case DATA_SLIDES:
				return data.noSlides;
			case DATA_HE:
				return data.noHE;
			case DATA_SS:
				return data.noSS;
			case DATA_IHC:
				return data.noIHC;
			case DATA_VALUE1:
				return data.fte1;
			case DATA_VALUE2:
				return data.fte2;
			case DATA_VALUE3:
				return data.fte3;
			case DATA_VALUE4:
				return data.fte4;
			case DATA_VALUE5:
				return data.fte5;
			default:
				return null;
			}
		}
	}

	private class WorkerData extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			byte spyID = 0;
			byte subID = 0;
			short spgID = 0;
			short facID2 = 0;
			int noDays = pj.dates.getNoDays(timeFrom, timeTo);
			double fte1 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER1_FTE) / 365;
			double fte2 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER2_FTE) / 365;
			double fte3 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER3_FTE) / 365;
			double fte4 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER4_FTE) / 365;
			double fte5 = 1.0 * noDays * pj.setup.getInt(LSetup.VAR_V5_FTE) / 365;
			OSpecimen specTotal = new OSpecimen();
			OSpecimen specSpeci = new OSpecimen();
			OSpecimen specSubsp = new OSpecimen();
			OSpecimen specGroup = new OSpecimen();
			OSpecimen specFacil = new OSpecimen();
			OSpecimen specPersn = new OSpecimen();
			ResultSet rst = null;
			try {
				setName("SpecWorker");
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SPG_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_SPG_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_SPG_SL_SUM));
				specTotal.name = "Total";
				while (rst.next()) {
					if (facID == 0 || facID == rst.getShort("FAID")) {
						if (spyID != rst.getByte("SYID")) {
							if (specSpeci.children.size() > 0) {
								specTotal.children.add(specSpeci);
							}
							subID = 0;
							spgID = 0;
							facID2 = 0;
							spyID = rst.getByte("SYID");
							specSpeci = new OSpecimen();
							specSpeci.name = rst.getString("SYNM").trim();
						}
						if (subID != rst.getByte("SBID")) {
							if (specSubsp.children.size() > 0) {
								specSpeci.children.add(specSubsp);
							}
							spgID = 0;
							facID2 = 0;
							subID = rst.getByte("SBID");
							specSubsp = new OSpecimen();
							specSubsp.name = rst.getString("SBNM").trim();
						}
						if (spgID != rst.getShort("SGID")) {
							if (specGroup.children.size() > 0) {
								specSubsp.children.add(specGroup);
							}
							facID2 = 0;
							spgID = rst.getShort("SGID");
							specGroup = new OSpecimen();
							specGroup.name = rst.getString("SGDC").trim();
						}
						if (facID > 0 && facID2 != rst.getShort("FAID")) {
							if (specFacil.children.size() > 0) {
								specGroup.children.add(specFacil);
							}
							facID2 = rst.getShort("FAID");
							specFacil = new OSpecimen();
							specFacil.name = rst.getString("FANM").trim();
						}
						specPersn = new OSpecimen();
						specPersn.noSpecs = rst.getInt("QTY");
						specPersn.noBlocks = rst.getInt("SPBL");
						specPersn.noSlides = rst.getInt("SPSL");
						specPersn.noHE = rst.getInt("SPHE");
						specPersn.noSS = rst.getInt("SPSS");
						specPersn.noIHC = rst.getInt("SPIH");
						specPersn.fte1 = rst.getDouble("SPV1");
						specPersn.fte2 = rst.getDouble("SPV2");
						specPersn.fte3 = rst.getDouble("SPV3");
						specPersn.fte4 = rst.getDouble("SPV4");
						specPersn.fte5 = rst.getDouble("SPV5");
						if (pj.userAccess[LConstants.ACCESS_NAMES] || pj.userID == rst.getShort("PRID")) {
							specPersn.name = rst.getString("PRNM").trim();
						} else if (facID > 0) {
							specPersn.name = "PA" + (specGroup.children.size() + 1);
						} else {
							specPersn.name = "PA" + (specFacil.children.size() + 1);
						}
						if (facID > 0) {
							specGroup.children.add(specPersn);
						} else {
							specFacil.children.add(specPersn);
							specFacil.noSpecs += specPersn.noSpecs;
							specFacil.noBlocks += specPersn.noBlocks;
							specFacil.noSlides += specPersn.noSlides;
							specFacil.noHE += specPersn.noHE;
							specFacil.noSS += specPersn.noSS;
							specFacil.noIHC += specPersn.noIHC;
							specFacil.fte1 += specPersn.fte1;
							specFacil.fte2 += specPersn.fte2;
							specFacil.fte3 += specPersn.fte3;
							specFacil.fte4 += specPersn.fte4;
							specFacil.fte5 += specPersn.fte5;
						}
						specGroup.noSpecs += specPersn.noSpecs;
						specGroup.noBlocks += specPersn.noBlocks;
						specGroup.noSlides += specPersn.noSlides;
						specGroup.noHE += specPersn.noHE;
						specGroup.noSS += specPersn.noSS;
						specGroup.noIHC += specPersn.noIHC;
						specGroup.fte1 += specPersn.fte1;
						specGroup.fte2 += specPersn.fte2;
						specGroup.fte3 += specPersn.fte3;
						specGroup.fte4 += specPersn.fte4;
						specGroup.fte5 += specPersn.fte5;
						specSubsp.noSpecs += specPersn.noSpecs;
						specSubsp.noBlocks += specPersn.noBlocks;
						specSubsp.noSlides += specPersn.noSlides;
						specSubsp.noHE += specPersn.noHE;
						specSubsp.noSS += specPersn.noSS;
						specSubsp.noIHC += specPersn.noIHC;
						specSubsp.fte1 += specPersn.fte1;
						specSubsp.fte2 += specPersn.fte2;
						specSubsp.fte3 += specPersn.fte3;
						specSubsp.fte4 += specPersn.fte4;
						specSubsp.fte5 += specPersn.fte5;
						specSpeci.noSpecs += specPersn.noSpecs;
						specSpeci.noBlocks += specPersn.noBlocks;
						specSpeci.noSlides += specPersn.noSlides;
						specSpeci.noHE += specPersn.noHE;
						specSpeci.noSS += specPersn.noSS;
						specSpeci.noIHC += specPersn.noIHC;
						specSpeci.fte1 += specPersn.fte1;
						specSpeci.fte2 += specPersn.fte2;
						specSpeci.fte3 += specPersn.fte3;
						specSpeci.fte4 += specPersn.fte4;
						specSpeci.fte5 += specPersn.fte5;
					}
				}
				try {
					Thread.sleep(LConstants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				if (fte1 == 0) {
					fte1 = 1.0;
				}
				if (fte2 == 0) {
					fte2 = 1.0;
				}
				if (fte3 == 0) {
					fte3 = 1.0;
				}
				if (fte4 == 0) {
					fte4 = 1.0;
				}
				if (fte5 == 0) {
					fte5 = 1.0;
				}
				specTotal.fte1 = specTotal.fte1 / fte1;
				specTotal.fte2 = specTotal.fte2 / fte2;
				specTotal.fte3 = specTotal.fte3 / fte3;
				specTotal.fte4 = specTotal.fte4 / fte4;
				specTotal.fte5 = specTotal.fte5 / fte5;
				setTotals(specTotal, fte1, fte2, fte3, fte4, fte5);
				OSpecnode nodeTotal = (OSpecnode) model.getRoot();
				setNodes(specTotal, nodeTotal);
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
			return null;
		}

		private void setTotals(OSpecimen master, double fte1, double fte2, double fte3, double fte4, double fte5) {
			OSpecimen child = new OSpecimen();
			for (int i = 0; i < master.children.size(); i++) {
				child = master.children.get(i);
				child.fte1 = child.fte1 / fte1;
				child.fte2 = child.fte2 / fte2;
				child.fte3 = child.fte3 / fte3;
				child.fte4 = child.fte4 / fte4;
				child.fte5 = child.fte5 / fte5;
				if (child.children.size() > 0) {
					setTotals(child, fte1, fte2, fte3, fte4, fte5);
				}
			}
		}

		private void setNodes(OSpecimen specMaster, OSpecnode nodeMaster) {
			nodeMaster.name = specMaster.name;
			nodeMaster.noSpecs = specMaster.noSpecs;
			nodeMaster.noBlocks = specMaster.noBlocks;
			nodeMaster.noSlides = specMaster.noSlides;
			nodeMaster.noHE = specMaster.noHE;
			nodeMaster.noSS = specMaster.noSS;
			nodeMaster.noIHC = specMaster.noIHC;
			nodeMaster.children = new OSpecnode[specMaster.children.size()];
			for (int i = 0; i < specMaster.children.size(); i++) {
				setNodes(specMaster.children.get(i), (OSpecnode) nodeMaster.children[i]);
			}
		}

		@Override
		public void done() {
			altered = false;
			// Display results
			OSpecnode root = (OSpecnode) model.getRoot();
			model = new ModelSpecimen(root);
			tree.setTreeModel(model);
			setCharts(tree.getTree().getPathForRow(0));
			pj.setBusy(false);
		}
	}
}