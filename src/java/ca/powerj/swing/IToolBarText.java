package ca.powerj.swing;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import ca.powerj.data.DiagnosisList;
import ca.powerj.data.ItemData;
import ca.powerj.data.OrganList;
import ca.powerj.data.ProcedureList;
import ca.powerj.data.SpecialtyList;
import ca.powerj.data.TissueList;
import ca.powerj.gui.AppFrame;
import ca.powerj.gui.ReportPanel;

public class IToolBarText extends JPanel {
	private static final byte TB_DIA = 0;
	private static final byte TB_DIS = 1;
	private static final byte TB_ORG = 2;
	private static final byte TB_PRO = 3;
	private static final byte TB_SPC = 4;
	private static final byte TB_SPY = 5;
	private static final byte TB_SUB = 6;
	private boolean alteredByCode = false;
	private int[] filters = {0, 0, 0, 0, 0, 0, 0};
	private OrganList lstOrgans;
	private ProcedureList lstProcedures;
	private TissueList lstSpecimens;
	private IComboBoxFilter cboDiseases;
	private IComboBoxFilter cboDiagnosis;
	private IComboBoxFilter cboOrgans;
	private IComboBoxFilter cboProcedures;
	private IComboBoxFilter cboSpecialties;
	private IComboBoxFilter cboStyles;
	private IComboBoxFilter cboSubspecialties;
	private IComboBoxFilter cboSpecimens;
	private AppFrame application;
	private ReportPanel panel;

	public IToolBarText(AppFrame application, ReportPanel panel) {
		super();
		this.application = application;
		this.panel = panel;
		createPanel();
	}

