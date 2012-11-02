package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.model.MonitoredRepository;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
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
        StringBuilder tooltip = new StringBuilder();
        tooltip.append("<html>")
                .append("<b>ID: </b>").append(repository.getId())
                .append("<br><b>Address: </b>").append(repository.getCloneAddress())
                .append("</html>");
        listItem.setToolTipText(tooltip.toString());

        if (pad) {
            listItem.setBorder(padBorder);
        }

        return listItem;
    }
}
