
package egl.env;

import java.nio.file.Paths;

public class WindowsUserDirProvider extends DesktopUserDirProvider {

    private static final String EGL_USER_HOME = System.getProperty("user.dir").toString();

    @Override
    public String getUserHomeDir() {
        return EGL_USER_HOME;
    }
}
