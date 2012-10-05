/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application;

import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 *
 * @author Cristiano
 */
public class TemplateGUI extends javax.swing.JFrame {

    private static final long serialVersionUID = 6569285531097330071L;

    /**
     * Creates new form TemplateGUI
     */
    public TemplateGUI() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        applicationSettingsBean1 = PreferencesUtils.loadPreferences();
        AboutDialog = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        AboutOK = new javax.swing.JButton();
        applicationPropertiesBean1 = new br.uff.ic.dyevc.beans.ApplicationPropertiesBean();
        monitoredRepositoriesBean1 = new br.uff.ic.dyevc.model.MonitoredRepositories();
        settingsWindow = new javax.swing.JFrame();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        workingPath = new javax.swing.JTextField();
        refreshRate = new javax.swing.JTextField();
        exploreWPathButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        addRepositoryWindow = new javax.swing.JFrame();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        repositoryNameTxt = new javax.swing.JTextField();
        cloneAddresTxt = new javax.swing.JTextField();
        originURLTxt = new javax.swing.JTextField();
        cloneAddressExploreButton = new javax.swing.JButton();
        originURLExploreButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        saveRepository = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        repositoryBean1 = new br.uff.ic.dyevc.model.Repository();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        repoList = new javax.swing.JList();
        workingPathFileChooser = new javax.swing.JFileChooser();
        jMenuBar1 = new javax.swing.JMenuBar();
        File = new javax.swing.JMenu();
        addProjectMnuItem = new javax.swing.JMenuItem();
        settingsMnuItem = new javax.swing.JMenuItem();
        exitMnuItem = new javax.swing.JMenuItem();
        Help = new javax.swing.JMenu();
        AboutMnuItem = new javax.swing.JMenuItem();

        AboutDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        AboutDialog.setTitle("About");
        AboutDialog.setMinimumSize(new java.awt.Dimension(428, 340));
        AboutDialog.setModal(true);
        AboutDialog.setResizable(false);
        int aboutDlgWidth = AboutDialog.getWidth();
        int aboutDlgHeight = AboutDialog.getHeight();
        AboutDialog.setBounds((java.awt.Toolkit.getDefaultToolkit().getScreenSize().width-aboutDlgWidth)/2, (java.awt.Toolkit.getDefaultToolkit().getScreenSize().height-aboutDlgHeight)/2, aboutDlgWidth, aboutDlgHeight);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/uff/ic/dyevc/images/splash.png"))); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, applicationPropertiesBean1, org.jdesktop.beansbinding.ELProperty.create("${appVersion}"), jLabel1, org.jdesktop.beansbinding.BeanProperty.create("text"), "");
        bindingGroup.addBinding(binding);