	private void createPanel() {
		JToolBar toolBarTop = new JToolBar();
		toolBarTop.setAlignmentX(Component.LEFT_ALIGNMENT);
		SpecialtyList lstSpecialties = application.dbPowerJ.getSpecialties(false);
		cboSpecialties = new IComboBoxFilter(lstSpecialties.getAll());
		cboSpecialties.setName("Specialties");
		cboSpecialties.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!alteredByCode) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setFilter(TB_SPY);
					}
				}
			}
		});
		JLabel lblSpecialties = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_S, "Specialties: ");
		lblSpecialties.setLabelFor(cboSpecialties);
		toolBarTop.add(lblSpecialties);
		toolBarTop.add(cboSpecialties);
		ArrayList<ItemData> lstSubspecialties = application.dbPowerJ.getSubs();
		ItemData[] arrSubspecialties = new ItemData[lstSubspecialties.size()];
		for (int i = 0; i < lstSubspecialties.size(); i++) {
			arrSubspecialties[i] = lstSubspecialties.get(i);
		}
		lstSubspecialties.clear();
		cboSubspecialties = new IComboBoxFilter(arrSubspecialties);
		cboSubspecialties.setName("Subspecialties");
		cboSubspecialties.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!alteredByCode) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setFilter(TB_SUB);
					}
				}
			}
		});
		JLabel lblSubspecialties = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_B, "Subspecialties: ");
		lblSubspecialties.setLabelFor(cboSubspecialties);
		toolBarTop.add(lblSubspecialties);
		toolBarTop.add(cboSubspecialties);
		lstOrgans = application.dbPowerJ.getOrgans();
		cboOrgans = new IComboBoxFilter(lstOrgans.getAll());
		cboOrgans.setName("Organs");
		cboOrgans.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!alteredByCode) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setFilter(TB_ORG);
					}
				}
			}
		});
		JLabel lblOrgans = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_O, "Organs: ");
		lblOrgans.setLabelFor(cboOrgans);
		toolBarTop.add(lblOrgans);
		toolBarTop.add(cboOrgans);
		lstProcedures = application.dbPowerJ.getProcs();
		cboProcedures = new IComboBoxFilter(lstProcedures.getAll());
		cboProcedures.setName("Procedures");
		cboProcedures.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!alteredByCode) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setFilter(TB_PRO);
					}
				}
			}
		});
		JLabel lblProcedures = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_P, "Procedures: ");
		lblProcedures.setLabelFor(cboProcedures);
		toolBarTop.add(lblProcedures);
		toolBarTop.add(cboProcedures);
		JToolBar toolBarBottom = new JToolBar();
		toolBarBottom.setAlignmentX(Component.LEFT_ALIGNMENT);
		lstSpecimens = new TissueList();
		cboSpecimens = new IComboBoxFilter(lstSpecimens.getAll());
		cboSpecimens.setName("Specimens");
		cboSpecimens.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!alteredByCode) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setFilter(TB_SPC);
					}
				}
			}
		});
		JLabel lblSpecimens = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_T, "Specimens: ");
		lblSpecimens.setLabelFor(cboSpecimens);
		toolBarBottom.add(lblSpecimens);
		toolBarBottom.add(cboSpecimens);
		ArrayList<ItemData> lstDiseases = application.dbPowerJ.getDiseases();
		ItemData[] arrDiseases = (ItemData[]) lstDiseases.toArray();
		lstDiseases.clear();
		cboDiseases = new IComboBoxFilter(arrDiseases);
		cboDiseases.setName("Diseases");
		cboDiseases.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!alteredByCode) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setFilter(TB_DIS);
					}
				}
			}
		});
		JLabel lblDiseases = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_C, "Diseases: ");
		lblDiseases.setLabelFor(cboDiseases);
		toolBarBottom.add(lblDiseases);
		toolBarBottom.add(cboDiseases);
		DiagnosisList lstDiagnosis = new DiagnosisList();
		cboDiagnosis = new IComboBoxFilter(lstDiagnosis.getAll());
		cboDiagnosis.setName("Diagnosis");
		cboDiagnosis.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setFilter(TB_DIA);
						}
					});
				}
			}
		});
		JLabel lblDiagnosis = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_D, "Diagnosis: ");
		lblDiagnosis.setLabelFor(cboDiagnosis);
		toolBarBottom.add(lblDiagnosis);
		toolBarBottom.add(cboDiagnosis);
		cboStyles = new IComboBoxFilter((ItemData[]) application.dbPowerJ.getStyles().toArray());
		cboStyles.setName("Styles");
		cboStyles.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setStyle();
						}
					});
				}
			}
		});
		JLabel lblStyles = IUtilities.createJLabel(SwingConstants.RIGHT, KeyEvent.VK_Y, "Style: ");
		lblStyles.setLabelFor(cboStyles);
		toolBarBottom.add(lblStyles);
		toolBarBottom.add(cboStyles);
		setLayout(new GridLayout(0, 1));
		add(toolBarTop, BorderLayout.NORTH);
		add(toolBarBottom, BorderLayout.CENTER);
	}

	private void setFilter(byte key) {
		alteredByCode = true;
		int index = 0;
		switch (key) {
		case TB_DIA:
			index = cboDiagnosis.getIndex();
			if (filters[TB_DIA] != index) {
				filters[TB_DIA] = index;
			}
			break;
		case TB_DIS:
			index = cboDiseases.getIndex();
			if (filters[TB_DIS] != index) {
				filters[TB_DIS] = index;
			}
			break;
		case TB_ORG:
			index = cboOrgans.getIndex();
			if (filters[TB_ORG] != index) {
				filters[TB_ORG] = index;
			}
			break;
		case TB_PRO:
			index = cboProcedures.getIndex();
			if (filters[TB_PRO] != index) {
				filters[TB_PRO] = index;
			}
			break;
		case TB_SPC:
			index = cboSpecimens.getIndex();
			if (filters[TB_SPC] != index) {
				filters[TB_SPC] = index;
			}
			break;
		case TB_SPY:
			index = cboSpecialties.getIndex();
			if (filters[TB_SPY] != index) {
				filters[TB_SPY] = index;
				cboProcedures.setData(lstProcedures.getFiltered(filters[TB_SPY]));
				if (filters[TB_SPY] > 0 && filters[TB_SUB] > 0) {
					cboOrgans.setData(lstOrgans.getFiltered(filters[TB_SPY], filters[TB_SUB]));
				}
			}
			break;
		default:
			index = cboSubspecialties.getIndex();
			if (filters[TB_SUB] != index) {
				filters[TB_SUB] = index;
				if (filters[TB_SPY] > 0 && filters[TB_SUB] > 0) {
					cboOrgans.setData(lstOrgans.getFiltered(filters[TB_SPY], filters[TB_SUB]));
				}
			}
		}
		alteredByCode = false;
	}

	public void setFilter(int[] indices) {
		alteredByCode = true;
		if (filters[TB_SPY] != indices[TB_SPY]) {
			filters[TB_SPY] = indices[TB_SPY];
			cboSpecialties.setIndex(filters[TB_SPY]);
		}
		if (filters[TB_SUB] != indices[TB_SUB]) {
			filters[TB_SUB] = indices[TB_SUB];
			cboSubspecialties.setIndex(filters[TB_SUB]);
		}
		if (filters[TB_ORG] != indices[TB_ORG]) {
			filters[TB_ORG] = indices[TB_ORG];
			cboOrgans.setIndex(filters[TB_ORG]);
		}
		if (filters[TB_SPC] != indices[TB_SPC]) {
			filters[TB_SPC] = indices[TB_SPC];
			cboSpecimens.setIndex(filters[TB_SPC]);
		}
		if (filters[TB_DIS] != indices[TB_DIS]) {
			filters[TB_DIS] = indices[TB_DIS];
			cboDiseases.setIndex(filters[TB_DIS]);
		}
		if (filters[TB_DIA] != indices[TB_DIA]) {
			filters[TB_DIA] = indices[TB_DIA];
			cboDiagnosis.setIndex(filters[TB_DIA]);
		}
		alteredByCode = false;
	}

	private void setStyle() {
		panel.setStyle(cboStyles.getIndex());
	}
}