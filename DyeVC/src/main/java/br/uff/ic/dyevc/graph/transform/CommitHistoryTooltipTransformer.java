package br.uff.ic.dyevc.graph.transform;

import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.utils.DateUtil;
import org.apache.commons.collections15.Transformer;

/**
 * Transforms a CommitInfo in a string to be used as a tooltip.
 *
 * @author Cristiano
 */
public class CommitHistoryTooltipTransformer implements Transformer<CommitInfo, String>{

    /**
     * Returns a string to be used as a tooltip in a graph
     * @param ci Info to be transformed in tooltip
     * @return the info formatted as a tooltip
     */
    @Override
    public String transform(CommitInfo ci) {
        StringBuilder details = new StringBuilder("<html>");
        if (ci.getParentsCount() == 0) {
            details.append("<b>This is the first commit!</b>").append("<br><br>");
        }
        if (ci.getChildrenCount() == 0) {
            details.append("<b>This is a branch's head!</b>").append("<br><br>");
        }
        details.append("<b>Commit id: </b>").append(ci.getId()).append("<br>");
        details.append("<b>time: </b>")
                .append(DateUtil.format(ci.getCommitDate(), "yyyy-MM-dd HH:mm:ss.SSS"))
                .append("<br>");
        details.append("<b>commiter: </b>")
                .append(ci.getCommitter())
                .append("<br>");
        details.append("<b>message: </b>")
                .append(ci.getShortMessage())
                .append("</html>");
        return details.toString();
    }
}
