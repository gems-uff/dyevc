package br.uff.ic.dyevc.gui.main;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.branchhistory.controller.BranchesHistoryController;
import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.RepositoryReferencedException;
import br.uff.ic.dyevc.gui.core.AboutDialog;
import br.uff.ic.dyevc.gui.core.LogTextArea;
import br.uff.ic.dyevc.gui.core.MessageManager;
import br.uff.ic.dyevc.gui.core.RepositoryConfigWindow;
import br.uff.ic.dyevc.gui.core.SettingsWindow;
import br.uff.ic.dyevc.gui.core.StdOutErrWindow;
import br.uff.ic.dyevc.gui.graph.CommitHistoryWindow;
import br.uff.ic.dyevc.gui.graph.TopologyWindow;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.RepositoryStatus;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.monitor.RepositoryMonitor;
import br.uff.ic.dyevc.monitor.TopologyUpdater;
import br.uff.ic.dyevc.utils.ImageUtils;
import br.uff.ic.dyevc.utils.LimitLinesDocumentListener;
import br.uff.ic.dyevc.utils.TableColumnAdjuster;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultCaret;
import javax.swing.WindowConstants;

/**
 * Application's main window
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
        addListeners();
        minimizeToTray();
        startMonitors();
        addEasternEgg();
    }

    // <editor-fold defaultstate="collapsed" desc="private variables">
    private javax.swing.JDialog                         dlgAbout;
    private javax.swing.JFrame                          frameSettings;
    private StdOutErrWindow                             stdOutWindow;
    private javax.swing.JPanel                          pnlMain;
    private javax.swing.JScrollPane                     jScrollPane1;
    private javax.swing.JScrollPane                     jScrollPaneMessages;
    private LogTextArea                                 jTextAreaMessages;
    private br.uff.ic.dyevc.model.MonitoredRepositories monitoredRepositories;
    private javax.swing.JTable                          repoTable;
    private TableColumnAdjuster                         tca;

    // Menu variables
    private javax.swing.JMenuBar       jMenuBar;
    private JPopupMenu                 jPopupRepoTable;
    private JPopupMenu                 jPopupTextAreaMessages;
    private PopupMenu                  trayPopup;
    private TrayIcon                   trayIcon;
    private RepositoryMonitor          repositoryMonitor;
    private TopologyUpdater            topologyUpdater;
    private int                        lastMessagesCount = 0;
    private LimitLinesDocumentListener documentListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="initComponents">
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("DyeVC");
        setSize(new java.awt.Dimension(400, 400));
        setMinimumSize(new java.awt.Dimension(400, 400));
        setName("MainWindow");    // NOI18N
        setIconImages(getDyeVCImages());
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);

        stdOutWindow          = new StdOutErrWindow();

        dlgAbout              = new AboutDialog(this, rootPaneCheckingEnabled);
        frameSettings         = new SettingsWindow();
        monitoredRepositories = MonitoredRepositories.getInstance();

        pnlMain               = new javax.swing.JPanel();
        pnlMain.setBorder(javax.swing.BorderFactory.createTitledBorder("Monitored repositories"));

        jScrollPane1 = new javax.swing.JScrollPane();

        repoTable    = new javax.swing.JTable(monitoredRepositories);
        repoTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        repoTable.setDefaultRenderer(MonitoredRepository.class, new RepositoryRenderer());
        repoTable.setDefaultRenderer(String.class, new StringRenderer());
        repoTable.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        repoTable.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        repoTable.setAutoCreateRowSorter(true);
        repoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tca = new TableColumnAdjuster(repoTable);
        tca.adjustColumns();
        repoTable.setPreferredScrollableViewportSize(repoTable.getPreferredSize());
        repoTable.setRowHeight(36);

        jScrollPane1.setViewportView(repoTable);

        jScrollPaneMessages = new javax.swing.JScrollPane();
        jScrollPaneMessages.setBorder(javax.swing.BorderFactory.createTitledBorder("Messages"));
        jTextAreaMessages = new LogTextArea();
        jTextAreaMessages.setColumns(20);
        jTextAreaMessages.setRows(5);

        // this is to scroll messages automatically
        DefaultCaret caret = (DefaultCaret)jTextAreaMessages.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        documentListener = new LimitLinesDocumentListener(IConstants.DEFAULT_MAX_MESSAGE_LINES);
        jTextAreaMessages.getDocument().addDocumentListener(documentListener);

        jScrollPaneMessages.setViewportView(jTextAreaMessages);
        MessageManager manager = MessageManager.initialize(jTextAreaMessages);
        manager.addMessage("DyeVC started.");
        manager.addMessage("To see console windows, click on View -> Console Window.");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE).addComponent(
                jScrollPaneMessages));
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                pnlMainLayout.createSequentialGroup().addContainerGap().addComponent(
                    jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jScrollPaneMessages, javax.swing.GroupLayout.PREFERRED_SIZE, 150,
                    javax.swing.GroupLayout.PREFERRED_SIZE)));

        buildMainMenu();
        buildRepoTablePopup();
        buildTextAreaPopup();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE).addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                        layout.createSequentialGroup().addContainerGap().addContainerGap(
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE).addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                        layout.createSequentialGroup().addContainerGap().addContainerGap(
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
        pack();
    }    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="tray icon">

    /**
     * Minimizes the application to system tray
     */
    private void minimizeToTray() {
        WindowEvent ev = new WindowEvent(this, WindowEvent.WINDOW_STATE_CHANGED, NORMAL, ICONIFIED);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ev);
    }

    /**
     * Builds tray icon and menu
     */
    private void showTrayIcon() {

        // Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            LoggerFactory.getLogger(MainWindow.class).warn("Your system does not support tray icons.");
            setVisible(true);

            return;
        }

        trayPopup = new PopupMenu();
        trayIcon  = new TrayIcon(getDyeVCImage(), "DyeVC Application", trayPopup);
        final SystemTray tray = SystemTray.getSystemTray();


        // Create a pop-up menu components
        MenuItem showMainWindowItem = new MenuItem("Show Main Window");
        showMainWindowItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntshowMainWindowActionPerformed();
            }
        });

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntAboutActionPerformed(evt);
            }
        });

        MenuItem mntExitItem = new MenuItem("Exit Application");
        mntExitItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntExitActionPerformed(evt);
            }
        });

        // Add components to pop-up menu
        trayPopup.add(showMainWindowItem);
        trayPopup.addSeparator();
        trayPopup.add(aboutItem);
        trayPopup.addSeparator();
        trayPopup.add(mntExitItem);

        trayIcon.setPopupMenu(trayPopup);
        trayIcon.setToolTip("DyeVC Application");

        trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON3) {    // right button
                    try {
                        trayPopup.show(pnlMain, e.getX(), e.getY());
                    } catch (java.lang.IllegalArgumentException ex) {

                        // IllegalArgument suppressed
                    }
                } else {                                      // any other button
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
            trayIcon.displayMessage(
                "DyeVC",
                "DyeVC is running in background.\nClick on the icon to view application's console\nand configure settings.",
                TrayIcon.MessageType.INFO);
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
     * Gets application images and returns it as list.
     *
     * @return
     */
    private List<Image> getDyeVCImages() {
        List<Image> images = new ArrayList<Image>();
        images.add(getDyeVCImage());

        return images;
    }

    /**
     * Gets application image and returns it as an Image object.
     *
     * @return
     */
    private Image getDyeVCImage() {
        return ImageUtils.getInstance().getImage("DyeVCIcon_16.png");
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="main menu">

    /**
     * This method creates the menu bar
     */
    @SuppressWarnings("unchecked")
    private void buildMainMenu() {
        jMenuBar = new javax.swing.JMenuBar();

        // <editor-fold defaultstate="collapsed" desc="file">
        javax.swing.JMenu mnuFile = new javax.swing.JMenu();
        mnuFile.setText("File");

        javax.swing.JMenuItem mntAddProject = new javax.swing.JMenuItem();
        mntAddProject.setText("Add Project");
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

        mnuFile.addSeparator();

        javax.swing.JMenuItem mntCheckAllNow = new javax.swing.JMenuItem();
        mntCheckAllNow.setText("Check All Repositories Now");
        mntCheckAllNow.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntCheckAllNowActionPerformed(evt);
            }
        });
        mnuFile.add(mntCheckAllNow);

        mnuFile.addSeparator();

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

        // </editor-fold>

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

        // </editor-fold>

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

        // </editor-fold>

        setJMenuBar(jMenuBar);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="main menu events">
    private void mntAddProjectActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            new RepositoryConfigWindow(monitoredRepositories, null, repositoryMonitor).setVisible(true);
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
            new RepositoryConfigWindow(monitoredRepositories, getSelectedRepository(),
                                       repositoryMonitor).setVisible(true);
        } catch (DyeVCException ex) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable ex) {
                    LoggerFactory.getLogger(MainWindow.class).error(null, ex);
                }
            });
        }
    }

    private void mntShowLogActionPerformed(ActionEvent evt) {
        new CommitHistoryWindow(getSelectedRepository()).setVisible(true);
    }

    private void mntShowTopologyActionPerformed(ActionEvent evt) {
        MonitoredRepository rep = getSelectedRepository();

        // Verify if system name was specified.
        if ("".equals(rep.getSystemName()) || "no name".equals(rep.getSystemName())) {
            JOptionPane.showMessageDialog(
                this,
                "This clone doesn't have a system name configured. Edit its configuration and set a system name.",
                "Error", JOptionPane.ERROR_MESSAGE);

            return;
        }

        new TopologyWindow(getSelectedRepository().getSystemName(), rep.getId()).setVisible(true);
    }

    private void mntShowBranchesHistoryActionPerformed(ActionEvent evt) {
        BranchesHistoryController branchesHistoryController = new BranchesHistoryController(getSelectedRepository());
        branchesHistoryController.execute();

        // Main main = new Main(getSelectedRepository());
        // main.executar();
    }

    private void mntRemoveProjectActionPerformed(ActionEvent evt) {
        MonitoredRepository rep = getSelectedRepository();
        int                 n   = JOptionPane.showConfirmDialog(repoTable,
                                      "Do you really want to stop monitoring " + rep.getName() + "?",
                                      "Confirm removal", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (n == JOptionPane.YES_OPTION) {
            try {
                monitoredRepositories.removeMonitoredRepository(rep);
            } catch (RepositoryReferencedException rre) {
                StringBuilder message = new StringBuilder();
                message.append("DyeVC has stopped monitoring clone <").append(rep.getName()).append(
                    "> with id <").append(rep.getId()).append(
                    ">\nHowever, it is still in the topology because it is referenced by the following clone(s): ");

                for (RepositoryInfo info : rre.getRelatedRepositories()) {
                    message.append("\n<").append(info.getCloneName()).append(">, id: <").append(info.getId()).append(
                        ">, located at host <").append(info.getHostName()).append(">");
                }

                JOptionPane.showMessageDialog(this, message.toString(), "Information", JOptionPane.INFORMATION_MESSAGE);
            } catch (DyeVCException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "An error occurred while trying to remove the repository. Please try again later.  Access \"View -> Console Window\" for details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mntCheckAllNowActionPerformed(ActionEvent evt) {
        if (repositoryMonitor.getState().equals(Thread.State.TIMED_WAITING)) {
            repositoryMonitor.interrupt();
        } else {
            JOptionPane.showMessageDialog(repoTable, "Monitor is busy now. Please try again later.", "Information",
                                          JOptionPane.OK_OPTION);
        }
    }

    private void mntCheckProjectActionPerformed(ActionEvent evt) {
        MonitoredRepository rep = getSelectedRepository();
        repositoryMonitor.addRepositoryToMonitor(rep);

        if (repositoryMonitor.getState().equals(Thread.State.TIMED_WAITING)) {
            repositoryMonitor.interrupt();
        } else {
            JOptionPane.showMessageDialog(repoTable, "Monitor is busy now. Repository was added to the monitor queue.",
                                          "Information", JOptionPane.OK_OPTION);
        }
    }

    /**
     * Closes resources and exit from DyeVC
     */
    private void mntExitActionPerformed(java.awt.event.ActionEvent evt) {
        monitoredRepositories.closeRepositories();
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
    private void buildRepoTablePopup() {
        jPopupRepoTable = new JPopupMenu();
        JMenuItem mntCheckProject = new javax.swing.JMenuItem();
        mntCheckProject.setText("Check Project");
        mntCheckProject.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntCheckProjectActionPerformed(evt);
            }
        });
        jPopupRepoTable.add(mntCheckProject);

        JMenuItem mntEditProject = new javax.swing.JMenuItem();
        mntEditProject.setText("View Project Configuration");
        mntEditProject.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntEditProjectActionPerformed(evt);
            }
        });
        jPopupRepoTable.add(mntEditProject);

        JMenuItem mntShowLog = new javax.swing.JMenuItem();
        mntShowLog.setText("Show Commit History");
        mntShowLog.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntShowLogActionPerformed(evt);
            }
        });
        jPopupRepoTable.add(mntShowLog);

        JMenuItem mntShowTopology = new javax.swing.JMenuItem();
        mntShowTopology.setText("Show Topology");
        mntShowTopology.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntShowTopologyActionPerformed(evt);
            }
        });
        jPopupRepoTable.add(mntShowTopology);

        JMenuItem mntShowBranchesHistory = new javax.swing.JMenuItem();
        mntShowBranchesHistory.setText("Show Branches History");
        mntShowBranchesHistory.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntShowBranchesHistoryActionPerformed(evt);
            }
        });
        jPopupRepoTable.add(mntShowBranchesHistory);

        jPopupRepoTable.addSeparator();
        JMenuItem mntRemoveProject = new javax.swing.JMenuItem();
        mntRemoveProject.setText("Remove Project");
        mntRemoveProject.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntRemoveProjectActionPerformed(evt);
            }
        });
        jPopupRepoTable.add(mntRemoveProject);
    }

    /**
     * Shows up a popup menu when the user clicks with the right button
     *
     * @param evt
     */
    private void repoTableMouseClicked(java.awt.event.MouseEvent evt) {
        JTable table = (JTable)evt.getSource();
        if (evt.getButton() == MouseEvent.BUTTON3) {
            jPopupRepoTable.show(evt.getComponent(), evt.getX(), evt.getY());
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
     * Gets the selected repository in Jlist
     *
     * @return name of the selected repository
     */
    private MonitoredRepository getSelectedRepository() {
        int selectedRow = repoTable.getSelectedRow();

        return (MonitoredRepository)repoTable.getValueAt(selectedRow, 0);
    }

    /**
     * Starts the repository monitor.
     */
    private void startMonitors() {
        repositoryMonitor = new RepositoryMonitor(this);
        topologyUpdater   = new TopologyUpdater();
    }

    /**
     * Displays the specified message as a ballon in tray icon.
     *
     * @param message the message to be displayed.
     */
    public void notifyMessage(String message) {
        trayIcon.displayMessage("DyeVC", message + " Click on this balloon if you want to see details.",
                                TrayIcon.MessageType.WARNING);
        MessageManager.getInstance().addMessage(message);
    }

    /**
     * Displays messages from the status list as a balloon in tray icon.
     *
     * @param repStatusList the list of messages to be displayed.
     */
    public void notifyMessages(List<RepositoryStatus> repStatusList) {
        LoggerFactory.getLogger(MainWindow.class).trace("notifyMessages -> Entry");

        int countRepsWithMessages = 0;
        for (Iterator<RepositoryStatus> it = repStatusList.iterator(); it.hasNext(); ) {
            RepositoryStatus repositoryStatus = it.next();
            if (repositoryStatus.isInvalid()) {
                countRepsWithMessages++;
            } else {
                if ((repositoryStatus.getInvalidBranchesCount() > 0)
                        || (repositoryStatus.getNonSyncedBranchesCount() > 0)) {
                    countRepsWithMessages++;
                }
            }
        }

        if (countRepsWithMessages != lastMessagesCount) {
            notifyMessage("There are messages on " + countRepsWithMessages + " repositories.");
            lastMessagesCount = countRepsWithMessages;
        }

        repoTable.repaint();

        LoggerFactory.getLogger(MainWindow.class).trace("notifyMessages -> Exit");
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="addListeners">

    /**
     * Adds listeners to window components.
     */
    private void addListeners() {
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent evt) {
                handleWindowStateChanged(evt);
            }
        });

        addWindowListener(new java.awt.event.WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray();
            }

            @Override
            public void windowClosed(WindowEvent e) {}

            @Override
            public void windowIconified(WindowEvent e) {}

            @Override
            public void windowDeiconified(WindowEvent e) {}

            @Override
            public void windowActivated(WindowEvent e) {}

            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        repoTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repoTableMouseClicked(evt);
            }
        });

        repoTable.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent evt) {
                int row = repoTable.rowAtPoint(evt.getPoint());
                repoTable.setRowSelectionInterval(row, row);
            }
        });

        jTextAreaMessages.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextAreaMessagesMouseClicked(evt);
            }
        });

        repoTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                tca.adjustColumns();
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="eastern_egg -> CTRL + C">
    private Action checkAction;

    private void addEasternEgg() {

        // Eastern egg -> Activated with CTRL+C only when mouse focus is on menu
        checkAction = new CheckRepositoryAction("Check");
        InputMap  inputMap       = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap      = getRootPane().getActionMap();

        KeyStroke ctrlOKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, false);
        inputMap.put(ctrlOKeyStroke, "checkAction");
        actionMap.put("checkAction", checkAction);
    }

    /**
     * Class description
     * @author         Cristiano Cesario
     */
    @SuppressWarnings("serial")
    private class CheckRepositoryAction extends AbstractAction {
        /**
         * Constructs ...
         *
         * @param name
         */
        public CheckRepositoryAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(MainWindow.this, "Pressed CTRL + C", "Key Pressed",
                                          JOptionPane.INFORMATION_MESSAGE);

            // TODO implement jdialog to check a given git repository url;
        }
    }

    // </editor-fold>
}
