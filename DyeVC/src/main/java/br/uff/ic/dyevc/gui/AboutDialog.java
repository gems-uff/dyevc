package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.graph.layout.RepositoryHistoryLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.slf4j.LoggerFactory;

/**
 * Dialog that shows up application version obtained from manifest file.
 *
 * @author Cristiano
 */
public class AboutDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = -4997538889485458252L;
    private br.uff.ic.dyevc.beans.ApplicationVersionBean applicationPropertiesBean1;

    /**
     * Creates new form NewJDialog
     */
    public AboutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">
    @SuppressWarnings("unchecked")
    private void initComponents() {
        applicationPropertiesBean1 = new br.uff.ic.dyevc.beans.ApplicationVersionBean();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        setSize(new java.awt.Dimension(440, 370));
        setMinimumSize(new java.awt.Dimension(440, 370));
        setModal(true);
        setResizable(false);
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);

        JPanel jPanel1 = new javax.swing.JPanel(new BorderLayout());

        String textAbout = new StringBuilder("<html><body><p align='center'>")
                .append("<img src='")
                .append(RepositoryRenderer.class.getResource("/br/uff/ic/dyevc/images/splash.png"))
                .append("'/></p></body></html>").toString();
        
        JEditorPane editorPane = new JEditorPane("text/html", textAbout);
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        jPanel1.add(new JScrollPane(editorPane), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel memoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        memoryPanel.setOpaque(false);
        long memory = Runtime.getRuntime().totalMemory();
        memoryPanel.add(new JLabel("Memory used: " + Long.toString(Math.round(memory / Math.pow(2, 20))) + " MB"));
        
        JPanel versionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        versionPanel.setOpaque(false);
        versionPanel.add(new JLabel(applicationPropertiesBean1.getAppVersion()));
        
        topPanel.add(versionPanel, BorderLayout.WEST);
        topPanel.add(memoryPanel, BorderLayout.EAST);
        
        jPanel1.add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton aboutOK = new javax.swing.JButton();
        aboutOK.setText("OK");
        aboutOK.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });
        
        bottomPanel.add(aboutOK);
        jPanel1.add(bottomPanel, BorderLayout.SOUTH);
        this.add(jPanel1);

    }// </editor-fold>
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("About");
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        AboutDialog dialog = new AboutDialog(frame, true);
        
        
        frame.setVisible(true);
        dialog.setVisible(true);
        
    }
}
