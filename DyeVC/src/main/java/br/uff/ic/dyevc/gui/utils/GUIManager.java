package br.uff.ic.dyevc.gui.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.gui.core.SettingsWindow;
import br.uff.ic.dyevc.gui.core.SplashScreen;
import br.uff.ic.dyevc.gui.core.StdOutErrWindow;
import br.uff.ic.dyevc.gui.main.MainWindow;
import br.uff.ic.dyevc.utils.ApplicationVersionUtils;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
     * Show the about dialog
     */
    public void showAboutDialog() {
        URL url = getClass().getResource("/about.html");
        try {
            JPanel      aboutPanel = new JPanel(new BorderLayout());

            JEditorPane editorPane = new JEditorPane(url);
            editorPane.setEditable(false);
            editorPane.setOpaque(false);
            aboutPanel.add(new JScrollPane(editorPane), BorderLayout.CENTER);

            JPanel memoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            memoryPanel.setOpaque(false);
            long memory = Runtime.getRuntime().totalMemory();
            memoryPanel.add(new JLabel("Memory used: " + Long.toString(Math.round(memory / Math.pow(2, 20))) + " MB"));
            aboutPanel.add(memoryPanel, BorderLayout.NORTH);

            aboutPanel.setPreferredSize(new Dimension(400, 300));
            String title = "About DyeVC (version " + ApplicationVersionUtils.getInstance().getAppVersion() + ")";
            JOptionPane.showMessageDialog(mainWindow, aboutPanel, title, JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            showErrorDialog(e);
        } catch (HeadlessException e) {
            showErrorDialog(e);
        }
    }

    /**
     * Show any error message
     * @param e the exception to show in error dialog
     */
    public void showErrorDialog(Exception e) {
        StringWriter buffer      = new StringWriter();
        PrintWriter  printWriter = new PrintWriter(buffer);
        e.printStackTrace(printWriter);

        showMessageDialog("Error", e.getMessage(), "Error trace", buffer.toString(), false, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show a message dialog with a specific long text inside
     * @param windowTitle the window title
     * @param briefMessage the brief message to be shown
     * @param longMessageTitle the title of the long message
     * @param longMessage the long message to be shown
     * @param wrapLines controls whether lines will be automatically wrapped (if true) or not (if false)
     * @param MessageType the message type
     */
    public void showMessageDialog(String windowTitle, String briefMessage, String longMessageTitle, String longMessage,
                                  boolean wrapLines, int MessageType) {
        JTextArea textArea = new JTextArea(longMessage);
        textArea.setEditable(false);
        textArea.setOpaque(false);

        if (wrapLines) {
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
        }

        JPanel      messagePanel = new JPanel(new BorderLayout(15, 15));
        JScrollPane scrollPanel  = new JScrollPane(textArea);
        scrollPanel.setBorder(BorderFactory.createTitledBorder(longMessageTitle));
        messagePanel.add(scrollPanel, BorderLayout.CENTER);
        messagePanel.add(new JLabel("<html>" + briefMessage + "</html>"), BorderLayout.NORTH);
        messagePanel.setPreferredSize(new Dimension(400, 200));

        JOptionPane.showMessageDialog(mainWindow, messagePanel, windowTitle, MessageType);
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
