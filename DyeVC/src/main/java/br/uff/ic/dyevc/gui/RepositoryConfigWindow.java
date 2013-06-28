/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Cristiano
 */
public class RepositoryConfigWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = -5327813882224088396L;
    private ApplicationSettingsBean settings;

    /**
     * Creates new form RepositoryConfigWindow
     */
    public RepositoryConfigWindow(MonitoredRepositories monBean, MonitoredRepository repository) throws DyeVCException {
        if (monBean == null) {
            LoggerFactory.getLogger(RepositoryConfigWindow.class).error("Received a null list of monitored repositories");
            throw new DyeVCException("Received a null list of monitored repositories");
        }
        monitoredRepositoriesBean = monBean;
        if (repository != null) {
            create = false;
            repositoryBean = repository;
        } else {
            create = true;
            repositoryBean = new MonitoredRepository("rep" + System.currentTimeMillis());
        }
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="initComponents">
    private void initComponents() {
        settings = PreferencesUtils.loadPreferences();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        if (create) {
            setTitle("Creating a new monitoring configuration");
        } else {
            setTitle("Changing a monitoring configuration");
        }
        setSize(new java.awt.Dimension(528, 160));
        setMinimumSize(new java.awt.Dimension(528, 160));
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);

        pnlTop = new javax.swing.JPanel();
        pnlBottom = new javax.swing.JPanel();
        pnlTop.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblRepositoryName = new javax.swing.JLabel();
        lblRepositoryName.setText("Repository Name:");
        lblRepositoryName.setToolTipText("Enter a name to recognize this repository.");
        lblCloneAddress = new javax.swing.JLabel();
        lblCloneAddress.setText("Repository Address:");
        lblCloneAddress.setToolTipText("Click on the button to select the path to a local repository you want to monitor.");

        txtRepositoryName = new javax.swing.JTextField();
        txtRepositoryName.setText(repositoryBean.getName());
        txtCloneAddres = new javax.swing.JTextField();
        txtCloneAddres.setText(repositoryBean.getCloneAddress());
        txtCloneAddres.setEditable(false);

        btnExploreCloneAddress = new javax.swing.JButton();
        btnExploreCloneAddress.setText("Explore");
        btnExploreCloneAddress.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exploreAddressButtonActionPerformed(evt, txtCloneAddres);
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
        monitoredRepositoriesBean.addMonitoredRepository(repositoryBean);
        PreferencesUtils.persistRepositories();
        PreferencesUtils.storePreferences(settings);
        dispose();
    }

    private void exploreAddressButtonActionPerformed(java.awt.event.ActionEvent evt, JTextField field) {
        JFileChooser fileChooser = new javax.swing.JFileChooser();
        if (field.getText() != null && !"".equals(field.getText())) {
            fileChooser.setCurrentDirectory(new File(field.getText()));
        } else {
            if (settings.getLastUsedPath() != null && !"".equals(settings.getLastUsedPath())) {
                fileChooser.setCurrentDirectory(new File(settings.getLastUsedPath()));
            } else {
                fileChooser.setCurrentDirectory(null);
            }
        }
        fileChooser.setDialogTitle("Select path to repository.");
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fileChooser.showOpenDialog(this);
        String pathChosen;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            pathChosen = file.getAbsolutePath();
            settings.setLastUsedPath(pathChosen);
            if (GitConnector.isValidRepository(pathChosen)) {
                field.setText(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(txtCloneAddres, "The specified path does not contain a valid git repository.", "Message", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    //</editor-fold>
    
    private br.uff.ic.dyevc.model.MonitoredRepositories monitoredRepositoriesBean;
    private br.uff.ic.dyevc.model.MonitoredRepository repositoryBean;
    /**
     * If true, bean will be created. Otherwise it will be modified.
     */
    boolean create;
    private javax.swing.JButton btnExploreCloneAddress;
    private javax.swing.JButton btnSaveRepository;
    private javax.swing.JButton btnCancel;
    private javax.swing.JLabel lblRepositoryName;
    private javax.swing.JLabel lblCloneAddress;
    private javax.swing.JTextField txtCloneAddres;
    private javax.swing.JTextField txtRepositoryName;
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
                .addComponent(lblCloneAddress, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(txtRepositoryName)
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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
