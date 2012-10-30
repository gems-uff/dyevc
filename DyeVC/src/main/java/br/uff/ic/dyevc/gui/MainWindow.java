package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.monitor.RepositoryMonitor;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultCaret;
import org.slf4j.LoggerFactory;

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
        minizeToTray();
        startMonitor();
    }

    // <editor-fold defaultstate="collapsed" desc="private variables">      
    private javax.swing.JDialog dlgAbout;
    private javax.swing.JFrame frameSettings;
    private StdOutErrWindow stdOutWindow;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneMessages;
    private javax.swing.JTextArea jTextAreaMessages;
    private br.uff.ic.dyevc.model.MonitoredRepositories monitoredRepositoriesBean1;
    private javax.swing.JList repoList;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    //Variáveis de menu
    private javax.swing.JMenuBar jMenuBar;
    private JPopupMenu jPopupRepoList;
    private JPopupMenu jPopupTextAreaMessages;
    private PopupMenu trayPopup;
    private TrayIcon trayIcon;
    private RepositoryMonitor monitor;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="initComponents">                          
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DyeVC");
        setSize(new java.awt.Dimension(400, 400));
        setMinimumSize(new java.awt.Dimension(400, 400));
        setName("MainWindow"); // NOI18N
        setIconImage(getDyeVCImage());
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
        
        stdOutWindow = new StdOutErrWindow();

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
        org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, monitoredRepositoriesBean1, eLProperty, repoList, MonitoredRepositories.MONITORED_PROJECTS);
        jListBinding.setDetailBinding(org.jdesktop.beansbinding.ELProperty.create("${name}@${cloneAddress}"));
        bindingGroup.addBinding(jListBinding);

        repoList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repoListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(repoList);

        jScrollPaneMessages = new javax.swing.JScrollPane();
        jScrollPaneMessages.setBorder(javax.swing.BorderFactory.createTitledBorder("Messages"));
        jTextAreaMessages = new javax.swing.JTextArea();
        jTextAreaMessages.setColumns(20);
        jTextAreaMessages.setRows(5);
        //this is to scroll messages automatically
        DefaultCaret caret = (DefaultCaret)jTextAreaMessages.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        jTextAreaMessages.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextAreaMessagesMouseClicked(evt);
            }
        });

        jScrollPaneMessages.setViewportView(jTextAreaMessages);
        MessageManager manager = MessageManager.initialize(jTextAreaMessages);
        manager.addMessage("DyeVC started.");
        manager.addMessage("To see console windows, click on View -> Console Window.");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
                pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                .addComponent(jScrollPaneMessages));
        pnlMainLayout.setVerticalGroup(
                pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneMessages, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)));

        buildMainMenu();
        buildRepoListPopup();
        buildTextAreaPopup();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));

        bindingGroup.bind();
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent evt) {
                handleWindowStateChanged(evt);
            }
        });
        pack();
    }// </editor-fold>                        

    // <editor-fold defaultstate="collapsed" desc="tray icon">
    /**
     * Minimizes the application to system tray
     */
    private void minizeToTray() {
        WindowEvent ev = new WindowEvent(this, WindowEvent.WINDOW_STATE_CHANGED, NORMAL, ICONIFIED);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ev);
    }

    /**
     * Builds tray icon and menu
     */
    private void showTrayIcon() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            LoggerFactory.getLogger(MainWindow.class).warn("Your system does not support tray icons.");
            setVisible(true);
            return;
        }
        trayPopup = new PopupMenu();
        trayIcon =
                new TrayIcon(getDyeVCImage(), "DyeVC Application", trayPopup);
        final SystemTray tray = SystemTray.getSystemTray();


        // Create a pop-up menu components
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntAboutActionPerformed(evt);
            }
        });
        MenuItem showMainWindowItem = new MenuItem("Show Main Window");
        showMainWindowItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntshowMainWindowActionPerformed();
            }
        });

        //Add components to pop-up menu
        trayPopup.add(showMainWindowItem);
        trayPopup.addSeparator();
        trayPopup.add(aboutItem);

        trayIcon.setPopupMenu(trayPopup);
        trayIcon.setToolTip("DyeVC Application");

        trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON3) { //right button
                    try {
                        trayPopup.show(pnlMain, e.getX(), e.getY());
                    } catch (java.lang.IllegalArgumentException ex) {
                        //IllegalArgument suppressed
                    }
                } else { //any other button
                    mntshowMainWindowActionPerformed();
                }
            }
        });
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mntshowMainWindowActionPerformed();
            }
        });
        try {
            tray.add(trayIcon);
            trayIcon.displayMessage("DyeVC", "DyeVC is running in background.\nClick on the icon to view application's console\nand configure settings.", TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            LoggerFactory.getLogger(MainWindow.class).warn("TrayIcon could not be added.", e);
        }
    }

    /**
     * Handles changes in window state, minimizing application to tray when
     * window is iconified and restoring it when maximized.
     *
     * @param evt the event that has occurred.
     */
    private void handleWindowStateChanged(WindowEvent evt) {
        if (evt.getNewState() == ICONIFIED) {
            setVisible(false);
            showTrayIcon();
        }
        if (evt.getNewState() == MAXIMIZED_BOTH) {
            setVisible(true);
        }
        if (evt.getNewState() == NORMAL) {
            setVisible(true);
        }
    }

    /**
     * Gets application image and returns it as an Image object.
     *
     * @return
     */
    private Image getDyeVCImage() {
        return Toolkit.getDefaultToolkit().getImage(getClass().getResource("/br/uff/ic/dyevc/images/DyeVCIcon.png"));
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="main menu">                          
    /**
     * This method creates the menu bar
     */
    @SuppressWarnings("unchecked")
    private void buildMainMenu() {
        jMenuBar = new javax.swing.JMenuBar();

        //<editor-fold defaultstate="collapsed" desc="file">                          
        javax.swing.JMenu mnuFile = new javax.swing.JMenu();
        mnuFile.setText("File");

        javax.swing.JMenuItem mntAddProject = new javax.swing.JMenuItem();
        mntAddProject.setText(" Add Project");
        mntAddProject.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntAddProjectActionPerformed(evt);
            }
        });
        mnuFile.add(mntAddProject);

        javax.swing.JMenuItem mntSettings = new javax.swing.JMenuItem();
        mntSettings.setText("Settings");
        mntSettings.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntSettingsActionPerformed(evt);
            }
        });
        mnuFile.add(mntSettings);

        javax.swing.JMenuItem mntExit = new javax.swing.JMenuItem();
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
        //</editor-fold>                          

        // <editor-fold defaultstate="collapsed" desc="view">                          
        javax.swing.JMenu mnuView = new javax.swing.JMenu();
        mnuView.setText("View");

        javax.swing.JMenuItem mntConsole = new javax.swing.JMenuItem();
        mntConsole.setText("Console Window");
        mntConsole.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntConsoleActionPerformed(evt);
            }
        });
        mnuView.add(mntConsole);

        jMenuBar.add(mnuView);
        //</editor-fold>                          
        
        // <editor-fold defaultstate="collapsed" desc="help">                          
        javax.swing.JMenu mnuHelp = new javax.swing.JMenu();
        mnuHelp.setText("Help");

        javax.swing.JMenuItem mntAbout = new javax.swing.JMenuItem();
        mntAbout.setText("About");
        mntAbout.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mntAbout);

        jMenuBar.add(mnuHelp);
        //</editor-fold>                          
        
        setJMenuBar(jMenuBar);
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="main menu events">                          
    private void mntAddProjectActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            new RepositoryConfigWindow(monitoredRepositoriesBean1, null).setVisible(true);
        } catch (DyeVCException ex) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable ex) {
                    LoggerFactory.getLogger(MainWindow.class).error("Error creating RepositoryConfigWindow.", ex);
                }
            });
        }
    }

    private void mntEditProjectActionPerformed(ActionEvent evt) {
        try {
            new RepositoryConfigWindow(monitoredRepositoriesBean1, getSelectedRepName()).setVisible(true);
        } catch (DyeVCException ex) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable ex) {
                    LoggerFactory.getLogger(MainWindow.class).error(null, ex);
                }
            });
        }
    }

    private void mntRemoveProjectActionPerformed(ActionEvent evt) {
        String repName = getSelectedRepName();
        int n = JOptionPane.showConfirmDialog(repoList, "Do you really want to stop monitoring " + repName + "?", "Confirm removal", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (n == JOptionPane.YES_OPTION) {
            monitoredRepositoriesBean1.removeMonitoredRepository(repName);
            PreferencesUtils.persistRepositories(monitoredRepositoriesBean1);
        }
    }

    private void mntExitActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }

    private void mntAboutActionPerformed(java.awt.event.ActionEvent evt) {
        dlgAbout.setVisible(true);
    }

    private void mntConsoleActionPerformed(java.awt.event.ActionEvent evt) {
        stdOutWindow.setVisible(true);
    }

    private void mntshowMainWindowActionPerformed() {
        setVisible(true);
        setState(NORMAL);
        SystemTray.getSystemTray().remove(trayIcon);
    }

    private void mntSettingsActionPerformed(java.awt.event.ActionEvent evt) {
        frameSettings.setVisible(true);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="repoList menu">  
    private void buildRepoListPopup() {
        jPopupRepoList = new JPopupMenu();
        JMenuItem mntEditProject = new javax.swing.JMenuItem();
        mntEditProject.setText(" Edit Project");
        mntEditProject.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntEditProjectActionPerformed(evt);
            }
        });
        jPopupRepoList.add(mntEditProject);

        JMenuItem mntRemoveProject = new javax.swing.JMenuItem();
        mntRemoveProject.setText(" Remove Project");
        mntRemoveProject.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntRemoveProjectActionPerformed(evt);
            }
        });
        jPopupRepoList.add(mntRemoveProject);
    }
    
        /**
     * Shows up a popup menu when the user clicks with the right button
     *
     * @param evt
     */
    private void repoListMouseClicked(java.awt.event.MouseEvent evt) {
        JList list = (JList) evt.getSource();
        if (evt.getButton() == MouseEvent.BUTTON3) {
            list.setSelectedIndex(list.locationToIndex(evt.getPoint()));
            jPopupRepoList.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="textArea menu">  
    private void buildTextAreaPopup() {
        jPopupTextAreaMessages = new JPopupMenu();
        JMenuItem mntClear = new javax.swing.JMenuItem();
        mntClear.setText("Clear Messages");
        mntClear.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MessageManager.getInstance().clearMessages();
            }
        });
        jPopupTextAreaMessages.add(mntClear);
    }
    
   /**
     * Shows up a popup menu when the user clicks with the right button
     *
     * @param evt
     */
    private void jTextAreaMessagesMouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON3) {
            jPopupTextAreaMessages.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="other stuff">   
    /**
     * Gets the name of selected repository in Jlist
     *
     * @return name of the selected repository
     */
    private String getSelectedRepName() {
        String selectedRep = (String) repoList.getSelectedValue();
        String repName = selectedRep.substring(0, selectedRep.indexOf("@"));
        return repName;
    }

    /**
     * Starts the repository monitor.
     */
    private void startMonitor() {
        monitor = new RepositoryMonitor(this);
    }
    
    public void notifyMessage(String message) {
            trayIcon.displayMessage("DyeVC", message, TrayIcon.MessageType.WARNING);
    }
    // </editor-fold>
}
