package br.uff.ic.dyevc.utils;

import br.uff.ic.dyevc.model.CommitInfo;
import java.util.Comparator;

/**
 * Compares two commit infos by their dates
 * @author Cristiano
 */
public class CommitInfoDateComparator implements Comparator<CommitInfo> {

    @Override
    public int compare(CommitInfo o1, CommitInfo o2) {
        return (o1.getCommitDate().before(o2.getCommitDate()) ? -1 : (o1 == o2 ? 0 : 1));
    }
}
