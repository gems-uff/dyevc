package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.RepositoryStatus;
import br.uff.ic.dyevc.utils.DateUtil;
import br.uff.ic.dyevc.utils.ImageUtils;
import java.awt.Component;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This class renders information about the monitored repositories. Information 
 * is rendered as a table.
 * 
 * @author Cristiano
 */
class RepositoryRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = -1767445271244335267L;
    private final Border padBorder = new EmptyBorder(3, 3, 3, 3);

    @Override
    public Component getTableCellRendererComponent(
            JTable table, 
            Object value, 
            boolean isSelected, 
            boolean hasFocus, 
            int row, 
            int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(JLabel.CENTER);
        setValue("");
        
        MonitoredRepository repository = (MonitoredRepository) value;
        RepositoryStatus repStatus = repository.getRepStatus();

        StringBuilder tooltip = new StringBuilder();
        tooltip.append("<html>")
                .append("<b>ID: </b>").append(repository.getId())
                .append("<br><b>Address: </b>").append(repository.getCloneAddress())
                .append("<br><b>Last checked on: </b>");

        if (repStatus.getLastCheckedTime() == null) {
            tooltip.append("not yet checked in this session.")
                    .append("<br><br>Status for this repository is not ready yet.");
            setIcon(ImageUtils.getInstance().getIcon("question_32.png"));
        } else {
            tooltip.append(DateUtil.format(repStatus.getLastCheckedTime(), "yyyy-MM-dd HH:mm:ss"));
            if (repStatus.isInvalid()) {
                setIcon(ImageUtils.getInstance().getIcon("nocheck_32.png"));
                tooltip.append("<br><br><b>Repository could not be checked.</b> Message received from monitor: <br>")
                        .append(repStatus.getInvalidMessage());
            } else {
                if ((repStatus.getNonSyncedBranchesCount() == 0) && (repStatus.getInvalidBranchesCount() == 0)) {
                    setIcon(ImageUtils.getInstance().getIcon("check_32.png"));
                    tooltip.append("<br><br>Repository is in sync with all remotes.");
                } else {
                    if (repStatus.getAheadCount() > 0 && repStatus.getBehindCount() > 0) {
                        setIcon(ImageUtils.getInstance().getIcon("aheadbehind_ylw_32.png"));
                    } else if (repStatus.getAheadCount() > 0 ) {
                        setIcon(ImageUtils.getInstance().getIcon("ahead_ylw_32.png"));
                    } else {
                        setIcon(ImageUtils.getInstance().getIcon("behind_ylw_32.png"));
                    }
                    appendMessages(repStatus, tooltip);
                }
            }
        }

        tooltip.append("</html>");
        setToolTipText(tooltip.toString());
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        setBorder(padBorder);

        return this;
    }

    /**
     * Appends information regarding non-synchronized and invalid branches as a tooltip.
     * @param status
     *          status from which information will be appended.
     * @param tooltip
     *          the StringBuilder that holds messages to be displayed.
     */
    private void appendMessages(RepositoryStatus status, StringBuilder tooltip) {
        for (Iterator<BranchStatus> it = status.getNonSyncedRepositoryBranches().iterator(); it.hasNext();) {
            BranchStatus branchStatus = it.next();

            tooltip.append("<br><br><img src='");
            if (branchStatus.getStatus() == BranchStatus.STATUS_AHEAD) {
                tooltip.append(RepositoryRenderer.class.getResource("/br/uff/ic/dyevc/images/ahead_ylw_16.png"))
                        .append("'/> <b>Ahead:</b> ").append(branchStatus.getAhead());
            } else if (branchStatus.getStatus() == BranchStatus.STATUS_BEHIND) {
                tooltip.append(RepositoryRenderer.class.getResource("/br/uff/ic/dyevc/images/behind_ylw_16.png"))
                        .append("'/> <b>Behind:</b> ").append(branchStatus.getBehind());
            } else {
                tooltip.append(RepositoryRenderer.class.getResource("/br/uff/ic/dyevc/images/aheadbehind_ylw_16.png"))
                        .append("'/> <b>Ahead:</b> ").append(branchStatus.getAhead())
                        .append("&nbsp;&nbsp;&nbsp;<b>Behind:</b> ").append(branchStatus.getBehind());
            }
            tooltip.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                    .append("<b>Local branch:</b> ").append(branchStatus.getRepositoryBranch())
                    .append(" <b>Remote branch:</b> ").append(branchStatus.getMergeSpec())
                    .append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                    .append("<b>Remote url:</b> ").append(branchStatus.getReferencedRepositoryUrl());
        }
        for (Iterator<BranchStatus> it = status.getInvalidRepositoryBranches().iterator(); it.hasNext();) {
            BranchStatus branchStatus = it.next();

            tooltip.append("<br><br><img src='")
                    .append(RepositoryRenderer.class.getResource("/br/uff/ic/dyevc/images/question_16.png"))
                    .append("'/> <b>This branch could not be evaluated.</b>")
                    .append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                    .append("<b>Local branch:</b> ").append(branchStatus.getRepositoryBranch())
                    .append(" <b>Remote branch:</b> ").append(branchStatus.getMergeSpec())
                    .append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                    .append("<b>Remote url:</b> ").append(branchStatus.getReferencedRepositoryUrl());
        }
    }
}
