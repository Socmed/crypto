
package egl.env;

import java.io.File;
import java.util.Properties;

public interface DirProvider {

    boolean isLoadPropertyFileFromUserDir();

    void updateLogFileHandler(Properties loggingProperties);

    String getDbDir(String dbDir);

    File getLogFileDir();

    File getConfDir();

    String getUserHomeDir();
}
