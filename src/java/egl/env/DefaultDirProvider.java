
package egl.env;

import java.io.File;
import java.nio.file.Paths;
import java.util.Properties;

public class DefaultDirProvider implements DirProvider {

    @Override
    public boolean isLoadPropertyFileFromUserDir() {
        return false;
    }

    @Override
    public void updateLogFileHandler(Properties loggingProperties) {}

    @Override
    public String getDbDir(String dbDir) {
        return dbDir;
    }

    @Override
    public File getLogFileDir() {
        return new File(getUserHomeDir(), "logs");
    }

    @Override
    public File getConfDir() {
        return new File(getUserHomeDir(), "conf");
    }

    @Override
    public String getUserHomeDir() {
        return Paths.get(".").toAbsolutePath().getParent().toString();
    }

}
