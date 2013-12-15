package br.uff.ic.dyevc.gui.core;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.gui.utils.GUIManager;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.monitor.RepositoryMonitor;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import br.uff.ic.dyevc.utils.PreferencesManager;
import br.uff.ic.dyevc.utils.RepositoryConverter;
import br.uff.ic.dyevc.utils.StringUtils;
import br.uff.ic.dyevc.utils.SystemUtils;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.awt.HeadlessException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author Cristiano
 */
public class RepositoryConfigWindow extends javax.swing.JFrame {
    private static final long       serialVersionUID = -5327813882224088396L;
    private ApplicationSettingsBean settings;
    private Topology                topology;
    private RepositoryConverter     converter;
    private final GUIManager        guiManager;

    /**
     * Creates new form RepositoryConfigWindow
     * @param repository The repository to be configured. If null, will create a new one.
     */
    public RepositoryConfigWindow(MonitoredRepository repository) throws DyeVCException {
        this.guiManager = GUIManager.getInstance();

        if (repository != null) {
            create         = false;
            repositoryBean = repository;
        } else {
            create         = true;
            repositoryBean = new MonitoredRepository(StringUtils.generateRepositoryId());
        }

        initComponents();

        if (!create) {
            JOptionPane.showMessageDialog(this,
                                          "Configurations can only be viewed.\n"
                                          + "Remove it and create a new one if you want to change it.", "Information",
                                              JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">
    @SuppressWarnings("unchecked")
    private void initComponents() {
        settings = PreferencesManager.getInstance().loadPreferences();

        try {
            topology = new TopologyDAO().readTopology();
        } catch (ServiceException ex) {
            LoggerFactory.getLogger(RepositoryConfigWindow.class).warn(
                "Could not retrieve topology from database. It will not be possible to show the list of known systems.",
                ex);
            topology = new Topology();
        }

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        if (create) {
            setTitle("Creating a new monitoring configuration");
        } else {
            setTitle("Viewing a monitoring configuration");
        }

        setSize(new java.awt.Dimension(528, 180));
        setMinimumSize(new java.awt.Dimension(528, 180));
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);

        pnlTop    = new javax.swing.JPanel();
        pnlBottom = new javax.swing.JPanel();
        pnlTop.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblSystemName = new javax.swing.JLabel();
        lblSystemName.setText("System Name:");
        lblSystemName.setToolTipText("Enter the globally known name to recognize this repository.");
        lblRepositoryName = new javax.swing.JLabel();
        lblRepositoryName.setText("Clone Name:");
        lblRepositoryName.setToolTipText("Clone name is extracted from the path to your clone.");
        lblCloneAddress = new javax.swing.JLabel();
        lblCloneAddress.setText("Clone Address:");
        lblCloneAddress.setToolTipText("Click on the button to select the path to a clone you want to monitor.");

        cmbSystemName = new javax.swing.JComboBox<String>(topology.getSystems().toArray(new String[0]));
        cmbSystemName.setEditable(true);
        cmbSystemName.setSelectedItem(repositoryBean.getSystemName());
        cmbSystemName.setEditable(create);
        cmbSystemName.setEnabled(create);
        txtRepositoryName = new javax.swing.JTextField();
        txtRepositoryName.setText(repositoryBean.getName());
        txtRepositoryName.setEditable(false);
        txtRepositoryName.setEnabled(false);
        txtCloneAddres = new javax.swing.JTextField();
        txtCloneAddres.setText(repositoryBean.getCloneAddress());
        txtCloneAddres.setEditable(false);

        btnExploreCloneAddress = new javax.swing.JButton();
        btnExploreCloneAddress.setText("Explore");
        btnExploreCloneAddress.setEnabled(create);
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

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="event handlers">
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void btnSaveRepositoryActionPerformed(java.awt.event.ActionEvent evt) {

        // Verify if system name was specified.
        if ((cmbSystemName.getSelectedItem() == null) || "".equals(cmbSystemName.getSelectedItem().toString())
                || "no name".equalsIgnoreCase(cmbSystemName.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(this, "System name is a required field.", "Error", JOptionPane.ERROR_MESSAGE);
            cmbSystemName.requestFocus();

            return;
        }

        String systemName = cmbSystemName.getSelectedItem().toString().toLowerCase();

        if (!validateCloneName()) {
            return;
        }

        if (!validateRepositoryAlreadyExists(systemName)) {
            return;
        }

        repositoryBean.setSystemName(systemName);
        repositoryBean.setName(txtRepositoryName.getText());
        repositoryBean.setCloneAddress(txtCloneAddres.getText());

        if (!validateRelatedSystemName(systemName)) {
            return;
        }

        monitoredRepositoriesBean.addMonitoredRepository(repositoryBean);
        PreferencesManager.getInstance().persistRepositories();
        PreferencesManager.getInstance().storePreferences(settings);

        RepositoryMonitor.getInstance().addRepositoryToMonitor(repositoryBean);

        if (RepositoryMonitor.getInstance().getState().equals(Thread.State.TIMED_WAITING)) {
            RepositoryMonitor.getInstance().interrupt();
        } else {
            JOptionPane
                .showMessageDialog(
                    this, "The Repository Monitor is current running. This new repository was added to the monitor queue and"
                    + "\nwill be checked as soon as the current activities finish."
                    + "\n\nMeanwhile, remember that the topology and commits graphs will not reflect the current situation.", "Information", JOptionPane
                        .OK_OPTION);
        }

        dispose();
    }

    private void exploreAddressButtonActionPerformed(java.awt.event.ActionEvent evt, JTextField field) {
        JFileChooser fileChooser = new javax.swing.JFileChooser();
        if ((field.getText() != null) &&!"".equals(field.getText())) {
            fileChooser.setCurrentDirectory(new File(field.getText()));
        } else {
            if ((settings.getLastUsedPath() != null) &&!"".equals(settings.getLastUsedPath())) {
                fileChooser.setCurrentDirectory(new File(settings.getLastUsedPath()));
            } else {
                fileChooser.setCurrentDirectory(null);
            }
        }

        fileChooser.setDialogTitle("Select path to clone.");
        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        int    returnVal = fileChooser.showOpenDialog(this);
        String pathChosen;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            pathChosen = file.getAbsolutePath();
            settings.setLastUsedPath(pathChosen);

            if (GitConnector.isValidRepository(pathChosen)) {
                field.setText(file.getAbsolutePath());
                txtRepositoryName.setText(SystemUtils.getFilenameOrLastPath(file.getAbsolutePath()));
            } else {
                JOptionPane.showMessageDialog(txtCloneAddres,
                                              "The specified path does not contain a valid git repository.", "Message",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // </editor-fold>

    private br.uff.ic.dyevc.model.MonitoredRepositories monitoredRepositoriesBean;
    private br.uff.ic.dyevc.model.MonitoredRepository   repositoryBean;

    /**
     * If true, bean will be created. Otherwise it will be modified.
     */
    private boolean                       create;
    private javax.swing.JButton           btnExploreCloneAddress;
    private javax.swing.JButton           btnSaveRepository;
    private javax.swing.JButton           btnCancel;
    private javax.swing.JLabel            lblSystemName;
    private javax.swing.JLabel            lblRepositoryName;
    private javax.swing.JLabel            lblCloneAddress;
    private javax.swing.JTextField        txtCloneAddres;
    private javax.swing.JComboBox<String> cmbSystemName;
    private javax.swing.JTextField        txtRepositoryName;
    private javax.swing.JPanel            pnlTop;
    private javax.swing.JPanel            pnlBottom;

    private void buildUI() {
        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(pnlTop);
        pnlTop.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel5Layout.createSequentialGroup().addContainerGap().addGroup(
                    jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                        lblSystemName, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(
                        lblRepositoryName, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(
                        lblCloneAddress, javax.swing.GroupLayout.Alignment.TRAILING)).addPreferredGap(
                            javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                                cmbSystemName).addComponent(txtRepositoryName).addGroup(
                                javax.swing.GroupLayout.Alignment.TRAILING,
                                jPanel5Layout.createSequentialGroup().addComponent(
                                    btnExploreCloneAddress).addPreferredGap(
                                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                                    txtCloneAddres))).addContainerGap()));
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel5Layout.createSequentialGroup().addContainerGap().addGroup(
                    jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                        lblSystemName).addComponent(
                        cmbSystemName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                            javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                                lblRepositoryName).addComponent(
                                txtRepositoryName, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                                    jPanel5Layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                                        lblCloneAddress).addComponent(
                                        txtCloneAddres, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
                                            btnExploreCloneAddress)).addContainerGap(
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout();
        flowLayout1.setAlignOnBaseline(true);
        pnlBottom.setLayout(flowLayout1);
        pnlBottom.add(btnSaveRepository);
        pnlBottom.add(btnCancel);

        javax.swing.GroupLayout addRepositoryWindowLayout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(addRepositoryWindowLayout);
        addRepositoryWindowLayout.setHorizontalGroup(
            addRepositoryWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                pnlTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE).addComponent(
                    pnlBottom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        addRepositoryWindowLayout.setVerticalGroup(
            addRepositoryWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                addRepositoryWindowLayout.createSequentialGroup().addComponent(
                    pnlTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                        pnlBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE)));
    }

    private StringBuilder createMessage(String type, RepositoryInfo info) {
        StringBuilder message = new StringBuilder();
        message.append("There is a repository for this system in the topology with the same").append(
            "\nhostname and ").append(type).append(":").append("\n\nId: ").append(info.getId()).append(
            "\nHostname: ").append(info.getHostName()).append("\nClone name: ").append(info.getCloneName()).append(
            "\nPath: ").append(info.getClonePath()).append("\n\nWhat would you like to do?");

        return message;
    }

    private int showOption(StringBuilder message) throws HeadlessException {
        Object[] options = { "Use the existing", "Provide new information" };
        int      n       = JOptionPane.showOptionDialog(this, message.toString(), "Confirmation",
                               JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        return n;
    }

    private boolean validateCloneName() throws HeadlessException {

        // Verify if clone name was specified.
        if ((txtRepositoryName.getText() == null) || "".equals(txtRepositoryName.getText())
                || "no name".equalsIgnoreCase(txtRepositoryName.getText())) {
            JOptionPane.showMessageDialog(this, "Clone name is a required field.", "Error", JOptionPane.ERROR_MESSAGE);
            txtRepositoryName.requestFocus();
            txtRepositoryName.selectAll();

            return false;
        }

        // Verify if clone name is unique in this host.
        for (MonitoredRepository rep : MonitoredRepositories.getMonitoredProjects()) {
            if (rep.getName().equalsIgnoreCase(txtRepositoryName.getText())
                    &&!rep.getId().equals(repositoryBean.getId())) {
                JOptionPane.showMessageDialog(
                    this, "There is a clone defined with this name. Please choose another clone name.", "Error",
                    JOptionPane.ERROR_MESSAGE);
                txtRepositoryName.requestFocus();
                txtRepositoryName.selectAll();

                return false;
            }
        }

        return true;
    }

    private boolean validateRepositoryAlreadyExists(String systemName) throws HeadlessException {

        // Verify if there is a repository in the database for this system / host / clone
        RepositoryInfo   repoSameClone;
        RepositoryInfo   repoSamePath;
        RepositoryFilter filterSameClone = new RepositoryFilter();
        filterSameClone.setSystemName(systemName);
        filterSameClone.setHostName(SystemUtils.getLocalHostname());
        filterSameClone.setCloneName(txtRepositoryName.getText());

        // Verify if there is a repository in the database for this system / host / path
        RepositoryFilter filterSamePath = new RepositoryFilter();
        filterSamePath.setSystemName(systemName);
        filterSamePath.setHostName(SystemUtils.getLocalHostname());
        filterSamePath.setClonePath(StringUtils.normalizePath(txtRepositoryName.getText()));
        TopologyDAO               dao = new TopologyDAO();
        ArrayList<RepositoryInfo> listSameClone;
        ArrayList<RepositoryInfo> listSamePath;
        try {
            listSameClone = dao.getRepositoriesByQuery(filterSameClone);
            listSamePath  = dao.getRepositoriesByQuery(filterSamePath);
        } catch (ServiceException ex) {
            StringWriter s = new StringWriter();
            PrintWriter  p = new PrintWriter(s);
            ex.printStackTrace(p);
            JOptionPane.showMessageDialog(this,
                                          "It was not possible to contact the database due to the following exception."
                                          + "\nPlease try again later.\n\n" + s.toString(), "Error",
                                              JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!listSameClone.isEmpty()) {
            repoSameClone = listSameClone.get(0);
            StringBuilder message = createMessage("clone name", repoSameClone);
            int           n       = showOption(message);
            if (n == JOptionPane.NO_OPTION) {
                return false;
            } else {
                repositoryBean.setId(repoSameClone.getId());
                repositoryBean.setLastChanged(repoSameClone.getLastChanged());
                txtCloneAddres.setText(repoSameClone.getClonePath());
            }
        }

        if (!listSamePath.isEmpty()) {
            repoSamePath = listSamePath.get(0);
            StringBuilder message = createMessage("path", repoSamePath);
            int           n       = showOption(message);
            if (n == JOptionPane.NO_OPTION) {
                return false;
            } else {
                repositoryBean.setId(repoSamePath.getId());
                txtRepositoryName.setText(repoSamePath.getCloneName());
            }
        }

        return true;
    }

    private boolean validateRelatedSystemName(String systemName) throws HeadlessException {
        converter = new RepositoryConverter(repositoryBean);

        try {
            String relatedSystem = converter.getRelatedSystem();
            if (!relatedSystem.equals(systemName)) {
                JOptionPane.showMessageDialog(this,
                                              "The  repository you want to monitor relates with a different system."
                                              + "\nThe system name was changed to <" + relatedSystem
                                              + "> to reflect it and cannot be changed."
                                              + "\nClick on save again to confirm it, or change the data.", "Warning",
                                                  JOptionPane.WARNING_MESSAGE);
                cmbSystemName.setSelectedItem(relatedSystem);

                return false;
            }
        } catch (DyeVCException ex) {
            JOptionPane.showMessageDialog(
                this,
                "An error occurred while validating your information."
                + " Please try again later.  Access \"View -> Console Window\" for details.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return true;
    }

    private void updateTopology() throws DyeVCException {
        TopologyDAO dao         = new TopologyDAO();
        Date        lastChanged = dao.upsertRepository(converter.toRepositoryInfo());
        repositoryBean.setLastChanged(lastChanged);
        dao.upsertRepositories(converter.getRelatedNewList());
    }
}
