package ca.powerj;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.device.IUpdateNotifier;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.birt.core.framework.PlatformConfig;

class IChartCore extends JPanel implements IUpdateNotifier, ComponentListener {
	boolean bNeedsGeneration = true;
	transient GeneratedChartState gcs = null;
	IDeviceRenderer idr = null;
	BufferedImage bi = null;
	Chart cm = null;

	IChartCore(Dimension dim) {
		super();
		setSize(dim);
		try {
			PlatformConfig config = new PlatformConfig();
			config.setProperty("STANDALONE", "true");
			final PluginSettings ps = PluginSettings.instance(config);
			idr = ps.getDevice("dv.SWING");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void close() {
		idr.dispose();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		setVisible(false);
	}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
		Dimension dim = e.getComponent().getSize();
		if (dim.width < 200) {
			dim.width = 200;
		}
		if (dim.height < 200) {
			dim.height = 200;
		}
		setSize(dim);
		bNeedsGeneration = true;
	}

	@Override
	public void componentShown(ComponentEvent e) {
		setVisible(true);
	}

	@Override
	public Chart getDesignTimeModel() {
		return cm;
	}

	@Override
	public Chart getRunTimeModel() {
		return gcs.getChartModel();
	}

	public void paint(Graphics g) {
		super.paint(g);
		if (bi == null) {
			updateBuffer();
		}
		g.drawImage(bi, 0, 0, this);
	}

	@Override
	public Object peerInstance() {
		return this;
	}

	public void regenerateChart() {
		bNeedsGeneration = true;
		updateBuffer();
		repaint();
	}

	@Override
	public void repaintChart() {
		updateBuffer();
		repaint();
	}

	public void updateBuffer() {
		Dimension d = getSize();
		if (bi == null
				|| bi.getWidth() != d.width
				|| bi.getHeight() != d.height) {
			bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		}
		if (cm != null) {
			Graphics2D g2d = (Graphics2D) bi.getGraphics();
			idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, g2d);
			Bounds bo = BoundsImpl.create(0, 0, d.width, d.height);
			bo.scale(72d / idr.getDisplayServer().getDpiResolution());
			Generator gr = Generator.instance();
			if (bNeedsGeneration) {
				bNeedsGeneration = false;
				try {
					gcs = gr.build(idr.getDisplayServer(), cm, bo, null, null, null);
				} catch (ChartException ce) {
					ce.printStackTrace();
				}
			}
			try {
				gr.render(idr, gcs);
			} catch (ChartException ce) {
				ce.printStackTrace();
			} finally {
				g2d.dispose();
			}
		}
	}
}