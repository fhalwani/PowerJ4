package ca.powerj;

import java.awt.Dimension;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.DataPointComponent;
import org.eclipse.birt.chart.model.attribute.DataPointComponentType;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.DataPointComponentImpl;
import org.eclipse.birt.chart.model.attribute.impl.JavaNumberFormatSpecifierImpl;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.BaseSampleData;
import org.eclipse.birt.chart.model.data.DataFactory;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.OrthogonalSampleData;
import org.eclipse.birt.chart.model.data.SampleData;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;

class IChartPie extends IChartCore {
	static final byte COLOR_DEF = 0;
	static final byte COLOR_RAG = 1;

	IChartPie(Dimension dim) {
		super(dim);
	}

	private Chart createChart(String[] x, double[] y, String caption, byte colorID) {
		ChartWithoutAxes chart = ChartWithoutAxesImpl.create();
		chart.setType("Pie Chart");
		chart.setSubType("Standard Pie Chart");
		// Plot
		chart.setSeriesThickness(25);
		chart.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		chart.getBlock().getOutline().setVisible(true);
		// Legend
		chart.getLegend().getText().getFont().setBold(true);
		// Title
		chart.getTitle().getLabel().getCaption().setValue(caption);
		chart.getTitle().getOutline().setVisible(true);
		chart.getTitle().setAnchor(Anchor.NORTH_WEST_LITERAL);
		// Data Set
		TextDataSet categoryValues = TextDataSetImpl.create(x);
		NumberDataSet seriesValues = NumberDataSetImpl.create(y);
		SampleData sdata = DataFactory.eINSTANCE.createSampleData();
		BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
		sdBase.setDataSetRepresentation("");
		sdata.getBaseSampleData().add(sdBase);
		OrthogonalSampleData sdOrthogonal = DataFactory.eINSTANCE.createOrthogonalSampleData();
		sdOrthogonal.setDataSetRepresentation("");
		sdOrthogonal.setSeriesDefinitionIndex(0);
		sdata.getOrthogonalSampleData().add(sdOrthogonal);
		chart.setSampleData(sdata);
		// Base Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sd = SeriesDefinitionImpl.create();
		chart.getSeriesDefinitions().add(sd);
		sd.getSeriesPalette().shift(0);
		if (colorID == COLOR_RAG) {
			sd.getSeriesPalette().getEntries().clear();
			sd.getSeriesPalette().getEntries().add(ColorDefinitionImpl.RED());
			sd.getSeriesPalette().getEntries().add(ColorDefinitionImpl.create(255, 191, 0)); // Amber
			sd.getSeriesPalette().getEntries().add(ColorDefinitionImpl.GREEN());
		}
		sd.getSeries().add(seCategory);
		// Orthogonal Series
		PieSeries sePie = (PieSeries) PieSeriesImpl.create();
		sePie.setDataSet(seriesValues);
		sePie.setExplosion(3);
		DataPointComponent dpc = DataPointComponentImpl.create(
				DataPointComponentType.PERCENTILE_ORTHOGONAL_VALUE_LITERAL,
				JavaNumberFormatSpecifierImpl.create("##%"));
		sePie.getDataPoint().getComponents().clear();
		sePie.getDataPoint().getComponents().add(dpc);
		SeriesDefinition sdPie = SeriesDefinitionImpl.create();
		sd.getSeriesDefinitions().add(sdPie);
		sdPie.getSeries().add(sePie);
		return chart;
	}

	void setChart(String[] xData, double[] yData, String caption, byte color) {
		if (xData == null || yData == null) {
			return;
		}
		cm = createChart(xData, yData, caption, color);
		bNeedsGeneration = true;
		updateBuffer();
		repaint();
	}
}
