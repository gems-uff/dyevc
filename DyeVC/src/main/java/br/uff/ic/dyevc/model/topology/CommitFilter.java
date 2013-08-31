package br.uff.ic.dyevc.model.topology;

import br.uff.ic.dyevc.persistence.GenericFilter;
import java.util.Date;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Filter to be used in commit queries. All filters are inclusive (and operator
 * is implicit) and null fields are discarded.
 * @author Cristiano
 */

public class CommitFilter extends GenericFilter {
    @JsonProperty(value = "_id")
    private String hash;
    private String systemName;
    private Date commitDate;
    private String committer;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }
    
}
