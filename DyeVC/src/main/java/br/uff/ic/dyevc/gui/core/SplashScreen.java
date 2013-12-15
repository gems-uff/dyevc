package br.uff.ic.dyevc.gui.core;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.border.BevelBorder;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;

/**
 * Splash screen used in long operations.
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class SplashScreen extends JDialog {
    private JLabel statusLabel;
    private JLabel memoryLabel;

    /**
     * Construct a modal splash screen
     *
     * @param owner The owner of this splash screen.
     */
    public SplashScreen(JFrame owner) {
        super(owner);
        this.setLocationRelativeTo(owner);
        this.init();
    }

    /**
     * Construct a non-modal splash screen
     */
    public SplashScreen() {
        super();
        this.init();
    }

    /**
     * Create/Configure the splash screen
     */
    private void init() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setBackground(new Color(245, 245, 245));

        memoryLabel = new JLabel();
        memoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(memoryLabel, BorderLayout.NORTH);

        JLabel mainLabel = new JLabel("DyeVC", SwingConstants.CENTER);
        mainLabel.setFont(new Font("SansSerif", Font.PLAIN, 72));
        panel.add(mainLabel, BorderLayout.CENTER);

        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        panel.add(statusLabel, BorderLayout.SOUTH);

        this.setUndecorated(true);
        this.setContentPane(panel);
        this.setSize(400, 200);
        this.setLocationRelativeTo(getOwner());
        this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    }

    /**
     * Write a new status message
     * @param message Message to be shown in splash screen
     */
    public void setStatus(String message) {
        statusLabel.setText(message);
        updateMemory();
    }

    /**
     * Update the memory message
     */
    public void updateMemory() {
        long memory = Runtime.getRuntime().totalMemory();
        memoryLabel.setText("Memory used: " + Long.toString(Math.round(memory / Math.pow(2, 20))) + " MB");
    }

    /**
     * @param visible Controls whether the splash will be visible or not.
     * @see java.awt.Component#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        this.setLocationRelativeTo(null);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        super.setVisible(visible);
    }
}
