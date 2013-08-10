package br.uff.ic.dyevc.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This class renders String information regarding the monitored repositories.
 * 
 * @author Cristiano
 */
class StringRenderer extends DefaultTableCellRenderer {

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

        String stringValue = (String)value;
        switch (column) {
            case 1:
                if (stringValue.equals("") || stringValue.equals("no name")) {
                    setValue("Not specified");
                    setForeground(Color.RED);
                } else {
                    setForeground(UIManager.getColor("Label.foreground"));
                }
                break;
            default:
                setForeground(UIManager.getColor("Label.foreground"));
        }
        return this;
    }
}
