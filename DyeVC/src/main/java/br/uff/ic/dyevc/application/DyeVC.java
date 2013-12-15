package br.uff.ic.dyevc.application;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.gui.core.SettingsWindow;
import br.uff.ic.dyevc.gui.core.SplashScreen;
import br.uff.ic.dyevc.gui.core.StdOutErrWindow;
import br.uff.ic.dyevc.gui.main.MainWindow;
import br.uff.ic.dyevc.gui.utils.GUIManager;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.monitor.RepositoryMonitor;
import br.uff.ic.dyevc.utils.PreferencesManager;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * This class starts DyeVC
 * @author Cristiano Cesario (ccesario@ic.uff.br)
 */
public class DyeVC {
    /**
     * Singleton instance
     */
    private static DyeVC instance;

    /**
     * Singleton constructor
     */
    private DyeVC() {}

    /**
     * Provides the singleton instance
     * @return the singleton instance.
     */
    public synchronized static DyeVC getInstance() {
        if (instance == null) {
            instance = new DyeVC();
        }

        return instance;
    }

    /**
     * Start DyeVC
     */
    public synchronized void start() {
        // Show splash window
        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);
        splash.setStatus("Starting subsystems...");
        final PreferencesManager preferencesManager = PreferencesManager.getInstance();
        final GUIManager         guiManager         = GUIManager.getInstance();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(DyeVC.class.getName()).log(Level.SEVERE, null, ex);
        }

        splash.setStatus("Restoring repositories...");
        final MonitoredRepositories monitoredRepositories = MonitoredRepositories.getInstance();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(DyeVC.class.getName()).log(Level.SEVERE, null, ex);
        }

        splash.setStatus("Constructing the main window...");
        final MainWindow mainWindow = new MainWindow();
        guiManager.setMainWindow(mainWindow);
        splash.setVisible(false);

        // Showing the main window
        guiManager.run(new Runnable() {
            @Override
            public void run() {
                guiManager.setSplashScreenStatus("Constructing the settings window...");
                SettingsWindow settingsWindow = new SettingsWindow(mainWindow);
                guiManager.setSettingsWindow(settingsWindow);

                guiManager.setSplashScreenStatus("Constructing the console window...");
                StdOutErrWindow stdOutWindow = new StdOutErrWindow();
                guiManager.setConsoleWindow(stdOutWindow);

                guiManager.setSplashScreenStatus("Starting monitor...");
                final RepositoryMonitor monitor = RepositoryMonitor.getInstance();
                monitor.setContainer(mainWindow);
                monitor.start();
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the System look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            LoggerFactory.getLogger(DyeVC.class).error("Error starting DyeVC", ex);
        } catch (InstantiationException ex) {
            LoggerFactory.getLogger(DyeVC.class).error("Error starting DyeVC", ex);
        } catch (IllegalAccessException ex) {
            LoggerFactory.getLogger(DyeVC.class).error("Error starting DyeVC", ex);
        } catch (UnsupportedLookAndFeelException ex) {
            LoggerFactory.getLogger(DyeVC.class).error("Error starting DyeVC", ex);
        }
        // </editor-fold>

        System.out.println("Starting Dyevc...");
        DyeVC.getInstance().start();
    }
}
