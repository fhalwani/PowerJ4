package ca.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

class NWorkload extends NBase {
	private final byte DATA_NAMES = 0;
	private final byte DATA_CASES = 1;
	private final byte DATA_SPECS = 2;
	private final byte DATA_BLOCKS = 3;
	private final byte DATA_SLIDES = 4;
	private final byte DATA_VALUE1 = 5;
	private final byte DATA_VALUE2 = 6;
	private final byte DATA_VALUE3 = 7;
	private final byte DATA_VALUE4 = 8;
	private final byte DATA_VALUE5 = 9;
	private boolean alteredDate = false;
	private boolean alteredRows = false;
	private byte[] rowsView = new byte[5];
	private long timeFrom = 0;
	private long timeTo = 0;
	private String[] coders = new String[5];
	private TreePath treePath;
	private ITreeTable tree;
	private ModelWorkload model;
	private ArrayList<OWorkload> rows = new ArrayList<OWorkload>();
	private IChartPie chartCoder1, chartCoder2, chartCoder3, chartCoder4, chartCoder5;

	NWorkload(AClient parent) {
		super(parent);
		setName("Workload");
		pjStms = parent.dbPowerJ.prepareStatements(LConstants.ACTION_DISTRIBUTE);
		setDefaults();
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
		OWorknode root = new OWorknode("Total");
		model = new ModelWorkload(root);
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
		Dimension dim = new Dimension(200, 200);
		chartCoder1 = new IChartPie(dim);
		chartCoder2 = new IChartPie(dim);
		chartCoder3 = new IChartPie(dim);
		chartCoder4 = new IChartPie(dim);
		chartCoder5 = new IChartPie(dim);
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
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = Calendar.getInstance();
		calStart.setTimeInMillis(timeFrom);
		calEnd.setTimeInMillis(timeTo);
		calMax.setTimeInMillis(timeTo);
		calMin.setTimeInMillis(pj.setup.getLong(LSetup.VAR_MIN_WL_DATE));
		setLayout(new BorderLayout());
		setOpaque(true);
		add(new IToolBar(this, calStart, calEnd, calMin, calMax, pj.userAccess[LConstants.ACCESS_NAMES]),
				BorderLayout.NORTH);
		add(splitAll, BorderLayout.CENTER);
	}

