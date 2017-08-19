
package egl.addons;

import egl.Eagle;
import egl.util.Logger;

public final class BeforeShutdown implements AddOn {

    final String beforeShutdownScript = Eagle.getStringProperty("egl.beforeShutdownScript");

    @Override
    public void shutdown() {
        if (beforeShutdownScript != null) {
            try {
                Runtime.getRuntime().exec(beforeShutdownScript);
            } catch (Exception e) {
                Logger.logShutdownMessage("Failed to run after start script: " + beforeShutdownScript, e);
            }
        }
    }

}
