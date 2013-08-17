package br.uff.ic.dyevc.graph.transform.topology;

import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JComponent;

@SuppressWarnings("serial")
public class TopologyVertexLabelRenderer extends DefaultVertexLabelRenderer {

    public TopologyVertexLabelRenderer() {
        super(Color.BLACK);
    }

    @Override
    public <V> Component getVertexLabelRendererComponent(JComponent vv, Object value, Font font, boolean isSelected, V vertex) {
        super.setBackground(vv.getBackground());
        setFont(new Font(vv.getFont().getName(), Font.BOLD, vv.getFont().getSize()));
        setIcon(null);
        setBorder(noFocusBorder);
        setValue(value);
        return this;
    }
}
