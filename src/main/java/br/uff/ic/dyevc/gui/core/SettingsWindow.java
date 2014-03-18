package br.uff.ic.dyevc.gui.core;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.utils.PreferencesManager;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Frame;

import javax.swing.JDialog;

/**
 * This class presents a window where the user can set application general
 * settings and save them on the operating system.
 *
 * @author Cristiano
 */
public class SettingsWindow extends JDialog {
    private static final long serialVersionUID = -2566538226237705032L;

    /**
     * Creates new form SettingsWindow
     */
    public SettingsWindow(Frame owner) {
        super(owner, true);
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setTitle("DyeVC Settings");
        setSize(new java.awt.Dimension(400, 140));
        setResizable(false);
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);

        applicationSettingsBean = PreferencesManager.getInstance().loadPreferences();
        jPanel3                 = new javax.swing.JPanel();
        jPanel4                 = new javax.swing.JPanel();
        lblRefreshRate          = new javax.swing.JLabel();
        txtRefreshRate          = new javax.swing.JTextField();
        lblPerformanceMode      = new javax.swing.JLabel();
        ckPerformanceMode       = new javax.swing.JCheckBox();
        txtRefreshRate          = new javax.swing.JTextField();
        btnSave                 = new javax.swing.JButton();
        btnCancel               = new javax.swing.JButton();
        workingPathFileChooser  = new javax.swing.JFileChooser();

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel3Layout.createSequentialGroup().addContainerGap().addGroup(
                    jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                        lblRefreshRate, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(
                        lblPerformanceMode, javax.swing.GroupLayout.Alignment.TRAILING)).addPreferredGap(
                            javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                                jPanel3Layout.createSequentialGroup()).addComponent(txtRefreshRate).addComponent(
                                ckPerformanceMode)).addContainerGap()));
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel3Layout.createSequentialGroup().addContainerGap().addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                        lblRefreshRate).addComponent(
                        txtRefreshRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                            javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                                lblPerformanceMode).addComponent(
                                ckPerformanceMode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap(
                                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        lblRefreshRate.setText("Refresh Rate (s):");
        lblRefreshRate.setToolTipText("Enter the time in seconds between new checks.");
        txtRefreshRate.setText(Integer.valueOf(applicationSettingsBean.getRefreshInterval()).toString());

        lblPerformanceMode.setText("Performance Mode:");
        lblRefreshRate.setToolTipText("Check the box if the application will be collecting performance data.");
        ckPerformanceMode.setSelected(applicationSettingsBean.isPerformanceMode());

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
            settingsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE).addComponent(
                    jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE));
        settingsWindowLayout.setVerticalGroup(
            settingsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                settingsWindowLayout.createSequentialGroup().addComponent(
                    jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                        javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                        jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)));
    }    // </editor-fold>

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO acrescentar validação dos valores
        applicationSettingsBean.setRefreshInterval(new Integer(txtRefreshRate.getText()).intValue());
        applicationSettingsBean.setPerformanceMode(ckPerformanceMode.isSelected());

        PreferencesManager.getInstance().storePreferences(applicationSettingsBean);
        dispose();
    }

    private ApplicationSettingsBean  applicationSettingsBean;
    private javax.swing.JPanel       jPanel3;
    private javax.swing.JPanel       jPanel4;
    private javax.swing.JButton      btnSave;
    private javax.swing.JButton      btnCancel;
    private javax.swing.JLabel       lblRefreshRate;
    private javax.swing.JTextField   txtRefreshRate;
    private javax.swing.JLabel       lblPerformanceMode;
    private javax.swing.JCheckBox    ckPerformanceMode;
    private javax.swing.JFileChooser workingPathFileChooser;
}
