package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Cristiano
 */
public class SettingsWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = -2566538226237705032L;

    /**
     * Creates new form SettingsWindow
     */
    public SettingsWindow() {
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DyeVC Settings");
        setSize(new java.awt.Dimension(506, 144));
        setResizable(false);
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);

        applicationSettingsBean = PreferencesUtils.loadPreferences();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        lblWorkingPath = new javax.swing.JLabel();
        lblRefreshRate = new javax.swing.JLabel();
        txtWorkingPath = new javax.swing.JTextField();
        txtRefreshRate = new javax.swing.JTextField();
        btnExploreWorkingPath = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        workingPathFileChooser = new javax.swing.JFileChooser();

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblRefreshRate, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(lblWorkingPath, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(btnExploreWorkingPath)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtWorkingPath, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 76, Short.MAX_VALUE))
                .addComponent(txtRefreshRate))
                .addContainerGap()));
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblWorkingPath)
                .addComponent(txtWorkingPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnExploreWorkingPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblRefreshRate)
                .addComponent(txtRefreshRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        lblWorkingPath.setText("Working Path:");
        lblWorkingPath.setToolTipText("Enter path to be used to store temporary files.");
        lblRefreshRate.setText("Refresh Rate (s):");
        lblRefreshRate.setToolTipText("Enter the time in seconds between new checks.");

        txtWorkingPath.setText(applicationSettingsBean.getWorkingPath());
        txtRefreshRate.setText(Integer.valueOf(applicationSettingsBean.getRefreshInterval()).toString());

        btnExploreWorkingPath.setText("Explore");
        btnExploreWorkingPath.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exploreWPathButtonActionPerformed(evt);
            }
        });
        btnSave.setText("Save");
        btnSave.setSelected(true);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        jPanel4.add(btnSave);

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel4.add(btnCancel);

        javax.swing.GroupLayout settingsWindowLayout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(settingsWindowLayout);
        settingsWindowLayout.setHorizontalGroup(
                settingsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        settingsWindowLayout.setVerticalGroup(
                settingsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsWindowLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="main">
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SettingsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SettingsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SettingsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SettingsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SettingsWindow().setVisible(true);
            }
        });
    }
    //</editor-fold>

    private void exploreWPathButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                   
        workingPathFileChooser.setCurrentDirectory(null);
        workingPathFileChooser.setDialogTitle("Select a working directory for DyeVC");
        workingPathFileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        
        int returnVal = workingPathFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = workingPathFileChooser.getSelectedFile();
            // What to do with the file, e.g. display it in a TextArea
            txtWorkingPath.setText(file.getAbsolutePath());
        }
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //TODO acrescentar validação dos valores
        applicationSettingsBean.setRefreshInterval(new Integer(txtRefreshRate.getText()).intValue());
        applicationSettingsBean.setWorkingPath(txtWorkingPath.getText());
        
        PreferencesUtils.storePreferences(applicationSettingsBean);
        dispose();
    }
    
    private ApplicationSettingsBean applicationSettingsBean;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnExploreWorkingPath;
    private javax.swing.JLabel lblWorkingPath;
    private javax.swing.JLabel lblRefreshRate;
    private javax.swing.JTextField txtRefreshRate;
    private javax.swing.JTextField txtWorkingPath;
    private javax.swing.JFileChooser workingPathFileChooser;
}
