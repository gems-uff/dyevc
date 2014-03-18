package br.uff.ic.dyevc.gui.main;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.BranchStatus;
import br.uff.ic.dyevc.model.MonitoredRepository;
import br.uff.ic.dyevc.model.RepositoryStatus;
import br.uff.ic.dyevc.utils.DateUtil;
import br.uff.ic.dyevc.utils.ImageUtils;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Component;

import java.util.Iterator;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.ToolTipManager;

/**
 * This class renders information about the monitored repositories. Information
 * is rendered as a table.
 *
 * @author Cristiano
 */
public class RepositoryRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = -1767445271244335267L;
    private final Border      padBorder        = new EmptyBorder(3, 3, 3, 3);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(JLabel.CENTER);
        setValue("");

        MonitoredRepository repository = (MonitoredRepository)value;
        RepositoryStatus    repStatus  = repository.getRepStatus();
        String              tooltip    = TooltipRenderer.renderTooltipFor(repository);

        if (repStatus.getLastCheckedTime() == null) {
            setIcon(ImageUtils.getInstance().getIcon("question_32.png"));
        } else {
            if (repStatus.isInvalid()) {
                setIcon(ImageUtils.getInstance().getIcon("nocheck_32.png"));
            } else {
                if ((repStatus.getNonSyncedBranchesCount() == 0) && (repStatus.getInvalidBranchesCount() == 0)) {
                    setIcon(ImageUtils.getInstance().getIcon("check_32.png"));
                } else {
                    if ((repStatus.getAheadCount() > 0) && (repStatus.getBehindCount() > 0)) {
                        setIcon(ImageUtils.getInstance().getIcon("aheadbehind_ylw_32.png"));
                    } else if (repStatus.getAheadCount() > 0) {
                        setIcon(ImageUtils.getInstance().getIcon("ahead_ylw_32.png"));
                    } else {
                        setIcon(ImageUtils.getInstance().getIcon("behind_ylw_32.png"));
                    }
                }
            }
        }

        setToolTipText(tooltip);
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        setBorder(padBorder);

        return this;
    }
}
