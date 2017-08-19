
package egl.env.service;

import egl.Eagle;
import egl.env.LookAndFeel;

import javax.swing.*;

@SuppressWarnings("UnusedDeclaration")
public class EagleService_ServiceManagement {

    public static boolean serviceInit() {
        LookAndFeel.init();
        new Thread(() -> {
            String[] args = {};
            Eagle.main(args);
        }).start();
        return true;
    }

    // Invoked when registering the service
    public static String[] serviceGetInfo() {
        return new String[]{
                "EGL Server", // Long name
                "Manages the EGL cryptographic currency protocol", // Description
                "true", // IsAutomatic
                "true", // IsAcceptStop
                "", // failure exe
                "", // args failure
                "", // dependencies
                "NONE/NONE/NONE", // ACTION = NONE | REBOOT | RESTART | RUN
                "0/0/0", // ActionDelay in seconds
                "-1", // Reset time in seconds
                "", // Boot Message
                "false" // IsAutomatic Delayed
        };
    }

    public static boolean serviceIsCreate() {
        return JOptionPane.showConfirmDialog(null, "Do you want to install the EGL service ?", "Create Service", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static boolean serviceIsLaunch() {
        return true;
    }

    public static boolean serviceIsDelete() {
        return JOptionPane.showConfirmDialog(null, "This EGL service is already installed. Do you want to delete it ?", "Delete Service", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static boolean serviceControl_Pause() {
        return false;
    }

    public static boolean serviceControl_Continue() {
        return false;
    }

    public static boolean serviceControl_Stop() {
        return true;
    }

    public static boolean serviceControl_Shutdown() {
        return true;
    }

    public static void serviceFinish() {
        System.exit(0);
    }

}
