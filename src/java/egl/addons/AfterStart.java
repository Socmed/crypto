
package egl.addons;

import egl.Eagle;
import egl.util.Logger;
import egl.util.ThreadPool;

public final class AfterStart implements AddOn {

    @Override
    public void init() {
        String afterStartScript = Eagle.getStringProperty("egl.afterStartScript");
        if (afterStartScript != null) {
            ThreadPool.runAfterStart(() -> {
                try {
                    Runtime.getRuntime().exec(afterStartScript);
                } catch (Exception e) {
                    Logger.logErrorMessage("Failed to run after start script: " + afterStartScript, e);
                }
            });
        }
    }

}
