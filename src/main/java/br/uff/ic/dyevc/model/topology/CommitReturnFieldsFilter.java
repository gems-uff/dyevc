package br.uff.ic.dyevc.model.topology;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.persistence.GenericFilter;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Filter to be used in commit queries. All filters are inclusive (and operator
 * is implicit) and null fields should be discarded.
 * @author Cristiano
 */

public class CommitReturnFieldsFilter extends GenericFilter {
    @JsonProperty(value = "_id")
    private String hash;
    private String systemName;
    private String commitDate;
    private String committer;
    private String tracked;
    private String foundIn;

    public String getFoundIn() {
        return foundIn;
    }

    public void setFoundIn(String foundIn) {
        this.foundIn = foundIn;
    }

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

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public String getTracked() {
        return tracked;
    }

    public void setTracked(String tracked) {
        this.tracked = tracked;
    }
}
