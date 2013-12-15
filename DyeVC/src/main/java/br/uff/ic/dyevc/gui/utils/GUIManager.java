package br.uff.ic.dyevc.gui.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.gui.core.SettingsWindow;
import br.uff.ic.dyevc.gui.core.SplashScreen;
import br.uff.ic.dyevc.gui.core.StdOutErrWindow;
import br.uff.ic.dyevc.gui.main.MainWindow;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Cursor;

/**
 * Manages the DyeVC Windows
 *
 * @author Cristiano Cesario (ccesario@ic.uff.br)
 */
public class GUIManager {
    /**
     * Singleton instance
     */
    private static GUIManager instance;

    /**
     * The main window
     */
    private MainWindow mainWindow;

    /**
     * The configuration dialog
     */
    private SettingsWindow settingsWindow;

    /**
     * The console window
     */
    private StdOutErrWindow stdOutErrWindow;

    /**
     * The splash screen
     */
    private SplashScreen splashScreen;

    /**
     * Singleton constructor
     */
    private GUIManager() {
        // Private singleton constructor
    }

    /**
     * Provides the singleton instance
     * @return the singleton instance
     */
    public static GUIManager getInstance() {
        if (instance == null) {
            instance = new GUIManager();
        }

        return instance;
    }

    /**
     * Sets the main window.<br>
     * Should be called once, by DyeVC at boot time.
     * @param mainWindow the window to be set
     */
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow   = mainWindow;
        this.splashScreen = new SplashScreen(mainWindow);
    }

    /**
     * Sets an specified cursor to the mouse over the main window
     * @param cursor the cursor to be set
     */
    public void setMainWindowCursor(Cursor cursor) {
        mainWindow.setCursor(cursor);
    }

    /**
     * Sets the settings window
     * Should be called once, by DyeVC at boot time
     * @param settingsWindow the window to be set
     */
    public void setSettingsWindow(SettingsWindow settingsWindow) {
        this.settingsWindow = settingsWindow;
    }

    /**
     * Shows the settings window
     */
    public void showSettingsWindow() {
        settingsWindow.setVisible(true);
    }

    /**
     * Sets the console window
     * Should be called once, by DyeVC at boot time
     * @param stdOutErrWindow the window to be set
     */
    public void setConsoleWindow(StdOutErrWindow stdOutErrWindow) {
        this.stdOutErrWindow = stdOutErrWindow;
    }

    /**
     * Shows the console window
     */
    public void showConsoleWindow() {
        stdOutErrWindow.setVisible(true);
    }

    /**
     * Shows the splash screen and execute a given code in a separate thread.
     * At the end of the code, hide the splash screen
     * @param code the code to be ran.
     */
    public void run(final Runnable code) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                code.run();

                // Wait the splash screen to be visible before trying to hide it
                while (!splashScreen.isVisible()) {}

                splashScreen.setVisible(false);
            }
        });
        thread.start();
        splashScreen.setVisible(true);
    }

    /**
     * Set a new status message in the splash screen
     * @param message the message to be set in splash screen.
     */
    public void setSplashScreenStatus(String message) {
        splashScreen.setStatus(message);
    }
}