	private void setCharts(TreePath newPath) {
		if (newPath == null)
			return;
		if (treePath == null || !treePath.equals(newPath)) {
			treePath = newPath;
			OWorknode node = (OWorknode) treePath.getPathComponent(treePath.getPathCount() - 1);
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
				chartCoder1.setChart(xTitles, yCoder1, coders[0], IChartPie.COLOR_DEF);
				chartCoder2.setChart(xTitles, yCoder2, coders[1], IChartPie.COLOR_DEF);
				chartCoder3.setChart(xTitles, yCoder3, coders[2], IChartPie.COLOR_DEF);
				chartCoder4.setChart(xTitles, yCoder4, coders[3], IChartPie.COLOR_DEF);
				chartCoder5.setChart(xTitles, yCoder5, coders[4], IChartPie.COLOR_DEF);
			}
		}
		alteredRows = false;
	}

	private void setDefaults() {
		coders[0] = pj.setup.getString(LSetup.VAR_CODER1_NAME);
		coders[1] = pj.setup.getString(LSetup.VAR_CODER2_NAME);
		coders[2] = pj.setup.getString(LSetup.VAR_CODER3_NAME);
		coders[3] = pj.setup.getString(LSetup.VAR_CODER4_NAME);
		coders[4] = pj.setup.getString(LSetup.VAR_V5_NAME);
		if (pj.userAccess[LConstants.ACCESS_NAMES]) {
			rowsView[0] = IPanelRows.ROW_FACILITY;
			rowsView[1] = IPanelRows.ROW_SPECIALTY;
			rowsView[2] = IPanelRows.ROW_SUBSPECIAL;
			rowsView[3] = IPanelRows.ROW_PROCEDURE;
			rowsView[4] = IPanelRows.ROW_STAFF;
		} else {
			rowsView[0] = IPanelRows.ROW_STAFF;
			rowsView[1] = IPanelRows.ROW_FACILITY;
			rowsView[2] = IPanelRows.ROW_SPECIALTY;
			rowsView[3] = IPanelRows.ROW_SUBSPECIAL;
			rowsView[4] = IPanelRows.ROW_PROCEDURE;
		}
		Calendar calStart = pj.dates.setMidnight(null);
		Calendar calEnd = pj.dates.setMidnight(null);
		calStart.add(Calendar.YEAR, -1);
		timeFrom = calStart.getTimeInMillis();
		timeTo = calEnd.getTimeInMillis();
		WorkerData worker = new WorkerData();
		worker.execute();
	}

	@Override
	void setFilter(byte[] rows) {
		for (int i = 0; i < rowsView.length; i++) {
			rowsView[i] = rows[i];
		}
	}

	@Override
	void setFilter(short id, short value) {
		if (timeTo > timeFrom) {
			WorkerData worker = new WorkerData();
			worker.execute();
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
	}

	private class ModelWorkload extends ITreeTableModel implements ITreeModel {

		private final String[] headers = { "Name", "Cases", "Specs", "Blocks", "Slides", coders[0], coders[1],
				coders[2], coders[3], coders[4] };

		private final Class<?>[] types = { ITreeModel.class, Integer.class, Integer.class, Integer.class, Integer.class,
				Double.class, Double.class, Double.class, Double.class, Double.class };

		public ModelWorkload(Object nodeRoot) {
			super(nodeRoot);
		}

		@Override
		public Object getChild(Object node, int element) {
			return ((OWorknode) node).children[element];
		}

		@Override
		public int getChildCount(Object node) {
			Object[] children = getChildren(node);
			return (children == null) ? 0 : children.length;
		}

		@Override
		protected Object[] getChildren(Object node) {
			return ((OWorknode) node).children;
		}

		@Override
		public int getColumnCount() {
			return headers.length;
		}

		@Override
		public String getColumnName(int column) {
			return headers[column];
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return types[column];
		}

		@Override
		public Object getValueAt(Object node, int column) {
			OWorknode data = (OWorknode) node;
			switch (column) {
			case DATA_NAMES:
				return data.name;
			case DATA_CASES:
				return data.noCases;
			case DATA_SPECS:
				return data.noSpecs;
			case DATA_BLOCKS:
				return data.noBlocks;
			case DATA_SLIDES:
				return data.noSlides;
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
		private int noRows = 0;

		@Override
		protected Void doInBackground() throws Exception {
			setName("WLWorker");
			if (alteredDate) {
				getData();
			} else if (alteredRows) {
				structureData();
			}
			return null;
		}

		@Override
		public void done() {
			alteredDate = false;
			// Display results
			OWorknode root = (OWorknode) model.getRoot();
			model = new ModelWorkload(root);
			tree.setTreeModel(model);
			if (alteredRows) {
				setCharts(tree.getTree().getPathForRow(0));
			}
			pj.statusBar.setMessage("Workload " + pj.dates.formatter(timeFrom, LDates.FORMAT_DATELONG) + " - "
					+ pj.dates.formatter(timeTo, LDates.FORMAT_DATELONG));
			pj.setBusy(false);
		}

		private void getData() {
			boolean exists = false;
			OWorkload row = new OWorkload();
			ResultSet rst = null;
			try {
				rows.clear();
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_CSE_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_CSE_SL_SUM));
				while (rst.next()) {
					row = new OWorkload();
					row.spyID = rst.getByte("SYID");
					row.subID = rst.getByte("SBID");
					row.proID = rst.getByte("POID");
					row.facID = rst.getShort("FAID");
					row.prsID = rst.getShort("FNID");
					row.noCases = rst.getInt("CACA");
					row.noSpecs = rst.getInt("CASP");
					row.noBlocks = rst.getInt("CABL");
					row.noSlides = rst.getInt("CASL");
					row.fte1 = rst.getDouble("CAV1");
					row.fte2 = rst.getDouble("CAV2");
					row.fte3 = rst.getDouble("CAV3");
					row.fte4 = rst.getDouble("CAV4");
					row.fte5 = rst.getDouble("CAV5");
					row.facility = rst.getString("FANM").trim();
					row.specialty = rst.getString("SYNM").trim();
					row.subspecial = rst.getString("SBNM").trim();
					row.procedure = rst.getString("PONM").trim();
					if (pj.userAccess[LConstants.ACCESS_NAMES] || row.prsID == pj.userID) {
						// Hide names except the current user
						row.staff = rst.getString("FNNM").trim();
					}
					rows.add(row);
				}
				rst.close();
				// Frozen Sections
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_FRZ_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_FRZ_SL_SUM));
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).facID == rst.getShort("FAID") && rows.get(i).spyID == rst.getByte("SYID")
								&& rows.get(i).subID == rst.getByte("SBID") && rows.get(i).proID == rst.getByte("POID")
								&& rows.get(i).prsID == rst.getShort("PRID")) {
							row = rows.get(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						row = new OWorkload();
						row.spyID = rst.getByte("SYID");
						row.subID = rst.getByte("SBID");
						row.proID = rst.getByte("POID");
						row.facID = rst.getShort("FAID");
						row.prsID = rst.getShort("PRID");
						row.facility = rst.getString("FANM").trim();
						row.specialty = rst.getString("SYNM").trim();
						row.subspecial = rst.getString("SBNM").trim();
						row.procedure = rst.getString("PONM").trim();
						if (pj.userAccess[LConstants.ACCESS_NAMES] || row.prsID == pj.userID) {
							row.staff = rst.getString("PRNM").trim();
						}
						rows.add(row);
					}
					row.noSpecs += rst.getInt("FRSP");
					row.noBlocks += rst.getInt("FRBL");
					row.noSlides += rst.getInt("FRSL");
					row.fte1 += rst.getDouble("FRV1");
					row.fte2 += rst.getDouble("FRV2");
					row.fte3 += rst.getDouble("FRV3");
					row.fte4 += rst.getDouble("FRV4");
					row.fte5 += rst.getDouble("FRV5");
				}
				rst.close();
				// Additional
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_SUM), 1, timeFrom);
				pj.dbPowerJ.setDate(pjStms.get(DPowerJ.STM_ADD_SL_SUM), 2, timeTo);
				rst = pj.dbPowerJ.getResultSet(pjStms.get(DPowerJ.STM_ADD_SL_SUM));
				while (rst.next()) {
					exists = false;
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).facID == rst.getShort("FAID") && rows.get(i).spyID == rst.getByte("SYID")
								&& rows.get(i).subID == rst.getByte("SBID") && rows.get(i).proID == rst.getByte("POID")
								&& rows.get(i).prsID == rst.getShort("PRID")) {
							row = rows.get(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						row = new OWorkload();
						row.spyID = rst.getByte("SYID");
						row.subID = rst.getByte("SBID");
						row.proID = rst.getByte("POID");
						row.facID = rst.getShort("FAID");
						row.prsID = rst.getShort("PRID");
						row.facility = rst.getString("FANM").trim();
						row.specialty = rst.getString("SYNM").trim();
						row.subspecial = rst.getString("SBNM").trim();
						row.procedure = rst.getString("PONM").trim();
						if (pj.userAccess[LConstants.ACCESS_NAMES] || row.prsID == pj.userID) {
							row.staff = rst.getString("PRNM").trim();
						}
						rows.add(row);
					}
					row.noCases += rst.getInt("ADCA");
					row.fte1 += rst.getDouble("ADV1");
					row.fte2 += rst.getDouble("ADV2");
					row.fte3 += rst.getDouble("ADV3");
					row.fte4 += rst.getDouble("ADV4");
					row.fte5 += rst.getDouble("ADV5");
				}
			} catch (SQLException e) {
				pj.log(LConstants.ERROR_SQL, getName(), e);
			} finally {
				pj.dbPowerJ.close(rst);
			}
		}

		private void setModel(OWorklist root) {
			OWorknode node0 = (OWorknode) model.getRoot();
			OWorknode node1 = new OWorknode("node");
			OWorknode node2 = new OWorknode("node");
			OWorknode node3 = new OWorknode("node");
			OWorknode node4 = new OWorknode("node");
			OWorknode node5 = new OWorknode("node");
			OWorklist data1 = new OWorklist();
			OWorklist data2 = new OWorklist();
			OWorklist data3 = new OWorklist();
			OWorklist data4 = new OWorklist();
			OWorklist data5 = new OWorklist();
			node0.noCases = root.noCases;
			node0.noSpecs = root.noSpecs;
			node0.noBlocks = root.noBlocks;
			node0.noSlides = root.noSlides;
			node0.fte1 = root.fte1;
			node0.fte2 = root.fte2;
			node0.fte3 = root.fte3;
			node0.fte4 = root.fte4;
			node0.fte5 = root.fte5;
			node0.children = new OWorknode[root.children.size()];
			for (int i = 0; i < root.children.size(); i++) {
				data1 = root.children.get(i);
				node1 = new OWorknode(data1.name);
				node1.noCases = data1.noCases;
				node1.noSpecs = data1.noSpecs;
				node1.noBlocks = data1.noBlocks;
				node1.noSlides = data1.noSlides;
				node1.fte1 = data1.fte1;
				node1.fte2 = data1.fte2;
				node1.fte3 = data1.fte3;
				node1.fte4 = data1.fte4;
				node1.fte5 = data1.fte5;
				node1.children = new OWorknode[data1.children.size()];
				for (int j = 0; j < data1.children.size(); j++) {
					data2 = data1.children.get(j);
					node2 = new OWorknode(data2.name);
					node2.noCases = data2.noCases;
					node2.noSpecs = data2.noSpecs;
					node2.noBlocks = data2.noBlocks;
					node2.noSlides = data2.noSlides;
					node2.fte1 = data2.fte1;
					node2.fte2 = data2.fte2;
					node2.fte3 = data2.fte3;
					node2.fte4 = data2.fte4;
					node2.fte5 = data2.fte5;
					node2.children = new OWorknode[data2.children.size()];
					for (int k = 0; k < data2.children.size(); k++) {
						data3 = data2.children.get(k);
						node3 = new OWorknode(data3.name);
						node3.noCases = data3.noCases;
						node3.noSpecs = data3.noSpecs;
						node3.noBlocks = data3.noBlocks;
						node3.noSlides = data3.noSlides;
						node3.fte1 = data3.fte1;
						node3.fte2 = data3.fte2;
						node3.fte3 = data3.fte3;
						node3.fte4 = data3.fte4;
						node3.fte5 = data3.fte5;
						node3.children = new OWorknode[data3.children.size()];
						for (int l = 0; l < data3.children.size(); l++) {
							data4 = data3.children.get(l);
							node4 = new OWorknode(data4.name);
							node4.noCases = data4.noCases;
							node4.noSpecs = data4.noSpecs;
							node4.noBlocks = data4.noBlocks;
							node4.noSlides = data4.noSlides;
							node4.fte1 = data4.fte1;
							node4.fte2 = data4.fte2;
							node4.fte3 = data4.fte3;
							node4.fte4 = data4.fte4;
							node4.fte5 = data4.fte5;
							node4.children = new OWorknode[data4.children.size()];
							for (int m = 0; m < data4.children.size(); m++) {
								data5 = data4.children.get(m);
								node5 = new OWorknode(data5.name);
								node5.noCases = data5.noCases;
								node5.noSpecs = data5.noSpecs;
								node5.noBlocks = data5.noBlocks;
								node5.noSlides = data5.noSlides;
								node5.fte1 = data5.fte1;
								node5.fte2 = data5.fte2;
								node5.fte3 = data5.fte3;
								node5.fte4 = data5.fte4;
								node5.fte5 = data5.fte5;
								node4.children[m] = node5;
							}
							data4.children.clear();
							node3.children[l] = node4;
						}
						data3.children.clear();
						node2.children[k] = node3;
					}
					data2.children.clear();
					node1.children[j] = node2;
				}
				data1.children.clear();
				node0.children[i] = node1;
			}
			root.children.clear();
		}

		private void setTotals(OWorklist master, double fte1, double fte2, double fte3, double fte4, double fte5) {
			OWorklist child = new OWorklist();
			for (int i = master.children.size() - 1; i >= 0; i--) {
				child = master.children.get(i);
				if (child.id < 0) {
					// filtered out
					master.children.remove(i);
					continue;
				}
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

		private void sortChildren(OWorklist master) {
			Collections.sort(master.children, new Comparator<OWorklist>() {
				@Override
				public int compare(OWorklist o1, OWorklist o2) {
					return (o1.noCases > o2.noCases ? -1
							: (o1.noCases < o2.noCases ? 1 : (o1.noSlides > o2.noSlides ? -1 : 1)));
				}
			});
			int nameID = 0;
			OWorklist child = new OWorklist();
			for (int i = 0; i < master.children.size(); i++) {
				child = master.children.get(i);
				if (!pj.userAccess[LConstants.ACCESS_NAMES]) {
					if (child.name.trim().length() == 0) {
						nameID = 1;
						noRows++;
						child.name = "P" + noRows;
					} else if (nameID == 1) {
						noRows++;
					}
				}
				if (child.children.size() > 0) {
					sortChildren(child);
				}
			}
		}

		private void structureData() {
			short id = 0;
			short ids[] = new short[rowsView.length];
			int rowNos[] = new int[rowsView.length];
			int size = rows.size();
			int noDays = pj.dates.getNoDays(timeFrom, timeTo);
			double fte1 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER1_FTE) / 365;
			double fte2 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER2_FTE) / 365;
			double fte3 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER3_FTE) / 365;
			double fte4 = 1.0 * noDays * pj.setup.getShort(LSetup.VAR_CODER4_FTE) / 365;
			double fte5 = 1.0 * noDays * pj.setup.getInt(LSetup.VAR_V5_FTE) / 365;
			String name = "";
			OWorklist data0 = new OWorklist();
			OWorklist data1 = new OWorklist();
			OWorklist data2 = new OWorklist();
			OWorklist data3 = new OWorklist();
			OWorklist data4 = new OWorklist();
			OWorklist data5 = new OWorklist();
			OWorkload row = new OWorkload();
			for (int i = 0; i < rowsView.length; i++) {
				ids[i] = -1;
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
			for (int x = 0; x < size; x++) {
				row = rows.get(x);
				// Match 1st node
				switch (rowsView[0]) {
				case IPanelRows.ROW_FACILITY:
					id = row.facID;
					name = row.facility;
					break;
				case IPanelRows.ROW_SPECIALTY:
					id = row.spyID;
					name = row.specialty;
					break;
				case IPanelRows.ROW_SUBSPECIAL:
					id = row.subID;
					name = row.subspecial;
					break;
				case IPanelRows.ROW_PROCEDURE:
					id = row.proID;
					name = row.procedure;
					break;
				case IPanelRows.ROW_STAFF:
					id = row.prsID;
					name = row.staff;
					break;
				default:
					id = -2;
				}
				if (ids[0] != id) {
					ids[0] = id;
					rowNos[0] = -1;
					for (int i = 1; i < rowsView.length; i++) {
						ids[i] = -1;
						rowNos[i] = -1;
					}
					for (int j = 0; j < data0.children.size(); j++) {
						data1 = data0.children.get(j);
						if (data1.id == ids[0]) {
							rowNos[0] = j;
							break;
						}
					}
					if (rowNos[0] < 0) {
						rowNos[0] = data0.children.size();
						data1 = new OWorklist();
						data1.id = ids[0];
						data1.name = name;
						data0.children.add(data1);
					}
				}
				// Match 2nd node
				switch (rowsView[1]) {
				case IPanelRows.ROW_FACILITY:
					id = row.facID;
					name = row.facility;
					break;
				case IPanelRows.ROW_SPECIALTY:
					id = row.spyID;
					name = row.specialty;
					break;
				case IPanelRows.ROW_SUBSPECIAL:
					id = row.subID;
					name = row.subspecial;
					break;
				case IPanelRows.ROW_PROCEDURE:
					id = row.proID;
					name = row.procedure;
					break;
				case IPanelRows.ROW_STAFF:
					id = row.prsID;
					name = row.staff;
					break;
				default:
					id = -2;
				}
				if (ids[1] != id) {
					ids[1] = id;
					rowNos[1] = -1;
					for (int i = 2; i < rowsView.length; i++) {
						ids[i] = -1;
						rowNos[i] = -1;
					}
					for (int i = 0; i < data1.children.size(); i++) {
						data2 = data1.children.get(i);
						if (data2.id == ids[1]) {
							rowNos[1] = i;
							break;
						}
					}
					if (rowNos[1] < 0) {
						rowNos[1] = data1.children.size();
						data2 = new OWorklist();
						data2.id = ids[1];
						data2.name = name;
						data1.children.add(data2);
					}
				}
				// Match 3rd node
				switch (rowsView[2]) {
				case IPanelRows.ROW_FACILITY:
					id = row.facID;
					name = row.facility;
					break;
				case IPanelRows.ROW_SPECIALTY:
					id = row.spyID;
					name = row.specialty;
					break;
				case IPanelRows.ROW_SUBSPECIAL:
					id = row.subID;
					name = row.subspecial;
					break;
				case IPanelRows.ROW_PROCEDURE:
					id = row.proID;
					name = row.procedure;
					break;
				case IPanelRows.ROW_STAFF:
					id = row.prsID;
					name = row.staff;
					break;
				default:
					id = -2;
				}
				if (ids[2] != id) {
					ids[2] = id;
					ids[3] = -1;
					ids[4] = -1;
					rowNos[2] = -1;
					rowNos[3] = -1;
					rowNos[4] = -1;
					for (int i = 0; i < data2.children.size(); i++) {
						data3 = data2.children.get(i);
						if (data3.id == ids[2]) {
							rowNos[2] = i;
							break;
						}
					}
					if (rowNos[2] < 0) {
						rowNos[2] = data2.children.size();
						data3 = new OWorklist();
						data3.id = ids[2];
						data3.name = name;
						data2.children.add(data3);
					}
				}
				// Match 4th node
				switch (rowsView[3]) {
				case IPanelRows.ROW_FACILITY:
					id = row.facID;
					name = row.facility;
					break;
				case IPanelRows.ROW_SPECIALTY:
					id = row.spyID;
					name = row.specialty;
					break;
				case IPanelRows.ROW_SUBSPECIAL:
					id = row.subID;
					name = row.subspecial;
					break;
				case IPanelRows.ROW_PROCEDURE:
					id = row.proID;
					name = row.procedure;
					break;
				case IPanelRows.ROW_STAFF:
					id = row.prsID;
					name = row.staff;
					break;
				default:
					id = -2;
				}
				if (ids[3] != id) {
					ids[3] = id;
					ids[4] = -1;
					rowNos[3] = -1;
					rowNos[4] = -1;
					for (int i = 0; i < data3.children.size(); i++) {
						data4 = data3.children.get(i);
						if (data4.id == ids[3]) {
							rowNos[3] = i;
							break;
						}
					}
					if (rowNos[3] < 0) {
						rowNos[3] = data3.children.size();
						data4 = new OWorklist();
						data4.id = ids[3];
						data4.name = name;
						data3.children.add(data4);
					}
				}
				// Match 5th node
				switch (rowsView[4]) {
				case IPanelRows.ROW_FACILITY:
					id = row.facID;
					name = row.facility;
					break;
				case IPanelRows.ROW_SPECIALTY:
					id = row.spyID;
					name = row.specialty;
					break;
				case IPanelRows.ROW_SUBSPECIAL:
					id = row.subID;
					name = row.subspecial;
					break;
				case IPanelRows.ROW_PROCEDURE:
					id = row.proID;
					name = row.procedure;
					break;
				case IPanelRows.ROW_STAFF:
					id = row.prsID;
					name = row.staff;
					break;
				default:
					id = -2;
				}
				if (ids[4] != id) {
					ids[4] = id;
					rowNos[4] = -1;
					for (int i = 0; i < data4.children.size(); i++) {
						data5 = data4.children.get(i);
						if (data5.id == ids[4]) {
							rowNos[4] = i;
							break;
						}
					}
					if (rowNos[4] < 0) {
						rowNos[4] = data4.children.size();
						data5 = new OWorklist();
						data5.id = ids[4];
						data5.name = name;
						data4.children.add(data5);
					}
				}
				data0.noCases += row.noCases;
				data5.noCases += row.noCases;
				data4.noCases += row.noCases;
				data3.noCases += row.noCases;
				data2.noCases += row.noCases;
				data1.noCases += row.noCases;
				data0.noSpecs += row.noSpecs;
				data5.noSpecs += row.noSpecs;
				data4.noSpecs += row.noSpecs;
				data3.noSpecs += row.noSpecs;
				data2.noSpecs += row.noSpecs;
				data1.noSpecs += row.noSpecs;
				data0.noBlocks += row.noBlocks;
				data5.noBlocks += row.noBlocks;
				data4.noBlocks += row.noBlocks;
				data3.noBlocks += row.noBlocks;
				data2.noBlocks += row.noBlocks;
				data1.noBlocks += row.noBlocks;
				data0.noSlides += row.noSlides;
				data5.noSlides += row.noSlides;
				data4.noSlides += row.noSlides;
				data3.noSlides += row.noSlides;
				data2.noSlides += row.noSlides;
				data1.noSlides += row.noSlides;
				data0.fte1 += row.fte1;
				data5.fte1 += row.fte1;
				data4.fte1 += row.fte1;
				data3.fte1 += row.fte1;
				data2.fte1 += row.fte1;
				data1.fte1 += row.fte1;
				data0.fte2 += row.fte2;
				data5.fte2 += row.fte2;
				data4.fte2 += row.fte2;
				data3.fte2 += row.fte2;
				data2.fte2 += row.fte2;
				data1.fte2 += row.fte2;
				data0.fte3 += row.fte3;
				data5.fte3 += row.fte3;
				data4.fte3 += row.fte3;
				data3.fte3 += row.fte3;
				data2.fte3 += row.fte3;
				data1.fte3 += row.fte3;
				data0.fte4 += row.fte4;
				data5.fte4 += row.fte4;
				data4.fte4 += row.fte4;
				data3.fte4 += row.fte4;
				data2.fte4 += row.fte4;
				data1.fte4 += row.fte4;
				data0.fte5 += row.fte5;
				data5.fte5 += row.fte5;
				data4.fte5 += row.fte5;
				data3.fte5 += row.fte5;
				data2.fte5 += row.fte5;
				data1.fte5 += row.fte5;
			}
			try {
				Thread.sleep(LConstants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			data0.fte1 = data0.fte1 / fte1;
			data0.fte2 = data0.fte2 / fte2;
			data0.fte3 = data0.fte3 / fte3;
			data0.fte4 = data0.fte4 / fte4;
			data0.fte5 = data0.fte5 / fte5;
			setTotals(data0, fte1, fte2, fte3, fte4, fte5);
			sortChildren(data0);
			setModel(data0);
		}
	}
}