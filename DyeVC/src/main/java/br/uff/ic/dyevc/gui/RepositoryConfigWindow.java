/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.beans.RepositoryBean;

/**
 *
 * @author Cristiano
 */
public class RepositoryConfigWindow extends javax.swing.JFrame {
    private static final long serialVersionUID = -5327813882224088396L;
    /**
     * Creates new form RepositoryConfigWindow
     */
    public RepositoryConfigWindow(RepositoryBean bean) {
        if (bean != null) {
            create = false;
            repositoryBean = bean;
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
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);

        pnlTop = new javax.swing.JPanel();
        lblRepositoryName = new javax.swing.JLabel();
        lblCloneAddress = new javax.swing.JLabel();
        lblOriginURL = new javax.swing.JLabel();
        txtRepositoryName = new javax.swing.JTextField();
        txtCloneAddres = new javax.swing.JTextField();
        txtOriginURL = new javax.swing.JTextField();
        btnExploreCloneAddress = new javax.swing.JButton();
        btnExploreOriginURL = new javax.swing.JButton();
        pnlBottom = new javax.swing.JPanel();
        btnSaveRepository = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        pnlTop.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        
        lblRepositoryName.setText("Repository Name:");
        lblCloneAddress.setText("Clone Address:");
        lblOriginURL.setText("Origin URL:");

        txtRepositoryName.setText(repositoryBean.getName());
        txtCloneAddres.setText(repositoryBean.getCloneAddress());
        txtOriginURL.setText(repositoryBean.getOriginUrl());

        btnExploreCloneAddress.setText("Explore");
        btnExploreOriginURL.setText("Explore");

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
                .addContainerGap())
        );
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
                .addContainerGap(282, Short.MAX_VALUE))
        );

        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout();
        flowLayout1.setAlignOnBaseline(true);
        pnlBottom.setLayout(flowLayout1);

        btnSaveRepository.setText("Save");
        btnSaveRepository.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveRepositoryActionPerformed(evt);
            }
        });
        pnlBottom.add(btnSaveRepository);

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        pnlBottom.add(btnCancel);

        javax.swing.GroupLayout addRepositoryWindowLayout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(addRepositoryWindowLayout);
        addRepositoryWindowLayout.setHorizontalGroup(
            addRepositoryWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlBottom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        addRepositoryWindowLayout.setVerticalGroup(
            addRepositoryWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addRepositoryWindowLayout.createSequentialGroup()
                .addComponent(pnlTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        
    }// </editor-fold>

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {                                         
        dispose();
    }                                        
    private void btnSaveRepositoryActionPerformed(java.awt.event.ActionEvent evt) {                                         
        throw new UnsupportedOperationException("Not implemented yet.");
    }                                        

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
                new RepositoryConfigWindow(null).setVisible(true);
            }
        });
    }
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
}
