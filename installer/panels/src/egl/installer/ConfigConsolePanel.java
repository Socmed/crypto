
package egl.installer;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;
import static egl.installer.ConfigHandler.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigConsolePanel extends AbstractConsolePanel
{
    private ConfigHandler handler = new ConfigHandler();
    private Prompt prompt;

    public ConfigConsolePanel(PanelView<ConsolePanel> panel, InstallData installData, Prompt prompt) {
        super(panel);
        this.prompt = prompt;
    }

    @Override
    public boolean run(InstallData installData, Properties properties) {
        return true;
    }

    @Override
    public boolean run(InstallData installData, Console console) {
        if (handler.isServerRunning() &&
            askUser("A running server was detected.", "Do you want to stop the server?"))
        {
            installData.setVariable(VAR_SHUTDOWN_SERVER, "true");
        }

        String installPath = installData.getInstallPath();
        if (handler.isEagleInstallDir(installPath) &&
            askUser("An existing installation was found.", "Do you want to remove it?"))
        {
            installData.setVariable(VAR_CLEAN_INSTALL_DIR, "true");
        }

        List<ConfigHandler.Setting> allSettings = handler.readSettings();
        List<ConfigHandler.Setting> settings = new LinkedList<>();

        if (! allSettings.isEmpty() &&
            askUser("There are a few settings that can be customized now. " +
                    "They will be put into conf/egl.properties file which you can edit later.",
                    "Do you want to customize settings now?"))
        {
            Map<String, String> properties = new HashMap<>();
            next:
            for (Setting setting: allSettings) {
                for (Map.Entry<String, String> e: setting.properties.entrySet()) {
                    if (properties.containsKey(e.getKey()) &&
                        ! properties.get(e.getKey()).equals(e.getValue()))
                    {
                        // This setting happens to conflict with another one, so don't ask about it
                        break next;
                    }
                }
                if (askUser(setting.description, "Enable this setting?")) {
                    settings.add(setting);
                    properties.putAll(setting.properties);
                }
            }
        }
        installData.setVariable(VAR_FILE_CONTENTS, handler.writeSettings(settings));
        return true;
    }

    private boolean askUser(String title, String message) {
        return Prompt.Option.YES ==
                prompt.confirm(Prompt.Type.QUESTION, title, message, Prompt.Options.YES_NO, Prompt.Option.NO);
    }
}
