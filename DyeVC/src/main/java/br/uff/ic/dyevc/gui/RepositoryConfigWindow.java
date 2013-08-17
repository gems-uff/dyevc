package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.MonitoredRepositories;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.topology.RepositoryFilter;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.model.topology.Topology;
import br.uff.ic.dyevc.monitor.TopologyMonitor;
import br.uff.ic.dyevc.persistence.TopologyDAO;
import br.uff.ic.dyevc.tools.vcs.git.GitConnector;
import br.uff.ic.dyevc.utils.PreferencesUtils;
import br.uff.ic.dyevc.utils.RepositoryConverter;
import br.uff.ic.dyevc.utils.StringUtils;
import br.uff.ic.dyevc.utils.SystemUtils;
import java.awt.HeadlessException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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
    private Topology topology;
    private TopologyMonitor topologyMonitor;

    /**
     * Creates new form RepositoryConfigWindow
     */
    public RepositoryConfigWindow(MonitoredRepositories monBean, MonitoredRepository repository, TopologyMonitor monitor) throws DyeVCException {
        if (monBean == null) {
            LoggerFactory.getLogger(RepositoryConfigWindow.class).error("Received a null list of monitored repositories");
            throw new DyeVCException("Received a null list of monitored repositories");
        }
        monitoredRepositoriesBean = monBean;
        topologyMonitor = monitor;
        if (repository != null) {
            create = false;
            repositoryBean = repository;
        } else {
            create = true;
            repositoryBean = new MonitoredRepository(StringUtils.generateRepositoryId());
        }
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="initComponents">
    @SuppressWarnings("unchecked")
    private void initComponents() {
        settings = PreferencesUtils.loadPreferences();
        try {
            topology = new TopologyDAO().readTopology();
        } catch (ServiceException ex) {
            LoggerFactory.getLogger(RepositoryConfigWindow.class).warn("Could not retrieve topology from database. It will not be possible to show the list of known systems.", ex);
            topology = new Topology();
        }

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        if (create) {
            setTitle("Creating a new monitoring configuration");
        } else {
            setTitle("Changing a monitoring configuration");
        }
        setSize(new java.awt.Dimension(528, 180));
        setMinimumSize(new java.awt.Dimension(528, 180));
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);

        pnlTop = new javax.swing.JPanel();
        pnlBottom = new javax.swing.JPanel();
        pnlTop.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblSystemName = new javax.swing.JLabel();
        lblSystemName.setText("Repository Name:");
        lblSystemName.setToolTipText("Enter the globally known name to recognize this repository.");
        lblRepositoryName = new javax.swing.JLabel();
        lblRepositoryName.setText("Clone Name:");
        lblRepositoryName.setToolTipText("Enter a name to recognize this clone in your machine.");
        lblCloneAddress = new javax.swing.JLabel();
        lblCloneAddress.setText("Repository Address:");
        lblCloneAddress.setToolTipText("Click on the button to select the path to a local repository you want to monitor.");

        cmbSystemName = new javax.swing.JComboBox<String>(topology.getSystems().toArray(new String[0]));
        cmbSystemName.setEditable(true);
        cmbSystemName.setSelectedItem(repositoryBean.getSystemName());
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
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="event handlers">    
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void btnSaveRepositoryActionPerformed(java.awt.event.ActionEvent evt) {
        //Verify if system name was specified.
        if (cmbSystemName.getSelectedItem() == null || "".equals(cmbSystemName.getSelectedItem().toString()) || "no name".equalsIgnoreCase(cmbSystemName.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(this, "Repository name is a required field.", "Error", JOptionPane.ERROR_MESSAGE);
            cmbSystemName.requestFocus();
            return;
        }
        String systemName = cmbSystemName.getSelectedItem().toString().toLowerCase();

        //Verify if clone name was specified.
        if (txtRepositoryName.getText() == null || "".equals(txtRepositoryName.getText()) || "no name".equalsIgnoreCase(txtRepositoryName.getText())) {
            JOptionPane.showMessageDialog(this, "Clone name is a required field.", "Error", JOptionPane.ERROR_MESSAGE);
            txtRepositoryName.requestFocus();
            txtRepositoryName.selectAll();
            return;
        }

        //Verify if clone name is unique in this host.
        for (MonitoredRepository rep : MonitoredRepositories.getMonitoredProjects()) {
            if (rep.getName().equalsIgnoreCase(txtRepositoryName.getText()) && !rep.getId().equals(repositoryBean.getId())) {
                JOptionPane.showMessageDialog(this, "There is a clone defined with this name. Please choose another clone name.", "Error", JOptionPane.ERROR_MESSAGE);
                txtRepositoryName.requestFocus();
                txtRepositoryName.selectAll();
                return;
            }
        }

        //Verify if there is a repository in the database for this system / host / clone
        RepositoryInfo repoSameClone;
        RepositoryInfo repoSamePath;
        RepositoryFilter filterSameClone = new RepositoryFilter();
        filterSameClone.setSystemName(systemName);
        filterSameClone.setHostName(SystemUtils.getLocalHostname());
        filterSameClone.setCloneName(txtRepositoryName.getText());

        //Verify if there is a repository in the database for this system / host / path
        RepositoryFilter filterSamePath = new RepositoryFilter();
        filterSamePath.setSystemName(systemName);
        filterSamePath.setHostName(SystemUtils.getLocalHostname());
        filterSamePath.setClonePath(StringUtils.normalizePath(txtRepositoryName.getText()));

        TopologyDAO dao = new TopologyDAO();
        ArrayList<RepositoryInfo> listSameClone;
        ArrayList<RepositoryInfo> listSamePath;
        try {
            listSameClone = dao.getRepositoriesByQuery(filterSameClone);
            listSamePath = dao.getRepositoriesByQuery(filterSamePath);
        } catch (ServiceException ex) {
            StringWriter s = new StringWriter();
            PrintWriter p = new PrintWriter(s);
            ex.printStackTrace(p);
            JOptionPane.showMessageDialog(this, "It was not possible to contact the database due to the following exception."
                    + "\nPlease try again later.\n\n" + s.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!listSameClone.isEmpty()) {
            repoSameClone = listSameClone.get(0);
            StringBuilder message = createMessage("clone name", repoSameClone);

            int n = showOption(message);
            if (n == JOptionPane.NO_OPTION) {
                return;
            } else {
                repositoryBean.setId(repoSameClone.getId());
                txtCloneAddres.setText(repoSameClone.getClonePath());
            }
        }

        if (!listSamePath.isEmpty()) {
            repoSamePath = listSamePath.get(0);
            StringBuilder message = createMessage("path", repoSamePath);

            int n = showOption(message);
            if (n == JOptionPane.NO_OPTION) {
                return;
            } else {
                repositoryBean.setId(repoSamePath.getId());
                txtRepositoryName.setText(repoSamePath.getCloneName());
            }
        }

        repositoryBean.setSystemName(systemName);
        repositoryBean.setName(txtRepositoryName.getText());
        repositoryBean.setCloneAddress(txtCloneAddres.getText());
        try {
            updateTopology(repositoryBean);
        } catch (DyeVCException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while trying to include a repository in the topology."
                    + " Please try again later.  See the log for details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        monitoredRepositoriesBean.addMonitoredRepository(repositoryBean);
        PreferencesUtils.persistRepositories();
        PreferencesUtils.storePreferences(settings);
        if (topologyMonitor.getState().equals(Thread.State.TIMED_WAITING)) {
            topologyMonitor.setRepositoryToMonitor(repositoryBean);
            topologyMonitor.interrupt();
        } else {
            JOptionPane.showMessageDialog(this, "Started monitoring clone <" + repositoryBean.getName()
                    + "> with id <" + repositoryBean.getId() + ">.\nTopology will be updated on the next topology monitor cycle.",
                    "Information", JOptionPane.OK_OPTION);
        }
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
    private boolean create;
    private javax.swing.JButton btnExploreCloneAddress;
    private javax.swing.JButton btnSaveRepository;
    private javax.swing.JButton btnCancel;
    private javax.swing.JLabel lblSystemName;
    private javax.swing.JLabel lblRepositoryName;
    private javax.swing.JLabel lblCloneAddress;
    private javax.swing.JTextField txtCloneAddres;
    private javax.swing.JComboBox<String> cmbSystemName;
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
                .addComponent(lblSystemName, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(lblRepositoryName, javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(lblCloneAddress, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(cmbSystemName)
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
                .addComponent(lblSystemName)
                .addComponent(cmbSystemName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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

    private StringBuilder createMessage(String type, RepositoryInfo info) {
        StringBuilder message = new StringBuilder();
        message.append("There is a repository for this system in the topology with the same")
                .append("\nhostname and ").append(type).append(":")
                .append("\n\nId: ").append(info.getId())
                .append("\nHostname: ").append(info.getHostName())
                .append("\nClone name: ").append(info.getCloneName())
                .append("\nPath: ").append(info.getClonePath())
                .append("\n\nWhat would you like to do?");
        return message;
    }

    private int showOption(StringBuilder message) throws HeadlessException {
        Object[] options = {"Use the existing",
            "Provide new information"};
        int n = JOptionPane.showOptionDialog(this,
                message.toString(),
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        return n;
    }

    private void updateTopology(MonitoredRepository monitoredRepository) throws DyeVCException {
        TopologyDAO dao = new TopologyDAO();
        RepositoryConverter converter = new RepositoryConverter(monitoredRepository);
        dao.upsertRepository(converter.toRepositoryInfo());
        dao.upsertRepositories(converter.getRelatedNew());
    }
}
