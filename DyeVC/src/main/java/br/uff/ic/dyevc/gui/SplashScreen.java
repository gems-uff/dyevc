package br.uff.ic.dyevc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * Splash screen used in long operations.
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class SplashScreen extends JDialog {

    private JLabel statusLabel;
    private JLabel memoryLabel;
    private static SplashScreen instance;

    /**
     * Construct a modal splash screen
     * 
     * @param owner frame that owns this splash screen
     */
    private SplashScreen(JFrame owner) {
        super(owner, true);
        this.setLocationRelativeTo(owner);
        this.init();
    }

    /**
     * Construct a non-modal splash screen
     */
    private SplashScreen() {
        super();
        this.init();
    }

    /**
     * Gets a non-modal instance of splash screen
     *
     * @return the single instance of SplashScreen
     */
    public static synchronized SplashScreen getInstance() {
        if (instance == null) {
            instance = new SplashScreen();
        }
        return instance;
    }

    /**
     * Gets a modal instance of splash screen
     *
     * @param owner frame that owns this splash screen
     * @return the single instance of SplashScreen
     */
    public static synchronized SplashScreen getInstance(JFrame owner) {
        if (instance == null) {
            instance = new SplashScreen(owner);
        }
        return instance;
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
     * @see java.awt.Component#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        this.setLocationRelativeTo(null);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        super.setVisible(visible);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("Status");
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setLocation(400, 400);
        SplashScreen splash = SplashScreen.getInstance(frame);
        frame.setVisible(true);
        splash.setStatus("Teste");
        splash.setVisible(true);  
        
    }
}