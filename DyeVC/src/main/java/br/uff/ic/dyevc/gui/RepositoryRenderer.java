package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.RepositoryStatus;
import br.uff.ic.dyevc.utils.DateUtil;
import br.uff.ic.dyevc.utils.ImageUtils;
import java.awt.Component;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

class RepositoryRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = -1767445271244335267L;
    private boolean pad;
    private Border padBorder = new EmptyBorder(3, 3, 3, 3);

    RepositoryRenderer(boolean pad) {
        this.pad = pad;
    }

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        Component c = super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        JLabel listItem = (JLabel) c;
        MonitoredRepository repository = (MonitoredRepository) value;
        listItem.setText(repository.getName());

        RepositoryStatus repStatus = repository.getRepStatus();

        StringBuilder tooltip = new StringBuilder();
        tooltip.append("<html>")
                .append("<b>ID: </b>").append(repository.getId())
                .append("<br><b>Address: </b>").append(repository.getCloneAddress())
                .append("<br><b>Last checked on: </b>");

        if (repStatus.getLastCheckedTime() == null) {
            tooltip.append("not yet checked in this session.")
                    .append("<br><br>Status for this repository is not ready yet.");
            listItem.setIcon(ImageUtils.getInstance().getIcon("question_32.png"));
        } else {
            tooltip.append(DateUtil.format(repStatus.getLastCheckedTime(), "yyyy-MM-dd HH:mm:ss"));
            if (repStatus.isInvalid()) {
                listItem.setIcon(ImageUtils.getInstance().getIcon("nocheck_32.png"));
                tooltip.append("<br><br>Repository location is invalid. Please check or remove it from the configured repositories list.");
            } else {
                if (repStatus.getNonSyncedBranchesCount() == 0) {
                    listItem.setIcon(ImageUtils.getInstance().getIcon("check_32.png"));
                    tooltip.append("<br><br>Repository is in sync with all remotes.");
                } else {
                    listItem.setIcon(ImageUtils.getInstance().getIcon("aheadbehind_ylw_32.png"));
                    appendNonSyncedMessages(repStatus.getNonSyncedRepositoryBranches(), tooltip);
                }
            }
        }

        tooltip.append("</html>");
        listItem.setToolTipText(tooltip.toString());
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        if (pad) {
            listItem.setBorder(padBorder);
        }

        return listItem;
    }

    private void appendNonSyncedMessages(List<BranchStatus> nonSyncedRepositoryBranches, StringBuilder tooltip) {
        for (Iterator<BranchStatus> it = nonSyncedRepositoryBranches.iterator(); it.hasNext();) {
            BranchStatus branchStatus = it.next();

            tooltip.append("<br><br><img src='");
            if (branchStatus.getStatus() == BranchStatus.STATUS_AHEAD) {
                tooltip.append(RepositoryRenderer.class.getResource("/br/uff/ic/dyevc/images/ahead_ylw_16.png"))
                        .append("'/> <b>ahead:</b> ").append(branchStatus.getAhead());
            } else if (branchStatus.getStatus() == BranchStatus.STATUS_BEHIND) {
                tooltip.append(RepositoryRenderer.class.getResource("/br/uff/ic/dyevc/images/behind_ylw_16.png"))
                        .append("'/> <b>behind:</b> ").append(branchStatus.getBehind());
            } else {
                tooltip.append(RepositoryRenderer.class.getResource("/br/uff/ic/dyevc/images/aheadbehind_ylw_16.png"))
                        .append("'/> <b>ahead:</b> ").append(branchStatus.getAhead())
                        .append("&nbsp;&nbsp;&nbsp;<b>behind:</b> ").append(branchStatus.getBehind());
            }
            tooltip.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                    .append("<b>local branch:</b> ").append(branchStatus.getRepositoryBranch())
                    .append(" <b>remote branch:</b> ").append(branchStatus.getReferencedRepositoryBranch())
                    .append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                    .append("<b>remote url:</b> ").append(branchStatus.getReferencedRepositoryUrl());
        }
    }
}
