package ca.powerj;

import java.awt.Dimension;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.DialChart;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.Fill;
import org.eclipse.birt.chart.model.attribute.LineDecorator;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.GradientImpl;
import org.eclipse.birt.chart.model.attribute.impl.LineAttributesImpl;
import org.eclipse.birt.chart.model.component.DialRegion;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.DialRegionImpl;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.BaseSampleData;
import org.eclipse.birt.chart.model.data.DataFactory;
import org.eclipse.birt.chart.model.data.OrthogonalSampleData;
import org.eclipse.birt.chart.model.data.SampleData;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.DialChartImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.DialSeries;
import org.eclipse.birt.chart.model.type.impl.DialSeriesImpl;

class IChartDial extends IChartCore {

	IChartDial(Dimension dim) {
		super(dim);
	}

	private Chart createChart(String[] xData, double[] yData, double maxData, String caption) {
		DialChart chart = (DialChart) DialChartImpl.create();
		chart.setType("Meter Chart");
		chart.setSubType("Standard Meter Chart");
		chart.setDialSuperimposition(false);
		chart.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		chart.getBlock().getOutline().setVisible(true);
		// Plot
		Plot p = chart.getPlot();
		p.getClientArea().setBackground(ColorDefinitionImpl.WHITE());
		// p.setWidthHint(300);
		// p.setHeightHint(300);
		// Title
		chart.getTitle().getLabel().getCaption().setValue(caption);
		chart.getTitle().getOutline().setVisible(false);
		chart.getTitle().setAnchor(Anchor.NORTH_WEST_LITERAL);
		// Legend
		Legend lg = chart.getLegend();
		lg.setPosition(Position.BELOW_LITERAL);
		// lg.setAnchor(Anchor.WEST_LITERAL);
		// lg.getInsets().setTop(0);
		// lg.getInsets().setBottom(10);
		lg.getOutline().setVisible(false);
		lg.setShowValue(true);
		// Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(xData);
		SampleData sdata = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");
		sdata.getBaseSampleData().add(sdBase);
		OrthogonalSampleData sdOrthogonal = DataFactory.eINSTANCE.createOrthogonalSampleData();
		sdOrthogonal.setDataSetRepresentation("");
		sdOrthogonal.setSeriesDefinitionIndex(0);
		sdata.getOrthogonalSampleData().add(sdOrthogonal);
		chart.setSampleData(sdata);
		SeriesDefinition sd = SeriesDefinitionImpl.create();
		chart.getSeriesDefinitions().add(sd);
		Series seCategory = SeriesImpl.create();
		final Fill[] fiaBase = { ColorDefinitionImpl.BLACK(),
				GradientImpl.create(ColorDefinitionImpl.create(225, 225, 255),
						ColorDefinitionImpl.create(255, 255, 225), -35, false),
				ColorDefinitionImpl.CREAM(), ColorDefinitionImpl.RED(), ColorDefinitionImpl.GREEN(),
				ColorDefinitionImpl.BLUE().brighter(), ColorDefinitionImpl.CYAN().darker() };
		sd.getSeriesPalette().getEntries().clear();
		for (int i = 0; i < fiaBase.length; i++) {
			sd.getSeriesPalette().getEntries().add(fiaBase[i]);
		}
		seCategory.setDataSet(categoryValues);
		sd.getSeries().add(seCategory);
		SeriesDefinition sdCity = SeriesDefinitionImpl.create();
		// Dial
		DialSeries seDial = (DialSeries) DialSeriesImpl.create();
		seDial.setDataSet(NumberDataSetImpl.create(yData));
		seDial.getDial().setFill(GradientImpl.create(ColorDefinitionImpl.create(225, 225, 255),
				ColorDefinitionImpl.create(255, 255, 225), -35, false));
		seDial.getNeedle()
				.setLineAttributes(LineAttributesImpl.create(ColorDefinitionImpl.BLACK(), LineStyle.SOLID_LITERAL, 2));
		seDial.getNeedle().setDecorator(LineDecorator.ARROW_LITERAL);
		seDial.getDial().getMinorGrid().getTickAttributes().setVisible(true);
		seDial.getDial().getMinorGrid().getTickAttributes().setColor(ColorDefinitionImpl.RED());
		seDial.getDial().getMinorGrid().setTickStyle(TickStyle.ABOVE_LITERAL);
		seDial.getDial().getScale().setMin(NumberDataElementImpl.create(0));
		seDial.getDial().getScale().setMax(NumberDataElementImpl.create(maxData));
		seDial.getDial().getScale().setStep(maxData / 6);
		seDial.getLabel()
				.setOutline(LineAttributesImpl.create(ColorDefinitionImpl.GREY().darker(), LineStyle.SOLID_LITERAL, 1));
		seDial.getLabel().setBackground(ColorDefinitionImpl.GREY().brighter());
		DialRegion dregion1 = DialRegionImpl.create();
		dregion1.setFill(ColorDefinitionImpl.GREEN());
		dregion1.setStartValue(NumberDataElementImpl.create(0));
		dregion1.setEndValue(NumberDataElementImpl.create(maxData / 2));
		dregion1.setInnerRadius(75);
		seDial.getDial().getDialRegions().add(dregion1);
		DialRegion dregion2 = DialRegionImpl.create();
		dregion2.setFill(ColorDefinitionImpl.create(255, 191, 0)); // Amber
		dregion2.setStartValue(NumberDataElementImpl.create(maxData / 2));
		dregion2.setEndValue(NumberDataElementImpl.create(maxData * 2 / 3));
		dregion2.setInnerRadius(75);
		seDial.getDial().getDialRegions().add(dregion2);
		DialRegion dregion3 = DialRegionImpl.create();
		dregion3.setFill(ColorDefinitionImpl.RED());
		dregion3.setStartValue(NumberDataElementImpl.create(maxData * 2 / 3));
		dregion3.setEndValue(NumberDataElementImpl.create(maxData));
		dregion3.setInnerRadius(75);
		seDial.getDial().getDialRegions().add(dregion3);
		sd.getSeriesDefinitions().add(sdCity);
		sdCity.getSeries().add(seDial);
		return chart;
	}

	void setChart(String[] xData, double[] yData, double maxData, String caption) {
		if (xData == null || yData == null) {
			return;
		}
		cm = createChart(xData, yData, maxData, caption);
		bNeedsGeneration = true;
		updateBuffer();
		repaint();
	}
}
