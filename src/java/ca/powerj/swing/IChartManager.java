package ca.powerj.swing;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.birt.core.framework.PlatformConfig;

public class IChartManager {
	IDeviceRenderer idr = null;

	public IChartManager() {
		try {
			PlatformConfig config = new PlatformConfig();
			config.setProperty("STANDALONE", "true");
			final PluginSettings ps = PluginSettings.instance(config);
			idr = ps.getDevice("dv.SWING");
		} catch (Exception ignore) {
		}
	}

	void close() {
		try {
			idr.dispose();
		} catch (Exception ignore) {
		}
	}
}