        AboutOK.setText("OK");
        AboutOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(191, 191, 191)
                        .addComponent(AboutOK)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(AboutOK))
        );

        javax.swing.GroupLayout AboutDialogLayout = new javax.swing.GroupLayout(AboutDialog.getContentPane());
        AboutDialog.getContentPane().setLayout(AboutDialogLayout);
        AboutDialogLayout.setHorizontalGroup(
            AboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        AboutDialogLayout.setVerticalGroup(
            AboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AboutDialogLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        settingsWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        settingsWindow.setTitle("DyeVC Settings");
        settingsWindow.setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        settingsWindow.setMinimumSize(new java.awt.Dimension(506, 110));
        settingsWindow.setResizable(false);
        int settingsWindowWidth = settingsWindow.getWidth();
        int settingsWindowHeight = settingsWindow.getHeight();
        settingsWindow.setBounds((java.awt.Toolkit.getDefaultToolkit().getScreenSize().width-settingsWindowWidth)/2, (java.awt.Toolkit.getDefaultToolkit().getScreenSize().height-settingsWindowHeight)/2, settingsWindowWidth, settingsWindowHeight);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("Working Path:");
        jLabel2.setToolTipText("Informe o working Path");

        jLabel3.setText("Refresh Rate (s):");

        workingPath.setToolTipText("");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, applicationSettingsBean1, org.jdesktop.beansbinding.ELProperty.create("${workingPath}"), workingPath, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, applicationSettingsBean1, org.jdesktop.beansbinding.ELProperty.create("${refreshInterval}"), refreshRate, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        exploreWPathButton.setText("Explore");
        exploreWPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exploreWPathButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(exploreWPathButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(workingPath, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 76, Short.MAX_VALUE))
                    .addComponent(refreshRate))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(workingPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exploreWPathButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(refreshRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        saveButton.setText("Save");
        saveButton.setSelected(true);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        jPanel4.add(saveButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel4.add(cancelButton);

        javax.swing.GroupLayout settingsWindowLayout = new javax.swing.GroupLayout(settingsWindow.getContentPane());
        settingsWindow.getContentPane().setLayout(settingsWindowLayout);
        settingsWindowLayout.setHorizontalGroup(
            settingsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        settingsWindowLayout.setVerticalGroup(
            settingsWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsWindowLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        addRepositoryWindow.setTitle("Enter theh information about the repository to be monitored");
        addRepositoryWindow.setMinimumSize(new java.awt.Dimension(506, 110));
        int addRepositoryWindowWidth = addRepositoryWindow.getWidth();
        int addRepositoryWindowHeight = addRepositoryWindow.getHeight();
        settingsWindow.setBounds((java.awt.Toolkit.getDefaultToolkit().getScreenSize().width-addRepositoryWindowWidth)/2, (java.awt.Toolkit.getDefaultToolkit().getScreenSize().height-addRepositoryWindowHeight)/2, addRepositoryWindowWidth, addRepositoryWindowHeight);

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText("Repository Name:");

        jLabel5.setText("Clone Address:");

        jLabel6.setText("Origin URL:");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, repositoryBean1, org.jdesktop.beansbinding.ELProperty.create("${name}"), repositoryNameTxt, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, repositoryBean1, org.jdesktop.beansbinding.ELProperty.create("${cloneAddress}"), cloneAddresTxt, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, repositoryBean1, org.jdesktop.beansbinding.ELProperty.create("${originUrl}"), originURLTxt, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        cloneAddressExploreButton.setText("Explore");

        originURLExploreButton.setText("Explore");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(repositoryNameTxt)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(originURLExploreButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(originURLTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(cloneAddressExploreButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cloneAddresTxt)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(repositoryNameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cloneAddresTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cloneAddressExploreButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(originURLTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(originURLExploreButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout();
        flowLayout1.setAlignOnBaseline(true);
        jPanel7.setLayout(flowLayout1);

        saveRepository.setText("Save");
        jPanel7.add(saveRepository);

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton1);

        javax.swing.GroupLayout addRepositoryWindowLayout = new javax.swing.GroupLayout(addRepositoryWindow.getContentPane());
        addRepositoryWindow.getContentPane().setLayout(addRepositoryWindowLayout);
        addRepositoryWindowLayout.setHorizontalGroup(
            addRepositoryWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        addRepositoryWindowLayout.setVerticalGroup(
            addRepositoryWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addRepositoryWindowLayout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jMenuItem1.setText("jMenuItem1");
        jPopupMenu1.add(jMenuItem1);

        jMenuItem2.setText("jMenuItem2");
        jPopupMenu1.add(jMenuItem2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DyeVC");
        setMinimumSize(new java.awt.Dimension(400, 300));
        setName("MainWindow"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Monitored repositories"));

        repoList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        repoList.setName(""); // NOI18N

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${monitoredProjects}");
        org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, monitoredRepositoriesBean1, eLProperty, repoList, "repoNames");
        jListBinding.setDetailBinding(org.jdesktop.beansbinding.ELProperty.create("\"${name}\" at ${cloneAddress}"));
        bindingGroup.addBinding(jListBinding);

        repoList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repoListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(repoList);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
        );

        workingPathFileChooser.setCurrentDirectory(null);
        workingPathFileChooser.setDialogTitle("Select a working directory for DyeVC");
        workingPathFileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        File.setText("File");

        addProjectMnuItem.setText(" Add Project");
        addProjectMnuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProjectMnuItemActionPerformed(evt);
            }
        });
        File.add(addProjectMnuItem);

        settingsMnuItem.setText("Settings");
        settingsMnuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMnuItemActionPerformed(evt);
            }
        });
        File.add(settingsMnuItem);

        exitMnuItem.setText("Exit");
        exitMnuItem.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        exitMnuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMnuItemActionPerformed(evt);
            }
        });
        File.add(exitMnuItem);

        jMenuBar1.add(File);

        Help.setText("Help");

        AboutMnuItem.setText("About");
        AboutMnuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutMnuItemActionPerformed(evt);
            }
        });
        Help.add(AboutMnuItem);

        jMenuBar1.add(Help);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(workingPathFileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(workingPathFileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        bindingGroup.bind();

        pack();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
    }// </editor-fold>//GEN-END:initComponents

    private void exitMnuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMnuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMnuItemActionPerformed

    private void AboutOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutOKActionPerformed
        AboutDialog.setVisible(false);
    }//GEN-LAST:event_AboutOKActionPerformed

    private void AboutMnuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutMnuItemActionPerformed
        AboutDialog.setVisible(true);
    }//GEN-LAST:event_AboutMnuItemActionPerformed

    private void settingsMnuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMnuItemActionPerformed
        settingsWindow.setVisible(true);
    }//GEN-LAST:event_settingsMnuItemActionPerformed

    private void repoListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repoListMouseClicked
        //TODO implementar a chamada à janela para editar as informações do projeto.
        JList list = (JList) evt.getSource();
        if (evt.getClickCount() == 2) {          // Double-click
            // Get item index
            int index = list.locationToIndex(evt.getPoint());
        }
        
    }//GEN-LAST:event_repoListMouseClicked

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        settingsWindow.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        PreferencesUtils.storePreferences(applicationSettingsBean1);
        settingsWindow.dispose();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void exploreWPathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exploreWPathButtonActionPerformed
        int returnVal = workingPathFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = workingPathFileChooser.getSelectedFile();
            // What to do with the file, e.g. display it in a TextArea
            workingPath.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_exploreWPathButtonActionPerformed

    private void addProjectMnuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProjectMnuItemActionPerformed
        addRepositoryWindow.setVisible(true);
    }//GEN-LAST:event_addProjectMnuItemActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        addRepositoryWindow.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the System look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TemplateGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TemplateGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TemplateGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TemplateGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TemplateGUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog AboutDialog;
    private javax.swing.JMenuItem AboutMnuItem;
    private javax.swing.JButton AboutOK;
    private javax.swing.JMenu File;
    private javax.swing.JMenu Help;
    private javax.swing.JMenuItem addProjectMnuItem;
    private javax.swing.JFrame addRepositoryWindow;
    private br.uff.ic.dyevc.beans.ApplicationPropertiesBean applicationPropertiesBean1;
    private br.uff.ic.dyevc.beans.ApplicationSettingsBean applicationSettingsBean1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField cloneAddresTxt;
    private javax.swing.JButton cloneAddressExploreButton;
    private javax.swing.JMenuItem exitMnuItem;
    private javax.swing.JButton exploreWPathButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private br.uff.ic.dyevc.model.MonitoredRepositories monitoredRepositoriesBean1;
    private javax.swing.JButton originURLExploreButton;
    private javax.swing.JTextField originURLTxt;
    private javax.swing.JTextField refreshRate;
    private javax.swing.JList repoList;
    private br.uff.ic.dyevc.model.Repository repositoryBean1;
    private javax.swing.JTextField repositoryNameTxt;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton saveRepository;
    private javax.swing.JMenuItem settingsMnuItem;
    private javax.swing.JFrame settingsWindow;
    private javax.swing.JTextField workingPath;
    private javax.swing.JFileChooser workingPathFileChooser;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
