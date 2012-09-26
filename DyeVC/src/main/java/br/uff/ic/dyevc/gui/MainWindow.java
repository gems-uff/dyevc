package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.beans.MonitoredRepositoriesBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 *
 * @author Cristiano
 */
public class MainWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = 6569285531097330071L;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">                          
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DyeVC");
        setSize(new java.awt.Dimension(400, 300));
        setMinimumSize(new java.awt.Dimension(400, 300));
        setName("MainWindow"); // NOI18N
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);

        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
        dlgAbout = new AboutDialog(this, rootPaneCheckingEnabled);
        frameSettings = new SettingsWindow();
        monitoredRepositoriesBean1 = PreferencesUtils.loadMonitoredRepositories();

        pnlMain = new javax.swing.JPanel();
        pnlMain.setBorder(javax.swing.BorderFactory.createTitledBorder("Monitored repositories"));

        jScrollPane1 = new javax.swing.JScrollPane();

        repoList = new javax.swing.JList();
        repoList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${monitoredProjects}");
        org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, monitoredRepositoriesBean1, eLProperty, repoList, MonitoredRepositoriesBean.MONITORED_PROJECTS);
        jListBinding.setDetailBinding(org.jdesktop.beansbinding.ELProperty.create("\"${name}\" at ${cloneAddress}"));
        bindingGroup.addBinding(jListBinding);

        repoList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repoListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(repoList);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
                pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE));
        pnlMainLayout.setVerticalGroup(
                pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE));

        buildMenu();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                //                    .addComponent(workingPathFileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                //                    .addComponent(workingPathFileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));

        bindingGroup.bind();

        pack();
    }// </editor-fold>                        

    private void repoListMouseClicked(java.awt.event.MouseEvent evt) {
        //TODO implementar a chamada à janela para editar as informações do projeto.
        JList list = (JList) evt.getSource();
        if (evt.getClickCount() == 2) {          // Double-click
            // Get item index
            int index = list.locationToIndex(evt.getPoint());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="main method">                          
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the System look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="buildMenu">                          
    //Variáveis de menu
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenuItem mntAbout;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mntAddProject;
    private javax.swing.JMenuItem mntExit;
    private javax.swing.JMenuItem mntSettings;

    /**
     * This method creates the menu bar
     */
    @SuppressWarnings("unchecked")
    private void buildMenu() {
        jMenuBar = new javax.swing.JMenuBar();

        mnuFile = new javax.swing.JMenu();
        mnuFile.setText("File");

        mntAddProject = new javax.swing.JMenuItem();
        mntAddProject.setText(" Add Project");
        mntAddProject.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntAddProjectActionPerformed(evt);
            }
        });
        mnuFile.add(mntAddProject);

        mntSettings = new javax.swing.JMenuItem();
        mntSettings.setText("Settings");
        mntSettings.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntSettingsActionPerformed(evt);
            }
        });
        mnuFile.add(mntSettings);

        mntExit = new javax.swing.JMenuItem();
        mntExit.setText("Exit");
        mntExit.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        mntExit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntExitActionPerformed(evt);
            }
        });
        mnuFile.add(mntExit);
        jMenuBar.add(mnuFile);


        mnuHelp = new javax.swing.JMenu();
        mnuHelp.setText("Help");

        mntAbout = new javax.swing.JMenuItem();
        mntAbout.setText("About");
        mntAbout.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mntAbout);

        jMenuBar.add(mnuHelp);
        setJMenuBar(jMenuBar);
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="menu event actions">                          
    private void mntAddProjectActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            new RepositoryConfigWindow(monitoredRepositoriesBean1, null).setVisible(true);
        } catch (DyeVCException ex) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
        bindingGroup.bind();
    }

    private void mntExitActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }

    private void mntAboutActionPerformed(java.awt.event.ActionEvent evt) {
        dlgAbout.setVisible(true);
    }

    private void mntSettingsActionPerformed(java.awt.event.ActionEvent evt) {
        frameSettings.setVisible(true);
    }
    // </editor-fold>
    //Variaveis refatoradas
    private javax.swing.JDialog dlgAbout;
    private javax.swing.JFrame frameSettings;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JScrollPane jScrollPane1;
    private br.uff.ic.dyevc.beans.MonitoredRepositoriesBean monitoredRepositoriesBean1;
    private javax.swing.JList repoList;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration                   
}