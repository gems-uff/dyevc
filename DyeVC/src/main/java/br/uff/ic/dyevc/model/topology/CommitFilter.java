package br.uff.ic.dyevc.model.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.persistence.GenericFilter;

import org.codehaus.jackson.annotate.JsonProperty;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;

/**
 * Filter to be used in commit queries. All filters are inclusive (and operator
 * is implicit) and null fields should be discarded.
 * @author Cristiano
 */

public class CommitFilter extends GenericFilter {
    @JsonProperty(value = "_id")
    private String  hash;
    private String  systemName;
    private Date    commitDate;
    private String  committer;
    private Boolean tracked;

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

    public Boolean isTracked() {
        return tracked;
    }

    public void setTracked(Boolean tracked) {
        this.tracked = tracked;
    }
}
