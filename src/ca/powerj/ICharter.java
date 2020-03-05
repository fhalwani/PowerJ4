package ca.powerj;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.birt.core.framework.PlatformConfig;

class ICharter {
	protected AClient pj;
	IDeviceRenderer idr = null;

	public ICharter(AClient pj) {
		try {
			this.pj = pj;
			PlatformConfig config = new PlatformConfig();
			config.setProperty("STANDALONE", "true");
			final PluginSettings ps = PluginSettings.instance(config);
			idr = ps.getDevice("dv.SWING");
		} catch (Exception e) {
			pj.log(LConstants.ERROR_SQL, "Charter", e);
		}
	}

	void close() {
		try {
			idr.dispose();
		} catch (Exception ignore) {}
	}
}