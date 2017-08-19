
package egl.env;

import java.nio.file.Paths;

public class UnixUserDirProvider extends DesktopUserDirProvider {

    private static final String EGL_USER_HOME = Paths.get(System.getProperty("user.home"), ".egl").toString();

    @Override
    public String getUserHomeDir() {
        return EGL_USER_HOME;
    }
}
