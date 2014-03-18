package br.uff.ic.dyevc.gui.main;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.model.MonitoredRepository;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Component;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 * This class renders String information regarding the monitored repositories.
 *
 * @author Cristiano
 */
public class StringRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = -1767445271244335267L;
    private final Border      padBorder        = new EmptyBorder(3, 3, 3, 3);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        MonitoredRepository repository  = (MonitoredRepository)table.getValueAt(row, 0);
        String              tooltip     = TooltipRenderer.renderTooltipFor(repository);
        String              stringValue = (String)value;
        switch (column) {
        case 1 :
            if (stringValue.equals("") || stringValue.equals("no name")) {
                setValue("Not specified");
                setForeground(Color.RED);
            } else {
                setForeground(UIManager.getColor("Label.foreground"));
            }

            break;

        default :
            setForeground(UIManager.getColor("Label.foreground"));
        }

        setToolTipText(tooltip);
        ToolTipManager.sharedInstance().setDismissDelay(15000);

        return this;
    }
}
