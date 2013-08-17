package br.uff.ic.dyevc.gui.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * This class renders the headers of the repository table.
 *
 * @author Cristiano
 */
public class HeaderRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = -1767445271244335267L;

    public HeaderRenderer() {
        setFont(new Font(getFont().getName(), Font.BOLD, 14));
        setForeground(Color.BLUE);
        setBorder(BorderFactory.createEtchedBorder());
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        setText(value.toString());
        setHorizontalAlignment(JLabel.CENTER);
        return this;
    }
}