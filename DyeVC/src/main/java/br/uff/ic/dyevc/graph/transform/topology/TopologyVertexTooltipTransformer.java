package br.uff.ic.dyevc.graph.transform.topology;

import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.collections15.Transformer;

/**
 * Transforms a Repository in a string to be used as the tooltip of a vertex.
 *
 * @author Cristiano
 */
public class TopologyVertexTooltipTransformer implements Transformer<Object, String> {

    /**
     * Returns a string to be used as the tooltip
     *
     * @param o Info to be labeled
     * @return the info formatted as a tooltip
     */
    @Override
    public String transform(Object o) {
        StringBuilder tooltip = new StringBuilder();
        if ((o instanceof Graph)) {
            tooltip.append("<html>This is a collapsed node which groups a total of <B>");
            tooltip.append(((Graph) o).getVertexCount());
            tooltip.append("</B> nodes.</html>");
        }
        if (o instanceof RepositoryInfo) {
            RepositoryInfo info = (RepositoryInfo) o;
            tooltip.append("<html>");
            tooltip.append("<b>RepositoryInfo{</b>")
                    .append("<br>&nbsp;&nbsp;&nbsp;<b>id = </b>").append(info.getId())
                    .append("<br>&nbsp;&nbsp;&nbsp;<b>systemName = </b>").append(info.getSystemName())
                    .append("<br>&nbsp;&nbsp;&nbsp;<b>hostName = </b>").append(info.getHostName())
                    .append("<br>&nbsp;&nbsp;&nbsp;<b>cloneName = </b>").append(info.getCloneName())
                    .append("<br>&nbsp;&nbsp;&nbsp;<b>path = </b>").append(info.getClonePath());
            if (info.getPullsFrom().isEmpty()
                    && info.getPushesTo().isEmpty()) {
                tooltip.append("<br><br><b>This is a central repository.<br>It does not push to or pull from any other known repository.</b>");
            }
            tooltip.append("</html>");
        }
        return tooltip.toString();
    }
}
