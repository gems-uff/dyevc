/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.beans.MonitoredRepositoriesBean;
import br.uff.ic.dyevc.beans.RepositoryBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

/**
 *
 * @author Cristiano
 */
public class RepositoryConfigWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = -5327813882224088396L;

    /**
     * Creates new form RepositoryConfigWindow
     */
    public RepositoryConfigWindow(MonitoredRepositoriesBean monBean, String name) throws DyeVCException {
        if (monBean == null) {
            Logger.getLogger(RepositoryConfigWindow.class.getName()).log(Level.SEVERE, "Received a null list of monitored repositories");
            throw new DyeVCException("Received a null list of monitored repositories");
        }
        monitoredRepositoriesBean = monBean;
        if (name != null && !"".equals(name)) {
            create = false;
            repositoryBean = monitoredRepositoriesBean.getMonitoredProjectByName(name);
        } else {
            create = true;
            repositoryBean = new RepositoryBean();
        }
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="initComponents">
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        if (create) {
            setTitle("Creating a new monitoring configuration");
        } else {
            setTitle("Changing a monitoring configuration");
        }
        setSize(new java.awt.Dimension(506, 200));
        setMinimumSize(new java.awt.Dimension(506, 200));
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);

        pnlTop = new javax.swing.JPanel();
        pnlBottom = new javax.swing.JPanel();
        pnlTop.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblRepositoryName = new javax.swing.JLabel();
        lblCloneAddress = new javax.swing.JLabel();
        lblOriginURL = new javax.swing.JLabel();
        lblRepositoryName.setText("Repository Name:");
        lblCloneAddress.setText("Clone Address:");
        lblOriginURL.setText("Origin URL:");

        txtRepositoryName = new javax.swing.JTextField();
        txtCloneAddres = new javax.swing.JTextField();
        txtOriginURL = new javax.swing.JTextField();
        txtRepositoryName.setText(repositoryBean.getName());
        txtCloneAddres.setText(repositoryBean.getCloneAddress());
        txtOriginURL.setText(repositoryBean.getOriginUrl());

        btnExploreCloneAddress = new javax.swing.JButton();
        btnExploreCloneAddress.setText("Explore");
        btnExploreCloneAddress.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exploreAddressButtonActionPerformed(evt, txtCloneAddres);
            }
        });

        btnExploreOriginURL = new javax.swing.JButton();
        btnExploreOriginURL.setText("Explore");
        btnExploreOriginURL.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exploreAddressButtonActionPerformed(evt, txtOriginURL);
            }
        });

        btnSaveRepository = new javax.swing.JButton();
        btnSaveRepository.setText("Save");
        btnSaveRepository.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveRepositoryActionPerformed(evt);
            }
        });

        btnCancel = new javax.swing.JButton();
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });


        buildUI();

    }

    // <editor-fold defaultstate="collapsed" desc="event handlers">    
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void btnSaveRepositoryActionPerformed(java.awt.event.ActionEvent evt) {
        repositoryBean.setName(txtRepositoryName.getText());
        repositoryBean.setCloneAddress(txtCloneAddres.getText());
        repositoryBean.setOriginUrl(txtOriginURL.getText());
        monitoredRepositoriesBean.addMonitoredRepository(repositoryBean);
        PreferencesUtils.persistRepositories(monitoredRepositoriesBean);
        dispose();
    }

    @SuppressWarnings("empty-statement")
    private void exploreAddressButtonActionPerformed(java.awt.event.ActionEvent evt, JTextField field) {
        JFileChooser fileChooser = new javax.swing.JFileChooser();;
        if (field.getText() != null && !"".equals(field.getText())) {
            fileChooser.setCurrentDirectory(new File(field.getText()));
        } else {
            fileChooser.setCurrentDirectory(null);

        }
        fileChooser.setDialogTitle("Select path to repository.");
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            field.setText(file.getAbsolutePath());
        }
    }
    //</editor-fold>
    
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
            java.util.logging.Logger.getLogger(RepositoryConfigWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RepositoryConfigWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RepositoryConfigWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RepositoryConfigWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new RepositoryConfigWindow(null, null).setVisible(true);
                } catch (DyeVCException ex) {
                    Logger.getLogger(RepositoryConfigWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    private br.uff.ic.dyevc.beans.MonitoredRepositoriesBean monitoredRepositoriesBean;
    private br.uff.ic.dyevc.beans.RepositoryBean repositoryBean;
    /**
     * If true, bean will be created. Otherwise it will be modified.
     */
    boolean create;
    private javax.swing.JButton btnExploreCloneAddress;
    private javax.swing.JButton btnExploreOriginURL;
    private javax.swing.JButton btnSaveRepository;
    private javax.swing.JButton btnCancel;
    private javax.swing.JLabel lblRepositoryName;
    private javax.swing.JLabel lblCloneAddress;
    private javax.swing.JLabel lblOriginURL;
    private javax.swing.JTextField txtCloneAddres;
    private javax.swing.JTextField txtRepositoryName;
    private javax.swing.JTextField txtOriginURL;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JPanel pnlBottom;

    private void buildUI() {
        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(pnlTop);
        pnlTop.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblRepositoryName, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(lblCloneAddress, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(lblOriginURL, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(txtRepositoryName)
                .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(btnExploreOriginURL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtOriginURL, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(btnExploreCloneAddress)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCloneAddres)))
                .addContainerGap()));
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblRepositoryName)
                .addComponent(txtRepositoryName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblCloneAddress)
                .addComponent(txtCloneAddres, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnExploreCloneAddress))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblOriginURL)
                .addComponent(txtOriginURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnExploreOriginURL))
                .addContainerGap(282, Short.MAX_VALUE)));
        
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout();
        flowLayout1.setAlignOnBaseline(true);
        pnlBottom.setLayout(flowLayout1);
        pnlBottom.add(btnSaveRepository);
        pnlBottom.add(btnCancel);
        
        javax.swing.GroupLayout addRepositoryWindowLayout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(addRepositoryWindowLayout);
        addRepositoryWindowLayout.setHorizontalGroup(
                addRepositoryWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlBottom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        addRepositoryWindowLayout.setVerticalGroup(
                addRepositoryWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(addRepositoryWindowLayout.createSequentialGroup()
                .addComponent(pnlTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
    }
}
